package org.xml.sax.ext

import org.xml.sax.Locator

trait Locator2 extends Locator {
  def getXMLVersion(): String
  def getEncoding(): String
}
