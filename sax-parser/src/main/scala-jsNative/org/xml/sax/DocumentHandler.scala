package org.xml.sax

/**
 * Deprecated SAX1 DocumentHandler interface.
 * Included for compatibility with the deprecated Parser interface.
 */
@deprecated("Use ContentHandler instead", "SAX 2.0")
trait DocumentHandler {
  def setDocumentLocator(locator: Locator): Unit
  def startDocument(): Unit
  def endDocument(): Unit
  def startElement(name: String, atts: AttributeList): Unit
  def endElement(name: String): Unit
  def characters(ch: Array[Char], start: Int, length: Int): Unit
  def ignorableWhitespace(ch: Array[Char], start: Int, length: Int): Unit
  def processingInstruction(target: String, data: String): Unit
}
