package gov.va.api.health.dataquery.service.controller.medication;

import gov.va.api.health.argonaut.api.resources.Medication;
import lombok.Builder;

@Builder
public class DatamartMedicationTransformer {

    private  final DatamartMedication datamart;

    /** Convert the datamart structure to FHIR compliant structure. */
    public Medication toFhir() {
        return Medication.builder()
                .build();
    }
}
