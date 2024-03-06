package ionuth.todos.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TodoList {
    
	@JsonProperty("uuid")
    String uuid;
    
    @JsonProperty("title")
    String title;
    
    @JsonProperty("items")
    List<TodoItem> items = new ArrayList<TodoItem>();
    
    public TodoList() {}
    
    public TodoList(String uuid, String title) {
    	this.uuid = uuid;
    	this.title = title;
    }

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
    
    public List<TodoItem> getItems() {
    	return items;
    }

	@Override
	public String toString() {
		return "TodoList [uuid=" + uuid + ", title=" + title + ", items=" + items + "]";
	}
    
    
  
}
