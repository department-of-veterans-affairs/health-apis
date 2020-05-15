package gov.va.api.health.dataquery.service.controller.medication;

import org.junit.Test;

public class R4MedicationTransformerTest {

    @Test
    public void toFhir() {
        DatamartMedication dmMedication = MedicationSamples.Datamart.create().medication();

//       todo assertThat(R4MedicationTransformer.builder().datamart(dmMedication).build().toFhir()
//                .isEqualTo(MedicationSamples.));
    }
}
