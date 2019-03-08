package gov.va.api.health.sentinel.mockcdw;

import gov.va.api.health.sentinel.mockcdw.MockEntries.Entry;
import java.util.Arrays;
import lombok.Data;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Stored procedure call parameters. These will be matched in the index and the corresponding file
 * will be matched.
 */
@Data
class MockCall {
  private int page;
  private int count;
  private String fhirString;

  /** Return true if the count is with in normal bounds. Return false if it is beyond the limits. */
  private static boolean isNormalCount(int recordCount) {
    return recordCount > 0 && recordCount < 21;
  }

  private String aliasOf(String key) {
    if ("_id".equals(key)) {
      return "identifier";
    }
    return key;
  }

  private boolean isSameCount(Entry e) {
    return e.count() == count();
  }

  private boolean isSamePage(Entry e) {
    return e.page() == page();
  }

  boolean matches(MockEntries.Entry e) {
    if (!e.query().startsWith(resourceAndVersion())) {
      return false;
    }

    MultiValueMap<String, String> entryParams = parametersOf(e.query());
    if (!parametersOf(fhirString()).equals(entryParams)) {
      return false;
    }
    if (entryParams.size() == 1 && entryParams.keySet().contains("identifier")) {
      /*
       * Page and count don't matter for read
       */
      return true;
    }

    return isSamePage(e) && isSameCount(e);
  }

  /**
   * For fringe cases for page and _count, we might not have have search results for every single
   * case, we'll assume that we're _close_ if the count is 0 or 21 and the patient matches. We'll
   * ignore other parameters such category or type.
   */
  boolean matchesMostly(Entry entry) {
    if (!entry.query().startsWith(resourceAndVersion())) {
      return false;
    }
    /*
     * Only support queries that are beyond the fringes of normal record counts.
     */
    if (isNormalCount(count) || isNormalCount(entry.count())) {
      return false;
    }
    /*
     * Only compare patient or identifier values.
     */
    String myPatient = parametersOf(fhirString()).getFirst("patient");
    if (myPatient != null) {
      String theirPatient = parametersOf(entry.query()).getFirst("patient");
      return myPatient.equals(theirPatient) && isSameCount(entry);
    }
    String myIdentifier = parametersOf(fhirString()).getFirst("identifier");
    if (myIdentifier != null) {
      String theirIdentifier = parametersOf(entry.query()).getFirst("identifier");
      return myIdentifier.equals(theirIdentifier) && isSameCount(entry);
    }
    return false;
  }

  private MultiValueMap<String, String> parametersOf(String resourceVersionParams) {
    String[] parts = resourceVersionParams.split("\\?");
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    if (parts.length > 1) {
      Arrays.stream(parts[1].split("&"))
          .filter(p -> !p.startsWith("page=") && !p.startsWith("_count="))
          .map(kv -> kv.split("="))
          .forEach(kv -> params.add(aliasOf(kv[0]), kv[1]));
    }
    return params;
  }

  private String resourceAndVersion() {
    return fhirString.split("\\?")[0];
  }
}
