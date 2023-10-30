package it.wldt.adapter.coap.physical.resource.asset.functions;

import it.wldt.adapter.physical.event.PhysicalAssetActionWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;

import java.util.function.Function;

@FunctionalInterface
public interface CoapActionFunction extends Function<PhysicalAssetActionWldtEvent<?>, byte[]> {
}
