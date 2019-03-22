package gov.va.api.health.dataquery.service.config;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.dvp.cdw.xsd.model.CdwDiagnosticReport102Root;
import gov.va.dvp.cdw.xsd.model.CdwDiagnosticReport102Root.CdwDiagnosticReports;
import gov.va.dvp.cdw.xsd.model.CdwDiagnosticReport102Root.CdwDiagnosticReports.CdwDiagnosticReport;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import lombok.SneakyThrows;
import org.junit.Test;

public class XmlTest {

  private String reportXml() {
    return "<rowNumber>0</rowNumber>"
        + "<cdwId>10:L</cdwId><status>final</status>"
        + "<category><coding><system>http://hl7.org/fhir/ValueSet/diagnostic-service-sections</system><code>LAB</code><display>Laboratory</display></coding></category>"
        + "<code><text>panel</text></code>"
        + "<subject><reference>Patient/1</reference><display>Mr. Dwayne786 Pacocha935</display></subject>"
        + "<encounter><reference>Encounter/11</reference><display>Outpatient Visit</display></encounter>"
        + "<effective>2017-04-04T02:17:47Z</effective><issued>2017-04-04T02:17:47Z</issued>"
        + "<performer><reference>Organization/1</reference><display>Diagnostic Laboratory</display></performer>"
        + "<requests><request><reference>DiagnosticOrder/15</reference><display>Lipid Panel</display></request></requests>"
        + "<results><result><reference>Observation/63:L</reference><display>Total Cholesterol</display></result>"
        + "<result><reference>Observation/64:L</reference><display>Triglycerides</display></result>"
        + "<result><reference>Observation/65:L</reference><display>Low Density Lipoprotein Cholesterol</display></result>"
        + "<result><reference>Observation/66:L</reference><display>High Density Lipoprotein Cholesterol</display></result></results>";
  }

  @Test
  @SneakyThrows
  public void unmarshal() {
    String xml2 = "<CdwDiagnosticReport>" + reportXml() + "</CdwDiagnosticReport>";
    String xml3 =
        "<root>"
            + "<diagnosticReports>"
            + "<diagnosticReport>"
            + reportXml()
            + "</diagnosticReport>"
            + "<diagnosticReport>"
            + reportXml()
            + "</diagnosticReport>"
            + "</diagnosticReports>"
            + "</root>";
    try (Reader reader = new StringReader(xml3)) {
      JAXBContext jaxbContext = JAXBContext.newInstance(CdwDiagnosticReport102Root.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      CdwDiagnosticReport102Root sampleReport =
          (CdwDiagnosticReport102Root) jaxbUnmarshaller.unmarshal(reader);
      System.out.println(
          JacksonConfig.createMapper()
              .writerWithDefaultPrettyPrinter()
              .writeValueAsString(sampleReport));
    }
  }

  @Test
  @SneakyThrows
  public void writeRoot() {
    CdwDiagnosticReport102Root root = new CdwDiagnosticReport102Root();
    root.setDiagnosticReports(new CdwDiagnosticReports());
    root.getDiagnosticReports().getDiagnosticReport().add(new CdwDiagnosticReport());
    JAXBContext jaxbContext = JAXBContext.newInstance(CdwDiagnosticReport102Root.class);
    Marshaller marshaller = jaxbContext.createMarshaller();
    StringWriter writer = new StringWriter();
    marshaller.marshal(root, writer);
    System.out.println(writer.toString());
  }
}
