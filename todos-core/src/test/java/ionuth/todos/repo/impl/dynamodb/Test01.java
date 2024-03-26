package ionuth.todos.repo.impl.dynamodb;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.amazonaws.services.dynamodbv2.local.embedded.DynamoDBEmbedded;
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
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

public class Test01 {
	
	private static final String TABLE_NAME = "Manual-Todos";
	
	public static void main(String[] args) {
		
		try {
			
			//DynamoDBEmbedded.create().dynamoDbClient();
			//DynamoDBEmbedded.create().dynamoDbAsyncClient();
			
			DynamoDbClient dynamoClient;
			
			// it's also as simple as that - to check what are the differences
			// but this does not stop the java process either at then end
			//dynamoClient = DynamoDBEmbedded.create().dynamoDbClient();
			
			String serverPort = "8000";
			String serverUri = "http://localhost:" + serverPort;
			String[] serverArgs = { "-inMemory", "-port", serverPort };
			System.out.println("Starting DynamoDB local...");
			DynamoDBProxyServer server = ServerRunner.createServerFromCommandLineArgs(serverArgs);
			server.start();
			
			System.out.println(server.toString());
			
			AwsBasicCredentials credentials = AwsBasicCredentials.create("dummyKey", "dummySecret");
			dynamoClient = DynamoDbClient.builder()
					.endpointOverride(URI.create(serverUri))
					.httpClient(UrlConnectionHttpClient.builder().build())
					.region(Region.US_EAST_1)
					.credentialsProvider(StaticCredentialsProvider.create(credentials))
					.build();
			
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
			
			// Q: do we get this response before Waiting for creation from below happens ??
			CreateTableResponse createTableResp = dynamoClient.createTable(createTableReq);
			System.out.println("CreateTableResp: " + createTableResp.toString());
			
			DynamoDbWaiter waiter = dynamoClient.waiter();
			DescribeTableRequest descTableReq = DescribeTableRequest.builder()
					.tableName(TABLE_NAME)
					.build();
			WaiterResponse<DescribeTableResponse> waitResp = waiter.waitUntilTableExists(descTableReq); 
			Optional<DescribeTableResponse> optDescResp = waitResp.matched().response();
			if( optDescResp.isPresent() ) {
				System.out.println("DescribeTableResponse after waiting: " + optDescResp.get());
			}
			waiter.close();
			
			
			System.out.println();
			System.out.println("---------------------");
			System.out.println();
			
			// put items
			
			Map<String, AttributeValue> putAttrs = new HashMap<>();
			PutItemRequest putReq = null;
			
			putAttrs.put("PK", AttributeValue.fromS("pk-111"));
			putAttrs.put("SK", AttributeValue.fromS("sk-111"));
			putAttrs.put("UserEmail", AttributeValue.fromS("gigi@yahoo.com"));
			putAttrs.put("UserDesc", AttributeValue.fromS("gigi description"));
			putReq = PutItemRequest.builder()
					.tableName(TABLE_NAME)
					.item(putAttrs)
					.build();
			dynamoClient.putItem(putReq);
			
			putAttrs.clear();
			putAttrs.put("PK", AttributeValue.fromS("pk-111"));
			putAttrs.put("SK", AttributeValue.fromS("sk-222"));
			putAttrs.put("UserEmail", AttributeValue.fromS("vasile@gmail.com"));
			putAttrs.put("UserDesc", AttributeValue.fromS("vasile din vasilesti"));
			putReq = PutItemRequest.builder()
					.tableName(TABLE_NAME)
					.item(putAttrs)
					.build();
			dynamoClient.putItem(putReq);
			
			// get one item by full key PK + SK
			QueryRequest queryReq = QueryRequest.builder()
					.tableName(TABLE_NAME)
					.keyConditionExpression("PK = :pk AND SK = :sk")
					.expressionAttributeValues( Map.of(
							":pk", AttributeValue.fromS("pk-111"), 
							":sk", AttributeValue.fromS("sk-222") ))
					.build();
			QueryResponse response = dynamoClient.query(queryReq);
			if(response.hasItems()) {
				Map<String, AttributeValue> result = response.items().get(0);
				System.out.println("Select one item:" +
						" UserEmail: " + result.get("UserEmail").s() +
						" UserDesc: " + result.get("UserDesc").s());
			}
			
			dynamoClient.close();
			server.stop();
			System.out.println();
			System.out.println("server stopped");
			System.exit(1);
			
		} catch(Exception ex) {
			System.err.println(ex);
		}
	}
	
}
