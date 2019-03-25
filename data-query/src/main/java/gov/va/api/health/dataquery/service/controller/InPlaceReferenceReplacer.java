package gov.va.api.health.dataquery.service.controller;

import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;

/**
 * Searches for an replaces CDW identities in references in XML documents with public identities
 * returned by the Identity Service. Reference nodes are two fold. Resource Identifier _resources_
 * are always specified in all uppercase with underscores when interacting with the Identity
 * Service, e.g `PATIENT` or `ALLERGY_INTOLLERANCE`. For {@code <cdwId>} nodes, the resource type is
 * defined byt the resource in the query. For {@code <reference>} nodes, the resource type is
 * determined by splitting the value on `/` and using the first part.
 *
 * <p>Internally, the document is processed by seeing if "handler" can be applied to the node. For
 * each node with a handler, Resource Identities are created to represent the reference and
 * collected into a single list. All of the identities are registered in a single call. Using the
 * registration response, nodes with handlers are revisited and updated with their public identity
 * values.
 */
@Slf4j
final class InPlaceReferenceReplacer {
  @NonNull private final IdentityService identityService;

  @NonNull private final String resource;

  @NonNull private final MultiValueMap<String, String> parameters;

  @NonNull private final Document document;

  private final List<ReferenceNodeHandler> handlers =
      Arrays.asList(new NormalReferenceNodeHandler(), new CdwIdReferenceNodeHandler());

  @Builder
  private InPlaceReferenceReplacer(
      String resource,
      MultiValueMap<String, String> parameters,
      Document document,
      IdentityService identityService) {
    this.resource = resource;
    this.parameters = parameters;
    this.document = document;
    this.identityService = identityService;
  }

  /**
   * Given a FHIR name, magically turn it into an Identity Service name. For example,
   *
   * <pre>
   * Patient -> PATIENT
   * AllergyIntolerance -> ALLERGY_INTOLERANCE
   * </pre>
   */
  static String fhirToIdentityService(String fhirName) {
    return Arrays.stream(StringUtils.splitByCharacterTypeCamelCase(fhirName))
        .map(s -> s.toUpperCase(Locale.ENGLISH))
        .collect(Collectors.joining("_"));
  }

