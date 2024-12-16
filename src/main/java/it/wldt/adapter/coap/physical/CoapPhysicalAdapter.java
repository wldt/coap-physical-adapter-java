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
import java.util.function.Function;

public class CoapPhysicalAdapter
        extends ConfigurablePhysicalAdapter<CoapPhysicalAdapterConfiguration>
        implements PhysicalAssetResourceListener {
    private static final Logger logger = LoggerFactory.getLogger(CoapPhysicalAdapter.class);

    //private CoapPhysicalAdapterConfiguration configuration;

    public CoapPhysicalAdapter(String id, CoapPhysicalAdapterConfiguration configuration) {
        super(id, configuration);
    }

    @Override
    public void onIncomingPhysicalAction(PhysicalAssetActionWldtEvent<?> physicalActionEvent) {
        if (physicalActionEvent == null) {
            logger.warn("{} - CoAP physical adapter received null incoming action", super.getId());
            return;
        }
        logger.info("{} - CoAP physical adapter received incoming physical action", super.getId());


        String[] splittedActionKey = physicalActionEvent.getActionKey().split(" ");

        try {
            PhysicalAssetResource resource = (PhysicalAssetResource) getConfiguration().getResources().stream().filter(res -> res.getName().equals(splittedActionKey[1])).toArray()[0];

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
            String resourceKey = (resource.getResourceType() != null && !resource.getResourceType().isBlank() ?
                    String.format("%s.%s", resource.getResourceType(), resource.getName()) :
                    resource.getName()
                    );

            PhysicalAssetProperty<?> property = new PhysicalAssetProperty<>(resourceKey, 0.0);
            getConfiguration().getPhysicalAssetDescription().getProperties().add(property);

            PhysicalAssetEvent event = new PhysicalAssetEvent(resourceKey, getConfiguration().getEventType());
            getConfiguration().getPhysicalAssetDescription().getEvents().add(event);

            if (resource.isPostSupported()) {
                PhysicalAssetAction postAction = new PhysicalAssetAction(
                        String.format("change %s", resourceKey),
                        getConfiguration().getPostActionType(),
                        getConfiguration().getPostActionContentType()
                );
                getConfiguration().getPhysicalAssetDescription().getActions().add(postAction);
            }
            if (resource.isPutSupported()) {
                PhysicalAssetAction putAction = new PhysicalAssetAction(
                        String.format("update %s", resourceKey),
                        getConfiguration().getPutActionType(),
                        getConfiguration().getPutActionContentType()
                );
                getConfiguration().getPhysicalAssetDescription().getActions().add(putAction);
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

    @Override
    public void onAdapterStop() {
        getConfiguration().getResources().clear();
    }

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

                String uri = link.getURI();

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

    private void listenResource(PhysicalAssetResource resource) {
        logger.info("{} - CoAP physical adapter starting resource listening ({})", super.getId(), resource.getName());

        if (getConfiguration().isAutomaticResourceListeningEnabled()) {
            resource.addListener(this, PhysicalAssetResourceListener.ListenerType.ALL);
        } else if (getConfiguration().getCustomResourceListeningMap().containsKey(resource.getName())) {
            resource.addListener(this, getConfiguration().getCustomResourceListeningMap().get(resource.getName()));
        }
    }

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
