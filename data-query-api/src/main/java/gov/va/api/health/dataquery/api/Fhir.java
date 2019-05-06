package gov.va.api.health.dataquery.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import javax.xml.datatype.DatatypeFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Fhir {
  @Schema(description = "http://hl7.org/fhir/DSTU2/datatypes.html#id")
  public static final String ID = "[A-Za-z0-9\\-\\.]{1,64}";

  @Schema(description = "http://hl7.org/fhir/DSTU2/datatypes.html#code")
  public static final String CODE = "[^\\s]+([\\s]+[^\\s]+)*";

  @Schema(description = "http://hl7.org/fhir/DSTU2/datatypes.html#uri")
  public static final String URI = ".+";

  @Schema(description = "http://hl7.org/fhir/DSTU2/datatypes.html#base64binary")
  public static final String BASE64 =
      "^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$";

  @Schema(description = "http://hl7.org/fhir/DSTU2/datatypes.html#date")
  public static final String DATE = "-?[0-9]{4}(-(0[1-9]|1[0-2])(-(0[0-9]|[1-2][0-9]|3[0-1]))?)?";

  @Schema(description = "http://hl7.org/fhir/DSTU2/datatypes.html#datetime")
  public static final String DATETIME =
      "-?[0-9]{4}(-(0[1-9]|1[0-2])(-(0[0-9]|[1-2][0-9]|3[0-1])"
          + "(T([01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9](\\.[0-9]+)?(Z|(\\+|-)"
          + "((0[0-9]|1[0-3]):[0-5][0-9]|14:00)))?)?)?";

  @Schema(
    description =
        "http://hl7.org/fhir/DSTU2/datatypes.html#datetime http://hl7.org/fhir/DSTU2/search.html#prefix"
  )
  public static final String DATETIME_SEARCH =
      "-?(ge|le|eq|ne|gt|lt|sa|eb)?[0-9]{4}(-(0[1-9]|1[0-2])(-(0[0-9]|[1-2][0-9]|3[0-1])"
          + "(T([01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9](\\.[0-9]+)?(Z|(\\+|-)"
          + "((0[0-9]|1[0-3]):[0-5][0-9]|14:00)))?)?)?";

  @Schema(description = "http://hl7.org/fhir/DSTU2/datatypes.html#time")
  public static final String TIME = "([01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9](\\.[0-9]+)?";

  @Schema(description = "http://hl7.org/fhir/DSTU2/datatypes.html#instant")
  public static final String INSTANT =
      "^[0-9]{4}(-(0[1-9]|1[0-2])(-(0[0-9]|[1-2][0-9]|3[0-1])"
          + "(T([01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]"
          + "(\\.[0-9]+)?(Z|(\\+|-)((0[0-9]|1[0-3]):[0-5][0-9]|14:00)))))$";

  @Schema(description = "http://hl7.org/fhir/DSTU2/datatypes.html#oid")
  public static final String OID = "urn:oid:[0-2](\\.[1-9]\\d*)+";

  public static final String XHTML = "<.+>";

  /**
   * Attempt to parse the given dateTime string. Returns null if the given string is null or empty,
   * throws a IllegalArgumentException if the date cannot be parsed, otherwise returns an Instant.
   */
  @SneakyThrows
  public static Instant parseDateTime(String dateTime) {
    if (StringUtils.isBlank(dateTime)) {
      return null;
    }
    return DatatypeFactory.newInstance()
        .newXMLGregorianCalendar(dateTime)
        .toGregorianCalendar()
        .toInstant();
  }
}
