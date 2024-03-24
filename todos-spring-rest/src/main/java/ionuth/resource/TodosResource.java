package ionuth.resource;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import ionuth.todos.model.TodoItem;
import ionuth.todos.model.TodoList;
import ionuth.todos.service.TodoService;

@RestController
public class TodosResource {
	
	private final TodoService todoService;
	
	// constructor injection
	public TodosResource(TodoService todoService) {
		this.todoService = todoService;
	}
	
	@GetMapping("/todos")
	public List<TodoList> getAllListsByUser() {
		return todoService.getAllListsByUser();
	}
	
	@GetMapping("/todos/{uuid}")
	public TodoList getListById(@PathVariable("uuid") String uuid) {
		return todoService.getListById(uuid);
	}
	
	@PostMapping("/todos")
	public TodoList addTodoList(@RequestBody TodoList todoList) {
		return todoService.addTodoList(todoList);
	}
	
	@CrossOrigin
	@DeleteMapping("/todos/{listUuid}")
	public void deleteList(@PathVariable("listUuid") String listUuid) {
		todoService.deleteList(listUuid);
	}
	
	
	/*
	 * should return if the operation succeeded or failed.
	 * For example Using ResponseEntity and the appropriate HTTP status.
	 */
	@CrossOrigin
	@DeleteMapping("todos/{listUuid}/items/{itemUuid}")
	public void deleteItem(@PathVariable("listUuid") String listUuid,
							@PathVariable("itemUuid") String itemUuid) {
		todoService.deleteItem(listUuid, itemUuid);
	}
	
	@CrossOrigin
	@PutMapping("todos/{listUuid}/items/{itemUuid}")
	public TodoItem updateItem(@PathVariable("listUuid") String listUuid,
								@PathVariable("itemUuid") String itemUuid,
								@RequestBody TodoItem todoItem) {
		return todoService.updateItem(todoItem);
	}
	
	@CrossOrigin
	@PostMapping("/todos/{listUuid}/items")
	public TodoItem createItem(@PathVariable("listUuid") String listUuid,
								@RequestBody TodoItem todoItem) {
		todoItem.setUuid(UUID.randomUUID().toString());
		return todoService.createItem(todoItem);
	}
	
	
}
