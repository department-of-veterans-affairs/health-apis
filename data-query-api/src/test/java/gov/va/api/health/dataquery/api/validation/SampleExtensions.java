package gov.va.api.health.dataquery.api.validation;

import gov.va.api.health.dataquery.api.datatypes.CodeableConcept;
import gov.va.api.health.dataquery.api.datatypes.Coding;
import gov.va.api.health.dataquery.api.datatypes.HumanName;
import gov.va.api.health.dataquery.api.datatypes.Identifier;
import gov.va.api.health.dataquery.api.elements.Extension;
import gov.va.api.health.dataquery.api.elements.Reference;
import gov.va.api.health.dataquery.api.resources.Patient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import lombok.NoArgsConstructor;

@SuppressWarnings("WeakerAccess")
@NoArgsConstructor(staticName = "get")
class SampleExtensions {

  List<Identifier> identifier() {
    List<Identifier> identifiers = new LinkedList<>();
    identifiers.add(
        Identifier.builder()
            .use(Identifier.IdentifierUse.usual)
            .type(
                CodeableConcept.builder()
                    .coding(
                        Collections.singletonList(
                            Coding.builder().system("http://test-code").code("C0D3").build()))
                    .build())
            .system("http://test-system")
            .value("123456789")
            .assigner(Reference.builder().display("tester-test-index").build())
            .build());

    return identifiers;
  }

  List<Extension> multipleOptionalEthnicityExtension() {
    List<Extension> extensions = new ArrayList<>(3);

    List<Extension> ethnicityExtensions = new LinkedList<>();
    ethnicityExtensions.add(
        Extension.builder()
            .url("ombCategory")
            .valueCoding(
                Coding.builder()
                    .system("http://hl7.org/fhir/ValueSet/v3-Ethnicity")
                    .code("2135-2")
                    .display("Hispanic or Latino")
                    .build())
            .build());
    ethnicityExtensions.add(
        Extension.builder()
            .url("detailed")
            .valueCoding(
                Coding.builder()
                    .system("http://hl7.org/fhir/v3/Ethnicity")
                    .code("2137-8")
                    .display("Spaniard")
                    .build())
            .build());
    ethnicityExtensions.add(
        Extension.builder()
            .url("detailed")
            .valueCoding(
                Coding.builder()
                    .system("http://hl7.org/fhir/v3/Ethnicity")
                    .code("2138-6")
                    .display("Andalusian")
                    .build())
            .build());
    ethnicityExtensions.add(
        Extension.builder()
            .url("detailed")
            .valueCoding(
                Coding.builder()
                    .system("http://hl7.org/fhir/v3/Ethnicity")
                    .code("2139-4")
                    .display("Asturian")
                    .build())
            .build());
    ethnicityExtensions.add(Extension.builder().url("text").valueString("Spaniard").build());

    extensions.add(
        Extension.builder()
            .url("http://fhir.org/guides/argonaut/StructureDefinition/argo-ethnicity")
            .extension(ethnicityExtensions)
            .build());
    return extensions;
  }

  List<Extension> multipleOptionalRaceExtension() {
    List<Extension> extensions = new ArrayList<>(3);

    List<Extension> raceExtensions = new LinkedList<>();
    raceExtensions.add(
        Extension.builder()
            .url("ombCategory")
            .valueCoding(
                Coding.builder()
                    .system("http://hl7.org/fhir/v3/Race")
                    .code("1002-5")
                    .display("American Indian or Alaska Native")
                    .build())
            .build());
    raceExtensions.add(
        Extension.builder()
            .url("ombCategory")
            .valueCoding(
                Coding.builder()
                    .system("http://hl7.org/fhir/v3/Race")
                    .code("2028-9")
                    .display("Asian")
                    .build())
            .build());
    raceExtensions.add(
        Extension.builder()
            .url("ombCategory")
            .valueCoding(
                Coding.builder()
                    .system("http://hl7.org/fhir/v3/Race")
                    .code("2054-5")
                    .display("Black or African American")
                    .build())
            .build());
    raceExtensions.add(
        Extension.builder()
            .url("detailed")
            .valueCoding(
                Coding.builder()
                    .system("http://hl7.org/fhir/v3/Race")
                    .code("1004-1")
                    .display("American Indian")
                    .build())
            .build());
    raceExtensions.add(
        Extension.builder()
            .url("detailed")
            .valueCoding(
                Coding.builder()
                    .system("http://hl7.org/fhir/v3/Race")
                    .code("1006-6")
                    .display("Abenaki")
                    .build())
            .build());
    raceExtensions.add(
        Extension.builder()
            .url("detailed")
            .valueCoding(
                Coding.builder()
                    .system("http://hl7.org/fhir/v3/Race")
                    .code("1008-2")
                    .display("Algonquian")
                    .build())
            .build());
    raceExtensions.add(Extension.builder().url("text").valueString("testa").build());

    extensions.add(
        Extension.builder()
            .url("http://fhir.org/guides/argonaut/StructureDefinition/argo-race")
            .extension(raceExtensions)
            .build());
    return extensions;
  }

