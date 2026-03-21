package scalasaxparser

import munit.FunSuite

import scala.xml._

class AttributeTest extends FunSuite {

  test("UnprefixedAttribute") {
    val attr = new UnprefixedAttribute("key", "value", Null)
    assertEquals(attr.key, "key")
    assertEquals(attr.value.text, "value")
  }

  test("PrefixedAttribute") {
    val attr = new PrefixedAttribute("ns", "key", "value", Null)
    assertEquals(attr.pre, "ns")
    assertEquals(attr.key, "key")
    assertEquals(attr.value.text, "value")
  }

  test("attribute iteration") {
    val elem = XML.loadString("""<el a="1" b="2" c="3"/>""")
    val keys = elem.attributes.map(_.key).toList
    assertEquals(keys.sorted, List("a", "b", "c"))
  }

  test("attribute lookup by key") {
    val elem = XML.loadString("""<el key="val"/>""")
    assertEquals(elem.attribute("key").map(_.text), Some("val"))
  }

  test("attribute not found returns None") {
    val elem = XML.loadString("""<el key="val"/>""")
    assertEquals(elem.attribute("missing"), None)
  }

  test("MetaData append") {
    val a1 = new UnprefixedAttribute("a", "1", Null)
    val a2 = new UnprefixedAttribute("b", "2", a1)
    assertEquals(a2.apply("a").text, "1")
    assertEquals(a2.apply("b").text, "2")
  }

  test("parse prefixed attributes") {
    val elem = XML.loadString("""<root xmlns:ns="http://example.com" ns:attr="val"/>""")
    val attr = elem.attribute("http://example.com", "attr")
    assert(attr.isDefined)
    assertEquals(attr.get.text, "val")
  }
}
