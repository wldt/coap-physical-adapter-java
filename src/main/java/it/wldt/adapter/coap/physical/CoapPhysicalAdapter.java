package it.wldt.adapter.coap.physical;

import it.wldt.adapter.coap.physical.discovery.DiscoveredResource;
import it.wldt.adapter.coap.physical.resource.CoapResourceDescriptor;
import it.wldt.adapter.coap.physical.resource.asset.DigitalTwinCoapResource;
import it.wldt.adapter.coap.physical.resource.asset.core.interfaces.CoapCoreActuator;
import it.wldt.adapter.coap.physical.resource.asset.core.interfaces.CoapCoreParameter;
import it.wldt.adapter.coap.physical.resource.asset.core.interfaces.CoapCoreReadOnly;
import it.wldt.adapter.coap.physical.resource.asset.core.interfaces.CoapCoreSensor;
import it.wldt.adapter.coap.physical.resource.methods.CoapPostMethod;
import it.wldt.adapter.coap.physical.resource.methods.CoapPutMethod;
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
 * @see DigitalTwinCoapResource
 */
public class CoapPhysicalAdapter extends ConfigurablePhysicalAdapter<CoapPhysicalAdapterConfiguration> {
    private static final Logger logger = LoggerFactory.getLogger(CoapPhysicalAdapter.class);

    public static final String INCOMING_ACTION_POST_KEY = "change";
    public static final String INCOMING_ACTION_PUT_KEY = "update";

    public CoapPhysicalAdapter(String id, CoapPhysicalAdapterConfiguration configuration) {
        super(id, configuration);
    }

