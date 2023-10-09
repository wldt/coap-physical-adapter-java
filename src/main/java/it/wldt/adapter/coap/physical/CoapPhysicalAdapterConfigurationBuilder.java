package it.wldt.adapter.coap.physical;

import it.wldt.adapter.coap.physical.exception.CoapPhysicalAdapterConfigurationException;
import it.wldt.adapter.coap.physical.resource.asset.CoapPayloadFunction;
import it.wldt.adapter.physical.PhysicalAssetAction;
import it.wldt.adapter.physical.PhysicalAssetEvent;
import it.wldt.adapter.physical.PhysicalAssetProperty;

import java.util.ArrayList;
import java.util.List;

public class CoapPhysicalAdapterConfigurationBuilder {
    // TODO: Continue
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

    private boolean isValid(String param) {
        return param != null && !param.isBlank();
    }

    private boolean isValid(int param) {
        return param > 0;
    }
}
