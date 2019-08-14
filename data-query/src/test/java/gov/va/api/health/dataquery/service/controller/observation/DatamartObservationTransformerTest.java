package gov.va.api.health.dataquery.service.controller.observation;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dstu2.api.datatypes.Coding;
import org.junit.Test;

public class DatamartObservationTransformerTest {
  @Test
  public void categoryCoding() {
    assertThat(DatamartObservationTransformer.categoryCoding(DatamartObservation.Category.exam))
        .isEqualTo(
            Coding.builder()
                .system("http://hl7.org/fhir/observation-category")
                .code("exam")
                .display("Exam")
                .build());
    assertThat(DatamartObservationTransformer.categoryCoding(DatamartObservation.Category.imaging))
        .isEqualTo(
            Coding.builder()
                .system("http://hl7.org/fhir/observation-category")
                .code("imaging")
                .display("Imaging")
                .build());
    assertThat(
            DatamartObservationTransformer.categoryCoding(DatamartObservation.Category.laboratory))
        .isEqualTo(
            Coding.builder()
                .system("http://hl7.org/fhir/observation-category")
                .code("laboratory")
                .display("Laboratory")
                .build());
    assertThat(
            DatamartObservationTransformer.categoryCoding(DatamartObservation.Category.procedure))
        .isEqualTo(
            Coding.builder()
                .system("http://hl7.org/fhir/observation-category")
                .code("procedure")
                .display("Procedure")
                .build());
    assertThat(
            DatamartObservationTransformer.categoryCoding(
                DatamartObservation.Category.social_history))
        .isEqualTo(
            Coding.builder()
                .system("http://hl7.org/fhir/observation-category")
                .code("social-history")
                .display("Social History")
                .build());
    assertThat(DatamartObservationTransformer.categoryCoding(DatamartObservation.Category.survey))
        .isEqualTo(
            Coding.builder()
                .system("http://hl7.org/fhir/observation-category")
                .code("survey")
                .display("Survey")
                .build());
    assertThat(DatamartObservationTransformer.categoryCoding(DatamartObservation.Category.therapy))
        .isEqualTo(
            Coding.builder()
                .system("http://hl7.org/fhir/observation-category")
                .code("therapy")
                .display("Therapy")
                .build());
    assertThat(
            DatamartObservationTransformer.categoryCoding(DatamartObservation.Category.vital_signs))
        .isEqualTo(
            Coding.builder()
                .system("http://hl7.org/fhir/observation-category")
                .code("vital-signs")
                .display("Vital Signs")
                .build());
  }

  @Test
  public void interpretationDisplay() {
    assertThat(DatamartObservationTransformer.interpretationDisplay("<"))
        .isEqualTo("Off scale low");
    assertThat(DatamartObservationTransformer.interpretationDisplay(">"))
        .isEqualTo("Off scale high");
    assertThat(DatamartObservationTransformer.interpretationDisplay("A")).isEqualTo("Abnormal");
    assertThat(DatamartObservationTransformer.interpretationDisplay("AA"))
        .isEqualTo("Critically abnormal");
    assertThat(DatamartObservationTransformer.interpretationDisplay("B")).isEqualTo("Better");
    assertThat(DatamartObservationTransformer.interpretationDisplay("D"))
        .isEqualTo("Significant change down");
    assertThat(DatamartObservationTransformer.interpretationDisplay("DET")).isEqualTo("Detected");
    assertThat(DatamartObservationTransformer.interpretationDisplay("H")).isEqualTo("High");
    assertThat(DatamartObservationTransformer.interpretationDisplay("HH"))
        .isEqualTo("Critically high");
    assertThat(DatamartObservationTransformer.interpretationDisplay("HU")).isEqualTo("Very high");
    assertThat(DatamartObservationTransformer.interpretationDisplay("I")).isEqualTo("Intermediate");
    assertThat(DatamartObservationTransformer.interpretationDisplay("IE"))
        .isEqualTo("Insufficient evidence");
    assertThat(DatamartObservationTransformer.interpretationDisplay("IND"))
        .isEqualTo("Indeterminate");
    assertThat(DatamartObservationTransformer.interpretationDisplay("L")).isEqualTo("Low");
    assertThat(DatamartObservationTransformer.interpretationDisplay("LL"))
        .isEqualTo("Critically low");
    assertThat(DatamartObservationTransformer.interpretationDisplay("LU")).isEqualTo("Very low");
    assertThat(DatamartObservationTransformer.interpretationDisplay("MS"))
        .isEqualTo("Moderately susceptible. Indicates for microbiology susceptibilities only.");
    assertThat(DatamartObservationTransformer.interpretationDisplay("N")).isEqualTo("Normal");
    assertThat(DatamartObservationTransformer.interpretationDisplay("ND"))
        .isEqualTo("Not Detected");
    assertThat(DatamartObservationTransformer.interpretationDisplay("NEG")).isEqualTo("Negative");
    assertThat(DatamartObservationTransformer.interpretationDisplay("NR"))
        .isEqualTo("Non-reactive");
    assertThat(DatamartObservationTransformer.interpretationDisplay("NS"))
        .isEqualTo("Non-susceptible");
    assertThat(DatamartObservationTransformer.interpretationDisplay("POS")).isEqualTo("Positive");
    assertThat(DatamartObservationTransformer.interpretationDisplay("R")).isEqualTo("Resistant");
    assertThat(DatamartObservationTransformer.interpretationDisplay("RR")).isEqualTo("Reactive");
    assertThat(DatamartObservationTransformer.interpretationDisplay("S")).isEqualTo("Susceptible");
    assertThat(DatamartObservationTransformer.interpretationDisplay("SDD"))
        .isEqualTo("Susceptible-dose dependent");
    assertThat(DatamartObservationTransformer.interpretationDisplay("SYN-R"))
        .isEqualTo("Synergy - resistant");
    assertThat(DatamartObservationTransformer.interpretationDisplay("SYN-S"))
        .isEqualTo("Synergy - susceptible");
    assertThat(DatamartObservationTransformer.interpretationDisplay("U"))
        .isEqualTo("Significant change up");
    assertThat(DatamartObservationTransformer.interpretationDisplay("VS"))
        .isEqualTo("Very susceptible. Indicates for microbiology susceptibilities only.");
    assertThat(DatamartObservationTransformer.interpretationDisplay("W")).isEqualTo("Worse");
    assertThat(DatamartObservationTransformer.interpretationDisplay("WR"))
        .isEqualTo("Weakly reactive");
  }
}
