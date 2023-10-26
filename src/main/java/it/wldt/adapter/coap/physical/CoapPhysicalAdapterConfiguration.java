package it.wldt.adapter.coap.physical;

import it.wldt.adapter.coap.physical.discovery.ResourceDiscoveryFunction;
import it.wldt.adapter.coap.physical.exception.CoapPhysicalAdapterConfigurationException;
import it.wldt.adapter.coap.physical.resource.asset.DigitalTwinCoapResourceDescriptor;
import it.wldt.adapter.coap.physical.resource.asset.payload.CoapPayloadFunction;
import it.wldt.adapter.physical.PhysicalAssetAction;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.PhysicalAssetEvent;
import it.wldt.adapter.physical.PhysicalAssetProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class CoapPhysicalAdapterConfiguration {
    private final String serverAddress;
    private final Integer serverPort;

    private Boolean enableAutoUpdate;
    private Long autoUpdatePeriod;

    private Boolean enableResourceDiscovery;

    private ResourceDiscoveryFunction resourceDiscoveryFunction;

    // TODO: Add content type for requests

    private Function<byte[], ?> propertyBodyProducer;
    private Function<byte[], ?> actionBodyProducer;
    private Function<byte[], ?> eventBodyProducer;

    private PhysicalAssetDescription physicalAssetDescription;

    private final List<DigitalTwinCoapResourceDescriptor> resources = new ArrayList<>();

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

    public String getServerConnectionString() {
        return String.format("coap://%s:%d", serverAddress, serverPort);
    }

    public boolean getAutoUpdateFlag() {
        return enableAutoUpdate;
    }

    public long getAutoUpdatePeriod() {
        return autoUpdatePeriod;
    }

    public Function<byte[], ?> getPropertyBodyProducer() {
        return propertyBodyProducer;
    }
    public Function<byte[], ?> getActionBodyProducer() {
        return actionBodyProducer;
    }
    public Function<byte[], ?> getEventBodyProducer() {
        return eventBodyProducer;
    }

    public List<DigitalTwinCoapResourceDescriptor> getResources() {
        return resources;
    }

    public PhysicalAssetDescription getPhysicalAssetDescription() {
        return physicalAssetDescription;
    }

    public ResourceDiscoveryFunction getResourceDiscoveryFunction() {
        return resourceDiscoveryFunction;
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

    protected void setPropertyBodyProducer(Function<byte[], ?> propertyBodyProducer) {
        this.propertyBodyProducer = propertyBodyProducer;
    }

    protected void setActionBodyProducer(Function<byte[], ?> actionBodyProducer) {
        this.actionBodyProducer = actionBodyProducer;
    }

    protected void setEventBodyProducer(Function<byte[], ?> eventBodyProducer) {
        this.eventBodyProducer = eventBodyProducer;
    }

    protected void setPhysicalAssetDescription(List<PhysicalAssetAction> actions,
                       List<PhysicalAssetProperty<?>> properties,
                       List<PhysicalAssetEvent> events) {
        this.physicalAssetDescription = new PhysicalAssetDescription(actions, properties, events);
    }

    protected void addResource (DigitalTwinCoapResourceDescriptor resource) {
        resources.add(resource);
    }

}
