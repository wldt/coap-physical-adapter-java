package it.wldt.adapter.coap.physical;

import it.wldt.adapter.coap.physical.configuration.CoapPhysicalAdapterConfiguration;
import it.wldt.adapter.coap.physical.configuration.CoapPhysicalAdapterConfigurationException;
import it.wldt.adapter.coap.physical.utils.DefaultShadowingFunction;
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

        DigitalTwin dt = new DigitalTwin("coap-digital-twin", new DefaultShadowingFunction());
        ConsoleDigitalAdapter digitalAdapter = new ConsoleDigitalAdapter();

        dt.addDigitalAdapter(digitalAdapter);

        String serverAddress = "127.0.0.1";
        int serverPort = 5683;

        CoapPhysicalAdapterConfiguration configuration = CoapPhysicalAdapterConfiguration.builder(serverAddress, serverPort)
                .enableResourceDiscoverySupport(true)
                .enableObservability(true)
                .setAutoUpdateTimerSupport(true)
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
                    Request request;
                    request = new Request(event.getBody().equals("") ? CoAP.Code.POST : CoAP.Code.PUT);
                    request.setConfirmable(true);
                    request.setPayload((String) event.getBody());
                    return request;
                })
                .build();

        CoapPhysicalAdapter physicalAdapter = new CoapPhysicalAdapter("coap-test-physical-adapter", configuration);

        dt.addPhysicalAdapter(physicalAdapter);

        engine.addDigitalTwin(dt);
        engine.startAll();
    }
}
