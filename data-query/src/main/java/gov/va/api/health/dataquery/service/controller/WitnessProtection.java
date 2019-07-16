package gov.va.api.health.dataquery.service.controller;

import static java.util.Collections.emptyList;
import static org.springframework.util.CollectionUtils.isEmpty;

import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
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

  /** Register IDs. */
  public List<Registration> register(Collection<ResourceIdentity> ids) {
    if (isEmpty(ids)) {
      return emptyList();
    }
    return identityService.register(new ArrayList<>(ids));
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
}
