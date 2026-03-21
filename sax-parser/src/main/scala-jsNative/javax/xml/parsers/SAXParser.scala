package javax.xml.parsers

import java.io.InputStream
import org.xml.sax.{InputSource, Parser, XMLReader}
import org.xml.sax.helpers.DefaultHandler

@SuppressWarnings(Array("deprecation"))
abstract class SAXParser {
  def getXMLReader(): XMLReader

  def isNamespaceAware(): Boolean
  def isValidating(): Boolean

  @deprecated("Use getXMLReader instead", "")
  def getParser(): Parser = null

  def parse(is: InputSource, dh: DefaultHandler): Unit = {
    val reader = getXMLReader()
    if (dh != null) {
      reader.setContentHandler(dh)
      reader.setDTDHandler(dh)
      reader.setEntityResolver(dh)
      reader.setErrorHandler(dh)
    }
    reader.parse(is)
  }

  def parse(is: InputStream, dh: DefaultHandler): Unit =
    parse(new InputSource(is), dh)

  def parse(is: InputStream, dh: DefaultHandler, systemId: String): Unit = {
    val source = new InputSource(is)
    source.setSystemId(systemId)
    parse(source, dh)
  }

  def parse(uri: String, dh: DefaultHandler): Unit =
    parse(new InputSource(uri), dh)

  def reset(): Unit = ()

  def getProperty(name: String): AnyRef = getXMLReader().getProperty(name)
  def setProperty(name: String, value: AnyRef): Unit = getXMLReader().setProperty(name, value)
}
