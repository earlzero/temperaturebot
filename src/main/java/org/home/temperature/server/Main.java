package org.home.temperature.server;

import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.home.temperature.server.telegram.TelegramBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(String... args) throws Exception {

		TelegramBot telegramBot = new TelegramBot(System.getProperty("telegram.token"));
		
		new Thread(telegramBot).start();

		HttpServer server = HttpServer.createSimpleServer(null, Integer.valueOf(System.getProperty("port")));
		server.getServerConfiguration().addHttpHandler(new HttpHandler() {

			@Override
			public void service(Request request, Response response) throws Exception {

				try {
					float temperature = Float.parseFloat(request.getParameter("temp0"));
					String result = "ok";
					response.setContentType("text/plain");
					response.setContentLength(result.length());
					response.getWriter().write(result);
					telegramBot.setTemperature(temperature);
				} catch (NumberFormatException e) {
					logger.error("Can't parse temp0", e);
				}

			}
		});
		server.start();
		while (!Thread.currentThread().isInterrupted()) {
			Thread.sleep(1000);
		}
	}
}
