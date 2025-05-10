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

/**
 * The configuration builder for a CoAP Physical Adapter.
 * The builder instance is obtained by calling the <code>builder()</code> method of <code>{@link CoapPhysicalAdapterConfiguration}</code> class.
 * After the configuration is completed, the adapter configuration can be obtained calling the <code>build()</code> method.
 */
public class CoapPhysicalAdapterConfigurationBuilder {
    CoapPhysicalAdapterConfiguration configuration;

    private final List<UnprocessedResource> unprocessedResources = new ArrayList<>(); // List of unprocessed manually added resources

    /**
     * Creates a new CoapPhysicalAdapterConfigurationBuilder instance.
     * Called inside the <code>{@link CoapPhysicalAdapterConfiguration}</code>'s <code>build()</code> method.
     *
     * @param ip   the server address
     * @param port the server port
     */
    protected CoapPhysicalAdapterConfigurationBuilder(String ip, int port) {
        configuration = new CoapPhysicalAdapterConfiguration(ip, port);
    }

    /**
     * Builds the configuration instance by checking beforehand that everything needed is provided correctly.
     * After checking the data is correct, it processes the manually added resources adding them to the configuration.
     *
     * @return The configuration instance.
     * @throws CoapPhysicalAdapterConfigurationException In the case of the configuration being invalid.
     */
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

    /**
     * Sets the preferred content format used during the communication in the case of the server supporting it.
     * @param preferredContentFormat The preferred CoAP content format.
     * @return The builder instance.
     */
    public CoapPhysicalAdapterConfigurationBuilder setPreferredContentFormat(int preferredContentFormat) {
        configuration.setPreferredContentFormat(preferredContentFormat);
        return this;
    }

    /**
     * Sets the default WLDT event notification type.
     * @param eventType The WLDT event notification type
     * @return The builder instance.
     */
    public CoapPhysicalAdapterConfigurationBuilder setDefaultEventType(String eventType) {
        configuration.setDefaultEventType(eventType);
        return this;
    }

    /**
     * Adds a WLDT event notification type only to the specified resource.
     * @param resourceName The resource to apply the type to.
     * @param eventType The WLDT event notification type.
     * @return The builder instance.
     */
    public CoapPhysicalAdapterConfigurationBuilder addCustomEventType(String resourceName, String eventType) {
        configuration.addCustomEventType(resourceName, eventType);
        return this;
    }

    /**
     * Adds a WLDT action type only to the specified resource supporting both POST & PUT requests.
     * @param resourceName The resource to apply the type to.
     * @param actionType The WLDT action type.
     * @return The builder instance.
     */
    public CoapPhysicalAdapterConfigurationBuilder addCustomActionType(String resourceName, String actionType) {
        configuration.addCustomActionType(resourceName, actionType);
        return this;
    }

    /**
     * Sets the default WLDT action type for CoAP resources supporting both POST & PUT requests.
     * @param actionType The WLDT action type
     * @return The builder instance.
     */
    public CoapPhysicalAdapterConfigurationBuilder setDefaultActuatorActionType(String actionType) {
        configuration.setDefaultActuatorActionType(actionType);
        return this;
    }

    /**
     * Sets the default WLDT action type for CoAP resources supporting only POST requests.
     * @param actionType The WLDT action type
     * @return The builder instance.
     */
    public CoapPhysicalAdapterConfigurationBuilder setDefaultPostActionType(String actionType) {
        configuration.setDefaultPostActionType(actionType);
        return this;
    }

    /**
     * Sets the default WLDT action type for CoAP resources supporting only PUT requests.
     * @param actionType The WLDT action type
     * @return The builder instance.
     */
    public CoapPhysicalAdapterConfigurationBuilder setDefaultPutActionType(String actionType) {
        configuration.setDefaultPutActionType(actionType);
        return this;
    }

    /**
     * Adds a WLDT action content type only to the specified resource.
     * @param resourceName The resource name.
     * @param actionContentType The WLDT action content type.
     * @return The builder instance.
     */
    public CoapPhysicalAdapterConfigurationBuilder addCustomActionContentType(String resourceName, String actionContentType) {
        configuration.addCustomActionContentType(resourceName, actionContentType);
        return this;
    }

    /**
     * Sets the default WLDT action content type for CoAP resources supporting both POST & PUT requests.
     * @param actuatorActionContentType The WLDT action type
     * @return The builder instance.
     */
    public CoapPhysicalAdapterConfigurationBuilder setDefaultActuatorActionContentType(String actuatorActionContentType) {
        configuration.setDefaultActuatorActionContentType(actuatorActionContentType);
        return this;
    }

