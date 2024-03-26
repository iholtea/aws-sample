package ionuth.todos.repo;

import java.util.List;

import ionuth.todos.model.TodoItem;
import ionuth.todos.model.TodoList;

public interface TodoRepository {
	
	List<TodoList> getListsByUserEmail(String userEmail);
	
    TodoList getListById(String listUuid, String userEmail);

    TodoList createList(TodoList todoList);
    
    void deleteList(String listUuid, String userEmail); 

    
    TodoItem updateItem(TodoItem item);
    
    TodoItem createItem(TodoItem item);
    
    void deleteItem(TodoItem item);
    
    
}
