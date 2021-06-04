package gov.va.api.health.dataquery.service.config;

import static gov.va.api.lighthouse.vulcan.Vulcan.useUrl;

import gov.va.api.health.r4.api.resources.Resource;
import gov.va.api.lighthouse.vulcan.Vulcan;
import gov.va.api.lighthouse.vulcan.VulcanConfiguration;
import gov.va.api.lighthouse.vulcan.VulcanConfiguration.PagingConfiguration;

import java.util.List;
import java.util.function.Function;
import javax.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;

/** Configuration public URLS. */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("data-query")
@Data
@Accessors(fluent = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LinkProperties {
  private String publicUrl;
  private String publicDstu2BasePath;
  private String publicStu3BasePath;
  private String publicR4BasePath;
  private int defaultPageSize;
  private int maxPageSize;

  /**
   * Create standard page configuration for use for Vulcan based controllers. This is expecting a
   * resource name, e.g. DiagnosticReport and sorting, which is defined on the Datamart entities.
   */
  public VulcanConfiguration.PagingConfiguration pagingConfiguration(
      String resource, Sort sorting, Function<SortRequest, Sort> sortableParameters) {
    return VulcanConfiguration.PagingConfiguration.builder()
        .baseUrlStrategy(useUrl(r4().resourceUrl(resource)))
        .pageParameter("page")
        .countParameter("_count")
        .defaultCount(defaultPageSize)
        .maxCount(maxPageSize)
        .sort(sorting)
        .sortableParameters(sortableParameters)
        .build();
  }

  @Value
  @Builder
  public static final class SortRequest {
    @NonNull List<Parameter> sorting;

    @Value
    @Builder
    public static final class Parameter {
      @NonNull String parameterName;
      @NonNull Direction direction;
    }

    public static enum Direction {
      ASCENDING,
      DESCENDING
    }
  }

  public Links<Resource> r4() {
    return new Links<Resource>(publicUrl, publicR4BasePath);
  }

  /** Generate links for a specific base URL. */
  @Accessors(fluent = true)
  public static class Links<ResourceT> {
    @Getter private final String baseUrl;

    Links(String publicUrl, String publicBasePath) {
      baseUrl = publicUrl + "/" + publicBasePath;
    }

    public String readUrl(Resource resource) {
      return readUrl(resource.getClass().getSimpleName(), resource.id());
    }

    public String readUrl(String resource, String id) {
      return resourceUrl(resource) + "/" + id;
    }

    public String resourceUrl(String resource) {
      return baseUrl() + "/" + resource;
    }
  }
}
