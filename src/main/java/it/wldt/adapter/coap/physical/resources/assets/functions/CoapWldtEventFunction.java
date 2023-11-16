package it.wldt.adapter.coap.physical.resources.assets.functions;

import it.wldt.core.event.WldtEvent;

import java.util.List;
import java.util.function.Function;

@FunctionalInterface
public interface CoapWldtEventFunction extends Function<String, List<WldtEvent<?>>> {
}
