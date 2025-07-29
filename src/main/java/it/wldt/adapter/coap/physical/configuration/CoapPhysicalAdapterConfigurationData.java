package it.wldt.adapter.coap.physical.configuration;

import it.wldt.adapter.coap.physical.CoapPhysicalAdapter;
import it.wldt.adapter.coap.physical.model.PhysicalAssetResourceListener;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import java.util.*;

/**
 * Configuration data for the CoAP physical adapter.
 * This class holds all the necessary serializable configuration parameters for the CoAP Physical Adapter,
 * including server information, content formats, data synchronization settings,
 * resource discovery options, and WLDT event/action types.
 */
public class CoapPhysicalAdapterConfigurationData {
    // COAP

    // -> SERVER INFO
    private String ip;
    private int port;

    // -> CONTENT
    private int preferredContentFormat = MediaTypeRegistry.TEXT_PLAIN;

    // -> DATA SYNC
    private boolean observabilitySupport = true;
    private boolean autoUpdateTimerSupport = true;
    private long autoUpdateInterval = 5000;

    // -> RESOURCE DISCOVERY

    private boolean resourceDiscoverySupport = true;
    private List<String> ignoredResources = new ArrayList<>();

    // WLDT EVENTS

    // -> WLDT EVENT NOTIFICATION TYPES
    private String defaultWldtEventType = "event";
    private Map<String, String> customWldtEventTypesMap = new TreeMap<>();

    // -> WLDT ACTION EVENT TYPES
    private String defaultWldtActuatorActionType = "action";
    private String defaultWldtPostActionType = "toggle";
    private String defaultWldtPutActionType = "parameter";

    private Map<String, String> customWldtActionTypesMap = new TreeMap<>();

    // -> WLDT ACTION EVENT CONTENT TYPES

    private String defaultActuatorWldtActionContentType = MediaTypeRegistry.toString(MediaTypeRegistry.TEXT_PLAIN);
    private String defaultPostWldtActionContentType = MediaTypeRegistry.toString(MediaTypeRegistry.TEXT_PLAIN);
    private String defaultPutWldtActionContentType = MediaTypeRegistry.toString(MediaTypeRegistry.TEXT_PLAIN);

    private Map<String, String> customWldtActionContentTypes = new TreeMap<>();

    // ADAPTER

    // -> NOTIFICATIONS

    private boolean resourceNotificationsSupport = true;
    private Map<String, PhysicalAssetResourceListener.ListenerType> customResourceNotificationsMap = new TreeMap<>();

    // CONSTRUCTORS

    public CoapPhysicalAdapterConfigurationData() { }

    public CoapPhysicalAdapterConfigurationData(String ip, int port) {
        this.ip = ip;
        this.port = port;

        this.ignoredResources = new ArrayList<>();
        this.customWldtEventTypesMap = new TreeMap<>();
        this.customWldtActionTypesMap = new TreeMap<>();
        this.customWldtActionContentTypes = new TreeMap<>();
    }

    public CoapPhysicalAdapterConfigurationData(String ip, int port, boolean observabilitySupport, boolean autoUpdateTimerSupport, long autoUpdateInterval) {
        this(ip, port);
        this.observabilitySupport = observabilitySupport;
        this.autoUpdateTimerSupport = autoUpdateTimerSupport;
        this.autoUpdateInterval = autoUpdateInterval;
    }

    public CoapPhysicalAdapterConfigurationData(String ip, int port, boolean resourceDiscoverySupport, List<String> ignoredResources) {
        this(ip, port);
        this.resourceDiscoverySupport = resourceDiscoverySupport;
        this.ignoredResources = ignoredResources;
    }

    public CoapPhysicalAdapterConfigurationData(String ip, int port, boolean observabilitySupport, boolean autoUpdateTimerSupport, long autoUpdateInterval, boolean resourceDiscoverySupport, List<String> ignoredResources) {
        this(ip, port);
        this.observabilitySupport = observabilitySupport;
        this.autoUpdateTimerSupport = autoUpdateTimerSupport;
        this.autoUpdateInterval = autoUpdateInterval;
        this.resourceDiscoverySupport = resourceDiscoverySupport;
        this.ignoredResources = ignoredResources;
    }

