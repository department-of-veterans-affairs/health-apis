package gov.va.api.health.dataquery.tools.minimart;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import gov.va.api.health.argonaut.api.resources.AllergyIntolerance;
import gov.va.api.health.argonaut.api.resources.DiagnosticReport;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.DatamartAllergyIntolerance;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.DatamartDiagnosticReports;
import gov.va.api.health.dataquery.tools.minimart.transformers.F2DAllergyIntoleranceTransformer;
import gov.va.api.health.dataquery.tools.minimart.transformers.F2DDiagnosticReportTransformer;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;
import lombok.SneakyThrows;

public class FhirToDatamart {

  String inputDirectory;

  String resourceType;

  public FhirToDatamart(String directory, String resource) {
    inputDirectory = directory;
    resourceType = resource;
  }

  @SneakyThrows
  public static void main(String[] args) {
    if (args.length != 2) {
      throw new RuntimeException("Arg Count Incorrect: " + args.length);
    }
    String resourceType = args[0];
    String inputDirectory = args[1];
    new FhirToDatamart(inputDirectory, resourceType).fhirToDatamart();
    System.exit(0);
  }

  @SneakyThrows
  private void fhirToDatamart() {
    List<File> files =
        Files.walk(Paths.get(inputDirectory))
            .filter(Files::isRegularFile)
            .map(Path::toFile)
            .filter(f -> f.getName().matches(pattern(resourceType)))
            .collect(Collectors.toList());
    transformAndWriteFiles(files, resourceType);
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
        return "^Med(?!P|Sta|Ord).*json";
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
    F2DAllergyIntoleranceTransformer allergyIntoleranceTransformer =
        new F2DAllergyIntoleranceTransformer();
    ObjectMapper mapper = mapper();
    String outputDirectoryName = "target/fhir-to-datamart-samples";
    File outputDirectory = new File(outputDirectoryName);
    if (!outputDirectory.exists()) {
      outputDirectory.mkdir();
    }
    switch (resource) {
      case "AllergyIntolerance":
        for (File file : files) {
          AllergyIntolerance allergyIntolerance = mapper.readValue(file, AllergyIntolerance.class);
          DatamartAllergyIntolerance datamartAllergyIntolerance =
              allergyIntoleranceTransformer.fhirToDatamart(allergyIntolerance);
          mapper.writeValue(
              new File(
                  outputDirectory
                      + "/dmAllInt"
                      + datamartAllergyIntolerance.cdwId().replaceAll("-", "")
                      + ".json"),
              datamartAllergyIntolerance);
        }
        break;
      case "DiagnosticReport":
        F2DDiagnosticReportTransformer diagnosticReportTransformer =
            new F2DDiagnosticReportTransformer();
        for (File f : files) {
          DiagnosticReport diagnosticReport = mapper.readValue(f, DiagnosticReport.class);
          DatamartDiagnosticReports datamartDiagnosticReports =
              diagnosticReportTransformer.fhirToDatamart(diagnosticReport);
          mapper.writeValue(
              new File(
                  outputDirectory
                      + "/dmDiaRep"
                      + datamartDiagnosticReports.reports().get(0).identifier().replaceAll("-", "")
                      + ".json"),
              datamartDiagnosticReports);
        }
        break;
      default:
        throw new IllegalArgumentException("Unsupported Resource : " + resource);
    }
  }
}
