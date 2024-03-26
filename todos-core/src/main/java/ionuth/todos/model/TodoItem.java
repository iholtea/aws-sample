package ionuth.todos.model;

public class TodoItem {
    
	String uuid; 
    String text; 
    boolean done = false;
    int orderIdx;
    String extraInfo;
    
    String listUuid;
    
    
	public TodoItem() {}
    
    
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
	
    public String getListUuid() {
		return listUuid;
	}

	public void setListUuid(String listUuid) {
		this.listUuid = listUuid;
	}

	public int getOrderIdx() {
		return orderIdx;
	}

	public void setOrderIdx(int orderIdx) {
		this.orderIdx = orderIdx;
	}

	@Override
	public String toString() {
		return "TodoItem [uuid=" + uuid + ", text=" + text + 
				", done=" + done + ", orderIdx=" + orderIdx + "]";
	}
    

}
