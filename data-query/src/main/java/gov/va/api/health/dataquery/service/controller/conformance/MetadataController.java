package gov.va.api.health.dataquery.service.controller.conformance;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import gov.va.api.health.dataquery.service.config.ReferenceSerializerProperties;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.datatypes.ContactPoint;
import gov.va.api.health.dstu2.api.datatypes.ContactPoint.ContactPointSystem;
import gov.va.api.health.dstu2.api.elements.Extension;
import gov.va.api.health.dstu2.api.elements.Reference;
import gov.va.api.health.dstu2.api.resources.Conformance;
import gov.va.api.health.dstu2.api.resources.Conformance.AcceptUnknown;
import gov.va.api.health.dstu2.api.resources.Conformance.Contact;
import gov.va.api.health.dstu2.api.resources.Conformance.Kind;
import gov.va.api.health.dstu2.api.resources.Conformance.ResourceInteraction;
import gov.va.api.health.dstu2.api.resources.Conformance.ResourceInteractionCode;
import gov.va.api.health.dstu2.api.resources.Conformance.Rest;
import gov.va.api.health.dstu2.api.resources.Conformance.RestMode;
import gov.va.api.health.dstu2.api.resources.Conformance.RestResource;
import gov.va.api.health.dstu2.api.resources.Conformance.RestSecurity;
import gov.va.api.health.dstu2.api.resources.Conformance.SearchParamType;
import gov.va.api.health.dstu2.api.resources.Conformance.Software;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
  value = {"/api/metadata"},
  produces = {"application/json", "application/json+fhir", "application/fhir+json"}
)
@AllArgsConstructor(onConstructor = @__({@Autowired}))
class MetadataController {
  private static final String ALLERGYINTOLERANCE_HTML =
      "http://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-allergyintolerance.html";

  private static final String APPOINTMENT_HTML = "https://www.hl7.org/fhir/DSTU2/appointment.html";

  private static final String CONDITION_HTML =
      "http://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-condition.html";

  private static final String DIAGNOSTICREPORT_HTML =
      "http://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-diagnosticreport.html";

  private static final String ENCOUNTER_HTML = "https://www.hl7.org/fhir/DSTU2/encounter.html";

  private static final String IMMUNIZATION_HTML =
      "http://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-immunization.html";

  private static final String LOCATION_HTML = "https://www.hl7.org/fhir/DSTU2/location.html";

  private static final String MEDICATION_HTML =
      "http://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-medication.html";

  private static final String MEDICATIONDISPENSE_HTML =
      "https://www.hl7.org/fhir/DSTU2/medicationdispense.html";

  private static final String MEDICATIONORDER_HTML =
      "http://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-medicationorder.html";

  private static final String MEDICATIONSTATEMENT_HTML =
      "http://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-medicationstatement.html";

  private static final String OBSERVATIONRESULTS_HTML =
      "http://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-observationresults.html";

  private static final String ORGANIZATION_HTML =
      "https://www.hl7.org/fhir/DSTU2/organization.html";

  private static final String PATIENT_HTML =
      "http://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-patient.html";

  private static final String PRACTITIONER_HTML =
      "https://www.hl7.org/fhir/DSTU2/practitioner.html";

  private static final String PROCEDURE_HTML =
      "http://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-procedure.html";

  private final ConformanceStatementProperties properties;

  @Autowired ReferenceSerializerProperties referenceSerializerProperties;

  private Collection<SearchParam> appointmentSearchParams() {
    switch (properties.getStatementType()) {
      case PATIENT:
        return emptyList();
      case CLINICIAN:
        return singletonList(SearchParam.PATIENT);
      default:
        throw noSearchParamsForConformanceStatementTypeException();
    }
  }

  private Collection<SearchParam> conditionSearchParams() {
    switch (properties.getStatementType()) {
      case PATIENT:
        return singletonList(SearchParam.PATIENT);
      case CLINICIAN:
        return asList(SearchParam.CATEGORY, SearchParam.CLINICAL_STATUS, SearchParam.PATIENT);
      default:
        throw noSearchParamsForConformanceStatementTypeException();
    }
  }

  private List<Contact> contact() {
    return singletonList(
        Contact.builder()
            .name(properties.getContact().getName())
            .telecom(
                singletonList(
                    ContactPoint.builder()
                        .system(ContactPointSystem.email)
                        .value(properties.getContact().getEmail())
                        .build()))
            .build());
  }

