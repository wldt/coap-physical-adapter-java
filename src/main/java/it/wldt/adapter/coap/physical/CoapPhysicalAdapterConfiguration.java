package it.wldt.adapter.coap.physical;

import it.wldt.adapter.coap.physical.resource.CoapResource;
import it.wldt.adapter.coap.physical.resource.asset.DigitalTwinCoapResource;
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
    private Long autoUpdateDelay;
    private Long autoUpdatePeriod;

    private Boolean enableResourceDiscovery;

    private Function<byte[], ?> resourceFunction;

    private PhysicalAssetDescription physicalAssetDescription;

    private final List<DigitalTwinCoapResource> resources = new ArrayList<>();

    private CoapPhysicalAdapterConfiguration(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
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

    public Function<byte[], ?> getResourceFunction() {
        return resourceFunction;
    }

    public void setAutoUpdateFlag(boolean enableAutoUpdate) {
        this.enableAutoUpdate = enableAutoUpdate;
    }

    public long getAutoUpdateDelay() {
        return autoUpdateDelay;
    }

    public void setAutoUpdateDelay(long autoUpdateDelay) {
        this.autoUpdateDelay = autoUpdateDelay;
    }

    public long getAutoUpdatePeriod() {
        return autoUpdatePeriod;
    }

    public void setAutoUpdatePeriod(long autoUpdatePeriod) {
        this.autoUpdatePeriod = autoUpdatePeriod;
    }

    public void setResourceDiscoveryFlag(boolean enableResourceDiscovery) {
        this.enableResourceDiscovery = enableResourceDiscovery;
    }

    public void setResourceFunction(Function<byte[], ?> resourceFunction) {
        this.resourceFunction = resourceFunction;
    }

    public PhysicalAssetDescription getPhysicalAssetDescription() {
        return physicalAssetDescription;
    }

    public void setPhysicalAssetDescription(List<PhysicalAssetAction> actions,
                       List<PhysicalAssetProperty<?>> properties,
                       List<PhysicalAssetEvent> events) {
        this.physicalAssetDescription = new PhysicalAssetDescription(actions, properties, events);
    }

    public boolean addResource (DigitalTwinCoapResource resource) {
        return resources.add(resource);
    }

    public List<DigitalTwinCoapResource> getResources() {
        return resources;
    }
}
