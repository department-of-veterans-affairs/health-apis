package gov.va.api.health.dataquery.service.controller;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import lombok.experimental.UtilityClass;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/** Utilities for working with XML documents. */
@UtilityClass
final class XmlDocuments {
  private static DOMImplementationRegistry createRegistryOrDie() {
    DOMImplementationRegistry registry;
    try {
      registry = DOMImplementationRegistry.newInstance();
    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
      throw new WriteFailed(e);
    }
    return registry;
  }

  /**
   * Finds a DOM implementation is capable of 'Load/Save' operations, which gives us the ability to
   * write Documents.
   */
  private static DOMImplementationLS findLsDomImplementationOrDie(
      DOMImplementationRegistry registry) throws WriteFailed {
    DOMImplementation domImplementation = registry.getDOMImplementation("LS");
    if (domImplementation == null) {
      throw new WriteFailed("No Load/Save LS DOM implementation available.");
    }
    if (domImplementation instanceof DOMImplementationLS) {
      return (DOMImplementationLS) domImplementation;
    }
    throw new WriteFailed(
        "Unexpected LS DOM implementation. Required: org.w3c.dom.ls.DOMImplementationLS, Got: "
            + domImplementation.getClass());
  }

  /**
   * Parse the given XML into a Document model. A ParseFailed exception can be thrown if the
   * document cannot be read for any reason.
   */
  static Document parse(String xml) {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      DocumentBuilder builder = factory.newDocumentBuilder();
      InputSource is = new InputSource(new StringReader(xml));
      return builder.parse(is);
    } catch (ParserConfigurationException | SAXException | IOException e) {
      throw new ParseFailed(e);
    }
  }

  /**
   * Write the given Document as an indented XML string. A WriteFailed exception will be thrown if
   * the document cannot be written for some reason.
   */
  static String write(Document document) {
    DOMImplementationRegistry registry = createRegistryOrDie();
    DOMImplementationLS domImplementation = findLsDomImplementationOrDie(registry);
    Writer stringWriter = new StringWriter();
    LSOutput formattedOutput = domImplementation.createLSOutput();
    formattedOutput.setCharacterStream(stringWriter);
    LSSerializer domSerializer = domImplementation.createLSSerializer();
    domSerializer.getDomConfig().setParameter("format-pretty-print", true);
    domSerializer.getDomConfig().setParameter("xml-declaration", true);
    domSerializer.write(document, formattedOutput);
    return stringWriter.toString();
  }

  static class ParseFailed extends RuntimeException {
    ParseFailed(Exception cause) {
      super(cause);
    }
  }

  static class WriteFailed extends RuntimeException {
    WriteFailed(String message) {
      super(message);
    }

    WriteFailed(Exception cause) {
      super(cause);
    }
  }
}
