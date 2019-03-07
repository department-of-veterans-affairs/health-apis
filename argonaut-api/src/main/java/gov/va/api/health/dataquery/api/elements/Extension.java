package gov.va.api.health.dataquery.api.elements;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import gov.va.api.health.dataquery.api.Fhir;
import gov.va.api.health.dataquery.api.datatypes.Address;
import gov.va.api.health.dataquery.api.datatypes.Attachment;
import gov.va.api.health.dataquery.api.datatypes.CodeableConcept;
import gov.va.api.health.dataquery.api.datatypes.Coding;
import gov.va.api.health.dataquery.api.datatypes.ContactPoint;
import gov.va.api.health.dataquery.api.datatypes.HumanName;
import gov.va.api.health.dataquery.api.datatypes.Identifier;
import gov.va.api.health.dataquery.api.datatypes.Period;
import gov.va.api.health.dataquery.api.datatypes.Quantity;
import gov.va.api.health.dataquery.api.datatypes.Range;
import gov.va.api.health.dataquery.api.datatypes.Ratio;
import gov.va.api.health.dataquery.api.validation.ZeroOrOneOf;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Schema(description = "http://hl7.org/fhir/DSTU2/extensibility.html#extension")
@ZeroOrOneOf(
  fields = {
    "valueInteger",
    "valueDecimal",
    "valueDateTime",
    "valueDate",
    "valueDateTime",
    "valueDate",
    "valueInstant",
    "valueString",
    "valueUri",
    "valueBoolean",
    "valueCode",
    "valueBase64Binary",
    "valueCoding",
    "valueCodeableConcept",
    "valueIdentifier",
    "valueQuantity",
    "valueAttachment",
    "valueRange",
    "valuePeriod",
    "valueRatio",
    "valueHumanName",
    "valueAddress",
    "valueContactPoint",
    "valueReference"
  },
  message = "Only one value type may be specified"
)
public class Extension implements Element {

  @Pattern(regexp = Fhir.ID)
  String id;

  @Pattern(regexp = Fhir.URI)
  String url;

  @Valid List<Extension> extension;

  // TODO https://vasdvp.atlassian.net/browse/API-134
  Integer valueInteger;
  Double valueDecimal;

  @Pattern(regexp = Fhir.DATETIME)
  String valueDateTime;

  @Pattern(regexp = Fhir.DATE)
  String valueDate;

  @Pattern(regexp = Fhir.INSTANT)
  String valueInstant;

  String valueString;

  @Pattern(regexp = Fhir.URI)
  String valueUri;

  Boolean valueBoolean;

  @Pattern(regexp = Fhir.CODE)
  String valueCode;

  @Pattern(regexp = Fhir.BASE64)
  String valueBase64Binary;

  @Valid Coding valueCoding;
  @Valid CodeableConcept valueCodeableConcept;
  @Valid Identifier valueIdentifier;
  @Valid Quantity valueQuantity;
  @Valid Attachment valueAttachment;
  @Valid Range valueRange;
  @Valid Period valuePeriod;
  @Valid Ratio valueRatio;
  @Valid HumanName valueHumanName;
  @Valid Address valueAddress;
  @Valid ContactPoint valueContactPoint;
  @Valid Reference valueReference;
}
