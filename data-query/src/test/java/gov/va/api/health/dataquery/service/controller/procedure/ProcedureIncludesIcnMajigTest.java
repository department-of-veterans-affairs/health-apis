package gov.va.api.health.dataquery.service.controller.procedure;

import gov.va.api.health.dataquery.service.controller.ExtractIcnValidator;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ProcedureIncludesIcnMajigTest {
  @Test
  public void dstu2() {
    ExtractIcnValidator.builder()
        .majig(new Dstu2ProcedureIncludesIcnMajig())
        .body(
            gov.va.api.health.argonaut.api.resources.Procedure.builder()
                .id("123")
                .subject(
                    gov.va.api.health.dstu2.api.elements.Reference.builder()
                        .reference("Patient/1010101010V666666")
                        .build())
                .build())
        .expectedIcns(List.of("1010101010V666666"))
        .build()
        .assertIcn();
  }

  @Test
  public void r4() {
    ExtractIcnValidator.builder()
        .majig(new R4ProcedureIncludesIcnMajig())
        .body(
            gov.va.api.health.uscorer4.api.resources.Procedure.builder()
                .id("123")
                .subject(
                    gov.va.api.health.r4.api.elements.Reference.builder()
                        .reference("Patient/1010101010V666666")
                        .build())
                .build())
        .expectedIcns(List.of("1010101010V666666"))
        .build()
        .assertIcn();
  }
}
