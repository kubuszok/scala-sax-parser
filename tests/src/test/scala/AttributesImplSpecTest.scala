package scalasaxparser

import munit.FunSuite
import org.xml.sax.helpers.AttributesImpl

class AttributesImplSpecTest extends FunSuite {

  test("default constructor: length is 0") {
    val atts = new AttributesImpl()
    assertEquals(atts.getLength(), 0)
  }

  test("copy constructor: copies all attributes") {
    val src = new AttributesImpl()
    src.addAttribute("http://ns", "local1", "pre:local1", "CDATA", "val1")
    src.addAttribute("", "local2", "local2", "ID", "val2")

    val copy = new AttributesImpl(src)
    assertEquals(copy.getLength(), 2)
    assertEquals(copy.getURI(0), "http://ns")
    assertEquals(copy.getLocalName(0), "local1")
    assertEquals(copy.getQName(0), "pre:local1")
    assertEquals(copy.getType(0), "CDATA")
    assertEquals(copy.getValue(0), "val1")
    assertEquals(copy.getURI(1), "")
    assertEquals(copy.getLocalName(1), "local2")
    assertEquals(copy.getType(1), "ID")
    assertEquals(copy.getValue(1), "val2")
  }

  test("addAttribute: increases length") {
    val atts = new AttributesImpl()
    atts.addAttribute("", "a", "a", "CDATA", "1")
    assertEquals(atts.getLength(), 1)
    atts.addAttribute("", "b", "b", "CDATA", "2")
    assertEquals(atts.getLength(), 2)
  }

  test("addAttribute: values retrievable by index") {
    val atts = new AttributesImpl()
    atts.addAttribute("http://ns", "name", "ns:name", "CDATA", "value")
    assertEquals(atts.getURI(0), "http://ns")
    assertEquals(atts.getLocalName(0), "name")
    assertEquals(atts.getQName(0), "ns:name")
    assertEquals(atts.getType(0), "CDATA")
    assertEquals(atts.getValue(0), "value")
  }

  test("getURI returns null for out-of-range index (negative)") {
    val atts = new AttributesImpl()
    assertEquals(atts.getURI(-1), null)
  }

  test("getURI returns null for out-of-range index (too large)") {
    val atts = new AttributesImpl()
    assertEquals(atts.getURI(0), null)
  }

  test("getLocalName returns null for out-of-range index") {
    val atts = new AttributesImpl()
    assertEquals(atts.getLocalName(5), null)
  }

  test("getQName returns null for out-of-range index") {
    val atts = new AttributesImpl()
    assertEquals(atts.getQName(-1), null)
  }

  test("getType returns null for out-of-range index") {
    val atts = new AttributesImpl()
    assertEquals(atts.getType(100), null)
  }

  test("getValue returns null for out-of-range index") {
    val atts = new AttributesImpl()
    assertEquals(atts.getValue(0), null)
  }

  test("getIndex(uri, localName) returns -1 when not found") {
    val atts = new AttributesImpl()
    atts.addAttribute("http://ns", "name", "ns:name", "CDATA", "value")
    assertEquals(atts.getIndex("http://other", "name"), -1)
    assertEquals(atts.getIndex("http://ns", "other"), -1)
  }

  test("getIndex(uri, localName) returns correct index when found") {
    val atts = new AttributesImpl()
    atts.addAttribute("http://ns", "first", "ns:first", "CDATA", "v1")
    atts.addAttribute("http://ns", "second", "ns:second", "CDATA", "v2")
    assertEquals(atts.getIndex("http://ns", "second"), 1)
  }

  test("getIndex(qName) returns -1 when not found") {
    val atts = new AttributesImpl()
    atts.addAttribute("", "name", "name", "CDATA", "value")
    assertEquals(atts.getIndex("notfound"), -1)
  }

  test("getIndex(qName) returns correct index when found") {
    val atts = new AttributesImpl()
    atts.addAttribute("", "a", "a", "CDATA", "1")
    atts.addAttribute("", "b", "b", "CDATA", "2")
    assertEquals(atts.getIndex("b"), 1)
  }

  test("getType(uri, localName) returns null when not found") {
    val atts = new AttributesImpl()
    assertEquals(atts.getType("http://ns", "name"), null)
  }

  test("getType(uri, localName) returns type when found") {
    val atts = new AttributesImpl()
    atts.addAttribute("http://ns", "name", "ns:name", "ID", "value")
    assertEquals(atts.getType("http://ns", "name"), "ID")
  }

