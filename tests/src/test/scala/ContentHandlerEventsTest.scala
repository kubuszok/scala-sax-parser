package scalasaxparser

import munit.FunSuite
import org.xml.sax._
import org.xml.sax.ext.LexicalHandler
import org.xml.sax.helpers.DefaultHandler
import javax.xml.parsers.SAXParserFactory

import java.io.StringReader

class ContentHandlerEventsTest extends FunSuite {

  /** A recording handler that logs all ContentHandler and LexicalHandler events. */
  private class RecordingHandler extends DefaultHandler with LexicalHandler {
    val events = scala.collection.mutable.ListBuffer[String]()

    override def setDocumentLocator(locator: Locator): Unit =
      events += "setDocumentLocator"

    override def startDocument(): Unit =
      events += "startDocument"

    override def endDocument(): Unit =
      events += "endDocument"

    override def startPrefixMapping(prefix: String, uri: String): Unit =
      events += s"startPrefixMapping:$prefix=$uri"

    override def endPrefixMapping(prefix: String): Unit =
      events += s"endPrefixMapping:$prefix"

    override def startElement(uri: String, localName: String, qName: String, atts: Attributes): Unit =
      events += s"startElement:$qName"

    override def endElement(uri: String, localName: String, qName: String): Unit =
      events += s"endElement:$qName"

    override def characters(ch: Array[Char], start: Int, length: Int): Unit =
      events += s"characters:${new String(ch, start, length)}"

    override def processingInstruction(target: String, data: String): Unit =
      events += s"processingInstruction:$target:$data"

    override def ignorableWhitespace(ch: Array[Char], start: Int, length: Int): Unit =
      events += s"ignorableWhitespace"

    override def skippedEntity(name: String): Unit =
      events += s"skippedEntity:$name"

    // LexicalHandler
    override def startDTD(name: String, publicId: String, systemId: String): Unit =
      events += s"startDTD:$name"

    override def endDTD(): Unit =
      events += "endDTD"

    override def startEntity(name: String): Unit =
      events += s"startEntity:$name"

    override def endEntity(name: String): Unit =
      events += s"endEntity:$name"

    override def startCDATA(): Unit =
      events += "startCDATA"

    override def endCDATA(): Unit =
      events += "endCDATA"

    override def comment(ch: Array[Char], start: Int, length: Int): Unit =
      events += s"comment:${new String(ch, start, length)}"
  }

  private def newReader(): XMLReader = {
    val factory = SAXParserFactory.newInstance()
    factory.setNamespaceAware(true)
    factory.newSAXParser().getXMLReader()
  }

  private def parseWithRecorder(xml: String): RecordingHandler = {
    val reader = newReader()
    val handler = new RecordingHandler()
    reader.setContentHandler(handler)
    reader.setProperty("http://xml.org/sax/properties/lexical-handler", handler)
    reader.parse(new InputSource(new StringReader(xml)))
    handler
  }

  test("simple XML: correct event order") {
    val handler = parseWithRecorder("<root>hello</root>")
    val events = handler.events.toList

    val sdIdx = events.indexOf("startDocument")
    val seIdx = events.indexOf("startElement:root")
    val chIdx = events.indexOf("characters:hello")
    val eeIdx = events.indexOf("endElement:root")
    val edIdx = events.indexOf("endDocument")

    assertNotEquals(sdIdx, -1)
    assertNotEquals(seIdx, -1)
    assertNotEquals(chIdx, -1)
    assertNotEquals(eeIdx, -1)
    assertNotEquals(edIdx, -1)

    assertEquals(sdIdx < seIdx, true)
    assertEquals(seIdx < chIdx, true)
    assertEquals(chIdx < eeIdx, true)
    assertEquals(eeIdx < edIdx, true)
  }

  test("namespaces: startPrefixMapping before startElement") {
    val xml = """<root xmlns:ns="http://example.com"><ns:child/></root>"""
    val reader = newReader()
    val handler = new RecordingHandler()
    reader.setContentHandler(handler)
    reader.parse(new InputSource(new StringReader(xml)))
    val events = handler.events.toList

    val spmIdx = events.indexWhere(_.startsWith("startPrefixMapping:ns="))
    val seChildIdx = events.indexOf("startElement:ns:child")

    assertNotEquals(spmIdx, -1)
    assertNotEquals(seChildIdx, -1)
    assertEquals(spmIdx < seChildIdx, true)
  }

