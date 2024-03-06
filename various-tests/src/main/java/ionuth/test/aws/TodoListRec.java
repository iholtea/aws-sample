package ionuth.test.aws;

public record TodoListRec(
	String userEmail,
	String uuid,
	String title,
	String creationDate,
	String lastUpdated,
	String extraInfo
) {}
