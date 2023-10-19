package it.wldt.adapter.coap.physical;

import it.wldt.adapter.coap.physical.exception.CoapPhysicalAdapterConfigurationException;
import it.wldt.adapter.coap.physical.resource.asset.payload.CoapPayloadFunction;
import it.wldt.adapter.coap.physical.resource.asset.DigitalTwinCoapResource;
import it.wldt.adapter.physical.PhysicalAssetAction;
import it.wldt.adapter.physical.PhysicalAssetEvent;
import it.wldt.adapter.physical.PhysicalAssetProperty;

import java.util.ArrayList;
import java.util.List;

public class CoapPhysicalAdapterConfigurationBuilder {
    // TODO: Continue
    private final CoapPhysicalAdapterConfiguration configuration;

    // TODO: Are these lists really useful for a CoAP context?
    private final List<PhysicalAssetProperty<?>> properties = new ArrayList<>();
    private final List<PhysicalAssetEvent> events = new ArrayList<>();
    private final List<PhysicalAssetAction> actions = new ArrayList<>();

    public CoapPhysicalAdapterConfigurationBuilder(String serverAddress, int serverPort) throws CoapPhysicalAdapterConfigurationException {
        if (!isValid(serverAddress)) {
            throw new CoapPhysicalAdapterConfigurationException("Server address cannot be null or blank string");
        }
        if (!isValid(serverPort)) {
            throw new CoapPhysicalAdapterConfigurationException("Server port must be a positive number");
        }

        this.configuration = new CoapPhysicalAdapterConfiguration(serverAddress, serverPort);
    }

    public CoapPhysicalAdapterConfigurationBuilder setAutoUpdateFlag(boolean enableAutoUpdate) {
        this.configuration.setAutoUpdateFlag(enableAutoUpdate);
        return this;
    }

    public CoapPhysicalAdapterConfigurationBuilder setAutoUpdatePeriod(long period) {
        this.configuration.setAutoUpdatePeriod(period);
        return this;
    }

    public CoapPhysicalAdapterConfigurationBuilder setResourceDiscoveryFlag(boolean enableResourceDiscovery) {
        this.configuration.setResourceDiscoveryFlag(enableResourceDiscovery);
        return this;
    }

    public CoapPhysicalAdapterConfigurationBuilder setPayloadFunction(CoapPayloadFunction payloadFunction) {
        this.configuration.setPayloadFunction(payloadFunction);
        return this;
    }

    public CoapPhysicalAdapterConfigurationBuilder addCoapResource(DigitalTwinCoapResource resource) {
        this.configuration.addResource(resource);

        return this;
    }

    public CoapPhysicalAdapterConfiguration build() throws CoapPhysicalAdapterConfigurationException {
        if (!this.configuration.getResourceDiscoveryFlag() && this.configuration.getResources().isEmpty()) {
            throw new CoapPhysicalAdapterConfigurationException("If resource discovery is disabled Physical Adapter must define at least one resource");
        }

        if (this.configuration.getPayloadFunction() == null) {
            throw new CoapPhysicalAdapterConfigurationException("Physical Adapter needs a payload function");
        }

        this.configuration.setPhysicalAssetDescription(this.actions, this.properties, this.events);

        return this.configuration;
    }

    private boolean isValid(String param) {
        return param != null && !param.isBlank();
    }

    private boolean isValid(int param) {
        return param > 0;
    }
}
