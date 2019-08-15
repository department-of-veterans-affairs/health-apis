package gov.va.api.health.dataquery.tests.fhirtodatamart;

import gov.va.api.health.argonaut.api.resources.AllergyIntolerance;
import gov.va.api.health.dataquery.service.controller.EnumSearcher;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.DatamartAllergyIntolerance;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartCoding;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.elements.Reference;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static gov.va.api.health.dataquery.service.controller.Transformers.allBlank;

public class F2DAllergyIntoleranceTransformer {
    private Optional<DatamartReference> reference(Reference reference, String type){
        if(reference==null){
            return null;
        }
        if(allBlank(reference.display(),reference.reference())){
            return null;
        }
        return Optional.of(DatamartReference.builder().display(Optional.of(reference.display())).reference(Optional.of(reference.reference())).type(Optional.of(type)).build());
    }
    private DatamartAllergyIntolerance.Type type(AllergyIntolerance.Type type) {
        if (type == null) {
            return null;
        }
        return EnumSearcher.of(DatamartAllergyIntolerance.Type.class).find(type.toString());
    }
    private DatamartAllergyIntolerance.Status status(AllergyIntolerance.Status status) {
        if (status == null) {
            return null;
        }
        return EnumSearcher.of(DatamartAllergyIntolerance.Status.class).find(status.toString());
    }
    private DatamartAllergyIntolerance.Category category(AllergyIntolerance.Category category) {
        if (category == null) {
            return null;
        }
        return EnumSearcher.of(DatamartAllergyIntolerance.Category.class).find(category.toString());
    }
    private Optional<Instant> recordedDate(String date){
        if (date==null){
            return null;
        }
        return Optional.of(Instant.parse(date));
    }
//    private Optional<DatamartAllergyIntolerance.Substance> substance(CodeableConcept substance){
//        return Optional.of(DatamartAllergyIntolerance.Substance.builder().coding(coding(substance.coding()))).text(substance.text()).build());
//    }
//    private Optional<DatamartCoding> coding(List<Coding> coding){
//        return Optional.of(DatamartCoding.builder().code(Optional.of(coding.code())).display(Optional.of(coding.display())).system(Optional.of(coding.system())).build());}

    public DatamartAllergyIntolerance fhirToDatamart(AllergyIntolerance allergyIntolerance) {
        return DatamartAllergyIntolerance.builder()
                .objectType(allergyIntolerance.resourceType())
                .cdwId(allergyIntolerance.id())
                .patient(reference(allergyIntolerance.patient(),"Patient"))
                .recordedDate(recordedDate(allergyIntolerance.recordedDate()))
                .recorder(reference(allergyIntolerance.recorder(),"Practitioner"))
                //.substance(substance(allergyIntolerance.substance()))
                .status(status(allergyIntolerance.status()))
                .type(type(allergyIntolerance.type()))
                .category(category(allergyIntolerance.category()))
                // .notes(List.of(DatamartAllergyIntolerance.Note.builder().build()))
                // .reactions(List.of(DatamartAllergyIntolerance.Reaction.builder().build()))
                .build();
    }
}
