package gov.va.api.health.dataquery.service.controller.immunization;


import gov.va.api.health.argonaut.api.resources.Immunization;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DatamartImmunizationTransformerTest {
    @Test
    public void status() {
        DatamartImmunizationTransformer tx = tx(DatamartImmunizationSamples.Datamart.create().immunization());
        assertThat(tx.status(null)).isNull();
        assertThat(tx.status(DatamartImmunization.Status.completed))
                .isEqualTo(Immunization.Status.completed);
        assertThat(tx.status(DatamartImmunization.Status.entered_in_error))
                .isEqualTo(Immunization.Status.entered_in_error);

    }
    DatamartImmunizationTransformer tx(DatamartImmunization dm) {
        return DatamartImmunizationTransformer.builder().datamart(dm).build();
    }
}