  test("namespaces: endPrefixMapping after endElement") {
    val xml = """<root xmlns:ns="http://example.com"><ns:child/></root>"""
    val reader = newReader()
    val handler = new RecordingHandler()
    reader.setContentHandler(handler)
    reader.parse(new InputSource(new StringReader(xml)))
    val events = handler.events.toList

    val eeChildIdx = events.indexOf("endElement:ns:child")
    val epmIdx = events.indexWhere(_.startsWith("endPrefixMapping:ns"))

    assertNotEquals(eeChildIdx, -1)
    assertNotEquals(epmIdx, -1)
    assertEquals(eeChildIdx < epmIdx, true)
  }

  test("multiple elements: proper nesting") {
    val handler = parseWithRecorder("<root><a><b/></a><c/></root>")
    val events = handler.events.toList

    val startRoot = events.indexOf("startElement:root")
    val startA = events.indexOf("startElement:a")
    val startB = events.indexOf("startElement:b")
    val endB = events.indexOf("endElement:b")
    val endA = events.indexOf("endElement:a")
    val startC = events.indexOf("startElement:c")
    val endC = events.indexOf("endElement:c")
    val endRoot = events.indexOf("endElement:root")

    assertEquals(startRoot < startA, true)
    assertEquals(startA < startB, true)
    assertEquals(startB < endB, true)
    assertEquals(endB < endA, true)
    assertEquals(endA < startC, true)
    assertEquals(startC < endC, true)
    assertEquals(endC < endRoot, true)
  }

  test("attributes: Attributes parameter has correct values") {
    val xml = """<root attr1="val1" attr2="val2"/>"""
    val reader = newReader()
    var capturedAtts: Attributes = null

    reader.setContentHandler(new DefaultHandler() {
      override def startElement(uri: String, localName: String, qName: String, atts: Attributes): Unit = {
        capturedAtts = new org.xml.sax.helpers.AttributesImpl(atts)
      }
    })

    reader.parse(new InputSource(new StringReader(xml)))

    assertNotEquals(capturedAtts, null)
    assertEquals(capturedAtts.getLength(), 2)

    // Look up by qName
    val idx1 = capturedAtts.getIndex("attr1")
    val idx2 = capturedAtts.getIndex("attr2")
    assertNotEquals(idx1, -1)
    assertNotEquals(idx2, -1)
    assertEquals(capturedAtts.getValue(idx1), "val1")
    assertEquals(capturedAtts.getValue(idx2), "val2")
  }

  test("mixed content: correct interleaving of characters and elements") {
    val handler = parseWithRecorder("<p>Hello <b>world</b>!</p>")
    val events = handler.events.toList

    val helloIdx = events.indexOf("characters:Hello ")
    val startB = events.indexOf("startElement:b")
    val worldIdx = events.indexOf("characters:world")
    val endB = events.indexOf("endElement:b")
    val excIdx = events.indexOf("characters:!")

    assertNotEquals(helloIdx, -1)
    assertNotEquals(startB, -1)
    assertNotEquals(worldIdx, -1)
    assertNotEquals(endB, -1)
    assertNotEquals(excIdx, -1)

    assertEquals(helloIdx < startB, true)
    assertEquals(startB < worldIdx, true)
    assertEquals(worldIdx < endB, true)
    assertEquals(endB < excIdx, true)
  }

  test("empty element: startElement immediately followed by endElement") {
    val handler = parseWithRecorder("<root><empty/></root>")
    val events = handler.events.toList

    val startEmpty = events.indexOf("startElement:empty")
    val endEmpty = events.indexOf("endElement:empty")

    assertNotEquals(startEmpty, -1)
    assertNotEquals(endEmpty, -1)
    assertEquals(endEmpty, startEmpty + 1)
  }

  test("CDATA: characters event with CDATA content") {
    val handler = parseWithRecorder("<data><![CDATA[<not>xml</not>]]></data>")
    val events = handler.events.toList

    assertEquals(events.contains("startCDATA"), true)
    assertEquals(events.contains("endCDATA"), true)

    val cdataCharIdx = events.indexOf("characters:<not>xml</not>")
    assertNotEquals(cdataCharIdx, -1)

    val startCDATA = events.indexOf("startCDATA")
    val endCDATA = events.indexOf("endCDATA")
    assertEquals(startCDATA < cdataCharIdx, true)
    assertEquals(cdataCharIdx < endCDATA, true)
  }