  List<HumanName> name() {
    return Collections.singletonList(
        HumanName.builder()
            .use(HumanName.NameUse.usual)
            .text("FOOMAN FOO")
            .family(Collections.singletonList("FOO"))
            .given(Collections.singletonList("FOOMAN"))
            .build());
  }

  List<Extension> noRequiredEthnicityExtension() {
    List<Extension> extensions = new ArrayList<>(3);

    List<Extension> ethnicityExtensions = new LinkedList<>();
    extensions.add(
        Extension.builder()
            .url("http://fhir.org/guides/argonaut/StructureDefinition/argo-ethnicity")
            .extension(ethnicityExtensions)
            .build());
    return extensions;
  }

  List<Extension> noRequiredRaceExtension() {
    List<Extension> extensions = new ArrayList<>(3);

    List<Extension> raceExtensions = new LinkedList<>();
    extensions.add(
        Extension.builder()
            .url("http://fhir.org/guides/argonaut/StructureDefinition/argo-race")
            .extension(raceExtensions)
            .build());
    return extensions;
  }

  List<Extension> nullEthnicityExtension() {
    List<Extension> extensions = new ArrayList<>(3);

    List<Extension> raceExtensions = new LinkedList<>();
    raceExtensions.add(
        Extension.builder()
            .url("ombCategory")
            .valueCoding(
                Coding.builder()
                    .system("http://hl7.org/fhir/ValueSet/v3-Ethnicity")
                    .code("2135-2")
                    .display("Hispanic or Latino")
                    .build())
            .build());
    raceExtensions.add(Extension.builder().url("text").valueString("tester").build());
    extensions.add(Extension.builder().url("http://test-race").extension(raceExtensions).build());
    extensions.add(Extension.builder().url("http://test-birthsex").valueCode("M").build());
    return extensions;
  }

  List<Extension> nullRaceExtension() {
    List<Extension> extensions = new ArrayList<>(3);

    List<Extension> ethnicityExtensions = new LinkedList<>();
    ethnicityExtensions.add(
        Extension.builder()
            .url("ombCategory")
            .valueCoding(
                Coding.builder()
                    .system("http://hl7.org/fhir/ValueSet/v3-Ethnicity")
                    .code("2135-2")
                    .display("Hispanic or Latino")
                    .build())
            .build());
    ethnicityExtensions.add(Extension.builder().url("text").valueString("Spaniard").build());
    extensions.add(
        Extension.builder()
            .url("http://fhir.org/guides/argonaut/StructureDefinition/argo-ethnicity")
            .extension(ethnicityExtensions)
            .build());
    extensions.add(Extension.builder().url("http://test-birthsex").valueCode("M").build());
    return extensions;
  }

  private Patient patientWith(List<Extension> extension) {
    return Patient.builder()
        .resourceType("Patient")
        .extension(extension)
        .identifier(identifier())
        .gender(Patient.Gender.unknown)
        .name(name())
        .build();
  }

  Patient patientWithMultipleOptionalEthnicityExtension() {
    return patientWith(multipleOptionalEthnicityExtension());
  }

  Patient patientWithMultipleOptionalRaceExtension() {
    return patientWith(multipleOptionalRaceExtension());
  }

  Patient patientWithNoRequiredEthnicityExtension() {
    return patientWith(noRequiredEthnicityExtension());
  }

  Patient patientWithNoRequiredRaceExtension() {
    return patientWith(noRequiredRaceExtension());
  }

  Patient patientWithNullEthnicityExtension() {
    return patientWith(nullEthnicityExtension());
  }

  Patient patientWithNullRaceExtension() {
    return patientWith(nullRaceExtension());
  }

  Patient patientWithSingleOptionalEthnicityExtension() {
    return patientWith(singleOptionalEthnicityExtension());
  }

  Patient patientWithSingleOptionalRaceExtension() {
    return patientWith(singleOptionalRaceExtension());
  }

  Patient patientWithSingleRequiredEthnicityExtension() {
    return patientWith(singleRequiredEthnicityExtension());
  }

