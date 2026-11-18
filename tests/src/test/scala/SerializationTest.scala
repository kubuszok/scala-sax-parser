package scalasaxparser

import munit.FunSuite

import scala.xml._

class SerializationTest extends FunSuite {

  test("toString simple element") {
    val elem = <root/>
    assert(elem.toString.contains("root"))
  }

  test("toString element with text") {
    val elem = <hello>world</hello>
    assertEquals(elem.toString, "<hello>world</hello>")
  }

  test("toString element with attributes") {
    val elem = <person name="Alice"/>
    assert(elem.toString.contains("""name="Alice""""))
  }

  test("toString nested elements") {
    val elem = <a><b>text</b></a>
    assertEquals(elem.toString, "<a><b>text</b></a>")
  }

  test("PrettyPrinter basic formatting") {
    val pp = new PrettyPrinter(80, 2)
    val elem = <root><child>text</child></root>
    val result = pp.format(elem)
    assert(result.contains("root"))
    assert(result.contains("child"))
  }

  test("special characters escaped in text") {
    val elem = <data>{"<>&\""}</data>
    val s = elem.toString
    assert(s.contains("&lt;") || s.contains("<![CDATA["))
    assert(s.contains("&gt;") || s.contains("<![CDATA["))
    assert(s.contains("&amp;") || s.contains("<![CDATA["))
  }

  test("special characters escaped in attributes") {
    val elem = <a val={"a&b"}/>
    val s = elem.toString
    assert(s.contains("&amp;"))
  }

  test("Utility.trim removes whitespace-only nodes") {
    val elem = XML.loadString("<root>  <child/>  </root>")
    val trimmed = Utility.trim(elem)
    // After trimming, whitespace-only text nodes should be gone
    assertEquals(trimmed.child.count(_.isInstanceOf[Elem]), 1)
  }

  test("serialize and parse roundtrip preserves structure") {
    val original = <root attr="val"><child>text</child><other/></root>
    val serialized = original.toString
    val parsed = XML.loadString(serialized)
    assertEquals(parsed.label, original.label)
    assertEquals((parsed \@ "attr"), "val")
    assertEquals((parsed \ "child").text, "text")
    assertEquals((parsed \ "other").length, 1)
  }
}
