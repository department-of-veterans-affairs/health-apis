package gov.va.api.health.dataquery.tools.minimart;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.argonaut.api.resources.*;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.DatamartAllergyIntolerance;
import gov.va.api.health.dataquery.service.controller.condition.DatamartCondition;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.DatamartDiagnosticReports;
import gov.va.api.health.dataquery.service.controller.immunization.DatamartImmunization;
import gov.va.api.health.dataquery.service.controller.medication.DatamartMedication;
import gov.va.api.health.dataquery.service.controller.patient.DatamartPatient;
import gov.va.api.health.dataquery.tools.minimart.transformers.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FhirToDatamart {

  private String inputDirectory;

  private String resourceType;

  private FhirToDatamartUtils fauxIds;

  public FhirToDatamart(String inputDirectory, String resourceType, String idsFile) {
    this.inputDirectory = inputDirectory;
    this.resourceType = resourceType;
    this.fauxIds = new FhirToDatamartUtils(idsFile);
  }

  @SneakyThrows
  public static void main(String[] args) {
    if (args.length != 3) {
      throw new RuntimeException(
          "Missing command line arguments. Expected <resource-type> <input-directory> <config-file>");
    }
    String resourceType = args[0];
    String inputDirectory = args[1];
    String configFile = args[2];
    new FhirToDatamart(inputDirectory, resourceType, configFile).fhirToDatamart();
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
    log.info("Outputting to " + outputDirectory + "/dm" + fileName);
    mapper.writeValue(new File(outputDirectory + "/dm" + fileName), object);
  }

  @SneakyThrows
  private void fhirToDatamart() {
    log.info("Discovering " + resourceType + " files from " + inputDirectory);
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
        return "^Pat(?!i).*json$";
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
            new F2DAllergyIntoleranceTransformer(fauxIds);
        DatamartAllergyIntolerance datamartAllergyIntolerance =
            allergyIntoleranceTransformer.fhirToDatamart(
                mapper.readValue(file, AllergyIntolerance.class));
        dmObjectToFile(file.getName(), datamartAllergyIntolerance);
        break;
      case "Condition":
        F2DConditionTransformer conditionTransformer = new F2DConditionTransformer(fauxIds);
        DatamartCondition datamartCondition =
            conditionTransformer.fhirToDatamart(mapper.readValue(file, Condition.class));
        dmObjectToFile(file.getName(), datamartCondition);
        break;
      case "DiagnosticReport":
        F2DDiagnosticReportTransformer diagnosticReportTransformer =
            new F2DDiagnosticReportTransformer(fauxIds);
        DatamartDiagnosticReports datamartDiagnosticReports =
            diagnosticReportTransformer.fhirToDatamart(
                mapper.readValue(file, DiagnosticReport.class));
        dmObjectToFile(file.getName(), datamartDiagnosticReports);
        break;
      case "Immunization":
        F2DImmunizationTransformer immunizationTransformer =
            new F2DImmunizationTransformer(fauxIds);
        DatamartImmunization datamartImmunization =
            immunizationTransformer.fhirToDatamart(mapper.readValue(file, Immunization.class));
        dmObjectToFile(file.getName(), datamartImmunization);
        break;
      case "Medication":
        F2DMedicationTransformer medicationTransformer = new F2DMedicationTransformer(fauxIds);
        DatamartMedication datamartMedication =
            medicationTransformer.fhirToDatamart(mapper.readValue(file, Medication.class));
        dmObjectToFile(file.getName(), datamartMedication);
        break;
      case "Patient":
        F2DPatientTransformer patientTransformer = new F2DPatientTransformer(fauxIds);
        DatamartPatient datamartPatient =
            patientTransformer.fhirToDatamart(mapper.readValue(file, Patient.class));
        dmObjectToFile(file.getName(), datamartPatient);
        break;
      default:
        throw new IllegalArgumentException("Unsupported Resource : " + resource);
    }
  }
}
