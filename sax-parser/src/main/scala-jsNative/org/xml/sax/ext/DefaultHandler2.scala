package org.xml.sax.ext

import org.xml.sax.{InputSource, SAXException}
import org.xml.sax.helpers.DefaultHandler

class DefaultHandler2
    extends DefaultHandler
    with LexicalHandler
    with DeclHandler
    with EntityResolver2 {

  // LexicalHandler
  override def startDTD(name: String, publicId: String, systemId: String): Unit = ()
  override def endDTD(): Unit = ()
  override def startEntity(name: String): Unit = ()
  override def endEntity(name: String): Unit = ()
  override def startCDATA(): Unit = ()
  override def endCDATA(): Unit = ()
  override def comment(ch: Array[Char], start: Int, length: Int): Unit = ()

  // DeclHandler
  override def elementDecl(name: String, model: String): Unit = ()
  override def attributeDecl(eName: String, aName: String, `type`: String, mode: String, value: String): Unit = ()
  override def internalEntityDecl(name: String, value: String): Unit = ()
  override def externalEntityDecl(name: String, publicId: String, systemId: String): Unit = ()

  // EntityResolver2
  override def getExternalSubset(name: String, baseURI: String): InputSource = null
  override def resolveEntity(name: String, publicId: String, baseURI: String, systemId: String): InputSource = null

  // Override EntityResolver to delegate to EntityResolver2
  override def resolveEntity(publicId: String, systemId: String): InputSource =
    resolveEntity(null, publicId, null, systemId)
}
