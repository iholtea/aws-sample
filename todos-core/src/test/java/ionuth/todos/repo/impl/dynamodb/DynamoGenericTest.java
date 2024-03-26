package ionuth.todos.repo.impl.dynamodb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.CreateTableResponse;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

public class DynamoGenericTest {
	
	private static final String TABLE_NAME = "Manual-Todos";
	
	private static DynamoDBProxyServer server;
	private static DynamoDbClient dynamoClient;
	
	@BeforeAll
	static void initializeDynamoServerClient() {
		
		try {
			
			String serverPort = "8000";
			String serverUri = "http://localhost:" + serverPort;
			String[] serverArgs = { "-inMemory", "-port", serverPort };
			server = ServerRunner.createServerFromCommandLineArgs(serverArgs);
			server.start();
			
			AwsBasicCredentials credentials = AwsBasicCredentials.create("dummyKey", "dummySecret");
			dynamoClient = DynamoDbClient.builder()
					.endpointOverride(URI.create(serverUri))
					.httpClient(UrlConnectionHttpClient.builder().build())
					.region(Region.US_EAST_1)
					.credentialsProvider(StaticCredentialsProvider.create(credentials))
					.build();
			
			createTable();
			
		} catch(Exception ex) {
			System.err.println(ex);
		}
		
	}
	
	static void createTable() {
		
		AttributeDefinition attrPK = AttributeDefinition.builder()
				.attributeName("PK")
				.attributeType(ScalarAttributeType.S)
				.build();
		KeySchemaElement keyPK = KeySchemaElement.builder()
				.attributeName("PK")
				.keyType(KeyType.HASH)
				.build();
		
		AttributeDefinition attrSK = AttributeDefinition.builder()
				.attributeName("SK")
				.attributeType(ScalarAttributeType.S)
				.build();
		KeySchemaElement keySK = KeySchemaElement.builder()
				.attributeName("SK")
				.keyType(KeyType.RANGE)
				.build();
		
		ProvisionedThroughput throughput = ProvisionedThroughput.builder()
				.readCapacityUnits(5L)
				.writeCapacityUnits(5L)
				.build();
		
		CreateTableRequest createTableReq = CreateTableRequest.builder()
				.attributeDefinitions(attrPK, attrSK)
				.keySchema(keyPK, keySK)
				.provisionedThroughput(throughput)
				.tableName(TABLE_NAME)
				.build();
		
		dynamoClient.createTable(createTableReq);
		
		/*
		 * when running DynamoDB  locally this waiting is not needed
		 * unlike on AWS, CreateTable is instant on local
		 * also local version does not keep track of consumed capacity units
		 */
		/*
		DynamoDbWaiter waiter = dynamoClient.waiter();
		DescribeTableRequest descTableReq = DescribeTableRequest.builder()
				.tableName(TABLE_NAME)
				.build();
		WaiterResponse<DescribeTableResponse> waitResp = waiter.waitUntilTableExists(descTableReq); 
		Optional<DescribeTableResponse> optDescResp = waitResp.matched().response();
		if( optDescResp.isEmpty() ) {
			System.err.println("wait for table creation failed ...");
		}
		waiter.close();
		*/
	}
	
	@AfterAll
	static void cleanup() {
		try {
			dynamoClient.close();
			server.stop();
		} catch(Exception ex) {
			System.err.println(ex);
		}
	}
	
	@Test
	void testDynamoWriteRead() {
		
		PutItemRequest putReq = null;
		PutItemResponse putResp = null;
		
		Map<String, AttributeValue> putAttrs = new HashMap<>();
		
		putAttrs.put("PK", AttributeValue.fromS("pk-111"));
		putAttrs.put("SK", AttributeValue.fromS("sk-111"));
		putAttrs.put("UserEmail", AttributeValue.fromS("gigi@yahoo.com"));
		putAttrs.put("UserDesc", AttributeValue.fromS("gigi description"));
		putReq = PutItemRequest.builder()
				.tableName(TABLE_NAME)
				.item(putAttrs)
				.build();
		putResp = dynamoClient.putItem(putReq);
		assertNotNull(putResp);

		putAttrs.clear();
		putAttrs.put("PK", AttributeValue.fromS("pk-111"));
		putAttrs.put("SK", AttributeValue.fromS("sk-222"));
		putAttrs.put("UserEmail", AttributeValue.fromS("vasile@gmail.com"));
		putAttrs.put("UserDesc", AttributeValue.fromS("vasile din vasilesti"));
		putReq = PutItemRequest.builder()
				.tableName(TABLE_NAME)
				.item(putAttrs)
				.build();
		putResp = dynamoClient.putItem(putReq);
		assertNotNull(putResp);
		
		QueryRequest queryReq = QueryRequest.builder()
				.tableName(TABLE_NAME)
				.keyConditionExpression("PK = :pk AND SK = :sk")
				.expressionAttributeValues( Map.of(
						":pk", AttributeValue.fromS("pk-111"), 
						":sk", AttributeValue.fromS("sk-222") ))
				.build();
		QueryResponse response = dynamoClient.query(queryReq);
		assertTrue(response.hasItems(), "Query for one item");
		assertEquals(response.items().size(), 1);
		
		Map<String, AttributeValue> result = response.items().get(0);
		assertEquals(result.get("UserEmail").s(), "vasile@gmail.com");
		assertEquals(result.get("UserDesc").s(), "vasile din vasilesti");
		
	}
	
}
