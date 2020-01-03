package gov.va.api.health.dataquery.service.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.stu3.api.DataAbsentReason;
import gov.va.api.health.stu3.api.elements.Extension;
import gov.va.api.health.stu3.api.elements.Reference;
import gov.va.api.health.validation.api.ExactlyOneOf;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.SneakyThrows;
import org.junit.Test;

public class Stu3JacksonMapperTest {
  @Test
  @SneakyThrows
  public void preExistingDarsArePreserved() {
    ReferenceSerializerProperties disableEncounter =
        ReferenceSerializerProperties.builder().encounter(false).practitioner(true).build();
    FugaziReferenceMajig input =
        FugaziReferenceMajig.builder()
            .ref(reference("https://example.com/api/Practitioner/1234"))
            .nope(null)
            ._nope(DataAbsentReason.of(DataAbsentReason.Reason.error))
            .build();
    FugaziReferenceMajig expected =
        FugaziReferenceMajig.builder()
            .ref(reference("https://example.com/api/Practitioner/1234"))
            .nope(null)
            ._nope(DataAbsentReason.of(DataAbsentReason.Reason.error))
            .build();
    String serializedjson =
        new DataQueryJacksonMapper(
                new MagicReferenceConfig("https://example.com", "dstu2", "stu3", disableEncounter))
            .objectMapper()
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(input);
    FugaziReferenceMajig actual =
        JacksonConfig.createMapper().readValue(serializedjson, FugaziReferenceMajig.class);
    assertThat(actual).isEqualTo(expected);
  }

  private Reference reference(String path) {
    return Reference.builder().display("display-value").reference(path).id("id-value").build();
  }

  @Test
  @SneakyThrows
  public void referencesAreQualified() {
    ReferenceSerializerProperties disableEncounter =
        ReferenceSerializerProperties.builder()
            .appointment(true)
            .encounter(false)
            .location(true)
            .organization(true)
            .practitioner(true)
            .build();

    FugaziReferenceMajig input =
        FugaziReferenceMajig.builder()
            .whocares("noone") // kept
            .me(true) // kept
            .ref(reference("AllergyIntolerance/1234")) // kept
            .nope(reference("https://example.com/api/stu3/Encounter/1234")) // dar
            .alsoNo(reference("https://example.com/api/stu3/Encounter/1234")) // removed
            .thing(reference(null)) // kept
            .thing(reference("")) // kept
            .thing(reference("http://qualified.is.not/touched")) // kept
            .thing(reference("no/slash")) // kept
            .thing(reference("/cool/a/slash")) // kept
            .thing(reference("Encounter")) // kept
            .thing(reference("Encounter/1234")) // removed
            .thing(reference("https://example.com/api/stu3/Encounter/1234")) // removed
            .thing(reference("/Organization")) // kept
            .thing(reference("Organization/1234")) // kept
            .thing(reference("https://example.com/api/stu3/Organization/1234")) // kept
            .thing(reference("Practitioner/987"))
            .inner(
                FugaziReferenceMajig.builder()
                    .ref(
                        Reference.builder()
                            .reference("Appointment/615f31df-f0c7-5100-ac42-7fb952c630d0")
                            .display(null)
                            .build())
                    .build()) // kept
            .build();

    FugaziReferenceMajig expected =
        FugaziReferenceMajig.builder()
            .whocares("noone")
            .me(true)
            ._nope(DataAbsentReason.of(DataAbsentReason.Reason.unsupported))
            .ref(reference("https://example.com/api/stu3/AllergyIntolerance/1234"))
            .thing(reference(null))
            .thing(reference(null))
            .thing(reference("http://qualified.is.not/touched"))
            .thing(reference("https://example.com/api/stu3/no/slash"))
            .thing(reference("https://example.com/api/stu3/cool/a/slash"))
            .thing(reference("https://example.com/api/stu3/Encounter"))
            .thing(reference("https://example.com/api/stu3/Organization"))
            .thing(reference("https://example.com/api/stu3/Organization/1234"))
            .thing(reference("https://example.com/api/stu3/Organization/1234"))
            .thing(reference("https://example.com/api/stu3/Practitioner/987"))
            .inner(
                FugaziReferenceMajig.builder()
                    .ref(
                        Reference.builder()
                            .reference(
                                "https://example.com/api/stu3/Appointment/615f31df-f0c7-5100-ac42-7fb952c630d0")
                            .build())
                    .build())
            .build();

    String qualifiedJson =
        new DataQueryJacksonMapper(
                new MagicReferenceConfig(
                    "https://example.com", "api/dstu2", "api/stu3", disableEncounter))
            .objectMapper()
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(input);

    FugaziReferenceMajig actual =
        JacksonConfig.createMapper().readValue(qualifiedJson, FugaziReferenceMajig.class);

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  @SneakyThrows
  public void requiredReferencesEmitDar() {
    ReferenceSerializerProperties disableEncounter =
        ReferenceSerializerProperties.builder().encounter(false).build();

    FugaziRequiredReferenceMajig input =
        FugaziRequiredReferenceMajig.builder()
            .required(reference("https://example.com/api/dstu2/Encounter/1234")) // emits DAR
            ._required(DataAbsentReason.of(DataAbsentReason.Reason.unknown))
            .build();

    FugaziRequiredReferenceMajig expected =
        FugaziRequiredReferenceMajig.builder()
            .required(null)
            ._required(DataAbsentReason.of(DataAbsentReason.Reason.unknown))
            .build();

    String qualifiedJson =
        new DataQueryJacksonMapper(
                new MagicReferenceConfig(
                    "https://example.com", "api/dstu2", "api/stu3", disableEncounter))
            .objectMapper()
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(input);

    FugaziRequiredReferenceMajig actual =
        JacksonConfig.createMapper().readValue(qualifiedJson, FugaziRequiredReferenceMajig.class);

    assertThat(actual).isEqualTo(expected);
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor
  @JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.ANY,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE
  )
  static final class FugaziReferenceMajig {
    Reference ref;
    Reference nope;
    Extension _nope;
    Reference alsoNo;
    @Singular List<Reference> things;
    FugaziReferenceMajig inner;
    String whocares;
    Boolean me;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @ExactlyOneOf(
    fields = {"required", "_required"},
    message = "Exactly one required field must be specified"
  )
  static final class FugaziRequiredReferenceMajig {
    Reference required;
    Extension _required;
  }
}
