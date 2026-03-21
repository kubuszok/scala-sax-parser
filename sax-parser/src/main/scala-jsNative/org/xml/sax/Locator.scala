package org.xml.sax

trait Locator {
  def getPublicId(): String
  def getSystemId(): String
  def getLineNumber(): Int
  def getColumnNumber(): Int
}
