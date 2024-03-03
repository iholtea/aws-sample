package ionuth.todos.app;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import ionuth.todos.model.TodoItem;
import ionuth.todos.model.TodoList;
import ionuth.todos.repo.TodoRepository;
import ionuth.todos.repo.impl.file.local.TodoRepositoryFileLocal;

public class AppFileLocal {

    private static void createLists() {
        TodoRepository todoRepo = new TodoRepositoryFileLocal();

        String itemUUID1 = UUID.randomUUID().toString();
        String itemUUID2 = UUID.randomUUID().toString();
        String todoUUID = UUID.randomUUID().toString();

        TodoItem item1 = new TodoItem(itemUUID1, "Plateste internet", "", false);
        TodoItem item2 = new TodoItem(itemUUID2, "Programare masina", "", false);
        List<TodoItem> items = Arrays.asList(item1, item2);

        String todoTitle = "TODO next week";
        TodoList todoList = new TodoList(todoUUID, todoTitle, items);

        todoRepo.createList(todoList);  
    }

    private static void findTodoList() {
        TodoRepository todoRepo = new TodoRepositoryFileLocal();
        TodoList todoList = todoRepo.getListById("993c94c4-40dc-45dd-b222-bbfd2041fa0e");
        System.out.println("");
        System.out.println(todoList);
        System.out.println("");
    }
    
    private static void findTodoItem() {
        TodoRepository todoRepo = new TodoRepositoryFileLocal();
        TodoItem item = todoRepo.getItemById("d73d141b-ae94-4beb-82f7-272f51ed47c5");
        System.out.println("");
        System.out.println(item);
        System.out.println("");
    }

    public static void main(String[] args) {
        
    	//createLists();
    	findTodoList();
    	//findTodoItem();


    }
}
