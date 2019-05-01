package gov.va.api.health.dataquery.api.datatypes;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.api.samples.SampleDataTypes;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import org.junit.Test;

public class SignatureTest {

  @Test
  public void signatureWithOnlyWhoReferenceIsValid() {
    Signature psuedoSignature = signatureWithOnlyWhoReference();

    Set<ConstraintViolation<Signature>> problems = Validation
        .buildDefaultValidatorFactory()
        .getValidator()
        .validate(psuedoSignature);

    assertThat(problems.size()).isEqualTo(0);
  }

  @Test
  public void signatureWithOnlyWhoUriIsValid() {
    Signature psuedoSignature = signatureWithOnlyWhoUri();

    Set<ConstraintViolation<Signature>> problems = Validation
        .buildDefaultValidatorFactory()
        .getValidator()
        .validate(psuedoSignature);

    assertThat(problems.size()).isEqualTo(0);
  }

  @Test
  public void signatureWithBothWhoUriAndWhoReferenceIsInvalid() {
    Signature psuedoSignature = signatureWithBothWhoUriAndWhoReference();

    Set<ConstraintViolation<Signature>> problems = Validation
        .buildDefaultValidatorFactory()
        .getValidator()
        .validate(psuedoSignature);

    assertThat(problems.size()).isEqualTo(1);
  }

  @Test
  public void signatureMissingBothWhoUriAndWhoReferenceIsInvalid() {
    Signature psuedoSignature = signatureMissingBothWhoUriAndWhoReference();

    Set<ConstraintViolation<Signature>> problems = Validation
        .buildDefaultValidatorFactory()
        .getValidator()
        .validate(psuedoSignature);

    assertThat(problems.size()).isEqualTo(1);
  }

  private Signature signatureWithOnlyWhoReference() {
    SampleDataTypes data = SampleDataTypes.get();
    return Signature.builder()
        .id("1234")
        .type(data.codingList())
        .when("2000-01-01T00:00:00-00:00")
        .whoReference(data.reference())
        .contentType("contentTypeTest")
        .blob("aGVsbG8=")
        .build();
  }

  private Signature signatureWithOnlyWhoUri() {
    SampleDataTypes data = SampleDataTypes.get();
    return Signature.builder()
        .id("1234")
        .type(data.codingList())
        .when("2000-01-01T00:00:00-00:00")
        .whoUri("123456")
        .contentType("contentTypeTest")
        .blob("aGVsbG8=")
        .build();
  }

  private Signature signatureWithBothWhoUriAndWhoReference() {
    SampleDataTypes data = SampleDataTypes.get();
    return Signature.builder()
        .id("1234")
        .type(data.codingList())
        .when("2000-01-01T00:00:00-00:00")
        .whoUri("123456")
        .whoReference(data.reference())
        .contentType("contentTypeTest")
        .blob("aGVsbG8=")
        .build();
  }

  private Signature signatureMissingBothWhoUriAndWhoReference() {
    SampleDataTypes data = SampleDataTypes.get();
    return Signature.builder()
        .id("1234")
        .type(data.codingList())
        .when("2000-01-01T00:00:00-00:00")
        .contentType("contentTypeTest")
        .blob("aGVsbG8=")
        .build();
  }
}