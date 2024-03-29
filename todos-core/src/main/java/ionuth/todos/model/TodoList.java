package ionuth.todos.model;

import java.util.ArrayList;
import java.util.List;

public class TodoList {
    
	private String uuid;
	private String title;
    private String userEmail;
	private String creationDate;
	private String lastViewDate;
	private String extraInfo;
	
    List<TodoItem> items = new ArrayList<TodoItem>();
    
    public TodoList() {}
    
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
    
    public void setItems(List<TodoItem> items) {
    	this.items = items;
    }
    
	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}
	
	public String getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}

	public String getLastViewDate() {
		return lastViewDate;
	}

	public void setLastViewDate(String lastViewDate) {
		this.lastViewDate = lastViewDate;
	}
	
	public String getExtraInfo() {
		return extraInfo;
	}

	public void setExtraInfo(String extraInfo) {
		this.extraInfo = extraInfo;
	}

	@Override
	public String toString() {
		return "TodoList [uuid=" + uuid + ", title=" + title + 
				", creationDate=" + creationDate + ", lastViewDate=" + lastViewDate +
				System.lineSeparator() + ", items=" + items + "]";
	}
    
    
  
}
