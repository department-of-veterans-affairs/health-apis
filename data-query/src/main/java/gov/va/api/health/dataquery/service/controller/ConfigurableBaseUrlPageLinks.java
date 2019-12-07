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

/** This implementation uses a configurable base URL (argonaut.url) for the links. */
@Service
public class ConfigurableBaseUrlPageLinks implements PageLinks {
  /**
   * The published URL for argonaut, which is likely not the hostname of the machine running this
   * application.
   */
  private final String baseUrl;

  /** These base path for resources, e.g. api */
  private String basePath;

  @Autowired
  public ConfigurableBaseUrlPageLinks(
      @Value("${argonaut.url}") String baseUrl, @Value("${argonaut.base-path}") String basePath) {
    this.baseUrl = baseUrl;
    this.basePath = basePath;
  }

  @Override
  public List<gov.va.api.health.dstu2.api.bundle.BundleLink> dstu2Links(
      PageLinks.LinkConfig config) {
    Dstu2LinkContext context = new Dstu2LinkContext(baseUrl, basePath, config);
    List<gov.va.api.health.dstu2.api.bundle.BundleLink> links = new LinkedList<>();
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
  public String readLink(String resourcePath, String id) {
    return baseUrl + "/" + basePath + "/" + resourcePath + "/" + id;
  }

  @Override
  public List<gov.va.api.health.stu3.api.bundle.BundleLink> stu3Links(PageLinks.LinkConfig config) {
    Stu3LinkContext context = new Stu3LinkContext(baseUrl, basePath, config);
    List<gov.va.api.health.stu3.api.bundle.BundleLink> links = new LinkedList<>();
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

  /** This context wraps the link state to allow link creation to be clearly described. */
  @Data
  abstract static class AbstractLinkContext {
    private final String baseUrl;

    private final String basePath;

    private final LinkConfig config;

    final boolean hasNext() {
      return config.page() < lastPage();
    }

    final boolean hasPrevious() {
      return config.page() > 1 && config.page() <= lastPage();
    }

    final boolean isCountOnly() {
      return config.recordsPerPage() == 0;
    }

    final int lastPage() {
      return (int) Math.ceil((double) config.totalRecords() / (double) config.recordsPerPage());
    }

    final Stream<String> toKeyValueString(Map.Entry<String, List<String>> entry) {
      return entry.getValue().stream().map((value) -> entry.getKey() + '=' + value);
    }

    String toUrl(int page) {
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
