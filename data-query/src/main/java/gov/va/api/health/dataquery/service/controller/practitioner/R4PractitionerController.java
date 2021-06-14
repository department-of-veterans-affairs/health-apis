package gov.va.api.health.dataquery.service.controller.practitioner;

import static gov.va.api.lighthouse.vulcan.Rules.atLeastOneParameterOf;
import static gov.va.api.lighthouse.vulcan.Rules.parametersNeverSpecifiedTogether;
import static gov.va.api.lighthouse.vulcan.Vulcan.returnNothing;
import static gov.va.api.lighthouse.vulcan.VulcanConfiguration.PagingConfiguration.noSortableParameters;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;

import gov.va.api.health.dataquery.service.config.LinkProperties;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.vulcanizer.Bundling;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedBundler;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedReader;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedTransformation;
import gov.va.api.health.r4.api.resources.Practitioner;
import gov.va.api.lighthouse.vulcan.Specifications;
import gov.va.api.lighthouse.vulcan.SystemIdFields;
import gov.va.api.lighthouse.vulcan.Vulcan;
import gov.va.api.lighthouse.vulcan.VulcanConfiguration;
import gov.va.api.lighthouse.vulcan.mappings.Mappings;
import gov.va.api.lighthouse.vulcan.mappings.TokenParameter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** R4 practioner endpoint. */
@Validated
@RestController
@SuppressWarnings("WeakerAccess")
@RequestMapping(
    value = {"/r4/Practitioner"},
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor_ = @Autowired)
public class R4PractitionerController {
  private static final String PRACTITIONER_IDENTIFIER_SYSTEM_NPI = "http://hl7.org/fhir/sid/us-npi";

  private final LinkProperties linkProperties;

  private PractitionerRepository repository;

  private WitnessProtection witnessProtection;

  private VulcanConfiguration<PractitionerEntity> configuration() {
    return VulcanConfiguration.forEntity(PractitionerEntity.class)
        .paging(
            linkProperties.pagingConfiguration(
                "Practitioner", PractitionerEntity.naturalOrder(), noSortableParameters()))
        .mappings(
            Mappings.forEntity(PractitionerEntity.class)
                .value("_id", "cdwId", witnessProtection::toCdwId)
                .tokens(
                    "identifier",
                    this::tokenIdentifierIsSupported,
                    this::tokenIdentifierSpecification)
                .string("given", "givenName")
                .string("family", "familyName")
                .string("name", f -> Set.of("familyName", "givenName"))
                .get())
        .defaultQuery(returnNothing())
        .rules(
            List.of(
                atLeastOneParameterOf("_id", "identifier", "name", "given", "family"),
                parametersNeverSpecifiedTogether("identifier", "_id"),
                parametersNeverSpecifiedTogether("name", "given"),
                parametersNeverSpecifiedTogether("name", "family")))
        .build();
  }

  /** Read Support. */
  @GetMapping(value = "/{publicId}")
  public Practitioner read(@PathVariable("publicId") String publicId) {
    if (publicId.length() > 4 && startsWithIgnoreCase(publicId, "npi-")) {
      return readByNpi(publicId.substring(4));
    }
    return vulcanizedReader().read(publicId);
  }

  private Practitioner readByNpi(String npi) {
    PractitionerEntity entity =
        repository.findByNpi(npi, Pageable.unpaged()).stream()
            .filter(Objects::nonNull)
            .findFirst()
            .orElseThrow(() -> new ResourceExceptions.NotFound("NPI: " + npi));
    DatamartPractitioner dm = transformation().toDatamart().apply(entity);
    transformation().applyWitnessProtection(dm);
    return transformation().toResource().apply(dm);
  }

  @GetMapping(
      value = "/{publicId}",
      headers = {"raw=true"})
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    return vulcanizedReader().readRaw(publicId, response);
  }

  /** Search support. */
  @GetMapping
  public Practitioner.Bundle search(HttpServletRequest request) {
    return Vulcan.forRepo(repository)
        .config(configuration())
        .build()
        .search(request)
        .map(toBundle());
  }

  VulcanizedBundler<
          PractitionerEntity,
          DatamartPractitioner,
          Practitioner,
          Practitioner.Entry,
          Practitioner.Bundle>
      toBundle() {
    return VulcanizedBundler.forTransformation(transformation())
        .bundling(
            Bundling.newBundle(Practitioner.Bundle::new)
                .newEntry(Practitioner.Entry::new)
                .linkProperties(linkProperties)
                .build())
        .build();
  }

  private boolean tokenIdentifierIsSupported(TokenParameter token) {
    return token.hasSupportedSystem(PRACTITIONER_IDENTIFIER_SYSTEM_NPI) || token.hasAnySystem();
  }

  private Specification<PractitionerEntity> tokenIdentifierSpecification(TokenParameter token) {
    var systemMappings =
        SystemIdFields.forEntity(PractitionerEntity.class)
            .parameterName("identifier")
            .add(PRACTITIONER_IDENTIFIER_SYSTEM_NPI, "npi");
    return token
        .behavior()
        .onAnySystemAndExplicitCode(
            code ->
                Specifications.<PractitionerEntity>select("cdwId", witnessProtection.toCdwId(code))
                    .or(Specifications.select("npi", code)))
        .onExplicitSystemAndAnyCode(systemMappings.matchSystemOnly())
        .onExplicitSystemAndExplicitCode(systemMappings.matchSystemAndCode())
        .build()
        .execute();
  }

  VulcanizedTransformation<PractitionerEntity, DatamartPractitioner, Practitioner>
      transformation() {
    return VulcanizedTransformation.toDatamart(PractitionerEntity::asDatamartPractitioner)
        .toResource(dm -> R4PractitionerTransformer.builder().datamart(dm).build().toFhir())
        .witnessProtection(witnessProtection)
        .replaceReferences(
            resource ->
                Stream.concat(
                    resource.practitionerRole().stream()
                        .map(role -> role.managingOrganization().orElse(null)),
                    resource.practitionerRole().stream().flatMap(role -> role.location().stream())))
        .build();
  }

  VulcanizedReader<PractitionerEntity, DatamartPractitioner, Practitioner, String>
      vulcanizedReader() {
    return VulcanizedReader
        .<PractitionerEntity, DatamartPractitioner, Practitioner, String>forTransformation(
            transformation())
        .repository(repository)
        .toPatientId(e -> Optional.empty())
        .toPrimaryKey(Function.identity())
        .toPayload(PractitionerEntity::payload)
        .build();
  }
}
