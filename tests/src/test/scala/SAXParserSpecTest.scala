package scalasaxparser

import munit.FunSuite
import javax.xml.parsers.SAXParserFactory
import org.xml.sax._
import org.xml.sax.ext.LexicalHandler
import org.xml.sax.helpers.DefaultHandler

import java.io.{ByteArrayInputStream, StringReader}

class SAXParserSpecTest extends FunSuite {

  private def newParser(namespaceAware: Boolean = false): javax.xml.parsers.SAXParser = {
    val factory = SAXParserFactory.newInstance()
    factory.setNamespaceAware(namespaceAware)
    factory.newSAXParser()
  }

  test("getXMLReader returns non-null") {
    val parser = newParser()
    assertNotEquals(parser.getXMLReader(), null)
  }

  test("getXMLReader returns same instance on repeated calls") {
    val parser = newParser()
    val reader1 = parser.getXMLReader()
    val reader2 = parser.getXMLReader()
    assertEquals(reader1 eq reader2, true)
  }

  test("isNamespaceAware reflects factory setting: false") {
    val parser = newParser(namespaceAware = false)
    assertEquals(parser.isNamespaceAware(), false)
  }

  test("isNamespaceAware reflects factory setting: true") {
    val parser = newParser(namespaceAware = true)
    assertEquals(parser.isNamespaceAware(), true)
  }

  test("isValidating reflects factory setting") {
    val factory = SAXParserFactory.newInstance()
    factory.setValidating(false)
    val parser = factory.newSAXParser()
    assertEquals(parser.isValidating(), false)
  }

  test("parse(InputSource, DefaultHandler) works") {
    val parser = newParser()
    val events = scala.collection.mutable.ListBuffer[String]()

    val handler = new DefaultHandler() {
      override def startElement(uri: String, localName: String, qName: String, atts: Attributes): Unit =
        events += s"start:$qName"
      override def endElement(uri: String, localName: String, qName: String): Unit =
        events += s"end:$qName"
    }

    val source = new InputSource(new StringReader("<root><child/></root>"))
    parser.parse(source, handler)

    assertEquals(events.contains("start:root"), true)
    assertEquals(events.contains("end:root"), true)
    assertEquals(events.contains("start:child"), true)
    assertEquals(events.contains("end:child"), true)
  }

  test("parse(InputStream, DefaultHandler) works") {
    val parser = newParser()
    val events = scala.collection.mutable.ListBuffer[String]()

    val handler = new DefaultHandler() {
      override def startElement(uri: String, localName: String, qName: String, atts: Attributes): Unit =
        events += s"start:$qName"
      override def endElement(uri: String, localName: String, qName: String): Unit =
        events += s"end:$qName"
    }

    val is = new ByteArrayInputStream("<hello/>".getBytes("UTF-8"))
    parser.parse(is, handler)

    assertEquals(events.contains("start:hello"), true)
    assertEquals(events.contains("end:hello"), true)
  }

  test("parse sets ContentHandler from DefaultHandler") {
    val parser = newParser()
    val handler = new DefaultHandler()
    val source = new InputSource(new StringReader("<root/>"))
    parser.parse(source, handler)

    assertEquals(parser.getXMLReader().getContentHandler() eq handler, true)
  }

  test("parse sets DTDHandler from DefaultHandler") {
    val parser = newParser()
    val handler = new DefaultHandler()
    val source = new InputSource(new StringReader("<root/>"))
    parser.parse(source, handler)

    assertEquals(parser.getXMLReader().getDTDHandler() eq handler, true)
  }

  test("parse sets EntityResolver from DefaultHandler") {
    val parser = newParser()
    val handler = new DefaultHandler()
    val source = new InputSource(new StringReader("<root/>"))
    parser.parse(source, handler)

    assertEquals(parser.getXMLReader().getEntityResolver() eq handler, true)
  }

  test("parse sets ErrorHandler from DefaultHandler") {
    val parser = newParser()
    val handler = new DefaultHandler()
    val source = new InputSource(new StringReader("<root/>"))
    parser.parse(source, handler)

    assertEquals(parser.getXMLReader().getErrorHandler() eq handler, true)
  }

  test("getProperty delegates to XMLReader") {
    val parser = newParser()
    // lexical-handler should be null initially
    assertEquals(parser.getProperty("http://xml.org/sax/properties/lexical-handler"), null)
  }

  test("setProperty delegates to XMLReader") {
    val parser = newParser()
    val lexHandler = new DefaultHandler() with LexicalHandler {
      def startDTD(name: String, publicId: String, systemId: String): Unit = ()
      def endDTD(): Unit = ()
      def startEntity(name: String): Unit = ()
      def endEntity(name: String): Unit = ()
      def startCDATA(): Unit = ()
      def endCDATA(): Unit = ()
      def comment(ch: Array[Char], start: Int, length: Int): Unit = ()
    }

    parser.setProperty("http://xml.org/sax/properties/lexical-handler", lexHandler)
    val result = parser.getProperty("http://xml.org/sax/properties/lexical-handler")
    assertEquals(result eq lexHandler, true)
  }

  test("getProperty for unrecognized throws SAXNotRecognizedException") {
    val parser = newParser()
    intercept[SAXNotRecognizedException] {
      parser.getProperty("http://xml.org/sax/properties/nonexistent")
    }
  }

  test("setProperty for unrecognized throws SAXNotRecognizedException") {
    val parser = newParser()
    intercept[SAXNotRecognizedException] {
      parser.setProperty("http://xml.org/sax/properties/nonexistent", "value")
    }
  }

  test("namespace-aware parser reports namespace URIs") {
    val parser = newParser(namespaceAware = true)
    var capturedUri: String = null

    val handler = new DefaultHandler() {
      override def startElement(uri: String, localName: String, qName: String, atts: Attributes): Unit = {
        if (localName == "child" || qName == "ns:child") capturedUri = uri
      }
    }

    val xml = """<root xmlns:ns="http://example.com"><ns:child/></root>"""
    parser.parse(new InputSource(new StringReader(xml)), handler)

    assertEquals(capturedUri, "http://example.com")
  }
}
