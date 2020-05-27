package gov.va.api.health.dataquery.service.controller.observation;

import gov.va.api.health.argonaut.api.resources.Observation;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class R4ObservationTransformerTest {

    @Test
    public void empty() {
        assertThat(
                R4ObservationTransformer.builder()
                        .datamart(DatamartObservation.builder().build())
                        .build()
                        .toFhir())
                .isEqualTo(Observation.builder().resourceType("Observation").build());
    }

    @Test
    public void interpretationDisplay() {
        assertThat(R4ObservationTransformer.interpretationDisplay("_GeneticObservationInterpretation"))
                .isEqualTo("GeneticObservationInterpretation");
        assertThat(R4ObservationTransformer.interpretationDisplay("CAR"))
                .isEqualTo("Carrier");
        assertThat(R4ObservationTransformer.interpretationDisplay("_ObservationInterpretationChange"))
                .isEqualTo("ObservationInterpretationChange");
        assertThat(R4ObservationTransformer.interpretationDisplay("_ObservationInterpretationExceptions"))
                .isEqualTo("ObservationInterpretationExceptions");
        assertThat(R4ObservationTransformer.interpretationDisplay("_ObservationInterpretationSusceptibility"))
                .isEqualTo("ObservationInterpretationSusceptibility");
        assertThat(R4ObservationTransformer.interpretationDisplay("ObservationInterpretationDetection"))
                .isEqualTo("ObservationInterpretationDetection");
        assertThat(R4ObservationTransformer.interpretationDisplay("ObservationInterpretationExpectation"))
                .isEqualTo("ObservationInterpretationExpectation");
        assertThat(R4ObservationTransformer.interpretationDisplay("ReactivityObservationInterpretation"))
                .isEqualTo("ReactivityObservationInterpretation");
        assertThat(R4ObservationTransformer.interpretationDisplay("<")).isEqualTo("Off scale low");
        assertThat(R4ObservationTransformer.interpretationDisplay(">")).isEqualTo("Off scale high");
        assertThat(R4ObservationTransformer.interpretationDisplay("A")).isEqualTo("Abnormal");
        assertThat(R4ObservationTransformer.interpretationDisplay("AA"))
                .isEqualTo("Critically abnormal");
        assertThat(R4ObservationTransformer.interpretationDisplay("B")).isEqualTo("Better");
        assertThat(R4ObservationTransformer.interpretationDisplay("D"))
                .isEqualTo("Significant change down");
        assertThat(R4ObservationTransformer.interpretationDisplay("DET")).isEqualTo("Detected");
        assertThat(R4ObservationTransformer.interpretationDisplay("E")).isEqualTo("Equivocal");
        assertThat(R4ObservationTransformer.interpretationDisplay("EX")).isEqualTo("outside threshold");
        assertThat(R4ObservationTransformer.interpretationDisplay("EXP")).isEqualTo("Expected");
        assertThat(R4ObservationTransformer.interpretationDisplay("H")).isEqualTo("High");
        assertThat(R4ObservationTransformer.interpretationDisplay("HH"))
                .isEqualTo("Critically high");
        assertThat(R4ObservationTransformer.interpretationDisplay("HU")).isEqualTo("Significantly high");
        assertThat(R4ObservationTransformer.interpretationDisplay("I")).isEqualTo("Intermediate");
        assertThat(R4ObservationTransformer.interpretationDisplay("IE"))
                .isEqualTo("Insufficient evidence");
        assertThat(R4ObservationTransformer.interpretationDisplay("IND")).isEqualTo("Indeterminate");
        assertThat(R4ObservationTransformer.interpretationDisplay("L")).isEqualTo("Low");
        assertThat(R4ObservationTransformer.interpretationDisplay("LL")).isEqualTo("Critically low");
        assertThat(R4ObservationTransformer.interpretationDisplay("LU")).isEqualTo("Significantly low");
        assertThat(R4ObservationTransformer.interpretationDisplay("LX")).isEqualTo("below low threshold");
        assertThat(R4ObservationTransformer.interpretationDisplay("MS"))
                .isEqualTo("Moderately susceptible. Indicates for microbiology susceptibilities only.");
        assertThat(R4ObservationTransformer.interpretationDisplay("N")).isEqualTo("Normal");
        assertThat(R4ObservationTransformer.interpretationDisplay("NCL")).isEqualTo("No CLSI defined breakpoint");
        assertThat(R4ObservationTransformer.interpretationDisplay("ND")).isEqualTo("Not Detected");
        assertThat(R4ObservationTransformer.interpretationDisplay("NEG")).isEqualTo("Negative");
        assertThat(R4ObservationTransformer.interpretationDisplay("NR")).isEqualTo("Non-reactive");
        assertThat(R4ObservationTransformer.interpretationDisplay("NS"))
                .isEqualTo("Non-susceptible");
        assertThat(R4ObservationTransformer.interpretationDisplay("POS")).isEqualTo("Positive");
        assertThat(R4ObservationTransformer.interpretationDisplay("R")).isEqualTo("Resistant");
        assertThat(R4ObservationTransformer.interpretationDisplay("RR")).isEqualTo("Reactive");
        assertThat(R4ObservationTransformer.interpretationDisplay("S")).isEqualTo("Susceptible");
        assertThat(R4ObservationTransformer.interpretationDisplay("SDD"))
                .isEqualTo("Susceptible-dose dependent");
        assertThat(R4ObservationTransformer.interpretationDisplay("SYN-R"))
                .isEqualTo("Synergy - resistant");
        assertThat(R4ObservationTransformer.interpretationDisplay("SYN-S"))
                .isEqualTo("Synergy - susceptible");
        assertThat(R4ObservationTransformer.interpretationDisplay("U"))
                .isEqualTo("Significant change up");
        assertThat(R4ObservationTransformer.interpretationDisplay("UNE")).isEqualTo("Unexpected");
        assertThat(R4ObservationTransformer.interpretationDisplay("VS"))
                .isEqualTo("Very susceptible. Indicates for microbiology susceptibilities only.");
        assertThat(R4ObservationTransformer.interpretationDisplay("W")).isEqualTo("Worse");
        assertThat(R4ObservationTransformer.interpretationDisplay("WR"))
                .isEqualTo("Weakly reactive");
        assertThat(R4ObservationTransformer.interpretationDisplay("RANDOM")).isNull();
    }
}
