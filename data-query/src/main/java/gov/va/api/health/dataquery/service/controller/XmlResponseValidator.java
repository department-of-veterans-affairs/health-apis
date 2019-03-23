package gov.va.api.health.dataquery.service.controller;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import lombok.Builder;
import org.springframework.util.MultiValueMap;
import org.w3c.dom.Document;

/**
 * Process the CDW prc_Entity_Return XML responses for errors, throwing {@link
 * gov.va.api.health.mranderson.cdw.Resources} exceptions as necessary.
 */
public class XmlResponseValidator {
  private final MultiValueMap<String, String> parameters;
  private final Document response;

  @Builder
  private XmlResponseValidator(MultiValueMap<String, String> parameters, Document response) {
    this.parameters = parameters;
    this.response = response;
  }

  private int asIntegerOrDie(String errorNumberValue) {
    try {
      return Integer.parseInt(errorNumberValue);
    } catch (NumberFormatException e) {
      throw new WitnessProtection.SearchFailed(
          parameters, "Do not understand XML response. Error Number: " + errorNumberValue);
    }
  }

  private String extractErrorNumberValueOrDie() {
    XPath xpath = XPathFactory.newInstance().newXPath();
    try {
      return xpath.compile("/root/errorNumber").evaluate(response);
    } catch (XPathExpressionException e) {
      throw new WitnessProtection.SearchFailed(
          parameters, "Do not understand XML response. Error Number: <missing>");
    }
  }

  public void validate() {
    int errorNumber = asIntegerOrDie(extractErrorNumberValueOrDie());
    if (errorNumber == ErrorNumbers.UNKNOWN_RESOURCE) {
      throw new WitnessProtection.UnknownResource(parameters);
    }
    if (errorNumber == ErrorNumbers.BAD_PARAMETERS) {
      throw new WitnessProtection.MissingSearchParameters(parameters);
    }
    if (errorNumber != ErrorNumbers.NO_ERROR) {
      throw new WitnessProtection.SearchFailed(
          parameters, "Unknown response error: Error Number: " + errorNumber);
    }
  }

  private interface ErrorNumbers {
    int NO_ERROR = 0;
    int UNKNOWN_RESOURCE = -8;
    int BAD_PARAMETERS = -999;
  }
}
