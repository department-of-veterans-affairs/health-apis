package gov.va.api.health.dataquery.service.controller.observation;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.uscorer4.api.resources.Observation;
import java.util.List;
import java.util.Optional;
import org.assertj.core.util.Lists;
import org.junit.Test;

public class R4ObservationTransformerTest {
  @Test
  public void categoryCoding() {
    assertThat(R4ObservationTransformer.categoryCoding(DatamartObservation.Category.exam))
        .isEqualTo(
            Coding.builder()
                .system("http://terminology.hl7.org/CodeSystem/observation-category")
                .code("exam")
                .display("Exam")
                .build());
    assertThat(R4ObservationTransformer.categoryCoding(DatamartObservation.Category.imaging))
        .isEqualTo(
            Coding.builder()
                .system("http://terminology.hl7.org/CodeSystem/observation-category")
                .code("imaging")
                .display("Imaging")
                .build());
    assertThat(R4ObservationTransformer.categoryCoding(DatamartObservation.Category.laboratory))
        .isEqualTo(
            Coding.builder()
                .system("http://terminology.hl7.org/CodeSystem/observation-category")
                .code("laboratory")
                .display("Laboratory")
                .build());
    assertThat(R4ObservationTransformer.categoryCoding(DatamartObservation.Category.procedure))
        .isEqualTo(
            Coding.builder()
                .system("http://terminology.hl7.org/CodeSystem/observation-category")
                .code("procedure")
                .display("Procedure")
                .build());
    assertThat(R4ObservationTransformer.categoryCoding(DatamartObservation.Category.social_history))
        .isEqualTo(
            Coding.builder()
                .system("http://terminology.hl7.org/CodeSystem/observation-category")
                .code("social-history")
                .display("Social History")
                .build());
    assertThat(R4ObservationTransformer.categoryCoding(DatamartObservation.Category.survey))
        .isEqualTo(
            Coding.builder()
                .system("http://terminology.hl7.org/CodeSystem/observation-category")
                .code("survey")
                .display("Survey")
                .build());
    assertThat(R4ObservationTransformer.categoryCoding(DatamartObservation.Category.therapy))
        .isEqualTo(
            Coding.builder()
                .system("http://terminology.hl7.org/CodeSystem/observation-category")
                .code("therapy")
                .display("Therapy")
                .build());
    assertThat(R4ObservationTransformer.categoryCoding(DatamartObservation.Category.vital_signs))
        .isEqualTo(
            Coding.builder()
                .system("http://terminology.hl7.org/CodeSystem/observation-category")
                .code("vital-signs")
                .display("Vital Signs")
                .build());
  }

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
    assertThat(R4ObservationTransformer.interpretationDisplay("CAR")).isEqualTo("Carrier");
    assertThat(R4ObservationTransformer.interpretationDisplay("Carrier")).isEqualTo("Carrier");
    assertThat(R4ObservationTransformer.interpretationDisplay("<")).isEqualTo("Off scale low");
    assertThat(R4ObservationTransformer.interpretationDisplay(">")).isEqualTo("Off scale high");
    assertThat(R4ObservationTransformer.interpretationDisplay("A")).isEqualTo("Abnormal");
    assertThat(R4ObservationTransformer.interpretationDisplay("AA"))
        .isEqualTo("Critical abnormal");
    assertThat(R4ObservationTransformer.interpretationDisplay("AC"))
            .isEqualTo("Anti-complementary substances present");
    assertThat(R4ObservationTransformer.interpretationDisplay("B")).isEqualTo("Better");
    assertThat(R4ObservationTransformer.interpretationDisplay("D"))
        .isEqualTo("Significant change down");
    assertThat(R4ObservationTransformer.interpretationDisplay("DET")).isEqualTo("Detected");
    assertThat(R4ObservationTransformer.interpretationDisplay("E")).isEqualTo("Equivocal");
    assertThat(R4ObservationTransformer.interpretationDisplay("EX")).isEqualTo("outside threshold");
    assertThat(R4ObservationTransformer.interpretationDisplay("EXP")).isEqualTo("Expected");
    assertThat(R4ObservationTransformer.interpretationDisplay("H")).isEqualTo("High");
    assertThat(R4ObservationTransformer.interpretationDisplay("HH")).isEqualTo("Critical high");
    assertThat(R4ObservationTransformer.interpretationDisplay("HU"))
        .isEqualTo("Significantly high");
    assertThat(R4ObservationTransformer.interpretationDisplay("H>"))
            .isEqualTo("Significantly high");
    assertThat(R4ObservationTransformer.interpretationDisplay("HM"))
            .isEqualTo("Hold for Medical Review");
    assertThat(R4ObservationTransformer.interpretationDisplay("HX"))
        .isEqualTo("above high threshold");
    assertThat(R4ObservationTransformer.interpretationDisplay("I")).isEqualTo("Intermediate");
    assertThat(R4ObservationTransformer.interpretationDisplay("IE"))
        .isEqualTo("Insufficient evidence");
    assertThat(R4ObservationTransformer.interpretationDisplay("IND")).isEqualTo("Indeterminate");
    assertThat(R4ObservationTransformer.interpretationDisplay("L")).isEqualTo("Low");
    assertThat(R4ObservationTransformer.interpretationDisplay("LL")).isEqualTo("Critical low");
    assertThat(R4ObservationTransformer.interpretationDisplay("LU")).isEqualTo("Significantly low");
    assertThat(R4ObservationTransformer.interpretationDisplay("L<")).isEqualTo("Significantly low");
    assertThat(R4ObservationTransformer.interpretationDisplay("LX"))
        .isEqualTo("below low threshold");
    assertThat(R4ObservationTransformer.interpretationDisplay("MS"))
        .isEqualTo("moderately susceptible");
    assertThat(R4ObservationTransformer.interpretationDisplay("N")).isEqualTo("Normal");
    assertThat(R4ObservationTransformer.interpretationDisplay("NCL"))
        .isEqualTo("No CLSI defined breakpoint");
    assertThat(R4ObservationTransformer.interpretationDisplay("ND")).isEqualTo("Not detected");
    assertThat(R4ObservationTransformer.interpretationDisplay("NEG")).isEqualTo("Negative");
    assertThat(R4ObservationTransformer.interpretationDisplay("NR")).isEqualTo("Non-reactive");
    assertThat(R4ObservationTransformer.interpretationDisplay("NS")).isEqualTo("Non-susceptible");
    assertThat(R4ObservationTransformer.interpretationDisplay("OBX"))
            .isEqualTo("Interpretation qualifiers in separate OBX segments");
    assertThat(R4ObservationTransformer.interpretationDisplay("POS")).isEqualTo("Positive");
    assertThat(R4ObservationTransformer.interpretationDisplay("QCF")).isEqualTo("Quality control failure");
    assertThat(R4ObservationTransformer.interpretationDisplay("R")).isEqualTo("Resistant");
    assertThat(R4ObservationTransformer.interpretationDisplay("RR")).isEqualTo("Reactive");
    assertThat(R4ObservationTransformer.interpretationDisplay("S")).isEqualTo("Susceptible");
    assertThat(R4ObservationTransformer.interpretationDisplay("SDD"))
        .isEqualTo("Susceptible-dose dependent");
    assertThat(R4ObservationTransformer.interpretationDisplay("SYN-R"))
        .isEqualTo("Synergy - resistant");
    assertThat(R4ObservationTransformer.interpretationDisplay("SYN-S"))
        .isEqualTo("Synergy - susceptible");
    assertThat(R4ObservationTransformer.interpretationDisplay("TOX"))
            .isEqualTo("Cytotoxic substance present");
    assertThat(R4ObservationTransformer.interpretationDisplay("U"))
        .isEqualTo("Significant change up");
    assertThat(R4ObservationTransformer.interpretationDisplay("UNE")).isEqualTo("Unexpected");
    assertThat(R4ObservationTransformer.interpretationDisplay("VS"))
        .isEqualTo("very susceptible");
    assertThat(R4ObservationTransformer.interpretationDisplay("W")).isEqualTo("Worse");
    assertThat(R4ObservationTransformer.interpretationDisplay("WR")).isEqualTo("Weakly reactive");
    assertThat(R4ObservationTransformer.interpretationDisplay("RANDOM")).isNull();
  }

  @Test
  public void toFhir() {
    DatamartObservation datamartObservation = ObservationSamples.Datamart.create().observation();
    assertThat(R4ObservationTransformer.builder().datamart(datamartObservation).build().toFhir())
        .isEqualTo(ObservationSamples.R4.create().observation());
  }

  @Test
  public void transformerIsNullSafe() {
    assertThat(R4ObservationTransformer.category(null)).isNull();
    assertThat(R4ObservationTransformer.codeableConcept(java.util.Optional.empty())).isNull();
    assertThat(
            R4ObservationTransformer.codeableConcept(
                Optional.of(DatamartObservation.CodeableConcept.builder().build())))
        .isNull();
    assertThat(R4ObservationTransformer.component((DatamartObservation.VitalsComponent) null))
        .isNull();
    assertThat(
            R4ObservationTransformer.component(
                DatamartObservation.VitalsComponent.builder().build()))
        .isNull();
    assertThat(R4ObservationTransformer.component((DatamartObservation.AntibioticComponent) null))
        .isNull();
    assertThat(
            R4ObservationTransformer.component(
                DatamartObservation.AntibioticComponent.builder().build()))
        .isNull();
    assertThat(R4ObservationTransformer.component((DatamartObservation.BacteriologyComponent) null))
        .isNull();
    assertThat(
            R4ObservationTransformer.component(
                DatamartObservation.BacteriologyComponent.builder().build()))
        .isNull();
    assertThat(R4ObservationTransformer.interpretation(null)).isNull();
    assertThat(R4ObservationTransformer.performers(Lists.emptyList())).isNull();
    assertThat(
            R4ObservationTransformer.quantity(
                Optional.of(DatamartObservation.Quantity.builder().build())))
        .isNull();
    assertThat(R4ObservationTransformer.referenceRanges(Optional.empty())).isNull();
    assertThat(
            R4ObservationTransformer.referenceRanges(
                Optional.of(DatamartObservation.ReferenceRange.builder().build())))
        .isNull();
    assertThat(R4ObservationTransformer.status(null)).isNull();
  }

  @Test
  public void validTransformerInterpretationTest() {
    List<CodeableConcept> testCodeableConcept =
        List.of(
            CodeableConcept.builder()
                .coding(
                    asList(
                        Coding.builder()
                            .system("http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation")
                            .code("A")
                            .display("Abnormal")
                            .build()))
                .text("A")
                .build());
    assertThat(R4ObservationTransformer.interpretation("A")).isEqualTo(testCodeableConcept);
  }
}
