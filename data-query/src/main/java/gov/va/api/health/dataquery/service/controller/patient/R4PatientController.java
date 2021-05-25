package gov.va.api.health.dataquery.service.controller.patient;

import static gov.va.api.lighthouse.vulcan.Rules.ifParameter;
import static gov.va.api.lighthouse.vulcan.Rules.parametersNeverSpecifiedTogether;
import static gov.va.api.lighthouse.vulcan.Vulcan.returnNothing;

import gov.va.api.health.dataquery.service.config.LinkProperties;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.vulcanizer.Bundling;
import gov.va.api.health.dataquery.service.controller.vulcanizer.SystemIdColumns;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedBundler;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedReader;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedTransformation;
import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.health.r4.api.resources.Patient;
import gov.va.api.lighthouse.vulcan.Specifications;
import gov.va.api.lighthouse.vulcan.Vulcan;
import gov.va.api.lighthouse.vulcan.VulcanConfiguration;
import gov.va.api.lighthouse.vulcan.mappings.Mappings;
import gov.va.api.lighthouse.vulcan.mappings.TokenParameter;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
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
                .string("organization", "managingOrganization")
                .tokens("gender", this::tokenGenderIsSupported, this::tokenGenderSpecification)
                .tokens(
                    "_id",
                    token -> tokenIdentifierIsSupported(token, PATIENT_IDENTIFIER_SYSTEM_ICN),
                    token -> tokenIdSpecification(token.code()))
                .tokens(
                    "identifier",
                    token ->
                        tokenIdentifierIsSupported(
                            token, PATIENT_IDENTIFIER_SYSTEM_ICN, PATIENT_IDENTIFIER_SYSTEM_SSN),
                    this::tokenIdentiferSpecification)
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

  String toCdwGender(String fhirGender) {
    String cdw = GenderMapping.toCdw(fhirGender);
    if (cdw == null) {
      throw new IllegalArgumentException("Unknown gender: " + fhirGender);
    }
    return cdw;
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

  Specification<PatientEntityV2> tokenGenderSpecification(TokenParameter token) {
    return token
        .behavior()
        .onExplicitSystemAndExplicitCode(
            SystemIdColumns.forEntity(PatientEntityV2.class, "gender")
                .add(PATIENT_GENDER_SYSTEM, "gender", this::toCdwGender)
                .forSystemAndCode())
        .onAnySystemAndExplicitCode(
            c -> Specifications.<PatientEntityV2>select("gender", toCdwGender(c)))
        .onExplicitSystemAndAnyCode(s -> Specification.where(null))
        .build()
        .execute();
  }

  private Specification<PatientEntityV2> tokenIdSpecification(String id) {
    return Specifications.<PatientEntityV2>select("icn", id);
  }

  Specification<PatientEntityV2> tokenIdentiferSpecification(TokenParameter token) {
    return token
        .behavior()
        .onExplicitSystemAndExplicitCode(
            (system, code) -> {
              switch (system) {
                case PATIENT_IDENTIFIER_SYSTEM_ICN:
                  return tokenIdSpecification(code);
                case PATIENT_IDENTIFIER_SYSTEM_SSN:
                  return Specifications.<PatientEntityV2>select("ssn", code);
                default:
                  throw new IllegalStateException("Unknown Identifier System: " + system);
              }
            })
        .onAnySystemAndExplicitCode(
            code ->
                tokenIdSpecification(code).or(Specifications.<PatientEntityV2>select("ssn", code)))
        .build()
        .execute();
  }

  boolean tokenIdentifierIsSupported(TokenParameter token, String... supportedSystems) {
    /* Supported (A code is specified or you get nothing.):
     * - SYSTEM|CODE
     * - CODE
     */
    return (token.hasSupportedSystem(supportedSystems) && token.hasExplicitCode())
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

  VulcanizedReader<PatientEntityV2, DatamartPatient, Patient, String> vulcanizedReader() {
    return VulcanizedReader.<PatientEntityV2, DatamartPatient, Patient, String>forTransformation(
            transformation())
        .repository(repository)
        .toPatientId(e -> Optional.of(e.icn()))
        .toPrimaryKey(Function.identity())
        .toPayload(PatientEntityV2::payload)
        .build();
  }
}
