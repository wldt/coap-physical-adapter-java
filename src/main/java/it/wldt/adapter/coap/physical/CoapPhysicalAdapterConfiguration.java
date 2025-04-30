package it.wldt.adapter.coap.physical;

import it.wldt.adapter.coap.physical.model.PhysicalAssetResource;
import it.wldt.adapter.coap.physical.model.PhysicalAssetResourceListener;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.event.PhysicalAssetActionWldtEvent;
import it.wldt.core.event.WldtEvent;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class CoapPhysicalAdapterConfiguration {
    private PhysicalAssetDescription pad = new PhysicalAssetDescription();

    private String ip;
    private int port;

    private int preferredContentFormat = MediaTypeRegistry.TEXT_PLAIN;

    // EVENT TYPES
    private String defaultEventType = "event";
    private Map<String, String> customEventTypes = new TreeMap<>();

    // ACTION TYPES
    private String defaultActuatorActionType = "action";
    private String defaultPostActionType = "toggle";
    private String defaultPutActionType = "parameter";

    private Map<String, String> customActuatorActionTypes = new TreeMap<>();
    private Map<String, String> customPostActionTypes = new TreeMap<>();
    private Map<String, String> customPutActionTypes = new TreeMap<>();

    // ACTION CONTENT TYPES
    private String defaultActuatorActionContentType = MediaTypeRegistry.toString(MediaTypeRegistry.TEXT_PLAIN);
    private String defaultPostActionContentType = MediaTypeRegistry.toString(MediaTypeRegistry.TEXT_PLAIN);
    private String defaultPutActionContentType = MediaTypeRegistry.toString(MediaTypeRegistry.TEXT_PLAIN);

    private Map<String, String> customActuatorActionContentTypes = new TreeMap<>();
    private Map<String, String> customPostActionContentTypes = new TreeMap<>();
    private Map<String, String> customPutActionContentTypes = new TreeMap<>();

    private Set<PhysicalAssetResource> resources = new HashSet<>();

    private boolean enableObservability = true;
    private boolean enableAutoUpdateTimer = true;
    private long autoUpdateInterval = 5000;

    private boolean enableAutomaticResourceListening = true;
    private Map<String, PhysicalAssetResourceListener.ListenerType> customResourceListeningMap = new TreeMap<>();

    private boolean enableResourceDiscoverySupport;
    private Supplier<Set<PhysicalAssetResource>> customResourceDiscoveryFunction;
    private List<String> ignoredResources = new ArrayList<>();

    private BiFunction<String, byte[], List<? extends WldtEvent<?>>> defaultPropertyBodyTranslator;
    private Function<PhysicalAssetActionWldtEvent<?>, Request> defaultActionEventTranslator;
    private BiFunction<String, String, List<? extends WldtEvent<?>>> defaultEventTranslator;

    private Map<String, BiFunction<String, byte[], List<? extends WldtEvent<?>>>> customPropertyBodyTranslators = new TreeMap<>();
    private Map<String, Function<PhysicalAssetActionWldtEvent<?>, Request>> customActionEventTranslators = new TreeMap<>();
    private Map<String, BiFunction<String, String, List<? extends WldtEvent<?>>>> customEventTranslators = new TreeMap<>();

    private Function<Request, CoapResponse> customPropertyRequestFunction;
    private Function<Request, CoapResponse> customActionRequestFunction;

    protected CoapPhysicalAdapterConfiguration(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public static CoapPhysicalAdapterConfigurationBuilder builder(String ip, int port) {
        return new CoapPhysicalAdapterConfigurationBuilder(ip, port);
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getServerConnectionString() {
        return String.format("coap://%s:%d", ip, port);
    }

    public boolean isResourceDiscoveryEnabled() {
        return enableResourceDiscoverySupport;
    }

    public Supplier<Set<PhysicalAssetResource>> getCustomResourceDiscoveryFunction() {
        return this.customResourceDiscoveryFunction;
    }

    public int getPreferredContentFormat() {
        return preferredContentFormat;
    }

    public void addResources(Set<PhysicalAssetResource> discoveredResources) {
        this.resources.addAll(discoveredResources);
    }

    public Map<String, BiFunction<String, byte[], List<? extends WldtEvent<?>>>> getCustomPropertyBodyTranslators() {
        return this.customPropertyBodyTranslators;
    }
    public Map<String, BiFunction<String, String, List<? extends WldtEvent<?>>>> getCustomEventTranslatorsMap() {
        return customEventTranslators;
    }

    public BiFunction<String, byte[], List<? extends WldtEvent<?>>> getDefaultPropertyBodyTranslator() {
        return defaultPropertyBodyTranslator;
    }

    public BiFunction<String, String, List<? extends WldtEvent<?>>> getDefaultEventTranslator() {
        return defaultEventTranslator;
    }

    public Set<PhysicalAssetResource> getResources() {
        return resources;
    }

    public PhysicalAssetDescription getPhysicalAssetDescription() {
        return pad;
    }

    public List<String> getIgnoredResources() {
        return ignoredResources;
    }

    public boolean isAutomaticResourceListeningEnabled() {
        return enableAutomaticResourceListening;
    }

    public Map<String, PhysicalAssetResourceListener.ListenerType> getCustomResourceListeningMap() {
        return customResourceListeningMap;
    }

    public String getEventType(String eventName) {
        return this.customEventTypes.containsKey(eventName) ?
                this.customEventTypes.get(eventName) :
                this.defaultEventType;
    }

    public String getActuatorActionType(String resourceName) {
        return this.customActuatorActionTypes.containsKey(resourceName) ?
                this.customActuatorActionTypes.get(resourceName) :
                this.defaultActuatorActionType;
    }

    public String getPostActionType(String resourceName) {
        return this.customPostActionTypes.containsKey(resourceName) ?
                this.customPostActionTypes.get(resourceName) :
                this.defaultPostActionType;
    }

    public String getPutActionType(String resourceName) {
        return this.customPutActionTypes.containsKey(resourceName) ?
                this.customPutActionTypes.get(resourceName) :
                this.defaultPutActionType;
    }

    public String getActuatorActionContentType(String resourceName) {
        return this.customActuatorActionContentTypes.containsKey(resourceName) ?
                this.customActuatorActionContentTypes.get(resourceName) :
                this.defaultActuatorActionContentType;
    }

    public String getPostActionContentType(String resourceName) {
        return this.customPostActionContentTypes.containsKey(resourceName) ?
                this.customPostActionContentTypes.get(resourceName) :
                this.defaultPostActionContentType;
    }

    public String getPutActionContentType(String resourceName) {
        return this.customPutActionContentTypes.containsKey(resourceName) ?
                this.customPutActionContentTypes.get(resourceName) :
                this.defaultPutActionContentType;
    }

    public boolean isObservabilityEnabled() {
        return enableObservability;
    }

    public boolean isAutoUpdateTimerEnabled() {
        return enableAutoUpdateTimer;
    }

    public long getAutoUpdateInterval() {
        return autoUpdateInterval;
    }

    public Function<Request, CoapResponse> getCustomPropertyRequestFunction() {
        return customPropertyRequestFunction;
    }

    public Function<Request, CoapResponse> getCustomActionRequestFunction() {
        return customActionRequestFunction;
    }

    public Map<String, Function<PhysicalAssetActionWldtEvent<?>, Request>> getCustomActionEventTranslators() {
        return customActionEventTranslators;
    }

    public Function<PhysicalAssetActionWldtEvent<?>, Request> getDefaultActionEventTranslator() {
        return defaultActionEventTranslator;
    }

    protected void setPreferredContentFormat(int preferredContentFormat) {
        this.preferredContentFormat = preferredContentFormat;
    }

    // EVENT TYPES

    protected void setDefaultEventType(String defaultEventType) {
        this.defaultEventType = defaultEventType;
    }

    protected void addCustomEventType(String resourceName, String eventType) {
        this.customEventTypes.put(resourceName, eventType);
    }

    // ACTION TYPES

    protected void setDefaultActuatorActionType(String actionType) {
        this.defaultActuatorActionType = actionType;
    }

    protected void addCustomActuatorActionType(String resourceName, String actionType) {
        this.customActuatorActionTypes.put(resourceName, actionType);
    }

    protected void setDefaultPostActionType(String actionType) {
        this.defaultPostActionType = actionType;
    }

    protected void addCustomPostActionType(String resourceName, String actionType) {
        this.customPostActionTypes.put(resourceName, actionType);
    }

    protected void setDefaultPutActionType(String actionType) {
        this.defaultPutActionType = actionType;
    }

    protected void addCustomPutActionType(String resourceName, String actionType) {
        this.customPutActionTypes.put(resourceName, actionType);
    }

    // ACTION CONTENT TYPES

    protected void setDefaultActuatorActionContentType(String contentType) {
        this.defaultActuatorActionContentType = contentType;
    }

    protected void addCustomActuatorActionContentType(String resourceName, String contentType) {
        this.customActuatorActionContentTypes.put(resourceName, contentType);
    }

    protected void setDefaultPostActionContentType(String contentType) {
        this.defaultPostActionContentType = contentType;
    }

    protected void addCustomPostActionContentType(String resourceName, String contentType) {
        this.customPostActionContentTypes.put(resourceName, contentType);
    }

    protected void setDefaultPutActionContentType(String contentType) {
        this.defaultPutActionContentType = contentType;
    }

    protected void addCustomPutActionContentType(String resourceName, String contentType) {
        this.customPutActionContentTypes.put(resourceName, contentType);
    }

    // MANUAL RESOURCE ADDITION

    protected void setResources(Set<PhysicalAssetResource> resources) {
        this.resources = resources;
    }

    // OBSERVABILITY & POLLING

    protected void enableObservability(boolean enable) {
        this.enableObservability = enable;
    }

    protected void enableAutoUpdateTimer(boolean enable) {
        this.enableAutoUpdateTimer = enable;
    }

    protected void setAutoUpdateInterval(long autoUpdateInterval) {
        this.autoUpdateInterval = autoUpdateInterval;
    }

    protected void enableAutomaticResourceListening(boolean enable) {
        this.enableAutomaticResourceListening = enable;
    }

    protected void setCustomResourceListeningMap(Map<String, PhysicalAssetResourceListener.ListenerType> customResourceListeningMap) {
        this.customResourceListeningMap = customResourceListeningMap;
    }

    // RESOURCE DISCOVERY

    protected void enableResourceDiscoverySupport(boolean enable) {
        this.enableResourceDiscoverySupport = enable;
    }

    protected void setCustomResourceDiscoveryFunction(Supplier<Set<PhysicalAssetResource>> customResourceDiscoveryFunction) {
        this.customResourceDiscoveryFunction = customResourceDiscoveryFunction;
    }

    protected void setIgnoredResources(List<String> ignoredResources) {
        this.ignoredResources = ignoredResources;
    }

    protected void ignoreResource(String name) {
        this.ignoredResources.add(name);
    }

    // BODY TRANSLATORS

    protected void setDefaultPropertyBodyTranslator(BiFunction<String, byte[], List<? extends WldtEvent<?>>> defaultPropertyBodyTranslator) {
        this.defaultPropertyBodyTranslator = defaultPropertyBodyTranslator;
    }

    protected void setDefaultActionEventTranslator(Function<PhysicalAssetActionWldtEvent<?>, Request> defaultActionEventTranslator) {
        this.defaultActionEventTranslator = defaultActionEventTranslator;
    }

    protected void setDefaultEventTranslator(BiFunction<String, String, List<? extends WldtEvent<?>>> defaultEventTranslator) {
        this.defaultEventTranslator = defaultEventTranslator;
    }

    protected void setCustomPropertyBodyTranslators(Map<String, BiFunction<String, byte[], List<? extends WldtEvent<?>>>> customPropertyBodyTranslators) {
        this.customPropertyBodyTranslators = customPropertyBodyTranslators;
    }

    protected void setCustomActionEventTranslators(Map<String, Function<PhysicalAssetActionWldtEvent<?>, Request>> customActionEventTranslators) {
        this.customActionEventTranslators = customActionEventTranslators;
    }

    protected void setCustomEventTranslators(Map<String, BiFunction<String, String, List<? extends WldtEvent<?>>>> customEventTranslators) {
        this.customEventTranslators = customEventTranslators;
    }

    protected void setCustomPropertyRequestFunction(Function<Request, CoapResponse> customPropertyRequestFunction) {
        this.customPropertyRequestFunction = customPropertyRequestFunction;
    }

    protected void setCustomActionRequestFunction(Function<Request, CoapResponse> customActionRequestFunction) {
        this.customActionRequestFunction = customActionRequestFunction;
    }

}
