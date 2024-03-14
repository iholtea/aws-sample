package ionuth.resource;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import ionuth.todos.model.TodoList;
import ionuth.todos.repo.TodoRepository;
import ionuth.util.SecurityHelper;

@RestController
public class TodosResource {
	
	private final TodoRepository todoRepo;
	private final String userEmail = SecurityHelper.getUserEmail();
	
	@Autowired
	public TodosResource(TodoRepository todoRepo) {
		this.todoRepo = todoRepo;
	}
	
	@GetMapping("/todos")
	public List<TodoList> getAllTodoLists() {
		return todoRepo.getListsByUserEmail(userEmail);
	}
	
	@GetMapping("/todos/{uuid}")
	public TodoList getTodoListById(@PathVariable("uuid") String uuid) {
		return todoRepo.getListById(uuid, userEmail);
	}
	
	@PostMapping("/todos")
	public TodoList addTodoList(@RequestBody TodoList todoList) {
		return todoRepo.createList( mapForNew(todoList) );
	}
	
	/*
	 * should return if the operation succeeded or failed.
	 * This could be done using with the HTTP status.
	 * Also, it could return the received parameters either in a 
	 * record( listUuid, itemUuid) or as a Map<String, String> 
	 * which AWS does to pass data to and from API Gateway
	 */
	@CrossOrigin
	@DeleteMapping("todos/{listUuid}/items/{itemUuid}")
	public void deleteItemById(@PathVariable("listUuid") String listUuid,
								@PathVariable("itemUuid") String itemUuid) {
		System.out.println("TodosResource: invoked deleteItem");
		todoRepo.deleteItem(listUuid, itemUuid);
	}
	
	private TodoList mapForNew(TodoList todoList) {
		
		System.out.println("");
		System.out.println("Received data: ");
		System.out.println(todoList);
		
		String listUUID = UUID.randomUUID().toString();
		todoList.setUuid(listUUID);
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String dateStr = dateFormat.format(new Date());
		todoList.setCreationDate(dateStr);
		todoList.setLastUpdate(dateStr);
		
		todoList.setUserEmail(userEmail);
		
		todoList.getItems().forEach( item -> {
			item.setUuid(UUID.randomUUID().toString());
		});
		
		System.out.println("");
		System.out.println("Transformed data: ");
		System.out.println(todoList);
		System.out.println("");
		return todoList;
	}
	
	
}
