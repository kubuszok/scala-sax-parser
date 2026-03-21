package scalasaxparser

import munit.FunSuite
import org.xml.sax.InputSource
import java.io.{ByteArrayInputStream, InputStream, Reader, StringReader}

class InputSourceSpecTest extends FunSuite {

  test("default constructor: getPublicId returns null") {
    val is = new InputSource()
    assertEquals(is.getPublicId(), null)
  }

  test("default constructor: getSystemId returns null") {
    val is = new InputSource()
    assertEquals(is.getSystemId(), null)
  }

  test("default constructor: getByteStream returns null") {
    val is = new InputSource()
    assertEquals(is.getByteStream(), null)
  }

  test("default constructor: getCharacterStream returns null") {
    val is = new InputSource()
    assertEquals(is.getCharacterStream(), null)
  }

  test("default constructor: getEncoding returns null") {
    val is = new InputSource()
    assertEquals(is.getEncoding(), null)
  }

  test("constructor with systemId: getSystemId returns it") {
    val is = new InputSource("http://example.com/test.xml")
    assertEquals(is.getSystemId(), "http://example.com/test.xml")
  }

  test("constructor with systemId: other getters return null") {
    val is = new InputSource("http://example.com/test.xml")
    assertEquals(is.getPublicId(), null)
    assertEquals(is.getByteStream(), null)
    assertEquals(is.getCharacterStream(), null)
    assertEquals(is.getEncoding(), null)
  }

  test("constructor with InputStream: getByteStream returns it") {
    val stream = new ByteArrayInputStream(Array[Byte](1, 2, 3))
    val is = new InputSource(stream)
    assertEquals(is.getByteStream(), stream)
  }

  test("constructor with InputStream: other getters return null") {
    val stream = new ByteArrayInputStream(Array[Byte](1, 2, 3))
    val is = new InputSource(stream)
    assertEquals(is.getPublicId(), null)
    assertEquals(is.getSystemId(), null)
    assertEquals(is.getCharacterStream(), null)
    assertEquals(is.getEncoding(), null)
  }

  test("constructor with Reader: getCharacterStream returns it") {
    val reader = new StringReader("test")
    val is = new InputSource(reader)
    assertEquals(is.getCharacterStream(), reader)
  }

  test("constructor with Reader: other getters return null") {
    val reader = new StringReader("test")
    val is = new InputSource(reader)
    assertEquals(is.getPublicId(), null)
    assertEquals(is.getSystemId(), null)
    assertEquals(is.getByteStream(), null)
    assertEquals(is.getEncoding(), null)
  }

  test("setPublicId/getPublicId roundtrip") {
    val is = new InputSource()
    is.setPublicId("myPublicId")
    assertEquals(is.getPublicId(), "myPublicId")
  }

  test("setSystemId/getSystemId roundtrip") {
    val is = new InputSource()
    is.setSystemId("mySystemId")
    assertEquals(is.getSystemId(), "mySystemId")
  }

  test("setByteStream/getByteStream roundtrip") {
    val is = new InputSource()
    val stream = new ByteArrayInputStream(Array[Byte]())
    is.setByteStream(stream)
    assertEquals(is.getByteStream(), stream)
  }

  test("setCharacterStream/getCharacterStream roundtrip") {
    val is = new InputSource()
    val reader = new StringReader("data")
    is.setCharacterStream(reader)
    assertEquals(is.getCharacterStream(), reader)
  }

  test("setEncoding/getEncoding roundtrip") {
    val is = new InputSource()
    is.setEncoding("UTF-16")
    assertEquals(is.getEncoding(), "UTF-16")
  }

  test("setters override constructor values") {
    val is = new InputSource("original-system-id")
    is.setSystemId("new-system-id")
    assertEquals(is.getSystemId(), "new-system-id")
  }

  test("setPublicId with null") {
    val is = new InputSource()
    is.setPublicId("something")
    is.setPublicId(null)
    assertEquals(is.getPublicId(), null)
  }

  test("setEncoding with null") {
    val is = new InputSource()
    is.setEncoding("UTF-8")
    is.setEncoding(null)
    assertEquals(is.getEncoding(), null)
  }
}
