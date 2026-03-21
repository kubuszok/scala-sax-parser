package org.xml.sax

class SAXException(message: String, cause: Exception) extends Exception(message, cause) {
  def this(message: String) = this(message, null)
  def this(cause: Exception) = this(if (cause != null) cause.toString else null, cause)
  def this() = this(null: String)

  override def getMessage(): String = {
    val msg = super.getMessage()
    if (msg == null && getCause != null) getCause.getMessage
    else msg
  }

  override def toString(): String = {
    val base = super.toString()
    val ex = getException()
    if (ex != null) base + "\n" + ex.toString
    else base
  }

  def getException(): Exception = getCause match {
    case e: Exception => e
    case _            => null
  }
}
