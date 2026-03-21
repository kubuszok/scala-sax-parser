package scalasaxparser

import munit.FunSuite

import scala.xml._

class EdgeCaseTest extends FunSuite {

  test("empty text content") {
    val elem = XML.loadString("<root></root>")
    assertEquals(elem.text, "")
  }

  test("whitespace-only content") {
    val elem = XML.loadString("<root>   </root>")
    assertEquals(elem.text.trim, "")
  }

  test("element with single character content") {
    val elem = XML.loadString("<root>x</root>")
    assertEquals(elem.text, "x")
  }

  test("deeply nested (10 levels)") {
    val xml = "<a>" * 10 + "deep" + "</a>" * 10
    val elem = XML.loadString(xml)
    assertEquals(elem.text, "deep")
  }

  test("many siblings") {
    val children = (1 to 100).map(i => s"<item>$i</item>").mkString
    val xml = s"<root>$children</root>"
    val elem = XML.loadString(xml)
    assertEquals(elem.child.length, 100)
  }

  test("attribute with empty value") {
    val elem = XML.loadString("""<root attr=""/>""")
    assertEquals((elem \@ "attr"), "")
  }

  test("element name with hyphen") {
    val elem = XML.loadString("<my-element/>")
    assertEquals(elem.label, "my-element")
  }

  test("element name with dot") {
    val elem = XML.loadString("<my.element/>")
    assertEquals(elem.label, "my.element")
  }

  test("element name with underscore") {
    val elem = XML.loadString("<my_element/>")
    assertEquals(elem.label, "my_element")
  }

  test("element name with digits") {
    val elem = XML.loadString("<item123/>")
    assertEquals(elem.label, "item123")
  }

  test("attribute with single quotes in source") {
    val elem = XML.loadString("<root attr='value'/>")
    assertEquals((elem \@ "attr"), "value")
  }

  test("multiple CDATA sections") {
    val elem = XML.loadString("<root><![CDATA[first]]><![CDATA[second]]></root>")
    assertEquals(elem.text, "firstsecond")
  }

  test("CDATA with special characters") {
    val elem = XML.loadString("<root><![CDATA[<>&\"']]></root>")
    assertEquals(elem.text, "<>&\"'")
  }

  test("unicode in element name") {
    val elem = XML.loadString("<données/>")
    assertEquals(elem.label, "données")
  }

  test("unicode in attribute value") {
    val elem = XML.loadString("""<root name="日本"/>""")
    assertEquals((elem \@ "name"), "日本")
  }

  test("multiple comments") {
    val elem = XML.loadString("<root><!-- c1 --><!-- c2 --><child/></root>")
    val comments = elem.child.collect { case c: Comment => c }
    assertEquals(comments.length, 2)
  }

  test("comment before root element") {
    // Note: XML.loadString returns the root element, comments before it are typically discarded
    val elem = XML.loadString("<!-- pre-comment --><root/>")
    assertEquals(elem.label, "root")
  }
}
