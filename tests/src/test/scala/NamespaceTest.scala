package scalasaxparser

import munit.FunSuite

import scala.xml._

class NamespaceTest extends FunSuite {

  test("parse default namespace") {
    val elem = XML.loadString("""<root xmlns="http://example.com"/>""")
    assertEquals(elem.namespace, "http://example.com")
  }

  test("parse prefixed namespace") {
    val elem = XML.loadString("""<ns:root xmlns:ns="http://example.com"/>""")
    assertEquals(elem.prefix, "ns")
    assertEquals(elem.namespace, "http://example.com")
  }

  test("parse multiple namespaces") {
    val xml = """<root xmlns:a="http://a.com" xmlns:b="http://b.com">
      |  <a:child/>
      |  <b:child/>
      |</root>""".stripMargin
    val elem = XML.loadString(xml)
    val children = elem.child.collect { case e: Elem => e }
    assertEquals(children(0).prefix, "a")
    assertEquals(children(1).prefix, "b")
  }

  test("namespace in child element") {
    val xml = """<root xmlns:ns="http://example.com"><ns:child>text</ns:child></root>"""
    val elem = XML.loadString(xml)
    val child = (elem \ "child").head.asInstanceOf[Elem]
    assertEquals(child.prefix, "ns")
    assertEquals(child.namespace, "http://example.com")
  }

  test("NamespaceBinding lookup") {
    val elem = XML.loadString("""<root xmlns:ns="http://example.com"/>""")
    val binding = elem.scope
    assertEquals(binding.getURI("ns"), "http://example.com")
  }

  test("XML literal with namespace") {
    val ns = "http://example.com"
    val elem = <root xmlns="http://example.com"><child/></root>
    assertEquals(elem.namespace, ns)
  }
}
