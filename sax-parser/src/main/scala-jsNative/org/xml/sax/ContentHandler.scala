package org.xml.sax

trait ContentHandler {
  def setDocumentLocator(locator: Locator): Unit
  def startDocument(): Unit
  def endDocument(): Unit
  def startPrefixMapping(prefix: String, uri: String): Unit
  def endPrefixMapping(prefix: String): Unit
  def startElement(uri: String, localName: String, qName: String, atts: Attributes): Unit
  def endElement(uri: String, localName: String, qName: String): Unit
  def characters(ch: Array[Char], start: Int, length: Int): Unit
  def ignorableWhitespace(ch: Array[Char], start: Int, length: Int): Unit
  def processingInstruction(target: String, data: String): Unit
  def skippedEntity(name: String): Unit
}
