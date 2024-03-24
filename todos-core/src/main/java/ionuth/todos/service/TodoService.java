package ionuth.todos.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import ionuth.todos.model.TodoItem;
import ionuth.todos.model.TodoList;
import ionuth.todos.repo.TodoRepository;

public class TodoService {
	
	private final TodoRepository todoRepo;
	private final SecurityService securityService;
	
	// constructor dependency injection
	public TodoService(TodoRepository todoRepo, SecurityService securityService) {
		this.todoRepo = todoRepo;
		this.securityService = securityService;
	}
	
	public List<TodoList> getAllListsByUser() {
		return todoRepo.getListsByUserEmail(securityService.getUserEmail());
	}
	
	public TodoList getListById(String listUuid) {
		return todoRepo.getListById(listUuid, securityService.getUserEmail());
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
		return todoRepo.createItem(todoItem, securityService.getUserEmail());
	}
	
	private TodoList mapTodoListForAdd(TodoList todoList) {
		
		String listUUID = UUID.randomUUID().toString();
		todoList.setUuid(listUUID);
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String dateStr = dateFormat.format(new Date());
		todoList.setCreationDate(dateStr);
		todoList.setLastUpdate(dateStr);
		
		todoList.setUserEmail(securityService.getUserEmail());
		
		todoList.getItems().forEach( item -> {
			item.setUuid(UUID.randomUUID().toString());
		});
		
		return todoList;
	}
	
}
