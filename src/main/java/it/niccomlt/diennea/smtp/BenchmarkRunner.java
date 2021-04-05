package it.niccomlt.diennea.smtp;

import it.niccomlt.diennea.smtp.benchmarks.MultiMessageDeliveryBenchmark;
import it.niccomlt.diennea.smtp.benchmarks.SingleMessageDeliveryBenchmark;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class BenchmarkRunner {
    public static void main(String[] args) throws Exception {
        var opt = new OptionsBuilder()
            .include(SingleMessageDeliveryBenchmark.class.getSimpleName())
            .include(MultiMessageDeliveryBenchmark.class.getSimpleName())
            .shouldFailOnError(true)
            .build();

        new Runner(opt).run();
    }
}

