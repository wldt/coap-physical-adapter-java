package it.wldt.adapter.coap.physical;

import it.wldt.adapter.coap.physical.configuration.CoapPhysicalAdapterConfiguration;
import it.wldt.adapter.coap.physical.model.PhysicalAssetResource;
import it.wldt.adapter.coap.physical.model.PhysicalAssetResourceListener;
import it.wldt.adapter.physical.*;
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
import java.util.*;
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
            String resourceName = getConfiguration().getResourceKeyNameAssociationMap().get(physicalActionEvent.getActionKey());
            Optional<PhysicalAssetResource> resource = getConfiguration().getResources().stream().filter(res -> res.getName().equals(resourceName)).findFirst();

            if (resource.isPresent()) {
                if (getConfiguration().getCustomActionEventTranslators().containsKey(resource.get().getName())) {
                    resource.get().sendAction(getConfiguration().getCustomActionEventTranslators().get(resource.get().getName()).apply(physicalActionEvent));
                } else {
                    resource.get().sendAction(getConfiguration().getDefaultActionEventTranslator().apply(physicalActionEvent));
                }
                logger.info("{} - CoAP physical adapter invoked action on resource", super.getId());
            } else {
                logger.warn("{} - CoAP physical adapter received action to unregistered resource", super.getId());
            }
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

        // Resource discovery

        try {
            discoverResources();
        } catch (Exception e) {
            logger.error("{} - CoAP physical adapter failed to discover resources", super.getId(), e);
            notifyPhysicalAdapterUnBound("CoAP physical adapter failed to discover resources");
            return;
        }

        // Check if resources are present

        if (getConfiguration().getResources().isEmpty()) {
            logger.error("{} - CoAP physical adapter has no resources", super.getId());
            notifyPhysicalAdapterUnBound("CoAP physical adapter has no resources");
            return;
        }

        // Adapter starting process

        logger.info("{} - CoAP physical adapter generating Physical Asset Description (PAD)", super.getId());
        getConfiguration().getResources().forEach(resource -> {
            // -- Add adapter as listener --

            if (getConfiguration().getResourceNotificationsSupport()) {
                resource.addListener(this, ListenerType.ALL);
            } else if (getConfiguration().getCustomResourceNotificationsMap() != null && getConfiguration().getCustomResourceNotificationsMap().containsKey(resource.getName())) {
                resource.addListener(this, getConfiguration().getCustomResourceNotificationsMap().get(resource.getName()));
            }

            // -- Create the Physical Asset Description (PAD) --

            String wldtKey = resource.getResourceType().trim().isEmpty() ? resource.getName() : resource.getResourceType().concat(".").concat(resource.getName());
            getConfiguration().getResourceKeyNameAssociationMap().put(wldtKey, resource.getName());

            // Add properties & events

            getConfiguration().getPhysicalAssetDescription().getProperties().add(new PhysicalAssetProperty<>(wldtKey, 0.0));
            getConfiguration().getPhysicalAssetDescription().getEvents().add(new PhysicalAssetEvent(wldtKey, getConfiguration().getEventType(resource.getName())));

            // Add actions

            String contentType = null;
            String actionType = null;
            if (resource.isPostSupported() && resource.isPutSupported()) {
                contentType = getConfiguration().getActuatorActionContentType(resource.getName());
                actionType = getConfiguration().getActuatorActionType(resource.getName());
            } else if (resource.isPutSupported()) {
                contentType = getConfiguration().getPutActionContentType(resource.getName());
                actionType = getConfiguration().getPutActionType(resource.getName());
            } else if (resource.isPostSupported()) {
                contentType = getConfiguration().getPostActionContentType(resource.getName());
                actionType = getConfiguration().getPostActionType(resource.getName());
            }

            if (contentType != null && actionType != null) {
                getConfiguration().getPhysicalAssetDescription().getActions().add(new PhysicalAssetAction(wldtKey, actionType, contentType));
            }

            // -- Start observation & polling (adds relationships) --

            if (getConfiguration().isObservabilityEnabled() && resource.isObservable()) {
                resource.startObservation();

                getConfiguration().getPhysicalAssetDescription().getRelationships().add(new PhysicalAssetRelationship<>(wldtKey, "observation"));
            } else if (getConfiguration().isAutoUpdateTimerEnabled()) {
                resource.startAutoUpdate(getConfiguration().getAutoUpdateInterval());

                getConfiguration().getPhysicalAssetDescription().getRelationships().add(new PhysicalAssetRelationship<>(wldtKey, "polling"));
            }
        });

        try {
            notifyPhysicalAdapterBound(getConfiguration().getPhysicalAssetDescription());
        } catch (PhysicalAdapterException | EventBusException e) {
            logger.error("{} - CoAP physical adapter binding notification failed", super.getId(), e);
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
        if (!getConfiguration().getResourceDiscoverySupport()) {
            return;
        }

        logger.info("{} - CoAP physical adapter starting resource discovery", super.getId());

        Set<PhysicalAssetResource> discoveredResources;

        if (getConfiguration().getCustomResourceDiscoveryFunction() != null) {
            discoveredResources = getConfiguration().getCustomResourceDiscoveryFunction().get();
        } else {
            CoapClient client = new CoapClient(getConfiguration().getServerConnectionString());
            discoveredResources = new HashSet<>();

            try {
                Set<WebLink> links = client.discover();

                links.forEach(link -> {
                    // Get basic resource information
                    String uri = link.getURI().replaceFirst("/", "");
                    String resourceType = Optional.ofNullable(link.getAttributes().getFirstAttributeValue("rt")).orElse("");
                    int contentType = link.getAttributes().getAttributeValues("ct").contains(Integer.toString(getConfiguration().getPreferredContentFormat())) ?
                            getConfiguration().getPreferredContentFormat() :
                            MediaTypeRegistry.UNDEFINED;
                    String resourceInterface = link.getAttributes().getFirstAttributeValue("if");
                    boolean observable = link.getAttributes().containsAttribute("obs");

                    // Check POST & PUT support
                    boolean postSupport = resourceInterface.equals("core.a");
                    boolean putSupport = resourceInterface.equals("core.a") || resourceInterface.equals("core.p");

                    // Set translators
                    BiFunction<String, byte[], List<? extends WldtEvent<?>>> propertyTranslator = getConfiguration().getCustomPropertyBodyTranslators().containsKey(uri) ?
                            getConfiguration().getCustomPropertyBodyTranslators().get(uri) :
                            getConfiguration().getDefaultPropertyBodyTranslator();

                    BiFunction<String, String, List<? extends WldtEvent<?>>> eventTranslator = getConfiguration().getCustomEventTranslatorsMap().containsKey(uri) ?
                            getConfiguration().getCustomEventTranslatorsMap().get(uri) :
                            getConfiguration().getDefaultEventTranslator();

                    // Add resources
                    discoveredResources.add(new PhysicalAssetResource(
                            getConfiguration(),
                            uri,
                            resourceType,
                            contentType,
                            propertyTranslator,
                            postSupport,
                            putSupport,
                            eventTranslator,
                            observable));
                });
            } catch (ConnectorException | IOException e) {
                logger.error("{} - CoAP physical adapter failed to discover resources", super.getId(), e);
            }
        }

        getConfiguration().getResources().addAll(discoveredResources);
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
