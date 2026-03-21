package org.xml.sax

trait XMLReader {
  def getFeature(name: String): Boolean
  def setFeature(name: String, value: Boolean): Unit
  def getProperty(name: String): AnyRef
  def setProperty(name: String, value: AnyRef): Unit
  def setEntityResolver(resolver: EntityResolver): Unit
  def getEntityResolver(): EntityResolver
  def setDTDHandler(handler: DTDHandler): Unit
  def getDTDHandler(): DTDHandler
  def setContentHandler(handler: ContentHandler): Unit
  def getContentHandler(): ContentHandler
  def setErrorHandler(handler: ErrorHandler): Unit
  def getErrorHandler(): ErrorHandler
  def parse(input: InputSource): Unit
  def parse(systemId: String): Unit
}
