package it.wldt.adapter.coap.physical.discovery;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.WebLink;

import java.util.Set;

@FunctionalInterface
public interface ResourceDiscoveryFunction {
    Set<DiscoveredResource> discover(CoapClient client);
}
