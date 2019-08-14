package gov.va.api.health.dataquery.fhirtodatamart;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.argonaut.api.resources.AllergyIntolerance;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.DatamartAllergyIntolerance;
import lombok.SneakyThrows;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;


public class FhirToDatamart {
  private final Properties config = new Properties(System.getProperties());

  @SneakyThrows
  private Path directory() {
    String directoryPath = config.getProperty("fhir.json.directory", "");
    File directory = new File(directoryPath);
    return directory.toPath();
  }

  private String pattern(String Resource) {
    switch (Resource) {
      case "AllergyIntolerance":
        return "^AllInt.*json";
      case "Condition":
        return "^Con.*json";
      case "DiagnosticReport":
        return "^DiaRep.*json";
      case "Immunization":
        return "^Imm.*json";
      case "Medication":
        return "^Med.*json";
      case "MedicationOrder":
        return "^MedOrd.*json";
      case "MedicationStatement":
        return "^MedSta.*json";
      case "Observation":
        return "^Obs.*json";
      case "Patient":
        return "^Pat.*json";
      case "Procedure":
        return "^Pro.*json";
      default:
        return "^AllInt.*json|^Con.*json|^DiaRep.*json|^Imm.*json|^Med.*json|^MedOrd.*json|^MedSta.*json|^Obs.*json|^Pat.*json|^Pro.*json";
    }
  }

  private DatamartAllergyIntolerance fhirToDatamart(AllergyIntolerance allergyIntolerance) {
return DatamartAllergyIntolerance.builder()
        .objectType(allergyIntolerance.resourceType())
        .cdwId(allergyIntolerance.id())
        .build();
  }

  @SneakyThrows
  @Test
  public void readInAllergyIntolerance() {
    ObjectMapper mapper =new ObjectMapper();
    mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    List<File> files = Files.walk(directory())
            .filter(Files::isRegularFile)
            .map(Path::toFile)
            .filter(f -> f.getName().matches(pattern("AllergyIntolerance")))
            .collect(Collectors.toList());
    for (File file : files){
      AllergyIntolerance allergyIntolerance = mapper.readValue(file, AllergyIntolerance.class);
      DatamartAllergyIntolerance datamartAllergyIntolerance = fhirToDatamart(allergyIntolerance);
      mapper.writeValue(new File("target/DMAllInt"+datamartAllergyIntolerance.cdwId()+".json"),datamartAllergyIntolerance);
    }

  }
}