    /**
     * Sets the default WLDT action content type for CoAP resources supporting only POST requests.
     * @param postActionContentType The WLDT action type
     * @return The builder instance.
     */
    public CoapPhysicalAdapterConfigurationBuilder setDefaultPostActionContentType(String postActionContentType) {
        configuration.setDefaultPostActionContentType(postActionContentType);
        return this;
    }

    /**
     * Sets the default WLDT action content type for CoAP resources supporting only PUT requests.
     * @param putActionContentType The WLDT action type
     * @return The builder instance.
     */
    public CoapPhysicalAdapterConfigurationBuilder setDefaultPutActionContentType(String putActionContentType) {
        configuration.setDefaultPutActionContentType(putActionContentType);
        return this;
    }

    /**
     * Sets the list of already processed resources to the provided instance.
     * @param resources A set containing all the resources.
     * @return The builder instance.
     */
    public CoapPhysicalAdapterConfigurationBuilder setResources(Set<PhysicalAssetResource> resources) {
        configuration.setResources(resources);
        return this;
    }

    /**
     * Adds a new physical asset resource to the list of manually added resources.
     * Each resource added this way will be processed by the <code>build()</code> method invoked at the end of the configuration.
     * @param name The resource name.
     * @param resourceType The resource type.
     * @param contentType The resource preferred content type.
     * @param hasPostSupport A flag indicating if the resource supports POST requests.
     * @param hasPutSupport A flag indicating if the resource supports PUT requests.
     * @param observable A flag indicating if the resource supports observation.
     * @return The builder instance.
     */
    public CoapPhysicalAdapterConfigurationBuilder addResource(String name, String resourceType, int contentType, boolean hasPostSupport, boolean hasPutSupport, boolean observable) {
        this.unprocessedResources.add(new UnprocessedResource(name, resourceType, contentType, hasPostSupport, hasPutSupport, observable));
        return this;
    }

    /**
     * Enables/disables the automatic observation process of the Physical Adapter.
     * @param enable A flag indicating whether to enable or disable the observation.
     * @return The builder instance.
     */
    public CoapPhysicalAdapterConfigurationBuilder enableObservability(boolean enable) {
        configuration.enableObservability(enable);
        return this;
    }

    /**
     * Enables/disables the automatic polling process of the Physical Adapter.
     * @param enable A flag indicating whether to enable or disable the polling.
     * @return The builder instance.
     */
    public CoapPhysicalAdapterConfigurationBuilder enableAutoUpdateTimer(boolean enable) {
        configuration.enableAutoUpdateTimer(enable);
        return this;
    }

    /**
     * Sets the polling interval.
     * @param autoUpdateIntervalMs The polling interval (in milliseconds).
     * @return The builder instance.
     */
    public CoapPhysicalAdapterConfigurationBuilder setAutoUpdateInterval(long autoUpdateIntervalMs) {
        configuration.setAutoUpdateInterval(autoUpdateIntervalMs);
        return this;
    }

    /**
     * Enables/disables the automatic resource listening process of the Physical Adapter.
     * If enabled the Adapter will listen to every resource for both property updates and events.
     * @param enable A flag indicating whether to enable or disable automatic resource listening.
     * @return The builder instance.
     */
    public CoapPhysicalAdapterConfigurationBuilder setAutomaticResourceListening(boolean enable) {
        configuration.setAutomaticResourceListening(enable);
        return this;
    }

    /**
     * Adds a map of resources that the Physical Adapter will listen to, specifying if it has to listen to property updates, events, or both.
     * If automatic resource listening is enabled this map will be ignored since the Adapter will automatically listen to everything.
     * @param customResourceListeningMap A map having the name of the resource as key and what to listen to as a value (instance of <code>{@link PhysicalAssetResourceListener.ListenerType}</code>).
     * @return The builder instance.
     */
    public CoapPhysicalAdapterConfigurationBuilder setCustomResourceListeningMap(Map<String, PhysicalAssetResourceListener.ListenerType> customResourceListeningMap) {
        configuration.setCustomResourceListeningMap(customResourceListeningMap);
        return this;
    }

    /**
     * Enables/disables the resource discovery process.
     * @param enable A flag indicating whether to enable or disable the resource discovery process.
     * @return The builder instance.
     */
    public CoapPhysicalAdapterConfigurationBuilder enableResourceDiscoverySupport(boolean enable) {
        configuration.enableResourceDiscoverySupport(enable);
        return this;
    }

    /**
     * Sets a custom resource discovery function that will be used instead of the default standard one.
     * @param customResourceDiscoveryFunction A supplier returning a set of <code>{@link PhysicalAssetResource}</code> instances.
     * @return The builder instance.
     */
    public CoapPhysicalAdapterConfigurationBuilder setCustomResourceDiscoveryFunction(Supplier<Set<PhysicalAssetResource>> customResourceDiscoveryFunction) {
        configuration.setCustomResourceDiscoveryFunction(customResourceDiscoveryFunction);
        return this;
    }

