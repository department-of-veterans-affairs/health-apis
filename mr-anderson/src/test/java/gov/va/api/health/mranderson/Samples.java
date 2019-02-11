package gov.va.api.health.mranderson;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.util.StreamUtils;

@NoArgsConstructor(staticName = "create")
public class Samples {

  public String emptySearchResults() {
    return read("EmptySearchResults.xml");
  }

  public String fakeWithReferences() {
    return read("ResourcesWithReferences.xml");
  }

  public String invalidQueryParams() {
    return read("InvalidQueryParams.xml");
  }

  public String patient() {
    return read("Patient-1.03.xml");
  }

  @SneakyThrows
  private String read(String resource) {
    InputStream stream = getClass().getResourceAsStream("/samples/" + resource);
    assertThat(stream).isNotNull();
    return StreamUtils.copyToString(stream, StandardCharsets.UTF_8);
  }

  public String unknownResource() {
    return read("UnknownResource.xml");
  }
}
