package org.xml.sax

class SAXNotRecognizedException(message: String) extends SAXException(message) {
  def this() = this(null)
}
