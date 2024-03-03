package ionuth.resource;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import ionuth.todos.model.TodoList;
import ionuth.todos.repo.TodoRepository;

@RestController
public class TodosResource {
	
	private final TodoRepository todoRepo;
	
	@Autowired
	public TodosResource(TodoRepository todoRepo) {
		this.todoRepo = todoRepo;
	}
	
	@GetMapping("/todos")
	public List<TodoList> getAllTodoLists() {
		return todoRepo.getAllLists();
	}
	
	@GetMapping("/todos/{uuid}")
	public TodoList getTodoListById(@PathVariable("uuid") String uuid) {
		return todoRepo.getListById(uuid);
	}
	
}
