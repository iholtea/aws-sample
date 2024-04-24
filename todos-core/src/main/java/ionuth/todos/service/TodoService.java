package ionuth.todos.service;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import ionuth.todos.model.TodoItem;
import ionuth.todos.model.TodoList;
import ionuth.todos.repo.TodoRepository;

public class TodoService {
	
	// get only the TODOs last update after the reference date
	private static final String REFERENCE_DATE = "2024-01-01 00:00:00";
	
	private final TodoRepository todoRepo;
	
	
	// constructor dependency injection
	public TodoService(TodoRepository todoRepo) {
		this.todoRepo = todoRepo;
	}
	
	public List<TodoList> getAllListsByUser(String userEmail) {
		List<TodoList> todos = todoRepo.getListsByUserEmail(userEmail, REFERENCE_DATE);
		Collections.sort(todos, Comparator.comparing(TodoList::getCreationDate).reversed());
		return todos;
	}
	
	public TodoList getListById(String listUuid, String userEmail) {
		TodoList todoList = todoRepo.getListById(listUuid, userEmail);
		Collections.sort(todoList.getItems(), Comparator.comparing(TodoItem::getOrderIdx));
		return todoList;
	}
	
	public TodoList addTodoList(TodoList todoList) {
		return todoRepo.createList( mapTodoListForAdd(todoList) );
	}
	
	public void deleteList(String listUuid, String userEmail) {
		todoRepo.deleteList(listUuid, userEmail);
	}
	
	public void deleteItem(String listUuid, String itemUuid) {
		TodoItem item = new TodoItem();
		item.setListUuid(listUuid);
		item.setUuid(itemUuid);
		todoRepo.deleteItem(item);
	}
	
	public TodoItem updateItem(TodoItem todoItem) {
		return todoRepo.updateItem(todoItem);
	}
	
	public TodoItem createItem(TodoItem todoItem)  {
		todoItem.setUuid(UUID.randomUUID().toString());
		return todoRepo.createItem(todoItem);
	}
	
	private TodoList mapTodoListForAdd(TodoList todoList) {
		
		String listUUID = UUID.randomUUID().toString();
		todoList.setUuid(listUUID);
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateStr = dateFormat.format(new Date());
		todoList.setCreationDate(dateStr);
		todoList.setLastViewDate(dateStr);
		
		var items = todoList.getItems();
		for(int i=0; i<items.size(); i++ ) {
			items.get(i).setListUuid(listUUID);
			items.get(i).setUuid(UUID.randomUUID().toString());
			items.get(i).setOrderIdx(i+1);
		}
		
		return todoList;
	}
	
}
