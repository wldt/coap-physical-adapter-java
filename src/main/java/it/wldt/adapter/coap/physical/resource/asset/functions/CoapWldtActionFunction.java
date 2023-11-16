package it.wldt.adapter.coap.physical.resource.asset.functions;

import it.wldt.adapter.physical.event.PhysicalAssetActionWldtEvent;

import java.util.function.Function;

@FunctionalInterface
public interface CoapWldtActionFunction extends Function<PhysicalAssetActionWldtEvent<?>, byte[]> {
}
