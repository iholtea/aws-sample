package ionuth.todos.repo.impl.file.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ionuth.todos.model.TodoItem;
import ionuth.todos.model.TodoList;
import ionuth.todos.repo.TodoRepository;

public class TodoRepositoryFileLocal implements TodoRepository {

    private static final String APP_CONFIG_FILE = "app.properties";
    private static final String JSON_DATA_FILE_KEY = "todos.json.location";

    private ObjectMapper objectMapper;
    private File dataFile;

    /*
    URL currentURL = getClass().getClassLoader().getResource(".");
    Path currentPath = Paths.get(currentURL.toURI());
    System.out.println( currentPath ); 
        in VS Code this is: /Users/tzucu/git/aws-sample/todos-data/bin/main 
    */

     
    public TodoRepositoryFileLocal() {
        this.objectMapper = new ObjectMapper();
        try {
            Properties appProps = new Properties();
            String configPath = getClass().getClassLoader().getResource(APP_CONFIG_FILE).getPath();
            appProps.load(new FileInputStream(configPath));
            String jsonDataPath = appProps.getProperty(JSON_DATA_FILE_KEY);
            dataFile = new File(jsonDataPath);
        } catch( IOException ex ) {
            //TODO throw a custom exception
            System.err.println(ex);    
        }
        
    } 

    @Override
    public List<TodoList> getAllLists() {
        try {  
            byte[] buff = Files.readAllBytes( dataFile.toPath() );
            if(buff.length==0) {
                return new ArrayList<TodoList>();    
            }
            return deserialize(buff);
        } catch( IOException ex ) {
            System.err.println(ex);  
        }
        return Collections.<TodoList>emptyList();
    }

    @Override
    public void createList(TodoList todoList) {
      try {  
          List<TodoList> todos = getAllLists();
          todos.add(todoList);
          FileOutputStream fos = new FileOutputStream(dataFile);
          fos.write( serialize(todos) );
          fos.close(); 
      } catch( IOException ex ) {
          System.err.println(ex);
      }
    }

    @Override
    public TodoList getListById(String id) {
        List<TodoList> todos = getAllLists();
        Optional<TodoList> opt = todos.stream().filter(todo -> todo.uuid().equals(id)).findAny();
        if( opt.isPresent() ) {
            return opt.get();
        } else {
            // TODO define an exception
            throw new RuntimeException("bka");
        }
    }

    @Override
    public TodoItem getItemById(String id) {
        return getItemById(getAllLists(), id); 
        
    }

    private TodoItem getItemById(List<TodoList> todos, String id) {
        for( TodoList todoList : todos ) {
          for( TodoItem item : todoList.items() ) {
              if( item.uuid().equals(id) ) {
                  return item;
              }
          }
        } 
        // TODO define an exception
        throw new RuntimeException("bka");  
    }

    public void updateItem(TodoItem item) {
        List<TodoList> todos = getAllLists();
        TodoItem foundItem = getItemById(todos, item.uuid());
        
    }

    protected byte[] serialize(List<TodoList> todos) throws JsonProcessingException {
        return objectMapper.writeValueAsBytes(todos);  
    }

    protected List<TodoList> deserialize(byte[] buff) throws IOException {
        return objectMapper.readValue(buff, new TypeReference<List<TodoList>>(){});
    }

}
