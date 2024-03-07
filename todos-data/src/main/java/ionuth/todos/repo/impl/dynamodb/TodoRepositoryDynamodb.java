package ionuth.todos.repo.impl.dynamodb;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ionuth.todos.model.TodoItem;
import ionuth.todos.model.TodoList;
import ionuth.todos.repo.TodoRepository;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

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
	public void createList(TodoList todoList) {
		// TODO Auto-generated method stub
		
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
