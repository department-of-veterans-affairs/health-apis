package gov.va.api.health.dataquery.service.controller;

import static java.util.Collections.emptyList;
import static org.springframework.util.CollectionUtils.isEmpty;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dataquery.service.controller.datamart.HasReplaceableId;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

@Slf4j
@Builder
@Component
@AllArgsConstructor(onConstructor = @__({@Autowired}))
public class WitnessProtection {
  private IdentityService identityService;

  private <T extends HasReplaceableId> Function<T, Stream<DatamartReference>> imbellish(
      Function<T, Stream<DatamartReference>> referencesOf) {
    return t ->
        Stream.concat(
            Stream.of(DatamartReference.of().type(t.objectType()).reference(t.cdwId()).build()),
            referencesOf.apply(t));
  }

  /**
   * Register the IDs of the items in the given resource list. Each item will be converted to a
   * stream of references using the provided function. The IdentityMapping returned can be used for
   * look up.
   */
  public <T extends HasReplaceableId> IdentityMapping register(
      Collection<T> resources, Function<T, Stream<DatamartReference>> referencesOf) {
    Set<ResourceIdentity> ids =
        resources
            .stream()
            .flatMap(imbellish(referencesOf))
            .filter(Objects::nonNull)
            .map(DatamartReference::asResourceIdentity)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toSet());
    IdentityMapping identityMapping = registerAndMap(ids);

    resources
        .stream()
        .forEach(r -> identityMapping.publicIdOf(r.asReference()).ifPresent(r::cdwId));

    return identityMapping;
  }

  /** Register IDs. */
  public List<Registration> register(Collection<ResourceIdentity> ids) {
    if (isEmpty(ids)) {
      return emptyList();
    }
    return identityService.register(new ArrayList<>(ids));
  }

  /** Register IDs and return an IdentityMapping that can be used to easily find public IDs. */
  public IdentityMapping registerAndMap(Collection<ResourceIdentity> ids) {
    return new IdentityMapping(register(ids));
  }

  /**
   * Register the IDs of the items in the given resource list. Each item will be converted to a
   * stream of references using the provided function. After registration, the references WILL BE
   * MODIFIED to include new identity values.
   */
  public <T extends HasReplaceableId> void registerAndUpdateReferences(
      Collection<T> resources, Function<T, Stream<DatamartReference>> referencesOf) {
    IdentityMapping mapping = register(resources, referencesOf);
    resources
        .stream()
        .flatMap(referencesOf)
        .filter(Objects::nonNull)
        .forEach(
            reference -> {
              var id = mapping.publicIdOf(reference);
              if (id.isPresent()) {
                reference.reference(id);
              }
            });
  }

  /**
   * Replace public IDs with CDW IDs in the parameters.
   *
   * @see IdentityParameterReplacer
   */
  public MultiValueMap<String, String> replacePublicIdsWithCdwIds(
      MultiValueMap<String, String> publicParameters) {
    try {
      MultiValueMap<String, String> cdwParameters =
          IdentityParameterReplacer.builder()
              .identityService(identityService)
              .identityKey("patient")
              .identityKey("patient_identifier")
              .identityKey("patient_identifier:exact")
              .identityKey("identifier")
              .identityKey("identifier:exact")
              .identityKey("_id")
              .alias(Pair.of("_id", "identifier"))
              .build()
              .rebuildWithCdwIdentities(publicParameters);
      log.info(
          "Public parameters {} converted to CDW parameters {}.", publicParameters, cdwParameters);
      return cdwParameters;
    } catch (IdentityService.LookupFailed e) {
      log.error("Failed to lookup CDW identities: {}", e.getMessage());
      throw new ResourceExceptions.SearchFailed(publicParameters, e);
    } catch (IdentityService.UnknownIdentity e) {
      log.error("Identity is not known: {}", e.getMessage());
      throw new ResourceExceptions.UnknownIdentityInSearchParameter(publicParameters, e);
    }
  }

  /** Lookup and convert the given public ID to a CDW id. */
  public String toCdwId(String publicId) {
    MultiValueMap<String, String> publicParameters = Parameters.forIdentity(publicId);
    MultiValueMap<String, String> cdwParameters = replacePublicIdsWithCdwIds(publicParameters);
    return Parameters.identiferOf(cdwParameters);
  }

  /** Utility for easy look up of ids. */
  public static class IdentityMapping {

    private final Table<String, String, Registration> ids;

    /** Create a new instance with the given registrations. */
    public IdentityMapping(List<Registration> registrations) {
      ids = HashBasedTable.create();
      for (Registration r : registrations) {
        for (ResourceIdentity id : r.resourceIdentities()) {
          ids.put(id.resource(), id.identifier(), r);
        }
      }
    }

    /** Return the mapping for the public ID of the reference if it exists. */
    public Optional<String> publicIdOf(@NonNull DatamartReference reference) {
      return publicIdOf(
          ResourceNameTranslation.get().fhirToIdentityService(reference.type().get()),
          reference.reference().get());
    }

    /**
     * Return the mapping for the public ID of the resource and id if it exists. The resource name
     * should be in IdentityService format, e.g. DIAGNOSTIC_REPORT instead of "DiagnosticReport"
     */
    public Optional<String> publicIdOf(String resourceInIdentityServiceFormat, String identifier) {
      Registration registration = ids.get(resourceInIdentityServiceFormat, identifier);
      return Optional.ofNullable(registration == null ? null : registration.uuid());
    }
  }
}
