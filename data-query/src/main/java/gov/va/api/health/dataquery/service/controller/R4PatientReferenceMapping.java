package gov.va.api.health.dataquery.service.controller;

import gov.va.api.health.dataquery.service.config.LinkProperties;
import gov.va.api.lighthouse.datamart.DatamartEntity;
import gov.va.api.lighthouse.vulcan.mappings.ReferenceMapping;
import gov.va.api.lighthouse.vulcan.mappings.ReferenceParameter;
import java.util.Set;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;

/** Create reference mapping for R4 patients. */
@AllArgsConstructor(staticName = "forLinks")
public class R4PatientReferenceMapping<EntityT extends DatamartEntity>
    implements Supplier<ReferenceMapping<EntityT>> {

  private final LinkProperties linkProperties;

  @Override
  public ReferenceMapping<EntityT> get() {
    return ReferenceMapping.<EntityT>builder()
        .parameterName("patient")
        .fieldNameSelector(referenceParameter -> Set.of("icn"))
        .defaultResourceType("Patient")
        .allowedReferenceTypes(Set.of("Patient"))
        .supportedReference(this::referencePatientIsSupported)
        .valueSelector(this::referencePatientValues)
        .build();
  }

  /** Determine if the ReferenceParameter for a search by "patient" is supported. */
  private boolean referencePatientIsSupported(ReferenceParameter reference) {
    // Only support R4 Patient Read urls
    if (reference.url().isPresent()) {
      var allowedUrl = linkProperties.r4().readUrl("Patient", reference.publicId());
      return reference.url().get().equals(allowedUrl);
    }
    return "Patient".equals(reference.type());
  }

  private String referencePatientValues(ReferenceParameter reference) {
    return reference.publicId();
  }
}
