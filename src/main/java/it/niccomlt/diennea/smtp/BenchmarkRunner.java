package it.niccomlt.diennea.smtp;

import it.niccomlt.diennea.smtp.benchmarks.MessageDeliveryBenchmark;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class BenchmarkRunner {
    public static void main(String[] args) throws Exception {
        var opt = new OptionsBuilder()
            .include(MessageDeliveryBenchmark.class.getSimpleName())
            .shouldFailOnError(true)
            .build();

        new Runner(opt).run();
    }
}