  test("getType(qName) returns null when not found") {
    val atts = new AttributesImpl()
    assertEquals(atts.getType("missing"), null)
  }

  test("getType(qName) returns type when found") {
    val atts = new AttributesImpl()
    atts.addAttribute("", "name", "name", "NMTOKEN", "value")
    assertEquals(atts.getType("name"), "NMTOKEN")
  }

  test("getValue(uri, localName) returns null when not found") {
    val atts = new AttributesImpl()
    assertEquals(atts.getValue("http://ns", "name"), null)
  }

  test("getValue(uri, localName) returns value when found") {
    val atts = new AttributesImpl()
    atts.addAttribute("http://ns", "name", "ns:name", "CDATA", "thevalue")
    assertEquals(atts.getValue("http://ns", "name"), "thevalue")
  }

  test("getValue(qName) returns null when not found") {
    val atts = new AttributesImpl()
    assertEquals(atts.getValue("missing"), null)
  }

  test("getValue(qName) returns value when found") {
    val atts = new AttributesImpl()
    atts.addAttribute("", "key", "key", "CDATA", "thevalue")
    assertEquals(atts.getValue("key"), "thevalue")
  }

  test("clear: resets length to 0") {
    val atts = new AttributesImpl()
    atts.addAttribute("", "a", "a", "CDATA", "1")
    atts.addAttribute("", "b", "b", "CDATA", "2")
    assertEquals(atts.getLength(), 2)
    atts.clear()
    assertEquals(atts.getLength(), 0)
  }

  test("setAttributes: copies from another Attributes") {
    val src = new AttributesImpl()
    src.addAttribute("", "x", "x", "CDATA", "xval")

    val dest = new AttributesImpl()
    dest.addAttribute("", "old", "old", "CDATA", "oldval")
    dest.setAttributes(src)

    assertEquals(dest.getLength(), 1)
    assertEquals(dest.getQName(0), "x")
    assertEquals(dest.getValue(0), "xval")
  }

  test("removeAttribute: removes and shifts") {
    val atts = new AttributesImpl()
    atts.addAttribute("", "a", "a", "CDATA", "1")
    atts.addAttribute("", "b", "b", "CDATA", "2")
    atts.addAttribute("", "c", "c", "CDATA", "3")

    atts.removeAttribute(1) // remove "b"

    assertEquals(atts.getLength(), 2)
    assertEquals(atts.getQName(0), "a")
    assertEquals(atts.getValue(0), "1")
    assertEquals(atts.getQName(1), "c")
    assertEquals(atts.getValue(1), "3")
  }

  test("removeAttribute: removes first element correctly") {
    val atts = new AttributesImpl()
    atts.addAttribute("", "a", "a", "CDATA", "1")
    atts.addAttribute("", "b", "b", "CDATA", "2")

    atts.removeAttribute(0)

    assertEquals(atts.getLength(), 1)
    assertEquals(atts.getQName(0), "b")
  }

  test("removeAttribute: removes last element correctly") {
    val atts = new AttributesImpl()
    atts.addAttribute("", "a", "a", "CDATA", "1")
    atts.addAttribute("", "b", "b", "CDATA", "2")

    atts.removeAttribute(1)

    assertEquals(atts.getLength(), 1)
    assertEquals(atts.getQName(0), "a")
  }

  test("removeAttribute throws ArrayIndexOutOfBoundsException for negative index") {
    val atts = new AttributesImpl()
    atts.addAttribute("", "a", "a", "CDATA", "1")
    intercept[ArrayIndexOutOfBoundsException] {
      atts.removeAttribute(-1)
    }
  }

  test("removeAttribute throws ArrayIndexOutOfBoundsException for index >= length") {
    val atts = new AttributesImpl()
    atts.addAttribute("", "a", "a", "CDATA", "1")
    intercept[ArrayIndexOutOfBoundsException] {
      atts.removeAttribute(1)
    }
  }

  test("removeAttribute throws ArrayIndexOutOfBoundsException on empty") {
    val atts = new AttributesImpl()
    intercept[ArrayIndexOutOfBoundsException] {
      atts.removeAttribute(0)
    }
  }

  test("setAttribute throws ArrayIndexOutOfBoundsException for invalid index") {
    val atts = new AttributesImpl()
    intercept[ArrayIndexOutOfBoundsException] {
      atts.setAttribute(0, "", "a", "a", "CDATA", "1")
    }
  }