  Patient patientWithSingleRequiredRaceExtension() {
    return patientWith(singleRequiredRaceExtension());
  }

  Patient patientWithTooManyOptionalEthnicityExtension() {
    return patientWith(tooManyOptionalEthnicityExtension());
  }

  Patient patientWithTooManyOptionalRaceExtension() {
    return patientWith(tooManyOptionalRaceExtension());
  }

  Patient patientWithTooManyRequiredEthnicityExtension() {
    return patientWith(tooManyRequiredEthnicityExtension());
  }

  Patient patientWithTooManyRequiredRaceExtension() {
    return patientWith(tooManyRequiredRaceExtension());
  }

  List<Extension> singleOptionalEthnicityExtension() {
    List<Extension> extensions = new ArrayList<>(3);

    List<Extension> ethnicityExtensions = new LinkedList<>();
    ethnicityExtensions.add(
        Extension.builder()
            .url("ombCategory")
            .valueCoding(
                Coding.builder()
                    .system("http://hl7.org/fhir/ValueSet/v3-Ethnicity")
                    .code("2135-2")
                    .display("Hispanic or Latino")
                    .build())
            .build());
    ethnicityExtensions.add(
        Extension.builder()
            .url("detailed")
            .valueCoding(
                Coding.builder()
                    .system("http://hl7.org/fhir/v3/Ethnicity")
                    .code("2137-8")
                    .display("Spaniard")
                    .build())
            .build());
    ethnicityExtensions.add(Extension.builder().url("text").valueString("testa").build());

    extensions.add(
        Extension.builder()
            .url("http://fhir.org/guides/argonaut/StructureDefinition/argo-ethnicity")
            .extension(ethnicityExtensions)
            .build());
    return extensions;
  }

  List<Extension> singleOptionalRaceExtension() {
    List<Extension> extensions = new ArrayList<>(3);

    List<Extension> raceExtensions = new LinkedList<>();
    raceExtensions.add(
        Extension.builder()
            .url("ombCategory")
            .valueCoding(
                Coding.builder()
                    .system("http://hl7.org/fhir/v3/Race")
                    .code("1002-5")
                    .display("American Indian or Alaska Native")
                    .build())
            .build());
    raceExtensions.add(
        Extension.builder()
            .url("detailed")
            .valueCoding(
                Coding.builder()
                    .system("http://hl7.org/fhir/v3/Race")
                    .code("1004-1")
                    .display("American Indian")
                    .build())
            .build());
    raceExtensions.add(Extension.builder().url("text").valueString("testa").build());

    extensions.add(
        Extension.builder()
            .url("http://fhir.org/guides/argonaut/StructureDefinition/argo-race")
            .extension(raceExtensions)
            .build());
    return extensions;
  }

  List<Extension> singleRequiredEthnicityExtension() {
    List<Extension> extensions = new ArrayList<>(3);

    List<Extension> raceExtensions = new LinkedList<>();
    raceExtensions.add(
        Extension.builder()
            .url("ombCategory")
            .valueCoding(
                Coding.builder()
                    .system("http://hl7.org/fhir/ValueSet/v3-Ethnicity")
                    .code("2135-2")
                    .display("Hispanic or Latino")
                    .build())
            .build());
    raceExtensions.add(Extension.builder().url("text").valueString("tester").build());

    List<Extension> ethnicityExtensions = new LinkedList<>();
    ethnicityExtensions.add(Extension.builder().url("text").valueString("Spaniard").build());

    extensions.add(Extension.builder().url("http://test-race").extension(raceExtensions).build());
    extensions.add(
        Extension.builder()
            .url("http://fhir.org/guides/argonaut/StructureDefinition/argo-ethnicity")
            .extension(ethnicityExtensions)
            .build());
    extensions.add(Extension.builder().url("http://test-birthsex").valueCode("M").build());
    return extensions;
  }

  List<Extension> singleRequiredRaceExtension() {
    List<Extension> extensions = new ArrayList<>(3);

    List<Extension> raceExtensions = new LinkedList<>();
    raceExtensions.add(
        Extension.builder().url("text").valueString("American Indian or Alaska Native").build());

    List<Extension> ethnicityExtensions = new LinkedList<>();
    ethnicityExtensions.add(Extension.builder().url("text").valueString("Spaniard").build());

    extensions.add(
        Extension.builder()
            .url("http://fhir.org/guides/argonaut/StructureDefinition/argo-race")
            .extension(raceExtensions)
            .build());
    extensions.add(
        Extension.builder()
            .url("http://fhir.org/guides/argonaut/StructureDefinition/argo-ethnicity")
            .extension(ethnicityExtensions)
            .build());
    extensions.add(Extension.builder().url("http://test-birthsex").valueCode("M").build());
    return extensions;
  }

