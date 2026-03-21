package scalasaxparser

import munit.FunSuite
import javax.xml.parsers.SAXParserFactory

class SAXParserFactorySpecTest extends FunSuite {

  test("newInstance returns non-null factory") {
    val factory = SAXParserFactory.newInstance()
    assertNotEquals(factory, null)
  }

  test("default isNamespaceAware is false") {
    val factory = SAXParserFactory.newInstance()
    assertEquals(factory.isNamespaceAware(), false)
  }

  test("default isValidating is false") {
    val factory = SAXParserFactory.newInstance()
    assertEquals(factory.isValidating(), false)
  }

  test("setNamespaceAware/isNamespaceAware roundtrip: true") {
    val factory = SAXParserFactory.newInstance()
    factory.setNamespaceAware(true)
    assertEquals(factory.isNamespaceAware(), true)
  }

  test("setNamespaceAware/isNamespaceAware roundtrip: false") {
    val factory = SAXParserFactory.newInstance()
    factory.setNamespaceAware(true)
    factory.setNamespaceAware(false)
    assertEquals(factory.isNamespaceAware(), false)
  }

  test("setValidating/isValidating roundtrip: true") {
    val factory = SAXParserFactory.newInstance()
    factory.setValidating(true)
    assertEquals(factory.isValidating(), true)
  }

  test("setValidating/isValidating roundtrip: false") {
    val factory = SAXParserFactory.newInstance()
    factory.setValidating(true)
    factory.setValidating(false)
    assertEquals(factory.isValidating(), false)
  }

  test("newSAXParser returns non-null parser") {
    val factory = SAXParserFactory.newInstance()
    val parser = factory.newSAXParser()
    assertNotEquals(parser, null)
  }

  test("parser created with namespace awareness false reflects factory setting") {
    val factory = SAXParserFactory.newInstance()
    factory.setNamespaceAware(false)
    val parser = factory.newSAXParser()
    assertEquals(parser.isNamespaceAware(), false)
  }

  test("parser created with namespace awareness true reflects factory setting") {
    val factory = SAXParserFactory.newInstance()
    factory.setNamespaceAware(true)
    val parser = factory.newSAXParser()
    assertEquals(parser.isNamespaceAware(), true)
  }

  test("parser created reflects validating setting") {
    val factory = SAXParserFactory.newInstance()
    factory.setValidating(false)
    val parser = factory.newSAXParser()
    assertEquals(parser.isValidating(), false)
  }

  test("setFeature/getFeature roundtrip") {
    val factory = SAXParserFactory.newInstance()
    factory.setFeature("http://xml.org/sax/features/namespaces", true)
    assertEquals(factory.getFeature("http://xml.org/sax/features/namespaces"), true)
  }

  test("newDefaultInstance returns non-null factory") {
    val factory = SAXParserFactory.newDefaultInstance()
    assertNotEquals(factory, null)
  }

  test("newNSInstance returns namespace-aware factory") {
    val factory = SAXParserFactory.newNSInstance()
    assertEquals(factory.isNamespaceAware(), true)
  }

  test("multiple parsers from same factory are independent") {
    val factory = SAXParserFactory.newInstance()
    factory.setNamespaceAware(true)
    val parser1 = factory.newSAXParser()
    val parser2 = factory.newSAXParser()
    assertEquals(parser1 ne parser2, true)
    assertEquals(parser1.getXMLReader() ne parser2.getXMLReader(), true)
  }
}
