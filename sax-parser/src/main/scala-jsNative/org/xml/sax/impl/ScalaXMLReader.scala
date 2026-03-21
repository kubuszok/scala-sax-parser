package org.xml.sax.impl

import org.xml.sax._
import org.xml.sax.ext.{DeclHandler, LexicalHandler, Locator2}
import org.xml.sax.helpers.AttributesImpl

import java.io.{InputStreamReader, Reader, StringReader}

/**
 * A pure-Scala XMLReader implementation that parses XML 1.0 documents
 * and fires SAX events. This enables scala-xml's XMLLoader/FactoryAdapter
 * to work on Scala.js and Scala Native.
 */
class ScalaXMLReader extends XMLReader {
  private var contentHandler: ContentHandler = _
  private var dtdHandler: DTDHandler = _
  private var entityResolver: EntityResolver = _
  private var errorHandler: ErrorHandler = _
  private var lexicalHandler: LexicalHandler = _
  private var declHandler: DeclHandler = _

  // Features
  private var namespacesFeature: Boolean = true
  private var namespacePrefixesFeature: Boolean = false

  // Entity definitions (populated from DTD internal subset)
  private val entities = scala.collection.mutable.Map[String, String](
    "amp"  -> "&",
    "lt"   -> "<",
    "gt"   -> ">",
    "apos" -> "'",
    "quot" -> "\""
  )

  // Parser state
  private var reader: Reader = _
  private var current: Int = -1
  private var line: Int = 1
  private var col: Int = 0
  private var publicId: String = _
  private var systemId: String = _

  private val locator: Locator2 = new Locator2 {
    def getPublicId(): String = publicId
    def getSystemId(): String = systemId
    def getLineNumber(): Int = line
    def getColumnNumber(): Int = col
    def getXMLVersion(): String = "1.0"
    def getEncoding(): String = "UTF-8"
  }

  // XMLReader interface
  override def getFeature(name: String): Boolean = name match {
    case "http://xml.org/sax/features/namespaces"        => namespacesFeature
    case "http://xml.org/sax/features/namespace-prefixes" => namespacePrefixesFeature
    case "http://xml.org/sax/features/external-general-entities"   => false
    case "http://xml.org/sax/features/external-parameter-entities" => false
    case "http://xml.org/sax/features/validation"                  => false
    case _ => throw new SAXNotRecognizedException(s"Feature: $name")
  }

  override def setFeature(name: String, value: Boolean): Unit = name match {
    case "http://xml.org/sax/features/namespaces"        => namespacesFeature = value
    case "http://xml.org/sax/features/namespace-prefixes" => namespacePrefixesFeature = value
    case "http://xml.org/sax/features/external-general-entities"   => ()
    case "http://xml.org/sax/features/external-parameter-entities" => ()
    case "http://xml.org/sax/features/validation"                  => ()
    case "http://apache.org/xml/features/nonvalidating/load-external-dtd" => ()
    case "http://apache.org/xml/features/disallow-doctype-decl"    => ()
    case _ => throw new SAXNotRecognizedException(s"Feature: $name")
  }

  override def getProperty(name: String): AnyRef = name match {
    case "http://xml.org/sax/properties/lexical-handler"      => lexicalHandler
    case "http://xml.org/sax/properties/declaration-handler"  => declHandler
    case _ => throw new SAXNotRecognizedException(s"Property: $name")
  }

  override def setProperty(name: String, value: AnyRef): Unit = name match {
    case "http://xml.org/sax/properties/lexical-handler" =>
      lexicalHandler = value.asInstanceOf[LexicalHandler]
    case "http://xml.org/sax/properties/declaration-handler" =>
      declHandler = value.asInstanceOf[DeclHandler]
    case _ => throw new SAXNotRecognizedException(s"Property: $name")
  }

  override def setEntityResolver(resolver: EntityResolver): Unit = entityResolver = resolver
  override def getEntityResolver(): EntityResolver = entityResolver
  override def setDTDHandler(handler: DTDHandler): Unit = dtdHandler = handler
  override def getDTDHandler(): DTDHandler = dtdHandler
  override def setContentHandler(handler: ContentHandler): Unit = contentHandler = handler
  override def getContentHandler(): ContentHandler = contentHandler
  override def setErrorHandler(handler: ErrorHandler): Unit = errorHandler = handler
  override def getErrorHandler(): ErrorHandler = errorHandler

  override def parse(systemId: String): Unit = {
    val source = new InputSource(systemId)
    parse(source)
  }

