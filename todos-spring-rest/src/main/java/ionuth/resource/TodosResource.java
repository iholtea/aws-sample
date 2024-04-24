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
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import ionuth.todos.model.TodoItem;
import ionuth.todos.model.TodoList;
import ionuth.todos.service.TodoService;
import jakarta.servlet.ServletContext;

@RestController
public class TodosResource {
	
	private final TodoService todoService;
	
	// constructor injection
	public TodosResource(TodoService todoService, ServletContext servletContext) {
		this.todoService = todoService;
	}
	
	@GetMapping("/todos")
	public List<TodoList> getAllListsByUser(@RequestAttribute("loginEmail") String loginEmail) {
		return todoService.getAllListsByUser(loginEmail);
	}
	
	@GetMapping("/todos/{uuid}")
	//TODO since we already have the TodoList details in the UI we could 
	//just request for all items of this list and use the cached infro
	//from the client to display TodoList title in the details view
	public TodoList getListById(@PathVariable("uuid") String uuid,
			@RequestAttribute("loginEmail") String loginEmail) {
		return todoService.getListById(uuid, loginEmail);
	}
	
	@PostMapping("/todos")
	public ResponseEntity<TodoList> addTodoList(@RequestBody TodoList todoList,
			@RequestAttribute("loginEmail") String loginEmail) {
		todoList.setUserEmail(loginEmail);
		TodoList savedList = todoService.addTodoList(todoList);
		// add /todos/{listUuid} as location header
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{listUuid}")
				.buildAndExpand(savedList.getUuid())
				.toUri();
		return ResponseEntity.created(location).body(savedList);
	}
	
	//@CrossOrigin
	@DeleteMapping("/todos/{listUuid}")
	public void deleteList(@PathVariable("listUuid") String listUuid,
			@RequestAttribute("loginEmail") String loginEmail) {
		todoService.deleteList(listUuid,loginEmail);
	}
	
	
	/*
	 * should return if the operation succeeded or failed.
	 * For example Using ResponseEntity and the appropriate HTTP status.
	 */
	//@CrossOrigin
	@DeleteMapping("todos/{listUuid}/items/{itemUuid}")
	public void deleteItem(@PathVariable("listUuid") String listUuid,
							@PathVariable("itemUuid") String itemUuid) {
		todoService.deleteItem(listUuid, itemUuid);
	}
	
	//@CrossOrigin
	@PutMapping("todos/{listUuid}/items/{itemUuid}")
	public TodoItem updateItem(@PathVariable("listUuid") String listUuid,
								@PathVariable("itemUuid") String itemUuid,
								@RequestBody TodoItem todoItem) {
		return todoService.updateItem(todoItem);
	}
	
	//@CrossOrigin
	@PostMapping("/todos/{listUuid}/items")
	public TodoItem createItem(@PathVariable("listUuid") String listUuid,
								@RequestBody TodoItem todoItem) {
		
		//TODO some authorization checks, like if the listUuid belongs to the logged user
		todoItem.setListUuid(listUuid);
		return todoService.createItem(todoItem);
	
	}
	
	
}
