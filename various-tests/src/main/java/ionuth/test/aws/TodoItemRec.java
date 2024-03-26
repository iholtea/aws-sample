package ionuth.test.aws;

public record TodoItemRec(
	String listUuid,
	String itemUuid,
	String itemText,
	boolean itemDone,
	int orderIdx,
	String itemExtraInfo
) {}
