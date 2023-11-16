package it.wldt.adapter.coap.physical;

import it.wldt.adapter.coap.physical.exceptions.CoapPhysicalAdapterConfigurationException;
import it.wldt.adapter.coap.physical.resources.assets.functions.body.EventBodyProducer;
import it.wldt.adapter.coap.physical.resources.assets.functions.body.PropertyBodyProducer;
import it.wldt.adapter.coap.physical.utils.CoapTestShadowingFunction;
import it.wldt.adapter.coap.physical.utils.ConsoleDigitalAdapter;
import it.wldt.core.engine.WldtEngine;
import it.wldt.exception.EventBusException;
import it.wldt.exception.ModelException;
import it.wldt.exception.WldtConfigurationException;
import it.wldt.exception.WldtRuntimeException;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

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

        CoapPhysicalAdapterConfiguration configuration = CoapPhysicalAdapterConfiguration.builder(serverAddress, serverPort)
                .setAutoUpdateFlag(true)
                .setAutoUpdatePeriod(5000)
                .setResourceDiscoveryFlag(true)
                .setPreferredContentFormat(MediaTypeRegistry.APPLICATION_SENML_JSON)
                .setDefaultPropertyBodyProducer(new PropertyBodyProducer<>(String::new))
                .setDefaultEventBodyProducer(new EventBodyProducer<>(String::new))
                .setDigitalTwinEventsFlag(true)
                .build();

        CoapPhysicalAdapter physicalAdapter = new CoapPhysicalAdapter("coap-test-physical-adapter", configuration);

        dt.addPhysicalAdapter(physicalAdapter);

        dt.startLifeCycle();

        Timer timer = new Timer();

        timer.schedule(new TimerTask() {
                           @Override
                           public void run() {
                               digitalAdapter.invokeAction("change temperature-actuator", "", "text/plain");
                           }
                       }, 2000, 10000);
    }
}
