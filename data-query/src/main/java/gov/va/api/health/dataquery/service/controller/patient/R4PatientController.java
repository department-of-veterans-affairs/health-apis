package gov.va.api.health.dataquery.service.controller.patient;

import static gov.va.api.lighthouse.vulcan.Rules.ifParameter;
import static gov.va.api.lighthouse.vulcan.Rules.parametersNeverSpecifiedTogether;
import static gov.va.api.lighthouse.vulcan.Vulcan.returnNothing;

import gov.va.api.health.dataquery.service.config.LinkProperties;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.vulcanizer.Bundling;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedBundler;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedReader;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedTransformation;
import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.health.r4.api.resources.Patient;
import gov.va.api.lighthouse.vulcan.Vulcan;
import gov.va.api.lighthouse.vulcan.VulcanConfiguration;
import gov.va.api.lighthouse.vulcan.mappings.Mappings;
import gov.va.api.lighthouse.vulcan.mappings.TokenParameter;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Request Mappings for US-Core-R4 Patient Profile.
 *
 * @implSpec https://build.fhir.org/ig/HL7/US-Core-R4/StructureDefinition-us-core-patient.html
 */
@RestController
@Validated
@RequestMapping(
    value = {"/r4/Patient"},
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor_ = @Autowired)
public class R4PatientController {
  private static final String PATIENT_IDENTIFIER_SYSTEM_ICN = "http://va.gov/mpi";

  private static final String PATIENT_IDENTIFIER_SYSTEM_SSN = "http://hl7.org/fhir/sid/us-ssn";

  private static final String PATIENT_GENDER_SYSTEM = "http://hl7.org/fhir/administrative-gender";

  private final WitnessProtection witnessProtection;

  private final PatientRepositoryV2 repository;

  private final LinkProperties linkProperties;

  private VulcanConfiguration<PatientEntityV2> configuration() {
    return VulcanConfiguration.<PatientEntityV2>forEntity(PatientEntityV2.class)
        .paging(linkProperties.pagingConfiguration("Patient", PatientEntityV2.naturalOrder()))
        .mappings(
            Mappings.forEntity(PatientEntityV2.class)
                .dateAsInstant("birthdate", "birthDate")
                .string("name", "fullName")
                .string("family", "lastName")
                .token("gender", this::tokenGenderIsSupported, this::tokenGenderValues)
                .token(
                    "_id",
                    this::tokenIdentifierFieldName,
                    this::tokenIdentifierIsSupported,
                    this::tokenIdentiferValues)
                .token(
                    "identifier",
                    this::tokenIdentifierFieldName,
                    this::tokenIdentifierIsSupported,
                    this::tokenIdentiferValues)
                .get())
        .rule(parametersNeverSpecifiedTogether("_id", "identifier"))
        .rule(parametersNeverSpecifiedTogether("name", "family"))
        .rule(ifParameter("birthdate").thenAlsoAtLeastOneParameterOf("name", "family"))
        .rule(ifParameter("gender").thenAlsoAtLeastOneParameterOf("name", "family"))
        .defaultQuery(returnNothing())
        .build();
  }

  /** Read by id. */
  @GetMapping(value = {"/{publicId}"})
  public Patient read(@PathVariable("publicId") String publicId) {
    return vulcanizedReader().read(publicId);
  }