    /**
     * Sets a list of resources which will be ignored by the default resource discovery.
     * This list will be ignored if a custom resource discovery is used.
     * @param ignoredResources A list containing the names of the resources to ignore.
     * @return The builder instance.
     */
    public CoapPhysicalAdapterConfigurationBuilder setIgnoredResources(List<String> ignoredResources) {
        configuration.setIgnoredResources(ignoredResources);
        return this;
    }

    /**
     * Adds a resource to the list of resources ignored by the default resource discovery.
     * @param name The name of the resource to ignore.
     * @return The builder instance.
     */
    public CoapPhysicalAdapterConfigurationBuilder ignoreResource(String name) {
        configuration.ignoreResource(name);
        return this;
    }

    /**
     * Sets the default function to convert a resource's payload into a WLDT property event.
     * @param defaultPropertyBodyTranslator A function which receives as inputs the resource's key and payload, and returns a list of <code>{@link WldtEvent}</code> instances.
     * @return The builder instance.
     */
    public CoapPhysicalAdapterConfigurationBuilder setDefaultPropertyBodyTranslator(BiFunction<String, byte[], List<? extends WldtEvent<?>>> defaultPropertyBodyTranslator) {
        configuration.setDefaultPropertyBodyTranslator(defaultPropertyBodyTranslator);
        return this;
    }

    /**
     * Sets the default function to convert a resource's event into a WLDT event notification.
     * @param defaultEventTranslator A function which receives as inputs the resource's key and event message, and returns a list of <code>{@link WldtEvent}</code> instances.
     * @return The builder instance.
     */
    public CoapPhysicalAdapterConfigurationBuilder setDefaultEventTranslator(BiFunction<String, String, List<? extends WldtEvent<?>>> defaultEventTranslator) {
        configuration.setDefaultEventTranslator(defaultEventTranslator);
        return this;
    }

    /**
     * Sets a map containing the property translation functions of specific resources.
     * @param customPropertyBodyTranslators A map containing the resource names as keys and the translation functions as values.
     * @return The builder instance.
     */
    public CoapPhysicalAdapterConfigurationBuilder setCustomPropertyBodyTranslators(Map<String, BiFunction<String, byte[], List<? extends WldtEvent<?>>>> customPropertyBodyTranslators) {
        configuration.setCustomPropertyBodyTranslators(customPropertyBodyTranslators);
        return this;
    }

    /**
     * Sets a map containing the event translation functions of specific resources.
     * @param customEventTranslators A map containing the resource names as keys and the translation functions as values.
     * @return The builder instance.
     */
    public CoapPhysicalAdapterConfigurationBuilder setCustomEventTranslators(Map<String, BiFunction<String, String, List<? extends WldtEvent<?>>>> customEventTranslators) {
        configuration.setCustomEventTranslators(customEventTranslators);
        return this;
    }

    /**
     * Sets the default function to convert a WLDT action event into the request to send to the Physical Asset.
     * @param defaultActionEventTranslator A function which receives as input the action event and returns a CoAP request.
     * @return The builder instance.
     */
    public CoapPhysicalAdapterConfigurationBuilder setDefaultActionEventTranslator(Function<PhysicalAssetActionWldtEvent<?>, Request> defaultActionEventTranslator) {
        configuration.setDefaultActionEventTranslator(defaultActionEventTranslator);
        return this;
    }

    /**
     * Sets a map containing the action translation functions of specific resources.
     * @param customActionEventTranslators A map containing the resource names as keys and the translation functions as values
     * @return The builder instance.
     */
    public CoapPhysicalAdapterConfigurationBuilder setCustomActionEventTranslators(Map<String, Function<PhysicalAssetActionWldtEvent<?>, Request>> customActionEventTranslators) {
        configuration.setCustomActionEventTranslators(customActionEventTranslators);
        return this;
    }

    /**
     * Sets the function which sends CoAP GET request to the Physical Asset.
     * @param customPropertyRequestFunction A function which receives a CoAP request as input and returns the CoAP response.
     * @return The builder instance.
     */
    public CoapPhysicalAdapterConfigurationBuilder setCustomPropertyRequestFunction(Function<Request, CoapResponse> customPropertyRequestFunction) {
        configuration.setCustomPropertyRequestFunction(customPropertyRequestFunction);
        return this;
    }

    /**
     * Sets the function which sends CoAP POST & PUT request to the Physical Asset.
     * @param customActionRequestFunction A function which receives a CoAP request as input and returns the CoAP response.
     * @return The builder instance.
     */
    public CoapPhysicalAdapterConfigurationBuilder setCustomActionRequestFunction(Function<Request, CoapResponse> customActionRequestFunction) {
        configuration.setCustomActionRequestFunction(customActionRequestFunction);
        return this;
    }
}
