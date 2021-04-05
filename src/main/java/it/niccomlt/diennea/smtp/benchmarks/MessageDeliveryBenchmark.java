package it.niccomlt.diennea.smtp.benchmarks;

import com.thedeanda.lorem.LoremIpsum;
import it.niccomlt.diennea.smtp.builders.MessageFactory;
import it.niccomlt.diennea.smtp.builders.SmtpSessionFactory;
import org.openjdk.jmh.annotations.*;

import javax.mail.*;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class MessageDeliveryBenchmark {

    @Param({"address@example.org"})
    private String email;

    @Param({"passo"})
    private String password;

    @Param({"localhost"})
    private String host;

    @Param({"25"})
    private int port;

    @Param({"debug@example.org"})
    private String recipient;

    @Param({"1", "100", "10000", "1000000"})
    private int wordCount;

    private Transport transport;

    private Message message;

    @Setup
    public void setup() throws MessagingException {
        var session = SmtpSessionFactory
            .sessionBuilder()
            .authenticateAs(email, password)
            .withTLS()
            .onServer(host, port)
            .buildSession();
        var subject = "Test subject";
        var body = LoremIpsum
            .getInstance()
            .getWords(this.wordCount);
        this.transport = session.getTransport();
        this.message = MessageFactory
            .messageBuilder(session)
            .from(this.email)
            .to(this.recipient)
            .withSubject(subject)
            .withTextBody(body)
            .build();
        this.transport.connect();
    }

    @Benchmark
    public void testSend() throws MessagingException {
        this.transport.sendMessage(message, message.getAllRecipients());
    }

    @TearDown
    public void tearDown() throws MessagingException {
        this.transport.close();
    }
}
