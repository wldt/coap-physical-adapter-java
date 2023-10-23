package it.wldt.adapter.coap.physical;

import it.wldt.adapter.coap.physical.resource.asset.DigitalTwinCoapResourceDescriptor;
import it.wldt.adapter.coap.physical.resource.asset.PropertyCoapResourceDescriptor;
import it.wldt.adapter.physical.ConfigurablePhysicalAdapter;
import it.wldt.adapter.physical.event.PhysicalAssetActionWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.core.event.WldtEvent;
import it.wldt.exception.EventBusException;
import it.wldt.exception.PhysicalAdapterException;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.WebLink;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * It is an implementation of a WLDT {@code ConfigurablePhysicalAdapter} implementing CoAP protocol support via the {@code californium-core} framework.
 * The configuration is passed through a {@code CoapPhysicalAdapterConfiguration} class instance.
 * It implements logging functionalities based on {@code SLF4J}.
 * Resources can be added both manually by passing them via the configuration and automatically by using resource discovery.
 *
 * @see ConfigurablePhysicalAdapter
 * @see CoapPhysicalAdapterConfiguration
 * @see DigitalTwinCoapResourceDescriptor
 */
public class CoapPhysicalAdapter extends ConfigurablePhysicalAdapter<CoapPhysicalAdapterConfiguration> {
    private static final Logger logger = LoggerFactory.getLogger(CoapPhysicalAdapter.class);

    public CoapPhysicalAdapter(String id, CoapPhysicalAdapterConfiguration configuration) {
        super(id, configuration);
    }

    @Override
    public void onIncomingPhysicalAction(PhysicalAssetActionWldtEvent<?> physicalAssetActionWldtEvent) {
        // TODO
        logger.debug("CoAP Physical Adapter - [START] onIncomingPhysicalAction()");
        logger.debug("CoAP Physical Adapter - [STOP] onIncomingPhysicalAction()");
    }

    @Override
    public void onAdapterStart() {
        logger.debug("CoAP Physical Adapter - [START] onAdapterStart()");
        try {
            if (getConfiguration().getResourceDiscoveryFlag()) {
                logger.info("CoAP Physical Adapter - CoAP client discovering resources");
                discoverCoapResources();
            }

            getConfiguration().getResources().forEach(this::manageResourcePayload);

            notifyPhysicalAdapterBound(getConfiguration().getPhysicalAssetDescription());
        } catch (PhysicalAdapterException | EventBusException e) {  // Bind notification exceptions
            logger.error("CoAP Physical Adapter - Error notifying binding");
            e.printStackTrace();
        } catch (ConnectorException | IOException e) {              // Resource discovery exceptions
            logger.error("CoAP Physical Adapter - Error discovering CoAP resources");
            e.printStackTrace();
        }

        logger.debug("CoAP Physical Adapter - [STOP] onAdapterStart()");
    }

    @Override
    public void onAdapterStop() {
        logger.debug("CoAP Physical Adapter - [START] onAdapterStop()");
        logger.debug("CoAP Physical Adapter - [STOP] onAdapterStop()");
    }

    public void discoverCoapResources() throws ConnectorException, IOException {
        logger.debug("CoAP Physical Adapter - [START] discoverCoapResources()");

        CoapClient coapClient = new CoapClient(getConfiguration().getServerConnectionString());

        Set<WebLink> linkSet = coapClient.discover();

        // TODO: Add custom resource discovery function support (add function in the configuration and here check if present. If not apply the default CoRE resource discovery)

        for (WebLink link : linkSet) {
            if (link.getURI() != null && !link.getURI().isBlank()) {
                String uri = link.getURI().substring(link.getURI().indexOf('/'));

                DigitalTwinCoapResourceDescriptor resource = null;

                if (!link.getAttributes().containsAttribute("rt")) {
                    continue;
                }

                String rt = link.getAttributes().getAttributeValues("rt").get(0);

                logger.debug("CoAP Physical Adapter - Resource discovery found resource '{}'", rt);

                boolean observable = link.getAttributes().containsAttribute("obs");

                List<String> ifList = link.getAttributes().getAttributeValues("if");

                if (ifList.contains("core.s")) {    // CoAP sensor
                    // CoapPayloadFunction requires a property key. As of now the property key is set to rt
                    if (observable) {
                        resource = new PropertyCoapResourceDescriptor(getConfiguration().getServerConnectionString(), uri, true, rt, getConfiguration().getPayloadFunction());
                    } else if (getConfiguration().getAutoUpdateFlag()){
                        resource = new PropertyCoapResourceDescriptor(getConfiguration().getServerConnectionString(), uri, getConfiguration().getAutoUpdatePeriod(), rt, getConfiguration().getPayloadFunction());
                    } else {
                        // TODO: Not observable && auto update disabled
                    }
                }
                /*
                // TODO: Manage actuators
                if (ifList.contains("core.a")) {    // CoAP actuator

                }
                */

                if (resource != null) {
                    getConfiguration().addResource(resource);
                }
            }
        }
        logger.debug("CoAP Physical Adapter - [STOP] discoverCoapResources()");
    }

    private void manageResourcePayload(DigitalTwinCoapResourceDescriptor resource) {
        logger.debug("CoAP Physical Adapter - [START] manageResourcePayload()");
        System.out.println("Add listener " + resource.getResourceUri());
        resource.addPayloadListener((value) -> {
            List<? extends WldtEvent<?>> wldtEvents = resource.applyPayloadFunction(value);

            wldtEvents.forEach(e -> {
                try {
                    if (e instanceof PhysicalAssetPropertyWldtEvent) {
                        publishPhysicalAssetPropertyWldtEvent((PhysicalAssetPropertyWldtEvent<?>) e);
                    } else if (e instanceof PhysicalAssetEventWldtEvent) {
                        publishPhysicalAssetEventWldtEvent((PhysicalAssetEventWldtEvent<?>) e);
                    } else {
                        logger.error("CoAP Physical Adapter - Received invalid event");
                    }
                }catch (EventBusException ex) {
                    ex.printStackTrace();
                }
            });
        });
        logger.debug("CoAP Physical Adapter - [STOP] manageResourcePayload()");
    }
}
