package ionuth.todos.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TodoList(
    @JsonProperty("uuid")
    String uuid,
    @JsonProperty("title")
    String title,
    @JsonProperty("items")
    List<TodoItem> items
) {
  
}
