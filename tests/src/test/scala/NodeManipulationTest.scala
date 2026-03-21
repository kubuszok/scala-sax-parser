package scalasaxparser

import munit.FunSuite

import scala.xml._

class NodeManipulationTest extends FunSuite {

  test("create Elem programmatically") {
    val elem = Elem(null, "tag", Null, TopScope, true)
    assertEquals(elem.label, "tag")
  }

  test("create Elem with children") {
    val child1 = <a/>
    val child2 = <b/>
    val parent = <parent>{child1}{child2}</parent>
    assertEquals(parent.child.length, 2)
  }

  test("copy element with new label") {
    val elem = <original/>
    val copy = elem.copy(label = "modified")
    assertEquals(copy.label, "modified")
  }

  test("add attribute with %") {
    val elem = <test/>
    val withAttr = elem % new UnprefixedAttribute("key", Text("value"), Null)
    assertEquals(withAttr \@ "key", "value")
  }

  test("NodeBuffer operations") {
    val buf = new NodeBuffer()
    buf += <a/>
    buf += <b/>
    buf += Text("hello")
    assertEquals(buf.length, 3)
  }

  test("NodeSeq operations") {
    val nodes: NodeSeq = <root><a/><b/><c/></root>.child
    assertEquals(nodes.length, 3)
  }

  test("create Text node") {
    val text = Text("hello world")
    assertEquals(text.text, "hello world")
  }

  test("create Comment node") {
    val comment = Comment("a comment")
    assertEquals(comment.commentText, "a comment")
  }

  test("create ProcInstr node") {
    val pi = ProcInstr("target", "data")
    assertEquals(pi.target, "target")
  }
}
