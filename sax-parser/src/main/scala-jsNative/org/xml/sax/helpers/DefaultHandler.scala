package org.xml.sax.helpers

import org.xml.sax._

class DefaultHandler
    extends ContentHandler
    with DTDHandler
    with EntityResolver
    with ErrorHandler {

  // EntityResolver
  override def resolveEntity(publicId: String, systemId: String): InputSource = null

  // DTDHandler
  override def notationDecl(name: String, publicId: String, systemId: String): Unit = ()
  override def unparsedEntityDecl(name: String, publicId: String, systemId: String, notationName: String): Unit = ()

  // ContentHandler
  override def setDocumentLocator(locator: Locator): Unit = ()
  override def startDocument(): Unit = ()
  override def endDocument(): Unit = ()
  override def startPrefixMapping(prefix: String, uri: String): Unit = ()
  override def endPrefixMapping(prefix: String): Unit = ()
  override def startElement(uri: String, localName: String, qName: String, atts: Attributes): Unit = ()
  override def endElement(uri: String, localName: String, qName: String): Unit = ()
  override def characters(ch: Array[Char], start: Int, length: Int): Unit = ()
  override def ignorableWhitespace(ch: Array[Char], start: Int, length: Int): Unit = ()
  override def processingInstruction(target: String, data: String): Unit = ()
  override def skippedEntity(name: String): Unit = ()

  // ErrorHandler
  override def warning(exception: SAXParseException): Unit = ()
  override def error(exception: SAXParseException): Unit = ()
  override def fatalError(exception: SAXParseException): Unit = throw exception
}
