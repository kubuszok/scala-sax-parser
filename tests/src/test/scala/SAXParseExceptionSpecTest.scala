package scalasaxparser

import munit.FunSuite
import org.xml.sax.{Locator, SAXParseException}

class SAXParseExceptionSpecTest extends FunSuite {

  private class TestLocator(
      pub: String,
      sys: String,
      ln: Int,
      col: Int
  ) extends Locator {
    def getPublicId(): String = pub
    def getSystemId(): String = sys
    def getLineNumber(): Int = ln
    def getColumnNumber(): Int = col
  }

  test("constructor with Locator: extracts publicId") {
    val locator = new TestLocator("pub1", "sys1", 10, 20)
    val ex = new SAXParseException("msg", locator)
    assertEquals(ex.getPublicId(), "pub1")
  }

  test("constructor with Locator: extracts systemId") {
    val locator = new TestLocator("pub1", "sys1", 10, 20)
    val ex = new SAXParseException("msg", locator)
    assertEquals(ex.getSystemId(), "sys1")
  }

  test("constructor with Locator: extracts lineNumber") {
    val locator = new TestLocator("pub1", "sys1", 10, 20)
    val ex = new SAXParseException("msg", locator)
    assertEquals(ex.getLineNumber(), 10)
  }

  test("constructor with Locator: extracts columnNumber") {
    val locator = new TestLocator("pub1", "sys1", 10, 20)
    val ex = new SAXParseException("msg", locator)
    assertEquals(ex.getColumnNumber(), 20)
  }

  test("constructor with null Locator: publicId is null") {
    val ex = new SAXParseException("msg", null: Locator)
    assertEquals(ex.getPublicId(), null)
  }

  test("constructor with null Locator: systemId is null") {
    val ex = new SAXParseException("msg", null: Locator)
    assertEquals(ex.getSystemId(), null)
  }

  test("constructor with null Locator: lineNumber is -1") {
    val ex = new SAXParseException("msg", null: Locator)
    assertEquals(ex.getLineNumber(), -1)
  }

  test("constructor with null Locator: columnNumber is -1") {
    val ex = new SAXParseException("msg", null: Locator)
    assertEquals(ex.getColumnNumber(), -1)
  }

  test("constructor with explicit location fields: getters return exact values") {
    val ex = new SAXParseException("msg", "myPub", "mySys", 42, 99)
    assertEquals(ex.getPublicId(), "myPub")
    assertEquals(ex.getSystemId(), "mySys")
    assertEquals(ex.getLineNumber(), 42)
    assertEquals(ex.getColumnNumber(), 99)
  }

  test("getLineNumber returns -1 when not available") {
    val ex = new SAXParseException("msg", null, null, -1, -1)
    assertEquals(ex.getLineNumber(), -1)
  }

  test("getColumnNumber returns -1 when not available") {
    val ex = new SAXParseException("msg", null, null, -1, -1)
    assertEquals(ex.getColumnNumber(), -1)
  }

  test("getPublicId returns null when not available") {
    val ex = new SAXParseException("msg", null, null, 1, 1)
    assertEquals(ex.getPublicId(), null)
  }

  test("getSystemId returns null when not available") {
    val ex = new SAXParseException("msg", null, null, 1, 1)
    assertEquals(ex.getSystemId(), null)
  }

  test("toString includes location information when available") {
    val ex = new SAXParseException("error here", "pub", "sys", 5, 10)
    val str = ex.toString()
    assertEquals(str.contains("5"), true)
    assertEquals(str.contains("10"), true)
  }

  test("toString includes systemId when available") {
    val ex = new SAXParseException("error", null, "file.xml", 1, 1)
    val str = ex.toString()
    assertEquals(str.contains("file.xml"), true)
  }

  test("toString includes publicId when available") {
    val ex = new SAXParseException("error", "pubId", null, 1, 1)
    val str = ex.toString()
    assertEquals(str.contains("pubId"), true)
  }

  test("toString includes message") {
    val ex = new SAXParseException("my error message", null, null, -1, -1)
    val str = ex.toString()
    assertEquals(str.contains("my error message"), true)
  }

  test("constructor with Locator and cause: preserves cause") {
    val locator = new TestLocator("p", "s", 1, 2)
    val cause = new RuntimeException("root cause")
    val ex = new SAXParseException("msg", locator, cause)
    assertEquals(ex.getException() eq cause, true)
    assertEquals(ex.getPublicId(), "p")
    assertEquals(ex.getSystemId(), "s")
  }

  test("constructor with 6 args: preserves cause") {
    val cause = new RuntimeException("root cause")
    val ex = new SAXParseException("msg", "pub", "sys", 3, 4, cause)
    assertEquals(ex.getException() eq cause, true)
    assertEquals(ex.getMessage(), "msg")
  }

  test("SAXParseException extends SAXException") {
    val ex = new SAXParseException("msg", null, null, -1, -1)
    assertEquals(ex.isInstanceOf[org.xml.sax.SAXException], true)
  }
}
