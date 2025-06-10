package io.github.wldt.demo.monitoring;

import com.sun.jdi.InvalidTypeException;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.*;
import io.opentelemetry.api.trace.Tracer;

import java.util.Set;

import it.wldt.core.state.DigitalTwinStateManager;
import it.wldt.exception.WldtDigitalTwinStatePropertyException;

public class OpenTelemetryWLDTMonitoring extends WLDTMonitoring {

    DigitalTwinStateManager stateManager;
    int prometheusPort = 19090; // Default value
    Meter meter;
    Tracer tracer;

    public OpenTelemetryWLDTMonitoring() {}

    public OpenTelemetryWLDTMonitoring(DigitalTwinStateManager digitalTwinStateManager) {
        this.stateManager = digitalTwinStateManager;
        setBasicOpenTelemetryObjects();
    }

    public OpenTelemetryWLDTMonitoring(DigitalTwinStateManager digitalTwinStateManager, int prometheusPort) {
        this.stateManager = digitalTwinStateManager;
        this.prometheusPort = prometheusPort;
        setBasicOpenTelemetryObjects();
    }

    void setBasicOpenTelemetryObjects() {
        OpenTelemetry openTelemetry = OTConfiguration.initOpenTelemetry(this.prometheusPort);
        this.tracer = openTelemetry.getTracer("io.opentelemetry.example.prometheus");
        this.meter = openTelemetry.getMeter("io.opentelemetry.example.prometheus");
        io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender.install(openTelemetry);
    }


    // PROPERTIES METRICS
    // ------------------

    @Override
    public void watchPropertyLongCounter(String propertyId) throws WldtDigitalTwinStatePropertyException, InvalidTypeException {
        if ( this.stateManager.getDigitalTwinState().getProperty(propertyId).isPresent() ) {

            Set<String> validTypes = Set.of("int", "long", "uint", "unsigned integer", "integer");
            String type = this.stateManager.getDigitalTwinState().getProperty(propertyId).get().getType().toLowerCase();

            if ( !validTypes.contains(type) ) {
                throw new InvalidTypeException("Invalid type: " + type + ". Expected one a Long or Int equivalent");

            } else {

                ObservableLongCounter longCounter = meter.counterBuilder(propertyId).buildWithCallback(
                        observableMeasurement -> {
                            try {
                                if (this.stateManager.getDigitalTwinState().getProperty(propertyId).isPresent()) {
                                    Integer value = (Integer) this.stateManager.getDigitalTwinState().getProperty(propertyId).get().getValue();
                                    observableMeasurement.record(value);
                                }
                            } catch (WldtDigitalTwinStatePropertyException e) {
                                throw new RuntimeException(e);
                            }
                        });
                longPropertyCounters.putIfAbsent(propertyId, longCounter);
            }
        }
    }

    @Override
    public void watchPropertyDoubleCounter(String propertyId) throws WldtDigitalTwinStatePropertyException, InvalidTypeException {
        if ( this.stateManager.getDigitalTwinState().getProperty(propertyId).isPresent() ) {

            Set<String> validTypes = Set.of("double", "float", "java.lang.double", "java.lang.float");
            String type = this.stateManager.getDigitalTwinState().getProperty(propertyId).get().getType().toLowerCase();

            if ( !validTypes.contains(type) ) {
                throw new InvalidTypeException("Invalid type: " + type + ". Expected one a Long or Int equivalent");

            } else {

                ObservableDoubleCounter doubleCounter = meter.counterBuilder(propertyId).ofDoubles().buildWithCallback(
                        observableMeasurement -> {
                            try {
                                if (this.stateManager.getDigitalTwinState().getProperty(propertyId).isPresent()) {
                                    Double value = (Double) this.stateManager.getDigitalTwinState().getProperty(propertyId).get().getValue();
                                    observableMeasurement.record(value);
                                }
                            } catch (WldtDigitalTwinStatePropertyException e) {
                                throw new RuntimeException(e);
                            }
                        });
                doublePropertyCounters.putIfAbsent(propertyId, doubleCounter);
            }
        }
    }

    @Override
    public void watchPropertyLongGauge(String propertyId) throws InvalidTypeException, WldtDigitalTwinStatePropertyException {
        if ( this.stateManager.getDigitalTwinState().getProperty(propertyId).isPresent() ) {

            Set<String> validTypes = Set.of("int", "long", "uint", "unsigned integer", "integer", "java.lang.int", "java.lang.long", "java.lang.short");
            String type = this.stateManager.getDigitalTwinState().getProperty(propertyId).get().getType().toLowerCase();

            if ( !validTypes.contains(type) ) {
                throw new InvalidTypeException("Invalid type: " + type + ". Expected one a Long or Int equivalent");

            } else {

                ObservableLongGauge longGauge = meter.gaugeBuilder(propertyId).ofLongs().buildWithCallback(
                        observableMeasurement -> {
                            try {
                                if ( this.stateManager.getDigitalTwinState().getProperty(propertyId).isPresent() ) {
                                    Integer value = (Integer) this.stateManager.getDigitalTwinState().getProperty(propertyId).get().getValue();
                                    observableMeasurement.record(value);
                                }
                            } catch (WldtDigitalTwinStatePropertyException e) {
                                throw new RuntimeException(e);
                            }
                        });
                longPropertyGauges.putIfAbsent(propertyId, longGauge);
            }
        }
    }

