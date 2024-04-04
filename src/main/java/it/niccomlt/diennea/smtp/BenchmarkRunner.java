package it.niccomlt.diennea.smtp;

import it.niccomlt.diennea.smtp.benchmarks.MultiMessageDeliveryBenchmark;
import it.niccomlt.diennea.smtp.benchmarks.SingleMessageDeliveryBenchmark;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * Entrypoint of the benchmarks.
 */
public class BenchmarkRunner {
    public static void main(final String[] args) throws Exception {
        final var opt = new OptionsBuilder()
            .include(SingleMessageDeliveryBenchmark.class.getSimpleName())
            .include(MultiMessageDeliveryBenchmark.class.getSimpleName())
            .shouldFailOnError(true)
            .result("output.json")
            .resultFormat(ResultFormatType.JSON)
            .output("output.log")
            .build();
        new Runner(opt).run();
    }
}