    @Override
    public void onIncomingPhysicalAction(PhysicalAssetActionWldtEvent<?> physicalAssetActionWldtEvent) {
        // TODO: Check why it's not entering the function
        logger.info("CoAP Physical Adapter - Incoming physical action: {}", physicalAssetActionWldtEvent);

        String[] splittedActionKey = physicalAssetActionWldtEvent.getActionKey().split(" ");
        String ct = physicalAssetActionWldtEvent.getContentType();

        String method = splittedActionKey[0];
        CoapResourceDescriptor resource = getConfiguration().getResources().get(splittedActionKey[1]);

        if (resource == null) {
            logger.error("CoAP Physical Adapter - Incoming action directed to unregistered resource: {}", physicalAssetActionWldtEvent);

            System.out.println("Unregistered resource: " + splittedActionKey[1]);
            return;
        }

        byte[] body;

        if (physicalAssetActionWldtEvent.getBody() instanceof String) {
            body = ((String) physicalAssetActionWldtEvent.getBody()).getBytes();
        } else if (physicalAssetActionWldtEvent.getBody() instanceof byte[]) {
            body = (byte[]) physicalAssetActionWldtEvent.getBody();
        } else {
            logger.error("CoAP Physical Adapter - Incoming action has unsupported body type: {}", physicalAssetActionWldtEvent);

            System.out.println("Unsupported body type: " + physicalAssetActionWldtEvent.getBody());
            return;
        }

        switch (method) {
            case INCOMING_ACTION_POST_KEY -> {
                if (resource instanceof CoapPostMethod) {
                    ((CoapPostMethod) resource).sendPOST(body, ct);
                } else {
                    System.out.println("POST unsupported");
                    logger.error("CoAP Physical Adapter - Incoming action method is not supported by resource: {}", physicalAssetActionWldtEvent);
                }
            }
            case INCOMING_ACTION_PUT_KEY -> {
                if (resource instanceof CoapPutMethod) {
                    ((CoapPutMethod) resource).sendPUT(body, ct);
                }
                else {
                    System.out.println("PUT unsupported");
                    logger.error("CoAP Physical Adapter - Incoming action method is not supported by resource: {}", physicalAssetActionWldtEvent);
                }
            }
            default -> {
                System.out.println("Method unsupported: " + method);
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

        // Create a new CoAP client ready for resource discovery
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

                DiscoveredResource.Interface resourceInterface = DiscoveredResource.Interface.fromString(linkInterface);

                discoveredResources.add(new DiscoveredResource(uri, resourceType, resourceInterface, observable));
            }
        }

        for (DiscoveredResource dr : discoveredResources) {
            if (dr.uri() == null || dr.uri().isBlank()) {
                continue;
            }

            String uri = dr.uri().substring(dr.uri().indexOf('/'));

            String wldtKey = (dr.resourceType() == null || dr.resourceType().isBlank()) ?
                    uri :
                    String.format("%s.%s", dr.resourceType(), uri);

            // Replaces each non-alphanumeric character different from '.' with ''
            wldtKey = wldtKey.replaceAll("[^a-zA-Z0-9.]", "");

            DigitalTwinCoapResource resource = null;

            logger.info("CoAP Physical Adapter - Resource discovery found resource {}", wldtKey);

            if (dr.resourceInterface() == null || dr.resourceInterface() == DiscoveredResource.Interface.UNKNOWN) {
                continue;
            }

            switch (dr.resourceInterface()) {
                case SENSOR -> {
                    logger.info("CoAP Physical Adapter - Resource {} is a sensor", wldtKey);
                    if (getConfiguration().getDigitalTwinEventsFlag()) {
                        resource = new CoapCoreSensor<>(getConfiguration().getServerConnectionString(), uri, wldtKey,
                                getConfiguration().getDefaultPropertyBodyProducer(),
                                getConfiguration().getDefaultEventBodyProducer());
                    } else {
                        resource = new CoapCoreSensor<>(getConfiguration().getServerConnectionString(), uri, wldtKey,
                                getConfiguration().getDefaultPropertyBodyProducer());
                    }
                }
                case ACTUATOR -> {
                    logger.info("CoAP Physical Adapter - Resource {} is an actuator", wldtKey);
                    if (getConfiguration().getDigitalTwinEventsFlag()) {
                        resource = new CoapCoreActuator<>(getConfiguration().getServerConnectionString(), uri, wldtKey,
                                getConfiguration().getDefaultPropertyBodyProducer(),
                                getConfiguration().getDefaultEventBodyProducer(),
                                getConfiguration().getDefaultActionBodyConsumer());
                    } else {
                        resource = new CoapCoreActuator<>(getConfiguration().getServerConnectionString(), uri, wldtKey,
                                getConfiguration().getDefaultPropertyBodyProducer(),
                                getConfiguration().getDefaultActionBodyConsumer());
                    }
                }
                case PARAMETER -> {
                    logger.info("CoAP Physical Adapter - Resource {} is a parameter", wldtKey);
                    if (getConfiguration().getDigitalTwinEventsFlag()) {
                        resource = new CoapCoreParameter<>(getConfiguration().getServerConnectionString(), uri, wldtKey,
                                getConfiguration().getDefaultPropertyBodyProducer(),
                                getConfiguration().getDefaultEventBodyProducer(),
                                getConfiguration().getDefaultActionBodyConsumer());
                    } else {
                        resource = new CoapCoreActuator<>(getConfiguration().getServerConnectionString(), uri, wldtKey,
                                getConfiguration().getDefaultPropertyBodyProducer(),
                                getConfiguration().getDefaultActionBodyConsumer());
                    }
                }
                case READ_ONLY -> {
                    logger.info("CoAP Physical Adapter - Resource {} is a read-only", wldtKey);
                    if (getConfiguration().getDigitalTwinEventsFlag()) {
                        resource = new CoapCoreReadOnly<>(getConfiguration().getServerConnectionString(), uri, wldtKey,
                                getConfiguration().getDefaultPropertyBodyProducer(),
                                getConfiguration().getDefaultEventBodyProducer());
                    } else {
                        resource = new CoapCoreReadOnly<>(getConfiguration().getServerConnectionString(), uri, wldtKey,
                                getConfiguration().getDefaultPropertyBodyProducer());
                    }
                }
                default -> {    // UNKNOWN
                    // If not in CoRE Interfaces format resource is discarded
                    logger.warn("CoAP Physical Adapter - Resource {} is not in CoRE Interfaces format. Discarded.", wldtKey);
                }
            }

            if (resource == null) {
                continue;
            }

            resource.setPreferredContentType(getConfiguration().getPreferredContentFormat());

            if (dr.observable()) {
                resource.startObserving();
            } else if (getConfiguration().getAutoUpdateFlag()){
                resource.setAutoUpdatePeriod(getConfiguration().getAutoUpdatePeriod());
                resource.startAutoUpdate();
            }

            getConfiguration().addResource(dr.uri(), resource);
        }
        logger.info("CoAP Physical Adapter - Ending resource discovery");
    }

    private void manageResourcePayload(DigitalTwinCoapResource resource) {
        logger.info("CoAP Physical Adapter - Starting resource {} payload management", resource.getResourceUri());
        resource.addPayloadListener(value -> {
            List<? extends WldtEvent<?>> wldtEvents = resource.applyPropertyFunction(value);

            wldtEvents.forEach(e -> {
                try {
                    // TODO: Set content type of published event? Could be impossible since payload can be modified with body producers in specific resource classes
                    if (e instanceof PhysicalAssetPropertyWldtEvent) {
                        publishPhysicalAssetPropertyWldtEvent((PhysicalAssetPropertyWldtEvent<?>) e);
                    }
                    if (e instanceof PhysicalAssetActionWldtEvent) {
                        // TODO: Manage if physical asset action

                    }
                }catch (EventBusException ex) {
                    ex.printStackTrace();
                }
            });
        });

        resource.addEventListener(message -> {
            List<? extends WldtEvent<?>> wldtEvents = resource.applyEventFunction(message);

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

        logger.info("CoAP Physical Adapter - Ending resource {} payload management", resource.getResourceUri());
    }
}
