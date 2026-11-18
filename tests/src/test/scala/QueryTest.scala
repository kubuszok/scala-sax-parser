package scalasaxparser

import munit.FunSuite

import scala.xml._

class QueryTest extends FunSuite {

  val doc = XML.loadString(
    """<library>
      |  <book id="1">
      |    <title>Scala Programming</title>
      |    <author>Martin</author>
      |  </book>
      |  <book id="2">
      |    <title>FP in Scala</title>
      |    <author>Chiusano</author>
      |  </book>
      |  <magazine>
      |    <title>Scala Times</title>
      |  </magazine>
      |</library>""".stripMargin
  )

  test("child selector \\") {
    val books = doc \ "book"
    assertEquals(books.length, 2)
  }

  test("descendant selector \\\\") {
    val titles = doc \\ "title"
    assertEquals(titles.length, 3)
  }

  test("attribute selector \\@") {
    val books = doc \ "book"
    assertEquals((books.head \@ "id"), "1")
    assertEquals((books(1) \@ "id"), "2")
  }

  test("chained queries") {
    val authorNames = (doc \ "book" \ "author").map(_.text)
    assertEquals(authorNames.toList, List("Martin", "Chiusano"))
  }

  test("select by label") {
    val magazines = doc \ "magazine"
    assertEquals(magazines.length, 1)
    assertEquals((magazines \ "title").text, "Scala Times")
  }

  test("\\\\ finds at any depth") {
    val nested = XML.loadString("<a><b><c><d>found</d></c></b></a>")
    assertEquals((nested \\ "d").text, "found")
  }

  test("\\ returns empty for non-existent children") {
    val elem = XML.loadString("<root><child/></root>")
    assertEquals((elem \ "nonexistent").length, 0)
  }

  test("text of multiple results") {
    val titles = doc \\ "title"
    val allText = titles.map(_.text).toList
    assertEquals(allText, List("Scala Programming", "FP in Scala", "Scala Times"))
  }
}
