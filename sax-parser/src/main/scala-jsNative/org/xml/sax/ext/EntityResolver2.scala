package org.xml.sax.ext

import org.xml.sax.{EntityResolver, InputSource}

trait EntityResolver2 extends EntityResolver {
  def getExternalSubset(name: String, baseURI: String): InputSource
  def resolveEntity(name: String, publicId: String, baseURI: String, systemId: String): InputSource
}