  List<Extension> tooManyOptionalEthnicityExtension() {
    List<Extension> extensions = new ArrayList<>(3);
    List<Extension> ethnicityExtensions = new LinkedList<>();

    ethnicityExtensions.add(
        Extension.builder()
            .url("ombCategory")
            .valueCoding(
                Coding.builder()
                    .system("http://hl7.org/fhir/ValueSet/v3-Ethnicity")
                    .code("2135-2")
                    .display("Hispanic or Latino")
                    .build())
            .build());

    ethnicityExtensions.add(
        Extension.builder()
            .url("ombCategory")
            .valueCoding(
                Coding.builder()
                    .system("http://hl7.org/fhir/ValueSet/v3-Ethnicity")
                    .code("2135-2")
                    .display("Hispanic or Latino")
                    .build())
            .build());

    ethnicityExtensions.add(Extension.builder().url("text").valueString("Spaniard").build());

    extensions.add(
        Extension.builder()
            .url("http://fhir.org/guides/argonaut/StructureDefinition/argo-ethnicity")
            .extension(ethnicityExtensions)
            .build());

    return extensions;
  }

  List<Extension> tooManyOptionalRaceExtension() {
    List<Extension> extensions = new ArrayList<>(3);
    List<Extension> raceExtensions = new LinkedList<>();
    raceExtensions.add(
        Extension.builder()
            .url("ombCategory")
            .valueCoding(
                Coding.builder()
                    .system("http://hl7.org/fhir/v3/Race")
                    .code("1002-5")
                    .display("American Indian or Alaska Native")
                    .build())
            .build());
    raceExtensions.add(
        Extension.builder()
            .url("ombCategory")
            .valueCoding(
                Coding.builder()
                    .system("http://hl7.org/fhir/v3/Race")
                    .code("2028-9")
                    .display("Asian")
                    .build())
            .build());
    raceExtensions.add(
        Extension.builder()
            .url("ombCategory")
            .valueCoding(
                Coding.builder()
                    .system("http://hl7.org/fhir/v3/Race")
                    .code("2054-5")
                    .display("Black or African American")
                    .build())
            .build());
    raceExtensions.add(
        Extension.builder()
            .url("ombCategory")
            .valueCoding(
                Coding.builder()
                    .system("http://hl7.org/fhir/v3/Race")
                    .code("2076-8")
                    .display("Native Hawaiian or Other Pacific Islander")
                    .build())
            .build());
    raceExtensions.add(
        Extension.builder()
            .url("ombCategory")
            .valueCoding(
                Coding.builder()
                    .system("http://hl7.org/fhir/v3/Race")
                    .code("2106-3")
                    .display("White")
                    .build())
            .build());
    raceExtensions.add(
        Extension.builder()
            .url("ombCategory")
            .valueCoding(
                Coding.builder()
                    .system("http://hl7.org/fhir/v3/NullFlavor")
                    .code("UNK")
                    .display("Unknown")
                    .build())
            .build());

    raceExtensions.add(Extension.builder().url("text").valueString("American Indian").build());

    extensions.add(
        Extension.builder()
            .url("http://fhir.org/guides/argonaut/StructureDefinition/argo-race")
            .extension(raceExtensions)
            .build());

    return extensions;
  }

  List<Extension> tooManyRequiredEthnicityExtension() {
    List<Extension> extensions = new ArrayList<>(3);

    List<Extension> ethnicityExtensions = new LinkedList<>();
    ethnicityExtensions.add(Extension.builder().url("text").valueString("Spaniard").build());
    ethnicityExtensions.add(Extension.builder().url("text").valueString("Andalusian").build());

    extensions.add(
        Extension.builder()
            .url("http://fhir.org/guides/argonaut/StructureDefinition/argo-ethnicity")
            .extension(ethnicityExtensions)
            .build());
    return extensions;
  }

  List<Extension> tooManyRequiredRaceExtension() {
    List<Extension> extensions = new ArrayList<>(3);

    List<Extension> raceExtensions = new LinkedList<>();
    raceExtensions.add(Extension.builder().url("text").valueString("American Indian").build());
    raceExtensions.add(Extension.builder().url("text").valueString("Abenaki").build());

    extensions.add(
        Extension.builder()
            .url("http://fhir.org/guides/argonaut/StructureDefinition/argo-ethnicity")
            .extension(raceExtensions)
            .build());
    return extensions;
  }
}
