package org.xml.sax

trait DTDHandler {
  def notationDecl(name: String, publicId: String, systemId: String): Unit
  def unparsedEntityDecl(name: String, publicId: String, systemId: String, notationName: String): Unit
}
