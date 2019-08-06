package gov.va.api.health.dataquery.service.controller.procedure;

import static gov.va.api.health.autoconfig.configuration.JacksonConfig.createMapper;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dataquery.service.controller.immunization.DatamartImmunization;
import lombok.SneakyThrows;
import org.junit.Test;

public class DatamartProcedureTest {
  private DatamartProcedure sample() {
    return DatamartProcedure.builder()
   .cdwId("1000000719261")
       .etlDate("2008-01-02T06:00:00Z")
       .patient(DatamartReference.of()
     .type("Patient")
         .reference("1004476237V111282")
         .display("VETERAN,GRAY PRO").build()) 
   .status("completed")
       .coding": {
     .system("http://www.ama-assn.org/go/cpt")
         .code("90870")
         .display("ELECTROCONVULSIVE THERAPY"
    },
   .notPerformed": false,
       .reasonNotPerformed": null,
       .performedDateTime("2008-01-02T06:00:00Z")
       .encounter(DatamartReference.of()
     .type("Encounter")
         .reference("1000124525706")
         .display("1000124525706").build()
       .location(DatamartReference.of()
     .type("Location")
         .reference("237281")
         .display("ZZPSYCHIATRY")
    .build())
        
        
        .build();
    
  }

  @Test
  @SneakyThrows
  public void unmarshalSample() {
    DatamartImmunization dm =
        createMapper()
            .readValue(
                getClass().getResourceAsStream("datamart-procedure.json"),
                DatamartImmunization.class);
    assertThat(dm).isEqualTo(sample());
  }
}
