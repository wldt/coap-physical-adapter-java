package it.wldt.adapter.coap.physical;

import it.wldt.adapter.coap.physical.discovery.ResourceDiscoveryFunction;
import it.wldt.adapter.coap.physical.exception.CoapPhysicalAdapterConfigurationException;
import it.wldt.adapter.coap.physical.resource.asset.DigitalTwinCoapResourceDescriptor;
import it.wldt.adapter.physical.PhysicalAssetAction;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.PhysicalAssetEvent;
import it.wldt.adapter.physical.PhysicalAssetProperty;

import java.util.*;
import java.util.function.Function;

public class CoapPhysicalAdapterConfiguration {
    private final String serverAddress;
    private final Integer serverPort;

    private Boolean enableAutoUpdate;
    private Long autoUpdatePeriod;

    private int preferredContentFormat;

    private boolean enableDigitalTwinEvents;

    private Boolean enableResourceDiscovery;

    private ResourceDiscoveryFunction resourceDiscoveryFunction;


    private Function<byte[], ?> defaultPropertyBodyProducer;
    private Function<byte[], ?> defaultActionBodyProducer;
    private Function<String, ?> defaultEventBodyProducer;

    private PhysicalAssetDescription physicalAssetDescription;

    private final Map<String, DigitalTwinCoapResourceDescriptor> resources = new HashMap<>();

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

    public Function<byte[], ?> getDefaultPropertyBodyProducer() {
        return defaultPropertyBodyProducer;
    }
    public Function<byte[], ?> getDefaultActionBodyProducer() {
        return defaultActionBodyProducer;
    }
    public Function<String, ?> getDefaultEventBodyProducer() {
        return defaultEventBodyProducer;
    }

    public Map<String, DigitalTwinCoapResourceDescriptor> getResources() {
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

    protected void setDefaultPropertyBodyProducer(Function<byte[], ?> defaultPropertyBodyProducer) {
        this.defaultPropertyBodyProducer = defaultPropertyBodyProducer;
    }

    protected void setDefaultActionBodyProducer(Function<byte[], ?> defaultActionBodyProducer) {
        this.defaultActionBodyProducer = defaultActionBodyProducer;
    }

    protected void setDefaultEventBodyProducer(Function<String, ?> defaultEventBodyProducer) {
        this.defaultEventBodyProducer = defaultEventBodyProducer;
    }

    protected void setPhysicalAssetDescription(List<PhysicalAssetAction> actions,
                       List<PhysicalAssetProperty<?>> properties,
                       List<PhysicalAssetEvent> events) {
        this.physicalAssetDescription = new PhysicalAssetDescription(actions, properties, events);
    }

    protected boolean addResource (String uri, DigitalTwinCoapResourceDescriptor resource) {
        if (!resources.containsKey(uri)) {
            resources.put(uri, resource);

            return true;
        }

        return false;
    }

}
