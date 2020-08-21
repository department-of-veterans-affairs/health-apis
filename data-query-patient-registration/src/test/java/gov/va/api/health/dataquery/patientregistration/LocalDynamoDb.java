package gov.va.api.health.dataquery.patientregistration;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import gov.va.api.health.dataquery.patientregistration.DynamoPatientRegistrar.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocalDynamoDb implements AutoCloseable {

  @Getter private final int port;

  @Getter private final String tableName;

  private transient DynamoDBProxyServer server;

  @Getter private DynamoDB dynamoDb;

  @Getter private Table table;

  @Getter private String signingRegion;

  @Builder
  public LocalDynamoDb(int port, String tableName, String signingRegion) {
    this.port = port;
    this.tableName = tableName;
    this.signingRegion = signingRegion == null ? "us-gov-west-1" : signingRegion;
  }

  public static LocalDynamoDb startDefault() {
    return LocalDynamoDb.builder()
        .port(8001)
        .tableName("patient-registration-local")
        .build()
        .start();
  }

  @Override
  @SneakyThrows
  public void close() {
    if (server != null) {
      server.stop();
      server = null;
    }
  }

  public void initializeSchema() {

    List<AttributeDefinition> attributeDefinitions =
        List.of(
            new AttributeDefinition(
                Schema.PARTITION_KEY, Schema.ATTRIBUTES.get(Schema.PARTITION_KEY)));
    List<KeySchemaElement> ks = List.of(new KeySchemaElement(Schema.PARTITION_KEY, KeyType.HASH));
    ProvisionedThroughput provisionedthroughput = new ProvisionedThroughput(1000L, 1000L);
    CreateTableRequest request =
        new CreateTableRequest()
            .withTableName(tableName())
            .withAttributeDefinitions(attributeDefinitions)
            .withKeySchema(ks)
            .withProvisionedThroughput(provisionedthroughput);
    table = dynamoDb.createTable(request);
    log.info("{}", table.describe());
    log.info("wow");
  }

  @SneakyThrows
  public LocalDynamoDb start() {
    if (System.getProperty("sqlite4java.library.path") == null) {
      System.setProperty("sqlite4java.library.path", "target/dependencies");
    }
    System.setProperty("aws.accessKeyId", "NOTUSED");
    System.setProperty("aws.secretKey", "NOTUSED");
    server =
        ServerRunner.createServerFromCommandLineArgs(
            new String[] {"-inMemory", "-port", "" + port()});
    server.start();
    AmazonDynamoDB client =
        AmazonDynamoDBClientBuilder.standard()
            .withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration(
                    "http://localhost:" + port(), signingRegion))
            .build();
    dynamoDb = new DynamoDB(client);
    initializeSchema();
    return this;
  }
}
