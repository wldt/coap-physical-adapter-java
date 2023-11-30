package it.wldt.adapter.coap.physical;

import it.wldt.adapter.coap.physical.resources.assets.DigitalTwinActionResource;
import it.wldt.adapter.coap.physical.resources.discovery.DiscoveredResource;
import it.wldt.adapter.coap.physical.resources.CoapResourceDescriptor;
import it.wldt.adapter.coap.physical.resources.assets.DigitalTwinResource;
import it.wldt.adapter.coap.physical.resources.assets.core.DigitalTwinActuatorResource;
import it.wldt.adapter.coap.physical.resources.assets.core.DigitalTwinParameterResource;
import it.wldt.adapter.coap.physical.resources.assets.core.DigitalTwinReadOnlyResource;
import it.wldt.adapter.coap.physical.resources.assets.core.DigitalTwinSensorResource;
import it.wldt.adapter.coap.physical.resources.methods.CoapPostSupport;
import it.wldt.adapter.coap.physical.resources.methods.CoapPutSupport;
import it.wldt.adapter.physical.ConfigurablePhysicalAdapter;
import it.wldt.adapter.physical.PhysicalAssetAction;
import it.wldt.adapter.physical.PhysicalAssetEvent;
import it.wldt.adapter.physical.PhysicalAssetProperty;
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
 * @see DigitalTwinResource
 */
public class CoapPhysicalAdapter extends ConfigurablePhysicalAdapter<CoapPhysicalAdapterConfiguration> {
    private static final Logger logger = LoggerFactory.getLogger(CoapPhysicalAdapter.class);

    public CoapPhysicalAdapter(String id, CoapPhysicalAdapterConfiguration configuration) {
        super(id, configuration);
    }

    @Override
    public void onIncomingPhysicalAction(PhysicalAssetActionWldtEvent<?> physicalAssetActionWldtEvent) {
        logger.info("CoAP Physical Adapter - Received incoming physical action: {}", physicalAssetActionWldtEvent);

        if (physicalAssetActionWldtEvent == null) {
            logger.info("CoAP Physical Adapter - Received null action");
            return;
        }

        logger.info("CoAP Physical Adapter - Incoming physical action: {}", physicalAssetActionWldtEvent);

        String[] splittedActionKey = physicalAssetActionWldtEvent.getActionKey().split(" ");

        String method = splittedActionKey[0];
        CoapResourceDescriptor resource = getConfiguration().getResources().get(splittedActionKey[1]);

        if (resource == null) {
            logger.error("CoAP Physical Adapter - Incoming action directed to unregistered resource: {}", physicalAssetActionWldtEvent);
            return;
        }

        byte[] body = ((DigitalTwinActionResource) resource).applyActionFunction(physicalAssetActionWldtEvent);
        String ct = ((DigitalTwinActionResource) resource).getActionContentType();

        switch (method) {
            case CoapPostSupport.ACTION_KEY -> {
                if (resource instanceof CoapPostSupport) {
                    ((CoapPostSupport) resource).sendPOST(body, ct);
                } else {
                    logger.error("CoAP Physical Adapter - Incoming action method is not supported by resource: {}", physicalAssetActionWldtEvent);
                }
            }
            case CoapPutSupport.ACTION_KEY -> {
                if (resource instanceof CoapPutSupport) {
                    ((CoapPutSupport) resource).sendPUT(body, ct);
                }
                else {
                    logger.error("CoAP Physical Adapter - Incoming action method is not supported by resource: {}", physicalAssetActionWldtEvent);
                }
            }
            default -> {
                logger.error("CoAP Physical Adapter - Incoming action has unsupported key method: {}", physicalAssetActionWldtEvent);
            }
        }
    }

