package org.xml.sax

class SAXParseException(
    message: String,
    publicId: String,
    systemId: String,
    lineNumber: Int,
    columnNumber: Int,
    cause: Exception
) extends SAXException(message, cause) {

  def this(message: String, publicId: String, systemId: String, lineNumber: Int, columnNumber: Int) =
    this(message, publicId, systemId, lineNumber, columnNumber, null)

  def this(message: String, locator: Locator) =
    this(
      message,
      if (locator != null) locator.getPublicId() else null,
      if (locator != null) locator.getSystemId() else null,
      if (locator != null) locator.getLineNumber() else -1,
      if (locator != null) locator.getColumnNumber() else -1,
      null
    )

  def this(message: String, locator: Locator, cause: Exception) =
    this(
      message,
      if (locator != null) locator.getPublicId() else null,
      if (locator != null) locator.getSystemId() else null,
      if (locator != null) locator.getLineNumber() else -1,
      if (locator != null) locator.getColumnNumber() else -1,
      cause
    )

  def getPublicId(): String = publicId
  def getSystemId(): String = systemId
  def getLineNumber(): Int = lineNumber
  def getColumnNumber(): Int = columnNumber

  override def toString(): String = {
    val sb = new StringBuilder(getClass.getName)
    val parts = scala.collection.mutable.ArrayBuffer[String]()
    if (publicId != null) parts += s"publicId: $publicId"
    if (systemId != null) parts += s"systemId: $systemId"
    if (lineNumber != -1) parts += s"lineNumber: $lineNumber"
    if (columnNumber != -1) parts += s"columnNumber: $columnNumber"
    if (parts.nonEmpty) sb.append("[").append(parts.mkString("; ")).append("]")
    val msg = getMessage()
    if (msg != null) sb.append(": ").append(msg)
    sb.toString()
  }
}
