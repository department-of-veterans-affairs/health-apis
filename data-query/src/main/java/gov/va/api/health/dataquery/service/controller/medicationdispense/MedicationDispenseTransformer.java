package gov.va.api.health.dataquery.service.controller.medicationdispense;

import static gov.va.api.health.dataquery.service.controller.Transformers.allBlank;
import static gov.va.api.health.dataquery.service.controller.Transformers.asDateTimeString;
import static gov.va.api.health.dataquery.service.controller.Transformers.convert;
import static gov.va.api.health.dataquery.service.controller.Transformers.convertAll;
import static gov.va.api.health.dataquery.service.controller.Transformers.ifPresent;
import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.dataquery.api.datatypes.CodeableConcept;
import gov.va.api.health.dataquery.api.datatypes.Coding;
import gov.va.api.health.dataquery.api.datatypes.Identifier;
import gov.va.api.health.dataquery.api.datatypes.SimpleQuantity;
import gov.va.api.health.dataquery.api.datatypes.Timing;
import gov.va.api.health.dataquery.api.elements.Reference;
import gov.va.api.health.dataquery.api.resources.MedicationDispense;
import gov.va.api.health.dataquery.api.resources.MedicationDispense.DosageInstruction;
import gov.va.api.health.dataquery.api.resources.MedicationDispense.Status;
import gov.va.api.health.dataquery.service.controller.EnumSearcher;
import gov.va.dvp.cdw.xsd.model.CdwCodeableConcept;
import gov.va.dvp.cdw.xsd.model.CdwCoding;
import gov.va.dvp.cdw.xsd.model.CdwIdentifierUseCodes;
import gov.va.dvp.cdw.xsd.model.CdwMedicationDispense100Root.CdwMedicationDispenses.CdwMedicationDispense;
import gov.va.dvp.cdw.xsd.model.CdwMedicationDispense100Root.CdwMedicationDispenses.CdwMedicationDispense.CdwAuthorizingPrescriptions;
import gov.va.dvp.cdw.xsd.model.CdwMedicationDispense100Root.CdwMedicationDispenses.CdwMedicationDispense.CdwDosageInstructions;
import gov.va.dvp.cdw.xsd.model.CdwMedicationDispense100Root.CdwMedicationDispenses.CdwMedicationDispense.CdwDosageInstructions.CdwDosageInstruction;
import gov.va.dvp.cdw.xsd.model.CdwMedicationDispense100Root.CdwMedicationDispenses.CdwMedicationDispense.CdwDosageInstructions.CdwDosageInstruction.CdwRoute;
import gov.va.dvp.cdw.xsd.model.CdwMedicationDispense100Root.CdwMedicationDispenses.CdwMedicationDispense.CdwDosageInstructions.CdwDosageInstruction.CdwTiming;
import gov.va.dvp.cdw.xsd.model.CdwMedicationDispense100Root.CdwMedicationDispenses.CdwMedicationDispense.CdwIdentifier;
import gov.va.dvp.cdw.xsd.model.CdwMedicationDispenseStatus;
import gov.va.dvp.cdw.xsd.model.CdwMedicationDispenseType;
import gov.va.dvp.cdw.xsd.model.CdwMedicationDispenseTypeCoding;
import gov.va.dvp.cdw.xsd.model.CdwReference;
import gov.va.dvp.cdw.xsd.model.CdwSimpleQuantity;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MedicationDispenseTransformer implements MedicationDispenseController.Transformer {
  @Override
  public MedicationDispense apply(CdwMedicationDispense cdw) {
    return MedicationDispense.builder()
        .resourceType("MedicationDispense")
        .id(cdw.getCdwId())
        .identifier(identifier(cdw.getIdentifier()))
        .authorizingPrescription(authorizingPrescriptions(cdw.getAuthorizingPrescriptions()))
        .status(status(cdw.getStatus()))
        .patient(reference(cdw.getPatient()))
        .dispenser(reference(cdw.getDispenser()))
        .type(typeCodeableConcept(cdw.getType()))
        .quantity(simpleQuantity(cdw.getQuantity()))
        .daysSupply(simpleQuantity(cdw.getDaysSupply()))
        .medicationReference(reference(cdw.getMedicationReference()))
        .whenPrepared(asDateTimeString(cdw.getWhenPrepared()))
        .whenHandedOver(asDateTimeString(cdw.getWhenHandedOver()))
        .note(cdw.getNote())
        .dosageInstruction(dosageInstructions(cdw.getDosageInstructions()))
        .build();
  }

  /**
   * This takes the first element if it exists of Authorizing Prescriptions because it's a 2d array,
   * which is a funky mapping for this field.
   */
  List<Reference> authorizingPrescriptions(List<CdwAuthorizingPrescriptions> maybeCdw) {
    if (maybeCdw == null || maybeCdw.isEmpty()) {
      return null;
    }
    CdwAuthorizingPrescriptions firstList = maybeCdw.get(0);
    if (firstList == null) {
      return null;
    }
    return convertAll(firstList.getAuthorizingPrescription(), this::reference);
  }

  /** Generic codeable concept transformer for when cdw isn't returning a one off type. */
  CodeableConcept codeableConcept(CdwCodeableConcept source) {
    if (source == null || (source.getCoding().isEmpty() && isBlank(source.getText()))) {
      return null;
    }
    return CodeableConcept.builder()
        .coding(codings(source.getCoding()))
        .text(source.getText())
        .build();
  }

  /* Is there a nice way to check if all the fields are blank?*/
  Coding coding(CdwCoding cdw) {
    if (cdw == null || allBlank(cdw.getCode(), cdw.getDisplay(), cdw.getSystem())) {
      return null;
    }
    return Coding.builder()
        .system(cdw.getSystem())
        .code(cdw.getCode())
        .display(cdw.getDisplay())
        .build();
  }

  List<Coding> codings(List<CdwCoding> source) {
    return convertAll(source, this::coding);
  }

  DosageInstruction dosageInstruction(CdwDosageInstruction cdw) {
    if (cdw == null
        || (allBlank(
                cdw.getAdditionalInstructions(),
                cdw.isAsNeededBoolean(),
                cdw.getDoseQuantity(),
                cdw.getSiteCodeableConcept(),
                cdw.getTiming())
            && isBlank(cdw.getText()))) {
      return null;
    }
    return convert(
        cdw,
        source ->
            DosageInstruction.builder()
                .text(source.getText())
                .additionalInstructions(codeableConcept(source.getAdditionalInstructions()))
                .doseQuantity(simpleQuantity(source.getDoseQuantity()))
                .timing(timing(source.getTiming()))
                .asNeededBoolean(source.isAsNeededBoolean())
                .siteCodeableConcept(codeableConcept(source.getSiteCodeableConcept()))
                .route(routeCodeableConcept(source.getRoute()))
                .build());
  }

  /**
   * Maps dosage instructions out which is a complex type with multiple complex types contained
   * within.
   */
  List<DosageInstruction> dosageInstructions(CdwDosageInstructions cdw) {
    return convertAll(
        ifPresent(cdw, CdwDosageInstructions::getDosageInstruction), this::dosageInstruction);
  }

  /**
   * This is from a cardinality mismatch between the XSD and model. XSD says that there can be an
   * array of identifiers coming back, but the DSTU2 spec specifies only one should return.
   */
  Identifier identifier(List<CdwIdentifier> maybeCdw) {
    if (maybeCdw == null || maybeCdw.isEmpty()) {
      return null;
    }
    CdwIdentifier firstItem = maybeCdw.get(0);
    if (firstItem == null
        || allBlank(firstItem.getSystem(), firstItem.getValue(), firstItem.getUse())) {
      return null;
    }
    return Identifier.builder()
        .system(firstItem.getSystem())
        .value(firstItem.getValue())
        .use(identifierUse(firstItem.getUse()))
        .build();
  }

  Identifier.IdentifierUse identifierUse(CdwIdentifierUseCodes source) {
    if (source == null) {
      return null;
    }
    return EnumSearcher.of(Identifier.IdentifierUse.class).find(source.value());
  }

  private boolean isUsable(CdwReference reference) {
    return reference != null && !allBlank(reference.getReference(), reference.getDisplay());
  }

  Double quantityValue(String source) {
    if (isBlank(source)) {
      return null;
    }
    try {
      return Double.valueOf(source);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Cannot create double value from " + source, e);
    }
  }

  Reference reference(CdwReference maybeSource) {
    if (!isUsable(maybeSource)) {
      return null;
    }
    return convert(
        maybeSource,
        source ->
            Reference.builder()
                .display(source.getDisplay())
                .reference(source.getReference())
                .build());
  }

  /**
   * Not a fan of having multiple methods for building codeable concepts just because of these one
   * off cdw versions. Maybe make more generic so this isn't like this?
   */
  CodeableConcept routeCodeableConcept(CdwRoute maybeCdw) {
    if (maybeCdw == null || isBlank(maybeCdw.getText())) {
      return null;
    }
    return CodeableConcept.builder().text(maybeCdw.getText()).build();
  }

  /**
   * simpleQuantity and quantityValue might be useful to take out to Transformers? This same pattern
   * is in Observation.
   */
  SimpleQuantity simpleQuantity(CdwSimpleQuantity source) {
    if (source == null
        || allBlank(source.getCode(), source.getSystem(), source.getUnit(), source.getValue())) {
      return null;
    }
    return SimpleQuantity.builder()
        .value(quantityValue(source.getValue()))
        .unit(source.getUnit())
        .code(source.getCode())
        .system(source.getSystem())
        .build();
  }

  Status status(CdwMedicationDispenseStatus source) {
    if (source == null) {
      return null;
    }
    return EnumSearcher.of(MedicationDispense.Status.class).find(source.value());
  }

  /** Our version of Timing is just a wrapper around a codeable concept. */
  Timing timing(CdwTiming maybeCdw) {
    if (maybeCdw == null || maybeCdw.getCode() == null) {
      return null;
    }
    return Timing.builder().code(codeableConcept(maybeCdw.getCode())).build();
  }

  /** Maps codeable concept out of Type field. */
  CodeableConcept typeCodeableConcept(CdwMedicationDispenseType maybeCdw) {
    if (maybeCdw == null) {
      return null;
    }
    Coding maybeCoding = typeCoding(maybeCdw.getCoding());
    if (maybeCoding == null && isBlank(maybeCdw.getText())) {
      return null;
    }
    return CodeableConcept.builder()
        .text(maybeCdw.getText())
        .coding(Collections.singletonList(maybeCoding))
        .build();
  }

  Coding typeCoding(CdwMedicationDispenseTypeCoding source) {
    if (source == null || allBlank(source.getCode(), source.getDisplay(), source.getSystem())) {
      return null;
    }
    Coding.CodingBuilder builder = Coding.builder();
    if (source.getCode() != null) {
      builder.code(source.getCode().value());
    }
    if (source.getDisplay() != null) {
      builder.display(source.getDisplay().value());
    }
    return builder.system(source.getSystem()).build();
  }
}
