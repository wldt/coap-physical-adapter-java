package it.wldt.adapter.coap.physical;

import it.wldt.adapter.coap.physical.model.PhysicalAssetResource;
import it.wldt.adapter.coap.physical.model.PhysicalAssetResourceListener;
import it.wldt.adapter.physical.*;
import it.wldt.adapter.physical.event.PhysicalAssetActionWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.core.event.WldtEvent;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.WebLink;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * CoAP Physical Adapter implementation.
 * <p>
 * This adapter is used to interact with CoAP resources.
 * It supports resource discovery, observability, and automatic resource listening.
 * </p>
 */
public class CoapPhysicalAdapter
        extends ConfigurablePhysicalAdapter<CoapPhysicalAdapterConfiguration>
        implements PhysicalAssetResourceListener {
    private static final Logger logger = LoggerFactory.getLogger(CoapPhysicalAdapter.class);
    private static final String COAP_PHYSICAL_ADAPTER_ID = "coap-physical-adapter";

    /**
     * Constructs a new CoapPhysicalAdapter with the given ID and configuration.
     *
     * @param id            the ID of the adapter
     * @param configuration the configuration for the adapter
     */
    public CoapPhysicalAdapter(String id, CoapPhysicalAdapterConfiguration configuration) {
        super(id, configuration);
    }

    /**
     * Handles an incoming physical action event for the CoAP physical adapter.
     * <p>
     * This method processes the received action event, identifies the corresponding
     * resource, and sends the action to the resource using the appropriate translator.
     * If the resource is unknown or an error occurs, it logs the issue.
     * </p>
     *
     * @param physicalActionEvent the incoming physical action event to process
     */
    @Override
    public void onIncomingPhysicalAction(PhysicalAssetActionWldtEvent<?> physicalActionEvent) {
        if (physicalActionEvent == null) {
            logger.warn("{} - CoAP physical adapter received null incoming action", super.getId());
            return;
        }
        logger.info("{} - CoAP physical adapter received incoming physical action", super.getId());

        try {
            PhysicalAssetResource resource = (PhysicalAssetResource) getConfiguration().getResources().stream().filter(res -> res.getName().equals(physicalActionEvent.getActionKey()));

            if (getConfiguration().getCustomActionEventTranslators().containsKey(resource.getName())) {
                resource.sendAction(getConfiguration().getCustomActionEventTranslators().get(resource.getName()).apply(physicalActionEvent));
            } else {
                resource.sendAction(getConfiguration().getDefaultActionEventTranslator().apply(physicalActionEvent));
            }
        } catch (NoSuchElementException e) {
            logger.warn("{} - CoAP physical adapter received action to unknown resource", super.getId());
        } catch (Exception e) {
            logger.error("{} - CoAP physical adapter encountered an error", super.getId(), e);
        }
    }

    /**
     * Starts the CoAP physical adapter.
     * <p>
     * This method performs the following operations:
     * <ul>
     *     <li>Discovers available resources using the {@code discoverResources()} method.</li>
     *     <li>Checks the presence of resources; if no resources are found, notifies that the adapter is unbound.</li>
     *     <li>Starts listening to the found resources.</li>
     *     <li>Generates the physical asset description (PAD) containing properties, events, and actions of the resources.</li>
     *     <li>If enabled, handles observability and automatic updates for resources.</li>
     * </ul>
     * Once the method has performed all its operations without errors, it notifies that the adapter is successfully bound.
     * If any errors occur during execution, appropriate logs are recorded, and the adapter is notified as unbound.
     * </p>
     */
    @Override
    public void onAdapterStart() {
        logger.info("{} - CoAP physical adapter starting", super.getId());

        try {
            discoverResources();
        } catch (Exception e) {
            logger.error("{} - CoAP physical adapter failed to discover resources", super.getId(), e);
            notifyPhysicalAdapterUnBound("CoAP physical adapter failed to discover resources");
            return;
        }

        if (getConfiguration().getResources().isEmpty()) {
            logger.error("{} - CoAP physical adapter has no resources", super.getId());
            notifyPhysicalAdapterUnBound("CoAP physical adapter has no resources");
            return;
        }

        getConfiguration().getResources().forEach(this::listenResource);

        logger.info("{} - CoAP physical adapter generating PAD", super.getId());
        getConfiguration().getResources().forEach(resource -> {
            String resourceKey = (resource.getResourceType() != null && !resource.getResourceType().trim().isEmpty() ?
                    String.format("%s.%s", resource.getResourceType(), resource.getName()) :
                    resource.getName()
            );

            PhysicalAssetProperty<?> property = new PhysicalAssetProperty<>(resourceKey, 0.0);
            getConfiguration().getPhysicalAssetDescription().getProperties().add(property);

            PhysicalAssetEvent event = new PhysicalAssetEvent(resourceKey, getConfiguration().getEventType(resource.getName()));
            getConfiguration().getPhysicalAssetDescription().getEvents().add(event);

            PhysicalAssetAction action = null;
            if (resource.isPostSupported() && resource.isPutSupported()) {
                action = new PhysicalAssetAction(
                        resourceKey,
                        getConfiguration().getActuatorActionType(resource.getName()),
                        getConfiguration().getActuatorActionContentType(resource.getName())
                );
            } else if (resource.isPutSupported()) {
                action = new PhysicalAssetAction(
                        resourceKey,
                        getConfiguration().getPutActionType(resource.getName()),
                        getConfiguration().getPutActionContentType(resource.getName())
                );
            }else if (resource.isPostSupported()) {
                action = new PhysicalAssetAction(
                        resourceKey,
                        getConfiguration().getPostActionType(resource.getName()),
                        getConfiguration().getPostActionContentType(resource.getName())
                );
            }

            if (action != null) {
                getConfiguration().getPhysicalAssetDescription().getActions().add(action);
            }
        });

        getConfiguration().getResources().forEach(resource -> {
            if (getConfiguration().isObservabilityEnabled() && resource.isObservable()) {
                resource.startObservation();

                getConfiguration().getPhysicalAssetDescription().getRelationships().add(new PhysicalAssetRelationship<>(resource.getName(), "observation"));
            } else if (getConfiguration().isAutoUpdateTimerEnabled()) {
                resource.startAutoUpdate(getConfiguration().getAutoUpdateInterval());

                getConfiguration().getPhysicalAssetDescription().getRelationships().add(new PhysicalAssetRelationship<>(resource.getName(), "auto-update"));
            }
        });

        try {
            notifyPhysicalAdapterBound(getConfiguration().getPhysicalAssetDescription());
        } catch (Exception e) {
            logger.error("{} - CoAP physical adapter bounding notification failed", super.getId(), e);
            notifyPhysicalAdapterUnBound("CoAP physical adapter bounding notification failed");
        }
    }

    /**
     * Stops the CoAP physical adapter and clears all the registered resources.
     */
    @Override
    public void onAdapterStop() {
        getConfiguration().getResources().clear();
    }

   /**
    * Discovers resources available on the CoAP server.
    * <p>
    * This method performs resource discovery by either using a custom resource discovery function,
    * if provided, or by querying the CoAP server directly. It processes the discovered resources,
    * extracting their URI, resource type, content type, and other attributes. It also determines
    * the resource's capabilities, such as support for POST and PUT methods, observability, and
    * event translation. Discovered resources are added to the adapter's configuration unless they
    * are explicitly ignored.
    * </p>
    *
    * @throws Exception if an error occurs during resource discovery
    */
    private void discoverResources() throws Exception {
        if (!getConfiguration().isResourceDiscoveryEnabled()) {
            return;
        }

        logger.info("{} - CoAP physical adapter starting resource discovery", super.getId());

        Set<PhysicalAssetResource> discoveredResources;

        if (getConfiguration().getCustomResourceDiscoveryFunction() != null) {
            discoveredResources = getConfiguration().getCustomResourceDiscoveryFunction().get();
        } else {
            CoapClient coapClient = new CoapClient(getConfiguration().getServerConnectionString());
            discoveredResources = new HashSet<>();

            Set<WebLink> webLinks = coapClient.discover();

            webLinks.forEach(link -> {
                // URI

                String uri = link.getURI().replaceFirst("/", "");

                // RESOURCE TYPE

                String resourceType = link.getAttributes().getFirstAttributeValue("rt");

                // CONTENT TYPE

                int contentType = MediaTypeRegistry.UNDEFINED;
                if (link.getAttributes().getAttributeValues("ct").contains(Integer.toString(getConfiguration().getPreferredContentFormat()))) {
                    contentType = getConfiguration().getPreferredContentFormat();
                }

                // PAYLOAD TRANSLATORS

                BiFunction<String, byte[], List<? extends WldtEvent<?>>> getRequestTranslator;
                boolean hasPostSupport = false;
                boolean hasPutSupport = false;

                if (getConfiguration().getCustomPropertyBodyTranslators().containsKey(uri)) {
                    getRequestTranslator = getConfiguration().getCustomPropertyBodyTranslators().get(uri);
                } else {
                    getRequestTranslator = getConfiguration().getDefaultPropertyBodyTranslator();
                }

                if (link.getAttributes().getFirstAttributeValue("if").equals("core.a")) {       // POST
                    hasPostSupport = true;
                }

                if (link.getAttributes().getFirstAttributeValue("if").equals("core.p") ||
                        link.getAttributes().getFirstAttributeValue("if").equals("core.a")) {   // PUT
                    hasPutSupport = true;
                }

                // EVENT TRANSLATOR

                BiFunction<String, String, List<? extends WldtEvent<?>>> eventTranslator = (
                        getConfiguration().getCustomEventTranslatorsMap().containsKey(uri) ?
                                getConfiguration().getCustomEventTranslatorsMap().get(uri) :
                                getConfiguration().getDefaultEventTranslator()
                        );

                // OBSERVABILITY

                boolean observable = link.getAttributes().containsAttribute("obs");

                // RESOURCE ADDITION

                if (!getConfiguration().getIgnoredResources().contains(uri)) {
                    discoveredResources.add(new PhysicalAssetResource(getConfiguration(), uri, resourceType, contentType, getRequestTranslator, hasPostSupport, hasPutSupport, eventTranslator, observable));
                }
            });
        }
        this.getConfiguration().addResources(discoveredResources);
    }

    /**
     * Adds the adapter instance as listener to the resource
     * @param resource the resource to listen to
     */
    private void listenResource(PhysicalAssetResource resource) {
        logger.info("{} - CoAP physical adapter starting resource listening ({})", super.getId(), resource.getName());

        if (getConfiguration().isAutomaticResourceListeningEnabled()) {
            resource.addListener(this, PhysicalAssetResourceListener.ListenerType.ALL);
        } else if (getConfiguration().getCustomResourceListeningMap().containsKey(resource.getName())) {
            resource.addListener(this, getConfiguration().getCustomResourceListeningMap().get(resource.getName()));
        }
    }

    /**
     * Publishes a physical asset property event containing the received property updates
     * @param resource   The resource which received an update.
     * @param properties The list of updated properties.
     */
    @Override
    public void onPropertyChanged(PhysicalAssetResource resource, List<? extends WldtEvent<?>> properties) {
        properties.forEach(e -> {
            try {
                publishPhysicalAssetPropertyWldtEvent((PhysicalAssetPropertyWldtEvent<?>) e);
            } catch (Exception ex) {
                logger.error("{} - CoAP physical adapter failed to publish property", super.getId(), ex);
            }
        });
    }

    /**
     * Publishes a physical asset event containing the received events
     * @param resource The resource which triggered the event.
     * @param events   The list of events.
     */
    @Override
    public void onEvent(PhysicalAssetResource resource, List<? extends WldtEvent<?>> events) {
        events.forEach(e -> {
            try {
                publishPhysicalAssetEventWldtEvent((PhysicalAssetEventWldtEvent<?>) e);
            } catch (Exception ex) {
                logger.error("{} - CoAP physical adapter failed to publish event", super.getId(), ex);
            }
        });
    }

}
