package gov.va.api.health.dataquery.service.controller.immunization;

import gov.va.api.health.argonaut.api.resources.Immunization;
import gov.va.api.health.dataquery.service.controller.EnumSearcher;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dstu2.api.datatypes.Annotation;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import lombok.Builder;

import java.util.List;
import java.util.Optional;

import static gov.va.api.health.dataquery.service.controller.Transformers.asReference;

@Builder
public class DatamartImmunizationTransformer {

    private final DatamartImmunization datamart;

    Immunization.Status status(DatamartImmunization.Status status) {
       return EnumSearcher.of(Immunization.Status.class).find(status.toString());
    }

    CodeableConcept vaccineCode(DatamartImmunization.VaccineCode vaccineCode){
        return CodeableConcept.builder()
                .text(vaccineCode.text)
                .coding(List.of(
                        Coding.builder().code(vaccineCode.code).build()
                )).build();
    }

    List<Annotation> note (Optional <String> note){
        if (note.isPresent()) {
            return List.of(Annotation.builder()
                    .text(note.get())
                    .build());
        }
        else{
            return null;
        }
    }
    List<Immunization.Reaction> reaction (Optional<DatamartReference> reaction)
    {
        return List.of(Immunization.Reaction.builder().detail(asReference(reaction)).build());
    }
    /** Convert the datamart structure to FHIR compliant structure. */
    public Immunization toFhir() {
        return Immunization.builder()
                .resourceType("Immunization")
                .id(datamart.cdwId())
                .status(status(datamart.status()))
                .vaccineCode(vaccineCode(datamart.vaccineCode()))
                .patient(asReference(datamart.patient()))
                .wasNotGiven(datamart.wasNotGiven())
                .performer(asReference(datamart.performer()))
                .requester(asReference(datamart.requester()))
                .encounter(asReference(datamart.encounter()))
                .location(asReference(datamart.location()))
                .note(note(datamart.note()))
                .reaction(reaction(datamart.reaction()))
                .build();
    }
}
