package it.wldt.adapter.coap.physical.server;

import it.wldt.adapter.coap.physical.server.model.TemperatureActuator;
import it.wldt.adapter.coap.physical.server.model.TemperatureSensor;
import it.wldt.adapter.coap.physical.server.resource.TemperatureActuatorResource;
import it.wldt.adapter.coap.physical.server.resource.TemperatureSensorResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.server.resources.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoapTestServer extends CoapServer {
    private static Logger logger = LoggerFactory.getLogger(CoapTestServer.class);

    private static final int SERVER_PORT = 5683;

    private static final String DEVICE_ID = "wldt:coap:test";

    public CoapTestServer() {
        super(SERVER_PORT);

        this.add(new TemperatureSensorResource(DEVICE_ID, "temperature-sensor", new TemperatureSensor()));
        this.add(new TemperatureActuatorResource(DEVICE_ID, "temperature-actuator", new TemperatureActuator()));
    }

    private static void logResource(Resource resource) {
        logger.info("Resource log: '{}'{}", resource.getURI(), (resource.isObservable() ? "(observable)" : ""));

        if (!resource.getURI().equals("/.well-known")) {
            resource.getChildren().forEach(CoapTestServer::logResource);
        }
    }

    public static void main(String[] args) {
        CoapTestServer server = new CoapTestServer();

        server.start();

        server.getRoot().getChildren().forEach(CoapTestServer::logResource);
    }
}
