package gov.va.api.health.dataquery.service.controller;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/** This implementation uses a configurable base URL (data-query.public-url) for the links. */
@Service
public class ConfigurableBaseUrlPageLinks implements PageLinks {
  /**
   * The published URL for argonaut, which is likely not the hostname of the machine running this
   * application.
   */
  private final String baseUrl;

  /** Base path for DSTU2 resources, e.g. api/dstu2 */
  private String dstu2BasePath;

  /** Base path for STU3 esources, e.g. api/stu3 */
  private String stu3BasePath;

  /** Spring constructor. */
  @Autowired
  public ConfigurableBaseUrlPageLinks(
      @Value("${data-query.public-url}") String baseUrl,
      @Value("${data-query.public-dstu2-base-path}") String dstu2BasePath,
      @Value("${data-query.public-stu3-base-path}") String stu3BasePath) {
    this.baseUrl = baseUrl;
    this.dstu2BasePath = dstu2BasePath;
    this.stu3BasePath = stu3BasePath;
  }

  @Override
  public List<gov.va.api.health.dstu2.api.bundle.BundleLink> dstu2Links(
      PageLinks.LinkConfig config) {
    return links(new Dstu2LinkContext(baseUrl, dstu2BasePath, config));
  }

  @Override
  public String dstu2ReadLink(String resourcePath, String id) {
    return baseUrl + "/" + dstu2BasePath + "/" + resourcePath + "/" + id;
  }

  private <B> List<B> links(AbstractLinkContext<B> context) {
    List<B> links = new LinkedList<>();
    /*
     * If recordsPerPage = 0, then only return the self link.
     */
    if (!context.isCountOnly()) {
      links.add(context.first());
      if (context.hasPrevious()) {
        links.add(context.previous());
      }
    }
    links.add(context.self());
    if (!context.isCountOnly()) {
      if (context.hasNext()) {
        links.add(context.next());
      }
      links.add(context.last());
    }
    return links;
  }

  @Override
  public List<gov.va.api.health.stu3.api.bundle.BundleLink> stu3Links(PageLinks.LinkConfig config) {
    return links(new Stu3LinkContext(baseUrl, stu3BasePath, config));
  }

  @Override
  public String stu3ReadLink(String resourcePath, String id) {
    return baseUrl + "/" + stu3BasePath + "/" + resourcePath + "/" + id;
  }

  /**
   * This context wraps the link state to allow link creation to be clearly described.
   *
   * @param <B> The bundle link class
   */
  @Data
  abstract static class AbstractLinkContext<B> {
    private final String baseUrl;

    private final String basePath;

    private final LinkConfig config;

    abstract B first();

    final boolean hasNext() {
      return config.page() < lastPage();
    }

    final boolean hasPrevious() {
      return config.page() > 1 && config.page() <= lastPage();
    }

    final boolean isCountOnly() {
      return config.recordsPerPage() == 0;
    }

    abstract B last();

    final int lastPage() {
      return (int) Math.ceil((double) config.totalRecords() / (double) config.recordsPerPage());
    }

    abstract B next();

    abstract B previous();

    abstract B self();

    final Stream<String> toKeyValueString(Map.Entry<String, List<String>> entry) {
      return entry.getValue().stream().map((value) -> entry.getKey() + '=' + value);
    }

    final String toUrl(int page) {
      MultiValueMap<String, String> mutableParams = new LinkedMultiValueMap<>(config.queryParams());
      mutableParams.remove("page");
      mutableParams.remove("_count");
      StringBuilder msg = new StringBuilder(baseUrl).append('/').append(basePath).append('/');
      msg.append(config.path()).append('?');
      String params =
          mutableParams
              .entrySet()
              .stream()
              .sorted(Comparator.comparing(Entry::getKey))
              .flatMap(this::toKeyValueString)
              .collect(Collectors.joining("&"));
      if (!params.isEmpty()) {
        msg.append(params).append('&');
      }
      msg.append("page=").append(page).append("&_count=").append(config.recordsPerPage());
      return msg.toString();
    }
  }
}
