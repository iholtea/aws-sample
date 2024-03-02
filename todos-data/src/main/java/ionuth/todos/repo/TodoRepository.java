package ionuth.todos.repo;

import java.util.List;

import ionuth.todos.model.TodoItem;
import ionuth.todos.model.TodoList;

public interface TodoRepository {

    void createList(TodoList todoList);

    TodoList getListById(String id);

    List<TodoList> getAllLists();

    TodoItem getItemById(String id);
 
    void updateItem(TodoItem item);

}
