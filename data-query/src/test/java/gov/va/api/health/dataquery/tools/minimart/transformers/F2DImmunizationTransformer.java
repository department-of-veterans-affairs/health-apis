package gov.va.api.health.dataquery.tools.minimart.transformers;

import static gov.va.api.health.dataquery.tools.minimart.RevealSecretIdentity.toDatamartReferenceWithCdwId;
import static gov.va.api.health.dataquery.tools.minimart.RevealSecretIdentity.unmask;

import gov.va.api.health.argonaut.api.resources.Immunization;
import gov.va.api.health.argonaut.api.resources.Immunization.Reaction;
import gov.va.api.health.argonaut.api.resources.Immunization.VaccinationProtocol;
import gov.va.api.health.dataquery.service.controller.EnumSearcher;
import gov.va.api.health.dataquery.service.controller.immunization.DatamartImmunization;
import gov.va.api.health.dataquery.service.controller.immunization.DatamartImmunization.Status;
import gov.va.api.health.dataquery.service.controller.immunization.DatamartImmunization.VaccinationProtocols;
import gov.va.api.health.dataquery.service.controller.immunization.DatamartImmunization.VaccineCode;
import gov.va.api.health.dstu2.api.datatypes.Annotation;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.elements.Extension;
import gov.va.api.health.dstu2.api.elements.Reference;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class F2DImmunizationTransformer {

  private Instant date(String date) {
    if (date == null) {
      return null;
    }
    return Instant.parse(date);
  }

  public DatamartImmunization fhirToDatamart(Immunization immunization) {
    return DatamartImmunization.builder()
        .cdwId(unmask(immunization.id()))
        .status(status(immunization.status(), immunization._status()))
        .date(date(immunization.date()))
        .vaccineCode(vaccineCode(immunization.vaccineCode()))
        .patient(toDatamartReferenceWithCdwId(immunization.patient()).get())
        .wasNotGiven(immunization.wasNotGiven())
        .performer(toDatamartReferenceWithCdwId(immunization.performer()))
        .requester(toDatamartReferenceWithCdwId(immunization.requester()))
        .encounter(toDatamartReferenceWithCdwId(immunization.encounter()))
        .location(toDatamartReferenceWithCdwId(immunization.location()))
        .note(note(immunization.note()))
        .reaction(toDatamartReferenceWithCdwId(reaction(immunization.reaction())))
        .vaccinationProtocols(vaccinationProtocols(immunization.vaccinationProtocol()))
        .build();
  }

  private Optional<String> note(List<Annotation> note) {
    if (note == null || note.isEmpty()) {
      return null;
    }
    return Optional.of(note.get(0).text());
  }

  private Reference reaction(List<Reaction> reactions) {
    if (reactions == null) {
      return null;
    }
    if (reactions.isEmpty()) {
      return null;
    }
    return reactions.get(0).detail();
  }

  private Status status(Immunization.Status status, Extension extension) {
    if (status == null && extension == null) {
      return null;
    }
    if (status != null) {
      return EnumSearcher.of(DatamartImmunization.Status.class).find(status.toString());
    } else {
      return Status.data_absent_reason_unsupported;
    }
  }

  private Optional<VaccinationProtocols> vaccinationProtocols(
      List<VaccinationProtocol> vaccinationProtocol) {
    if (vaccinationProtocol == null || vaccinationProtocol.isEmpty()) {
      return null;
    }
    return Optional.of(
        VaccinationProtocols.builder()
            .series(vaccinationProtocol.get(0).series())
            .seriesDoses(vaccinationProtocol.get(0).seriesDoses())
            .build());
  }

  private VaccineCode vaccineCode(CodeableConcept vaccineCode) {
    return VaccineCode.builder()
        .code(vaccineCode.coding().get(0).code())
        .text(vaccineCode.text())
        .build();
  }
}
