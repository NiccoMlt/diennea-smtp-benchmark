# SMTP Benchmarks

You have to implement a simple Java application which executes a bench on an SMTP service.

Requirements:

- The application MUST work with an SMTP service with username/password authentication
- The application MUST measure min/max/avg time of message delivery
- The application MUST measure the impact of the size of the message
- The application MAY evaluate the impact of sending multiple messages on the same SMTP connection
- The application MUST use JavaMail API

## Implementation details

The project was implemented with JavaMail API version 1.6.2 
(the final version before moving to the Eclipse Foundation as part of the Eclipse Enterprise for Java project)
and Java Microbenchmark Harness (JMH) version 1.29 by OpenJDK.

Use `mvn clean install` to generate the executable JAR containing the benchmarks.
You can also use the Maven Wrapper using the bundled scripts `mvnw` (on UNIX) or `mvnw.cmd` (on Windows). 

Run it with `java -jar target/benchmarks.jar`.

Executing `it.niccomlt.diennea.smtp.BenchmarkRunner` `main` method is another way to run the benchmarks.

### SMTP test server and authentication

The benchmarks are provided already configured to execute against a free instance of develmail.com SMTP fake server.

The execution of the benchmarks _may_ fill up the mailbox, as it is very small in the free plan.
This is also the reason why no huge numbers were used as default parameters.

The benchmarks accept `host`, `port`, `username` and `password` as parameters, so they can be configured if needed with `-p <param={v,}*>` CLI parameter; 
see [official samples](https://github.com/openjdk/jmh/blob/master/jmh-samples/src/main/java/org/openjdk/jmh/samples/JMHSample_27_Params.java#L76-L92) for more details.

### Benchmarks

`SingleMessageDeliveryBenchmark` benchmarks the sending operation of a message with size of 1, 100 or 1000 randomly-generated words.
The generation operation was delegated to `com.thedeanda:lorem` library, provided by the author under [MIT license](https://github.com/mdeanda/lorem/blob/master/license.txt).

`MultiMessageDeliveryBenchmark` benchmarks the sending operation of 1, 2, or 5 messages of previously specified sizes onto a single SMTP session.

The benchmarks are run in mode [`AverageTime`](https://javadoc.io/static/org.openjdk.jmh/jmh-core/1.29/org/openjdk/jmh/annotations/Mode.html#AverageTime),
which outputs the maximum, minimum and average time needed for the benchmarked operation.
Times are expressed in milliseconds.

For example:

```
Result "it.niccomlt.diennea.smtp.benchmarks.MultiMessageDeliveryBenchmark.testSend":
  5124,934 ±(99.9%) 220,576 ms/op [Average]
  (min, avg, max) = (5068,578, 5124,934, 5221,316), stdev = 57,283
  CI (99.9%): [4904,357, 5345,510] (assumes normal distribution)
```

Both `wordCount` and `messageCount` are parameters and can be configured if needed.
