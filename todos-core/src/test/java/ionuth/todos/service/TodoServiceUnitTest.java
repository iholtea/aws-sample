package ionuth.todos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ionuth.todos.model.TodoItem;
import ionuth.todos.model.TodoList;
import ionuth.todos.repo.TodoRepository;

public class TodoServiceUnitTest {
	
	String userEmail = "test@test.com";
	
	TodoRepository todoRepo;
	TodoService underTest;
	 
	
	@BeforeEach
	public void init() {
		todoRepo = mock(TodoRepository.class);
		underTest = new TodoService(todoRepo);
	}
	
	@Test
	public void getAllListsByUser_test() {
		
		TodoList list1 = new TodoList();
		list1.setUuid("test-uuid-1");
		list1.setCreationDate("2024-02-02 04:04:04");
		
		TodoList list2 = new TodoList();
		list2.setUuid("test-uuid-2");
		list2.setCreationDate("2024-04-11 08:08:08");
		
		TodoList list3 = new TodoList();
		list3.setUuid("test-uuid-3");
		list3.setCreationDate("2024-02-02 11:11:11");
		
		when( todoRepo.getListsByUserEmail(anyString(), anyString()) )
				.thenReturn( Arrays.asList(list1, list2, list3) );
		
		List<TodoList> testTodos = underTest.getAllListsByUser(userEmail);
		
		assertNotNull(testTodos);
		assertEquals(3, testTodos.size());
		
		// TodoList-s should have inverse order by Creation Date
		assertEquals("test-uuid-2", testTodos.get(0).getUuid());
		assertEquals("test-uuid-3", testTodos.get(1).getUuid());
		assertEquals("test-uuid-1", testTodos.get(2).getUuid());
		
	}
	
	@Test
	public void getListById_test() {
		
		TodoItem item;
		
		TodoList todo = new TodoList();
		todo.setUuid("list-uuid");
		
		item = new TodoItem();
		item.setUuid("item-001");
		item.setOrderIdx(3);
		todo.getItems().add(item);
		
		item = new TodoItem();
		item.setUuid("item-002");
		item.setOrderIdx(1);
		todo.getItems().add(item);
		
		item = new TodoItem();
		item.setUuid("item-003");
		item.setOrderIdx(2);
		todo.getItems().add(item);
		
		
		when( todoRepo.getListById( anyString(), anyString()) ).thenReturn(todo);
		
		TodoList testTodo = underTest.getListById("list-uuid", userEmail);
		
		assertEquals(3, testTodo.getItems().size());
		assertEquals("item-002", testTodo.getItems().get(0).getUuid());
		assertEquals("item-003", testTodo.getItems().get(1).getUuid());
		assertEquals("item-001", testTodo.getItems().get(2).getUuid());
		
	}
	
}