    @Override
    public void watchPropertyDoubleGauge(String propertyId) throws WldtDigitalTwinStatePropertyException, InvalidTypeException {
        if ( this.stateManager.getDigitalTwinState().getProperty(propertyId).isPresent() ) {

            Set<String> validTypes = Set.of("double", "float", "java.lang.double", "java.lang.float");
            String type = this.stateManager.getDigitalTwinState().getProperty(propertyId).get().getType().toLowerCase();

            if ( !validTypes.contains(type) ) {
                throw new InvalidTypeException("Invalid type: " + type + ". Expected one a Long or Int equivalent");

            } else {

                ObservableDoubleGauge doubleGauge = meter.gaugeBuilder(propertyId).buildWithCallback(
                        observableMeasurement -> {
                            try {
                                if (this.stateManager.getDigitalTwinState().getProperty(propertyId).isPresent()) {
                                    Double value = (Double) this.stateManager.getDigitalTwinState().getProperty(propertyId).get().getValue();
                                    observableMeasurement.record(value);
                                }
                            } catch (WldtDigitalTwinStatePropertyException e) {
                                throw new RuntimeException(e);
                            }
                        });
                doublePropertyGauges.putIfAbsent(propertyId, doubleGauge);
            }
        }
    }


    // GENERAL PURPOSE METRICS
    // -----------------------

    // Long Counter
    @Override
    public void addLongCounter(String metricName, Long initialValue) {
        LongCounter counter = meter.counterBuilder(metricName).build();
        longCounters.put(metricName, counter);
    }

    @Override
    public void removeLongCounter(String metricName) {
        longCounters.remove(metricName);
    }

    @Override
    public void incrementLongCounter(String metricName, Long amount) {
        LongCounter counter = longCounters.get(metricName);
        if (counter != null) {
            counter.add(amount);
        } else {
            throw new IllegalArgumentException("LongCounter for metric '" + metricName + "' does not exist.");
        }
    }

    // Double Counter
    @Override
    public void addDoubleCounter(String metricName, Double initialValue) {
        DoubleCounter counter = meter.counterBuilder(metricName).ofDoubles().build();
        doubleCounters.put(metricName, counter);
    }

    @Override
    public void removeDoubleCounter(String metricName) {
        // TODO unlink counter?
        doubleCounters.remove(metricName);
    }

    @Override
    public void incrementDoubleCounter(String metricName, Double amount) {
        DoubleCounter counter = doubleCounters.get(metricName);
        if (counter != null) {
            counter.add(amount);
        } else {
            throw new IllegalArgumentException("DoubleCounter for metric '" + metricName + "' does not exist.");
        }
    }


    // Long Gauge
    @Override
    public void addLongGauge(String metricName, Long initialValue) {
        LongGauge gauge = meter.gaugeBuilder(metricName).ofLongs().build();
        longGauges.put(metricName, gauge);
    }

    @Override
    public void removeLongGauge(String metricName) {
        // TODO unlink counter?
        longGauges.remove(metricName);
    }

    @Override
    public void setLongGauge(String metricName, Long value) {
        LongGauge gauge = longGauges.get(metricName);
        if (gauge != null) {
            longGauges.get(metricName).set(value);
        } else {
            throw new IllegalArgumentException("LongGauge for metric '" + metricName + "' does not exist.");
        }
    }


    // Double Gauge
    @Override
    public void addDoubleGauge(String metricName, Double initialValue) {
        DoubleGauge gauge = meter.gaugeBuilder(metricName).build();
        doubleGauges.put(metricName, gauge);
    }

    @Override
    public void removeDoubleGauge(String metricName) {
        // TODO unlink counter?
        doubleGauges.remove(metricName);
    }

    @Override
    public void setDoubleGauge(String metricName, Double value) {
        DoubleGauge gauge = doubleGauges.get(metricName);
        if (gauge != null) {
            doubleGauges.get(metricName).set(value);
        } else {
            throw new IllegalArgumentException("DoubleGauge for metric '" + metricName + "' does not exist.");
        }
    }


    /*
    @Override
    public void addLongUpDownCounter(String id, String propertyId) {
        LongUpDownCounter longUpDownCounter = meter.upDownCounterBuilder(id).build();
        longUpDownCounters.putIfAbsent(id, longUpDownCounter);
    }

    @Override
    public void addDoubleUpDownCounter(String id, String propertyId) {
        DoubleUpDownCounter doubleUpDownCounter = meter.upDownCounterBuilder(id).ofDoubles().build();
        doubleUpDownCounters.putIfAbsent(id, doubleUpDownCounter);
    }*/

    /*
    @Override
    public void addLongHistogram(String id, String name) {
        LongHistogram longHistogram = meter.histogramBuilder(id).ofLongs().build();
        longHistograms.putIfAbsent(id, longHistogram);
    }

    @Override
    public void addDoubleHistogram(String id, String name) {
        LongHistogram histogram = meter.histogramBuilder("super.timer").ofLongs().setUnit("ms").build();

        DoubleHistogram doubleHistogram = meter.histogramBuilder(id).build();
        doubleHistograms.putIfAbsent(id, doubleHistogram);
    }
    */
}
