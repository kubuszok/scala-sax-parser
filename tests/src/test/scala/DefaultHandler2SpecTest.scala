package scalasaxparser

import munit.FunSuite
import org.xml.sax.InputSource
import org.xml.sax.ext.DefaultHandler2

class DefaultHandler2SpecTest extends FunSuite {

  test("startDTD is no-op") {
    val handler = new DefaultHandler2()
    handler.startDTD("root", "pub", "sys")
  }

  test("endDTD is no-op") {
    val handler = new DefaultHandler2()
    handler.endDTD()
  }

  test("startEntity is no-op") {
    val handler = new DefaultHandler2()
    handler.startEntity("entity")
  }

  test("endEntity is no-op") {
    val handler = new DefaultHandler2()
    handler.endEntity("entity")
  }

  test("startCDATA is no-op") {
    val handler = new DefaultHandler2()
    handler.startCDATA()
  }

  test("endCDATA is no-op") {
    val handler = new DefaultHandler2()
    handler.endCDATA()
  }

  test("comment is no-op") {
    val handler = new DefaultHandler2()
    handler.comment("a comment".toCharArray, 0, 9)
  }

  test("elementDecl is no-op") {
    val handler = new DefaultHandler2()
    handler.elementDecl("elem", "(#PCDATA)")
  }

  test("attributeDecl is no-op") {
    val handler = new DefaultHandler2()
    handler.attributeDecl("elem", "attr", "CDATA", "#REQUIRED", null)
  }

  test("internalEntityDecl is no-op") {
    val handler = new DefaultHandler2()
    handler.internalEntityDecl("entity", "value")
  }

  test("externalEntityDecl is no-op") {
    val handler = new DefaultHandler2()
    handler.externalEntityDecl("entity", "pub", "sys")
  }

  test("getExternalSubset returns null") {
    val handler = new DefaultHandler2()
    assertEquals(handler.getExternalSubset("root", "http://base"), null)
  }

  test("resolveEntity(4-arg) returns null") {
    val handler = new DefaultHandler2()
    assertEquals(handler.resolveEntity("name", "pub", "http://base", "sys"), null)
  }

  test("resolveEntity(2-arg) delegates to 4-arg version") {
    var calledWith: (String, String, String, String) = null

    val handler = new DefaultHandler2() {
      override def resolveEntity(
          name: String,
          publicId: String,
          baseURI: String,
          systemId: String
      ): InputSource = {
        calledWith = (name, publicId, baseURI, systemId)
        null
      }
    }

    handler.resolveEntity("myPublic", "mySystem")

    assertNotEquals(calledWith, null)
    assertEquals(calledWith._1, null)
    assertEquals(calledWith._2, "myPublic")
    assertEquals(calledWith._3, null)
    assertEquals(calledWith._4, "mySystem")
  }

  test("resolveEntity(2-arg) returns result from 4-arg version") {
    val expectedSource = new InputSource("http://test.xml")

    val handler = new DefaultHandler2() {
      override def resolveEntity(
          name: String,
          publicId: String,
          baseURI: String,
          systemId: String
      ): InputSource = expectedSource
    }

    val result = handler.resolveEntity("pub", "sys")
    assertEquals(result eq expectedSource, true)
  }

  test("DefaultHandler2 extends DefaultHandler") {
    val handler = new DefaultHandler2()
    assertEquals(handler.isInstanceOf[org.xml.sax.helpers.DefaultHandler], true)
  }

  test("DefaultHandler2 implements LexicalHandler") {
    val handler = new DefaultHandler2()
    assertEquals(handler.isInstanceOf[org.xml.sax.ext.LexicalHandler], true)
  }

  test("DefaultHandler2 implements DeclHandler") {
    val handler = new DefaultHandler2()
    assertEquals(handler.isInstanceOf[org.xml.sax.ext.DeclHandler], true)
  }

  test("DefaultHandler2 implements EntityResolver2") {
    val handler = new DefaultHandler2()
    assertEquals(handler.isInstanceOf[org.xml.sax.ext.EntityResolver2], true)
  }
}
