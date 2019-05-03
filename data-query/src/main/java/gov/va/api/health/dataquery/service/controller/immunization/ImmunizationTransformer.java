package gov.va.api.health.dataquery.service.controller.immunization;

import static gov.va.api.health.dataquery.service.controller.Transformers.allBlank;
import static gov.va.api.health.dataquery.service.controller.Transformers.asDateTimeString;
import static gov.va.api.health.dataquery.service.controller.Transformers.convert;
import static gov.va.api.health.dataquery.service.controller.Transformers.convertAll;
import static gov.va.api.health.dataquery.service.controller.Transformers.ifPresent;
import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.argonaut.api.resources.Immunization;
import gov.va.api.health.argonaut.api.resources.Immunization.Reaction;
import gov.va.api.health.argonaut.api.resources.Immunization.Status;
import gov.va.api.health.dataquery.service.controller.EnumSearcher;
import gov.va.api.health.dstu2.api.DataAbsentReason;
import gov.va.api.health.dstu2.api.DataAbsentReason.Reason;
import gov.va.api.health.dstu2.api.datatypes.Annotation;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.datatypes.Identifier;
import gov.va.api.health.dstu2.api.datatypes.Identifier.IdentifierUse;
import gov.va.api.health.dstu2.api.elements.Extension;
import gov.va.api.health.dstu2.api.elements.Reference;
import gov.va.dvp.cdw.xsd.model.CdwCodeableConcept;
import gov.va.dvp.cdw.xsd.model.CdwCoding;
import gov.va.dvp.cdw.xsd.model.CdwImmunization103Root.CdwImmunizations.CdwImmunization;
import gov.va.dvp.cdw.xsd.model.CdwImmunization103Root.CdwImmunizations.CdwImmunization.CdwIdentifiers;
import gov.va.dvp.cdw.xsd.model.CdwImmunization103Root.CdwImmunizations.CdwImmunization.CdwNotes;
import gov.va.dvp.cdw.xsd.model.CdwImmunization103Root.CdwImmunizations.CdwImmunization.CdwReactions;
import gov.va.dvp.cdw.xsd.model.CdwImmunizationReported;
import gov.va.dvp.cdw.xsd.model.CdwImmunizationStatus;
import gov.va.dvp.cdw.xsd.model.CdwReference;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ImmunizationTransformer implements ImmunizationController.Transformer {
  @Override
  public Immunization apply(CdwImmunization source) {
    return Immunization.builder()
        .resourceType(Immunization.class.getSimpleName())
        .id(source.getCdwId())
        .identifier(identifier(source.getIdentifiers()))
        .status(status(source.getStatus()))
        ._status(statusExtension(source.getStatus()))
        .date(asDateTimeString(source.getDate()))
        .vaccineCode(vaccineCode(source.getVaccineCode()))
        .patient(reference(source.getPatient()))
        .wasNotGiven(source.isWasNotGiven())
        .reported(reported(source.getReported()))
        ._reported(reportedExtension(source.getReported()))
        .performer(reference(source.getPerformer()))
        .requester(reference(source.getRequester()))
        .encounter(reference(source.getEncounter()))
        .location(reference(source.getLocation()))
        .note(note(source.getNotes()))
        .reaction(reaction(source.getReactions()))
        .build();
  }

  private Coding coding(CdwCoding cdw) {
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

  List<Identifier> identifier(CdwIdentifiers maybeSource) {
    return convertAll(
        ifPresent(maybeSource, CdwIdentifiers::getIdentifier),
        source ->
            Identifier.builder()
                .system(source.getSystem())
                .value(source.getValue())
                .use(convert(source.getUse(), IdentifierUse::valueOf))
                .build());
  }

  List<Annotation> note(CdwNotes notes) {
    return convertAll(
        ifPresent(notes, CdwNotes::getNote),
        item -> Annotation.builder().text(item.getText()).build());
  }

  List<Reaction> reaction(CdwReactions maybeSource) {
    return convertAll(
        ifPresent(maybeSource, CdwReactions::getReaction),
        item -> Reaction.builder().detail(reference(item.getDetail())).build());
  }

  Reference reference(CdwReference maybeSource) {
    if (maybeSource == null || allBlank(maybeSource.getReference(), maybeSource.getDisplay())) {
      return null;
    }
    return convert(
        maybeSource,
        source ->
            Reference.builder()
                .reference(source.getReference())
                .display(source.getDisplay())
                .build());
  }

  Boolean reported(CdwImmunizationReported source) {
    if (source == null || source == CdwImmunizationReported.DATA_ABSENT_REASON_UNSUPPORTED) {
      return null;
    }
    return source == CdwImmunizationReported.TRUE;
  }

  Extension reportedExtension(CdwImmunizationReported source) {
    if (source == null) {
      return DataAbsentReason.of(Reason.unknown);
    }
    if (source == CdwImmunizationReported.DATA_ABSENT_REASON_UNSUPPORTED) {
      return DataAbsentReason.of(Reason.unsupported);
    }
    return null;
  }

  Status status(CdwImmunizationStatus source) {
    if (source == null || source == CdwImmunizationStatus.DATA_ABSENT_REASON_UNSUPPORTED) {
      return null;
    }
    return EnumSearcher.of(Immunization.Status.class).find(source.value());
  }

  Extension statusExtension(CdwImmunizationStatus source) {
    if (source == null) {
      return DataAbsentReason.of(Reason.unknown);
    }
    if (source == CdwImmunizationStatus.DATA_ABSENT_REASON_UNSUPPORTED) {
      return DataAbsentReason.of(Reason.unsupported);
    }
    return null;
  }

  CodeableConcept vaccineCode(CdwCodeableConcept maybeSource) {
    if (maybeSource == null) {
      return null;
    }
    if (maybeSource.getCoding().isEmpty() && isBlank(maybeSource.getText())) {
      return null;
    }
    return convert(
        maybeSource,
        source ->
            CodeableConcept.builder()
                .text(source.getText())
                .coding(codings(source.getCoding()))
                .build());
  }
}
