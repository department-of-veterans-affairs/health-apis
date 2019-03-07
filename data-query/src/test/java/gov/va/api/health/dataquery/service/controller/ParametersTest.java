package gov.va.api.health.dataquery.service.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class ParametersTest {

  @Test
  public void add() {
    MultiValueMap<String, String> expected = new LinkedMultiValueMap<>();
    expected.put("a", Arrays.asList("1", "2"));
    expected.set("b", "3");
    assertThat(Parameters.builder().add("a", "1").add("a", "2").add("b", 3).build())
        .isEqualTo(expected);
  }

  @Test
  public void addAllWithArray() {
    MultiValueMap<String, String> expected = new LinkedMultiValueMap<>();
    expected.put("a", Arrays.asList("1", "2"));
    expected.set("b", "3");
    assertThat(Parameters.builder().addAll("a", "1", "2").add("b", "3").build())
        .isEqualTo(expected);
  }

  @Test
  public void addAllWithEmptyArray() {
    MultiValueMap<String, String> expected = new LinkedMultiValueMap<>();
    expected.set("b", "3");
    assertThat(Parameters.builder().addAll("a", new String[0]).add("b", "3").build())
        .isEqualTo(expected);
  }

  @Test
  public void addAllWithEmptyList() {
    MultiValueMap<String, String> expected = new LinkedMultiValueMap<>();
    expected.set("b", "3");
    assertThat(Parameters.builder().addAll("a", new ArrayList<>()).add("b", "3").build())
        .isEqualTo(expected);
  }

  @Test
  public void addAllWithList() {
    MultiValueMap<String, String> expected = new LinkedMultiValueMap<>();
    expected.put("a", Arrays.asList("1", "2"));
    expected.set("b", "3");
    assertThat(Parameters.builder().addAll("a", Arrays.asList("1", "2")).add("b", "3").build())
        .isEqualTo(expected);
  }

  @Test
  public void addAllWithNullArray() {
    MultiValueMap<String, String> expected = new LinkedMultiValueMap<>();
    expected.set("b", "3");
    assertThat(Parameters.builder().addAll("a", (String[]) null).add("b", "3").build())
        .isEqualTo(expected);
  }

  @Test
  public void addAllWithNullList() {
    MultiValueMap<String, String> expected = new LinkedMultiValueMap<>();
    expected.set("b", "3");
    assertThat(Parameters.builder().addAll("a", (List<String>) null).add("b", "3").build())
        .isEqualTo(expected);
  }

  @Test
  public void empty() {
    MultiValueMap<String, String> expected = new LinkedMultiValueMap<>();
    assertThat(Parameters.empty()).isEqualTo(expected);
  }

  @Test
  public void forIdentity() {
    MultiValueMap<String, String> expected = new LinkedMultiValueMap<>();
    expected.set("identifier", "123");
    assertThat(Parameters.forIdentity("123")).isEqualTo(expected);
  }
}