  /** Return the raw Datamart document for the given identifier. */
  @GetMapping(
      value = "/{publicId}",
      headers = {"raw=true"})
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    return vulcanizedReader().readRaw(publicId, response);
  }

  /** US-Core-R4 Patient Search Support. */
  @GetMapping
  public Patient.Bundle search(HttpServletRequest request) {
    return Vulcan.forRepo(repository)
        .config(configuration())
        .build()
        .search(request)
        .map(toBundle());
  }

  VulcanizedBundler<PatientEntityV2, DatamartPatient, Patient, Patient.Entry, Patient.Bundle>
      toBundle() {
    return VulcanizedBundler.forTransformation(transformation())
        .bundling(
            Bundling.newBundle(Patient.Bundle::new)
                .newEntry(Patient.Entry::new)
                .linkProperties(linkProperties)
                .build())
        .build();
  }

  Set<String> toCdwGender(String fhirGender) {
    String cdw = GenderMapping.toCdw(fhirGender);
    if (cdw == null) {
      throw new IllegalArgumentException("Unknown gender: " + fhirGender);
    }
    return Set.of(cdw);
  }

  boolean tokenGenderIsSupported(TokenParameter token) {
    /* Supported:
     * - GENDER_SYSTEM|SOME_CODE (assumes someone using the system is using valid codes)
     * - GENDER_SYSTEM|
     * - GENDER_CODE
     * (If we support searches by SSN identifier, we will need to support SYSTEM| and |CODE)
     */
    return (token.hasSupportedSystem(PATIENT_GENDER_SYSTEM) && token.hasExplicitSystem())
        || (token.hasSupportedCode(Patient.Gender.values()) && token.hasAnySystem());
  }

  Collection<String> tokenGenderValues(TokenParameter token) {
    return token
        .behavior()
        .onExplicitSystemAndExplicitCode((s, c) -> toCdwGender(c))
        .onAnySystemAndExplicitCode(this::toCdwGender)
        .onNoSystemAndExplicitCode(this::toCdwGender)
        .onExplicitSystemAndAnyCode(s -> Set.of())
        .build()
        .execute();
  }

  Collection<String> tokenIdentiferValues(TokenParameter token) {
    return List.of(token.code());
  }

  Collection<String> tokenIdentifierFieldName(TokenParameter token) {
    if (token.hasExplicitSystem()) {
      switch (token.system()) {
        case PATIENT_IDENTIFIER_SYSTEM_SSN:
          return List.of("ssn");
        case PATIENT_IDENTIFIER_SYSTEM_ICN:
          return List.of("icn");
        default:
          /* If a system is not applicable (e.g. an element of type uri),
           * then just the form [parameter]=[code] is used. */
          break;
      }
    }
    /* [code] matches irrespective of the value of the system property. */
    return List.of("icn", "ssn");
  }

  boolean tokenIdentifierIsSupported(TokenParameter token) {
    /* Supported (A code is specified or you get nothing.):
     * - SYSTEM|CODE
     * - CODE
     */
    return (token.hasSupportedSystem(PATIENT_IDENTIFIER_SYSTEM_ICN, PATIENT_IDENTIFIER_SYSTEM_SSN)
            && token.hasExplicitCode())
        || token.hasAnySystem();
  }

  VulcanizedTransformation<PatientEntityV2, DatamartPatient, Patient> transformation() {
    return VulcanizedTransformation.toDatamart(PatientEntityV2::asDatamartPatient)
        .toResource(dm -> R4PatientTransformer.builder().datamart(dm).build().toFhir())
        .witnessProtection(witnessProtection)
        .replaceReferences(resource -> Stream.empty())
        .specializedWitnessProtection(
            (wp, resource) -> {
              if (resource.managingOrganization().isPresent()) {
                var publicId =
                    wp.register(
                        List.of(
                            ResourceIdentity.builder()
                                .system("CDW")
                                .resource("Organization")
                                .identifier(resource.managingOrganization().get())
                                .build()));
                if (!publicId.isEmpty()) {
                  resource.managingOrganization(Optional.ofNullable(publicId.get(0).uuid()));
                }
              }
            })
        .build();
  }

  VulcanizedReader<PatientEntityV2, DatamartPatient, Patient> vulcanizedReader() {
    return VulcanizedReader.forTransformation(transformation())
        .repository(repository)
        .toPatientId(e -> Optional.of(e.icn()))
        .toPayload(PatientEntityV2::payload)
        .build();
  }
}
