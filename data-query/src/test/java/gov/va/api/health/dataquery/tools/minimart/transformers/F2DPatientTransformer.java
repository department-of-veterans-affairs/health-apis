package gov.va.api.health.dataquery.tools.minimart.transformers;

import gov.va.api.health.argonaut.api.resources.Patient;
import gov.va.api.health.dataquery.service.controller.patient.DatamartPatient;
import gov.va.api.health.dstu2.api.datatypes.HumanName;

import java.time.Instant;
import java.util.List;


import static gov.va.api.health.dataquery.service.controller.Transformers.parseInstant;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class F2DPatientTransformer {

    private String firstName(List<HumanName> name){
        if(name==null||name.isEmpty()||name.get(0)==null||name.get(0).given()==null||name.get(0).given().isEmpty()){
            return null;
        }
        return name.get(0).given().get(0);
    }

    private String lastName(List<HumanName> name){
        if(name==null||name.isEmpty()||name.get(0)==null||name.get(0).family()==null||name.get(0).family().isEmpty()){
            return null;
        }
        return name.get(0).family().get(0);
    }

    private String name(List<HumanName> name){
        if(name==null||name.isEmpty()||name.get(0)==null){
            return null;
        }
        return name.get(0).text();
    }

    private String deceased(String deathDateTime, Boolean deceasedBoolean){
        if (deathDateTime != null || deceasedBoolean == null){
            return null;
        }
        if(deceasedBoolean) {
            return "Y";
        }
        return "N";
        }

        private String deathDateTime(String deathDateTime, Boolean deceasedBoolean) {
            if (deceasedBoolean != null || isBlank(deathDateTime)) {
                return null;
            }
            Instant instant = parseInstant(deathDateTime);
            if (instant == null) {
                return null;
            }
            return instant.toString();
        }

        private String gender(Patient.Gender gender){
            switch (gender) {
                case Patient.Gender.male:
                    return "M";
                case Patient.Gender.female:
                    return "F";
                case Patient.Gender.other:
                    return "*MISSING*"
                case Patient.Gender.unknown:
                    return "*UNKNOWN AT THIS TIME*";
                default:
                    return null;
            }
        }

    public DatamartPatient fhirToDatamart(Patient patient)
    {
        return DatamartPatient.builder()
                .objectType(patient.resourceType())
                .fullIcn(patient.id())
                .firstName(firstName(patient.name()))
                .lastName(lastName(patient.name()))
                .name(name(patient.name()))
                .birthDateTime(patient.birthDate())
                .deathDateTime(deathDateTime(patient.deceasedDateTime(),patient.deceasedBoolean()))
                .deceased(deceased(patient.deceasedDateTime(),patient.deceasedBoolean()))
                .gender(gender(patient.gender()))
                //.ssn(ssn(patient.identifier()))
                //.telecom(telecom(patient.telecom()))
                //.maritalStatus(maritalStatus(patient.maritalStatus()))
                //.address(address(patient.address()))
                //.ethnicity(ethnicity(patient.extension()))
                //.contact(contact(patient.contact()))
                //.race(race(patient.extension()))
                .build();
    }
}
