package scalasaxparser

import munit.FunSuite
import org.xml.sax.SAXException

class SAXExceptionSpecTest extends FunSuite {

  test("constructor with no args: getMessage returns null") {
    val ex = new SAXException()
    assertEquals(ex.getMessage(), null)
  }

  test("constructor with message: getMessage returns that message") {
    val ex = new SAXException("test message")
    assertEquals(ex.getMessage(), "test message")
  }

  test("constructor with Exception: getMessage returns cause's toString") {
    val cause = new RuntimeException("inner error")
    val ex = new SAXException(cause)
    assertEquals(ex.getMessage(), cause.toString)
  }

  test("constructor with Exception: getException returns the cause") {
    val cause = new RuntimeException("inner error")
    val ex = new SAXException(cause)
    assertEquals(ex.getException() eq cause, true)
  }

  test("constructor with Exception: getCause returns the cause") {
    val cause = new RuntimeException("inner error")
    val ex = new SAXException(cause)
    assertEquals(ex.getCause eq cause, true)
  }

  test("constructor with message and Exception: getMessage returns own message") {
    val cause = new RuntimeException("inner error")
    val ex = new SAXException("outer message", cause)
    assertEquals(ex.getMessage(), "outer message")
  }

  test("constructor with message and Exception: getException returns cause") {
    val cause = new RuntimeException("inner error")
    val ex = new SAXException("outer message", cause)
    assertEquals(ex.getException() eq cause, true)
  }

  test("constructor with message and Exception: getCause returns cause") {
    val cause = new RuntimeException("inner error")
    val ex = new SAXException("outer message", cause)
    assertEquals(ex.getCause eq cause, true)
  }

  test("toString includes embedded exception info when cause present") {
    val cause = new RuntimeException("inner error")
    val ex = new SAXException("outer message", cause)
    val str = ex.toString()
    assertEquals(str.contains("outer message"), true)
    assertEquals(str.contains("inner error"), true)
  }

  test("toString works when no cause") {
    val ex = new SAXException("just a message")
    val str = ex.toString()
    assertEquals(str.contains("just a message"), true)
  }

  test("getException returns null when no cause") {
    val ex = new SAXException("no cause")
    assertEquals(ex.getException(), null)
  }

  test("getCause returns null when no cause") {
    val ex = new SAXException("no cause")
    assertEquals(ex.getCause, null)
  }

  test("getException returns null for default constructor") {
    val ex = new SAXException()
    assertEquals(ex.getException(), null)
  }

  test("constructor with null Exception: getMessage returns null") {
    val ex = new SAXException(null: Exception)
    assertEquals(ex.getMessage(), null)
  }

  test("SAXException is an Exception") {
    val ex = new SAXException("test")
    assertEquals(ex.isInstanceOf[Exception], true)
  }
}
