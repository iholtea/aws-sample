package ionuth.test.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TodoDemo {


    private static List<TodoList> createTodoList() {

        String itemUUID1 = UUID.randomUUID().toString();
        String itemUUID2 = UUID.randomUUID().toString();
        String todoUUID = UUID.randomUUID().toString();;

        TodoItem item1 = new TodoItem(itemUUID1, "Plateste internet", "", false);
        TodoItem item2 = new TodoItem(itemUUID2, "Programare masina", "", false);
        List<TodoItem> items = Arrays.asList(item1, item2);

        String todoTitle = "TODO next week";
        return Arrays.asList(new TodoList(todoUUID, todoTitle, items));

    }

    public static void main(String[] args) {

       try {
            
          ObjectMapper objectMapper = new ObjectMapper();
          
          List<TodoList> todoLists = createTodoList();
          String listsStr = objectMapper.writeValueAsString(todoLists);
          
          System.out.println("");
          System.out.println("Created TODO LIST serialized as JSON:");
          System.out.println( todoLists ); 
          System.out.println("");
          
          List<TodoList> deserializedLists = objectMapper.readValue(listsStr,
                    new TypeReference<List<TodoList>>(){});
          System.out.println("");
          System.out.println("Todo Lists deserialized from JSON:");
          System.out.println(deserializedLists);
          System.out.println("");


        } catch( JsonProcessingException ex ) {
            System.out.println(ex.toString());
        }

    }

}
