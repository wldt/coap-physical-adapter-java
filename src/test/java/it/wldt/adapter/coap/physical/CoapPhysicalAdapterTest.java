package it.wldt.adapter.coap.physical;

import it.wldt.adapter.coap.physical.model.PhysicalAssetResource;
import it.wldt.adapter.coap.physical.utils.CoapTestShadowingFunction;
import it.wldt.adapter.coap.physical.utils.ConsoleDigitalAdapter;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.core.engine.DigitalTwin;
import it.wldt.core.engine.DigitalTwinEngine;
import it.wldt.core.event.WldtEvent;
import it.wldt.exception.*;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;

import java.util.*;

public class CoapPhysicalAdapterTest {
    public static void main(String[] args) throws ModelException, WldtRuntimeException, EventBusException, WldtConfigurationException, InterruptedException, WldtWorkerException, WldtDigitalTwinStateException, WldtEngineException, CoapPhysicalAdapterConfigurationException {
        DigitalTwinEngine engine = new DigitalTwinEngine();

        DigitalTwin dt = new DigitalTwin("coap-digital-twin", new CoapTestShadowingFunction());
        ConsoleDigitalAdapter digitalAdapter = new ConsoleDigitalAdapter();

        dt.addDigitalAdapter(digitalAdapter);

        String serverAddress = "127.0.0.1";
        //String serverAddress = "172.16.0.114";
        int serverPort = 5683;

        CoapPhysicalAdapterConfiguration configuration = CoapPhysicalAdapterConfiguration.builder(serverAddress, serverPort)
                //.enableResourceDiscoverySupport(false)
                .enableResourceDiscoverySupport(true)
                //.enableObservability(false)
                .enableObservability(true)
                .enableAutoUpdateTimer(true)
                .setAutoUpdateInterval(5000)
                .setPreferredContentFormat(MediaTypeRegistry.APPLICATION_JSON)
                .setDefaultPropertyBodyTranslator((key, payload) -> {
                    List<WldtEvent<String>> events = new ArrayList<>();
                    try {
                        events.add(new PhysicalAssetPropertyWldtEvent<>(key, new String(payload)));
                    } catch (EventBusException e) {
                        e.printStackTrace();
                    }
                    return events;
                })
                .setDefaultEventTranslator((key, message) -> {
                    List<WldtEvent<String>> events = new ArrayList<>();
                    try {
                        events.add(new PhysicalAssetEventWldtEvent<>(key, message));
                    } catch (EventBusException e) {
                        e.printStackTrace();
                    }
                    return events;
                })
                .setDefaultActionEventTranslator(event -> {
                    String[] splitted = event.getActionKey().split(" ");
                    Request request;
                    if (splitted[0].equals("change")) {
                        request = new Request(CoAP.Code.POST);
                        request.getOptions().setUriPath(splitted[1]);
                        request.setConfirmable(true);
                        return request;
                    } else {
                        request = new Request(CoAP.Code.PUT);
                        request.getOptions().setUriPath(splitted[1]);
                        request.setConfirmable(true);
                        request.setPayload((String) event.getBody());
                    }
                    return request;
                })
                //.addResource("temperature-sensor", "", MediaTypeRegistry.APPLICATION_SENML_JSON, false, false, false)
                //.addResource("temperature", "", MediaTypeRegistry.APPLICATION_JSON, false, false, false)
                //.addResource("humidity", "", MediaTypeRegistry.APPLICATION_JSON, false, false, false)
                .build();

        CoapPhysicalAdapter physicalAdapter = new CoapPhysicalAdapter("coap-test-physical-adapter", configuration);

        dt.addPhysicalAdapter(physicalAdapter);

        engine.addDigitalTwin(dt);
        engine.startAll();
    }
}