  /**
   * Extract the CDW identity from the possible identities in the given registration. An error is
   * thrown if a CDW id cannot be found.
   */
  private static ResourceIdentity findCdwIdentity(@NonNull Registration registration) {
    return registration
        .resourceIdentities()
        .stream()
        .filter(InPlaceReferenceReplacer::isCdw)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("No CDW identity for: " + registration));
  }

  /**
   * Given an Identity Service name, magically turn it into a FHIR name. For example,
   *
   * <pre>
   * PATIENT -> Patient
   * ALLERGY_INTOLERANCE -> AllergyIntolerance
   * </pre>
   */
  static String identityServiceToFhir(String identityServiceName) {
    return Arrays.stream(StringUtils.splitByWholeSeparator(identityServiceName, "_"))
        .map(s -> StringUtils.capitalize(s.toLowerCase(Locale.ENGLISH)))
        .collect(Collectors.joining());
  }

  /** Return true if the given identity belongs to the CDW system. */
  static boolean isCdw(@NonNull ResourceIdentity identity) {
    return "CDW".equals(identity.system());
  }

  /** Convert the 'resource/identity' reference into a CDW Resource Identity instance. */
  static ResourceIdentity referenceToResourceIdentity(@NonNull String typeSlashId) {
    String[] parts = typeSlashId.split("/");
    if (parts.length != 2) {
      throw new IllegalArgumentException(
          "Invalid reference. Expected resource/identity. Got: " + typeSlashId);
    }
    String type = parts[0];
    String id = parts[1];
    return ResourceIdentity.builder()
        .system("CDW")
        .resource(fhirToIdentityService(type))
        .identifier(id)
        .build();
  }

  /** Create reference pairs of CDW and Universal identities in the `resource/identity` form. */
  static ReferencePair referencesOf(@NonNull Registration registration) {
    ResourceIdentity identity = findCdwIdentity(registration);
    String resource = identityServiceToFhir(identity.resource());
    return ReferencePair.builder()
        .cdw(resource + "/" + identity.identifier())
        .universal(resource + "/" + registration.uuid())
        .build();
  }

  /**
   * Scan the document for reference node and record their reference value to node. It is possible
   * the multiple nodes have the same reference. This will build map that associates all nodes that
   * share a reference. For example,
   *
   * <pre>
   *   patient/1234 = [ n1 ]
   *   practitioner/5678 = [ n2, n3, n4 ]
   * </pre>
   */
  private MultiValueMap<String, Node> collectReferenceNodes() {
    if (!(document instanceof DocumentTraversal)) {
      throw new CannotTraverseDocument();
    }
    MultiValueMap<String, Node> referenceNodes = new LinkedMultiValueMap<>();
    DocumentTraversal traversal = (DocumentTraversal) document;
    NodeIterator iterator =
        traversal.createNodeIterator(
            document, NodeFilter.SHOW_ELEMENT, referenceNodeFilter(), false);
    Node node;
    while ((node = iterator.nextNode()) != null) {
      String reference = handlerFor(node).referenceOf(node);
      referenceNodes.add(reference, node);
    }
    return referenceNodes;
  }

  /**
   * Get the handler for the given node or throw an exception. This method expects that determining
   * whether or not a node qualifies for handling has already been done.
   */
  private ReferenceNodeHandler handlerFor(Node node) {
    return handlers
        .stream()
        .filter(t -> t.isReference(node))
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "Node appears to no longer have a handler:" + node.getNodeName()));
  }

  private NodeFilter referenceNodeFilter() {
    return node ->
        handlers.stream().anyMatch(t -> t.isReference(node))
            ? NodeFilter.FILTER_ACCEPT
            : NodeFilter.FILTER_SKIP;
  }

  /**
   * Make the service call to register resource identities for each reference and return the
   * corresponding registrations.
   */
  private List<Registration> registerIds(MultiValueMap<String, Node> referenceNodes) {
    List<ResourceIdentity> identities =
        referenceNodes
            .keySet()
            .stream()
            .map(InPlaceReferenceReplacer::referenceToResourceIdentity)
            .collect(Collectors.toList());
    return identityService.register(identities);
  }

  private Consumer<? super Registration> replaceReference(
      MultiValueMap<String, Node> referenceNodes) {
    return (registration) -> {
      ReferencePair reference = referencesOf(registration);
      List<Node> nodes = referenceNodes.get(reference.cdw());
      if (nodes == null) {
        log.warn(
            "Ignoring registration reference {}. There are no associated nodes for {}",
            reference,
            registration);
        return;
      }
      nodes.forEach(node -> handlerFor(node).updateReference(node, reference.universal()));
    };
  }

  /**
   * Update and then return the document by registering all references and replacing their values.
   */
  Document replaceReferences() {
    MultiValueMap<String, Node> referenceNodes = collectReferenceNodes();
    if (referenceNodes.isEmpty()) {
      return document;
    }
    List<Registration> registrations = registerIds(referenceNodes);
    registrations.forEach(replaceReference(referenceNodes));
    return document;
  }

  /** This interface defines a generic mechanism to process nodes. */
  private interface ReferenceNodeHandler {

    /** Return true if this handler can process the given node, otherwise false. */
    boolean isReference(Node node);

    /** Determine the reference value of the node in `resource/identity` form. */
    String referenceOf(Node node);

    /** Update the reference value in 'resource/identity' form. */
    void updateReference(Node node, String reference);
  }

  private static class CannotTraverseDocument extends RuntimeException {}

  /**
   * Handler for standard reference nodes following the {@code
   * <reference>resource/identity</reference>}. convention.
   */
  private static class NormalReferenceNodeHandler implements ReferenceNodeHandler {

    @Override
    public boolean isReference(Node node) {
      return "reference".equals(node.getNodeName());
    }

    @Override
    public String referenceOf(Node node) {
      return node.getTextContent();
    }

    @Override
    public void updateReference(Node node, String reference) {
      node.setTextContent(reference);
    }
  }

  /** A pair of references that represent the same object, in the `resource/identity` form. */
  @Value
  @Builder
  static class ReferencePair {

    String cdw;

    String universal;
  }

  /**
   * Handler for {@code <cdwId>identity</cdwId>} nodes. The Query object will be used to determine
   * the resource type.
   */
  private class CdwIdReferenceNodeHandler implements ReferenceNodeHandler {

    @Override
    public boolean isReference(Node node) {
      return "cdwId".equals(node.getNodeName());
    }

    @Override
    public String referenceOf(Node node) {
      return resource + "/" + node.getTextContent();
    }

    @Override
    public void updateReference(Node node, String reference) {
      int slash = reference.indexOf('/');
      if (slash < 0 || slash == reference.length() - 2) {
        throw new ResourceExceptions.SearchFailed(
            parameters, "Do not understand registration value: " + reference);
      }
      node.setTextContent(reference.substring(slash + 1));
    }
  }
}
