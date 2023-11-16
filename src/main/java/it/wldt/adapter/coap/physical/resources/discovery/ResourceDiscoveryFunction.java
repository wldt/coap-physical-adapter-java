package it.wldt.adapter.coap.physical.resources.discovery;

import org.eclipse.californium.core.CoapClient;

import java.util.Set;

@FunctionalInterface
public interface ResourceDiscoveryFunction {
    Set<DiscoveredResource> discover(CoapClient client);
}
