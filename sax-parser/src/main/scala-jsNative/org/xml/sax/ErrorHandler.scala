package org.xml.sax

trait ErrorHandler {
  def warning(exception: SAXParseException): Unit
  def error(exception: SAXParseException): Unit
  def fatalError(exception: SAXParseException): Unit
}
