package gov.va.api.health.dataquery.service.controller.medicationstatement;

import gov.va.api.health.argonaut.api.resources.MedicationStatement;
import gov.va.api.health.dataquery.service.controller.ExtractIcnValidator;
import gov.va.api.health.dstu2.api.elements.Reference;
import java.util.List;
import org.junit.Test;

public class MedicationStatementIncludesIcnMajigTest {

  @Test
  public void extractIcn() {
    ExtractIcnValidator.<MedicationStatementIncludesIcnMajig, MedicationStatement>builder()
        .majig(new MedicationStatementIncludesIcnMajig())
        .body(
            MedicationStatement.builder()
                .id("123")
                .patient(Reference.builder().reference("Patient/1010101010V666666").build())
                .build())
        .expectedIcns(List.of("1010101010V666666"))
        .build()
        .assertIcn();
  }
}
