package org.home.temperature.server.telegram;

public class Message {

	private int message_id;
	private Person from;
	private Person chat;
	private long date;
	private String text;

	public int getMessage_id() {
		return message_id;
	}

	public void setMessage_id(int message_id) {
		this.message_id = message_id;
	}

	public Person getFrom() {
		return from;
	}

	public void setFrom(Person from) {
		this.from = from;
	}

	public Person getChat() {
		return chat;
	}

	public void setChat(Person chat) {
		this.chat = chat;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
