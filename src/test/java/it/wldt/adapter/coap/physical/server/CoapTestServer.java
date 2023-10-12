package it.wldt.adapter.coap.physical.server;

import it.wldt.adapter.coap.physical.server.model.TemperatureSensor;
import it.wldt.adapter.coap.physical.server.resource.TemperatureSensorResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.server.resources.Resource;

public class CoapTestServer extends CoapServer {
    private static final int SERVER_PORT = 5683;

    private static final String DEVICE_ID = "wldt:coap:test";

    public CoapTestServer() {
        super(SERVER_PORT);

        this.add(new TemperatureSensorResource(DEVICE_ID, "temperature-sensor", new TemperatureSensor()));
    }

    private static void logResource(Resource resource) {
        System.out.println(String.format(
                "[RESOURCE] -> %s %s",
                resource.getURI(),
                resource.isObservable() ? "<observable>" : ""
        ));

        if (!resource.getURI().equals("/.well-known")) {
            resource.getChildren().forEach(CoapTestServer::logResource);
        }
    }

    public static void main(String[] args) {
        CoapTestServer server = new CoapTestServer();

        server.start();

        System.out.println("--RESOURCE-LOGGING--");

        server.getRoot().getChildren().forEach(CoapTestServer::logResource);

        System.out.println("--SERVER-LOGGING--");
    }
}
