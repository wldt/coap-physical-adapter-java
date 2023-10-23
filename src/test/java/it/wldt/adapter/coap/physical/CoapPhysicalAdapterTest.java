package it.wldt.adapter.coap.physical;

import it.wldt.adapter.coap.physical.exception.CoapPhysicalAdapterConfigurationException;
import it.wldt.adapter.coap.physical.resource.asset.PropertyCoapResourceDescriptor;
import it.wldt.adapter.coap.physical.resource.asset.payload.CoapBytePayloadFunction;
import it.wldt.adapter.coap.physical.resource.asset.payload.CoapPayloadFunction;
import it.wldt.adapter.coap.physical.utils.CoapTestShadowingFunction;
import it.wldt.adapter.coap.physical.utils.ConsoleDigitalAdapter;
import it.wldt.core.engine.WldtEngine;
import it.wldt.exception.EventBusException;
import it.wldt.exception.ModelException;
import it.wldt.exception.WldtConfigurationException;
import it.wldt.exception.WldtRuntimeException;

import java.util.ArrayList;

public class CoapPhysicalAdapterTest {
    public static void main(String[] args) throws ModelException, WldtRuntimeException, EventBusException, WldtConfigurationException, CoapPhysicalAdapterConfigurationException, InterruptedException {
        WldtEngine dt = new WldtEngine(new CoapTestShadowingFunction(), "coap-digital-twin");
        ConsoleDigitalAdapter digitalAdapter = new ConsoleDigitalAdapter();

        dt.addDigitalAdapter(digitalAdapter);

        // String serverAddress = "192.168.10.40";
        String serverAddress = "127.0.0.1";
        int serverPort = 5683;

        CoapBytePayloadFunction payloadFunction = bytes -> {
            System.out.println("PAYLOAD");
            return new ArrayList<>();
        };

        CoapPhysicalAdapterConfiguration configuration = CoapPhysicalAdapterConfiguration.builder(serverAddress, serverPort)
                .setAutoUpdateFlag(true)
                .setAutoUpdatePeriod(5000)
                .setResourceDiscoveryFlag(true)
                .setPayloadFunction(payloadFunction)
                .build();

        // configuration.addResource(new PropertyCoapResourceDescriptor(configuration.getServerConnectionString(), "temperature", configuration.getAutoUpdatePeriod(), "wldt.test.property.temperature", payloadFunction));

        dt.addPhysicalAdapter(new CoapPhysicalAdapter("coap-test-physical-adapter", configuration));

        System.out.println("ENGINE START");
        dt.startLifeCycle();
    }
}
