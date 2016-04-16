package org.home.temperature.server.telegram.rest;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.moxy.json.MoxyJsonFeature;
import org.home.temperature.server.Main;
import org.home.temperature.server.telegram.Message;
import org.home.temperature.server.telegram.OutgoingMessage;
import org.home.temperature.server.telegram.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * http://stackoverflow.com/questions/33858927/how-to-obtain-the-chat-id-of-a-private-telegram-channel
 */

@Singleton
@Path("/")
public class UpdateService {

	class SensorInfo {
		private float temperature;
		private OffsetDateTime updated;

		public SensorInfo(float temperature, OffsetDateTime updated) {
			this.temperature = temperature;
			this.updated = updated;
		}

		public float getTemperature() {
			return temperature;
		}

		public void setTemperature(float temperature) {
			this.temperature = temperature;
		}

		public OffsetDateTime getUpdated() {
			return updated;
		}

		public void setUpdated(OffsetDateTime updated) {
			this.updated = updated;
		}

	}

	private static final int COUNT = 2;

	private volatile SensorInfo[] temperature = new SensorInfo[COUNT];

	private String updateTime;

	private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm yyyy-MM-dd");

	private WebTarget webTarget;

	private Logger logger = LoggerFactory.getLogger(UpdateService.class);

	private static final String SEND_MESSAGE_METHOD = "sendMessage";

	private WebTarget createTarget(String baseUrl, String telegramToken) {
		ClientConfig clientConfig = new ClientConfig(new MoxyJsonFeature());
		Client client = ClientBuilder.newClient(clientConfig);
		String url = String.join("", baseUrl, telegramToken);
		return client.target(url);
	}

	public UpdateService() {
		 webTarget = createTarget(Main.BASE_URL,
		 System.getProperty("telegram.token")).path(SEND_MESSAGE_METHOD);
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
			handleMessage(msg);
			break;
		default:
		}
		return Response.status(201).entity("ok").build();
	}

	private void handleMessage(Message msg) {
		if (msg.getText().equals("/gryazi")) {
			sendMessage(Integer.toString(msg.getChat().getId()), 0);
		} else if (msg.getText().equals("/gryazichannel")) {
			sendMessage(System.getProperty("telegram.channel"), 0);
		} else if (msg.getText().equals("/dublin")) {
			sendMessage(Integer.toString(msg.getChat().getId()), 1);
		} else if (msg.getText().equals("/dublinchannel")) {
			sendMessage(System.getProperty("telegram.channel"), 0);
		}
	}

	@GET
	@Path("/add")
	public Response addTemperature(@Context UriInfo context) {

		Optional<String> temperature0 = Optional.ofNullable(context.getQueryParameters().get("temp0"))
				.flatMap(l -> l.stream().findFirst());
		Optional<String> temperature1 = Optional.ofNullable(context.getQueryParameters().get("temp1"))
				.flatMap(l -> l.stream().findFirst());

		temperature0.ifPresent(t -> {
			updateTemperature(0, t);
		});

		temperature1.ifPresent(t -> {
			updateTemperature(1, t);
		});
		return Response.status(200).entity("ok").build();
	}

	private void updateTemperature(int index, String value) {
		OffsetDateTime updateTime = OffsetDateTime.now(ZoneId.of("Europe/Moscow"));
		this.temperature[index] = new SensorInfo(Float.valueOf(value), updateTime);
	}

	private void sendMessage(String destination, int sensorId) {
		SensorInfo sensorInfo = temperature[sensorId];
		OutgoingMessage outMsg = new OutgoingMessage();
		outMsg.setChat_id(destination);
		if (sensorInfo == null) {
			outMsg.setText("Temperature is not available");
		} else {
			outMsg.setText(String.format("Temperature is %.2f at %s", sensorInfo.getTemperature(),
					sensorInfo.getUpdated().format(formatter)));
		}
		Invocation.Builder sendBuilder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
		Response r = sendBuilder.accept(MediaType.APPLICATION_JSON)
				.post(Entity.entity(outMsg, MediaType.APPLICATION_JSON));
		logger.info("" + r.getStatus());

	}
}
