package org.home.temperature.server.telegram.rest;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Path("/")
public class UpdateService {

	private static final String CHANNEL = "@ezflat";
	
	private volatile float temperature;

	private String updateTime;

	private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm yyyy-MM-dd");

	private WebTarget webTarget;

	private WebTarget thingspeakTarget;

	private Logger logger = LoggerFactory.getLogger(UpdateService.class);

	private static final String SEND_MESSAGE_METHOD = "sendMessage";

	private WebTarget createTarget(String baseUrl, String telegramToken) {
		ClientConfig clientConfig = new ClientConfig(new MoxyJsonFeature());
		Client client = ClientBuilder.newClient(clientConfig);
		String url = String.join("", baseUrl, telegramToken);
		return client.target(url);
	}

	private WebTarget createThingspeakTarget(String url) {
		Client client = ClientBuilder.newClient();
		return client.target(url);
	}

	public UpdateService() {
		webTarget = createTarget(Main.BASE_URL, System.getProperty("telegram.token")).path(SEND_MESSAGE_METHOD);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(Update update) {
		Message msg = update.getMessage();
		logger.info("" + msg.getChat().getId());
		switch (msg.getChat().getId()) {
		case 130318030:
		case 76305315:
		case 113136451:
			if (msg.getText().equals("/temp")) {
				OutgoingMessage outMsg = new OutgoingMessage();
				outMsg.setChat_id(Integer.toString(msg.getChat().getId()));
				if (updateTime == null) {
					outMsg.setText("Temperature is not available");
				} else {
					outMsg.setText(String.format("Temperature is %.2f at %s", temperature, updateTime));
				}
				Invocation.Builder sendBuilder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
				sendBuilder.accept(MediaType.APPLICATION_JSON).post(Entity.entity(outMsg, MediaType.APPLICATION_JSON),
						String.class);
			} else if(msg.getText().equals("/tempchannel")) {
				OutgoingMessage outMsg = new OutgoingMessage();
				outMsg.setChat_id(CHANNEL);
				if (updateTime == null) {
					outMsg.setText("Temperature is not available");
				} else {
					outMsg.setText(String.format("Temperature is %.2f at %s", temperature, updateTime));
				}
				Invocation.Builder sendBuilder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
				Object o = sendBuilder.accept(MediaType.APPLICATION_JSON).post(Entity.entity(outMsg, MediaType.APPLICATION_JSON),
						String.class);
				logger.info(o.toString());
			}
			break;
		default:
		}
		return Response.status(201).entity("ok").build();
	}

	@GET
	@Path("/add")
	public Response addTemperature(@QueryParam("temp0") float temperature) {
		this.temperature = temperature;
		this.updateTime = OffsetDateTime.now(ZoneId.of("Europe/Moscow")).format(formatter);
		return Response.status(200).entity("ok").build();
	}

	private void sendTemperature() {

	}
}
