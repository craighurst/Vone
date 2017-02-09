package vone;

import org.joda.time.DateTime;

public class V1Comments {
	
	private String Author;
	private String Body;
	private String Mentions;
	private String InReplyTo;
	private DateTime AuthoredAt;
	private String Content;
	private String BelongsTo;
	public String getAuthor() {
		return Author;
	}
	public void setAuthor(String author) {
		Author = author;
	}
	public String getBody() {
		return Body;
	}
	public void setBody(String body) {
		Body = body;
	}
	public String getMentions() {
		return Mentions;
	}
	public void setMentions(String mentions) {
		Mentions = mentions;
	}
	public String getInReplyTo() {
		return InReplyTo;
	}
	public void setInReplyTo(String inReplyTo) {
		InReplyTo = inReplyTo;
	}
	public DateTime getAuthoredAt() {
		return AuthoredAt;
	}
	public void setAuthoredAt(DateTime authoredAt) {
		AuthoredAt = authoredAt;
	}
	public String getContent() {
		return Content;
	}
	public void setContent(String content) {
		Content = content;
	}
	public String getBelongsTo() {
		return BelongsTo;
	}
	public void setBelongsTo(String belongsTo) {
		BelongsTo = belongsTo;
	}
	@Override
	public String toString() {
		return "comments [Author=" + Author + ", Body=" + Body + ", Mentions=" + Mentions + ", InReplyTo=" + InReplyTo
				+ ", AuthoredAt=" + AuthoredAt + ", Content=" + Content + ", BelongsTo=" + BelongsTo + "]";
	}





}
