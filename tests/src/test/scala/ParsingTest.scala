package scalasaxparser

import munit.FunSuite

import scala.xml._
import scala.xml.factory.XMLLoader
import javax.xml.parsers.{SAXParser => JSAXParser, SAXParserFactory}

class ParsingTest extends FunSuite {

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

  test("parse simple element") {
    val elem = XML.loadString("<root/>")
    assertEquals(elem.label, "root")
    assertEquals(elem.child.length, 0)
  }

  test("parse element with text content") {
    val elem = XML.loadString("<hello>world</hello>")
    assertEquals(elem.label, "hello")
    assertEquals(elem.text, "world")
  }

  test("parse nested elements") {
    val elem = XML.loadString("<a><b><c>text</c></b></a>")
    assertEquals(elem.label, "a")
    assertEquals((elem \ "b" \ "c").text, "text")
  }

  test("parse element with attributes") {
    val elem = XML.loadString("""<person name="Alice" age="30"/>""")
    assertEquals(elem.label, "person")
    assertEquals((elem \@ "name"), "Alice")
    assertEquals((elem \@ "age"), "30")
  }

  test("parse mixed content") {
    val elem = XML.loadString("<p>Hello <b>world</b>!</p>")
    assertEquals(elem.label, "p")
    assert(elem.child.length >= 3)
    assertEquals(elem.text, "Hello world!")
  }

  test("parse CDATA section") {
    val elem = XML.loadString("<data><![CDATA[<not>xml</not>]]></data>")
    assertEquals(elem.text, "<not>xml</not>")
  }

  test("parse processing instruction") {
    val elem = XML.loadString("""<?xml version="1.0"?><root><?pi-target pi-data?></root>""")
    assertEquals(elem.label, "root")
    val pis = elem.child.collect { case pi: ProcInstr => pi }
    assertEquals(pis.length, 1)
    assertEquals(pis.head.target, "pi-target")
    assertEquals(pis.head.proctext, "pi-data")
  }

  test("parse comments") {
    val elem = XML.loadString("<root><!-- a comment --><child/></root>")
    assertEquals(elem.label, "root")
    val comments = elem.child.collect { case c: Comment => c }
    assertEquals(comments.length, 1)
    assertEquals(comments.head.commentText, " a comment ")
  }

  test("parse built-in entity references") {
    val elem = XML.loadString("<data>&amp; &lt; &gt; &apos; &quot;</data>")
    assertEquals(elem.text, "& < > ' \"")
  }

  test("parse character references (decimal)") {
    val elem = XML.loadString("<data>&#65;&#66;&#67;</data>")
    assertEquals(elem.text, "ABC")
  }

  test("parse character references (hex)") {
    val elem = XML.loadString("<data>&#x41;&#x42;&#x43;</data>")
    assertEquals(elem.text, "ABC")
  }

  test("parse entities in attributes") {
    val elem = XML.loadString("""<a val="1 &amp; 2"/>""")
    assertEquals((elem \@ "val"), "1 & 2")
  }

  test("parse XML declaration") {
    val elem = XML.loadString("""<?xml version="1.0" encoding="UTF-8"?><root/>""")
    assertEquals(elem.label, "root")
  }

  test("parse with DTD internal subset - entity") {
    val xml = """<?xml version="1.0"?>
      |<!DOCTYPE root [
      |  <!ENTITY greeting "Hello World">
      |]>
      |<root>&greeting;</root>""".stripMargin
    val elem = xmlWithDtd.loadString(xml)
    assertEquals(elem.text, "Hello World")
  }

  test("parse empty element - self-closing") {
    val elem = XML.loadString("<empty/>")
    assertEquals(elem.label, "empty")
    assertEquals(elem.child.length, 0)
  }

  test("parse empty element - open/close") {
    val elem = XML.loadString("<empty></empty>")
    assertEquals(elem.label, "empty")
    assertEquals(elem.child.length, 0)
  }

  test("parse multiple children") {
    val elem = XML.loadString("<root><a/><b/><c/></root>")
    assertEquals(elem.child.length, 3)
    assertEquals(elem.child(0).label, "a")
    assertEquals(elem.child(1).label, "b")
    assertEquals(elem.child(2).label, "c")
  }

  test("parse deeply nested") {
    val xml = "<a><b><c><d><e><f>deep</f></e></d></c></b></a>"
    val elem = XML.loadString(xml)
    assertEquals((elem \\ "f").text, "deep")
  }

  test("parse with whitespace") {
    val xml =
      """<root>
        |  <child/>
        |</root>""".stripMargin
    val elem = XML.loadString(xml)
    assertEquals(elem.label, "root")
    assert((elem \ "child").nonEmpty)
  }

  test("parse malformed XML throws exception") {
    intercept[Exception] {
      XML.loadString("<unclosed>")
    }
  }

  test("parse mismatched tags throws exception") {
    intercept[Exception] {
      XML.loadString("<a></b>")
    }
  }

  test("parse unicode content") {
    val elem = XML.loadString("<text>日本語テスト</text>")
    assertEquals(elem.text, "日本語テスト")
  }

  test("parse multiple attributes") {
    val elem = XML.loadString("""<el a="1" b="2" c="3"/>""")
    assertEquals((elem \@ "a"), "1")
    assertEquals((elem \@ "b"), "2")
    assertEquals((elem \@ "c"), "3")
  }

  test("parse from InputSource with Reader") {
    import org.xml.sax.InputSource
    import java.io.StringReader
    val source = new InputSource(new StringReader("<test/>"))
    val elem = XML.load(source)
    assertEquals(elem.label, "test")
  }
}
