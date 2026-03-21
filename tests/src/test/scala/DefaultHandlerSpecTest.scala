package scalasaxparser

import munit.FunSuite
import org.xml.sax.SAXParseException
import org.xml.sax.helpers.{AttributesImpl, DefaultHandler}

class DefaultHandlerSpecTest extends FunSuite {

  test("resolveEntity returns null") {
    val handler = new DefaultHandler()
    assertEquals(handler.resolveEntity("pub", "sys"), null)
  }

  test("notationDecl is no-op (does not throw)") {
    val handler = new DefaultHandler()
    handler.notationDecl("name", "pub", "sys")
  }

  test("unparsedEntityDecl is no-op (does not throw)") {
    val handler = new DefaultHandler()
    handler.unparsedEntityDecl("name", "pub", "sys", "notation")
  }

  test("setDocumentLocator is no-op") {
    val handler = new DefaultHandler()
    handler.setDocumentLocator(null)
  }

  test("startDocument is no-op") {
    val handler = new DefaultHandler()
    handler.startDocument()
  }

  test("endDocument is no-op") {
    val handler = new DefaultHandler()
    handler.endDocument()
  }

  test("startPrefixMapping is no-op") {
    val handler = new DefaultHandler()
    handler.startPrefixMapping("pre", "http://ns")
  }

  test("endPrefixMapping is no-op") {
    val handler = new DefaultHandler()
    handler.endPrefixMapping("pre")
  }

  test("startElement is no-op") {
    val handler = new DefaultHandler()
    handler.startElement("", "root", "root", new AttributesImpl())
  }

  test("endElement is no-op") {
    val handler = new DefaultHandler()
    handler.endElement("", "root", "root")
  }

  test("characters is no-op") {
    val handler = new DefaultHandler()
    handler.characters("hello".toCharArray, 0, 5)
  }

  test("ignorableWhitespace is no-op") {
    val handler = new DefaultHandler()
    handler.ignorableWhitespace(" ".toCharArray, 0, 1)
  }

  test("processingInstruction is no-op") {
    val handler = new DefaultHandler()
    handler.processingInstruction("target", "data")
  }

  test("skippedEntity is no-op") {
    val handler = new DefaultHandler()
    handler.skippedEntity("entity")
  }

  test("warning is no-op") {
    val handler = new DefaultHandler()
    val ex = new SAXParseException("warn", null, null, -1, -1)
    handler.warning(ex)
  }

  test("error is no-op") {
    val handler = new DefaultHandler()
    val ex = new SAXParseException("err", null, null, -1, -1)
    handler.error(ex)
  }

  test("fatalError THROWS the SAXParseException") {
    val handler = new DefaultHandler()
    val ex = new SAXParseException("fatal", null, null, 1, 1)
    val thrown = intercept[SAXParseException] {
      handler.fatalError(ex)
    }
    assertEquals(thrown eq ex, true)
  }

  test("fatalError throws exception with correct message") {
    val handler = new DefaultHandler()
    val ex = new SAXParseException("specific fatal message", null, null, -1, -1)
    val thrown = intercept[SAXParseException] {
      handler.fatalError(ex)
    }
    assertEquals(thrown.getMessage(), "specific fatal message")
  }
}
