package gov.va.api.health.dataquery.service.controller.medicationrequest;

import gov.va.api.health.dataquery.service.controller.ExtractIcnValidator;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.uscorer4.api.resources.MedicationRequest;
import java.util.List;
import org.junit.jupiter.api.Test;

public class MedicationRequestIncludesIcnMajigTest {

  @Test
  public void r4() {
    ExtractIcnValidator.builder()
        .majig(new R4MedicationRequestIncludesIcnMajig())
        .body(
            MedicationRequest.builder()
                .id("123")
                .subject(Reference.builder().reference("Patient/1010101010V666666").build())
                .build())
        .expectedIcns(List.of("1010101010V666666"))
        .build()
        .assertIcn();
  }
}