  private Collection<SearchParam> diagnosticReportSearchParams() {
    switch (properties.getStatementType()) {
      case PATIENT:
        return singletonList(SearchParam.PATIENT);
      case CLINICIAN:
        return asList(
            SearchParam.CATEGORY, SearchParam.CODE, SearchParam.DATE, SearchParam.PATIENT);
      default:
        throw noSearchParamsForConformanceStatementTypeException();
    }
  }

  private Collection<SearchParam> medicationDispenseSearchParams() {
    switch (properties.getStatementType()) {
      case PATIENT:
        return singletonList(SearchParam.PATIENT);
      case CLINICIAN:
        return asList(SearchParam.PATIENT, SearchParam.STATUS, SearchParam.TYPE);
      default:
        throw noSearchParamsForConformanceStatementTypeException();
    }
  }

  private IllegalArgumentException noSearchParamsForConformanceStatementTypeException() {
    throw new IllegalStateException(
        "Search parameters are not configured for conformance statement type: "
            + properties.getStatementType());
  }

  private Collection<SearchParam> observationSearchParams() {
    switch (properties.getStatementType()) {
      case PATIENT:
        return asList(SearchParam.PATIENT, SearchParam.CATEGORY);
      case CLINICIAN:
        return asList(
            SearchParam.CATEGORY, SearchParam.CODE, SearchParam.DATE, SearchParam.PATIENT);
      default:
        throw noSearchParamsForConformanceStatementTypeException();
    }
  }

  private Collection<SearchParam> patientSearchParams() {
    switch (properties.getStatementType()) {
      case PATIENT:
        return singletonList(SearchParam.ID);
      case CLINICIAN:
        return asList(
            SearchParam.BIRTH_DATE,
            SearchParam.FAMILY,
            SearchParam.GENDER,
            SearchParam.GIVEN,
            SearchParam.ID,
            SearchParam.NAME);
      default:
        throw noSearchParamsForConformanceStatementTypeException();
    }
  }

  private Collection<SearchParam> procedureSearchParams() {
    switch (properties.getStatementType()) {
      case PATIENT:
        return singletonList(SearchParam.PATIENT);
      case CLINICIAN:
        return asList(SearchParam.DATE, SearchParam.PATIENT);
      default:
        throw noSearchParamsForConformanceStatementTypeException();
    }
  }

  @GetMapping
  Conformance read() {
    return Conformance.builder()
        .resourceType("Conformance")
        .id(properties.getId())
        .version(properties.getVersion())
        .name(properties.getName())
        .publisher(properties.getPublisher())
        .contact(contact())
        .date(properties.getPublicationDate())
        .description(properties.getDescription())
        .kind(Kind.capability)
        .software(software())
        .fhirVersion(properties.getFhirVersion())
        .acceptUnknown(AcceptUnknown.no)
        .format(asList("application/json+fhir", "application/json", "application/fhir+json"))
        .rest(rest())
        .build();
  }

  private List<RestResource> resources() {
    return Stream.of(
            support("AllergyIntolerance")
                .documentation(ALLERGYINTOLERANCE_HTML)
                .searchBy(SearchParam.PATIENT)
                .build(),
            support("Appointment")
                .documentation(APPOINTMENT_HTML)
                .search(appointmentSearchParams())
                .build(),
            support("Condition")
                .documentation(CONDITION_HTML)
                .search(conditionSearchParams())
                .build(),
            support("DiagnosticReport")
                .documentation(DIAGNOSTICREPORT_HTML)
                .search(diagnosticReportSearchParams())
                .build(),
            support("Encounter").documentation(ENCOUNTER_HTML).build(),
            support("Immunization")
                .documentation(IMMUNIZATION_HTML)
                .searchBy(SearchParam.PATIENT)
                .build(),
            support("Location").documentation(LOCATION_HTML).build(),
            support("Medication").documentation(MEDICATION_HTML).build(),
            support("MedicationDispense")
                .documentation(MEDICATIONDISPENSE_HTML)
                .search(medicationDispenseSearchParams())
                .build(),
            support("MedicationOrder")
                .documentation(MEDICATIONORDER_HTML)
                .searchBy(SearchParam.PATIENT)
                .build(),
            support("MedicationStatement")
                .documentation(MEDICATIONSTATEMENT_HTML)
                .searchBy(SearchParam.PATIENT)
                .build(),
            support("Observation")
                .documentation(OBSERVATIONRESULTS_HTML)
                .search(observationSearchParams())
                .build(),
            support("Organization").documentation(ORGANIZATION_HTML).build(),
            support("Patient").documentation(PATIENT_HTML).search(patientSearchParams()).build(),
            support("Practitioner").documentation(PRACTITIONER_HTML).build(),
            support("Procedure")
                .documentation(PROCEDURE_HTML)
                .search(procedureSearchParams())
                .build())
        .map(SupportedResource::asResource)
        .filter(r -> referenceSerializerProperties.checkForResource(r.type()))
        .collect(Collectors.toList());
  }

