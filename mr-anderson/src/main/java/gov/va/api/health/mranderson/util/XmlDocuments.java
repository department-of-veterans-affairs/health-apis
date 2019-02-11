package gov.va.api.health.mranderson.util;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import lombok.NoArgsConstructor;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/** Utilities for working with XML documents. */
@NoArgsConstructor(staticName = "create")
public final class XmlDocuments {
  /**
   * Finds a DOM implementation is capable of 'Load/Save' operations, which gives us the ability to
   * write Documents.
   */
  static DOMImplementationLS findLsDomImplementationOrDie(DOMImplementationRegistry registry)
      throws WriteFailed {
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

  private DOMImplementationRegistry createRegistryOrDie() {
    DOMImplementationRegistry registry;
    try {
      registry = DOMImplementationRegistry.newInstance();
    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
      throw new WriteFailed(e);
    }
    return registry;
  }

  /**
   * Parse the given XML into a Document model. A ParseFailed exception can be thrown if the
   * document cannot be read for any reason.
   */
  public Document parse(String xml) {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
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
  public String write(Document document) {
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

  public static class ParseFailed extends RuntimeException {
    ParseFailed(Exception cause) {
      super(cause);
    }
  }

  public static class WriteFailed extends RuntimeException {
    WriteFailed(String message) {
      super(message);
    }

    WriteFailed(Exception cause) {
      super(cause);
    }
  }
}
