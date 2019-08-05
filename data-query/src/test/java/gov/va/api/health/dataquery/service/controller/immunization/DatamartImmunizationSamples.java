package gov.va.api.health.dataquery.service.controller.immunization;

import gov.va.api.health.dataquery.service.controller.condition.DatamartCondition;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import lombok.AllArgsConstructor;

import java.util.Optional;

public class DatamartImmunizationSamples {

    @AllArgsConstructor(staticName = "create")
    static class Datamart {
        public DatamartImmunization immunization() {return immunization("1000000030337","1011549983V753765"); }
              public DatamartImmunization immunization(String cdwId, String patientId) {

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
                                        .reference(patientId)
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
        }
