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
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemResponse;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemResponse;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.PutRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.ReturnValue;
import software.amazon.awssdk.services.dynamodb.model.WriteRequest;

public class TodoRepositoryDynamodb implements TodoRepository {
	
	private static final String LIST_TABLE_NAME = "Manual-Todo-List";
	private static final String ITEM_TABLE_NAME = "Manual-Todo-Item";
	
	// get only the TODOs last update after the reference date
	private static final String REFERENCE_DATE = "2024-01-01";
	
	private final DynamoDbClient dynamoClient;
	
	public TodoRepositoryDynamodb() {
		dynamoClient = DynamoDbClient.builder()
				.region(Region.US_EAST_1)
		        .build();
	}
	
	@Override
	public TodoList createList(TodoList todoList) {
		
		List<WriteRequest> todosWriteReq = new ArrayList<>();
		Map<String, AttributeValue> todosAttrValues = mapDynamodbFromList(todoList);
		todosWriteReq.add( attrValues2WriteRequest(todosAttrValues) ) ;
		
		List<WriteRequest> itemsWriteReq = todoList.getItems().stream()
				.map( item -> {
					return attrValues2WriteRequest( mapDynamodbFromItem(item, todoList) );
				})
				.collect(Collectors.toList()) ;
		
		try {
			BatchWriteItemRequest batchRequest = BatchWriteItemRequest.builder()
					.requestItems( Map.of(
							LIST_TABLE_NAME, todosWriteReq,
							ITEM_TABLE_NAME, itemsWriteReq ))
					.build();
			BatchWriteItemResponse batchResponse = dynamoClient.batchWriteItem(batchRequest);
			System.out.println("Batch Write response: " + batchResponse);
			
		} catch(DynamoDbException ex) {
			System.err.println(ex);
		}
		
		return todoList;
		
	}
	