  override def parse(input: InputSource): Unit = {
    this.publicId = input.getPublicId()
    this.systemId = input.getSystemId()

    reader = if (input.getCharacterStream() != null) {
      input.getCharacterStream()
    } else if (input.getByteStream() != null) {
      val encoding = if (input.getEncoding() != null) input.getEncoding() else "UTF-8"
      new InputStreamReader(input.getByteStream(), encoding)
    } else if (input.getSystemId() != null) {
      throw new SAXException("Cannot resolve systemId: " + input.getSystemId() + " (no file I/O on this platform)")
    } else {
      throw new SAXException("InputSource has no character stream, byte stream, or system ID")
    }

    line = 1
    col = 0
    advance()

    if (contentHandler != null) {
      contentHandler.setDocumentLocator(locator)
      contentHandler.startDocument()
    }

    // Skip BOM if present
    if (current == 0xFEFF) advance()

    // Handle optional XML declaration
    skipXmlDeclaration()

    // Parse document content (prolog misc, root element, trailing misc)
    val nsCtx = new NamespaceContext()
    parseMiscAndElement(nsCtx)

    if (contentHandler != null) contentHandler.endDocument()
  }

  // ---- Character reading ----

  private def advance(): Unit = {
    current = reader.read()
    if (current == '\n') {
      line += 1
      col = 0
    } else {
      col += 1
    }
  }

  // ---- Top-level parsing ----

  private def skipXmlDeclaration(): Unit = {
    // Check for <?xml ...?> at the start
    if (current != '<') return

    // Peek by advancing
    advance() // consumed '<'
    if (current != '?') {
      // Not a PI — we've consumed '<' and current is the next char.
      // We need to handle this as content. Parse what comes after '<'.
      handleAfterLt(new NamespaceContext())
      return
    }

    advance() // consumed '?'
    val target = readName()
    if (target == "xml") {
      skipUntil("?>")
    } else {
      // Regular PI, not XML declaration
      skipWhitespace()
      val data = readUntil("?>")
      if (contentHandler != null)
        contentHandler.processingInstruction(target, data.trim)
    }
  }

  private def parseMiscAndElement(nsCtx: NamespaceContext): Unit = {
    while (current != -1) {
      if (current == '<') {
        advance() // consumed '<'
        handleAfterLt(nsCtx)
      } else if (current == '&') {
        parseReference()
      } else {
        parseCharData()
      }
    }
  }

  /** Called after '<' has been consumed. current is the character after '<'. */
  private def handleAfterLt(nsCtx: NamespaceContext): Unit = {
    if (current == '?') {
      advance()
      parsePi()
    } else if (current == '!') {
      advance()
      parseMarkupDecl(nsCtx)
    } else if (current == '/') {
      advance()
      parseEndTag(nsCtx)
    } else {
      // Element — current is the first char of the element name
      parseElement(nsCtx)
    }
  }

  // ---- Element parsing ----

  private def parseElement(nsCtx: NamespaceContext): Unit = {
    val qName = readName()

    // Parse attributes
    val atts = new AttributesImpl()
    val newPrefixes = scala.collection.mutable.ListBuffer[(String, String)]()

    skipWhitespace()
    while (current != '>' && current != '/' && current != -1) {
      val attQName = readName()
      skipWhitespace()
      expect('=')
      skipWhitespace()
      val attValue = readQuotedValue()

      if (attQName == "xmlns") {
        nsCtx.declarePrefix("", attValue)
        newPrefixes += (("", attValue))
        if (namespacePrefixesFeature) {
          atts.addAttribute("", "", attQName, "CDATA", attValue)
        }
      } else if (attQName.startsWith("xmlns:")) {
        val prefix = attQName.substring(6)
        nsCtx.declarePrefix(prefix, attValue)
        newPrefixes += ((prefix, attValue))
        if (namespacePrefixesFeature) {
          atts.addAttribute("", "", attQName, "CDATA", attValue)
        }
      } else {
        atts.addAttribute("", "", attQName, "CDATA", attValue)
      }

      skipWhitespace()
    }

    // Fire startPrefixMapping events
    if (contentHandler != null) {
      for ((prefix, uri) <- newPrefixes) {
        contentHandler.startPrefixMapping(prefix, uri)
      }
    }

    // Resolve element namespace
    val (elemUri, elemLocalName, _) = resolveQName(qName, nsCtx, isAttribute = false)

    // Resolve attribute namespaces
    if (namespacesFeature) {
      var i = 0
      while (i < atts.getLength()) {
        val aqn = atts.getQName(i)
        if (aqn != "xmlns" && !aqn.startsWith("xmlns:")) {
          val (aUri, aLocalName, _) = resolveQName(aqn, nsCtx, isAttribute = true)
          atts.setURI(i, aUri)
          atts.setLocalName(i, aLocalName)
        }
        i += 1
      }
    }

    val isEmpty = current == '/'
    if (isEmpty) {
      advance() // consume '/'
      expect('>')
    } else {
      expect('>')
    }

    if (contentHandler != null)
      contentHandler.startElement(elemUri, elemLocalName, qName, atts)

    if (isEmpty) {
      if (contentHandler != null)
        contentHandler.endElement(elemUri, elemLocalName, qName)
      fireEndPrefixMappings(newPrefixes)
      nsCtx.popPrefixes(newPrefixes.size)
      return
    }

    // Parse element content until matching end tag
    parseElementContent(nsCtx, elemUri, elemLocalName, qName)

    fireEndPrefixMappings(newPrefixes)
    nsCtx.popPrefixes(newPrefixes.size)
  }

