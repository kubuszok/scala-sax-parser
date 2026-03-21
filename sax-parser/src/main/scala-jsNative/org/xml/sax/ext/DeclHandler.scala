package org.xml.sax.ext

trait DeclHandler {
  def elementDecl(name: String, model: String): Unit
  def attributeDecl(eName: String, aName: String, `type`: String, mode: String, value: String): Unit
  def internalEntityDecl(name: String, value: String): Unit
  def externalEntityDecl(name: String, publicId: String, systemId: String): Unit
}
