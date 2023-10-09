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
    }

    @Override
    public void onAdapterStart() {
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
    }

    @Override
    public void onAdapterStop() {

    }

    public <T> void discoverCoapResources() throws ConnectorException, IOException {
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
                    if (observable) {
                        resource = new PropertyCoapResource<>(this.coapClient, uri, true, rt, getConfiguration().getResourceFunction());
                    } else {
                        resource = new PropertyCoapResource<>(this.coapClient, uri, getConfiguration().getAutoUpdatePeriod(), rt, getConfiguration().getResourceFunction());
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
    }

    private void manageResourcePayload(DigitalTwinCoapResource resource) {
        resource.addPayloadListener((value) -> {
            List<? extends WldtEvent<?>> wldtEvents = resource.applyPayloadFunction(value);

            wldtEvents.forEach(e -> {
                try {
                    if (e instanceof PhysicalAssetPropertyWldtEvent) {
                        publishPhysicalAssetPropertyWldtEvent((PhysicalAssetPropertyWldtEvent<?>) e);
                    } else if (e instanceof PhysicalAssetEventWldtEvent) {
                        publishPhysicalAssetEventWldtEvent((PhysicalAssetEventWldtEvent<?>) e);
                    }
                }catch (EventBusException ex) {
                    ex.printStackTrace();
                }
            });
        });

    }
}
