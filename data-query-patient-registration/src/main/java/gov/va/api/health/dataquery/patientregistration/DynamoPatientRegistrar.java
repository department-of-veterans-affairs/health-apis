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
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(value = "dynamo-patient-registrar.enabled", havingValue = "true")
@Slf4j
public class DynamoPatientRegistrar implements PatientRegistrar {

  private final Table table;

  private final DynamoPatientRegistrarProperties options;

  /** Construct a new instance that will bind to a particular table as specified in the options. */
  public DynamoPatientRegistrar(@Autowired DynamoPatientRegistrarProperties options) {
    AmazonDynamoDB client =
        AmazonDynamoDBClientBuilder.standard()
            .withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration(
                    options.getEndpoint(), options.getRegion()))
            .build();
    DynamoDB dynamoDB = new DynamoDB(client);

    table = dynamoDB.getTable(options.getTable());

    this.options = options;
    log.info("Configuration: {}", options);
  }

  @Override
  @Async
  public CompletableFuture<PatientRegistration> register(String icn) {
    log.info("Registering {}", icn.replace('\n', '_').replace('\r', '_'));

    long now = Instant.now().toEpochMilli();

    UpdateItemSpec spec =
        new UpdateItemSpec()
            .withPrimaryKey(Schema.primaryKey(icn))
            .withUpdateExpression(
                "set lastAccessTime = :l, "
                    + "firstAccessTime = if_not_exists(firstAccessTime,:f), "
                    + "application = if_not_exists(application,:a)")
            .withValueMap(Map.of(":f", now, ":l", now, ":a", options.getApplicationName()))
            .withReturnValues(ReturnValue.ALL_NEW);
    Item item = table.updateItem(spec).getItem();

    var registration =
        PatientRegistration.builder()
            .icn(icn)
            .application(options.getApplicationName())
            .firstAccessTime(Instant.ofEpochMilli(item.getLong(Schema.FIRST_ACCESS_TIME)))
            .lastAccessTime(Instant.ofEpochMilli(item.getLong(Schema.LAST_ACCESS_TIME)))
            .build();
    return new AsyncResult<>(registration).completable();
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
