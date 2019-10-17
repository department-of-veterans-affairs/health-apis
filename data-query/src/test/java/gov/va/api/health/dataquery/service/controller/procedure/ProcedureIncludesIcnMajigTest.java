package gov.va.api.health.dataquery.service.controller.procedure;

import gov.va.api.health.argonaut.api.resources.Procedure;
import gov.va.api.health.dataquery.service.controller.ExtractIcnValidator;
import gov.va.api.health.dstu2.api.elements.Reference;
import java.util.List;
import org.junit.Test;

public class ProcedureIncludesIcnMajigTest {

  @Test
  public void extractIcn() {
    ExtractIcnValidator.<ProcedureIncludesIcnMajig, Procedure>builder()
        .majig(new ProcedureIncludesIcnMajig())
        .body(
            Procedure.builder()
                .id("123")
                .subject(Reference.builder().reference("Patient/1010101010V666666").build())
                .build())
        .expectedIcns(List.of("1010101010V666666"))
        .build()
        .assertIcn();
  }
}