  test("comment: LexicalHandler.comment callback") {
    val handler = parseWithRecorder("<root><!-- a comment --></root>")
    val events = handler.events.toList

    val commentIdx = events.indexOf("comment: a comment ")
    assertNotEquals(commentIdx, -1)
  }

  test("processing instruction: processingInstruction callback") {
    val handler = parseWithRecorder("<root><?target data?></root>")
    val events = handler.events.toList

    val piIdx = events.indexOf("processingInstruction:target:data")
    assertNotEquals(piIdx, -1)
  }

  test("setDocumentLocator is fired before startDocument") {
    val handler = parseWithRecorder("<root/>")
    val events = handler.events.toList

    val sdlIdx = events.indexOf("setDocumentLocator")
    val sdIdx = events.indexOf("startDocument")

    assertNotEquals(sdlIdx, -1)
    assertNotEquals(sdIdx, -1)
    assertEquals(sdlIdx < sdIdx, true)
  }

  test("default namespace: startPrefixMapping with empty prefix") {
    val xml = """<root xmlns="http://default.ns"><child/></root>"""
    val reader = newReader()
    val handler = new RecordingHandler()
    reader.setContentHandler(handler)
    reader.parse(new InputSource(new StringReader(xml)))
    val events = handler.events.toList

    val spmIdx = events.indexWhere(_.startsWith("startPrefixMapping:=http://default.ns"))
    assertNotEquals(spmIdx, -1)
  }

  test("multiple namespace declarations on same element") {
    val xml = """<root xmlns:a="http://a" xmlns:b="http://b"><a:x/><b:y/></root>"""
    val reader = newReader()
    val handler = new RecordingHandler()
    reader.setContentHandler(handler)
    reader.parse(new InputSource(new StringReader(xml)))
    val events = handler.events.toList

    assertEquals(events.exists(_.startsWith("startPrefixMapping:a=")), true)
    assertEquals(events.exists(_.startsWith("startPrefixMapping:b=")), true)
  }

  test("characters with entity references") {
    val handler = parseWithRecorder("<data>a&amp;b</data>")
    val events = handler.events.toList

    val charEvents = events.filter(_.startsWith("characters:"))
    val allText = charEvents.map(_.stripPrefix("characters:")).mkString
    assertEquals(allText, "a&b")
  }

  test("namespace-aware parsing: localName and URI populated for prefixed elements") {
    val xml = """<root xmlns:ns="http://example.com"><ns:item/></root>"""
    val reader = newReader()
    var capturedUri: String = null
    var capturedLocalName: String = null
    var capturedQName: String = null

    reader.setContentHandler(new DefaultHandler() {
      override def startElement(uri: String, localName: String, qName: String, atts: Attributes): Unit = {
        if (qName == "ns:item") {
          capturedUri = uri
          capturedLocalName = localName
          capturedQName = qName
        }
      }
    })

    reader.parse(new InputSource(new StringReader(xml)))

    assertEquals(capturedUri, "http://example.com")
    assertEquals(capturedLocalName, "item")
    assertEquals(capturedQName, "ns:item")
  }

  test("attributes with namespaces: URI and localName populated") {
    val xml = """<root xmlns:ns="http://example.com" ns:attr="val"/>"""
    val reader = newReader()
    var capturedAtts: Attributes = null

    reader.setContentHandler(new DefaultHandler() {
      override def startElement(uri: String, localName: String, qName: String, atts: Attributes): Unit = {
        capturedAtts = new org.xml.sax.helpers.AttributesImpl(atts)
      }
    })

    reader.parse(new InputSource(new StringReader(xml)))

    assertNotEquals(capturedAtts, null)
    val idx = capturedAtts.getIndex("ns:attr")
    assertNotEquals(idx, -1)
    assertEquals(capturedAtts.getURI(idx), "http://example.com")
    assertEquals(capturedAtts.getLocalName(idx), "attr")
  }

  test("processing instruction at document level (before root)") {
    val xml = """<?xml version="1.0"?><?mytarget mydata?><root/>"""
    val handler = parseWithRecorder(xml)
    val events = handler.events.toList

    val piIdx = events.indexWhere(_.startsWith("processingInstruction:mytarget"))
    val startRoot = events.indexOf("startElement:root")

    assertNotEquals(piIdx, -1)
    assertEquals(piIdx < startRoot, true)
  }
}
