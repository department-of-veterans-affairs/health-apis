package gov.va.api.health.dataquery.service.controller;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.r4.api.elements.Reference;

import java.util.Optional;

public class R4Transformers {

    /** Convert the datamart reference (if specified) to a FHIR reference. */
    public static Reference asReference(Optional<DatamartReference> maybeReference) {
        if (maybeReference == null || maybeReference.isEmpty()) {
            return null;
        }
        return asReference(maybeReference.get());
    }

    /** Convert the datamart reference (if specified) to a FHIR reference. */
    public static Reference asReference(DatamartReference maybeReference) {
        if (maybeReference == null) {
            return null;
        }
        Optional<String> path = maybeReference.asRelativePath();
        if (maybeReference.display().isEmpty() && path.isEmpty()) {
            return null;
        }
        return Reference.builder()
                .display(maybeReference.display().orElse(null))
                .reference(path.orElse(null))
                .build();
    }
}
