package ionuth.todos.app;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import ionuth.todos.model.TodoItem;
import ionuth.todos.model.TodoList;
import ionuth.todos.repo.TodoRepository;
import ionuth.todos.repo.impl.file.local.TodoRepositoryFileLocal;

public class AppFileLocal {
	
	private static void createList01() {
		
		TodoRepository todoRepo = new TodoRepositoryFileLocal();
		
		TodoItem item1 = new TodoItem("uuid-item-01-01", "Buy travel insurance");
        TodoItem item2 = new TodoItem("uuid-item-01-02", "Create online account for Norway road tax");
        item2.setDone(true);
        TodoItem item3 = new TodoItem("uuid-item-01-03", "Buy travel insurance");
        
        TodoList todoList = new TodoList("uuid-list-01", "Norway road trip");
        todoList.getItems().add(item1);
        todoList.getItems().add(item2);
        todoList.getItems().add(item3);
        
        todoRepo.createList(todoList);
        
	}
	
	private static void createList02() {
		
		TodoRepository todoRepo = new TodoRepositoryFileLocal();
		
		TodoItem item1 = new TodoItem("uuid-item-02-01", "Oranges 2kg");
        TodoItem item2 = new TodoItem("uuid-item-02-02", "Pink lady apples 2 packs");
        item2.setDone(true);
        TodoItem item3 = new TodoItem("uuid-item-02-03", "Potatoes 3kg");
        item3.setDone(true);
        TodoItem item4 = new TodoItem("uuid-item-02-04", "Orange juice 2 boxes");
        
        TodoList todoList = new TodoList("uuid-list-02", "Supermarket shopping");
        todoList.getItems().add(item1);
        todoList.getItems().add(item2);
        todoList.getItems().add(item3);
        todoList.getItems().add(item4);
        
        todoRepo.createList(todoList);
        
	}
  

    private static void findTodoList() {
        TodoRepository todoRepo = new TodoRepositoryFileLocal();
        TodoList todoList = todoRepo.getListById("uuid-list-02");
        System.out.println("");
        System.out.println(todoList);
        System.out.println("");
    }
    
    private static void findTodoItem() {
        TodoRepository todoRepo = new TodoRepositoryFileLocal();
        TodoItem item = todoRepo.getItemById("uuid-item-01-02");
        System.out.println("");
        System.out.println(item);
        System.out.println("");
    }

    public static void main(String[] args) {
        
    	//createList01();
    	//createList02();
    	findTodoList();
    	findTodoItem();


    }
}
