package org.xml.sax

trait EntityResolver {
  def resolveEntity(publicId: String, systemId: String): InputSource
}
