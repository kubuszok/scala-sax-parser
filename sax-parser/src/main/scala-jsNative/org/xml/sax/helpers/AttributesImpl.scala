package org.xml.sax.helpers

import org.xml.sax.Attributes

class AttributesImpl() extends Attributes {
  private var length_ : Int = 0
  private var data: Array[String] = new Array[String](25) // 5 fields per attribute

  def this(atts: Attributes) = {
    this()
    setAttributes(atts)
  }

  override def getLength(): Int = length_

  override def getURI(index: Int): String =
    if (index >= 0 && index < length_) data(index * 5) else null

  override def getLocalName(index: Int): String =
    if (index >= 0 && index < length_) data(index * 5 + 1) else null

  override def getQName(index: Int): String =
    if (index >= 0 && index < length_) data(index * 5 + 2) else null

  override def getType(index: Int): String =
    if (index >= 0 && index < length_) data(index * 5 + 3) else null

  override def getValue(index: Int): String =
    if (index >= 0 && index < length_) data(index * 5 + 4) else null

  override def getIndex(uri: String, localName: String): Int = {
    var i = 0
    while (i < length_) {
      if (data(i * 5) == uri && data(i * 5 + 1) == localName) return i
      i += 1
    }
    -1
  }

  override def getIndex(qName: String): Int = {
    var i = 0
    while (i < length_) {
      if (data(i * 5 + 2) == qName) return i
      i += 1
    }
    -1
  }

  override def getType(uri: String, localName: String): String = {
    val idx = getIndex(uri, localName)
    if (idx >= 0) data(idx * 5 + 3) else null
  }

  override def getType(qName: String): String = {
    val idx = getIndex(qName)
    if (idx >= 0) data(idx * 5 + 3) else null
  }

  override def getValue(uri: String, localName: String): String = {
    val idx = getIndex(uri, localName)
    if (idx >= 0) data(idx * 5 + 4) else null
  }

  override def getValue(qName: String): String = {
    val idx = getIndex(qName)
    if (idx >= 0) data(idx * 5 + 4) else null
  }

  def clear(): Unit = {
    length_ = 0
  }

  def setAttributes(atts: Attributes): Unit = {
    clear()
    val len = atts.getLength()
    if (len > 0) {
      ensureCapacity(len)
      var i = 0
      while (i < len) {
        data(i * 5) = atts.getURI(i)
        data(i * 5 + 1) = atts.getLocalName(i)
        data(i * 5 + 2) = atts.getQName(i)
        data(i * 5 + 3) = atts.getType(i)
        data(i * 5 + 4) = atts.getValue(i)
        i += 1
      }
      length_ = len
    }
  }

  def addAttribute(uri: String, localName: String, qName: String, `type`: String, value: String): Unit = {
    ensureCapacity(length_ + 1)
    data(length_ * 5) = uri
    data(length_ * 5 + 1) = localName
    data(length_ * 5 + 2) = qName
    data(length_ * 5 + 3) = `type`
    data(length_ * 5 + 4) = value
    length_ += 1
  }

  def removeAttribute(index: Int): Unit = {
    if (index < 0 || index >= length_)
      throw new ArrayIndexOutOfBoundsException("Attribute index out of bounds: " + index)
    if (index < length_ - 1) {
      System.arraycopy(data, (index + 1) * 5, data, index * 5, (length_ - index - 1) * 5)
    }
    length_ -= 1
    // Clear the last slot
    val base = length_ * 5
    var i = 0
    while (i < 5) { data(base + i) = null; i += 1 }
  }

  def setAttribute(index: Int, uri: String, localName: String, qName: String, `type`: String, value: String): Unit = {
    if (index < 0 || index >= length_)
      throw new ArrayIndexOutOfBoundsException("Attribute index out of bounds: " + index)
    data(index * 5) = uri
    data(index * 5 + 1) = localName
    data(index * 5 + 2) = qName
    data(index * 5 + 3) = `type`
    data(index * 5 + 4) = value
  }

  def setURI(index: Int, uri: String): Unit = {
    if (index < 0 || index >= length_)
      throw new ArrayIndexOutOfBoundsException("Attribute index out of bounds: " + index)
    data(index * 5) = uri
  }

  def setLocalName(index: Int, localName: String): Unit = {
    if (index < 0 || index >= length_)
      throw new ArrayIndexOutOfBoundsException("Attribute index out of bounds: " + index)
    data(index * 5 + 1) = localName
  }

  def setQName(index: Int, qName: String): Unit = {
    if (index < 0 || index >= length_)
      throw new ArrayIndexOutOfBoundsException("Attribute index out of bounds: " + index)
    data(index * 5 + 2) = qName
  }

  def setType(index: Int, `type`: String): Unit = {
    if (index < 0 || index >= length_)
      throw new ArrayIndexOutOfBoundsException("Attribute index out of bounds: " + index)
    data(index * 5 + 3) = `type`
  }

  def setValue(index: Int, value: String): Unit = {
    if (index < 0 || index >= length_)
      throw new ArrayIndexOutOfBoundsException("Attribute index out of bounds: " + index)
    data(index * 5 + 4) = value
  }

  private def ensureCapacity(n: Int): Unit = {
    if (n * 5 > data.length) {
      val newData = new Array[String](Math.max(data.length * 2, n * 5))
      System.arraycopy(data, 0, newData, 0, length_ * 5)
      data = newData
    }
  }
}
