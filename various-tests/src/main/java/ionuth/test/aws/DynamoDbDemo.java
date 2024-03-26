package ionuth.test.aws;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemResponse;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbResponseMetadata;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.ReturnConsumedCapacity;
import software.amazon.awssdk.services.dynamodb.model.WriteRequest;

public class DynamoDbDemo {
	
	private static final String TABLE_NAME = "Manual-Todos";
	
	private final DynamoDbClient dynamoClient;
	
	public DynamoDbDemo() {
		dynamoClient = DynamoDbClient.builder()
				.region(Region.US_EAST_1)
		        .credentialsProvider(DefaultCredentialsProvider.create())
		        .build();
	}
	
	public void addAllTodoLists() {
		
		TodoListRec listNorway = new TodoListRec("holteai@yahoo.com","uuid-list-01",
				"Norway Road Trip","2024-01-25 05:05:05","2024-01-25 05:05:05","");
		TodoListRec listShopping = new TodoListRec("holteai@yahoo.com","uuid-list-02",
				"Supermarket shopping","2024-02-14 05:05:05","2024-02-14 05:05:05","");
		TodoListRec listNextWeek = new TodoListRec("john.smith@gmail.com","uuid-list-03",
				"Week 04-10 march","2024-03-02 05:05:05","2024-03-02 05:05:05","");
		
		addTodoList(listNorway);
		addTodoList(listShopping);
		addTodoList(listNextWeek);
	}
	
	/*
	 * NOTE that DynamoDbClient.putClient() adds OR updates an item if it exists with the
	 * same value for the key fields
	 * 
	 * To check for duplicate and throw ConditionalCheckException we need to provide
	 * conditionExpression() and expressionAttributesValues() as per code below 
	 */
	public void addTodoList(TodoListRec todoList) {
		
		var userEmailVal = AttributeValue.fromS(todoList.userEmail());
		var listUuidVal = AttributeValue.fromS(todoList.uuid());
		Map<String, AttributeValue> itemValues = new HashMap<>();
		itemValues.put("PK", userEmailVal);
		itemValues.put("SK",  listUuidVal);
		itemValues.put("ListTitle", AttributeValue.fromS(todoList.title()));
		itemValues.put("ListCreationDate", AttributeValue.fromS(todoList.creationDate()));
		itemValues.put("ListLastViewDate", AttributeValue.fromS(todoList.lastViewDate()));
		
		PutItemRequest request = PutItemRequest.builder()
				.tableName(TABLE_NAME)
				.item(itemValues)
				.conditionExpression("PK <> :UserEmail AND SK <> :ListUuid")
				.expressionAttributeValues(Map.of(
						":UserEmail", userEmailVal,
						":ListUuid", listUuidVal))
				.returnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
				.build();
		
		try {
			var response = dynamoClient.putItem(request);
			var capacity = response.consumedCapacity();
			double capUnits = capacity.capacityUnits();  
			System.out.println("Added " + todoList.title() + " total consumed capacity: " + capUnits);
		} catch(ConditionalCheckFailedException ex) {
			System.err.println("TodoList already exists: " + todoList.title() + " # " + ex.getMessage());
		} catch(Exception ex) {
			System.err.println(ex);
		}
		
	}
	
	public WriteRequest item2WriteRequest(TodoItemRec item) {
		Map<String, AttributeValue> attrValues = new HashMap<>();
		attrValues.put("PK", AttributeValue.fromS(item.listUuid()));
		attrValues.put("SK", AttributeValue.fromS(item.itemUuid()));
		attrValues.put("ItemText", AttributeValue.fromS(item.itemText()));
		attrValues.put("ItemDone", AttributeValue.fromBool(item.itemDone()));
		attrValues.put("ItemOrderIdx", AttributeValue.fromN(String.valueOf(item.orderIdx())));
		PutRequest putRequest = PutRequest.builder().item(attrValues).build();
		return WriteRequest.builder().putRequest(putRequest).build();
		
	}
	
	public void addTodoItems01() {
		
		List<WriteRequest> allWriteRequests = new ArrayList<>();
		
		var item_01_01 = new TodoItemRec("uuid-list-01", "uuid-item-01-01", 
				"Buy travel insurance", false, 1, "");
		allWriteRequests.add(item2WriteRequest(item_01_01));
		
		var item_01_02 = new TodoItemRec("uuid-list-01", "uuid-item-01-02", 
				"Create online account for Norway road tax", true, 2, "");
		allWriteRequests.add(item2WriteRequest(item_01_02));
		
		var item_01_03 = new TodoItemRec("uuid-list-01", "uuid-item-01-03",
				"Buy travel insurance", false, 3, "");
		allWriteRequests.add(item2WriteRequest(item_01_03));
		
		try {
			BatchWriteItemRequest batchRequest = BatchWriteItemRequest.builder()
					.requestItems( Map.of(TABLE_NAME, allWriteRequests) )
					.build();
			BatchWriteItemResponse batchResponse = dynamoClient.batchWriteItem(batchRequest);
			System.out.println("Batch Write response: " + batchResponse);
			
		} catch(DynamoDbException ex) {
			System.err.println(ex);
		}
		
	}
	
