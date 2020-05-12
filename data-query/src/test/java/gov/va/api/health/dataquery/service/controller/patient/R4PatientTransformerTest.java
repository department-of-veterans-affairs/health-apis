package gov.va.api.health.dataquery.service.controller.patient;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.service.controller.patient.PatientSamples.Datamart;
import gov.va.api.health.dataquery.service.controller.patient.PatientSamples.R4;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.uscorer4.api.resources.Patient;
import java.util.List;
import org.junit.Test;

public class R4PatientTransformerTest {
  @Test
  public void contact() {
    Datamart datamart = Datamart.create();
    R4 r4 = R4.create();
    assertThat(R4PatientTransformer.contact(datamart.contact())).isEqualTo(r4.contact());
  }

  @Test
  public void empty() {
    assertThat(
            R4PatientTransformer.builder()
                .datamart(DatamartPatient.builder().build())
                .build()
                .toFhir())
        .isEqualTo(Patient.builder().resourceType("Patient").build());
  }

  @Test
  public void ethnicityDisplay() {
    assertThat(
            R4PatientTransformer.ethnicityDisplay(
                DatamartPatient.Ethnicity.builder().hl7("2135-2").display("hi").build()))
        .isEqualTo("Hispanic or Latino");
    assertThat(
            R4PatientTransformer.ethnicityDisplay(
                DatamartPatient.Ethnicity.builder().hl7("2186-5").display("hi").build()))
        .isEqualTo("Non Hispanic or Latino");
    assertThat(
            R4PatientTransformer.ethnicityDisplay(
                DatamartPatient.Ethnicity.builder().hl7("???").display("hi").build()))
        .isEqualTo("hi");
    assertThat(
            R4PatientTransformer.ethnicityDisplay(
                DatamartPatient.Ethnicity.builder().hl7(null).display(null).build()))
        .isNull();
    assertThat(R4PatientTransformer.ethnicityDisplay(null)).isNull();
  }

  @Test
  public void ethnicityExtensions() {
    assertThat(
            R4PatientTransformer.ethnicityExtensions(
                DatamartPatient.Ethnicity.builder().display("display").hl7("code").build()))
        .isEqualTo(
            List.of(
                Extension.builder()
                    .url("ombCategory")
                    .valueCoding(
                        Coding.builder()
                            .display("display")
                            .system("https://www.hl7.org/fhir/us/core/CodeSystem-cdcrec.html")
                            .code("code")
                            .build())
                    .build(),
                Extension.builder().url("text").valueString("display").build()));
    assertThat(R4PatientTransformer.ethnicityExtensions(null)).isNull();
  }

  @Test
  public void maritalStatus() {
    assertThat(R4PatientTransformer.maritalStatusCoding("A").display()).isEqualTo("Annulled");
    assertThat(R4PatientTransformer.maritalStatusCoding("D").display()).isEqualTo("Divorced");
    assertThat(R4PatientTransformer.maritalStatusCoding("I").display()).isEqualTo("Interlocutory");
    assertThat(R4PatientTransformer.maritalStatusCoding("L").display())
        .isEqualTo("Legally Separated");
    assertThat(R4PatientTransformer.maritalStatusCoding("M").display()).isEqualTo("Married");
    assertThat(R4PatientTransformer.maritalStatusCoding("P").display()).isEqualTo("Polygamous");
    assertThat(R4PatientTransformer.maritalStatusCoding("S").display()).isEqualTo("Never Married");
    assertThat(R4PatientTransformer.maritalStatusCoding("T").display())
        .isEqualTo("Domestic partner");
    assertThat(R4PatientTransformer.maritalStatusCoding("W").display()).isEqualTo("Widowed");
    assertThat(R4PatientTransformer.maritalStatusCoding("UNK").display()).isEqualTo("unknown");
    assertThat(R4PatientTransformer.maritalStatusCoding(null)).isNull();
  }

  @Test
  public void raceCoding() {
    assertThat(
            R4PatientTransformer.raceCoding(
                    DatamartPatient.Race.builder().display("Alaska Native").build())
                .code())
        .isEqualTo("1002-5");
    assertThat(
            R4PatientTransformer.raceCoding(
                    DatamartPatient.Race.builder().display("American Indian").build())
                .code())
        .isEqualTo("1002-5");
    assertThat(
            R4PatientTransformer.raceCoding(DatamartPatient.Race.builder().display("Asian").build())
                .code())
        .isEqualTo("2028-9");
    assertThat(
            R4PatientTransformer.raceCoding(DatamartPatient.Race.builder().display("Black").build())
                .code())
        .isEqualTo("2054-5");
    assertThat(
            R4PatientTransformer.raceCoding(
                    DatamartPatient.Race.builder().display("African American").build())
                .code())
        .isEqualTo("2054-5");
    assertThat(
            R4PatientTransformer.raceCoding(
                    DatamartPatient.Race.builder().display("Native Hawaiian").build())
                .code())
        .isEqualTo("2076-8");
    assertThat(
            R4PatientTransformer.raceCoding(
                    DatamartPatient.Race.builder().display("Pacific Islander").build())
                .code())
        .isEqualTo("2076-8");
    assertThat(
            R4PatientTransformer.raceCoding(DatamartPatient.Race.builder().display("White").build())
                .code())
        .isEqualTo("2106-3");
    assertThat(
            R4PatientTransformer.raceCoding(DatamartPatient.Race.builder().display("???").build())
                .code())
        .isEqualTo("UNK");
    assertThat(
            R4PatientTransformer.raceCoding(DatamartPatient.Race.builder().display(null).build()))
        .isNull();
  }

  @Test
  public void relationshipCoding() {
    assertThat(
            R4PatientTransformer.relationshipCoding(
                    DatamartPatient.Contact.builder().type("Civil Guardian").build())
                .code())
        .isEqualTo("guardian");
    assertThat(
            R4PatientTransformer.relationshipCoding(
                    DatamartPatient.Contact.builder().type("VA Guardian").build())
                .code())
        .isEqualTo("guardian");
    assertThat(
            R4PatientTransformer.relationshipCoding(
                    DatamartPatient.Contact.builder().type("Emergency Contact").build())
                .code())
        .isEqualTo("emergency");
    assertThat(
            R4PatientTransformer.relationshipCoding(
                    DatamartPatient.Contact.builder().type("Secondary Emergency Contact").build())
                .code())
        .isEqualTo("emergency");
    assertThat(
            R4PatientTransformer.relationshipCoding(
                    DatamartPatient.Contact.builder().type("Next of Kin").build())
                .code())
        .isEqualTo("family");
    assertThat(
            R4PatientTransformer.relationshipCoding(
                    DatamartPatient.Contact.builder().type("Secondary Next of Kin").build())
                .code())
        .isEqualTo("family");
    assertThat(
            R4PatientTransformer.relationshipCoding(
                    DatamartPatient.Contact.builder().type("Spouse Employer").build())
                .code())
        .isEqualTo("family");
    assertThat(
            R4PatientTransformer.relationshipCoding(
                DatamartPatient.Contact.builder().type(null).build()))
        .isNull();
  }

  @Test
  public void toFhir() {
    assertThat(
            R4PatientTransformer.builder().datamart(Datamart.create().patient()).build().toFhir())
        .isEqualTo(R4.create().patient());
  }
}