  private def parseElementContent(nsCtx: NamespaceContext, elemUri: String, elemLocalName: String, elemQName: String): Unit = {
    while (current != -1) {
      if (current == '<') {
        advance() // consumed '<'
        if (current == '/') {
          advance() // consumed '/'
          // End tag
          val endQName = readName()
          skipWhitespace()
          expect('>')
          if (endQName != elemQName)
            fatalError(s"End tag '</$endQName>' does not match start tag '<$elemQName>'")
          if (contentHandler != null)
            contentHandler.endElement(elemUri, elemLocalName, elemQName)
          return
        } else {
          handleAfterLt(nsCtx)
        }
      } else if (current == '&') {
        parseReference()
      } else {
        parseCharData()
      }
    }
    fatalError(s"Unexpected end of input in element '$elemQName'")
  }

  private def fireEndPrefixMappings(prefixes: scala.collection.mutable.ListBuffer[(String, String)]): Unit = {
    if (contentHandler != null) {
      for ((prefix, _) <- prefixes.reverse) {
        contentHandler.endPrefixMapping(prefix)
      }
    }
  }

  // ---- End tag (standalone, used from handleAfterLt at top level) ----

  private def parseEndTag(nsCtx: NamespaceContext): Unit = {
    val qName = readName()
    skipWhitespace()
    expect('>')
    val (uri, localName, _) = resolveQName(qName, nsCtx, isAttribute = false)
    if (contentHandler != null)
      contentHandler.endElement(uri, localName, qName)
  }

  // ---- PI, Comments, CDATA, DOCTYPE ----

  private def parsePi(): Unit = {
    val target = readName()
    if (current == '?') {
      advance()
      expect('>')
      if (contentHandler != null) contentHandler.processingInstruction(target, "")
    } else {
      skipWhitespace()
      val data = readUntil("?>")
      if (contentHandler != null) contentHandler.processingInstruction(target, data)
    }
  }

  private def parseMarkupDecl(nsCtx: NamespaceContext): Unit = {
    if (current == '-') {
      advance()
      expect('-')
      parseComment()
    } else if (current == '[') {
      advance()
      expectString("CDATA[")
      parseCdata()
    } else if (current == 'D') {
      expectString("DOCTYPE")
      parseDtd()
    } else {
      fatalError(s"Unexpected markup declaration character: '${current.toChar}'")
    }
  }

  private def parseComment(): Unit = {
    val sb = new StringBuilder
    while (true) {
      if (current == -1) fatalError("Unexpected end of input in comment")
      if (current == '-') {
        advance()
        if (current == '-') {
          advance()
          expect('>')
          if (lexicalHandler != null) {
            val chars = sb.toString.toCharArray
            lexicalHandler.comment(chars, 0, chars.length)
          }
          return
        } else {
          sb.append('-')
        }
      } else {
        sb.append(current.toChar)
        advance()
      }
    }
  }

  private def parseCdata(): Unit = {
    if (lexicalHandler != null) lexicalHandler.startCDATA()
    val sb = new StringBuilder
    while (true) {
      if (current == -1) fatalError("Unexpected end of input in CDATA section")
      if (current == ']') {
        advance()
        if (current == ']') {
          advance()
          if (current == '>') {
            advance()
            if (contentHandler != null) {
              val chars = sb.toString.toCharArray
              contentHandler.characters(chars, 0, chars.length)
            }
            if (lexicalHandler != null) lexicalHandler.endCDATA()
            return
          } else {
            sb.append("]]")
          }
        } else {
          sb.append(']')
        }
      } else {
        sb.append(current.toChar)
        advance()
      }
    }
  }

