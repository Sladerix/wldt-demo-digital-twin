package io.github.wldt.demo.monitoring;

import io.opentelemetry.api.metrics.*;

import java.util.HashMap;

public abstract class WLDTMonitoring implements WLDTMonitoringInterface {
    // List of wldt property metrics
    HashMap<String, ObservableLongCounter> longPropertyCounters = new HashMap<>();
    HashMap<String, ObservableDoubleCounter> doublePropertyCounters = new HashMap<>();
    HashMap<String, ObservableLongGauge> longPropertyGauges = new HashMap<>();
    HashMap<String, ObservableDoubleGauge> doublePropertyGauges = new HashMap<>();

    // List of general purpose metrics
    HashMap<String, LongCounter> longCounters = new HashMap<>();
    HashMap<String, DoubleCounter> doubleCounters = new HashMap<>();
    HashMap<String, LongGauge> longGauges = new HashMap<>();
    HashMap<String, DoubleGauge> doubleGauges = new HashMap<>();
    HashMap<String, LongUpDownCounter> longUpDownCounters = new HashMap<>();
    HashMap<String, DoubleUpDownCounter> doubleUpDownCounters = new HashMap<>();
    HashMap<String, LongHistogram> longHistograms = new HashMap<>();
    HashMap<String, DoubleHistogram> doubleHistograms = new HashMap<>();
}
