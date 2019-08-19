package gov.va.api.health.dataquery.tests.fhirtodatamart;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import gov.va.api.health.argonaut.api.resources.AllergyIntolerance;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.DatamartAllergyIntolerance;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.junit.Test;

public class FhirToDatamart {

  private final Properties config = new Properties(System.getProperties());

  F2DAllergyIntoleranceTransformer allergyIntoleranceTransformer =
      new F2DAllergyIntoleranceTransformer();

  @SneakyThrows
  private Path directory() {
    String directoryPath =
        config.getProperty(
            "fhir.json.directory",
            "/home/lighthouse/Documents/health-apis-data-query"
                + "/data-query-tests/target/lab-crawl-1017283132V631076");
    File directory = new File(directoryPath);
    return directory.toPath();
  }

  private ObjectMapper mapper() {
    return new ObjectMapper()
        .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .registerModule(new Jdk8Module())
        .registerModule(new JavaTimeModule())
        .setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
  }

  private String pattern(String resource) {
    switch (resource) {
      case "AllergyIntolerance":
        return "^AllInt(?!P).*json";
      case "Condition":
        return "^Con(?!P).*json";
      case "DiagnosticReport":
        return "^DiaRep(?!P).*json";
      case "Immunization":
        return "^Imm(?!P).*json";
      case "Medication":
        return "^Med(?!P).*json";
      case "MedicationOrder":
        return "^MedOrd(?!P).*json";
      case "MedicationStatement":
        return "^MedSta(?!P).*json";
      case "Observation":
        return "^Obs(?!P).*json";
      case "Patient":
        return "^Pat(?!P).*json";
      case "Procedure":
        return "^Pro(?!P).*json";
      default:
        return "^AllInt(?!P).*json|^Con(?!P).*json|^DiaRep(?!P).*json|^Imm(?!P).*json"
            + "|^Med(?!P).*json|^MedOrd(?!P).*json|^MedSta(?!P).*json"
            + "|^Obs(?!P).*json|^Pat(?!P).*json|^Pro(?!P).*json";
    }
  }

  @SneakyThrows
  @Test
  public void readInAllergyIntolerance() {
    ObjectMapper mapper = mapper();
    List<File> files =
        Files.walk(directory())
            .filter(Files::isRegularFile)
            .map(Path::toFile)
            .filter(f -> f.getName().matches(pattern("AllergyIntolerance")))
            .collect(Collectors.toList());
    for (File file : files) {
      AllergyIntolerance allergyIntolerance = mapper.readValue(file, AllergyIntolerance.class);
      DatamartAllergyIntolerance datamartAllergyIntolerance =
          allergyIntoleranceTransformer.fhirToDatamart(allergyIntolerance);
      mapper.writeValue(
          new File(
              "target/DMAllInt" + datamartAllergyIntolerance.cdwId().replaceAll("-", "") + ".json"),
          datamartAllergyIntolerance);
    }
  }
}
