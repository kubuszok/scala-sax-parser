package org.xml.sax

class SAXNotSupportedException(message: String) extends SAXException(message) {
  def this() = this(null)
}
