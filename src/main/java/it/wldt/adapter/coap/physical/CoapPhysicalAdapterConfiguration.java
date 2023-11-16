package it.wldt.adapter.coap.physical;

import it.wldt.adapter.coap.physical.discovery.ResourceDiscoveryFunction;
import it.wldt.adapter.coap.physical.exceptions.CoapPhysicalAdapterConfigurationException;
import it.wldt.adapter.coap.physical.resources.assets.DigitalTwinCoapResource;
import it.wldt.adapter.coap.physical.resources.assets.functions.body.ActionBodyConsumer;
import it.wldt.adapter.coap.physical.resources.assets.functions.body.EventBodyProducer;
import it.wldt.adapter.coap.physical.resources.assets.functions.body.PropertyBodyProducer;
import it.wldt.adapter.physical.PhysicalAssetAction;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.PhysicalAssetEvent;
import it.wldt.adapter.physical.PhysicalAssetProperty;

import java.util.*;

public class CoapPhysicalAdapterConfiguration {
    private final String serverAddress;
    private final Integer serverPort;

    private Boolean enableAutoUpdate;
    private Long autoUpdatePeriod;

    private int preferredContentFormat;

    private boolean enableDigitalTwinEvents;

    private Boolean enableResourceDiscovery;

    private ResourceDiscoveryFunction resourceDiscoveryFunction;


    private PropertyBodyProducer<?> defaultPropertyBodyProducer;
    private ActionBodyConsumer<?> defaultActionBodyConsumer;
    private EventBodyProducer<?> defaultEventBodyProducer;

    private PhysicalAssetDescription physicalAssetDescription;

    private final Map<String, DigitalTwinCoapResource> resources = new HashMap<>();

    protected CoapPhysicalAdapterConfiguration(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public static CoapPhysicalAdapterConfigurationBuilder builder(String serverAddress, int serverPort) throws CoapPhysicalAdapterConfigurationException {
        return new CoapPhysicalAdapterConfigurationBuilder(serverAddress, serverPort);
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public int getServerPort() {
        return serverPort;
    }

    public boolean getResourceDiscoveryFlag() {
        return enableResourceDiscovery;
    }

    public boolean getDigitalTwinEventsFlag() {
        return enableDigitalTwinEvents;
    }

    public String getServerConnectionString() {
        return String.format("coap://%s:%d", serverAddress, serverPort);
    }

    public boolean getAutoUpdateFlag() {
        return enableAutoUpdate;
    }

    public long getAutoUpdatePeriod() {
        return autoUpdatePeriod;
    }

    public PropertyBodyProducer<?> getDefaultPropertyBodyProducer() {
        return defaultPropertyBodyProducer;
    }
    public ActionBodyConsumer<?> getDefaultActionBodyConsumer() {
        return defaultActionBodyConsumer;
    }
    public EventBodyProducer<?> getDefaultEventBodyProducer() {
        return defaultEventBodyProducer;
    }

    public Map<String, DigitalTwinCoapResource> getResources() {
        return resources;
    }

    public PhysicalAssetDescription getPhysicalAssetDescription() {
        return physicalAssetDescription;
    }

    public ResourceDiscoveryFunction getResourceDiscoveryFunction() {
        return resourceDiscoveryFunction;
    }

    public int getPreferredContentFormat() {
        return preferredContentFormat;
    }

    protected void setAutoUpdateFlag(boolean enableAutoUpdate) {
        this.enableAutoUpdate = enableAutoUpdate;
    }

    protected void setAutoUpdatePeriod(long autoUpdatePeriod) {
        this.autoUpdatePeriod = autoUpdatePeriod;
    }

    protected void setResourceDiscoveryFlag(boolean enableResourceDiscovery) {
        this.enableResourceDiscovery = enableResourceDiscovery;
    }

    protected void setResourceDiscoveryFunction(ResourceDiscoveryFunction function) {
        this.resourceDiscoveryFunction = function;
    }

    protected void setDigitalTwinEventsFlag (boolean enableDigitalTwinEvents) {
        this.enableDigitalTwinEvents = enableDigitalTwinEvents;
    }

    protected void setPreferredContentFormat(int preferredContentFormat) {
        this.preferredContentFormat = preferredContentFormat;
    }

    protected void setDefaultPropertyBodyProducer(PropertyBodyProducer<?> defaultPropertyBodyProducer) {
        this.defaultPropertyBodyProducer = defaultPropertyBodyProducer;
    }

    protected void setDefaultActionBodyConsumer(ActionBodyConsumer<?> defaultActionBodyConsumer) {
        this.defaultActionBodyConsumer = defaultActionBodyConsumer;
    }

    protected void setDefaultEventBodyProducer(EventBodyProducer<?> defaultEventBodyProducer) {
        this.defaultEventBodyProducer = defaultEventBodyProducer;
    }

    protected void setPhysicalAssetDescription(List<PhysicalAssetAction> actions,
                       List<PhysicalAssetProperty<?>> properties,
                       List<PhysicalAssetEvent> events) {
        this.physicalAssetDescription = new PhysicalAssetDescription(actions, properties, events);
    }

    protected boolean addResource (String uri, DigitalTwinCoapResource resource) {
        if (!resources.containsKey(uri)) {
            resources.put(uri, resource);

            return true;
        }

        return false;
    }

}
