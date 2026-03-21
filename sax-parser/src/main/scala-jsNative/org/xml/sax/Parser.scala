package org.xml.sax

/**
 * Deprecated SAX1 Parser interface. Included for compatibility with
 * javax.xml.parsers.SAXParser which declares getParser().
 */
@deprecated("Use XMLReader instead", "SAX 2.0")
trait Parser {
  def setLocale(locale: java.util.Locale): Unit
  def setEntityResolver(resolver: EntityResolver): Unit
  def setDTDHandler(handler: DTDHandler): Unit
  def setDocumentHandler(handler: DocumentHandler): Unit
  def setErrorHandler(handler: ErrorHandler): Unit
  def parse(source: InputSource): Unit
  def parse(systemId: String): Unit
}
