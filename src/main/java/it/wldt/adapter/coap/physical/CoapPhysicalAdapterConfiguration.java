package it.wldt.adapter.coap.physical;

import it.wldt.adapter.coap.physical.resource.CoapResource;
import it.wldt.adapter.coap.physical.resource.asset.CoapPayloadFunction;
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
    private Long autoUpdatePeriod;

    private Boolean enableResourceDiscovery;

    private CoapPayloadFunction resourceFunction;

    private PhysicalAssetDescription physicalAssetDescription;

    private final List<DigitalTwinCoapResource> resources = new ArrayList<>();

    protected CoapPhysicalAdapterConfiguration(String serverAddress, int serverPort) {
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

    public long getAutoUpdatePeriod() {
        return autoUpdatePeriod;
    }

    public CoapPayloadFunction getPayloadFunction() {
        return resourceFunction;
    }

    public List<DigitalTwinCoapResource> getResources() {
        return resources;
    }

    public PhysicalAssetDescription getPhysicalAssetDescription() {
        return physicalAssetDescription;
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

    protected void setResourceFunction(CoapPayloadFunction resourceFunction) {
        this.resourceFunction = resourceFunction;
    }

    protected void setPhysicalAssetDescription(List<PhysicalAssetAction> actions,
                       List<PhysicalAssetProperty<?>> properties,
                       List<PhysicalAssetEvent> events) {
        this.physicalAssetDescription = new PhysicalAssetDescription(actions, properties, events);
    }

    protected boolean addResource (DigitalTwinCoapResource resource) {
        return resources.add(resource);
    }

}
