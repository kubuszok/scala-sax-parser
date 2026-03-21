package scalasaxparser

import munit.FunSuite

import scala.xml._

class RoundTripTest extends FunSuite {

  private def roundTrip(xml: String): Unit = {
    val parsed1 = XML.loadString(xml)
    val serialized = parsed1.toString
    val parsed2 = XML.loadString(serialized)
    assertEquals(parsed1.toString, parsed2.toString)
  }

  test("roundtrip simple element") {
    roundTrip("<root/>")
  }

  test("roundtrip element with text") {
    roundTrip("<hello>world</hello>")
  }

  test("roundtrip nested elements") {
    roundTrip("<a><b><c>text</c></b></a>")
  }

  test("roundtrip with attributes") {
    roundTrip("""<person name="Alice" age="30"/>""")
  }

  test("roundtrip mixed content") {
    roundTrip("<p>Hello <b>world</b>!</p>")
  }

  test("roundtrip multiple children") {
    roundTrip("<root><a>1</a><b>2</b><c>3</c></root>")
  }

  test("roundtrip with entities") {
    val elem = XML.loadString("<data>&amp; &lt; &gt;</data>")
    val serialized = elem.toString
    val parsed = XML.loadString(serialized)
    assertEquals(elem.text, parsed.text)
  }

  test("roundtrip with namespace") {
    val xml = """<root xmlns:ns="http://example.com"><ns:child>text</ns:child></root>"""
    val parsed1 = XML.loadString(xml)
    val serialized = parsed1.toString
    val parsed2 = XML.loadString(serialized)
    assertEquals(parsed1.label, parsed2.label)
    assertEquals(
      (parsed1 \ "child").head.asInstanceOf[Elem].prefix,
      (parsed2 \ "child").head.asInstanceOf[Elem].prefix
    )
  }

  test("roundtrip complex document") {
    val xml = """<library>
      |  <book id="1" lang="en">
      |    <title>Scala Programming</title>
      |    <author>Odersky</author>
      |    <year>2021</year>
      |  </book>
      |  <book id="2" lang="en">
      |    <title>FP in Scala</title>
      |    <author>Chiusano</author>
      |    <year>2020</year>
      |  </book>
      |</library>""".stripMargin
    val parsed1 = XML.loadString(xml)
    val serialized = parsed1.toString
    val parsed2 = XML.loadString(serialized)
    assertEquals((parsed1 \\ "title").map(_.text).toList, (parsed2 \\ "title").map(_.text).toList)
    assertEquals((parsed1 \\ "author").map(_.text).toList, (parsed2 \\ "author").map(_.text).toList)
  }
}
