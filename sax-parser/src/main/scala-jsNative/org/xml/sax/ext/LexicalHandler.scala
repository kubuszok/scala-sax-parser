package org.xml.sax.ext

trait LexicalHandler {
  def startDTD(name: String, publicId: String, systemId: String): Unit
  def endDTD(): Unit
  def startEntity(name: String): Unit
  def endEntity(name: String): Unit
  def startCDATA(): Unit
  def endCDATA(): Unit
  def comment(ch: Array[Char], start: Int, length: Int): Unit
}
