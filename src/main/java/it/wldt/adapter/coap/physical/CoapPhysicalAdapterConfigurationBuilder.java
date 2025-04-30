package it.wldt.adapter.coap.physical;

import it.wldt.adapter.coap.physical.model.PhysicalAssetResource;
import it.wldt.adapter.coap.physical.model.PhysicalAssetResourceListener;
import it.wldt.adapter.coap.physical.model.UnprocessedResource;
import it.wldt.adapter.physical.event.PhysicalAssetActionWldtEvent;
import it.wldt.core.event.WldtEvent;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.Request;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class CoapPhysicalAdapterConfigurationBuilder {
    CoapPhysicalAdapterConfiguration configuration;

    private List<UnprocessedResource> unprocessedResources = new ArrayList<>();

    protected CoapPhysicalAdapterConfigurationBuilder(String ip, int port) {
        configuration = new CoapPhysicalAdapterConfiguration(ip, port);
    }

    public CoapPhysicalAdapterConfiguration build() throws CoapPhysicalAdapterConfigurationException {
        if (configuration.getIp() == null || configuration.getIp().trim().isEmpty()) {
            throw new CoapPhysicalAdapterConfigurationException("Server address cannot be empty");
        }
        if (configuration.getPort() < 0) {
            throw new CoapPhysicalAdapterConfigurationException("Server port must be positive");
        }

        if (configuration.getAutoUpdateInterval() < 0) {
            throw new CoapPhysicalAdapterConfigurationException("Auto update interval must be positive");
        }

        if (configuration.getDefaultEventTranslator() == null ||
                configuration.getDefaultActionEventTranslator() == null ||
                configuration.getDefaultPropertyBodyTranslator() == null) {
            throw new CoapPhysicalAdapterConfigurationException("Default translators cannot be null");
        }

        Set<PhysicalAssetResource> resources = new HashSet<>();
        unprocessedResources.forEach(res -> {
            resources.add(new PhysicalAssetResource(
                    configuration,
                    res.getName(),
                    res.getResourceType(),
                    res.getContentType(),
                    configuration.getCustomPropertyBodyTranslators().containsKey(res.getName()) ?
                            configuration.getCustomPropertyBodyTranslators().get(res.getName()) :
                            configuration.getDefaultPropertyBodyTranslator(),
                    res.hasPostSupport(),
                    res.hasPutSupport(),
                    configuration.getCustomEventTranslatorsMap().containsKey(res.getName()) ?
                            configuration.getCustomEventTranslatorsMap().get(res.getName()) :
                            configuration.getDefaultEventTranslator(),
                    res.isObservable()
                    ));
        });
        configuration.addResources(resources);

        if (!configuration.isResourceDiscoveryEnabled() && configuration.getResources().isEmpty()) {
            throw new CoapPhysicalAdapterConfigurationException("Resources collection cannot be empty if resource discovery is enabled");
        }

        return configuration;
    }

    public CoapPhysicalAdapterConfigurationBuilder setPreferredContentFormat(int preferredContentFormat) {
        configuration.setPreferredContentFormat(preferredContentFormat);
        return this;
    }


    public CoapPhysicalAdapterConfigurationBuilder setDefaultEventType(String eventType) {
        configuration.setDefaultEventType(eventType);
        return this;
    }

    public CoapPhysicalAdapterConfigurationBuilder addCustomEventType(String resourceName, String eventType) {
        configuration.addCustomEventType(resourceName, eventType);
        return this;
    }

    public CoapPhysicalAdapterConfigurationBuilder setDefaultActuatorActionType(String actionType) {
        configuration.setDefaultActuatorActionType(actionType);
        return this;
    }

    public CoapPhysicalAdapterConfigurationBuilder addCustomActuatorActionType(String resourceName, String actionType) {
        configuration.addCustomActuatorActionType(resourceName, actionType);
        return this;
    }

    public CoapPhysicalAdapterConfigurationBuilder setDefaultPostActionType(String postActionType) {
        configuration.setDefaultPostActionType(postActionType);
        return this;
    }

    public CoapPhysicalAdapterConfigurationBuilder addCustomPostActionType(String resourceName, String postActionType) {
        configuration.addCustomPostActionType(resourceName, postActionType);
        return this;
    }

    public CoapPhysicalAdapterConfigurationBuilder setDefaultPutActionType(String putActionType) {
        configuration.setDefaultPutActionType(putActionType);
        return this;
    }

    public CoapPhysicalAdapterConfigurationBuilder addCustomPutActionType(String resourceName, String putActionType) {
        configuration.addCustomPutActionType(resourceName, putActionType);
        return this;
    }

    public CoapPhysicalAdapterConfigurationBuilder setDefaultActuatorActionContentType(String actuatorActionContentType) {
        configuration.setDefaultActuatorActionContentType(actuatorActionContentType);
        return this;
    }

    public CoapPhysicalAdapterConfigurationBuilder addCustomActuatorActionContentType(String resourceName, String actuatorActionContentType) {
        configuration.addCustomActuatorActionContentType(resourceName, actuatorActionContentType);
        return this;
    }

    public CoapPhysicalAdapterConfigurationBuilder setDefaultPostActionContentType(String postActionContentType) {
        configuration.setDefaultPostActionContentType(postActionContentType);
        return this;
    }

    public CoapPhysicalAdapterConfigurationBuilder addCustomPostActionContentType(String resourceName, String postActionContentType) {
        configuration.addCustomPostActionContentType(resourceName, postActionContentType);
        return this;
    }

    public CoapPhysicalAdapterConfigurationBuilder setDefaultPutActionContentType(String putActionContentType) {
        configuration.setDefaultPutActionContentType(putActionContentType);
        return this;
    }

    public CoapPhysicalAdapterConfigurationBuilder addCustomPutActionContentType(String resourceName, String putActionContentType) {
        configuration.addCustomPutActionContentType(resourceName, putActionContentType);
        return this;
    }

    public CoapPhysicalAdapterConfigurationBuilder setResources(Set<PhysicalAssetResource> resources) {
        configuration.setResources(resources);
        return this;
    }

    public CoapPhysicalAdapterConfigurationBuilder addResource(String name, String resourceType, int contentType, boolean hasPostSupport, boolean hasPutSupport, boolean observable) {
        this.unprocessedResources.add(new UnprocessedResource(name, resourceType, contentType, hasPostSupport, hasPutSupport, observable));
        return this;
    }

    public CoapPhysicalAdapterConfigurationBuilder enableObservability(boolean enable) {
        configuration.enableObservability(enable);
        return this;
    }

    public CoapPhysicalAdapterConfigurationBuilder enableAutoUpdateTimer(boolean enable) {
        configuration.enableAutoUpdateTimer(enable);
        return this;
    }

    public CoapPhysicalAdapterConfigurationBuilder setAutoUpdateInterval(long autoUpdateIntervalMs) {
        configuration.setAutoUpdateInterval(autoUpdateIntervalMs);
        return this;
    }

    public CoapPhysicalAdapterConfigurationBuilder enableAutomaticResourceListening(boolean enable) {
        configuration.enableAutomaticResourceListening(true);
        return this;
    }

    public CoapPhysicalAdapterConfigurationBuilder setCustomResourceListeningMap(Map<String, PhysicalAssetResourceListener.ListenerType> customResourceListeningMap) {
        configuration.setCustomResourceListeningMap(customResourceListeningMap);
        return this;
    }

    public CoapPhysicalAdapterConfigurationBuilder enableResourceDiscoverySupport(boolean enable) {
        configuration.enableResourceDiscoverySupport(enable);
        return this;
    }

    public CoapPhysicalAdapterConfigurationBuilder setCustomResourceDiscoveryFunction(Supplier<Set<PhysicalAssetResource>> customResourceDiscoveryFunction) {
        configuration.setCustomResourceDiscoveryFunction(customResourceDiscoveryFunction);
        return this;
    }

    public CoapPhysicalAdapterConfigurationBuilder setIgnoredResources(List<String> ignoredResources) {
        configuration.setIgnoredResources(ignoredResources);
        return this;
    }

    public CoapPhysicalAdapterConfigurationBuilder ignoreResource(String name) {
        configuration.ignoreResource(name);
        return this;
    }

    public CoapPhysicalAdapterConfigurationBuilder setDefaultPropertyBodyTranslator(BiFunction<String, byte[], List<? extends WldtEvent<?>>> defaultPropertyBodyTranslator) {
        configuration.setDefaultPropertyBodyTranslator(defaultPropertyBodyTranslator);
        return this;
    }

    public CoapPhysicalAdapterConfigurationBuilder setDefaultActionEventTranslator(Function<PhysicalAssetActionWldtEvent<?>, Request> defaultActionEventTranslator) {
        configuration.setDefaultActionEventTranslator(defaultActionEventTranslator);
        return this;
    }

    public CoapPhysicalAdapterConfigurationBuilder setDefaultEventTranslator(BiFunction<String, String, List<? extends WldtEvent<?>>> defaultEventTranslator) {
        configuration.setDefaultEventTranslator(defaultEventTranslator);
        return this;
    }

    public CoapPhysicalAdapterConfigurationBuilder setCustomPropertyBodyTranslators(Map<String, BiFunction<String, byte[], List<? extends WldtEvent<?>>>> customPropertyBodyTranslators) {
        configuration.setCustomPropertyBodyTranslators(customPropertyBodyTranslators);
        return this;
    }

    public CoapPhysicalAdapterConfigurationBuilder setCustomActionEventTranslators(Map<String, Function<PhysicalAssetActionWldtEvent<?>, Request>> customActionEventTranslators) {
        configuration.setCustomActionEventTranslators(customActionEventTranslators);
        return this;
    }

    public CoapPhysicalAdapterConfigurationBuilder setCustomEventTranslators(Map<String, BiFunction<String, String, List<? extends WldtEvent<?>>>> customEventTranslators) {
        configuration.setCustomEventTranslators(customEventTranslators);
        return this;
    }

    public CoapPhysicalAdapterConfigurationBuilder setCustomPropertyRequestFunction(Function<Request, CoapResponse> customPropertyRequestFunction) {
        configuration.setCustomPropertyRequestFunction(customPropertyRequestFunction);
        return this;
    }

    public CoapPhysicalAdapterConfigurationBuilder setCustomActionRequestFunction(Function<Request, CoapResponse> customActionRequestFunction) {
        configuration.setCustomActionRequestFunction(customActionRequestFunction);
        return this;
    }
}
