package gov.va.api.health.dataquery.service.controller;

import gov.va.api.health.autoconfig.logging.Loggable;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions.NotFound;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.IdentitySubstitution;
import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.lighthouse.datamart.DatamartReference;
import gov.va.api.lighthouse.datamart.HasReplaceableId;
import gov.va.api.lighthouse.datamart.ResourceNameTranslation;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WitnessProtection extends IdentitySubstitution<DatamartReference> {

  @Builder
  @Autowired
  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  public WitnessProtection(IdentityService identityService) {
    super(identityService, WitnessProtection::asResourceIdentity, NotFound::new);
  }

  /** Return a ResourceIdentity if the type and reference fields are available. */
  private static Optional<ResourceIdentity> asResourceIdentity(
      DatamartReference datamartReference) {
    if (!datamartReference.hasTypeAndReference()) {
      return Optional.empty();
    }
    return Optional.of(
        ResourceIdentity.builder()
            .system("CDW")
            .resource(
                ResourceNameTranslation.get().fhirToIdentityService(datamartReference.type().get()))
            .identifier(datamartReference.reference().get())
            .build());
  }

  private <T extends HasReplaceableId>
      Function<T, Stream<DatamartReference>> appendCdwIdReferenceTo(
          Function<T, Stream<DatamartReference>> referencesOf) {
    return t -> Stream.concat(Stream.of(new WriteThroughReference(t)), referencesOf.apply(t));
  }

  /**
   * Register the IDs of the items in the given resource list. Each item will be converted to a
   * stream of references using the provided function. After registration, the references WILL BE
   * MODIFIED to include new identity values.
   */
  @Loggable(arguments = false)
  public <T extends HasReplaceableId> void registerAndUpdateReferences(
      Collection<T> resources, Function<T, Stream<DatamartReference>> referencesOfResource) {
    Operations<T, DatamartReference> operations =
        Operations.<T, DatamartReference>builder()
            .toReferences(appendCdwIdReferenceTo(referencesOfResource))
            .isReplaceable(DatamartReference::hasTypeAndReference)
            .resourceNameOf(
                reference ->
                    ResourceNameTranslation.get()
                        .fhirToIdentityService(reference.type().orElse("")))
            .privateIdOf(reference -> reference.reference().orElse(""))
            .updatePrivateIdToPublicId(DatamartReference::reference)
            .build();
    IdentityMapping identities = register(resources, operations.toReferences());
    identities.replacePrivateIdsWithPublicIds(resources, operations);
  }

  /** Lookup and convert the given public ID to a CDW id. */
  @Loggable(arguments = false)
  public String toCdwId(String publicId) {
    return privateIdOf("CDW", publicId).orElse(publicId);
  }

  /**
   * This write through reference will update the backing datamart object's ID when the reference
   * field is changed.
   */
  @EqualsAndHashCode(callSuper = true)
  private static class WriteThroughReference extends DatamartReference {

    private HasReplaceableId target;

    WriteThroughReference(HasReplaceableId target) {
      super(target.objectType(), target.cdwId(), null);
      this.target = target;
    }

    @Override
    public DatamartReference reference(Optional<String> reference) {
      target.cdwId(reference.orElse(null));
      return super.reference(reference);
    }
  }
}
