package gov.va.api.health.dataquery.service.controller.immunization;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DatamartImmunizationSamples {

  @AllArgsConstructor(staticName = "create")
  public static class Datamart {
    DatamartImmunization immunization() {
      String cdwId = "1000000030337";
      return DatamartImmunization.builder()
          .cdwId(cdwId)
          .status(DatamartImmunization.Status.completed)
          .etlDate("1997-04-03T21:02:15Z")
          .vaccineCode(
              DatamartImmunization.VaccineCode.builder()
                  .code("112")
                  .text("TETANUS TOXOID, UNSPECIFIED FORMULATION")
                  .build())
          .patient(
              DatamartReference.of()
                  .type("Patient")
                  .reference("1011549983V753765")
                  .display("ZZTESTPATIENT,THOMAS THE")
                  .build())
          .wasNotGiven(false)
          .performer(
              Optional.of(
                  DatamartReference.of()
                      .type("Practitioner")
                      .reference("3868169")
                      .display("ZHIVAGO,YURI ANDREYEVICH")
                      .build()))
          .requester(
              Optional.of(
                  DatamartReference.of()
                      .type("Practitioner")
                      .reference("1702436")
                      .display("SHINE,DOC RAINER")
                      .build()))
          .encounter(
              Optional.of(
                  DatamartReference.of()
                      .type("Encounter")
                      .reference("1000589847194")
                      .display("1000589847194")
                      .build()))
          .location(
              Optional.of(
                  DatamartReference.of()
                      .type("Location")
                      .reference("358359")
                      .display("ZZGOLD PRIMARY CARE")
                      .build()))
          .note(Optional.of("PATIENT CALM AFTER VACCINATION"))
          .reaction(
              Optional.of(
                  DatamartReference.of()
                      .type("Observation")
                      .reference(null)
                      .display("Other")
                      .build()))
          .vaccinationProtocols(
              Optional.of(
                  DatamartImmunization.VaccinationProtocols.builder()
                      .series("Booster")
                      .seriesDoses(1)
                      .build()))
          .build();
    }
  }

  @AllArgsConstructor(staticName = "create")
  static class Fhir {}
}
