package org.home.temperature.server.telegram.rest;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatterBuilder;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.moxy.json.MoxyJsonFeature;
import org.home.temperature.server.Main;
import org.home.temperature.server.telegram.Message;
import org.home.temperature.server.telegram.OutgoingMessage;
import org.home.temperature.server.telegram.Update;

@Singleton
@Path("/")
public class UpdateService {

	private volatile float temperature;

	private WebTarget webTarget;

	private static final String SEND_MESSAGE_METHOD = "sendMessage";

	private WebTarget createTarget(String baseUrl, String token) {
		ClientConfig clientConfig = new ClientConfig(new MoxyJsonFeature());
		Client client = ClientBuilder.newClient(clientConfig);
		String url = String.join("", baseUrl, token);
		return client.target(url);
	}

	public UpdateService() {
		webTarget = createTarget(Main.BASE_URL, System.getProperty("telegram.token")).path(SEND_MESSAGE_METHOD);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(Update update) {
		Message msg = update.getMessage();
		if (msg.getChat().getId() == 130318030) {
			OutgoingMessage outMsg = new OutgoingMessage();
			outMsg.setChat_id(msg.getChat().getId());
			OffsetDateTime time = OffsetDateTime.now(ZoneId.of("Europe/Moscow"));
			outMsg.setText(String.format("Temperature is %.2f at %s", temperature, time.toString()));
			Invocation.Builder sendBuilder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
			sendBuilder.accept(MediaType.APPLICATION_JSON).post(Entity.entity(outMsg, MediaType.APPLICATION_JSON),
					String.class);
		}
		return Response.status(201).entity("ok").build();
	}

	@GET
	@Path("/add")
	public Response addTemperature(@QueryParam("temp0") float temperature) {
		this.temperature = temperature;
		return Response.status(200).entity("ok").build();
	}
}
