package gov.va.api.health.dataquery.service.controller;

import gov.va.api.health.dataquery.service.controller.XmlDocuments.ParseFailed;
import gov.va.api.health.dataquery.service.controller.XmlDocuments.WriteFailed;
import gov.va.api.health.ids.api.IdentityService;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.MultiValueMap;
import org.w3c.dom.Document;

@Slf4j
@UtilityClass
public final class WitnessProtection {
  private static Document parse(MultiValueMap<String, String> parameters, String xml) {
    try {
      return XmlDocuments.parse(xml);
    } catch (ParseFailed e) {
      log.error("Failed to parse CDW response: {} ", e.getMessage());
      throw new ResourceExceptions.SearchFailed(parameters, e);
    }
  }

  public static String replaceCdwIdsWithPublicIds(
      IdentityService identityService,
      String resource,
      MultiValueMap<String, String> parameters,
      String cdwXml) {
    try {
      Document cdwDoc = parse(parameters, cdwXml);
      Document publicDoc =
          InPlaceReferenceReplacer.builder()
              .resource(resource)
              .parameters(parameters)
              .document(cdwDoc)
              .identityService(identityService)
              .build()
              .replaceReferences();
      return write(parameters, publicDoc);
    } catch (IdentityService.RegistrationFailed e) {
      throw new ResourceExceptions.SearchFailed(parameters, e);
    }
  }

  public static MultiValueMap<String, String> replacePublicIdsWithCdwIds(
      IdentityService identityService, MultiValueMap<String, String> publicParameters) {
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

  private static String write(MultiValueMap<String, String> parameters, Document xml) {
    try {
      return XmlDocuments.write(xml);
    } catch (WriteFailed e) {
      log.error("Failed to write XML: {}", e.getMessage());
      throw new ResourceExceptions.SearchFailed(parameters, e);
    }
  }
}
