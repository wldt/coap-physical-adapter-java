package it.wldt.adapter.coap.physical.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.wldt.adapter.coap.physical.model.PhysicalAssetResource;
import it.wldt.adapter.coap.physical.model.PhysicalAssetResourceListener;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.event.PhysicalAssetActionWldtEvent;
import it.wldt.core.event.WldtEvent;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.Request;

import java.io.*;
import java.nio.file.Files;
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

    private Set<PhysicalAssetResource> resources = new HashSet<>();
    private final Map<String, String> resourceKeyNameAssociationMap = new HashMap<>();

    CoapPhysicalAdapterConfigurationData configurationData;

    // RESOURCE DISCOVERY CONFIGURATION
    private Supplier<Set<PhysicalAssetResource>> customResourceDiscoveryFunction;

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
        this.configurationData = new CoapPhysicalAdapterConfigurationData(ip, port);
    }

    protected CoapPhysicalAdapterConfiguration(CoapPhysicalAdapterConfigurationData configurationData) {
        this.configurationData = configurationData;
    }

    /**
     * Creates a new instance of the CoAP Physical Adapter configuration builder from a YAML file.
     * @param yamlConfig The YAML file containing the configuration.
     * @return A new instance of the CoAP Physical Adapter configuration builder.
     * @throws IOException If an error occurs while reading the YAML file.
     */
    public static CoapPhysicalAdapterConfigurationBuilder fromYaml(File yamlConfig) throws IOException {
        try(InputStream inputStream = Files.newInputStream(yamlConfig.toPath())) {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            mapper.findAndRegisterModules();

            return new CoapPhysicalAdapterConfigurationBuilder(mapper.readValue(inputStream, CoapPhysicalAdapterConfigurationData.class));
        }
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
        return configurationData.getIp();
    }

    public int getPort() {
        return configurationData.getPort();
    }

    public String getServerConnectionString() {
        return String.format("coap://%s:%d", configurationData.getIp(), configurationData.getPort());
    }

    public boolean getResourceDiscoverySupport() {
        return configurationData.getResourceDiscoverySupport();
    }

    public Supplier<Set<PhysicalAssetResource>> getCustomResourceDiscoveryFunction() {
        return this.customResourceDiscoveryFunction;
    }

    public int getPreferredContentFormat() {
        return configurationData.getPreferredContentFormat();
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
        return configurationData.getIgnoredResources();
    }

    public boolean getResourceNotificationsSupport() {
        return configurationData.getResourceNotificationsSupport();
    }

    public Map<String, PhysicalAssetResourceListener.ListenerType> getCustomResourceNotificationsMap() {
        return configurationData.getCustomResourceNotificationsMap();
    }

    public String getEventType(String eventName) {
        return this.configurationData.getCustomWldtEventTypesMap().containsKey(eventName) ?
                this.configurationData.getCustomWldtEventTypesMap().get(eventName) :
                this.configurationData.getDefaultWldtEventType();
    }

    public String getActuatorActionType(String resourceName) {
        return this.configurationData.getCustomWldtActionTypesMap().containsKey(resourceName) ?
                this.configurationData.getCustomWldtActionTypesMap().get(resourceName) :
                this.configurationData.getDefaultWldtActuatorActionType();
    }

    public String getPostActionType(String resourceName) {
        return this.configurationData.getCustomWldtActionTypesMap().containsKey(resourceName) ?
                this.configurationData.getCustomWldtActionTypesMap().get(resourceName) :
                this.configurationData.getDefaultWldtPostActionType();
    }

    public String getPutActionType(String resourceName) {
        return this.configurationData.getCustomWldtActionTypesMap().containsKey(resourceName) ?
                this.configurationData.getCustomWldtActionTypesMap().get(resourceName) :
                this.configurationData.getDefaultWldtPutActionType();
    }

    public String getActuatorActionContentType(String resourceName) {
        return this.configurationData.getCustomWldtActionContentTypes().containsKey(resourceName) ?
                this.configurationData.getCustomWldtActionContentTypes().get(resourceName) :
                this.configurationData.getDefaultActuatorWldtActionContentType();
    }

    public String getPostActionContentType(String resourceName) {
        return this.configurationData.getCustomWldtActionContentTypes().containsKey(resourceName) ?
                this.configurationData.getCustomWldtActionContentTypes().get(resourceName) :
                this.configurationData.getDefaultPostWldtActionContentType();
    }

    public String getPutActionContentType(String resourceName) {
        return this.configurationData.getCustomWldtActionContentTypes().containsKey(resourceName) ?
                this.configurationData.getCustomWldtActionContentTypes().get(resourceName) :
                this.configurationData.getDefaultPutWldtActionContentType();
    }

    public boolean isObservabilityEnabled() {
        return configurationData.getObservabilitySupport();
    }

    public boolean isAutoUpdateTimerEnabled() {
        return configurationData.getAutoUpdateTimerSupport();
    }

    public long getAutoUpdateInterval() {
        return configurationData.getAutoUpdateInterval();
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
        this.configurationData.setPreferredContentFormat(preferredContentFormat);
    }

    // EVENT TYPES

    protected void setDefaultEventType(String defaultEventType) {
        this.configurationData.setDefaultWldtEventType(defaultEventType);
    }

    protected void addCustomEventType(String resourceName, String eventType) {
        this.configurationData.getCustomWldtEventTypesMap().put(resourceName, eventType);
    }

    // ACTION TYPES

    protected void setDefaultActuatorActionType(String actionType) {
        this.configurationData.setDefaultWldtActuatorActionType(actionType);
    }

    protected void setDefaultPostActionType(String actionType) {
        this.configurationData.setDefaultWldtPostActionType(actionType);
    }

    protected void setDefaultPutActionType(String actionType) {
        this.configurationData.setDefaultWldtPutActionType(actionType);
    }

    protected void addCustomActionType(String resourceName, String actionType) {
        this.configurationData.getCustomWldtActionTypesMap().put(resourceName, actionType);
    }

    // ACTION CONTENT TYPES

    protected void setDefaultActuatorActionContentType(String contentType) {
        this.configurationData.setDefaultActuatorWldtActionContentType(contentType);
    }

    protected void setDefaultPostActionContentType(String contentType) {
        this.configurationData.setDefaultPostWldtActionContentType(contentType);
    }

    protected void setDefaultPutActionContentType(String contentType) {
        this.configurationData.setDefaultPutWldtActionContentType(contentType);
    }

    protected void addCustomActionContentType(String resourceName, String contentType) {
        this.configurationData.getCustomWldtActionContentTypes().put(resourceName, contentType);
    }

    // MANUAL RESOURCE ADDITION

    protected void setResources(Set<PhysicalAssetResource> resources) {
        this.resources = resources;
    }

    // OBSERVABILITY & POLLING

    protected void setObservabilitySupport(boolean enable) {
        this.configurationData.setObservabilitySupport(enable);
    }

    protected void setAutoUpdateTimerSupport(boolean enable) {
        this.configurationData.setAutoUpdateTimerSupport(enable);
    }

    protected void setAutoUpdateInterval(long autoUpdateInterval) {
        this.configurationData.setAutoUpdateInterval(autoUpdateInterval);
    }

    protected void setAutomaticResourceListening(boolean enable) {
        this.configurationData.setResourceNotificationsSupport(enable);
    }

    // RESOURCE DISCOVERY

    protected void enableResourceDiscoverySupport(boolean enable) {
        this.configurationData.setResourceDiscoverySupport(enable);
    }

    protected void setCustomResourceDiscoveryFunction(Supplier<Set<PhysicalAssetResource>> customResourceDiscoveryFunction) {
        this.customResourceDiscoveryFunction = customResourceDiscoveryFunction;
    }

    protected void ignoreResources(Collection<String> ignoredResources) {
        this.configurationData.getIgnoredResources().addAll(ignoredResources);
    }

    protected void ignoreResource(String name) {
        this.configurationData.getIgnoredResources().add(name);
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
