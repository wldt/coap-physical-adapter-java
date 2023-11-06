package it.wldt.adapter.coap.physical;

import it.wldt.adapter.coap.physical.discovery.ResourceDiscoveryFunction;
import it.wldt.adapter.coap.physical.exception.CoapPhysicalAdapterConfigurationException;
import it.wldt.adapter.coap.physical.resource.asset.DigitalTwinCoapResourceDescriptor;
import it.wldt.adapter.physical.PhysicalAssetAction;
import it.wldt.adapter.physical.PhysicalAssetEvent;
import it.wldt.adapter.physical.PhysicalAssetProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class CoapPhysicalAdapterConfigurationBuilder {
    private final CoapPhysicalAdapterConfiguration configuration;

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

    public CoapPhysicalAdapterConfigurationBuilder setResourceDiscoveryFunction(ResourceDiscoveryFunction function) {
        this.configuration.setResourceDiscoveryFunction(function);
        return this;
    }

    public CoapPhysicalAdapterConfigurationBuilder setDigitalTwinEventsFlag(boolean enableDigitalTwinEvents) {
        this.configuration.setDigitalTwinEventsFlag(enableDigitalTwinEvents);
        return this;
    }

    public CoapPhysicalAdapterConfigurationBuilder setPreferredContentFormat(int preferredContentFormat) {
        this.configuration.setPreferredContentFormat(preferredContentFormat);
        return this;
    }

    public CoapPhysicalAdapterConfigurationBuilder setDefaultPropertyBodyProducer(Function<byte[], ?> defaultPropertyBodyProducer) {
        this.configuration.setDefaultPropertyBodyProducer(defaultPropertyBodyProducer);
        return this;
    }
    public CoapPhysicalAdapterConfigurationBuilder setDefaultActionBodyProducer(Function<byte[], ?> defaultActionBodyProducer) {
        this.configuration.setDefaultActionBodyProducer(defaultActionBodyProducer);
        return this;
    }
    public CoapPhysicalAdapterConfigurationBuilder setDefaultEventBodyProducer(Function<String, ?> defaultEventBodyProducer) {
        this.configuration.setDefaultEventBodyProducer(defaultEventBodyProducer);
        return this;
    }

    public CoapPhysicalAdapterConfigurationBuilder addCoapResource(String uri, DigitalTwinCoapResourceDescriptor resource) {
        this.configuration.addResource(uri, resource);

        return this;
    }

    public CoapPhysicalAdapterConfiguration build() throws CoapPhysicalAdapterConfigurationException {
        if (!this.configuration.getResourceDiscoveryFlag() && this.configuration.getResources().isEmpty()) {
            throw new CoapPhysicalAdapterConfigurationException("If resource discovery is disabled Physical Adapter must define at least one resource");
        }

        if (this.configuration.getResourceDiscoveryFlag() && this.configuration.getDefaultPropertyBodyProducer() == null) {
            this.configuration.setDefaultPropertyBodyProducer(Function.identity());
        }
        if (this.configuration.getResourceDiscoveryFlag() && this.configuration.getDefaultActionBodyProducer() == null) {
            this.configuration.setDefaultActionBodyProducer(Function.identity());
        }
        if (this.configuration.getResourceDiscoveryFlag() && this.configuration.getDefaultEventBodyProducer() == null) {
            this.configuration.setDefaultEventBodyProducer(Function.identity());
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
