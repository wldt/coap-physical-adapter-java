package it.wldt.adapter.coap.physical;

import it.wldt.adapter.coap.physical.discovery.DiscoveredResource;
import it.wldt.adapter.coap.physical.resource.asset.CoapSensorResourceDescriptor;
import it.wldt.adapter.coap.physical.resource.asset.DigitalTwinCoapResourceDescriptor;
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
import java.util.HashSet;
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

            getConfiguration().getResources().values().forEach(this::manageResourcePayload);

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

    /**
     * The method responsible for the resource discovery.
     * Note that if in the discovery function a resource with {@code Interface.UNKNOWN} is passed it will be ignored by the function
     *
     * @throws ConnectorException
     * @throws IOException
     */
    public void discoverCoapResources() throws ConnectorException, IOException {
        logger.info("CoAP Physical Adapter - Starting resource discovery");

        CoapClient coapClient = new CoapClient(getConfiguration().getServerConnectionString());

        Set<DiscoveredResource> discoveredResources;

        if (getConfiguration().getResourceDiscoveryFunction() != null) {
            discoveredResources = getConfiguration().getResourceDiscoveryFunction().discover(coapClient);
        } else {
            discoveredResources = new HashSet<>();
            Set<WebLink> linkSet = coapClient.discover();

            for (WebLink link : linkSet) {
                String uri = link.getURI();
                String resourceType = link.getAttributes().getFirstAttributeValue(DiscoveredResource.WKC_ATTR_RESOURCE_TYPE);
                String linkInterface = link.getAttributes().getFirstAttributeValue(DiscoveredResource.WKC_ATTR_RESOURCE_INTERFACE);
                boolean observable = link.getAttributes().containsAttribute(DiscoveredResource.WKC_ATTR_OBSERVABLE);

                DiscoveredResource.Interface resourceInterface;

                switch (linkInterface) {
                    case "core.s" -> resourceInterface = DiscoveredResource.Interface.SENSOR;
                    case "core.a" -> resourceInterface = DiscoveredResource.Interface.ACTUATOR;
                    default -> resourceInterface = DiscoveredResource.Interface.UNKNOWN;
                }

                discoveredResources.add(new DiscoveredResource(uri, resourceType, resourceInterface, observable));
            }
        }

        for (DiscoveredResource dr : discoveredResources) {
            if (dr.uri() != null && !dr.uri().isBlank()) {
                String uri = dr.uri().substring(dr.uri().indexOf('/'));

                String wldtKey;

                if (dr.resourceType() != null && !dr.resourceType().isBlank()) {
                    wldtKey = String.format("%s.%s", dr.resourceType(), uri);
                } else {
                    wldtKey = uri;
                }
                wldtKey = wldtKey.replaceAll("[^a-zA-Z0-9.]", "");
                // Replaces each non-alphanumeric character different from '.'

                DigitalTwinCoapResourceDescriptor resource = null;

                logger.info("CoAP Physical Adapter - Resource discovery found resource {}", wldtKey);

                if (dr.resourceInterface() != null && !(dr.resourceInterface() == DiscoveredResource.Interface.UNKNOWN)) {
                    switch (dr.resourceInterface()) {
                        case SENSOR -> {
                            logger.info("CoAP Physical Adapter - Resource {} is a sensor", wldtKey);
                            if (getConfiguration().getDigitalTwinEventsFlag()) {
                                resource = new CoapSensorResourceDescriptor<>(getConfiguration().getServerConnectionString(), uri, wldtKey, getConfiguration().getDefaultPropertyBodyProducer(), getConfiguration().getDefaultEventBodyProducer());
                            } else {
                                resource = new CoapSensorResourceDescriptor<>(getConfiguration().getServerConnectionString(), uri, wldtKey, getConfiguration().getDefaultPropertyBodyProducer());
                            }
                        }
                        case ACTUATOR -> {
                            // TODO: How to implement actuator?
                            if (getConfiguration().getDigitalTwinEventsFlag()) {
                            }
                            logger.info("CoAP Physical Adapter - Resource {} is an actuator", wldtKey);
                        }
                        default -> {
                            // If not in CoRE Interfaces format resource is discarded
                            logger.warn("CoAP Physical Adapter - Resource {} is not in CoRE Interfaces format. Discarded.", wldtKey);
                        }
                    }

                }

                if (resource != null) {
                    resource.setPreferredContentType(getConfiguration().getPreferredContentFormat());

                    if (dr.observable()) {
                        resource.startObserving();
                    } else if (getConfiguration().getAutoUpdateFlag()){
                        resource.setAutoUpdatePeriod(getConfiguration().getAutoUpdatePeriod());
                        resource.startAutoUpdate();
                    }

                    getConfiguration().addResource(dr.uri(), resource);
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
                    // TODO: Set content type of published event? Could be impossible since payload can be modified with body producers in specific resource classes
                    if (e instanceof PhysicalAssetPropertyWldtEvent) {
                        publishPhysicalAssetPropertyWldtEvent((PhysicalAssetPropertyWldtEvent<?>) e);
                    }
                }catch (EventBusException ex) {
                    ex.printStackTrace();
                }
            });
        });

        resource.addErrorListener(message -> {
            List<? extends WldtEvent<?>> wldtEvents = resource.applyErrorFunction(message);

            wldtEvents.forEach(e -> {
                try {
                    if (e instanceof PhysicalAssetEventWldtEvent) {
                        publishPhysicalAssetEventWldtEvent((PhysicalAssetEventWldtEvent<?>) e);
                    }
                } catch (EventBusException ex) {
                    ex.printStackTrace();
                }
            });
        });

        // TODO: Since actuators can have a GET method and theoretically could be observable, what if event is action event?

        logger.info("CoAP Physical Adapter - Ending resource {} payload management", resource.getResourceUri());
    }
}
