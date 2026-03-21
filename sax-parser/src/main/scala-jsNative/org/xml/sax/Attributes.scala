package org.xml.sax

trait Attributes {
  def getLength(): Int
  def getURI(index: Int): String
  def getLocalName(index: Int): String
  def getQName(index: Int): String
  def getType(index: Int): String
  def getValue(index: Int): String
  def getIndex(uri: String, localName: String): Int
  def getIndex(qName: String): Int
  def getType(uri: String, localName: String): String
  def getType(qName: String): String
  def getValue(uri: String, localName: String): String
  def getValue(qName: String): String
}
