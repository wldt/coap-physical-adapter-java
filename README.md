# CoAP Physical Adapter

The `CoapPhysicalAdapter` acts as a bridge between the Digital Twin and the Physical Assets, leveraging the CoAP protocol for communication.
It implements various features, which can be enabled or disabled via the usage of a builder-based configuration, which also allows the implementation of custom behavior where needed.

Key features:

- **Configuration builder**: The Adapter configuration is managed via the builder provided by the `CoapPhysicalAdapterConfiguration.build()` method, which enables users to effortlessly configure the CoAP server parameters and each Adapter's feature.
- **Standard & custom resource discovery support**: The Adapter configuration makes it possible to enable/disable the Resource Discovery process at the start of the Adapter, and to provide the Adapter with a custom Resource Discovery method in case the default (standard) one is not suited for the Digital Twin integration in the system.
- **Observation & polling support**: It's possible to configure the Adapter to use both Observation or Polling to keep the Adapter synced with the Physical Asset.
- **Customizable behavior**: Users can extend and customize the Adapter's behavior to meet specific requirements, such as implementing custom message processing or custom request handlers to better integrate with proprietary systems.

In the WLDT library, Physical Adapters have the responsibility to generate and publish the `PhysicalAssetDescription` (PAD) to describe the capabilities and the characteristics of the Object, allowing the Shadowing Function to decide how to digitalize its Physical counterpart.

**In the `CoapPhysicalAdapter` the generation of the PAD (Physical Asset Description) is automatically and internally executed by the adapter itself accordingly to its configuration.**

## WLDT-Core Version Compatibility

| coap-physical-adapter | wldt-core 0.2.1 | wldt-core 0.3.0 | wldt-core 0.4.0 |
|-----------------------|-----------------|-----------------|-----------------|
| 0.1.1                 | ❌               | ✅               | ✅               |


## Installation

### Maven

### Gradle

## Class Structure & Functionalities

### CoapPhysicalAdapterConfigurationBuilder

The `CoapPhysicalAdapterConfigurationBuilder` is the class used to build the configuration used by the `CoapPhysicalAdapter` and described and implemented through the class `CoapPhysicalAdapterConfiguration`.

In order to create a configuration builder we can use the `builder()` static method implemented by the `CoapPhysicalAdapterConfiguration` class:

```java
CoapPhysicalAdapterConfiguration.builder()
```
Creates a new instance of the CoAP Physical Adapter configuration builder.

*Parameters*:

- **ip**: The IP address of the CoAP server.
- **port**: The port of the CoAP server.

*Returns*: A new instance of the CoAP Physical Adapter configuration builder.

In the builder there's plenty of methods that can be used to configure the Adapter's behavior:

#### Resource discovery

##### enableResourceDiscoverySupport

```java
CoapPhysicalAdapterConfigurationBuilder enableResourceDiscoverySupport(boolean enable)
```

Enables/disables the resource discovery process.

*Parameters*:

- **enable**: A flag indicating whether to enable or disable the resource discovery process.

*Returns*: The builder instance.

##### setIgnoredResources

```java
CoapPhysicalAdapterConfigurationBuilder setIgnoredResources(List<String> ignoredResources)
```

Sets a list of resources which will be ignored by the default resource discovery.

This list will be ignored if a custom resource discovery is used.

*Parameters*:

- **ignoredResources**: A list containing the names of the resources to ignore.

*Returns*: The builder instance.

##### ignoreResources

```java
CoapPhysicalAdapterConfigurationBuilder ignoreResource(String name)
```

Adds a resource to the list of resources ignored by the default resource discovery.

*Parameters*:

- **name**: The name of the resource to ignore.

*Returns*: The builder instance.

##### setCustomResourceDiscoveryFunction

```java
CoapPhysicalAdapterConfigurationBuilder setCustomResourceDiscoveryFunction(Supplier<Set<PhysicalAssetResource>> customResourceDiscoveryFunction)
```

Sets a custom resource discovery function that will be used instead of the default standard one.

*Parameters*:

- **customResourceDiscoveryFunction**: A supplier returning a set of `PhysicalAssetResource` instances.

*Returns*: The builder instance.

#### Manual resource addition

##### setResources
```java
CoapPhysicalAdapterConfigurationBuilder setResources(Set<PhysicalAssetResource> resources)
```

Sets the list of already processed resources to the provided instance.

*Parameters*:

- **resources**: A set containing all the resources.