	public void addTodoItems02() {
		
		List<WriteRequest> allWriteRequests = new ArrayList<>();
		
		var item_02_01 = new TodoItemRec("uuid-list-02", "uuid-item-02-01",
				"Oranges 2kg", false, 1, "");
		allWriteRequests.add(item2WriteRequest(item_02_01));
		
		var item_02_02 = new TodoItemRec("uuid-list-02", "uuid-item-02-02",
				"Pink lady apples 2 packs", true, 2, "");
		allWriteRequests.add(item2WriteRequest(item_02_02));
		
		var item_02_03 = new TodoItemRec("uuid-list-02", "uuid-item-02-03",
				"Potatoes 3kg", true, 3, "");
		allWriteRequests.add(item2WriteRequest(item_02_03));
		
		var item_02_04 = new TodoItemRec("uuid-list-02", "uuid-item-02-04",
				"Orange juice 2 boxes", false, 4, "");
		allWriteRequests.add(item2WriteRequest(item_02_04));
		
		try {
			BatchWriteItemRequest batchRequest = BatchWriteItemRequest.builder()
					.requestItems( Map.of(TABLE_NAME, allWriteRequests) )
					.build();
			BatchWriteItemResponse batchResponse = dynamoClient.batchWriteItem(batchRequest);
			System.out.println("Batch Write response: " + batchResponse);
			
		} catch(DynamoDbException ex) {
			System.err.println(ex);
		}
		
	}
	
	public void addTodoItems03() {
		
		List<WriteRequest> allWriteRequests = new ArrayList<>();
		
		var item_03_01 = new TodoItemRec("uuid-list-03", "uuid-item-03-01",
				"Robin and lorem ipsum dolor sit amet", false, 1, "");
		allWriteRequests.add(item2WriteRequest(item_03_01));
		
		var item_03_02 = new TodoItemRec("uuid-list-03", "uuid-item-03-02",
				"Lorem ipsum dolor sit amet is back", true, 2, "");
		allWriteRequests.add(item2WriteRequest(item_03_02));
		
		var item_03_03 = new TodoItemRec("uuid-list-03", "uuid-item-03-03",
				"Lorem ipsum dolor sit amet in vacation", true, 3, "");
		allWriteRequests.add(item2WriteRequest(item_03_03));
		
		var item_03_04 = new TodoItemRec("uuid-list-03", "uuid-item-03-04",
				"Superman versus lorem ipsum dolor sit amet", false, 4, "");
		allWriteRequests.add(item2WriteRequest(item_03_04));
		
		try {
			BatchWriteItemRequest batchRequest = BatchWriteItemRequest.builder()
					.requestItems( Map.of(TABLE_NAME, allWriteRequests) )
					.build();
			BatchWriteItemResponse batchResponse = dynamoClient.batchWriteItem(batchRequest);
			System.out.println("Batch Write response: " + batchResponse);
			
		} catch(DynamoDbException ex) {
			System.err.println(ex);
		}
		
	}
	
	public void getTodosByUser(String userEmail) {
		
		Map<String, AttributeValue> attrValues = Map.of(
				":UserEmail", AttributeValue.fromS(userEmail),
				":StartDate", AttributeValue.fromS("2024-01-01 00:00:00"));
		
		QueryRequest queryReq = QueryRequest.builder()
				.tableName(TABLE_NAME)
				.keyConditionExpression("PK = :UserEmail")
				.filterExpression("ListCreationDate > :StartDate")
				.expressionAttributeValues(attrValues)
				.build();
		
		try {
			QueryResponse response = dynamoClient.query(queryReq);
			System.out.println("");
			System.out.println("Found: " + response.count()  + " TODOs for User: " + userEmail);
			
			List<Map<String, AttributeValue>> items = response.items();
			items.forEach( System.out::println );
			
		} catch(DynamoDbException ex) {
			System.err.println(ex);
		}
		
	}
	
	public void getTodoItems(String listUuid) {
		
		QueryRequest queryReq = QueryRequest.builder()
				.tableName(TABLE_NAME)
				.keyConditionExpression("PK = :ListUuid")
				.expressionAttributeValues( Map.of(
						":ListUuid", AttributeValue.fromS(listUuid) ))
				.build();
		
		try {
			QueryResponse response = dynamoClient.query(queryReq);
			System.out.println("");
			System.out.println("Found: " + response.count()  + " TODOs for List: " + listUuid);
			
			List<Map<String, AttributeValue>> items = response.items();
			items.forEach( System.out::println );
			
		} catch(DynamoDbException ex) {
			System.err.println(ex);
		}
	}
	
	public void closeConnection() {
		dynamoClient.close();
	}
	
	public static void main(String[] args) {
		
		DynamoDbDemo dynamoDemo = new DynamoDbDemo();
		
		System.out.println("");
		
		//dynamoDemo.addAllTodoLists();
		
		//dynamoDemo.getTodosByUser("holteai@yahoo.com");
		
		//dynamoDemo.addTodoItems01();
		//dynamoDemo.addTodoItems02();
		//dynamoDemo.addTodoItems03();
		
		dynamoDemo.getTodoItems("uuid-list-02");
		System.out.println("");
		dynamoDemo.getTodoItems("uuid-list-03");
		
		dynamoDemo.closeConnection();
				
	}
}
