package it.wldt.adapter.coap.physical.discovery;

import org.eclipse.californium.core.WebLink;

import java.util.List;
import java.util.function.Function;

@FunctionalInterface
public interface ResourceDiscoveryFunction {
    List<WebLink> discover();
}