*Returns*: The builder instance.

##### addResource

```java
CoapPhysicalAdapterConfigurationBuilder addResource(String name, String resourceType, int contentType, boolean hasPostSupport, boolean hasPutSupport, boolean observable)
```

Adds a new physical asset resource to the list of manually added resources.

Each resource added this way will be processed by the `build()` method invoked at the end of the configuration.

*Parameters*:

- **name**: The resource name.
- **resourceType**: The resource type.
- **contentType**: The resource preferred content type.
- **hasPostSupport**: A flag indicating if the resource supports POST requests.
- **hasPutSupport**: A flag indicating if the resource supports PUT requests.
- **observable**: A flag indicating if the resource supports observation.

*Returns*: The builder instance.

#### Resource communication

##### setPreferredContentFormat

```java
public CoapPhysicalAdapterConfigurationBuilder setPreferredContentFormat(int preferredContentFormat)
```

Sets the preferred content format used during the communication in the case of the server supporting it.

*Parameters*:

- **preferredContentFormat**: The preferred CoAP content format.

*Returns*: The builder instance.

##### setDefaultActuatorActionContentType

```java
CoapPhysicalAdapterConfigurationBuilder setDefaultActuatorActionContentType(String actuatorActionContentType)
```

Sets the default WLDT action content type for CoAP resources supporting both POST & PUT requests.

*Parameters*:

- **actuatorActionContentType**: The WLDT action type

*Returns*: The builder instance.

#####

```java
CoapPhysicalAdapterConfigurationBuilder setDefaultPostActionContentType(String postActionContentType)
```

Sets the default WLDT action content type for CoAP resources supporting only POST requests.

*Parameters*:

- **postActionContentType**: The WLDT action type

*Returns*: The builder instance.

##### setDefaultPutActionContentType

```java
CoapPhysicalAdapterConfigurationBuilder setDefaultPutActionContentType(String putActionContentType)
```

Sets the default WLDT action content type for CoAP resources supporting only PUT requests.

*Parameters*:

- **putActionContentType**: The WLDT action type

*Returns*: The builder instance.

##### addCustomActionContentType

```java
CoapPhysicalAdapterConfigurationBuilder addCustomActionContentType(String resourceName, String actionContentType)
```

Adds a WLDT action content type only to the specified resource.

*Parameters*:

- **resourceName**: The resource name.
- **actionContentType**: The WLDT action content type.

*Returns*: The builder instance.

##### setCustomPropertyRequestFunction

```java
CoapPhysicalAdapterConfigurationBuilder setCustomPropertyRequestFunction(Function<Request, CoapResponse> customPropertyRequestFunction)
```

Sets the function which sends CoAP GET request to the Physical Asset.

*Parameters*:

- **customPropertyRequestFunction**: A function which receives a CoAP request as input and returns the CoAP response.

*Returns*: The builder instance.

##### setCustomActionRequestFunction

```java
CoapPhysicalAdapterConfigurationBuilder setCustomActionRequestFunction(Function<Request, CoapResponse> customActionRequestFunction)
```

Sets the function which sends CoAP POST & PUT request to the Physical Asset.

*Parameters*:

- **customActionRequestFunction**: A function which receives a CoAP request as input and returns the CoAP response.

*Returns*: The builder instance.

#### Observability & polling

##### enableObservability

```java
CoapPhysicalAdapterConfigurationBuilder enableObservability(boolean enable)
```

Enables/disables the automatic observation process of the Physical Adapter.

*Parameters*:

- **enable**: A flag indicating whether to enable or disable the observation.

*Returns*: The builder instance.

##### enableAutoUpdateTimer

```java
CoapPhysicalAdapterConfigurationBuilder enableAutoUpdateTimer(boolean enable)
```

Enables/disables the automatic polling process of the Physical Adapter.

*Parameters*:

- **enable**: A flag indicating whether to enable or disable the polling.

*Returns*: The builder instance.

##### setAutoUpdateInterval

```java
CoapPhysicalAdapterConfigurationBuilder setAutoUpdateInterval(long autoUpdateIntervalMs)
```

Sets the polling interval.

*Parameters*:

- **autoUpdateIntervalMs**: The polling interval (in milliseconds).

*Returns*: The builder instance.

#### CoAP responses to WLDT properties translation

##### setDefaultPropertyBodyTranslator

