package ionuth.todos.repo;

import java.util.List;

import ionuth.todos.model.TodoItem;
import ionuth.todos.model.TodoList;

public interface TodoRepository {
	
	List<TodoList> getListsByUserEmail(String userEmail);
	
    /*
     * userEmail is needed for DynamoDB implementation optimization
     * as in TodoList table the UserEmail is a Partition Key.
     * 
     * If not we would need to either scan the TodoList table or create an
     * alternate index for the List UUID
     * 
     * For a relational DB where List ID would be Primary key the 
     * userEmail parameter may be ignored
     */
    TodoList getListById(String listUuid, String userEmail);

    TodoList createList(TodoList todoList);

    TodoItem getItemById(String id);
 
    void updateItem(TodoItem item);
    
    void deleteItem(String listUuid, String itemUuid);

}
