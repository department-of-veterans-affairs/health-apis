package gov.va.api.health.dataquery.service.controller.medication;

import gov.va.api.health.argonaut.api.resources.Medication;
import gov.va.api.health.dataquery.service.controller.ExtractIcnValidator;
import gov.va.api.health.dstu2.api.elements.Reference;
import java.util.List;
import org.junit.Test;

public class MedicationIncludesIcnMajigTest {

    @Test
    public void extractNoIcns() {
        ExtractIcnValidator.<MedicationIncludesIcnMajig, Medication>builder()
                .majig(new MedicationIncludesIcnMajig())
                .body(
                        Medication.builder()
                                .id("123")
                                .build())
                .expectedIcns(List.of("NONE"))
                .build()
                .assertIcn();
    }
}