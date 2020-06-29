package gov.va.api.health.dataquery.tests.crawler;

import static gov.va.api.health.dataquery.tests.EnvironmentAssumptions.assumeLocal;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dstu2.api.elements.Reference;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ReferenceInterceptorTest {

  private Reference reference(String path) {
    return Reference.builder().display("display-value").reference(path).id("id-value").build();
  }

  @Test
  @SneakyThrows
  public void referencesFound() {
    FugaziReferencemajig before =
        FugaziReferencemajig.builder()
            .whocares("noone")
            .me(true)
            .ref(reference("https://example.com/api/AllergyIntolerance/1234"))
            .thing(reference(null))
            .thing(reference("https://example.com/api/Organization?patient=123"))
            .thing(reference("https://example.com/api/Organization/1234"))
            .thing(reference("https://example.com/api/Organization/1234"))
            .inner(
                FugaziReferencemajig.builder()
                    .ref(
                        Reference.builder()
                            .reference(
                                "https://example.com/api/Location/615f31df-f0c7-5100-ac42-7fb952c630d0")
                            .build())
                    .build())
            .build();
    ReferenceInterceptor interceptor = new ReferenceInterceptor();
    FugaziReferencemajig after =
        interceptor
            .mapper()
            .readValue(
                JacksonConfig.createMapper().writeValueAsString(before),
                FugaziReferencemajig.class);

    assertThat(after).isEqualTo(before);
    assertThat(interceptor.references())
        .containsExactlyInAnyOrder(
            "https://example.com/api/AllergyIntolerance/1234",
            "https://example.com/api/Organization?patient=123",
            "https://example.com/api/Organization/1234",
            "https://example.com/api/Location/615f31df-f0c7-5100-ac42-7fb952c630d0");
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor
  @JsonAutoDetect(
      fieldVisibility = JsonAutoDetect.Visibility.ANY,
      isGetterVisibility = Visibility.NONE)
  public static class FugaziReferencemajig {
    Reference ref;
    Reference nope;
    @Singular List<Reference> things;
    FugaziReferencemajig inner;
    String whocares;
    Boolean me;
  }
}
