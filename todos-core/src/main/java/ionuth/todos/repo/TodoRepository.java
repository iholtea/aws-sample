package ionuth.todos.repo;

import java.util.List;

import ionuth.todos.model.TodoItem;
import ionuth.todos.model.TodoList;

public interface TodoRepository {
	
	/*
     * userEmail is the Partition Key for TodoList-s
     * Or maybe we should add to the Data Model TodoList as property
     * and use a different DTO TodoListDto without it to send to the client
     */
    
	List<TodoList> getListsByUserEmail(String userEmail);
	
    TodoList getListById(String listUuid, String userEmail);

    TodoList createList(TodoList todoList);
    
    void deleteList(String listUuid, String userEmail); 

    
    TodoItem updateItem(TodoItem item);
    
    TodoItem createItem(TodoItem item, String userEmail);
    
    void deleteItem(TodoItem item);
    
    
}
