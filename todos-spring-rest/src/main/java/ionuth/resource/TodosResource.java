package ionuth.resource;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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
	//TODO since we already have the TodoList details in the UI we could 
	//just request for all items of this list and use the cached infro
	//from the client to display TodoList title in the details view
	public TodoList getListById(@PathVariable("uuid") String uuid) {
		return todoService.getListById(uuid);
	}
	
	@PostMapping("/todos")
	public ResponseEntity<TodoList> addTodoList(@RequestBody TodoList todoList) {
		TodoList savedList = todoService.addTodoList(todoList);
		// add /todos/{listUuid} as location header
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{listUuid}")
				.buildAndExpand(savedList.getUuid())
				.toUri();
		return ResponseEntity.created(location).body(savedList);
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
		
		// or better call the TodoService with the listUuid
		// it should be the Service responsibility to fill the data to be sent to the Repository
		// and maybe do some authorization checks, like if the listUuid belongs to the logged user
		todoItem.setListUuid(listUuid);
		return todoService.createItem(todoItem);
	
	}
	
	
}
