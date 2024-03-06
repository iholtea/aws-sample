package ionuth.todos.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TodoItem {
    
	@JsonProperty("uuid")
    String uuid; 
    
    @JsonProperty("text")
    String text; 
    
    @JsonProperty("description")
    String description = "";
    
    @JsonProperty("done")
    boolean done = false;
    
    public TodoItem() {}
    
    public TodoItem(String uuid, String text) {
    	this.uuid = uuid;
    	this.text = text;
    }
    
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	@Override
	public String toString() {
		return "TodoItem [uuid=" + uuid + ", text=" + text + ", description=" + description + ", done=" + done + "]";
	}
    
	
    

}