  test("setAttribute throws ArrayIndexOutOfBoundsException for negative index") {
    val atts = new AttributesImpl()
    atts.addAttribute("", "a", "a", "CDATA", "1")
    intercept[ArrayIndexOutOfBoundsException] {
      atts.setAttribute(-1, "", "x", "x", "CDATA", "v")
    }
  }

  test("setAttribute modifies existing attribute") {
    val atts = new AttributesImpl()
    atts.addAttribute("", "a", "a", "CDATA", "old")
    atts.setAttribute(0, "http://new", "newLocal", "ns:newLocal", "ID", "newValue")

    assertEquals(atts.getURI(0), "http://new")
    assertEquals(atts.getLocalName(0), "newLocal")
    assertEquals(atts.getQName(0), "ns:newLocal")
    assertEquals(atts.getType(0), "ID")
    assertEquals(atts.getValue(0), "newValue")
    assertEquals(atts.getLength(), 1)
  }

  test("setURI throws ArrayIndexOutOfBoundsException for invalid index") {
    val atts = new AttributesImpl()
    intercept[ArrayIndexOutOfBoundsException] {
      atts.setURI(0, "http://ns")
    }
  }

  test("setLocalName throws ArrayIndexOutOfBoundsException for invalid index") {
    val atts = new AttributesImpl()
    intercept[ArrayIndexOutOfBoundsException] {
      atts.setLocalName(0, "name")
    }
  }

  test("setQName throws ArrayIndexOutOfBoundsException for invalid index") {
    val atts = new AttributesImpl()
    intercept[ArrayIndexOutOfBoundsException] {
      atts.setQName(0, "name")
    }
  }

  test("setType throws ArrayIndexOutOfBoundsException for invalid index") {
    val atts = new AttributesImpl()
    intercept[ArrayIndexOutOfBoundsException] {
      atts.setType(0, "CDATA")
    }
  }

  test("setValue throws ArrayIndexOutOfBoundsException for invalid index") {
    val atts = new AttributesImpl()
    intercept[ArrayIndexOutOfBoundsException] {
      atts.setValue(0, "val")
    }
  }

  test("setURI modifies existing attribute URI") {
    val atts = new AttributesImpl()
    atts.addAttribute("", "a", "a", "CDATA", "1")
    atts.setURI(0, "http://updated")
    assertEquals(atts.getURI(0), "http://updated")
  }

  test("setLocalName modifies existing attribute localName") {
    val atts = new AttributesImpl()
    atts.addAttribute("", "a", "a", "CDATA", "1")
    atts.setLocalName(0, "updated")
    assertEquals(atts.getLocalName(0), "updated")
  }

  test("setQName modifies existing attribute qName") {
    val atts = new AttributesImpl()
    atts.addAttribute("", "a", "a", "CDATA", "1")
    atts.setQName(0, "ns:updated")
    assertEquals(atts.getQName(0), "ns:updated")
  }

  test("setType modifies existing attribute type") {
    val atts = new AttributesImpl()
    atts.addAttribute("", "a", "a", "CDATA", "1")
    atts.setType(0, "ID")
    assertEquals(atts.getType(0), "ID")
  }

  test("setValue modifies existing attribute value") {
    val atts = new AttributesImpl()
    atts.addAttribute("", "a", "a", "CDATA", "old")
    atts.setValue(0, "new")
    assertEquals(atts.getValue(0), "new")
  }

  test("multiple attributes: correct indexing") {
    val atts = new AttributesImpl()
    atts.addAttribute("http://a", "a", "a:a", "CDATA", "va")
    atts.addAttribute("http://b", "b", "b:b", "ID", "vb")
    atts.addAttribute("http://c", "c", "c:c", "NMTOKEN", "vc")

    assertEquals(atts.getLength(), 3)

    assertEquals(atts.getURI(0), "http://a")
    assertEquals(atts.getLocalName(1), "b")
    assertEquals(atts.getQName(2), "c:c")
    assertEquals(atts.getType(1), "ID")
    assertEquals(atts.getValue(2), "vc")

    assertEquals(atts.getIndex("http://b", "b"), 1)
    assertEquals(atts.getIndex("c:c"), 2)
    assertEquals(atts.getValue("http://a", "a"), "va")
    assertEquals(atts.getType("b:b"), "ID")
  }

  test("adding many attributes grows internal storage") {
    val atts = new AttributesImpl()
    for (i <- 0 until 20) {
      atts.addAttribute("", s"attr$i", s"attr$i", "CDATA", s"val$i")
    }
    assertEquals(atts.getLength(), 20)
    assertEquals(atts.getValue(19), "val19")
    assertEquals(atts.getQName(0), "attr0")
  }
}
