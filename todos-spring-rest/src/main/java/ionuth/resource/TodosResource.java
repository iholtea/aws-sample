package ionuth.resource;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
	
}
