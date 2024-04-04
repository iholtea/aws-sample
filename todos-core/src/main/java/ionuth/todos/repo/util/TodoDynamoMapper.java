package ionuth.todos.repo.util;

import java.util.HashMap;
import java.util.Map;

import ionuth.todos.model.TodoItem;
import ionuth.todos.model.TodoList;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

/**
 * map Todo records to and from DynamoDb specific Attribute Value data structures
 */
public class TodoDynamoMapper {
	
	public TodoDynamoMapper() {}
	
	//TODO this should throw a DynamoMapException if user-email or list-uuid
	//or what we consider mandatory null / empty in the TodoList record
	public Map<String, AttributeValue> mapDynamodbFromList(TodoList todoList) {
		Map<String, AttributeValue> attrValues = new HashMap<>();
		attrValues.put("PK", AttributeValue.fromS(todoList.getUserEmail()));
		attrValues.put("SK",  AttributeValue.fromS(todoList.getUuid()));
		attrValues.put("ListTitle", AttributeValue.fromS(todoList.getTitle()));
		attrValues.put("ListCreationDate", AttributeValue.fromS(todoList.getCreationDate()));
		attrValues.put("ListLastViewDate", AttributeValue.fromS(todoList.getLastViewDate()));
		String listExtraInfo = todoList.getExtraInfo();
		if(listExtraInfo!=null) {
			attrValues.put("ListExtraInfo", AttributeValue.fromS(listExtraInfo));
		}
		return attrValues;
	}
	
	//TODO this should throw a DynamoMapException if list-uuid or item-uuid
	//or what we consider mandatory null / empty in the TodoList record
	public Map<String, AttributeValue> mapDynamodbFromItem(TodoItem item) {
		Map<String, AttributeValue> attrValues = new HashMap<>();
		attrValues.put("PK", AttributeValue.fromS(item.getListUuid()));
		attrValues.put("SK", AttributeValue.fromS(item.getUuid()));
		attrValues.put("ItemText", AttributeValue.fromS(item.getText()));
		attrValues.put("ItemDone", AttributeValue.fromBool(item.isDone()));
		attrValues.put("ItemOrderIdx", AttributeValue.fromN(String.valueOf(item.getOrderIdx())));
		String itemExtraInfo = item.getExtraInfo();
		if(itemExtraInfo!=null) {
			attrValues.put("ItemExtraInfo", AttributeValue.fromS(itemExtraInfo));
		}
		return attrValues;
	}
	
	public TodoList mapListFromDynamodb( Map<String, AttributeValue> dynamoObj ) {
		AttributeValue attrVal;
		TodoList todoList = new TodoList();
		todoList.setUuid( dynamoObj.get("SK").s() );
		todoList.setUserEmail( dynamoObj.get("PK").s() );
		todoList.setTitle( dynamoObj.get("ListTitle").s() );
		todoList.setCreationDate(dynamoObj.get("ListCreationDate").s());
		todoList.setLastViewDate(dynamoObj.get("ListLastViewDate").s());
		attrVal = dynamoObj.get("ListExtraInfo");
		if( attrVal != null ) todoList.setExtraInfo(attrVal.s());
		return todoList;
	}
	
	
	public TodoItem mapItemFromDynamodb( Map<String, AttributeValue> dynamoObj ) {
		AttributeValue attrVal;
		TodoItem todoItem = new TodoItem();
		todoItem.setUuid( dynamoObj.get("SK").s() );
		todoItem.setListUuid( dynamoObj.get("PK").s() );
		todoItem.setText( dynamoObj.get("ItemText").s() );
		todoItem.setDone( dynamoObj.get("ItemDone").bool() );
		todoItem.setOrderIdx( Integer.parseInt(dynamoObj.get("ItemOrderIdx").n()) );
		attrVal = dynamoObj.get("ItemExtraInfo");
		if( attrVal != null ) todoItem.setExtraInfo(attrVal.s());
		return todoItem;
	}
	
	
	
	
}
