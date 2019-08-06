package gov.va.api.health.dataquery.service.controller.medication;

import gov.va.api.health.argonaut.api.resources.Medication;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.elements.Narrative;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DatamartMedicationSamples {

    @AllArgsConstructor(staticName = "create")
    static class Datamart {
        DatamartMedication medication() {return medication("1000","4015523");}
        DatamartMedication medication(String cdwId, String productId) {
            return DatamartMedication.builder()
                    .objectType("Medication")
                    .objectVersion("1")
                    .cdwId(cdwId)
                    .etlDate("2019-08-06T16:12:26.430")
                    .rxnorm(
                            DatamartMedication.RxNorm.builder()
                                    .code("284205")
                                    .text("ALMOTRIPTAN MALATE 12.5MG TAB,UD")
                                    .build())
                    .product(DatamartMedication.Product.builder().id(productId).formText("TAB").build())
                    .build();
        }
    }

    @AllArgsConstructor(staticName = "create")
    static class Fhir {
        Medication medication(){return medication("1000","4015523");}

        Medication medication(String id, String productId) {
            return Medication.builder()
                    .resourceType(Medication.class.getSimpleName())
                    .id(id)
                    .code(CodeableConcept.builder().text("ALMOTRIPTAN MALATE 12.5MG TAB,UD").id("4015523").build())
                    .product(Medication.Product.builder().form(CodeableConcept.builder().id("4015523").text("TAB").build()).build())
                    .build();
        }
    }
}
