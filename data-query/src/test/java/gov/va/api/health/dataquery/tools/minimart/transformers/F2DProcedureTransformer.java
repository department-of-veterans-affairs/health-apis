package gov.va.api.health.dataquery.tools.minimart.transformers;

import gov.va.api.health.argonaut.api.resources.Procedure;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartCoding;
import gov.va.api.health.dataquery.service.controller.procedure.DatamartProcedure;
import gov.va.api.health.dataquery.tools.minimart.FhirToDatamartUtils;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class F2DProcedureTransformer {

  FhirToDatamartUtils fauxIds;

  private DatamartCoding coding(List<Coding> procCoding) {
    if (procCoding == null || procCoding.isEmpty()) {
      return null;
    }
    // We only expect one coding
    Coding fhirCoding = procCoding.get(0);
    return DatamartCoding.builder()
        .system(optionalString(fhirCoding.system()))
        .code(optionalString(fhirCoding.code()))
        .display(optionalString(fhirCoding.display()))
        .build();
  }

  public DatamartProcedure fhirToDatamart(Procedure procedure) {
    return DatamartProcedure.builder()
        .cdwId(procedure.id())
        // Subject should always be there so if it isn't, we probably want the NPE
        .patient(fauxIds.toDatamartReferenceWithCdwId(procedure.subject()).get())
        .status(DatamartProcedure.Status.valueOf(procedure.status().toString()))
        .coding(coding(procedure.code().coding()))
        .notPerformed(procedure.notPerformed())
        .reasonNotPerformed(reasonNotPerformed(procedure.reasonNotPerformed()))
        .performedDateTime(performedDateTime(procedure.performedDateTime()))
        .encounter(fauxIds.toDatamartReferenceWithCdwId(procedure.encounter()))
        .location(fauxIds.toDatamartReferenceWithCdwId(procedure.location()))
        .build();
  }

  private Optional<String> optionalString(String value) {
    return value != null ? Optional.of(value) : null;
  }

  private Optional<Instant> performedDateTime(String pdt) {
    return pdt != null ? Optional.of(Instant.parse(pdt)) : null;
  }

  private Optional<String> reasonNotPerformed(List<CodeableConcept> cc) {
    if (cc == null || cc.isEmpty()) {
      return null;
    }
    return optionalString(cc.get(0).text());
  }
}
