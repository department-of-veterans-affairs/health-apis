package gov.va.api.health.argonaut.api.swaggerexamples;

import static com.google.common.base.Preconditions.checkArgument;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Streams;
import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.springframework.util.ReflectionUtils;

/**
 * Inject Swagger examples into the openapi.json and openapi.yaml files produced during the compile
 * phase of the build.
 *
 * <p>Swagger examples are provided in the other classes of this package, in static fields prefixed
 * with 'SWAGGER_EXAMPLE_'. These field names are used as substitution markers to be overwritten
 * with their corresponding example objects.
 */
@Slf4j
public class InjectSwaggerExamplesTest {
  private static void sortObjectNode(JsonNode node) {
    checkArgument(node instanceof ObjectNode);
    ObjectNode objNode = (ObjectNode) node;
    List<Map.Entry<String, JsonNode>> elements =
        Streams.stream(objNode.fields())
            .sorted((left, right) -> left.getKey().compareToIgnoreCase(right.getKey()))
            .collect(Collectors.toList());
    objNode.removeAll();
    for (Map.Entry<String, JsonNode> element : elements) {
      objNode.set(element.getKey(), element.getValue());
    }
  }

  private static File toFile(URL url) {
    try {
      return new File(url.toURI());
    } catch (Exception e) {
      return new File(url.getPath());
    }
  }

  @Test
  public void _json() {
    injectSwaggerExamples("openapi.json", JacksonConfig.createMapper());
  }

  @Test
  public void _yaml() {
    injectSwaggerExamples("openapi.yaml", JacksonConfig.createMapper(new YAMLFactory()));
  }

  @SneakyThrows
  private void injectSwaggerExamples(String filename, ObjectMapper mapper) {
    final Map<String, Object> swaggerExamples = loadSwaggerExamples();
    File swaggerFile = new File(new File(targetDirectory(), "swagger"), filename);
    assertThat(swaggerFile.exists()).isTrue();
    JsonNode root = mapper.readTree(swaggerFile);
    List<JsonNode> parents = root.findParents("example");
    Set<String> usedKeys = new LinkedHashSet<>();
    for (final JsonNode parent : parents) {
      final String key = parent.get("example").toString();
      final Object exampleObject = swaggerExamples.get(key);
      if (exampleObject == null) {
        continue;
      }
      JsonNode exampleJsonNode = mapper.readTree(mapper.writeValueAsString(exampleObject));
      assertThat(parent instanceof ObjectNode).isTrue();
      ((ObjectNode) parent).set("example", exampleJsonNode);
      usedKeys.add(key);
    }
    Set<String> unusedKeys = new LinkedHashSet<>(swaggerExamples.keySet());
    unusedKeys.removeAll(usedKeys);
    if (!unusedKeys.isEmpty()) {
      log.warn("No Swagger example injection performed for {}.", unusedKeys);
    }
    sortObjectNode(root.get("paths"));
    sortObjectNode(root.get("components").get("schemas"));
    mapper.writerWithDefaultPrettyPrinter().writeValue(swaggerFile, root);
  }

  /**
   * Collect Swagger examples from static fields in the other classes in this package. The resulting
   * map contains the examples keyed by their field names.
   */
  @SneakyThrows
  private Map<String, Object> loadSwaggerExamples() {
    final Set<Class<?>> swaggerExampleClasses =
        new Reflections(getClass().getPackage().getName(), new SubTypesScanner(false))
            .getSubTypesOf(Object.class);
    final Map<String, Object> swaggerExamples = new LinkedHashMap<>();
    for (Class<?> swaggerExampleClass : swaggerExampleClasses) {
      for (Field swaggerExampleField : swaggerExampleClass.getDeclaredFields()) {
        ReflectionUtils.makeAccessible(swaggerExampleField);
        if (!swaggerExampleField.getName().startsWith("SWAGGER_EXAMPLE_")) {
          continue;
        }
        assertThat(Modifier.isStatic(swaggerExampleField.getModifiers())).isTrue();
        final Object exampleObject = swaggerExampleField.get(null);
        assertThat(exampleObject).isNotNull();
        String key = "\"" + swaggerExampleField.getName() + "\"";
        assertThat(swaggerExamples.containsKey(key)).isFalse();
        swaggerExamples.put(key, exampleObject);
      }
    }
    return swaggerExamples;
  }

  /** Find the target directory by climbing up the file tree from this class. */
  private File targetDirectory() {
    final URL classUrl = getClass().getResource(getClass().getSimpleName() + ".class");
    File file = toFile(classUrl);
    while (true) {
      file = file.getParentFile();
      if (file == null) {
        throw new IllegalStateException(
            "Failed to find target directory from class resource " + toFile(classUrl));
      }
      if (file.isDirectory() && file.getName().equals("target")) {
        return file;
      }
    }
  }
}