    public CoapPhysicalAdapterConfigurationData(String ip, int port, int preferredContentFormat, boolean observabilitySupport, boolean autoUpdateTimerSupport, long autoUpdateInterval, boolean resourceDiscoverySupport, List<String> ignoredResources, String defaultWldtEventType, Map<String, String> customWldtEventTypesMap, String defaultWldtActuatorActionType, String defaultWldtPostActionType, String defaultWldtPutActionType, Map<String, String> customWldtActionTypesMap, String defaultActuatorWldtActionContentType, String defaultPostWldtActionContentType, String defaultPutWldtActionContentType, Map<String, String> customWldtActionContentTypes, boolean resourceNotificationsSupport, Map<String, PhysicalAssetResourceListener.ListenerType> customResourceNotificationsMap) {
        this(ip, port);
        this.preferredContentFormat = preferredContentFormat;
        this.observabilitySupport = observabilitySupport;
        this.autoUpdateTimerSupport = autoUpdateTimerSupport;
        this.autoUpdateInterval = autoUpdateInterval;
        this.resourceDiscoverySupport = resourceDiscoverySupport;
        this.ignoredResources = ignoredResources;
        this.defaultWldtEventType = defaultWldtEventType;
        this.customWldtEventTypesMap = customWldtEventTypesMap;
        this.defaultWldtActuatorActionType = defaultWldtActuatorActionType;
        this.defaultWldtPostActionType = defaultWldtPostActionType;
        this.defaultWldtPutActionType = defaultWldtPutActionType;
        this.customWldtActionTypesMap = customWldtActionTypesMap;
        this.defaultActuatorWldtActionContentType = defaultActuatorWldtActionContentType;
        this.defaultPostWldtActionContentType = defaultPostWldtActionContentType;
        this.defaultPutWldtActionContentType = defaultPutWldtActionContentType;
        this.customWldtActionContentTypes = customWldtActionContentTypes;
        this.resourceNotificationsSupport = resourceNotificationsSupport;
        this.customResourceNotificationsMap = customResourceNotificationsMap;
    }

    // GETTERS AND SETTERS

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPreferredContentFormat() {
        return preferredContentFormat;
    }

    public void setPreferredContentFormat(int preferredContentFormat) {
        this.preferredContentFormat = preferredContentFormat;
    }

    public boolean getObservabilitySupport() {
        return observabilitySupport;
    }

    public void setObservabilitySupport(boolean observabilitySupport) {
        this.observabilitySupport = observabilitySupport;
    }

    public boolean getAutoUpdateTimerSupport() {
        return autoUpdateTimerSupport;
    }

    public void setAutoUpdateTimerSupport(boolean autoUpdateTimerSupport) {
        this.autoUpdateTimerSupport = autoUpdateTimerSupport;
    }

    public long getAutoUpdateInterval() {
        return autoUpdateInterval;
    }

    public void setAutoUpdateInterval(long autoUpdateInterval) {
        this.autoUpdateInterval = autoUpdateInterval;
    }

    public boolean getResourceDiscoverySupport() {
        return resourceDiscoverySupport;
    }

    public void setResourceDiscoverySupport(boolean resourceDiscoverySupport) {
        this.resourceDiscoverySupport = resourceDiscoverySupport;
    }

    public List<String> getIgnoredResources() {
        return ignoredResources;
    }

    public String getDefaultWldtEventType() {
        return defaultWldtEventType;
    }

    public void setDefaultWldtEventType(String defaultWldtEventType) {
        this.defaultWldtEventType = defaultWldtEventType;
    }

    public Map<String, String> getCustomWldtEventTypesMap() {
        return customWldtEventTypesMap;
    }

    public String getDefaultWldtActuatorActionType() {
        return defaultWldtActuatorActionType;
    }

    public void setDefaultWldtActuatorActionType(String defaultWldtActuatorActionType) {
        this.defaultWldtActuatorActionType = defaultWldtActuatorActionType;
    }

    public String getDefaultWldtPostActionType() {
        return defaultWldtPostActionType;
    }

    public void setDefaultWldtPostActionType(String defaultWldtPostActionType) {
        this.defaultWldtPostActionType = defaultWldtPostActionType;
    }

    public String getDefaultWldtPutActionType() {
        return defaultWldtPutActionType;
    }

    public void setDefaultWldtPutActionType(String defaultWldtPutActionType) {
        this.defaultWldtPutActionType = defaultWldtPutActionType;
    }

    public Map<String, String> getCustomWldtActionTypesMap() {
        return customWldtActionTypesMap;
    }

    public String getDefaultActuatorWldtActionContentType() {
        return defaultActuatorWldtActionContentType;
    }

    public void setDefaultActuatorWldtActionContentType(String defaultActuatorWldtActionContentType) {
        this.defaultActuatorWldtActionContentType = defaultActuatorWldtActionContentType;
    }

    public String getDefaultPostWldtActionContentType() {
        return defaultPostWldtActionContentType;
    }

    public void setDefaultPostWldtActionContentType(String defaultPostWldtActionContentType) {
        this.defaultPostWldtActionContentType = defaultPostWldtActionContentType;
    }

    public String getDefaultPutWldtActionContentType() {
        return defaultPutWldtActionContentType;
    }

    public void setDefaultPutWldtActionContentType(String defaultPutWldtActionContentType) {
        this.defaultPutWldtActionContentType = defaultPutWldtActionContentType;
    }

    public Map<String, String> getCustomWldtActionContentTypes() {
        return customWldtActionContentTypes;
    }

    public boolean getResourceNotificationsSupport() {
        return resourceNotificationsSupport;
    }

    public void setResourceNotificationsSupport(boolean resourceNotificationsSupport) {
        this.resourceNotificationsSupport = resourceNotificationsSupport;
    }

    public Map<String, PhysicalAssetResourceListener.ListenerType> getCustomResourceNotificationsMap() {
        return customResourceNotificationsMap;
    }
}
