package gov.va.api.health.dataquery.patientregistration;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import gov.va.api.health.dataquery.patientregistration.PatientRegistration.Access;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DynamoPatientRegistrar implements PatientRegistrar {

  private final DynamoDB dynamoDB;

  private final Table table;

  public DynamoPatientRegistrar() {
    // SHOULD PICK UP FROM EC2 ROLE OR ENVIRONMENT
    // System.setProperty("aws.accessKeyId", "NOTUSED");
    // System.setProperty("aws.secretKey", "NOTUSED");

    AmazonDynamoDB client =
        AmazonDynamoDBClientBuilder.standard()
            .withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration(
                    "http://localhost:8000", "us-gov-west-1"))
            .build();
    dynamoDB = new DynamoDB(client);
    table = dynamoDB.getTable("patient-registration-local");
    table.describe();
  }

  @Override
  @Async
  public Future<PatientRegistration> register(String icn) {
    log.info("Registering {}", icn);

    long now = Instant.now().toEpochMilli();

    UpdateItemSpec spec =
        new UpdateItemSpec()
            .withPrimaryKey(Schema.primaryKey(icn))
            .withUpdateExpression(
                "set lastAccessTime = :l, firstAccessTime = if_not_exists(firstAccessTime,:f)")
            .withValueMap(Map.of(":f", now, ":l", now))
            .withReturnValues(ReturnValue.ALL_NEW);
    Item item = table.updateItem(spec).getItem();

    var registration =
        PatientRegistration.builder()
            .icn(icn)
            .access(
                List.of(
                    Access.builder()
                        .application("fugazi") // TODO inject name
                        .firstAccessTime(
                            Instant.ofEpochMilli(item.getLong(Schema.FIRST_ACCESS_TIME)))
                        .lastAccessTime(Instant.ofEpochMilli(item.getLong(Schema.LAST_ACCESS_TIME)))
                        .build()))
            .build();
    registration.icn(icn);
    return new AsyncResult<>(registration);
  }

  public static class Schema {

    public static final String FIRST_ACCESS_TIME = "firstAccessTime";

    public static final String LAST_ACCESS_TIME = "lastAccessTime";

    public static final String PARTITION_KEY = "icn";

    public static final Map<String, ScalarAttributeType> ATTRIBUTES =
        Map.of(
            PARTITION_KEY,
            ScalarAttributeType.S,
            FIRST_ACCESS_TIME,
            ScalarAttributeType.N,
            LAST_ACCESS_TIME,
            ScalarAttributeType.N);

    public static PrimaryKey primaryKey(String icn) {
      return new PrimaryKey(PARTITION_KEY, icn);
    }
  }
}