  private def parseDtd(): Unit = {
    skipWhitespace()
    val rootName = readName()
    skipWhitespace()

    var dtdPublicId: String = null
    var dtdSystemId: String = null

    if (current == 'S') {
      expectString("SYSTEM")
      skipWhitespace()
      dtdSystemId = readQuotedValue()
      skipWhitespace()
    } else if (current == 'P') {
      expectString("PUBLIC")
      skipWhitespace()
      dtdPublicId = readQuotedValue()
      skipWhitespace()
      dtdSystemId = readQuotedValue()
      skipWhitespace()
    }

    if (lexicalHandler != null)
      lexicalHandler.startDTD(rootName, dtdPublicId, dtdSystemId)

    if (current == '[') {
      advance()
      parseDtdInternalSubset()
      skipWhitespace()
    }

    if (current == '>') advance()
    else fatalError(s"Expected '>' to close DOCTYPE but got '${if (current == -1) "EOF" else current.toChar.toString}'")

    if (lexicalHandler != null) lexicalHandler.endDTD()
  }

  private def parseDtdInternalSubset(): Unit = {
    while (current != -1 && current != ']') {
      skipWhitespace()
      if (current == ']') { advance(); return }
      if (current == '<') {
        advance()
        if (current == '!') {
          advance()
          if (current == '-') {
            advance()
            expect('-')
            parseComment() // comment in DTD
          } else if (current == 'E') {
            advance()
            if (current == 'N') {
              expectString("NTITY")
              parseDtdEntity()
            } else if (current == 'L') {
              expectString("LEMENT")
              parseDtdElement()
            } else {
              fatalError(s"Unexpected DTD declaration: <!E${current.toChar}")
            }
          } else if (current == 'A') {
            expectString("ATTLIST")
            parseDtdAttlist()
          } else if (current == 'N') {
            expectString("NOTATION")
            parseDtdNotation()
          } else {
            skipUntil(">")
          }
        } else if (current == '?') {
          advance()
          val target = readName()
          skipWhitespace()
          readUntil("?>")
        } else {
          fatalError(s"Unexpected character in DTD: '<${current.toChar}'")
        }
      } else if (current == '%') {
        advance()
        readName()
        expect(';')
      } else {
        advance()
      }
    }
    if (current == ']') advance()
  }

  private def parseDtdEntity(): Unit = {
    skipWhitespace()
    val isParameterEntity = current == '%'
    if (isParameterEntity) { advance(); skipWhitespace() }
    val name = readName()
    skipWhitespace()

    if (current == '"' || current == '\'') {
      val value = readQuotedValue()
      if (!isParameterEntity) {
        entities(name) = value
        if (declHandler != null) declHandler.internalEntityDecl(name, value)
      }
    } else {
      var extPublicId: String = null
      var extSystemId: String = null
      if (current == 'S') {
        expectString("SYSTEM"); skipWhitespace()
        extSystemId = readQuotedValue()
      } else if (current == 'P') {
        expectString("PUBLIC"); skipWhitespace()
        extPublicId = readQuotedValue(); skipWhitespace()
        extSystemId = readQuotedValue()
      }
      skipWhitespace()
      if (current == 'N') {
        expectString("NDATA"); skipWhitespace()
        val notationName = readName()
        if (dtdHandler != null && !isParameterEntity)
          dtdHandler.unparsedEntityDecl(name, extPublicId, extSystemId, notationName)
      } else {
        if (declHandler != null && !isParameterEntity)
          declHandler.externalEntityDecl(name, extPublicId, extSystemId)
      }
    }
    skipWhitespace()
    if (current == '>') advance()
  }

  private def parseDtdElement(): Unit = {
    skipWhitespace()
    val name = readName()
    skipWhitespace()
    val model = readUntil(">")
    if (declHandler != null) declHandler.elementDecl(name, model.trim)
  }

  private def parseDtdAttlist(): Unit = {
    skipWhitespace()
    val eName = readName()
    skipWhitespace()
    while (current != '>' && current != -1) {
      if (current == '>') { advance(); return }
      val aName = readName()
      skipWhitespace()
      val aType = readAttType()
      skipWhitespace()
      val (mode, defaultValue) = readAttDefault()
      skipWhitespace()
      if (declHandler != null)
        declHandler.attributeDecl(eName, aName, aType, mode, defaultValue)
    }
    if (current == '>') advance()
  }

