package javax.xml.parsers

import org.xml.sax.XMLReader
import org.xml.sax.impl.ScalaXMLReader

abstract class SAXParserFactory {
  private var namespaceAware: Boolean = false
  private var validating: Boolean = false
  private val features = scala.collection.mutable.Map[String, Boolean]()

  def newSAXParser(): SAXParser

  def setNamespaceAware(awareness: Boolean): Unit = namespaceAware = awareness
  def isNamespaceAware(): Boolean = namespaceAware

  def setValidating(validating: Boolean): Unit = this.validating = validating
  def isValidating(): Boolean = validating

  def setFeature(name: String, value: Boolean): Unit = features(name) = value
  def getFeature(name: String): Boolean = features.getOrElse(name, false)

  def setXIncludeAware(state: Boolean): Unit = () // XInclude not supported; silently accept
  def isXIncludeAware(): Boolean = false
}

object SAXParserFactory {
  def newInstance(): SAXParserFactory = new DefaultSAXParserFactory()

  def newDefaultInstance(): SAXParserFactory = newInstance()

  def newNSInstance(): SAXParserFactory = {
    val factory = newInstance()
    factory.setNamespaceAware(true)
    factory
  }

  private class DefaultSAXParserFactory extends SAXParserFactory {
    override def newSAXParser(): SAXParser = {
      val factory = this
      new SAXParser {
        private val xmlReader: XMLReader = {
          val reader = new ScalaXMLReader()
          reader.setFeature("http://xml.org/sax/features/namespaces", factory.isNamespaceAware())
          reader.setFeature("http://xml.org/sax/features/namespace-prefixes", !factory.isNamespaceAware())
          reader
        }

        override def getXMLReader(): XMLReader = xmlReader
        override def isNamespaceAware(): Boolean = factory.isNamespaceAware()
        override def isValidating(): Boolean = factory.isValidating()
      }
    }
  }
}
