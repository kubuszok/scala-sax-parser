package scalasaxparser

import munit.FunSuite
import org.xml.sax._
import org.xml.sax.ext.{DeclHandler, LexicalHandler}
import org.xml.sax.helpers.DefaultHandler
import javax.xml.parsers.SAXParserFactory

import java.io.StringReader

class XMLReaderSpecTest extends FunSuite {

  private def newReader(): XMLReader = {
    val factory = SAXParserFactory.newInstance()
    factory.setNamespaceAware(true)
    factory.newSAXParser().getXMLReader()
  }

  private def inputFromString(xml: String): InputSource =
    new InputSource(new StringReader(xml))

  test("getFeature for namespaces returns true when factory is namespace-aware") {
    val reader = newReader()
    assertEquals(reader.getFeature("http://xml.org/sax/features/namespaces"), true)
  }

  test("getFeature for namespace-prefixes returns false when factory is namespace-aware") {
    val reader = newReader()
    assertEquals(reader.getFeature("http://xml.org/sax/features/namespace-prefixes"), false)
  }

  test("setFeature/getFeature roundtrip for namespaces") {
    val reader = newReader()
    reader.setFeature("http://xml.org/sax/features/namespaces", false)
    assertEquals(reader.getFeature("http://xml.org/sax/features/namespaces"), false)
    reader.setFeature("http://xml.org/sax/features/namespaces", true)
    assertEquals(reader.getFeature("http://xml.org/sax/features/namespaces"), true)
  }

  test("setFeature/getFeature roundtrip for namespace-prefixes") {
    val reader = newReader()
    reader.setFeature("http://xml.org/sax/features/namespace-prefixes", true)
    assertEquals(reader.getFeature("http://xml.org/sax/features/namespace-prefixes"), true)
  }

  test("getFeature for unrecognized feature throws SAXNotRecognizedException") {
    val reader = newReader()
    intercept[SAXNotRecognizedException] {
      reader.getFeature("http://xml.org/sax/features/nonexistent")
    }
  }

  test("setFeature for unrecognized feature throws SAXNotRecognizedException") {
    val reader = newReader()
    intercept[SAXNotRecognizedException] {
      reader.setFeature("http://xml.org/sax/features/nonexistent", true)
    }
  }

  test("setProperty/getProperty for lexical-handler") {
    val reader = newReader()
    val handler = new DefaultHandler() with LexicalHandler {
      def startDTD(name: String, publicId: String, systemId: String): Unit = ()
      def endDTD(): Unit = ()
      def startEntity(name: String): Unit = ()
      def endEntity(name: String): Unit = ()
      def startCDATA(): Unit = ()
      def endCDATA(): Unit = ()
      def comment(ch: Array[Char], start: Int, length: Int): Unit = ()
    }
    reader.setProperty("http://xml.org/sax/properties/lexical-handler", handler)
    val result = reader.getProperty("http://xml.org/sax/properties/lexical-handler")
    assertEquals(result eq handler, true)
  }

  test("setProperty/getProperty for declaration-handler") {
    val reader = newReader()
    val handler = new DeclHandler {
      def elementDecl(name: String, model: String): Unit = ()
      def attributeDecl(eName: String, aName: String, `type`: String, mode: String, value: String): Unit = ()
      def internalEntityDecl(name: String, value: String): Unit = ()
      def externalEntityDecl(name: String, publicId: String, systemId: String): Unit = ()
    }
    reader.setProperty("http://xml.org/sax/properties/declaration-handler", handler)
    val result = reader.getProperty("http://xml.org/sax/properties/declaration-handler")
    assertEquals(result eq handler, true)
  }

  test("getProperty for unrecognized property throws SAXNotRecognizedException") {
    val reader = newReader()
    intercept[SAXNotRecognizedException] {
      reader.getProperty("http://xml.org/sax/properties/nonexistent")
    }
  }

  test("setProperty for unrecognized property throws SAXNotRecognizedException") {
    val reader = newReader()
    intercept[SAXNotRecognizedException] {
      reader.setProperty("http://xml.org/sax/properties/nonexistent", "value")
    }
  }

  test("setContentHandler/getContentHandler roundtrip") {
    val reader = newReader()
    val handler = new DefaultHandler()
    reader.setContentHandler(handler)
    assertEquals(reader.getContentHandler() eq handler, true)
  }

  test("setErrorHandler/getErrorHandler roundtrip") {
    val reader = newReader()
    val handler = new DefaultHandler()
    reader.setErrorHandler(handler)
    assertEquals(reader.getErrorHandler() eq handler, true)
  }

  test("setEntityResolver/getEntityResolver roundtrip") {
    val reader = newReader()
    val resolver = new DefaultHandler()
    reader.setEntityResolver(resolver)
    assertEquals(reader.getEntityResolver() eq resolver, true)
  }

  test("setDTDHandler/getDTDHandler roundtrip") {
    val reader = newReader()
    val handler = new DefaultHandler()
    reader.setDTDHandler(handler)
    assertEquals(reader.getDTDHandler() eq handler, true)
  }

  test("getContentHandler returns null initially") {
    val factory = SAXParserFactory.newInstance()
    factory.setNamespaceAware(true)
    // After newSAXParser, the reader has no content handler set yet
    // Note: the parser may set handlers during parse(), but getXMLReader()
    // before parse should have null content handler
    val reader = factory.newSAXParser().getXMLReader()
    // Content handler is null before any parse call sets it
    val ch = reader.getContentHandler()
    assertEquals(ch, null)
  }

  test("parse fires startDocument before other events") {
    val reader = newReader()
    val events = scala.collection.mutable.ListBuffer[String]()

    reader.setContentHandler(new DefaultHandler() {
      override def startDocument(): Unit = events += "startDocument"
      override def startElement(uri: String, localName: String, qName: String, atts: Attributes): Unit =
        events += s"startElement:$qName"
    })

    reader.parse(inputFromString("<root/>"))

    assertEquals(events.nonEmpty, true)
    assertEquals(events.head, "startDocument")
  }

  test("parse fires endDocument last") {
    val reader = newReader()
    val events = scala.collection.mutable.ListBuffer[String]()

    reader.setContentHandler(new DefaultHandler() {
      override def startDocument(): Unit = events += "startDocument"
      override def endDocument(): Unit = events += "endDocument"
      override def startElement(uri: String, localName: String, qName: String, atts: Attributes): Unit =
        events += "startElement"
      override def endElement(uri: String, localName: String, qName: String): Unit =
        events += "endElement"
    })

    reader.parse(inputFromString("<root/>"))

    assertEquals(events.last, "endDocument")
  }

  test("parse fires startElement/endElement pairs") {
    val reader = newReader()
    val events = scala.collection.mutable.ListBuffer[String]()

    reader.setContentHandler(new DefaultHandler() {
      override def startElement(uri: String, localName: String, qName: String, atts: Attributes): Unit =
        events += s"start:$qName"
      override def endElement(uri: String, localName: String, qName: String): Unit =
        events += s"end:$qName"
    })

    reader.parse(inputFromString("<root><child/></root>"))

    assertEquals(events.contains("start:root"), true)
    assertEquals(events.contains("end:root"), true)
    assertEquals(events.contains("start:child"), true)
    assertEquals(events.contains("end:child"), true)

    val startRoot = events.indexOf("start:root")
    val endRoot = events.indexOf("end:root")
    val startChild = events.indexOf("start:child")
    val endChild = events.indexOf("end:child")

    assertEquals(startRoot < startChild, true)
    assertEquals(startChild < endChild, true)
    assertEquals(endChild < endRoot, true)
  }
}