  private def readAttType(): String = {
    if (current == '(') {
      val sb = new StringBuilder("(")
      advance()
      while (current != ')' && current != -1) { sb.append(current.toChar); advance() }
      if (current == ')') { sb.append(')'); advance() }
      sb.toString
    } else {
      val name = readName()
      if (name == "NOTATION") {
        skipWhitespace()
        val sb = new StringBuilder("NOTATION ")
        if (current == '(') {
          sb.append('('); advance()
          while (current != ')' && current != -1) { sb.append(current.toChar); advance() }
          if (current == ')') { sb.append(')'); advance() }
        }
        sb.toString
      } else name
    }
  }

  private def readAttDefault(): (String, String) = {
    if (current == '#') {
      advance()
      val keyword = readName()
      keyword match {
        case "REQUIRED" => ("#REQUIRED", null)
        case "IMPLIED"  => ("#IMPLIED", null)
        case "FIXED"    => skipWhitespace(); ("#FIXED", readQuotedValue())
        case _          => ("#" + keyword, null)
      }
    } else if (current == '"' || current == '\'') {
      (null, readQuotedValue())
    } else {
      (null, null)
    }
  }

  private def parseDtdNotation(): Unit = {
    skipWhitespace()
    val name = readName()
    skipWhitespace()
    var notPublicId: String = null
    var notSystemId: String = null
    if (current == 'S') {
      expectString("SYSTEM"); skipWhitespace()
      notSystemId = readQuotedValue()
    } else if (current == 'P') {
      expectString("PUBLIC"); skipWhitespace()
      notPublicId = readQuotedValue(); skipWhitespace()
      if (current != '>') notSystemId = readQuotedValue()
    }
    skipWhitespace()
    if (current == '>') advance()
    if (dtdHandler != null) dtdHandler.notationDecl(name, notPublicId, notSystemId)
  }

  // ---- Character data and references ----

  private def parseCharData(): Unit = {
    val sb = new StringBuilder
    while (current != -1 && current != '<' && current != '&') {
      sb.append(current.toChar)
      advance()
    }
    if (sb.nonEmpty && contentHandler != null) {
      val chars = sb.toString.toCharArray
      contentHandler.characters(chars, 0, chars.length)
    }
  }

  private def parseReference(): Unit = {
    advance() // skip '&'
    if (current == '#') {
      advance()
      val charRef = parseCharReference()
      if (contentHandler != null) {
        val chars = Character.toChars(charRef)
        contentHandler.characters(chars, 0, chars.length)
      }
    } else {
      val name = readName()
      expect(';')
      entities.get(name) match {
        case Some(value) =>
          if (contentHandler != null) {
            val chars = value.toCharArray
            contentHandler.characters(chars, 0, chars.length)
          }
        case None =>
          if (contentHandler != null) contentHandler.skippedEntity(name)
      }
    }
  }

  private def parseCharReference(): Int = {
    val sb = new StringBuilder
    val hex = current == 'x'
    if (hex) advance()
    while (current != ';' && current != -1) {
      sb.append(current.toChar)
      advance()
    }
    expect(';')
    if (hex) Integer.parseInt(sb.toString, 16) else Integer.parseInt(sb.toString)
  }

  // ---- Namespace handling ----

  private class NamespaceContext {
    private val prefixes = scala.collection.mutable.ListBuffer[(String, String)](("", ""))

    def declarePrefix(prefix: String, uri: String): Unit = prefixes += ((prefix, uri))

    def popPrefixes(count: Int): Unit = {
      var i = 0
      while (i < count) {
        if (prefixes.nonEmpty) prefixes.remove(prefixes.size - 1)
        i += 1
      }
    }

    def resolvePrefix(prefix: String): String = {
      var i = prefixes.size - 1
      while (i >= 0) {
        if (prefixes(i)._1 == prefix) return prefixes(i)._2
        i -= 1
      }
      if (prefix.isEmpty) "" else null
    }
  }

  private def resolveQName(qName: String, nsCtx: NamespaceContext, isAttribute: Boolean): (String, String, String) = {
    if (!namespacesFeature) return ("", qName, "")
    val colonIdx = qName.indexOf(':')
    if (colonIdx < 0) {
      val uri = if (isAttribute) "" else nsCtx.resolvePrefix("")
      (if (uri == null) "" else uri, qName, "")
    } else {
      val prefix = qName.substring(0, colonIdx)
      val localName = qName.substring(colonIdx + 1)
      val uri = nsCtx.resolvePrefix(prefix)
      if (uri == null) fatalError(s"Undeclared namespace prefix: '$prefix'")
      (uri, localName, prefix)
    }
  }