```java
CoapPhysicalAdapterConfigurationBuilder setDefaultPropertyBodyTranslator(BiFunction<String, byte[], List<? extends WldtEvent<?>>> defaultPropertyBodyTranslator)
```

Sets the default function to convert a resource's payload into a WLDT property event.

*Parameters*:

- **defaultPropertyBodyTranslator**: A function which receives as inputs the resource's key and payload, and returns a list of `WldtEvent` instances.

*Returns*: The builder instance.

##### setCustomPropertyBodyTranslators

```java
CoapPhysicalAdapterConfigurationBuilder setCustomPropertyBodyTranslators(Map<String, BiFunction<String, byte[], List<? extends WldtEvent<?>>>> customPropertyBodyTranslators)
```

Sets a map containing the property translation functions of specific resources.

*Parameters*:

- **customPropertyBodyTranslators**: A map containing the resource names as keys and the translation functions as values.

*Returns*: The builder instance.

#### CoAP communication events to WLDT event notifications

##### setDefaultEventTranslator

```java
CoapPhysicalAdapterConfigurationBuilder setDefaultEventTranslator(BiFunction<String, String, List<? extends WldtEvent<?>>> defaultEventTranslator)
```

Sets the default function to convert a resource's event into a WLDT event notification.

*Parameters*:

- **defaultEventTranslator**: A function which receives as inputs the resource's key and event message, and returns a list of `WldtEvent` instances.

*Returns*: The builder instance.

##### setCustomEventTranslators

```java
CoapPhysicalAdapterConfigurationBuilder setCustomEventTranslators(Map<String, BiFunction<String, String, List<? extends WldtEvent<?>>>> customEventTranslators)
```

Sets a map containing the event translation functions of specific resources.

*Parameters*:

- **customEventTranslators**: A map containing the resource names as keys and the translation functions as values.

*Returns*: The builder instance.

#### WLDT actions to CoAP requests

##### setDefaultActionEventTranslator

```java
CoapPhysicalAdapterConfigurationBuilder setDefaultActionEventTranslator(Function<PhysicalAssetActionWldtEvent<?>, Request> defaultActionEventTranslator)
```

Sets the default function to convert a WLDT action event into the request to send to the Physical Asset.

*Parameters*:

- **defaultActionEventTranslator**: A function which receives as input the action event and returns a CoAP request.

*Returns*: The builder instance.

##### setCustomActionEventTranslators

```java
CoapPhysicalAdapterConfigurationBuilder setCustomActionEventTranslators(Map<String, Function<PhysicalAssetActionWldtEvent<?>, Request>> customActionEventTranslators)
```

Sets a map containing the action translation functions of specific resources.

*Parameters*:

- **customActionEventTranslators**: A map containing the resource names as keys and the translation functions as values

*Returns*: The builder instance.

#### Resource updates listening

##### setAutomaticResourceListening

```java
CoapPhysicalAdapterConfigurationBuilder setAutomaticResourceListening(boolean enable)
```

Enables/disables the automatic resource listening process of the Physical Adapter.
If enabled the Adapter will listen to every resource for both property updates and events.

*Parameters*:

- **enable**: A flag indicating whether to enable or disable automatic resource listening.

*Returns*: The builder instance.

##### setCustomResourceListeningMap

```java
CoapPhysicalAdapterConfigurationBuilder setCustomResourceListeningMap(Map<String, PhysicalAssetResourceListener.ListenerType> customResourceListeningMap)
```

Adds a map of resources that the Physical Adapter will listen to, specifying if it has to listen to property updates, events, or both.

If automatic resource listening is enabled this map will be ignored since the Adapter will automatically listen to everything.

*Parameters*:

- **customResourceListeningMap**: A map having the name of the resource as key and what to listen to as a value (instance of `PhysicalAssetResourceListener.ListenerType`).

*Returns*: The builder instance.

#### WLDT events management

##### setDefaultEventType

```java
CoapPhysicalAdapterConfigurationBuilder setDefaultEventType(String eventType)
```

Sets the default WLDT event notification type.

*Parameters*:

- **eventType**: The WLDT event notification type

*Returns*: The builder instance.

##### addCustomEventType

```java
CoapPhysicalAdapterConfigurationBuilder addCustomEventType(String resourceName, String eventType)
```

Adds a WLDT event notification type only to the specified resource.

*Parameters*:

- **resourceName**: The resource to apply the type to.
- **eventType**: The WLDT event notification type.

*Returns*: The builder instance.

#### WLDT actions management

