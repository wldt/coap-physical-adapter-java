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

/**
 * The configuration class of a CoAP Physical Adapter.
 * The configuration instance is created using the <code>{@link CoapPhysicalAdapterConfigurationBuilder}</code> class, whose instance can be retrieved calling the <code>builder()</code> method.
 */
public class CoapPhysicalAdapterConfiguration {
    private final PhysicalAssetDescription pad = new PhysicalAssetDescription();

    // COAP SERVER INFO
    private final String ip;
    private final int port;

    // CONTENT FORMAT PREFERENCE
    private int preferredContentFormat = MediaTypeRegistry.TEXT_PLAIN;

    // WLDT EVENT TYPES
    private String defaultEventType = "event";
    private final Map<String, String> customEventTypes = new TreeMap<>();

    // WLDT ACTION TYPES
    private String defaultActuatorActionType = "action";
    private String defaultPostActionType = "toggle";
    private String defaultPutActionType = "parameter";

    private final Map<String, String> customActionTypes = new TreeMap<>();

    // WLDT ACTION CONTENT TYPES
    private String defaultActuatorActionContentType = MediaTypeRegistry.toString(MediaTypeRegistry.TEXT_PLAIN);
    private String defaultPostActionContentType = MediaTypeRegistry.toString(MediaTypeRegistry.TEXT_PLAIN);
    private String defaultPutActionContentType = MediaTypeRegistry.toString(MediaTypeRegistry.TEXT_PLAIN);

    private final Map<String, String> customActionContentTypes = new TreeMap<>();

    // ADAPTER COAP RESOURCES
    private Set<PhysicalAssetResource> resources = new HashSet<>();

    private final Map<String, String> resourceKeyNameAssociationMap = new HashMap<>();

    // RESOURCES OBSERVABILITY/POLLING
    private boolean observabilitySupport = true;
    private boolean autoUpdateTimerSupport = true;
    private long autoUpdateInterval = 5000;

    // RESOURCES LISTENERS
    private boolean automaticResourceListening = true;
    private Map<String, PhysicalAssetResourceListener.ListenerType> customResourceListeningMap = new TreeMap<>();

    // RESOURCE DISCOVERY CONFIGURATION
    private boolean resourceDiscoverySupport;
    private Supplier<Set<PhysicalAssetResource>> customResourceDiscoveryFunction;
    private List<String> ignoredResources = new ArrayList<>();

    // COAP TO WLDT DATA TRANSLATION
    private BiFunction<String, byte[], List<? extends WldtEvent<?>>> defaultPropertyBodyTranslator;
    private BiFunction<String, String, List<? extends WldtEvent<?>>> defaultEventTranslator;

    private Map<String, BiFunction<String, byte[], List<? extends WldtEvent<?>>>> customPropertyBodyTranslators = new TreeMap<>();
    private Map<String, BiFunction<String, String, List<? extends WldtEvent<?>>>> customEventTranslators = new TreeMap<>();

    // WLDT TO COAP DATA TRANSLATION
    private Function<PhysicalAssetActionWldtEvent<?>, Request> defaultActionEventTranslator;
    private Map<String, Function<PhysicalAssetActionWldtEvent<?>, Request>> customActionEventTranslators = new TreeMap<>();


    // CUSTOM REQUEST HANDLERS
    private Function<Request, CoapResponse> customPropertyRequestFunction;  // WLDT property (GET request)
    private Function<Request, CoapResponse> customActionRequestFunction;    // WLDT action (POST/PUT request)


    protected CoapPhysicalAdapterConfiguration(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    /**
     * Creates a new instance of the CoAP Physical Adapter configuration builder.
     * @param ip The IP address of the CoAP server.
     * @param port The port of the CoAP server.
     * @return A new instance of the CoAP Physical Adapter configuration builder.
     */
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
        return resourceDiscoverySupport;
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
        return automaticResourceListening;
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
        return this.customActionTypes.containsKey(resourceName) ?
                this.customActionTypes.get(resourceName) :
                this.defaultActuatorActionType;
    }

    public String getPostActionType(String resourceName) {
        return this.customActionTypes.containsKey(resourceName) ?
                this.customActionTypes.get(resourceName) :
                this.defaultPostActionType;
    }

    public String getPutActionType(String resourceName) {
        return this.customActionTypes.containsKey(resourceName) ?
                this.customActionTypes.get(resourceName) :
                this.defaultPutActionType;
    }

    public String getActuatorActionContentType(String resourceName) {
        return this.customActionContentTypes.containsKey(resourceName) ?
                this.customActionContentTypes.get(resourceName) :
                this.defaultActuatorActionContentType;
    }

    public String getPostActionContentType(String resourceName) {
        return this.customActionContentTypes.containsKey(resourceName) ?
                this.customActionContentTypes.get(resourceName) :
                this.defaultPostActionContentType;
    }

    public String getPutActionContentType(String resourceName) {
        return this.customActionContentTypes.containsKey(resourceName) ?
                this.customActionContentTypes.get(resourceName) :
                this.defaultPutActionContentType;
    }

    public boolean isObservabilityEnabled() {
        return observabilitySupport;
    }

    public boolean isAutoUpdateTimerEnabled() {
        return autoUpdateTimerSupport;
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

    protected void setDefaultPostActionType(String actionType) {
        this.defaultPostActionType = actionType;
    }

    protected void setDefaultPutActionType(String actionType) {
        this.defaultPutActionType = actionType;
    }

    protected void addCustomActionType(String resourceName, String actionType) {
        this.customActionTypes.put(resourceName, actionType);
    }

    // ACTION CONTENT TYPES

    protected void setDefaultActuatorActionContentType(String contentType) {
        this.defaultActuatorActionContentType = contentType;
    }

    protected void setDefaultPostActionContentType(String contentType) {
        this.defaultPostActionContentType = contentType;
    }

    protected void setDefaultPutActionContentType(String contentType) {
        this.defaultPutActionContentType = contentType;
    }

    protected void addCustomActionContentType(String resourceName, String contentType) {
        this.customActionContentTypes.put(resourceName, contentType);
    }

    // MANUAL RESOURCE ADDITION

    protected void setResources(Set<PhysicalAssetResource> resources) {
        this.resources = resources;
    }

    // OBSERVABILITY & POLLING

    protected void enableObservability(boolean enable) {
        this.observabilitySupport = enable;
    }

    protected void enableAutoUpdateTimer(boolean enable) {
        this.autoUpdateTimerSupport = enable;
    }

    protected void setAutoUpdateInterval(long autoUpdateInterval) {
        this.autoUpdateInterval = autoUpdateInterval;
    }

    protected void setAutomaticResourceListening(boolean enable) {
        this.automaticResourceListening = enable;
    }

    protected void setCustomResourceListeningMap(Map<String, PhysicalAssetResourceListener.ListenerType> customResourceListeningMap) {
        this.customResourceListeningMap = customResourceListeningMap;
    }

    // RESOURCE DISCOVERY

    protected void enableResourceDiscoverySupport(boolean enable) {
        this.resourceDiscoverySupport = enable;
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

    public Map<String, String> getResourceKeyNameAssociationMap() {
        return resourceKeyNameAssociationMap;
    }
}
