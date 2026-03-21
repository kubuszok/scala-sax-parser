package org.xml.sax

/**
 * Deprecated SAX1 AttributeList interface.
 * Included for compatibility with the deprecated DocumentHandler interface.
 */
@deprecated("Use Attributes instead", "SAX 2.0")
trait AttributeList {
  def getLength(): Int
  def getName(i: Int): String
  def getType(i: Int): String
  def getValue(i: Int): String
  def getType(name: String): String
  def getValue(name: String): String
}