    @Override
    public void onAdapterStart() {
        logger.info("CoAP Physical Adapter - Starting");
        try {
            if (getConfiguration().getResourceDiscoveryFlag()) {
                logger.info("CoAP Physical Adapter - CoAP client discovering resources");
                discoverCoapResources();
            }

            getConfiguration().getResources().values().forEach(this::manageResource);

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

        // Create a new CoAP client ready for resource discovery
        CoapClient coapClient = new CoapClient(getConfiguration().getServerConnectionString());

        // Create a set of discovered resource objects
        Set<DiscoveredResource> discoveredResources;

        if (getConfiguration().getResourceDiscoveryFunction() != null) {
            // If resource discovery has a custom function get resources from that
            discoveredResources = getConfiguration().getResourceDiscoveryFunction().discover(coapClient);
        } else {
            // Else use default CoAP CoRE resource discovery (/.well-known/core)
            discoveredResources = new HashSet<>();
            Set<WebLink> linkSet = coapClient.discover();

            for (WebLink link : linkSet) {
                String uri = link.getURI();
                String resourceType = link.getAttributes().getFirstAttributeValue(DiscoveredResource.WKC_ATTR_RESOURCE_TYPE);
                List<String> supportedContentTypes = link.getAttributes().getAttributeValues(DiscoveredResource.WKC_ATTR_CONTENT_TYPE);
                String linkInterface = link.getAttributes().getFirstAttributeValue(DiscoveredResource.WKC_ATTR_RESOURCE_INTERFACE);
                boolean observable = link.getAttributes().containsAttribute(DiscoveredResource.WKC_ATTR_OBSERVABLE);

                int contentType = -1;
                for (String ct: supportedContentTypes) {
                    if (getConfiguration().getPreferredContentFormat() == Integer.parseInt(ct)) {
                        contentType = Integer.parseInt(ct);
                    }
                }

                DiscoveredResource.Interface resourceInterface = DiscoveredResource.Interface.fromString(linkInterface);

                if (contentType < 0) {
                    // If ct is not defined in the Link set it to text/plain
                    contentType = MediaTypeRegistry.TEXT_PLAIN;
                }

                discoveredResources.add(new DiscoveredResource(uri, resourceType, contentType, resourceInterface, observable));
            }
        }

        for (DiscoveredResource dr : discoveredResources) {
            if (dr.uri() == null || dr.uri().isBlank() || dr.resourceInterface() == null ||
                    dr.resourceInterface() == DiscoveredResource.Interface.UNKNOWN) {
                // If resource does not contain necessary info proceed to the next one
                continue;
            }

            // Separate uri from server address
            String uri = dr.uri().substring(dr.uri().indexOf('/'));

            // Get WLDT key. If rt is not defined use uri, otherwise use <rt>.<uri>
            String wldtKey = (dr.resourceType() == null || dr.resourceType().isBlank()) ?
                    uri :
                    String.format("%s.%s", dr.resourceType(), uri);

            // Replaces each non-alphanumeric character different from '.' with ''
            wldtKey = wldtKey.replaceAll("[^a-zA-Z0-9.]", "");

            DigitalTwinResource resource = null;

            logger.info("CoAP Physical Adapter - Resource discovery found resource {}", wldtKey);

            switch (dr.resourceInterface()) {
                case SENSOR -> {
                    logger.info("CoAP Physical Adapter - Resource {} is a sensor", wldtKey);
                    if (getConfiguration().getDigitalTwinEventsFlag()) {
                        resource = new DigitalTwinSensorResource<>(getConfiguration().getServerConnectionString(), uri, wldtKey,
                                getConfiguration().getDefaultPropertyBodyProducer(),
                                getConfiguration().getDefaultEventBodyProducer());
                    } else {
                        resource = new DigitalTwinSensorResource<>(getConfiguration().getServerConnectionString(), uri, wldtKey,
                                getConfiguration().getDefaultPropertyBodyProducer());
                    }
                }
                case ACTUATOR -> {
                    logger.info("CoAP Physical Adapter - Resource {} is an actuator", wldtKey);
                    if (getConfiguration().getDigitalTwinEventsFlag()) {
                        resource = new DigitalTwinActuatorResource<>(
                                getConfiguration().getServerConnectionString(),
                                uri,
                                wldtKey,
                                getConfiguration().getDefaultPropertyBodyProducer(),
                                getConfiguration().getDefaultEventBodyProducer(),
                                getConfiguration().getDefaultActionBodyConsumer());
                    } else {
                        resource = new DigitalTwinActuatorResource<>(
                                getConfiguration().getServerConnectionString(),
                                uri,
                                wldtKey,
                                getConfiguration().getDefaultPropertyBodyProducer(),
                                getConfiguration().getDefaultActionBodyConsumer());
                    }
                }
                case PARAMETER -> {
                    logger.info("CoAP Physical Adapter - Resource {} is a parameter", wldtKey);
                    if (getConfiguration().getDigitalTwinEventsFlag()) {
                        resource = new DigitalTwinParameterResource<>(
                                getConfiguration().getServerConnectionString(),
                                uri,
                                wldtKey,
                                getConfiguration().getDefaultPropertyBodyProducer(),
                                getConfiguration().getDefaultEventBodyProducer(),
                                getConfiguration().getDefaultActionBodyConsumer());
                    } else {
                        resource = new DigitalTwinActuatorResource<>(
                                getConfiguration().getServerConnectionString(),
                                uri,
                                wldtKey,
                                getConfiguration().getDefaultPropertyBodyProducer(),
                                getConfiguration().getDefaultActionBodyConsumer());
                    }
                }
                case READ_ONLY -> {
                    logger.info("CoAP Physical Adapter - Resource {} is a read-only", wldtKey);
                    if (getConfiguration().getDigitalTwinEventsFlag()) {
                        resource = new DigitalTwinReadOnlyResource<>(
                                getConfiguration().getServerConnectionString(),
                                uri,
                                wldtKey,
                                getConfiguration().getDefaultPropertyBodyProducer(),
                                getConfiguration().getDefaultEventBodyProducer());
                    } else {
                        resource = new DigitalTwinReadOnlyResource<>(
                                getConfiguration().getServerConnectionString(),
                                uri,
                                wldtKey,
                                getConfiguration().getDefaultPropertyBodyProducer());
                    }
                }
                default -> {    // UNKNOWN (Should not enter here)
                    // If not in CoRE Interfaces format resource is discarded
                    logger.warn("CoAP Physical Adapter - Resource {} is not in CoRE Interfaces format. Discarded.", wldtKey);
                }
            }

            if (resource == null) {
                // If resource is not defined proceed to next one
                // This condition should never enter since it was already checked before, however it is kept for prevention
                continue;
            }

            if (resource instanceof DigitalTwinActuatorResource && getConfiguration().getDefaultPutRequestFunction() != null) {
                ((DigitalTwinActuatorResource<?,?,?>) resource).setPutRequestFunction(getConfiguration().getDefaultPutRequestFunction());
            }
            if (resource instanceof DigitalTwinParameterResource && getConfiguration().getDefaultPutRequestFunction() != null) {
                ((DigitalTwinParameterResource<?,?,?>) resource).setPutRequestFunction(getConfiguration().getDefaultPutRequestFunction());
            }

            if (resource instanceof DigitalTwinActuatorResource && getConfiguration().getDefaultPostRequestFunction() != null) {
                ((DigitalTwinActuatorResource<?,?,?>) resource).setPostRequestFunction(getConfiguration().getDefaultPostRequestFunction());
            }

            // Set resource content type and start observing or auto updating
            resource.setPreferredContentType(dr.contentType());

            if (dr.observable()) {
                resource.startObserving();
            } else if (getConfiguration().getAutoUpdateFlag()){
                resource.setAutoUpdatePeriod(getConfiguration().getAutoUpdatePeriod());
                resource.startAutoUpdate();
            }

            getConfiguration().addResource(resource.getResourceUri(), resource);
        }
        logger.info("CoAP Physical Adapter - Ending resource discovery");
    }

    private void manageResource(DigitalTwinResource resource) {
        logger.info("CoAP Physical Adapter - Starting resource {} payload management", resource.getResourceUri());

        // Add new payload listener
        resource.addPayloadListener(value -> {
            // Apply property body production function and obtain events
            List<? extends WldtEvent<?>> wldtEvents = resource.applyPropertyFunction(value);

            wldtEvents.forEach(e -> {
                // Publish PAP WLDT events
                try {
                    if (e instanceof PhysicalAssetPropertyWldtEvent) {
                        publishPhysicalAssetPropertyWldtEvent((PhysicalAssetPropertyWldtEvent<?>) e);
                    }
                }catch (EventBusException ex) {
                    ex.printStackTrace();
                }
            });
        });

        // Add new event listener
        resource.addEventListener(message -> {
            // Apply event body production function and obtain events
            List<? extends WldtEvent<?>> wldtEvents = resource.applyEventFunction(message);

            wldtEvents.forEach(e -> {
                // Publish PAE WLDT events
                try {
                    if (e instanceof PhysicalAssetEventWldtEvent) {
                        publishPhysicalAssetEventWldtEvent((PhysicalAssetEventWldtEvent<?>) e);
                    }
                } catch (EventBusException ex) {
                    ex.printStackTrace();
                }
            });
        });

        // Create property asset and add it to the PAD
        PhysicalAssetProperty<?> property = new PhysicalAssetProperty<>(resource.getPropertyKey(), 0.0);
        getConfiguration().getPhysicalAssetDescription().getProperties().add(property);

        // Create event asset and add it to the PAD
        PhysicalAssetEvent event = new PhysicalAssetEvent(resource.getPropertyKey(), MediaTypeRegistry.toString(MediaTypeRegistry.TEXT_PLAIN));
        getConfiguration().getPhysicalAssetDescription().getEvents().add(event);

        if (resource instanceof CoapPostSupport) {
            // If resource can receive "change" action events create the action asset and add it to the PAD
            PhysicalAssetAction action = new PhysicalAssetAction(
                    String.format("%s %s", CoapPostSupport.ACTION_KEY, resource.getResourceUri()),
                    resource.getPropertyKey(),
                    MediaTypeRegistry.toString(resource.getPreferredContentType()));

            getConfiguration().getPhysicalAssetDescription().getActions().add(action);
        }
        if (resource instanceof CoapPutSupport) {
            // If resource can receive "update" action events create the action asset and add it to the PAD
            PhysicalAssetAction action = new PhysicalAssetAction(
                    String.format("%s %s", CoapPutSupport.ACTION_KEY, resource.getResourceUri()),
                    resource.getPropertyKey(),
                    MediaTypeRegistry.toString(resource.getPreferredContentType()));

            getConfiguration().getPhysicalAssetDescription().getActions().add(action);
        }

        logger.info("CoAP Physical Adapter - Ending resource {} payload management", resource.getResourceUri());
    }
}
