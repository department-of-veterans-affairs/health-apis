package gov.va.api.health.dataquery.tools.minimart;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import gov.va.api.health.argonaut.api.resources.AllergyIntolerance;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.DatamartAllergyIntolerance;
import gov.va.api.health.dataquery.tools.minimart.transformers.F2DAllergyIntoleranceTransformer;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;
import lombok.SneakyThrows;

public class FhirToDatamart {

  F2DAllergyIntoleranceTransformer allergyIntoleranceTransformer =
      new F2DAllergyIntoleranceTransformer();

  @SneakyThrows
  public void main(String[] args) {
    if (args.length != 2) {
      throw new RuntimeException("Arg Count Incorrect: " + args.length);
    }
    String resource = args[0];
    String directory = args[1];
    List<File> files =
        Files.walk(Path.of(directory))
            .filter(Files::isRegularFile)
            .map(Path::toFile)
            .filter(f -> f.getName().matches(pattern(resource)))
            .collect(Collectors.toList());
    transformAndWriteFiles(files, resource);
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
        throw new IllegalArgumentException("Unknown Resource : " + resource);
    }
  }

  @SneakyThrows
  private void transformAndWriteFiles(List<File> files, String resource) {
    ObjectMapper mapper = mapper();
    switch (resource) {
      case "AllergyIntolerance":
        for (File file : files) {
          AllergyIntolerance allergyIntolerance = mapper.readValue(file, AllergyIntolerance.class);
          DatamartAllergyIntolerance datamartAllergyIntolerance =
              allergyIntoleranceTransformer.fhirToDatamart(allergyIntolerance);
          mapper.writeValue(
              new File(
                  "target/DMAllInt"
                      + datamartAllergyIntolerance.cdwId().replaceAll("-", "")
                      + ".json"),
              datamartAllergyIntolerance);
        }
        break;
      default:
        throw new IllegalArgumentException("Unsupported Resource : " + resource);
    }
  }
}
