package gov.va.api.health.argonaut.api.samples;

import static java.util.Collections.singletonList;

import gov.va.api.health.argonaut.api.*;
import gov.va.api.health.argonaut.api.Narrative.NarrativeStatus;
import gov.va.api.health.argonaut.api.bundle.AbstractEntry;
import java.math.BigDecimal;
import lombok.NoArgsConstructor;

@SuppressWarnings("WeakerAccess")
@NoArgsConstructor(staticName = "get")
public final class SampleDataTypes {

  public Coding coding() {
    return Coding.builder()
        .system("http://HelloSystem.com")
        .version("Hello Version")
        .code("Hello Code")
        .display("Hello Display")
        .userSelected(true)
        .build();
  }

  public CodeableConcept details() {
    return CodeableConcept.builder().coding(singletonList(coding())).text("HelloText").build();
  }

  public Extension extension() {
    return Extension.builder().url("http://HelloUrl.com").valueInteger(1).build();
  }

  public Extension extensionWithQuantity() {
    return Extension.builder()
        .url("http://HelloUrl.com")
        .valueQuantity(
            Quantity.builder()
                .code("Q")
                .comparator(">=")
                .id("Q1")
                .unit("things")
                .value(1.0)
                .build())
        .build();
  }

  public Extension extensionWithRatio() {
    return Extension.builder()
        .url("http://HelloUrl.com")
        .valueRatio(
            Ratio.builder()
                .id("R1")
                .denominator(Quantity.builder().value(1.0).build())
                .numerator(Quantity.builder().value(2.0).build())
                .build())
        .build();
  }

  public Issue issue() {
    return Issue.builder()
        .severity(Issue.IssueSeverity.error)
        .code("HelloCode")
        .details(details())
        .diagnostics("HelloDiagnostics")
        .location(singletonList("HelloLocation"))
        .expression(singletonList("HelloExpression"))
        .build();
  }

  public Meta meta() {
    return Meta.builder()
        .versionId("1111")
        .lastUpdated("2000-01-01T00:00:00-00:00")
        .profile(singletonList("http://HelloProfile.com"))
        .security(singletonList(coding()))
        .tag(singletonList(coding()))
        .build();
  }

  public Narrative narrative() {
    return Narrative.builder().status(NarrativeStatus.additional).div("<p>HelloDiv<p>").build();
  }

  public Quantity quantity() {
    return Quantity.builder().value(11.11).unit("HelloUnit").build();
  }

  public Range range() {
    return Range.builder().low(simpleQuantity()).high(simpleQuantity()).build();
  }

  public Ratio ratio() {
    return Ratio.builder().numerator(quantity()).denominator(quantity()).build();
  }

  public Reference reference() {
    return Reference.builder().reference("HelloReference").display("HelloDisplay").build();
  }

  public SimpleResource resource() {
    return SimpleResource.builder()
        .id("1111")
        .meta(meta())
        .implicitRules("http://HelloRules.com")
        .language("Hello Language")
        .build();
  }

  public SimpleQuantity simpleQuantity() {
    return SimpleQuantity.builder().value(11.11).unit("HelloUnit").build();
  }

  public AbstractEntry.Response response() {
    return AbstractEntry.Response.builder()
        .id("request1")
        .extension(singletonList(extension()))
        .modifierExtension(singletonList(extension()))
        .status("single")
        .location("http://example.com")
        .etag("you're it")
        .lastModified("2005-01-21T07:57:00Z")
        .build();
  }

  public AbstractEntry.Request request() {
    return AbstractEntry.Request.builder()
        .id("request1")
        .extension(singletonList(extension()))
        .modifierExtension(singletonList(extension()))
        .method(AbstractEntry.HttpVerb.GET)
        .url("http://example.com")
        .ifNoneMatch("ok")
        .ifModifiedSince("also ok")
        .ifMatch("really ok")
        .ifNoneExist("meh, ok.")
        .build();
  }

  public AbstractEntry.Search search() {
    return AbstractEntry.Search.builder()
        .id("s1")
        .mode(AbstractEntry.SearchMode.match)
        .extension(singletonList(extension()))
        .modifierExtension(singletonList(extension()))
        .rank(new BigDecimal(0.5))
        .build();
  }
}
