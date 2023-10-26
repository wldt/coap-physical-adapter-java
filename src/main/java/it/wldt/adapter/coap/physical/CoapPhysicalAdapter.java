package it.wldt.adapter.coap.physical;

import it.wldt.adapter.coap.physical.resource.asset.ActionCoapResourceDescriptor;
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
import java.util.function.Function;

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

        // TODO: Add custom resource discovery function support (add function in the configuration and here check if present. If not apply the default CoRE resource discovery)

        Set<WebLink> linkSet = coapClient.discover();

        for (WebLink link : linkSet) {
            if (link.getURI() != null && !link.getURI().isBlank()) {
                String uri = link.getURI().substring(link.getURI().indexOf('/'));

                DigitalTwinCoapResourceDescriptor resource = null;

                if (!link.getAttributes().containsAttribute("rt")) {
                    // TODO: What if resource contains uri but not rtAttr? Shouldn't discard it
                    continue;
                }

                String rtAttr = link.getAttributes().getAttributeValues("rt").get(0);

                logger.debug("CoAP Physical Adapter - Resource discovery found resource '{}'", rtAttr);

                boolean observable = link.getAttributes().containsAttribute("obs");

                String ifAttr = link.getAttributes().getFirstAttributeValue("if");

                switch (ifAttr) {
                    case "core.s" -> {      // Sensor -> WLDT Property
                        if (observable) {
                            resource = new PropertyCoapResourceDescriptor<>(getConfiguration().getServerConnectionString(), uri, true, rtAttr, getConfiguration().getPropertyBodyProducer());
                        } else if (getConfiguration().getAutoUpdateFlag()){
                            resource = new PropertyCoapResourceDescriptor<>(getConfiguration().getServerConnectionString(), uri, getConfiguration().getAutoUpdatePeriod(), rtAttr, getConfiguration().getPropertyBodyProducer());
                        }

                        // if not observable and auto update is disabled the resource is discarded
                    }
                    case "core.a" -> {      // Actuator -> WLDT Action
                        if (observable) {
                            resource = new ActionCoapResourceDescriptor<>(getConfiguration().getServerConnectionString(), uri, true, rtAttr, getConfiguration().getActionBodyProducer());
                        } else if (getConfiguration().getAutoUpdateFlag()){
                            resource = new ActionCoapResourceDescriptor<>(getConfiguration().getServerConnectionString(), uri, getConfiguration().getAutoUpdatePeriod(), rtAttr, getConfiguration().getActionBodyProducer());
                        }
                    }
                    default -> {

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
        logger.debug("CoAP Physical Adapter - [STOP] manageResourcePayload()");
    }
}
