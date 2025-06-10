package io.github.wldt.demo;

import io.github.wldt.demo.monitoring.OpenTelemetryWLDTMonitoring;
import io.github.wldt.demo.utils.GlobalKeywords;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import it.wldt.adapter.digital.event.DigitalActionWldtEvent;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.PhysicalAssetRelationshipInstance;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceCreatedWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceDeletedWldtEvent;
import it.wldt.core.model.ShadowingFunction;
import it.wldt.core.state.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Random;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 01/09/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public class DemoShadowingFunction extends ShadowingFunction {

    private static final Logger logger = LoggerFactory.getLogger(DemoShadowingFunction.class);
    OpenTelemetryWLDTMonitoring otMetricExporter;

    public DemoShadowingFunction(String id) {
        super(id);
        maybeRunWithSpan(() -> logger.info("A slf4j log message without a span"), false);
    }

    //// Shadowing Function Management Callbacks ////

    @Override
    protected void onCreate() {

    }

    @Override
    protected void onStart() {

    }

    @Override
    protected void onStop() {

    }

    //// Bound LifeCycle State Management Callbacks ////

    @Override
    protected void onDigitalTwinBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap) {

        try {

            logger.info("[TestShadowingFunction] -> onDigitalTwinBound(): {}", adaptersPhysicalAssetDescriptionMap);

            // NEW in 0.3.0 -> Start DT State Change Transaction
            this.digitalTwinStateManager.startStateTransaction();

            //Iterate over all the received PAD from connected Physical Adapters
            adaptersPhysicalAssetDescriptionMap.values().forEach(pad -> {

                pad.getProperties().forEach(property -> {
                    try {

                        //Create and write the property on the DT's State
                        this.digitalTwinStateManager.createProperty(new DigitalTwinStateProperty<>(property.getKey(),(Double) property.getInitialValue()));

                        //Start observing the variation of the physical property in order to receive notifications
                        //Without this call the Shadowing Function will not receive any notifications or callback about
                        //incoming physical property of the target type and with the target key
                        this.observePhysicalAssetProperty(property);

                        logger.info("[TestShadowingFunction] -> onDigitalTwinBound() -> Property Created & Observed:{}", property.getKey());

                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
                });

                //Iterate over available declared Physical Events for the target Physical Adapter's PAD
                pad.getEvents().forEach(event -> {
                    try {

                        //Instantiate a new DT State Event with the same key and type
                        DigitalTwinStateEvent dtStateEvent = new DigitalTwinStateEvent(event.getKey(), event.getType());

                        //Create and write the event on the DT's State
                        this.digitalTwinStateManager.registerEvent(dtStateEvent);

                        //Start observing the variation of the physical event in order to receive notifications
                        //Without this call the Shadowing Function will not receive any notifications or callback about
                        //incoming physical events of the target type and with the target key
                        this.observePhysicalAssetEvent(event);

                        logger.info("[TestShadowingFunction] -> onDigitalTwinBound() -> Event Created & Observed:{}", event.getKey());

                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
                });

                //Iterate over available declared Physical Actions for the target Physical Adapter's PAD
                pad.getActions().forEach(action -> {
                    try {

                        //Instantiate a new DT State Action with the same key and type
                        DigitalTwinStateAction dtStateAction = new DigitalTwinStateAction(action.getKey(), action.getType(), action.getContentType());

                        //Enable the action on the DT's State
                        this.digitalTwinStateManager.enableAction(dtStateAction);

                        logger.info("[TestShadowingFunction] -> onDigitalTwinBound() -> Action Enabled:{}", action.getKey());

                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
                });

                pad.getRelationships().forEach(relationship -> {
                    try{
                        if(relationship != null && relationship.getName().equals(GlobalKeywords.INSIDE_IN_RELATIONSHIP_NAME)){

                            DigitalTwinStateRelationship<String> insideInDtStateRelationship = new DigitalTwinStateRelationship<>(relationship.getName(), relationship.getName());

                            this.digitalTwinStateManager.createRelationship(insideInDtStateRelationship);

                            observePhysicalAssetRelationship(relationship);

                            logger.info("[TestShadowingFunction] -> onDigitalTwinBound() -> Relationship Created & Observed :{}", relationship.getName());
                        }
                    }catch (Exception e){
                        logger.error(e.getMessage());
                    }
                });

            });

            // NEW in 0.3.0 -> Commit DT State Change Transaction to apply the changes on the DT State and notify about the change
            this.digitalTwinStateManager.commitStateTransaction();

            // WLDT Metrics
            otMetricExporter = new OpenTelemetryWLDTMonitoring(this.digitalTwinStateManager);
            otMetricExporter.watchPropertyDoubleGauge(GlobalKeywords.TEMPERATURE_PROPERTY_KEY);
            otMetricExporter.addLongCounter("test.long.counter", 0L);
            otMetricExporter.addDoubleCounter("test.double.counter", 0.0);
            otMetricExporter.addLongGauge("test.long.gauge", 0L);
            otMetricExporter.addDoubleGauge("test.double.gauge", 0.0);



            //Start observation to receive all incoming Digital Action through active Digital Adapter
            //Without this call the Shadowing Function will not receive any notifications or callback about
            //incoming request to execute an exposed DT's Action
            observeDigitalActionEvents();

            //Notify the DT Core that the Bounding phase has been correctly completed and the DT has evaluated its
            //internal status according to what is available and declared through the Physical Adapters
            notifyShadowingSync();

        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    protected void onDigitalTwinUnBound(Map<String, PhysicalAssetDescription> map, String s) {

    }

    @Override
    protected void onPhysicalAdapterBidingUpdate(String s, PhysicalAssetDescription physicalAssetDescription) {

    }

    //// Physical Property Variation Callback ////

    @Override
    protected void onPhysicalAssetPropertyVariation(PhysicalAssetPropertyWldtEvent<?> physicalAssetPropertyWldtEvent) {

        try {

            logger.info("[TestShadowingFunction] -> onPhysicalAssetPropertyVariation() -> Variation on Property :{}", physicalAssetPropertyWldtEvent.getPhysicalPropertyId());

            //Update Digital Twin State
            //NEW from 0.3.0 -> Start State Transaction
            this.digitalTwinStateManager.startStateTransaction();

            this.digitalTwinStateManager.updateProperty(new DigitalTwinStateProperty<>(
                    physicalAssetPropertyWldtEvent.getPhysicalPropertyId(),
                    physicalAssetPropertyWldtEvent.getBody()));

            //NEW from 0.3.0 -> Commit State Transaction
            this.digitalTwinStateManager.commitStateTransaction();

            this.otMetricExporter.incrementLongCounter("test.long.counter", 1L);
            this.otMetricExporter.incrementDoubleCounter("test.double.counter", 1.43);
            this.otMetricExporter.setLongGauge("test.long.gauge", new Random().nextLong(101));
            this.otMetricExporter.setDoubleGauge("test.double.gauge", new Random().nextDouble(101));

            logger.info("[TestShadowingFunction] -> onPhysicalAssetPropertyVariation() -> DT State UPDATE Property :{}", physicalAssetPropertyWldtEvent.getPhysicalPropertyId());

        } catch (Exception e) {
            logger.error(e.getMessage());
        }

    }

    //// Physical Event Notification Callback ////

    @Override
    protected void onPhysicalAssetEventNotification(PhysicalAssetEventWldtEvent<?> physicalAssetEventWldtEvent) {
        try {

            logger.info("[TestShadowingFunction] -> onPhysicalAssetPropertyVariation() -> Notification for Event :{}", physicalAssetEventWldtEvent.getPhysicalEventKey());

            this.digitalTwinStateManager.notifyDigitalTwinStateEvent(new DigitalTwinStateEventNotification<>(
                    physicalAssetEventWldtEvent.getPhysicalEventKey(),
                    physicalAssetEventWldtEvent.getBody(),
                    physicalAssetEventWldtEvent.getCreationTimestamp()));

            logger.info("[TestShadowingFunction] -> onPhysicalAssetPropertyVariation() -> DT State Notification for Event:{}", physicalAssetEventWldtEvent.getPhysicalEventKey());

        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    //// Physical Relationships Notification Callbacks ////

    @Override
    protected void onPhysicalAssetRelationshipEstablished(PhysicalAssetRelationshipInstanceCreatedWldtEvent<?> physicalAssetRelationshipInstanceCreatedWldtEvent) {
        try{

            if(physicalAssetRelationshipInstanceCreatedWldtEvent != null
                    && physicalAssetRelationshipInstanceCreatedWldtEvent.getBody() != null){

                PhysicalAssetRelationshipInstance<?> paRelInstance = physicalAssetRelationshipInstanceCreatedWldtEvent.getBody();

                if(paRelInstance.getTargetId() instanceof String){

                    String relName = paRelInstance.getRelationship().getName();
                    String relKey = paRelInstance.getKey();
                    String relTargetId = (String)paRelInstance.getTargetId();

                    DigitalTwinStateRelationshipInstance<String> instance = new DigitalTwinStateRelationshipInstance<String>(relName, relTargetId, relKey);

                    //Update Digital Twin State
                    //NEW from 0.3.0 -> Start State Transaction
                    this.digitalTwinStateManager.startStateTransaction();

                    this.digitalTwinStateManager.addRelationshipInstance(instance);

                    //NEW from 0.3.0 -> Commit State Transaction
                    this.digitalTwinStateManager.commitStateTransaction();
                }
            }
        }catch (Exception e){
            logger.error(e.getMessage());
        }
    }

    @Override
    protected void onPhysicalAssetRelationshipDeleted(PhysicalAssetRelationshipInstanceDeletedWldtEvent<?> physicalAssetRelationshipInstanceDeletedWldtEvent) {

    }

    //// Digital Action Received Callbacks ////

    @Override
    protected void onDigitalActionEvent(DigitalActionWldtEvent<?> digitalActionWldtEvent) {
        try {
            this.publishPhysicalAssetActionWldtEvent(digitalActionWldtEvent.getActionKey(), digitalActionWldtEvent.getBody());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private static void maybeRunWithSpan(Runnable runnable, boolean withSpan) {
        if (!withSpan) {
            runnable.run();
            return;
        }
        Span span = GlobalOpenTelemetry.getTracer("my-tracer").spanBuilder("my-span").startSpan();
        try (Scope unused = span.makeCurrent()) {
            runnable.run();
        } finally {
            span.end();
        }
    }
}
