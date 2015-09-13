package org.home.temperature.server;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.home.temperature.server.telegram.rest.UpdateService;

public class BotApplication extends ResourceConfig {
	
	public BotApplication() {
		// Register resources and providers using package-scanning.
        packages(UpdateService.class.getPackage().getName());
 
        property(ServerProperties.TRACING, "ALL");
	}
}
