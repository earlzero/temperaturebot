package org.home.temperature.server;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.grizzly.http.server.HttpServer;
import org.home.temperature.server.telegram.rest.UpdateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;

public class Main {

	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	public static final String BASE_URL = "https://api.telegram.org/bot";
	private static final String SET_WEBHOOK_METHOD = "setWebhook";

	private static void setWebHook(String token, String url) {
		Client client = ClientBuilder.newClient();
		Invocation.Builder ib = client.target(String.join("", BASE_URL, token)).path(SET_WEBHOOK_METHOD)
				.request(MediaType.APPLICATION_JSON_TYPE);
		String request = String.format("{\"url\":\"%s\"", url);
		Response response = ib.post(Entity.entity(request, MediaType.APPLICATION_JSON));
		logger.info(String.format("Registration %s", response.getStatus()));
	}

	public static void main(String... args) throws Exception {

		setWebHook(System.getProperty("telegram.token"), System.getProperty("webhook.url"));
		ResourceConfig rc = new PackagesResourceConfig(UpdateService.class.getPackage().getName());
		HttpServer server = GrizzlyServerFactory
				.createHttpServer(String.format("http://0.0.0.0:%s", System.getProperty("port")), rc);
		server.start();

		while (!Thread.currentThread().isInterrupted()) {
			Thread.sleep(1000);
		}

	}
}
