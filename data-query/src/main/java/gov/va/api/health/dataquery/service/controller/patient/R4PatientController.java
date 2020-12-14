package gov.va.api.health.dataquery.service.controller.patient;

import static gov.va.api.lighthouse.vulcan.Rules.ifParameter;
import static gov.va.api.lighthouse.vulcan.Rules.parametersNeverSpecifiedTogether;
import static gov.va.api.lighthouse.vulcan.Vulcan.returnNothing;

import gov.va.api.health.dataquery.service.config.LinkProperties;
import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedBundler;
import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.health.r4.api.bundle.AbstractBundle;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.resources.Patient;
import gov.va.api.lighthouse.vulcan.Vulcan;
import gov.va.api.lighthouse.vulcan.VulcanConfiguration;
import gov.va.api.lighthouse.vulcan.VulcanResult;
import gov.va.api.lighthouse.vulcan.mappings.Mappings;
import gov.va.api.lighthouse.vulcan.mappings.TokenParameter;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
                .token("gender", this::tokenGenderIsSupported, this::tokenGenderValues)
                .token("_id", "icn", this::tokenIdentifierIsSupported, this::tokenIdentiferValues)
                .token(
                    "identifier",
                    "icn",
                    this::tokenIdentifierIsSupported,
                    this::tokenIdentiferValues)
                .value("name", "fullName")
                .value("family", "lastName")
                .get())
        .rule(parametersNeverSpecifiedTogether("_id", "identifier"))
        .rule(parametersNeverSpecifiedTogether("name", "family"))
        .rule(ifParameter("birthdate").thenAlsoAtLeastOneParameterOf("name", "family"))
        .rule(ifParameter("gender").thenAlsoAtLeastOneParameterOf("name", "family"))
        .defaultQuery(returnNothing())
        .build();
  }

  PatientEntityV2 findById(String publicId) {
    Optional<PatientEntityV2> entity = repository.findById(witnessProtection.toCdwId(publicId));
    return entity.orElseThrow(() -> new ResourceExceptions.NotFound(publicId));
  }

  /** Read by id. */
  @GetMapping(value = {"/{publicId}"})
  public Patient read(@PathVariable("publicId") String publicId) {
    DatamartPatient dm = replaceReferences(findById(publicId).asDatamartPatient());
    return R4PatientTransformer.builder().datamart(dm).build().toFhir();
  }

  /** Return the raw Datamart document for the given identifier. */
  @GetMapping(
      value = "/{publicId}",
      headers = {"raw=true"})
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    PatientEntityV2 entityV2 = findById(publicId);
    IncludesIcnMajig.addHeader(response, entityV2.icn());
    return entityV2.payload();
  }

  private DatamartPatient replaceReferences(DatamartPatient p) {
    if (p.managingOrganization().isPresent()) {
      var publicId =
          witnessProtection.register(
              List.of(
                  ResourceIdentity.builder()
                      .system("CDW")
                      .resource("Organization")
                      .identifier(p.managingOrganization().get())
                      .build()));
      if (!publicId.isEmpty()) {
        p.managingOrganization(Optional.of(publicId.get(0).uuid()));
      }
    }
    return p;
  }

  /** US-Core-R4 Patient Search Support. */
  @GetMapping
  public Patient.Bundle search(HttpServletRequest request) {
    return Vulcan.forRepo(repository)
        .config(configuration())
        .build()
        .search(request)
        .map(this::toBundle);
  }

  // Patient doesn't have a CDWId and can't use the VulcanizedBundler
  Patient.Bundle toBundle(VulcanResult<PatientEntityV2> result) {
    List<Patient.Entry> entries =
        result
            .entities()
            .map(PatientEntityV2::asDatamartPatient)
            .map(this::replaceReferences)
            .map(dm -> R4PatientTransformer.builder().datamart(dm).build().toFhir())
            .map(
                pat ->
                    Patient.Entry.builder()
                        .fullUrl(linkProperties.r4().readUrl(pat))
                        .resource(pat)
                        .search(
                            AbstractEntry.Search.builder()
                                .mode(AbstractEntry.SearchMode.match)
                                .build())
                        .build())
            .collect(Collectors.toList());
    return Patient.Bundle.builder()
        .resourceType("Bundle")
        .type(AbstractBundle.BundleType.searchset)
        .total((int) result.paging().totalRecords())
        .link(VulcanizedBundler.toLinks(result.paging()))
        .entry(entries)
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
        || (token.hasSupportedCode("male", "female", "other", "unknown") && token.hasAnySystem());
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

  boolean tokenIdentifierIsSupported(TokenParameter token) {
    /* Supported (ICN is specified or you get nothing.):
     * - MPI_SYSTEM|ICN
     * - ICN
     * (If we support searches by SSN identifier, we will need to support SYSTEM| and |CODE)
     */
    return (token.hasSupportedSystem(PATIENT_IDENTIFIER_SYSTEM_ICN) && token.hasExplicitCode())
        || token.hasAnySystem();
  }
}
