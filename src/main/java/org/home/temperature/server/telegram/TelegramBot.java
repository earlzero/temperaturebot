package org.home.temperature.server.telegram;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.moxy.json.MoxyJsonFeature;

public class TelegramBot implements Runnable {

	private float temperature;
	private String token;

	private static final String BASE_URL = "https://api.telegram.org/bot";
	private static final String METHOD = "getUpdates";

	public TelegramBot(String token) {
		this.token = token;
	}

	public synchronized float getTemperature() {
		return temperature;
	}

	public synchronized void setTemperature(float temperature) {
		this.temperature = temperature;
	}

	public WebTarget createTarget(String baseUrl, String token) {
		ClientConfig clientConfig = new ClientConfig(new MoxyJsonFeature());
		Client client = ClientBuilder.newClient(clientConfig);
		String url = String.join("", baseUrl, token);
		return client.target(url);
	}

	private int offset = 0;

	public void makeRequest(WebTarget target) {
		Request info = new Request();
		info.setOffset(offset);
		info.setLimit(5);
		info.setTimeout(1);

		Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON_TYPE);

		Response response = invocationBuilder.accept(MediaType.APPLICATION_JSON)
				.post(Entity.entity(info, MediaType.APPLICATION_JSON), Response.class);

		for (Update update : response.getResult()) {
			Message msg = update.getMessage();
			if (msg != null) {
				offset = Math.max(offset, update.getUpdate_id() + 1);
				if (msg.getFrom().getId() == 130318030) {
					if(msg.getText().startsWith("/temp")) {
						System.out.println(temperature);
					}
				}
			}
		}

	}

	public void run() {

		WebTarget target = createTarget(BASE_URL, token).path(METHOD);

		while (!Thread.currentThread().isInterrupted()) {
			makeRequest(target);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// whatever
			}
		}

	}

	public static void main(String[] args) throws Exception {
		new TelegramBot(System.getProperty("telegram.token")).run();
	}

}
