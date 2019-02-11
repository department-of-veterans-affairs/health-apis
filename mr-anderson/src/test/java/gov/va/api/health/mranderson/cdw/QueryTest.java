package gov.va.api.health.mranderson.cdw;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.springframework.util.LinkedMultiValueMap;

public class QueryTest {
  private Query query() {
    LinkedMultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
    parameters.add("b", "banana");
    parameters.add("a", "ack");
    parameters.add("a", "ick");
    return Query.builder()
        .profile(Profile.ARGONAUT)
        .resource("Foo")
        .version("1.00")
        .count(1)
        .page(2)
        .raw(false)
        .parameters(parameters)
        .build();
  }

  @Test
  public void toQueryString() {
    Query query = query();
    assertThat(query.toQueryString(), is("/Foo:1.00?a=ack&a=ick&b=banana"));
  }

  @Test
  public void toResourceString() {
    Query query = query();
    assertThat(query.toResourceString(), is("/Foo:1.00"));
  }
}
