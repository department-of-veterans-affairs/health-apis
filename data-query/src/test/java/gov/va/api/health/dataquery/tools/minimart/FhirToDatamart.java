package gov.va.api.health.dataquery.tools.minimart;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.argonaut.api.resources.AllergyIntolerance;
import gov.va.api.health.argonaut.api.resources.Condition;
import gov.va.api.health.argonaut.api.resources.DiagnosticReport;
import gov.va.api.health.argonaut.api.resources.Immunization;
import gov.va.api.health.argonaut.api.resources.Medication;
import gov.va.api.health.argonaut.api.resources.MedicationOrder;
import gov.va.api.health.argonaut.api.resources.MedicationStatement;
import gov.va.api.health.argonaut.api.resources.Observation;
import gov.va.api.health.argonaut.api.resources.Patient;
import gov.va.api.health.argonaut.api.resources.Procedure;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.medication.DatamartMedication;
import gov.va.api.health.dataquery.tools.minimart.transformers.F2DAllergyIntoleranceTransformer;
import gov.va.api.health.dataquery.tools.minimart.transformers.F2DConditionTransformer;
import gov.va.api.health.dataquery.tools.minimart.transformers.F2DDiagnosticReportTransformer;
import gov.va.api.health.dataquery.tools.minimart.transformers.F2DImmunizationTransformer;
import gov.va.api.health.dataquery.tools.minimart.transformers.F2DMedicationOrderTransformer;
import gov.va.api.health.dataquery.tools.minimart.transformers.F2DMedicationStatementTransformer;
import gov.va.api.health.dataquery.tools.minimart.transformers.F2DMedicationTransformer;
import gov.va.api.health.dataquery.tools.minimart.transformers.F2DObservationTransformer;
import gov.va.api.health.dataquery.tools.minimart.transformers.F2DPatientTransformer;
import gov.va.api.health.dataquery.tools.minimart.transformers.F2DProcedureTransformer;
import gov.va.api.health.dstu2.api.bundle.AbstractEntry;
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

  FhirToDatamart(String inputDirectory, String resourceType, String idsFile) {
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
    String outputPath = outputDirectory + "/dm" + fileName.replaceAll(":", "");
    log.info("Outputting to " + outputPath);
    mapper.writeValue(new File(outputPath), object);
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
        return "^AllIntP.*json$";
      case "Condition":
        return "^ConP.*json$";
      case "DiagnosticReport":
        return "^DiaRepP.*json$";
      case "Immunization":
        return "^ImmP.*json$";
      case "Medication":
        return "^Med(?!P|Sta|Ord).*json$";
      case "MedicationOrder":
        return "^MedOrdP.*json$";
      case "MedicationStatement":
        return "^MedStaP.*json$";
      case "Observation":
        return "^ObsP.*json$";
      case "Patient":
        return "^Pati.*json$";
      case "Procedure":
        return "^ProP.*json$";
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
        AllergyIntolerance.Bundle allergyIntolerances =
            mapper.readValue(file, AllergyIntolerance.Bundle.class);
        allergyIntolerances
            .entry()
            .stream()
            .map(AbstractEntry::resource)
            .map(allergyIntoleranceTransformer::fhirToDatamart)
            .forEach(a -> dmObjectToFile("AllInt" + a.cdwId() + ".json", a));
        break;
      case "Condition":
        F2DConditionTransformer conditionTransformer = new F2DConditionTransformer(fauxIds);
        Condition.Bundle conditions = mapper.readValue(file, Condition.Bundle.class);
        conditions
            .entry()
            .stream()
            .map(AbstractEntry::resource)
            .map(conditionTransformer::fhirToDatamart)
            .forEach(c -> dmObjectToFile("Con" + c.cdwId() + ".json", c));
        break;
      case "DiagnosticReport":
        F2DDiagnosticReportTransformer diagnosticReportTransformer =
            new F2DDiagnosticReportTransformer(fauxIds);
        DiagnosticReport.Bundle diagnosticReports =
            mapper.readValue(file, DiagnosticReport.Bundle.class);
        diagnosticReports
            .entry()
            .stream()
            .map(AbstractEntry::resource)
            .map(diagnosticReportTransformer::fhirToDatamart)
            .forEach(d -> dmObjectToFile("DiaRep" + d.reports().get(0).identifier() + ".json", d));
        break;
      case "Immunization":
        F2DImmunizationTransformer immunizationTransformer =
            new F2DImmunizationTransformer(fauxIds);
        Immunization.Bundle immunizations = mapper.readValue(file, Immunization.Bundle.class);
        immunizations
            .entry()
            .stream()
            .map(AbstractEntry::resource)
            .map(immunizationTransformer::fhirToDatamart)
            .forEach(i -> dmObjectToFile("Imm" + i.cdwId() + ".json", i));
        break;
      case "Medication":
        F2DMedicationTransformer medicationTransformer = new F2DMedicationTransformer(fauxIds);
        DatamartMedication datamartMedication =
            medicationTransformer.fhirToDatamart(mapper.readValue(file, Medication.class));
        dmObjectToFile(file.getName(), datamartMedication);
        break;
      case "MedicationOrder":
        F2DMedicationOrderTransformer medicationOrderTransformer =
            new F2DMedicationOrderTransformer(fauxIds);
        MedicationOrder.Bundle medicationOrders =
            mapper.readValue(file, MedicationOrder.Bundle.class);
        medicationOrders
            .entry()
            .stream()
            .map(AbstractEntry::resource)
            .map(medicationOrderTransformer::fhirToDatamart)
            .forEach(mo -> dmObjectToFile("MedOrd" + mo.cdwId() + ".json", mo));
        break;
      case "MedicationStatement":
        F2DMedicationStatementTransformer medicationStatementTransformer =
            new F2DMedicationStatementTransformer(fauxIds);
        MedicationStatement.Bundle medicationStatements =
            mapper.readValue(file, MedicationStatement.Bundle.class);
        medicationStatements
            .entry()
            .stream()
            .map(AbstractEntry::resource)
            .map(medicationStatementTransformer::fhirToDatamart)
            .forEach(ms -> dmObjectToFile("MedSta" + ms.cdwId() + ".json", ms));
        break;
      case "Observation":
        F2DObservationTransformer observationTransformer = new F2DObservationTransformer(fauxIds);
        Observation.Bundle observations = mapper.readValue(file, Observation.Bundle.class);
        observations
            .entry()
            .stream()
            .map(AbstractEntry::resource)
            .map(observationTransformer::fhirToDatamart)
            .forEach(o -> dmObjectToFile("Obs" + o.cdwId() + ".json", o));
        break;
      case "Patient":
        F2DPatientTransformer patientTransformer = new F2DPatientTransformer(fauxIds);
        Patient.Bundle patients = mapper.readValue(file, Patient.Bundle.class);
        patients
            .entry()
            .stream()
            .map(AbstractEntry::resource)
            .map(patientTransformer::fhirToDatamart)
            .forEach(p -> dmObjectToFile("Pat" + p.fullIcn() + ".json", p));
        break;
      case "Procedure":
        F2DProcedureTransformer procedureTransformer = new F2DProcedureTransformer(fauxIds);
        Procedure.Bundle procedures = mapper.readValue(file, Procedure.Bundle.class);
        procedures
            .entry()
            .stream()
            .map(AbstractEntry::resource)
            .map(procedureTransformer::fhirToDatamart)
            .forEach(pr -> dmObjectToFile("Pro" + pr.cdwId() + ".json", pr));
        break;
      default:
        throw new IllegalArgumentException("Unsupported Resource : " + resource);
    }
  }
}
