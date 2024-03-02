package ionuth.test.json;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TodoItem(
    @JsonProperty("uuid")
    String uuid, 
    @JsonProperty("text")
    String text, 
    @JsonProperty("description")
    String description,
    @JsonProperty("done")
    boolean done) {
}
