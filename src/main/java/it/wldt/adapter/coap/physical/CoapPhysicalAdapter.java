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
import org.eclipse.californium.core.coap.MediaTypeRegistry;
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
        // TODO: Manage incoming physical action
        logger.info("CoAP Physical Adapter - Incoming physical action");
    }

    @Override
    public void onAdapterStart() {
        logger.info("CoAP Physical Adapter - Starting");
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

        logger.info("CoAP Physical Adapter - Start sequence completed");
    }

    @Override
    public void onAdapterStop() {
        logger.info("CoAP Physical Adapter - Stopping adapter");
    }

    public void discoverCoapResources() throws ConnectorException, IOException {
        logger.info("CoAP Physical Adapter - Starting resource discovery");

        CoapClient coapClient = new CoapClient(getConfiguration().getServerConnectionString());

        // TODO: Is it correct to oblige user to use web-link format? Is it better to make a wrapper class and use that instead?

        Set<WebLink> linkSet;

        if (getConfiguration().getResourceDiscoveryFunction() != null) {
            linkSet = getConfiguration().getResourceDiscoveryFunction().discover(coapClient);
        } else {
            linkSet = coapClient.discover();
        }

        for (WebLink link : linkSet) {
            if (link.getURI() != null && !link.getURI().isBlank()) {
                String uri = link.getURI().substring(link.getURI().indexOf('/'));

                DigitalTwinCoapResourceDescriptor resource = null;

                if (!link.getAttributes().containsAttribute("rt")) {
                    // TODO: What if resource contains uri but not rt? Shouldn't discard it
                    continue;
                }

                String rtAttr = link.getAttributes().getAttributeValues("rt").get(0);

                logger.info("CoAP Physical Adapter - Resource discovery found resource '{}'", rtAttr);

                boolean observable = link.getAttributes().containsAttribute("obs");

                String ifAttr = link.getAttributes().getFirstAttributeValue("if");

                if (ifAttr != null) {
                    switch (ifAttr) {
                        case "core.s" -> {      // Sensor -> WLDT Property
                            resource = new PropertyCoapResourceDescriptor<>(getConfiguration().getServerConnectionString(), uri, rtAttr, getConfiguration().getPropertyBodyProducer());
                        }
                        case "core.a" -> {      // Actuator -> WLDT Action
                            resource = new ActionCoapResourceDescriptor<>(getConfiguration().getServerConnectionString(), uri, rtAttr, getConfiguration().getActionBodyProducer());
                        }
                        default -> {
                            // TODO: What if not sensor nor actuator?
                        }
                    }
                } else {
                    // TODO: What if field "if" not present
                }

                if (resource != null) {
                    resource.setPreferredContentType(getConfiguration().getPreferredContentType());

                    if (observable) {
                        resource.startObserving();
                    } else if (getConfiguration().getAutoUpdateFlag()){
                        resource.setAutoUpdatePeriod(getConfiguration().getAutoUpdatePeriod());
                        resource.startAutoUpdate();
                    }

                    getConfiguration().addResource(resource);
                }
            }
        }
        logger.info("CoAP Physical Adapter - Ending resource discovery");
    }

    private void manageResourcePayload(DigitalTwinCoapResourceDescriptor resource) {
        logger.info("CoAP Physical Adapter - Starting resource {} payload management", resource.getResourceUri());
        resource.addPayloadListener((value) -> {
            List<? extends WldtEvent<?>> wldtEvents = resource.applyPayloadFunction(value);

            wldtEvents.forEach(e -> {
                try {
                    // TODO: Set content type of published event?
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
        logger.info("CoAP Physical Adapter - Ending resource {} payload management", resource.getResourceUri());
    }
}
