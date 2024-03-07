package ionuth.todos.model;

public class TodoItem {
    
	String uuid; 
    String text; 
    String extraInfo;
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

	public String getExtraInfo() {
		return extraInfo;
	}

	public void setExtraInfo(String extraInfo) {
		this.extraInfo = extraInfo;
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	@Override
	public String toString() {
		return "TodoItem [uuid=" + uuid + ", text=" + text + ", done=" + done + "]";
	}
    

}