  private List<Rest> rest() {
    return singletonList(
        Rest.builder()
            .mode(RestMode.server)
            .security(restSecurity())
            .resource(resources())
            .build());
  }

  private RestSecurity restSecurity() {
    return RestSecurity.builder()
        .extension(
            singletonList(
                Extension.builder()
                    .url("http://fhir-registry.smarthealthit.org/StructureDefinition/oauth-uris")
                    .extension(
                        asList(
                            Extension.builder()
                                .url("token")
                                .valueUri(properties.getSecurity().getTokenEndpoint())
                                .build(),
                            Extension.builder()
                                .url("authorize")
                                .valueUri(properties.getSecurity().getAuthorizeEndpoint())
                                .build()))
                    .build()))
        .cors(true)
        .service(singletonList(smartOnFhirCodeableConcept()))
        .description(properties.getSecurity().getDescription())
        .build();
  }

  private CodeableConcept smartOnFhirCodeableConcept() {
    return CodeableConcept.builder()
        .coding(
            singletonList(
                Coding.builder()
                    .system("http://hl7.org/fhir/restful-security-service")
                    .code("SMART-on-FHIR")
                    .display("SMART-on-FHIR")
                    .build()))
        .build();
  }

  private Software software() {
    return Software.builder().name(properties.getSoftwareName()).build();
  }

  private SupportedResource.SupportedResourceBuilder support(String type) {
    return SupportedResource.builder().properties(properties).type(type);
  }

  @Getter
  @AllArgsConstructor
  enum SearchParam {
    BIRTH_DATE("birthdate", SearchParamType.date),
    CATEGORY("category", SearchParamType.string),
    CLINICAL_STATUS("clinicalstatus", SearchParamType.token),
    CODE("code", SearchParamType.token),
    DATE("date", SearchParamType.date),
    FAMILY("family", SearchParamType.string),
    GENDER("gender", SearchParamType.token),
    GIVEN("given", SearchParamType.string),
    ID("_id", SearchParamType.string),
    NAME("name", SearchParamType.string),
    PATIENT("patient", SearchParamType.reference),
    STATUS("status", SearchParamType.token),
    TYPE("type", SearchParamType.token);

    private final String param;

    private final SearchParamType type;
  }

  @Value
  @Builder
  private static class SupportedResource {
    String type;

    String documentation;

    @Singular("searchBy")
    Set<SearchParam> search;

    ConformanceStatementProperties properties;

    RestResource asResource() {
      return RestResource.builder()
          .type(type)
          .profile(Reference.builder().reference(documentation).build())
          .interaction(interactions())
          .searchParam(searchParams())
          .build();
    }

    private List<ResourceInteraction> interactions() {
      if (search.isEmpty()) {
        return singletonList(readable());
      }
      return asList(searchable(), readable());
    }

    private ResourceInteraction readable() {
      return ResourceInteraction.builder()
          .code(ResourceInteractionCode.read)
          .documentation(properties.getResourceDocumentation())
          .build();
    }

    private List<Conformance.SearchParam> searchParams() {
      if (search.isEmpty()) {
        return null;
      }
      return search
          .stream()
          .map(s -> Conformance.SearchParam.builder().name(s.param()).type(s.type()).build())
          .collect(Collectors.toList());
    }

    private ResourceInteraction searchable() {
      return ResourceInteraction.builder()
          .code(ResourceInteractionCode.search_type)
          .documentation(properties.getResourceDocumentation())
          .build();
    }
  }
}
