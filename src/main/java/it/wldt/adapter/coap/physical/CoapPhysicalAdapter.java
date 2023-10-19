package it.wldt.adapter.coap.physical;

import it.wldt.adapter.coap.physical.resource.asset.DigitalTwinCoapResource;
import it.wldt.adapter.coap.physical.resource.asset.PropertyCoapResource;
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

public class CoapPhysicalAdapter extends ConfigurablePhysicalAdapter<CoapPhysicalAdapterConfiguration> {
    private static final Logger logger = LoggerFactory.getLogger(CoapPhysicalAdapter.class);

    private CoapClient coapClient;

    public CoapPhysicalAdapter(String id, CoapPhysicalAdapterConfiguration configuration) {
        super(id, configuration);

        this.coapClient = new CoapClient(getConfiguration().getServerConnectionString());
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
        Set<WebLink> linkSet = coapClient.discover();

        for (WebLink link : linkSet) {
            if (link.getURI() != null && !link.getURI().isBlank()) {
                String uri = link.getURI().substring(link.getURI().indexOf('/'));

                DigitalTwinCoapResource resource = null;

                boolean observable = link.getAttributes().containsAttribute("obs");

                if (!link.getAttributes().containsAttribute("rt")) {
                    continue;
                }

                String rt = link.getAttributes().getAttributeValues("rt").get(0);

                List<String> ifList = link.getAttributes().getAttributeValues("if");

                if (ifList.contains("core.s")) {    // CoAP sensor
                    // CoapPayloadFunction requires a property key. As of now the property key is set to rt
                    if (observable) {
                        resource = new PropertyCoapResource(getConfiguration().getServerConnectionString(), uri, true, rt, getConfiguration().getPayloadFunction());
                    } else if (getConfiguration().getAutoUpdateFlag()){
                        resource = new PropertyCoapResource(getConfiguration().getServerConnectionString(), uri, getConfiguration().getAutoUpdatePeriod(), rt, getConfiguration().getPayloadFunction());
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

    private void manageResourcePayload(DigitalTwinCoapResource resource) {
        logger.debug("CoAP Physical Adapter - [START] manageResourcePayload()");
        System.out.println("RESOURCE " + resource.getResourceUri() + " - ADDING PAYLOAD LISTENER");
        resource.addPayloadListener((value) -> {
            System.out.println("LISTENER OK");
            List<? extends WldtEvent<?>> wldtEvents = resource.applyPayloadFunction(value);

            wldtEvents.forEach(e -> {
                try {
                    if (e instanceof PhysicalAssetPropertyWldtEvent) {
                        System.out.println("Physical asset property");
                        publishPhysicalAssetPropertyWldtEvent((PhysicalAssetPropertyWldtEvent<?>) e);
                    } else if (e instanceof PhysicalAssetEventWldtEvent) {
                        System.out.println("Physical asset event");
                        publishPhysicalAssetEventWldtEvent((PhysicalAssetEventWldtEvent<?>) e);
                    } else {
                        // TODO: Manage
                        System.out.println("Not actual event");
                    }
                }catch (EventBusException ex) {
                    ex.printStackTrace();
                }
            });
        });
        logger.debug("CoAP Physical Adapter - [STOP] manageResourcePayload()");
    }
}
