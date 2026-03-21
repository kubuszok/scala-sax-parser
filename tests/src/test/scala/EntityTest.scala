package scalasaxparser

import munit.FunSuite

import scala.xml._
import scala.xml.factory.XMLLoader
import javax.xml.parsers.{SAXParser => JSAXParser, SAXParserFactory}

class EntityTest extends FunSuite {

  // XML loader that allows DOCTYPE declarations
  private val xmlWithDtd: XMLLoader[Elem] = new XMLLoader[Elem] {
    override def parser: JSAXParser = {
      val factory = SAXParserFactory.newInstance()
      factory.setNamespaceAware(true)
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false)
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
      factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
      factory.newSAXParser()
    }
  }

  test("built-in entities in content") {
    val elem = XML.loadString("<root>&amp;&lt;&gt;&apos;&quot;</root>")
    assertEquals(elem.text, "&<>'\"")
  }

  test("built-in entities in attributes") {
    val elem = XML.loadString("""<root val="a&amp;b"/>""")
    assertEquals((elem \@ "val"), "a&b")
  }

  test("decimal character reference") {
    val elem = XML.loadString("<root>&#72;&#101;&#108;&#108;&#111;</root>")
    assertEquals(elem.text, "Hello")
  }

  test("hex character reference") {
    val elem = XML.loadString("<root>&#x48;&#x65;&#x6C;&#x6C;&#x6F;</root>")
    assertEquals(elem.text, "Hello")
  }

  test("character reference for non-ASCII") {
    val elem = XML.loadString("<root>&#169;</root>") // copyright symbol
    assertEquals(elem.text, "\u00A9")
  }

  test("mixed entities and text") {
    val elem = XML.loadString("<root>a &amp; b &lt; c</root>")
    assertEquals(elem.text, "a & b < c")
  }

  test("entity in DTD internal subset") {
    val xml = """<?xml version="1.0"?>
      |<!DOCTYPE root [
      |  <!ENTITY myent "replacement text">
      |]>
      |<root>&myent;</root>""".stripMargin
    val elem = xmlWithDtd.loadString(xml)
    assertEquals(elem.text, "replacement text")
  }
}
