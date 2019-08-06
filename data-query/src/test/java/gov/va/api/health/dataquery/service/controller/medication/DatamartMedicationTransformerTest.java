package gov.va.api.health.dataquery.service.controller.medication;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import lombok.SneakyThrows;
import org.junit.Test;
import gov.va.api.health.dataquery.service.controller.medication.DatamartMedicationSamples.Fhir;
import gov.va.api.health.dataquery.service.controller.medication.DatamartMedicationSamples.Datamart;

public class DatamartMedicationTransformerTest {

    @Test public void medication() {
        assertThat(json(tx(Datamart.create().medication()).toFhir()))
        .isEqualTo(json(Fhir.create().medication()));
    }
    DatamartMedicationTransformer tx(DatamartMedication dm) {
        return DatamartMedicationTransformer.builder().datamart(dm).build();
    }

    @SneakyThrows
    String json(Object o) {
        return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
    }

}
