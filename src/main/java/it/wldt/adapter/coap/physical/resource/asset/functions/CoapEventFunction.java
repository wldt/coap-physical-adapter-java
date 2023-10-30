package it.wldt.adapter.coap.physical.resource.asset.functions;

import it.wldt.core.event.WldtEvent;

import java.util.List;
import java.util.function.Function;

@FunctionalInterface
public interface CoapEventFunction extends Function<String, List<WldtEvent<?>>> {
}
