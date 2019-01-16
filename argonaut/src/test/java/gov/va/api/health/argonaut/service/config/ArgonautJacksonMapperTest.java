package gov.va.api.health.argonaut.service.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import gov.va.api.health.argonaut.api.DataAbsentReason;
import gov.va.api.health.argonaut.api.DataAbsentReason.Reason;
import gov.va.api.health.argonaut.api.elements.Extension;
import gov.va.api.health.argonaut.api.elements.Reference;
import gov.va.api.health.argonaut.api.validation.ExactlyOneOf;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Singular;
import lombok.SneakyThrows;
import org.junit.Test;

public class ArgonautJacksonMapperTest {

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
            .whocares("noone") // kept
            .me(true) // kept
            .ref(reference("AllergyIntolerance/1234")) // kept
            .nope(reference("https://example.com/api/Encounter/1234")) // removed
            .thing(reference(null)) // kept
            .thing(reference("")) // kept
            .thing(reference("http://qualified.is.not/touched")) // kept
            .thing(reference("no/slash")) // kept
            .thing(reference("/cool/a/slash")) // kept
            .thing(reference("Encounter")) // kept
            .thing(reference("Encounter/1234")) // removed
            .thing(reference("https://example.com/api/Encounter/1234")) // removed
            .thing(reference("/Organization")) // kept
            .thing(reference("Organization/1234")) // kept
            .thing(reference("https://example.com/api/Organization/1234")) // kept
            .thing(reference("Practitioner/987"))
            .inner(
                FugaziReferencemajig.builder()
                    .ref(
                        Reference.builder()
                            .reference("Appointment/615f31df-f0c7-5100-ac42-7fb952c630d0")
                            .display(null)
                            .build())
                    .build()) // kept
            .build();

    FugaziReferencemajig expected =
        FugaziReferencemajig.builder()
            .whocares("noone")
            .me(true)
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
        new ArgonautJacksonMapper(
                new MagicReferenceConfig("https://example.com", "api", disableEncounter))
            .objectMapper()
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(input);

    FugaziReferencemajig actual =
        JacksonConfig.createMapper().readValue(qualifiedJson, FugaziReferencemajig.class);

    System.out.println(qualifiedJson);

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  @SneakyThrows
  public void requiredReferencesEmitDar() {
    ReferenceSerializerProperties disableEncounter =
        ReferenceSerializerProperties.builder()
            .encounter(false)
            .build();

    FugaziRequiredReferencemajig input =
        FugaziRequiredReferencemajig.builder()
            .required(reference("https://example.com/api/Encounter/1234")) // emits DAR
            ._required(null)
            .build();

    FugaziRequiredReferencemajig expected =
        FugaziRequiredReferencemajig.builder()
            .required(null)
            ._required(DataAbsentReason.of(Reason.unknown))
            .build();

    String qualifiedJson =
        new ArgonautJacksonMapper(
            new MagicReferenceConfig("https://example.com", "api", disableEncounter))
            .objectMapper()
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(input);

    FugaziRequiredReferencemajig actual =
        JacksonConfig.createMapper().readValue(qualifiedJson, FugaziRequiredReferencemajig.class);

    System.out.println(qualifiedJson);

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
