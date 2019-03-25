package gov.va.api.health.dataquery.service.controller;

import gov.va.api.health.dataquery.service.controller.XmlDocuments.ParseFailed;
import gov.va.api.health.dataquery.service.controller.XmlDocuments.WriteFailed;
import gov.va.api.health.ids.api.IdentityService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.w3c.dom.Document;

@Slf4j
@Component
@AllArgsConstructor(onConstructor = @__({@Autowired}))
public final class WitnessProtection {
  private IdentityService identityService;

  private static Document parse(MultiValueMap<String, String> parameters, String xml) {
    try {
      return XmlDocuments.parse(xml);
    } catch (ParseFailed e) {
      log.error("Failed to parse CDW response: {} ", e.getMessage());
      throw new ResourceExceptions.SearchFailed(parameters, e);
    }
  }

  private static String write(MultiValueMap<String, String> parameters, Document doc) {
    try {
      return XmlDocuments.write(doc);
    } catch (WriteFailed e) {
      log.error("Failed to write XML: {}", e.getMessage());
      throw new ResourceExceptions.SearchFailed(parameters, e);
    }
  }

  public String replaceCdwIdsWithPublicIds(
      String resource, MultiValueMap<String, String> parameters, String cdwXml) {
    try {
      Document cdwDoc = parse(parameters, cdwXml);
      Document publicDoc =
          InPlaceReferenceReplacer.builder()
              .identityService(identityService)
              .resource(resource)
              .parameters(parameters)
              .document(cdwDoc)
              .build()
              .replaceReferences();
      return write(parameters, publicDoc);
    } catch (IdentityService.RegistrationFailed e) {
      throw new ResourceExceptions.SearchFailed(parameters, e);
    }
  }

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
