package gov.va.api.health.dataquery.service.controller.practitionerrole;

import static gov.va.api.lighthouse.vulcan.Rules.atLeastOneParameterOf;
import static gov.va.api.lighthouse.vulcan.Rules.forbidUnknownParameters;
import static gov.va.api.lighthouse.vulcan.Rules.parametersNeverSpecifiedTogether;
import static gov.va.api.lighthouse.vulcan.Vulcan.returnNothing;

import gov.va.api.health.dataquery.service.config.LinkProperties;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.practitioner.DatamartPractitioner;
import gov.va.api.health.dataquery.service.controller.practitioner.PractitionerEntity;
import gov.va.api.health.dataquery.service.controller.practitioner.PractitionerRepository;
import gov.va.api.health.dataquery.service.controller.vulcanizer.Bundling;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedBundler;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedReader;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedTransformation;
import gov.va.api.health.r4.api.resources.PractitionerRole;
import gov.va.api.lighthouse.vulcan.Rule;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Request Mappings for Practitioner Role Profile, see
 * https://build.fhir.org/ig/HL7/US-Core-R4/StructureDefinition-us-core-practitionerrole.html for
 * implementation details.
 */
@Validated
@RestController
@SuppressWarnings("WeakerAccess")
@RequestMapping(
    value = "/r4/PractitionerRole",
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor = @__({@Autowired}))
public class R4PractitionerRoleController {
  private final WitnessProtection witnessProtection;

  private final LinkProperties linkProperties;

  private PractitionerRepository repository;

  /** Vulcan rule for unimplemented parameter. */
  public static Rule parameterNotImplemented(String parameter) {
    return (r) -> {
      if (r.request().getParameter(parameter) != null) {
        throw new ResourceExceptions.NotImplemented(
            "specialty search param is not yet implemented");
      }
    };
  }

  private VulcanConfiguration<PractitionerEntity> configuration() {
    return VulcanConfiguration.forEntity(PractitionerEntity.class)
        .paging(
            linkProperties.pagingConfiguration(
                "PractitionerRole", PractitionerEntity.naturalOrder()))
        .mappings(
            Mappings.forEntity(PractitionerEntity.class)
                .value("_id", "cdwId", witnessProtection::toCdwId)
                .value("identifier", "npi")
                .string("practitioner.name", "familyName")
                .token(
                    "practitioner.identifier",
                    "npi",
                    this::tokenPractitionerIdentifierIsSupported,
                    this::tokenPractitionerIdentifierValue)
                .get())
        .defaultQuery(returnNothing())
        .rule(
            atLeastOneParameterOf(
                "specialty", "practitioner.identifier", "practitioner.name", "_id", "identifier"))
        .rule(parametersNeverSpecifiedTogether("_id", "identifier", "practitioner.identifier"))
        .rule(parametersNeverSpecifiedTogether("practitioner.identifier", "practitioner.name"))
        .rule(parametersNeverSpecifiedTogether("_id", "practitioner.name"))
        .rule(parametersNeverSpecifiedTogether("identifier", "practitioner.name"))
        .rule(parameterNotImplemented("specialty"))
        .rule(forbidUnknownParameters())
        .build();
  }

  @GetMapping(value = {"/{publicId}"})
  public PractitionerRole read(@PathVariable("publicId") String publicId) {
    return vulcanizedReader().read(publicId);
  }

  @GetMapping(
      value = {"/{publicId}"},
      headers = {"raw=true"})
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    return vulcanizedReader().readRaw(publicId, response);
  }

  /** Search support. */
  @GetMapping
  public PractitionerRole.Bundle search(HttpServletRequest request) {
    return Vulcan.forRepo(repository)
        .config(configuration())
        .build()
        .search(request)
        .map(toBundle());
  }

  VulcanizedBundler<
          PractitionerEntity,
          DatamartPractitioner,
          PractitionerRole,
          PractitionerRole.Entry,
          PractitionerRole.Bundle>
      toBundle() {
    return VulcanizedBundler.forTransformation(transformation())
        .bundling(
            Bundling.newBundle(PractitionerRole.Bundle::new)
                .newEntry(PractitionerRole.Entry::new)
                .linkProperties(linkProperties)
                .build())
        .build();
  }

  /** Validate identifier token. */
  public boolean tokenPractitionerIdentifierIsSupported(TokenParameter token) {
    if (token.hasExplicitlyNoSystem()) {
      return false;
    }
    if (!token.hasSupportedSystem("http://hl7.org/fhir/sid/us-npi")) {
      return false;
    }
    return StringUtils.isNotBlank(token.code());
  }

  /** Get identifier values. */
  public Collection<String> tokenPractitionerIdentifierValue(TokenParameter token) {
    return token
        .behavior()
        .onExplicitSystemAndExplicitCode(List::of)
        .onNoSystemAndExplicitCode(List::of)
        .onAnySystemAndExplicitCode(List::of)
        .build()
        .execute();
  }

  VulcanizedTransformation<PractitionerEntity, DatamartPractitioner, PractitionerRole>
      transformation() {
    return VulcanizedTransformation.toDatamart(PractitionerEntity::asDatamartPractitioner)
        .toResource(dm -> R4PractitionerRoleTransformer.builder().datamart(dm).build().toFhir())
        .witnessProtection(witnessProtection)
        .replaceReferences(
            resource ->
                Stream.concat(
                    resource.practitionerRole().stream()
                        .map(role -> role.managingOrganization().orElse(null)),
                    resource.practitionerRole().stream().flatMap(role -> role.location().stream())))
        .build();
  }

  /** Build VulcanizedReader. */
  public VulcanizedReader<PractitionerEntity, DatamartPractitioner, PractitionerRole>
      vulcanizedReader() {
    return VulcanizedReader.forTransformation(transformation())
        .repository(repository)
        .toPatientId(e -> Optional.empty())
        .toPayload(PractitionerEntity::payload)
        .build();
  }
}