	@Override
	public List<TodoList> getListsByUserEmail(String userEmail) {
		Map<String, AttributeValue> attrValues = Map.of(
				":UserEmail", AttributeValue.fromS(userEmail),
				":StartDate", AttributeValue.fromS(REFERENCE_DATE));
		
		QueryRequest queryReq = QueryRequest.builder()
				.tableName(LIST_TABLE_NAME)
				.keyConditionExpression("UserEmail = :UserEmail")
				.filterExpression("ListLastUpdate > :StartDate")
				.expressionAttributeValues(attrValues)
				.build();
		
		try {
			QueryResponse response = dynamoClient.query(queryReq);
			System.out.println("TodoRepositoryDynamodb: Found: " + response.count()  + " TODOs for User: " + userEmail);
			if( response.count() > 0 ) {
				List<Map<String, AttributeValue>> items = response.items();
				return items.stream().map(this::mapListFromDynamodb).collect(Collectors.toList());
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
	//TODO since we already have the Title list in the ITEMS table
	//we could just get all the Items by List UUID. 
	//problem is that if there are none, we will not have the Title list
	//we could keep it either on client or server outside of the DB repository
	public TodoList getListById(String listUuid, String userEmail) {
		
		TodoList todoList = null;
		
		QueryRequest queryReq = QueryRequest.builder()
				.tableName(LIST_TABLE_NAME)
				.keyConditionExpression("UserEmail = :UserEmail AND ListUuid = :ListUuid")
				.expressionAttributeValues( Map.of(
						":UserEmail", AttributeValue.fromS(userEmail), 
						":ListUuid", AttributeValue.fromS(listUuid) ))
				.build();
		
		try {
			QueryResponse response = dynamoClient.query(queryReq);
			if(response.hasItems()) {
				todoList = mapListFromDynamodb( response.items().get(0) );
			} else {
				//TODO throw a custom DataNotFound exception
				throw new RuntimeException("Todo List not found with UUID: " + listUuid);
			}
		} catch(DynamoDbException ex) {
			//TODO throw custom exception
			System.err.println(ex);
			throw ex;
		}
		
		List<TodoItem> items = getItemsByListId(listUuid);
		if(!items.isEmpty()) {
			todoList.setItems(items);
		}
			
		return todoList;
	}
	
	private List<TodoItem> getItemsByListId(String listUuid) {
		
		QueryRequest queryReq = QueryRequest.builder()
				.tableName(ITEM_TABLE_NAME)
				.keyConditionExpression("ListUuid = :ListUuid")
				.expressionAttributeValues( Map.of(
						":ListUuid", AttributeValue.fromS(listUuid) ))
				.build();
		
		try {
			QueryResponse response = dynamoClient.query(queryReq);
			System.out.println("Found: " + response.count()  + " Items for List: " + listUuid);
			if(response.count() > 0) {
				List<Map<String, AttributeValue>> items = response.items();
				return items.stream().map(this::mapItemFromDynamodb).collect(Collectors.toList());
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
	public TodoItem getItemById(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateItem(TodoItem item) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void deleteItem(String listUuid, String itemUuid) {
		
		Map<String, AttributeValue> keyMap = Map.of(
					"ListUuid", AttributeValue.fromS(listUuid),
					"ItemUuid", AttributeValue.fromS(itemUuid) );
		
		DeleteItemRequest deleteReq = DeleteItemRequest.builder()
                .tableName(ITEM_TABLE_NAME)
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
	
	private TodoList mapListFromDynamodb( Map<String, AttributeValue> dynamoObj ) {
		AttributeValue attrVal;
		TodoList todoList = new TodoList();
		todoList.setUuid( dynamoObj.get("ListUuid").s() );
		todoList.setUserEmail( dynamoObj.get("UserEmail").s() );
		todoList.setTitle( dynamoObj.get("ListTitle").s() );
		attrVal = dynamoObj.get("ListCreationDate");
		if( attrVal != null ) todoList.setCreationDate(attrVal.s());
		attrVal = dynamoObj.get("ListLastUpdate");
		if( attrVal != null ) todoList.setLastUpdate(attrVal.s());
		attrVal = dynamoObj.get("ListExtraInfo");
		if( attrVal != null ) todoList.setExtraInfo(attrVal.s());
		return todoList;
	}
	
	private TodoItem mapItemFromDynamodb( Map<String, AttributeValue> dynamoObj ) {
		AttributeValue attrVal;
		TodoItem todoItem = new TodoItem();
		todoItem.setUuid( dynamoObj.get("ItemUuid").s() );
		todoItem.setText( dynamoObj.get("ItemText").s() );
		todoItem.setDone( dynamoObj.get("ItemDone").bool() );
		attrVal = dynamoObj.get("ListExtraInfo");
		if( attrVal != null ) todoItem.setExtraInfo(attrVal.s());
		return todoItem;
	}
	
	private Map<String, AttributeValue> mapDynamodbFromItem(TodoItem item, TodoList todoList) {
		Map<String, AttributeValue> attrValues = new HashMap<>();
		attrValues.put("ListUuid", AttributeValue.fromS(todoList.getUuid()));
		attrValues.put("ItemUuid", AttributeValue.fromS(item.getUuid()));
		attrValues.put("ListTitle", AttributeValue.fromS(todoList.getTitle()));
		attrValues.put("ItemText", AttributeValue.fromS(item.getText()));
		attrValues.put("ItemDone", AttributeValue.fromBool(item.isDone()));
		String itemExtraInfo = item.getExtraInfo();
		if(itemExtraInfo!=null) {
			attrValues.put("ItemExtraInfo", AttributeValue.fromS(itemExtraInfo));
		}
		return attrValues;
	}
	
	private Map<String, AttributeValue> mapDynamodbFromList(TodoList todoList) {
		Map<String, AttributeValue> attrValues = new HashMap<>();
		attrValues.put("UserEmail", AttributeValue.fromS(todoList.getUserEmail()));
		attrValues.put("ListUuid",  AttributeValue.fromS(todoList.getUuid()));
		attrValues.put("ListTitle", AttributeValue.fromS(todoList.getTitle()));
		attrValues.put("ListCreationDate", AttributeValue.fromS(todoList.getCreationDate()));
		attrValues.put("ListLastUpdate", AttributeValue.fromS(todoList.getLastUpdate()));
		String listExtraInfo = todoList.getExtraInfo();
		if(listExtraInfo!=null) {
			attrValues.put("ListExtraInfo", AttributeValue.fromS(listExtraInfo));
		}
		return attrValues;
	}
	
	private WriteRequest attrValues2WriteRequest(Map<String, AttributeValue> attrValues) {
		PutRequest putReq = PutRequest.builder().item(attrValues).build();
		return WriteRequest.builder().putRequest(putReq).build();
	}
	
	public static void main(String[] args) {
		
		TodoRepository todoRepo = new TodoRepositoryDynamodb();
		
		List<TodoList> todoLists = todoRepo.getListsByUserEmail("holteai@yahoo.com");
		System.out.println();
		System.out.println(todoLists);
		
		TodoList todoList = todoRepo.getListById("uuid-list-02", "holteai@yahoo.com");
		System.out.println();
		System.out.println(todoList);
		
	}

}
