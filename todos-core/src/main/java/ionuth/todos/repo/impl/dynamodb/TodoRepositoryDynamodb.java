package ionuth.todos.repo.impl.dynamodb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ionuth.todos.model.TodoItem;
import ionuth.todos.model.TodoList;
import ionuth.todos.repo.TodoRepository;
import ionuth.todos.repo.util.TodoDynamoMapper;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeAction;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.AttributeValueUpdate;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemResponse;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemResponse;
import software.amazon.awssdk.services.dynamodb.model.DeleteRequest;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.ReturnValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.WriteRequest;

public class TodoRepositoryDynamodb implements TodoRepository {
	
	private static final String TABLE_NAME = "Manual-Todos";
	
	private final DynamoDbClient dynamoClient;
	private final TodoDynamoMapper dynamoMapper;
	
	// constructor injection
	public TodoRepositoryDynamodb(DynamoDbClient dynamoClient, TodoDynamoMapper dynamoMapper) {
		this.dynamoClient = dynamoClient; 
		this.dynamoMapper = dynamoMapper;
	}
	
	@Override
	public TodoList createList(TodoList todoList) {
		
		List<WriteRequest> writeRequests = new ArrayList<>();
		Map<String, AttributeValue> todosAttrValues = dynamoMapper.mapDynamodbFromList(todoList);
		writeRequests.add( attrValues2WriteRequest(todosAttrValues) ) ;
		
		todoList.getItems().stream()
			.map( item -> {
				return attrValues2WriteRequest( dynamoMapper.mapDynamodbFromItem(item) );
			})
			.forEach( writeRequests::add );
		
		try {
			BatchWriteItemRequest batchRequest = BatchWriteItemRequest.builder()
					.requestItems( Map.of(TABLE_NAME, writeRequests) )
					.build();
			BatchWriteItemResponse batchResponse = dynamoClient.batchWriteItem(batchRequest);
			System.out.println("Batch Create TodoList response: " + batchResponse);
			
			
		} catch(DynamoDbException ex) {
			System.err.println(ex);
		}
		
		return todoList;
		
	}
	
	@Override
	public void deleteList(String listUuid, String userEmail) {
		
		List<WriteRequest> allRequests = new ArrayList<>();
		DeleteRequest delListReq = DeleteRequest.builder().key(
				Map.of( "PK", AttributeValue.fromS(userEmail),
						"SK", AttributeValue.fromS(listUuid))).build();
		allRequests.add( WriteRequest.builder().deleteRequest(delListReq).build());
		
		// in DynamoDB we if a table has both PK and SK we cannot delete using just the PK
		// so we cannot delete items using just ListUuid we need the ItemUuid also
		// so we need to fetch them first
		List<Map<String, AttributeValue>> selectedItems = getItemsByListIdInternal(listUuid);
		selectedItems.stream()
			.map( attrValMap -> { 
				DeleteRequest delItemReq = DeleteRequest.builder().key(
						Map.of("PK", attrValMap.get("PK"), 
								"SK", attrValMap.get("SK"))).build();
				return WriteRequest.builder().deleteRequest(delItemReq).build(); 
			})
			.forEach( allRequests::add );
		
		try {
			BatchWriteItemRequest batchRequest = BatchWriteItemRequest.builder()
					.requestItems( Map.of(TABLE_NAME, allRequests ))
					.build();
			BatchWriteItemResponse batchResponse = dynamoClient.batchWriteItem(batchRequest);
			System.out.println("Batch Delete TodoList response: " + batchResponse);
		} catch(DynamoDbException ex) {
			System.err.println(ex);
		}
		
	}
	
	@Override
	public List<TodoList> getListsByUserEmail(String userEmail, String startDate) {
		Map<String, AttributeValue> attrValues = Map.of(
				":UserEmail", AttributeValue.fromS(userEmail),
				":StartDate", AttributeValue.fromS(startDate));
		
		QueryRequest queryReq = QueryRequest.builder()
				.tableName(TABLE_NAME)
				.keyConditionExpression("PK = :UserEmail")
				.filterExpression("ListCreationDate > :StartDate")
				.expressionAttributeValues(attrValues)
				.build();
		
		try {
			QueryResponse response = dynamoClient.query(queryReq);
			System.out.println("TodoRepositoryDynamodb: Found: " + response.count()  + " TODOs for User: " + userEmail);
			if( response.count() > 0 ) {
				List<Map<String, AttributeValue>> items = response.items();
				return items.stream().map(dynamoMapper::mapListFromDynamodb).collect(Collectors.toList());
			} else {
				return Collections.emptyList();
			}
		} catch(DynamoDbException ex) {
			System.err.println(ex);
			//TODO throw custom exception
			throw ex;
		}
	}
	
	
	@Override
	public TodoList getListById(String listUuid, String userEmail) {
		
		TodoList todoList = getListByIdNoItems(listUuid, userEmail);
		
		List<TodoItem> items = getItemsByListId(listUuid);
		if(!items.isEmpty()) {
			todoList.setItems(items);
		}
			
		return todoList;
	}
	
	
	private TodoList getListByIdNoItems(String listUuid, String userEmail) {
		
		TodoList todoList = null;
		
		QueryRequest queryReq = QueryRequest.builder()
				.tableName(TABLE_NAME)
				.keyConditionExpression("PK = :UserEmail AND SK = :ListUuid")
				.expressionAttributeValues( Map.of(
						":UserEmail", AttributeValue.fromS(userEmail), 
						":ListUuid", AttributeValue.fromS(listUuid) ))
				.build();
		
		try {
			QueryResponse response = dynamoClient.query(queryReq);
			if(response.hasItems()) {
				todoList = dynamoMapper.mapListFromDynamodb( response.items().get(0) );
			} else {
				//TODO throw a custom DataNotFound exception
				throw new RuntimeException("Todo List not found with UUID: " + listUuid);
			}
		} catch(DynamoDbException ex) {
			//TODO throw custom exception
			System.err.println(ex);
			throw ex;
		}
		
		return todoList;
	}
	
