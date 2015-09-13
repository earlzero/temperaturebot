package org.home.temperature.server.telegram;

public class OutgoingMessage {
	private int chat_id;
	private String text;
	public int getChat_id() {
		return chat_id;
	}
	public void setChat_id(int chat_id) {
		this.chat_id = chat_id;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
}
