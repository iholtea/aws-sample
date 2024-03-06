package ionuth.test.aws;

public record TodoItemRec(
	String listUuid,
	String itemUuid,
	String listTitle,
	String itemText,
	boolean itemDone,
	String itemExtraInfo
) {}
