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
	private final SecurityService securityService;
	
	// constructor dependency injection
	public TodoService(TodoRepository todoRepo, SecurityService securityService) {
		this.todoRepo = todoRepo;
		this.securityService = securityService;
	}
	
	public List<TodoList> getAllListsByUser() {
		List<TodoList> todos = todoRepo.getListsByUserEmail(securityService.getUserEmail(), REFERENCE_DATE);
		Collections.sort(todos, Comparator.comparing(TodoList::getCreationDate).reversed());
		return todos;
	}
	
	public TodoList getListById(String listUuid) {
		TodoList todoList = todoRepo.getListById(listUuid, securityService.getUserEmail());
		Collections.sort(todoList.getItems(), Comparator.comparing(TodoItem::getOrderIdx));
		return todoList;
	}
	
	public TodoList addTodoList(TodoList todoList) {
		return todoRepo.createList( mapTodoListForAdd(todoList) );
	}
	
	public void deleteList(String listUuid) {
		todoRepo.deleteList(listUuid, securityService.getUserEmail());
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
		
		todoList.setUserEmail(securityService.getUserEmail());
		
		var items = todoList.getItems();
		for(int i=0; i<items.size(); i++ ) {
			items.get(i).setListUuid(listUUID);
			items.get(i).setUuid(UUID.randomUUID().toString());
			items.get(i).setOrderIdx(i+1);
		}
		
		System.out.println("TodoService: adding TodoList: " + todoList);
		
		return todoList;
	}
	
}
