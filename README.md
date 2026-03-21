# scala-sax-parser

A clean-room, BSD-licensed implementation of the SAX and JAXP XML parsing APIs (`org.xml.sax.*` and `javax.xml.parsers.*`) for Scala.js and Scala Native.

## The Problem

[scala-xml](https://github.com/scala/scala-xml) publishes cross-platform artifacts for JVM, Scala.js, and Scala Native. XML literals, the node model (`Elem`, `Node`, `Text`, etc.), serialization, pattern matching, and XPath-like queries (`\`, `\\`) all work on every platform.

However, **XML parsing does not work on Scala.js or Scala Native**. Methods like `XML.loadString()`, `XML.load()`, and everything in `scala.xml.factory.XMLLoader` depend on the SAX parsing APIs — `javax.xml.parsers.SAXParserFactory`, `org.xml.sax.XMLReader`, and related classes — which are part of the JVM standard library and have no equivalent on Scala.js or Scala Native.

This means any Scala project that needs to **parse XML from strings or streams** cannot cross-compile for Scala.js or Scala Native, even though scala-xml itself is nominally available on those platforms.

## The Solution

scala-sax-parser provides pure-Scala implementations of the SAX and JAXP classes that scala-xml requires. It follows the same polyfill pattern established by [scala-java-time](https://github.com/cquiroz/scala-java-time) and [scala-java-locales](https://github.com/cquiroz/scala-java-locales):

- **On JVM**: the published artifact is empty. The real `javax.xml.parsers` and `org.xml.sax` classes from the JDK are used. There is zero overhead.
- **On Scala.js and Scala Native**: the artifact provides a pure-Scala recursive-descent XML 1.0 parser that implements the SAX `XMLReader` interface and fires the same events as the JDK's built-in parser.

Adding scala-sax-parser as a dependency alongside scala-xml makes `XML.loadString()` and `XML.load()` work on all three platforms with identical behavior.

## Usage

Add to your `build.sbt`:

```scala
libraryDependencies += "io.github.scalasaxparser" %%% "scala-sax-parser" % "<version>"
```

No code changes required. scala-xml's `XML.loadString()` will work on Scala.js and Scala Native automatically.

## What's Implemented

### SAX Interfaces (`org.xml.sax`)

| Type | Classes |
|------|---------|
| Core interfaces | `XMLReader`, `ContentHandler`, `DTDHandler`, `EntityResolver`, `ErrorHandler`, `Attributes`, `Locator` |
| Extension interfaces | `LexicalHandler`, `DeclHandler`, `Locator2`, `EntityResolver2` |
| Helper classes | `DefaultHandler`, `DefaultHandler2`, `AttributesImpl` |
| Data classes | `InputSource` |
| Exceptions | `SAXException`, `SAXParseException`, `SAXNotRecognizedException`, `SAXNotSupportedException` |
| Legacy (SAX1) | `Parser`, `DocumentHandler`, `AttributeList` |

### JAXP (`javax.xml.parsers`)

| Type | Classes |
|------|---------|
| Factory | `SAXParserFactory` |
| Parser | `SAXParser` |
| Exceptions | `ParserConfigurationException` |

### XML Parser Engine

The core of the library is `ScalaXMLReader` — a recursive-descent XML 1.0 parser written in pure Scala that fires SAX events. It supports:

- Well-formed XML 1.0 parsing
- Namespace processing (prefix mappings, default namespaces)
- Built-in entities (`&amp;`, `&lt;`, `&gt;`, `&apos;`, `&quot;`)
- Character references (decimal and hexadecimal)
- CDATA sections
- Comments and processing instructions
- DTD internal subset (entity and element declarations)
- XML declaration handling
- Line/column tracking via `Locator`

### Not Implemented (by design)

These features are intentionally omitted as they are rarely needed in practice and would add significant complexity:

- DTD validation (declarations are parsed but not enforced)
- External DTD subsets and external entity resolution (would require network/file I/O)
- XInclude processing
- XML Schema validation

## Clean-Room Implementation

This library is a **clean-room implementation**. The behavioral contracts are derived from the [SAX specification](http://www.saxproject.org/) (public domain) and the JAXP API documentation. No code was copied from OpenJDK, Apache Xerces, or any GPL/LGPL-licensed source.

The test suite (298 tests) includes both XML parsing/serialization tests and specification-based tests that verify compliance with the documented SAX behavioral contracts. All tests run identically on JVM, Scala.js, and Scala Native.

## Build

```bash
# Run all tests on all platforms
sbt test

# Run tests on a specific platform
sbt tests3/test        # JVM
sbt testsJS3/test      # Scala.js
sbt testsNative3/test  # Scala Native
```

## Project Structure

```
sax-parser/
  src/main/scala/            # Shared (empty)
  src/main/scala-jvm/        # JVM (empty — uses real JDK classes)
  src/main/scala-jsNative/   # Pure-Scala polyfill for JS and Native
    javax/xml/parsers/        # SAXParserFactory, SAXParser
    org/xml/sax/              # SAX interfaces, exceptions, helpers
    org/xml/sax/impl/         # ScalaXMLReader (parser engine)

tests/
  src/test/scala/             # Cross-platform test suite
```

## License

BSD 3-Clause. See [LICENSE](LICENSE).

## Related Projects

- [scala-xml](https://github.com/scala/scala-xml) — The Scala XML library this project enables cross-platform parsing for
- [scala-java-time](https://github.com/cquiroz/scala-java-time) — Similar polyfill pattern for `java.time`
- [scala-java-locales](https://github.com/cquiroz/scala-java-locales) — Similar polyfill pattern for `java.util.Locale`
