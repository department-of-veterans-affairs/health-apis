package gov.va.api.health.dataquery.service.controller.practitioner;

import static gov.va.api.lighthouse.vulcan.Rules.atLeastOneParameterOf;
import static gov.va.api.lighthouse.vulcan.Rules.forbidUnknownParameters;
import static gov.va.api.lighthouse.vulcan.Rules.ifParameter;
import static gov.va.api.lighthouse.vulcan.Rules.parametersNeverSpecifiedTogether;
import static gov.va.api.lighthouse.vulcan.Vulcan.returnNothing;
import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.dataquery.service.config.LinkProperties;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.vulcanizer.Bundling;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedBundler;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedReader;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedTransformation;
import gov.va.api.health.r4.api.resources.Practitioner;
import gov.va.api.lighthouse.vulcan.Vulcan;
import gov.va.api.lighthouse.vulcan.VulcanConfiguration;
import gov.va.api.lighthouse.vulcan.mappings.Mappings;
import gov.va.api.lighthouse.vulcan.mappings.TokenParameter;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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

@Validated
@RestController
@SuppressWarnings("WeakerAccess")
@RequestMapping(
    value = {"/r4/vulcanized/Practitioner"},
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor = @__({@Autowired}))
public class R4PractitionerController {
  private final LinkProperties linkProperties;

  private PractitionerRepository repository;

  private WitnessProtection witnessProtection;

  private VulcanConfiguration<PractitionerEntity> configuration() {
    return VulcanConfiguration.forEntity(PractitionerEntity.class)
        .paging(
            linkProperties.pagingConfiguration("Practitioner", PractitionerEntity.naturalOrder()))
        .mappings(
            Mappings.forEntity(PractitionerEntity.class)
                .value("_id", "cdwId", witnessProtection::toCdwId)
                .token(
                    "practitioner.identifier",
                    "npi",
                    this::tokenIdentifierIsSupported,
                    this::tokenIdentifierValue)
                .get())
        .defaultQuery(returnNothing())
        .rule(atLeastOneParameterOf("patient", "_id", "identifier"))
        .rule(parametersNeverSpecifiedTogether("patient", "_id", "identifier"))
        .rule(ifParameter("status").thenAlsoAtLeastOneParameterOf("patient"))
        .rule(forbidUnknownParameters())
        .build();
  }

  @GetMapping(value = "/{publicId}")
  public Practitioner read(@PathVariable("publicId") String publicId) {
    return vulcanizedReader().read(publicId);
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

  /** Validate token identifier. */
  public boolean tokenIdentifierIsSupported(TokenParameter token) {
    if (token.hasExplicitlyNoSystem()
        || !token.hasSupportedSystem("http://hl7.org/fhir/sid/us-npi")) {
      return false;
    }
    return (isBlank(token.code()));
  }

  /** Get identifier values. */
  public Collection<String> tokenIdentifierValue(TokenParameter token) {
    return token
        .behavior()
        .onExplicitSystemAndExplicitCode(List::of)
        .onNoSystemAndExplicitCode(List::of)
        .onAnySystemAndExplicitCode(List::of)
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

  VulcanizedReader<PractitionerEntity, DatamartPractitioner, Practitioner> vulcanizedReader() {
    return VulcanizedReader.forTransformation(transformation())
        .repository(repository)
        .toPatientId(e -> Optional.empty())
        .toPayload(PractitionerEntity::payload)
        .build();
  }
}
