package it.niccomlt.diennea.smtp.benchmarks;

import it.niccomlt.diennea.smtp.builders.MessageFactory;
import it.niccomlt.diennea.smtp.builders.SmtpSessionFactory;
import org.openjdk.jmh.annotations.*;

import javax.mail.MessagingException;
import javax.mail.Session;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class MessageDeliveryBenchmark {
    @Param({"address@example.org"})
    private String email;

    @Param({"pass"})
    private String password;

    @Param({"localhost"})
    private String host;

    @Param({"25"})
    private int port;

    @Param({"debug@example.org"})
    private String recipient;

    @Param({"foo"})
    private String subject;

    @Param({"bar"})
    private String body;

    private Session session;

    @Setup
    public void setup() {
        this.session = SmtpSessionFactory
            .sessionBuilder()
            .authenticateAs(email, password)
            .withoutSsl()
            .onServer(host, port)
            .buildSession();
    }

    @Benchmark
    public void testSend() throws MessagingException {
        MessageFactory
            .messageBuilder(this.session)
            .from(this.email)
            .to(this.recipient)
            .withSubject(this.subject)
            .withTextBody(this.body)
            .buildAndSend();
    }
}
