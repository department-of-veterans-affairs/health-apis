package gov.va.api.health.dataquery.tools.minimart;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.argonaut.api.resources.AllergyIntolerance;
import gov.va.api.health.argonaut.api.resources.DiagnosticReport;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.DatamartAllergyIntolerance;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.DatamartDiagnosticReports;
import gov.va.api.health.dataquery.tools.minimart.transformers.F2DAllergyIntoleranceTransformer;
import gov.va.api.health.dataquery.tools.minimart.transformers.F2DDiagnosticReportTransformer;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@AllArgsConstructor
public class FhirToDatamart {

  private String inputDirectory;

  private String resourceType;

  @SneakyThrows
  public static void main(String[] args) {
    if (args.length != 2) {
      throw new RuntimeException(
          "Missing command line arguments. Expected <resource-type> <input-directory>");
    }
    String resourceType = args[0];
    String inputDirectory = args[1];
    new FhirToDatamart(inputDirectory, resourceType).fhirToDatamart();
    System.exit(0);
  }

  @SneakyThrows
  private void dmObjectToFile(String fileName, Object object) {
    ObjectMapper mapper = mapper();
    String outputDirectoryName = "target/fhir-to-datamart-samples";
    File outputDirectory = new File(outputDirectoryName);
    if (!outputDirectory.exists()) {
      outputDirectory.mkdir();
    }
    mapper.writeValue(new File(outputDirectory + "/dm" + fileName), object);
  }

  @SneakyThrows
  private void fhirToDatamart() {
    Files.walk(Paths.get(inputDirectory))
        .filter(Files::isRegularFile)
        .map(Path::toFile)
        .filter(f -> f.getName().matches(pattern(resourceType)))
        .forEach(f -> transformToDm(f, resourceType));
  }

  private ObjectMapper mapper() {
    return JacksonConfig.createMapper()
        .setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
  }

  private String pattern(String resource) {
    switch (resource) {
      case "AllergyIntolerance":
        return "^AllInt(?!P).*json$";
      case "Condition":
        return "^Con(?!P).*json$";
      case "DiagnosticReport":
        return "^DiaRep(?!P).*json$";
      case "Immunization":
        return "^Imm(?!P).*json$";
      case "Medication":
        return "^Med(?!P|Sta|Ord).*json$";
      case "MedicationOrder":
        return "^MedOrd(?!P).*json$";
      case "MedicationStatement":
        return "^MedSta(?!P).*json$";
      case "Observation":
        return "^Obs(?!P).*json$";
      case "Patient":
        return "^Pat(?!P).*json$";
      case "Procedure":
        return "^Pro(?!P).*json$";
      default:
        throw new IllegalArgumentException("Unknown Resource : " + resource);
    }
  }

  @SneakyThrows
  private void transformToDm(File file, String resource) {
    ObjectMapper mapper = JacksonConfig.createMapper();
    switch (resource) {
      case "AllergyIntolerance":
        F2DAllergyIntoleranceTransformer allergyIntoleranceTransformer =
            new F2DAllergyIntoleranceTransformer();
        DatamartAllergyIntolerance datamartAllergyIntolerance =
            allergyIntoleranceTransformer.fhirToDatamart(
                mapper.readValue(file, AllergyIntolerance.class));
        dmObjectToFile(file.getName(), datamartAllergyIntolerance);
        break;
      case "DiagnosticReport":
        F2DDiagnosticReportTransformer diagnosticReportTransformer =
            new F2DDiagnosticReportTransformer();
        DatamartDiagnosticReports datamartDiagnosticReports =
            diagnosticReportTransformer.fhirToDatamart(
                mapper.readValue(file, DiagnosticReport.class));
        dmObjectToFile(file.getName(), datamartDiagnosticReports);
        break;
      default:
        throw new IllegalArgumentException("Unsupported Resource : " + resource);
    }
  }
}
