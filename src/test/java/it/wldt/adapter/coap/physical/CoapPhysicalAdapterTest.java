package it.wldt.adapter.coap.physical;

import it.wldt.adapter.coap.physical.exception.CoapPhysicalAdapterConfigurationException;
import it.wldt.adapter.coap.physical.resource.asset.CoapPayloadFunction;
import it.wldt.adapter.coap.physical.server.CoapTestServer;
import it.wldt.adapter.coap.physical.utils.CoapTestShadowingFunction;
import it.wldt.adapter.coap.physical.utils.ConsoleDigitalAdapter;
import it.wldt.core.engine.WldtEngine;
import it.wldt.core.event.WldtEvent;
import it.wldt.exception.EventBusException;
import it.wldt.exception.ModelException;
import it.wldt.exception.WldtConfigurationException;
import it.wldt.exception.WldtRuntimeException;

import java.util.ArrayList;
import java.util.List;

public class CoapPhysicalAdapterTest {
    public static void main(String[] args) throws ModelException, WldtRuntimeException, EventBusException, WldtConfigurationException, CoapPhysicalAdapterConfigurationException, InterruptedException {
        WldtEngine dt = new WldtEngine(new CoapTestShadowingFunction(), "coap-digital-twin");
        ConsoleDigitalAdapter digitalAdapter = new ConsoleDigitalAdapter();

        dt.addDigitalAdapter(digitalAdapter);

        CoapPhysicalAdapterConfiguration configuration = CoapPhysicalAdapterConfiguration.builder("127.0.0.1", 5683)
                .setAutoUpdateFlag(true)
                .setAutoUpdatePeriod(10000)
                .setResourceDiscoveryFlag(true)
                .setPayloadFunction(bytes -> {
                    return new ArrayList<>();
                })
                .build();

        dt.addPhysicalAdapter(new CoapPhysicalAdapter("coap-test-physical-adapter", configuration));

        dt.startLifeCycle();
    }
}
