package it.wldt.adapter.coap.physical;

import it.wldt.adapter.coap.physical.exceptions.CoapPhysicalAdapterConfigurationException;
import it.wldt.adapter.coap.physical.resources.assets.DigitalTwinActionResource;
import it.wldt.adapter.coap.physical.resources.assets.functions.methods.CustomPutRequestFunction;
import it.wldt.adapter.coap.physical.resources.assets.functions.preprocessing.ActionBodyConsumer;
import it.wldt.adapter.coap.physical.resources.assets.functions.preprocessing.EventBodyProducer;
import it.wldt.adapter.coap.physical.resources.assets.functions.preprocessing.PropertyBodyProducer;
import it.wldt.adapter.coap.physical.resources.methods.CoapPutSupport;
import it.wldt.adapter.coap.physical.utils.CoapTestShadowingFunction;
import it.wldt.adapter.coap.physical.utils.ConsoleDigitalAdapter;
import it.wldt.core.engine.WldtEngine;
import it.wldt.exception.EventBusException;
import it.wldt.exception.ModelException;
import it.wldt.exception.WldtConfigurationException;
import it.wldt.exception.WldtRuntimeException;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.elements.exception.ConnectorException;

import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class CoapPhysicalAdapterTest {
    public static void main(String[] args) throws ModelException, WldtRuntimeException, EventBusException, WldtConfigurationException, CoapPhysicalAdapterConfigurationException, InterruptedException {
        WldtEngine dt = new WldtEngine(new CoapTestShadowingFunction(), "coap-digital-twin");
        ConsoleDigitalAdapter digitalAdapter = new ConsoleDigitalAdapter();

        dt.addDigitalAdapter(digitalAdapter);

        // String serverAddress = "192.168.10.40";
        String serverAddress = "127.0.0.1";
        int serverPort = 5683;

        CustomPutRequestFunction customPutRequestFunction = new CustomPutRequestFunction() {
            @Override
            public String send(DigitalTwinActionResource resource, byte[] payload, String ct) {
                Request request = resource.getRequestOptionsBase(CoAP.Code.PUT);
                request.setPayload(payload);
                request.getOptions().setContentFormat(MediaTypeRegistry.parse(ct));

                try {
                    CoapResponse response = resource.getClient().advanced(request);

                    if (response == null) {
                        return "Response:null";
                    } else if (!response.isSuccess()) {
                        return "Response:" + response.getCode();
                    } else if (response.getPayload() != null && response.getPayload().length > 0) {
                        return String.format("Response:%s", new String(response.getPayload()));
                    } else {
                        return "Response:" + response.getCode();
                    }
                } catch (ConnectorException | IOException e) {
                    e.printStackTrace();
                    return e.toString();
                }
            }
        };

        CoapPhysicalAdapterConfiguration configuration = CoapPhysicalAdapterConfiguration.builder(serverAddress, serverPort)
                .setAutoUpdateFlag(true)
                .setAutoUpdatePeriod(5000)
                .setResourceDiscoveryFlag(true)
                .setPreferredContentFormat(MediaTypeRegistry.APPLICATION_SENML_JSON)
                .setDefaultPropertyBodyProducer(new PropertyBodyProducer<>(String::new))
                .setDefaultEventBodyProducer(new EventBodyProducer<>(String::new))
                .setDefaultActionBodyConsumer(new ActionBodyConsumer<String>(String::getBytes))
                .setDigitalTwinEventsFlag(true)
                .build();

        CoapPhysicalAdapter physicalAdapter = new CoapPhysicalAdapter("coap-test-physical-adapter", configuration);

        dt.addPhysicalAdapter(physicalAdapter);

        dt.startLifeCycle();

        Timer timer = new Timer();
        Random random = new Random();

        timer.schedule(new TimerTask() {
                           @Override
                           public void run() {
                               digitalAdapter.invokeAction("change /temperature-actuator", "", "text/plain");
                           }
                       }, 5000, 10000);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                String payload = String.format("%d", random.nextInt(20, 30));
                digitalAdapter.invokeAction("update /temperature-actuator", payload, MediaTypeRegistry.toString(MediaTypeRegistry.TEXT_PLAIN));
            }
        }, 7000, 15000);
    }
}
