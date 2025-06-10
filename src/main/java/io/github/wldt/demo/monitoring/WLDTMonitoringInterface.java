package io.github.wldt.demo.monitoring;

import com.sun.jdi.InvalidTypeException;
import io.opentelemetry.api.metrics.*;
import it.wldt.exception.WldtDigitalTwinStatePropertyException;

import java.util.HashMap;

public interface WLDTMonitoringInterface {

    // PROPERTIES METRICS
    // ------------------

    void watchPropertyLongCounter(String propertyId) throws WldtDigitalTwinStatePropertyException, InvalidTypeException;

    void watchPropertyDoubleCounter(String propertyId) throws WldtDigitalTwinStatePropertyException, InvalidTypeException;

    // void addPropertyLongUpDownCounter(String id, String propertyId) throws WldtDigitalTwinStatePropertyException, InvalidTypeException;

    // void addPropertyDoubleUpDownCounter(String id, String propertyId) throws WldtDigitalTwinStatePropertyException, InvalidTypeException;

    void watchPropertyLongGauge(String propertyId) throws WldtDigitalTwinStatePropertyException, InvalidTypeException;

    void watchPropertyDoubleGauge(String propertyId) throws WldtDigitalTwinStatePropertyException, InvalidTypeException;

    // void addPropertyLongHistogram(String id, String name);

    // void addPropertyDoubleHistogram(String id, String name);


    // GENERAL PURPOSE METRICS
    // -----------------------

    // Long Counter
    void addLongCounter(String metricName, Long initialValue);

    void removeLongCounter(String metricName);

    void incrementLongCounter(String metricName, Long amount);

    // Double Counter
    void addDoubleCounter(String metricName, Double initialValue);

    void removeDoubleCounter(String metricName);

    void incrementDoubleCounter(String metricName, Double amount);

    // Long Gauge
    void addLongGauge(String metricName, Long initialValue);

    void removeLongGauge(String metricName);

    void setLongGauge(String metricName, Long value);

    // Double Gauge
    void addDoubleGauge(String metricName, Double initialValue);

    void removeDoubleGauge(String metricName);

    void setDoubleGauge(String metricName, Double value);
}