  // ---- Utility ----

  private def readName(): String = {
    val sb = new StringBuilder
    if (current == -1 || !isNameStartChar(current.toChar))
      fatalError(s"Expected name start character but got '${if (current == -1) "EOF" else current.toChar.toString}'")
    sb.append(current.toChar)
    advance()
    while (current != -1 && isNameChar(current.toChar)) {
      sb.append(current.toChar)
      advance()
    }
    sb.toString
  }

  private def isNameStartChar(c: Char): Boolean = {
    c == ':' || c == '_' ||
      (c >= 'A' && c <= 'Z') ||
      (c >= 'a' && c <= 'z') ||
      (c >= '\u00C0' && c <= '\u00D6') ||
      (c >= '\u00D8' && c <= '\u00F6') ||
      (c >= '\u00F8' && c <= '\u02FF') ||
      (c >= '\u0370' && c <= '\u037D') ||
      (c >= '\u037F' && c <= '\u1FFF') ||
      (c >= '\u200C' && c <= '\u200D') ||
      (c >= '\u2070' && c <= '\u218F') ||
      (c >= '\u2C00' && c <= '\u2FEF') ||
      (c >= '\u3001' && c <= '\uD7FF') ||
      (c >= '\uF900' && c <= '\uFDCF') ||
      (c >= '\uFDF0' && c <= '\uFFFD')
  }

  private def isNameChar(c: Char): Boolean = {
    isNameStartChar(c) ||
      c == '-' || c == '.' ||
      (c >= '0' && c <= '9') ||
      c == '\u00B7' ||
      (c >= '\u0300' && c <= '\u036F') ||
      (c >= '\u203F' && c <= '\u2040')
  }

  private def skipWhitespace(): Unit = {
    while (current != -1 && (current == ' ' || current == '\t' || current == '\n' || current == '\r'))
      advance()
  }

  private def expect(ch: Char): Unit = {
    if (current != ch.toInt)
      fatalError(s"Expected '$ch' but got '${if (current == -1) "EOF" else current.toChar.toString}'")
    advance()
  }

  private def expectString(s: String): Unit = {
    for (ch <- s) {
      if (current != ch.toInt)
        fatalError(s"Expected '$s' but got unexpected character '${if (current == -1) "EOF" else current.toChar.toString}'")
      advance()
    }
  }

  private def readQuotedValue(): String = {
    val quote = current
    if (quote != '"' && quote != '\'')
      fatalError(s"Expected quote character but got '${if (current == -1) "EOF" else current.toChar.toString}'")
    advance()
    val sb = new StringBuilder
    while (current != -1 && current != quote) {
      if (current == '&') {
        advance()
        if (current == '#') {
          advance()
          val cp = parseCharReference()
          sb.appendAll(Character.toChars(cp))
        } else {
          val name = readName()
          expect(';')
          entities.get(name) match {
            case Some(value) => sb.append(value)
            case None        => sb.append('&').append(name).append(';')
          }
        }
      } else {
        sb.append(current.toChar)
        advance()
      }
    }
    advance() // skip closing quote
    sb.toString
  }

  private def readUntil(terminator: String): String = {
    val sb = new StringBuilder
    val termChars = terminator.toCharArray
    while (true) {
      if (current == -1) fatalError(s"Unexpected end of input, expected '$terminator'")
      if (current == termChars(0)) {
        if (termChars.length == 1) {
          advance()
          return sb.toString
        }
        // Check for full terminator
        val mark = sb.length
        sb.append(current.toChar)
        advance()
        var matched = 1
        while (matched < termChars.length && current == termChars(matched)) {
          sb.append(current.toChar)
          advance()
          matched += 1
        }
        if (matched == termChars.length) {
          sb.setLength(mark) // remove terminator chars from result
          return sb.toString
        }
        // Not a full match — partial terminator is now in sb, continue
      } else {
        sb.append(current.toChar)
        advance()
      }
    }
    sb.toString // unreachable
  }

  private def skipUntil(terminator: String): Unit = readUntil(terminator)

  private def fatalError(message: String): Nothing = {
    val ex = new SAXParseException(message, publicId, systemId, line, col)
    if (errorHandler != null) errorHandler.fatalError(ex)
    throw ex
  }
}
