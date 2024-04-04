package ionuth.todos.repo.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ionuth.todos.model.TodoItem;
import ionuth.todos.model.TodoList;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class TodoDynamoMapperUnitTest {
	
	@Test
	public void list_from_dynamo_all_attributes() {
		
		TodoDynamoMapper underTest = new TodoDynamoMapper();
		
		Map<String, AttributeValue> attrValues = new HashMap<>();
		attrValues.put("PK", AttributeValue.fromS("test@email.com"));
		attrValues.put("SK", AttributeValue.fromS("todo-uuid"));
		attrValues.put("ListTitle", AttributeValue.fromS("todo title"));
		attrValues.put("ListCreationDate", AttributeValue.fromS("creation date"));
		attrValues.put("ListLastViewDate", AttributeValue.fromS("last-view-date"));
		
		TodoList todoList = underTest.mapListFromDynamodb(attrValues);
		
		assertNotNull(todoList);
		assertEquals("test@email.com", todoList.getUserEmail());
		assertEquals("todo-uuid", todoList.getUuid());
		assertEquals("todo title", todoList.getTitle());
		assertEquals("creation date", todoList.getCreationDate());
		assertEquals("last-view-date", todoList.getLastViewDate());
		assertNull(todoList.getExtraInfo());
		
		attrValues.put("ListExtraInfo", AttributeValue.fromS("xtra-info"));
		todoList = underTest.mapListFromDynamodb(attrValues);
		assertEquals("xtra-info", todoList.getExtraInfo());
		
	}
	
	@Test
	public void list_from_dynamo_npe_missing_required_attribute() {
		
		TodoDynamoMapper underTest = new TodoDynamoMapper();
		
		Map<String, AttributeValue> attrValues = new HashMap<>();
		attrValues.put("PK", AttributeValue.fromS("test@email.com"));
		attrValues.put("SK", AttributeValue.fromS("todo-uuid"));
		
		// missing Todo Title
		assertThrows(NullPointerException.class, 
				() -> underTest.mapListFromDynamodb(attrValues));
	}
	
	@Test
	public void dynamo_from_list_all_attributes() {
		
		TodoList todoList = new TodoList();
		todoList.setUserEmail("test@email.com");
		todoList.setUuid("list-uuid");
		todoList.setTitle("list-title");
		todoList.setCreationDate("creation date");
		todoList.setLastViewDate("last-view-date");
		todoList.setExtraInfo("xtra-info");
		
		TodoDynamoMapper underTest = new TodoDynamoMapper();
		
		var attrValues = underTest.mapDynamodbFromList(todoList);
		
		assertEquals("test@email.com", attrValues.get("PK").s());
		assertEquals("list-uuid", attrValues.get("SK").s());
		assertEquals("list-title", attrValues.get("ListTitle").s());
		assertEquals("creation date", attrValues.get("ListCreationDate").s());
		assertEquals("last-view-date", attrValues.get("ListLastViewDate").s());
		assertEquals("xtra-info", attrValues.get("ListExtraInfo").s());
		
		todoList.setExtraInfo(null);
		attrValues = underTest.mapDynamodbFromList(todoList);
		assertFalse( attrValues.containsKey("ListExtraInfo") );
		
	}
	
	@Test
	public void dynamo_from_list_null_attribute() {
		
		TodoList todoList = new TodoList();
		todoList.setUserEmail("test@email.com");
		todoList.setUuid("list-uuid");
		
		TodoDynamoMapper underTest = new TodoDynamoMapper();
		
		var attrValues = underTest.mapDynamodbFromList(todoList);
		
		assertEquals("test@email.com", attrValues.get("PK").s());
		assertEquals("list-uuid", attrValues.get("SK").s());
		assertTrue( attrValues.containsKey("ListTitle") );
		assertNull(attrValues.get("ListTitle").s());
		
	}
	
	@Test
	public void item_from_dynamo_all_attributes() {
		
		Map<String, AttributeValue> attrValues = new HashMap<>();
		attrValues.put("PK", AttributeValue.fromS("list-uuid"));
		attrValues.put("SK", AttributeValue.fromS("item-uuid"));
		attrValues.put("ItemText", AttributeValue.fromS("item-text"));
		attrValues.put("ItemDone", AttributeValue.fromBool(true));
		attrValues.put("ItemOrderIdx", AttributeValue.fromN(String.valueOf(3)));
		
		TodoDynamoMapper underTest = new TodoDynamoMapper();
		TodoItem item = underTest.mapItemFromDynamodb(attrValues);
		
		assertNotNull(item);
		assertEquals("list-uuid", item.getListUuid());
		assertEquals("item-uuid", item.getUuid());
		assertEquals("item-text", item.getText());
		assertTrue(item.isDone());
		assertEquals(3, item.getOrderIdx());
		assertNull(item.getExtraInfo());
		
		attrValues.put("ItemExtraInfo", AttributeValue.fromS("item-extra"));
		item = underTest.mapItemFromDynamodb(attrValues);
		assertEquals("item-extra", item.getExtraInfo());
	}
	
	@Test
	public void item_from_dynamo_missing_required_attribute() {
		
		TodoDynamoMapper underTest = new TodoDynamoMapper();
		Map<String, AttributeValue> attrValues = new HashMap<>();
		attrValues.put("PK", AttributeValue.fromS("list-uuid"));
		attrValues.put("SK", AttributeValue.fromS("item-uuid"));
		
		// missing Item Text
		assertThrows(NullPointerException.class, 
				() -> underTest.mapItemFromDynamodb(attrValues));
	}
	
	@Test
	public void dynamo_from_item_all_attributes() {
		
		TodoDynamoMapper underTest = new TodoDynamoMapper();
		
		TodoItem item = new TodoItem();
		item.setListUuid("list-uuid");
		item.setUuid("item-uuid");
		item.setText("item-text");
		item.setDone(true);
		item.setOrderIdx(3);
		item.setExtraInfo("xtra-info");
		
		var attrValues = underTest.mapDynamodbFromItem(item);
		
		assertEquals("list-uuid", attrValues.get("PK").s());
		assertEquals("item-uuid", attrValues.get("SK").s());
		assertEquals("item-text", attrValues.get("ItemText").s());
		assertTrue(attrValues.get("ItemDone").bool());
		assertEquals(3, Integer.parseInt(attrValues.get("ItemOrderIdx").n()));
		assertEquals("xtra-info", attrValues.get("ItemExtraInfo").s());
		
		item.setExtraInfo(null);
		attrValues = underTest.mapDynamodbFromItem(item);
		assertFalse( attrValues.containsKey("ItemExtraInfo") );
		
	}
	
	@Test
	public void dynamo_from_item_null_attribute() {
		
		
		TodoItem item = new TodoItem();
		item.setListUuid("list-uuid");
		item.setUuid("item-uuid");
		
		TodoDynamoMapper underTest = new TodoDynamoMapper();
		
		var attrValues = underTest.mapDynamodbFromItem(item);
		
		assertEquals("list-uuid", attrValues.get("PK").s());
		assertEquals("item-uuid", attrValues.get("SK").s());
		assertTrue( attrValues.containsKey("ItemText") );
		assertNull(attrValues.get("ItemText").s());
		
	}
	
}
