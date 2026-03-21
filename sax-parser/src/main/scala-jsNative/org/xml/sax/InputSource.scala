package org.xml.sax

import java.io.{InputStream, Reader}

class InputSource() {
  private var publicId: String = _
  private var systemId: String = _
  private var byteStream: InputStream = _
  private var encoding: String = _
  private var characterStream: Reader = _

  def this(systemId: String) = {
    this()
    this.systemId = systemId
  }

  def this(byteStream: InputStream) = {
    this()
    this.byteStream = byteStream
  }

  def this(characterStream: Reader) = {
    this()
    this.characterStream = characterStream
  }

  def setPublicId(publicId: String): Unit = this.publicId = publicId
  def getPublicId(): String = publicId

  def setSystemId(systemId: String): Unit = this.systemId = systemId
  def getSystemId(): String = systemId

  def setByteStream(byteStream: InputStream): Unit = this.byteStream = byteStream
  def getByteStream(): InputStream = byteStream

  def setEncoding(encoding: String): Unit = this.encoding = encoding
  def getEncoding(): String = encoding

  def setCharacterStream(characterStream: Reader): Unit = this.characterStream = characterStream
  def getCharacterStream(): Reader = characterStream
}