	private List<TodoItem> getItemsByListId(String listUuid) {
		return getItemsByListIdInternal(listUuid).stream()
				.map(dynamoMapper::mapItemFromDynamodb)
				.collect(Collectors.toList());
	}
	
	private List<Map<String, AttributeValue>> getItemsByListIdInternal(String listUuid) {
		QueryRequest queryReq = QueryRequest.builder()
				.tableName(TABLE_NAME)
				.keyConditionExpression("PK = :ListUuid")
				.expressionAttributeValues( Map.of(
						":ListUuid", AttributeValue.fromS(listUuid) ))
				.build();
		
		try {
			QueryResponse response = dynamoClient.query(queryReq);
			System.out.println("Found: " + response.count()  + " Items for List: " + listUuid);
			if(response.count() > 0) {
				return response.items();
			} else {
				return Collections.emptyList();
			}
			
		} catch(DynamoDbException ex) {
			//TODO throw custom exception
			System.err.println(ex);
			throw ex;
		}
	}
	
	

	@Override
	public TodoItem updateItem(TodoItem item) {
				
		Map<String, AttributeValue> keyMap = new HashMap<>();
		keyMap.put("PK", AttributeValue.fromS(item.getListUuid()));
		keyMap.put("SK", AttributeValue.fromS(item.getUuid()) );
		
		Map<String, AttributeValueUpdate> updateMap = new HashMap<>();
		updateMap.put("ItemDone", AttributeValueUpdate.builder()
				.value(AttributeValue.fromBool(item.isDone()))
				.action(AttributeAction.PUT)
				.build() );
		
		if(item.getText()!=null) {
			updateMap.put("ItemText", AttributeValueUpdate.builder()
					.value(AttributeValue.fromS(item.getText()))
					.action(AttributeAction.PUT)
					.build() );
		}
		
		UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(keyMap)
                .attributeUpdates(updateMap)
                .build();

        try {
            dynamoClient.updateItem(request);
        } catch (DynamoDbException ex) {
        	//TODO throw custom exception
			System.err.println(ex);
			throw ex;
        }
        
        return item;
		
	}
	
	@Override
	public TodoItem createItem(TodoItem item) {
		
		Map<String, AttributeValue> attrValues = dynamoMapper.mapDynamodbFromItem(item);
		PutItemRequest request = PutItemRequest.builder()
	                		.tableName(TABLE_NAME)
	                		.item(attrValues)
	                		.build();
        try {
            PutItemResponse response = dynamoClient.putItem(request);
            System.out.println(TABLE_NAME + " was successfully updated. The request id is "
                    + response.responseMetadata().requestId() );
        } catch (DynamoDbException ex) {
        	//TODO throw custom exception
			System.err.println(ex);
			throw ex;
        }
        return item;
		
	}
	
	@Override
	public void deleteItem(TodoItem item) {
		
		Map<String, AttributeValue> keyMap = Map.of(
					"PK", AttributeValue.fromS(item.getListUuid()),
					"SK", AttributeValue.fromS(item.getUuid()) );
		
		DeleteItemRequest deleteReq = DeleteItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(keyMap)
                .returnValues(ReturnValue.ALL_OLD)
                .build();
		
		try {
			DeleteItemResponse deleteResp = dynamoClient.deleteItem(deleteReq);
			System.out.println("Deleted items: " + deleteResp.attributes());
		} catch (DynamoDbException ex) {
        	System.err.println(ex);
			//TODO throw custom exception
			throw ex;
        }
	}
	
	private WriteRequest attrValues2WriteRequest(Map<String, AttributeValue> attrValues) {
		PutRequest putReq = PutRequest.builder().item(attrValues).build();
		return WriteRequest.builder().putRequest(putReq).build();
	}
	
}