##### setDefaultActuatorActionType

```java
CoapPhysicalAdapterConfigurationBuilder setDefaultActuatorActionType(String actionType)
```

Sets the default WLDT action type for CoAP resources supporting both POST & PUT requests.

*Parameters*:

- **actionType**: The WLDT action type

*Returns*: The builder instance.

##### setDefaultPostActionType

```java
CoapPhysicalAdapterConfigurationBuilder setDefaultPostActionType(String actionType)
```

Sets the default WLDT action type for CoAP resources supporting only POST requests.

*Parameters*:

- **actionType**: The WLDT action type

*Returns*: The builder instance.

##### setDefaultPutActionType

```java
CoapPhysicalAdapterConfigurationBuilder setDefaultPutActionType(String actionType)
```

Sets the default WLDT action type for CoAP resources supporting only PUT requests.

*Parameters*:

- **actionType**: The WLDT action type

*Returns*: The builder instance.

##### addCustomActionType

```java
CoapPhysicalAdapterConfigurationBuilder addCustomActionType(String resourceName, String actionType)
```

Adds a WLDT action type only to the specified resource supporting both POST & PUT requests.

*Parameters*:

- **resourceName**: The resource to apply the type to.
- **actionType**: The WLDT action type.

*Returns*: The builder instance.

#####

```java

```

#### Build

After all the configuration is completed, to retreive the `CoapPhysicalAdapterConfiguration` instance, the `build()` method must be invoked:

```java
CoapPhysicalAdapterConfiguration build() throws CoapPhysicalAdapterConfigurationException
```

This method checks that everything needed is provided correctly and, if so, processes the manually added resources adding them to the configuration.

*Returns*: The configuration instance

*Throws*: `CoapPhysicalAdapterConfigurationException` - In the case of the configuration being invalid.

### CoapPhysicalAdapter

The `CoapPhysicalAdapter` class is the core component for interacting with physical assets. Instantiate it with a unique ID and the configuration. It extends the default WLDT Library `PhysicalAdapter` implementing all the functionalities to automatically interact with a CoAP physical device following the specifications and details provided in the `CoapPhysicalAdapterConfiguration` and built using `CoapPhysicalAdapterConfigurationBuilder`.

An example of its creation is:

```java
CoapPhysicalAdapter coapPhysicalAdapter = new CoapPhysicalAdapter("uniqueId", configuration);
```

### Integrated example

This example demonstrates the integration of a `CoapPhysicalAdapter` within a Digital Twin setup, where multiple adapters (including a console adapter) are added to a Digital Twin, and the overall system is managed by a `DigitalTwinEngine`.

```java
// Create a Digital Twin with a default shadowing function
DigitalTwin dt = new DigitalTwin("coap-digital-twin", new DefaultShadowingFunction());

// Create an instance of ConsoleDigitalAdapter
ConsoleDigitalAdapter digitalAdapter = new ConsoleDigitalAdapter();

// Create an instance of CoapPhysicalAdapterConfiguration
CoapPhysicalAdapterConfiguration configuration = CoapPhysicalAdapterConfiguration.builder(serverAddress, serverPort)
  .enableResourceDiscoverySupport(true)
  .enableObservability(true)
  .enableAutoUpdateTimer(true)
  .setAutoUpdateInterval(5000)
  .setPreferredContentFormat(MediaTypeRegistry.APPLICATION_JSON)
  .setDefaultPropertyBodyTranslator((key, payload) -> {
    List<WldtEvent<String>> events = new ArrayList<>();
    try {
      events.add(new PhysicalAssetPropertyWldtEvent<>(key, new String(payload)));
    } catch (EventBusException e) {
      e.printStackTrace();
    }
    return events;
  })
  .setDefaultEventTranslator((key, message) -> {
    List<WldtEvent<String>> events = new ArrayList<>();
    try {
      events.add(new PhysicalAssetEventWldtEvent<>(key, message));
    } catch (EventBusException e) {
      e.printStackTrace();
    }
    return events;
  })
  .setDefaultActionEventTranslator(event -> {
    Request request;
    request = new Request(event.getBody().equals("") ? CoAP.Code.POST : CoAP.Code.PUT);
    request.setConfirmable(true);
    request.setPayload((String) event.getBody());
    return request;
  })
  .build();
```

Since the resource discovery is enabled, the PAD gets automatically generated based on it, but in case it's needed it's possible to manually add resources using the `addResource` method
