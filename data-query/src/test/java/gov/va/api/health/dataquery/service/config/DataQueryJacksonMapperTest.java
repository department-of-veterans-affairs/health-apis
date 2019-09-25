package gov.va.api.health.dataquery.service.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dstu2.api.DataAbsentReason;
import gov.va.api.health.dstu2.api.DataAbsentReason.Reason;
import gov.va.api.health.dstu2.api.elements.Extension;
import gov.va.api.health.dstu2.api.elements.Reference;
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

public class DataQueryJacksonMapperTest {

  @Test
  @SneakyThrows
  public void preExistingDarsArePreserved() {
    ReferenceSerializerProperties disableEncounter =
        ReferenceSerializerProperties.builder().encounter(false).practitioner(true).build();
    FugaziReferencemajig input =
        FugaziReferencemajig.builder()
            .ref(reference("https://example.com/api/Practitioner/1234"))
            .encounter(null)
            ._encounter(DataAbsentReason.of(Reason.error))
            .build();
    FugaziReferencemajig expected =
        FugaziReferencemajig.builder()
            .ref(reference("https://example.com/api/Practitioner/1234"))
            .encounter(null)
            ._encounter(DataAbsentReason.of(Reason.error))
            .build();
    String serializedjson =
        new DataQueryJacksonMapper(
                new MagicReferenceConfig("https://example.com", "api", disableEncounter))
            .objectMapper()
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(input);
    FugaziReferencemajig actual =
        JacksonConfig.createMapper().readValue(serializedjson, FugaziReferencemajig.class);
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
    FugaziReferencemajig input =
        FugaziReferencemajig.builder()
            .whocares( // kept
                "noone")
            .me( // kept
                true)
            .ref( // kept
                reference("AllergyIntolerance/1234"))
            .nope( // dar
                reference("https://example.com/api/Encounter/1234"))
            .alsoNo( // removed
                reference("https://example.com/api/Encounter/1234"))
            .thing( // kept
                reference(null))
            .thing( // kept
                reference(""))
            .thing( // kept
                reference("http://qualified.is.not/touched"))
            .thing( // kept
                reference("no/slash"))
            .thing( // kept
                reference("/cool/a/slash"))
            .thing( // kept
                reference("Encounter"))
            .thing( // removed
                reference("Encounter/1234"))
            .thing( // removed
                reference("https://example.com/api/Encounter/1234"))
            .thing( // kept
                reference("/Organization"))
            .thing( // kept
                reference("Organization/1234"))
            .thing( // kept
                reference("https://example.com/api/Organization/1234"))
            .thing(reference("Practitioner/987"))
            .inner(
                FugaziReferencemajig.builder()
                    .ref(
                        Reference.builder()
                            .reference("Appointment/615f31df-f0c7-5100-ac42-7fb952c630d0")
                            .display(null)
                            .build())
                    .build())
            .build();
    FugaziReferencemajig expected =
        FugaziReferencemajig.builder()
            .whocares("noone")
            .me(true)
            ._nope(DataAbsentReason.of(Reason.unsupported))
            .ref(reference("https://example.com/api/AllergyIntolerance/1234"))
            .thing(reference(null))
            .thing(reference(null))
            .thing(reference("http://qualified.is.not/touched"))
            .thing(reference("https://example.com/api/no/slash"))
            .thing(reference("https://example.com/api/cool/a/slash"))
            .thing(reference("https://example.com/api/Encounter"))
            .thing(reference("https://example.com/api/Organization"))
            .thing(reference("https://example.com/api/Organization/1234"))
            .thing(reference("https://example.com/api/Organization/1234"))
            .thing(reference("https://example.com/api/Practitioner/987"))
            .inner(
                FugaziReferencemajig.builder()
                    .ref(
                        Reference.builder()
                            .reference(
                                "https://example.com/api/Appointment/615f31df-f0c7-5100-ac42-7fb952c630d0")
                            .build())
                    .build())
            .build();
    String qualifiedJson =
        new DataQueryJacksonMapper(
                new MagicReferenceConfig("https://example.com", "api", disableEncounter))
            .objectMapper()
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(input);
    FugaziReferencemajig actual =
        JacksonConfig.createMapper().readValue(qualifiedJson, FugaziReferencemajig.class);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  @SneakyThrows
  public void requiredReferencesEmitDar() {
    ReferenceSerializerProperties disableEncounter =
        ReferenceSerializerProperties.builder().encounter(false).build();
    FugaziRequiredReferencemajig input =
        FugaziRequiredReferencemajig.builder()
            .required( // emits DAR
                reference("https://example.com/api/Encounter/1234"))
            ._required(DataAbsentReason.of(Reason.unknown))
            .build();
    FugaziRequiredReferencemajig expected =
        FugaziRequiredReferencemajig.builder()
            .required(null)
            ._required(DataAbsentReason.of(Reason.unknown))
            .build();
    String qualifiedJson =
        new DataQueryJacksonMapper(
                new MagicReferenceConfig("https://example.com", "api", disableEncounter))
            .objectMapper()
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(input);
    FugaziRequiredReferencemajig actual =
        JacksonConfig.createMapper().readValue(qualifiedJson, FugaziRequiredReferencemajig.class);
    assertThat(actual).isEqualTo(expected);
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor
  @JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.ANY,
    isGetterVisibility = Visibility.NONE
  )
  public static class FugaziReferencemajig {

    Reference ref;

    Reference nope;

    Extension _nope;

    Reference encounter;

    Extension _encounter;

    Reference alsoNo;

    @Singular List<Reference> things;

    FugaziReferencemajig inner;

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
  public static class FugaziRequiredReferencemajig {

    Reference required;

    Extension _required;
  }
}
