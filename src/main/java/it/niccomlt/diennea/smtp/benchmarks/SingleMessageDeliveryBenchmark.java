package it.niccomlt.diennea.smtp.benchmarks;

import it.niccomlt.diennea.smtp.builders.MessageFactory;
import org.openjdk.jmh.annotations.*;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(value = 1, warmups = 1)
@Warmup(iterations = 1)
@State(Scope.Benchmark)
public class SingleMessageDeliveryBenchmark {

    private Message message;

    @Setup
    public void setup(SessionState sessionState, MessageState messageState) throws MessagingException {
        this.message = MessageFactory
            .messageBuilder(sessionState.getSession())
            .from(messageState.getSender())
            .to(messageState.getRecipient())
            .withSubject(messageState.getSubject())
            .withTextBody(messageState.getBody())
            .build();
    }

    @Benchmark
    public void testSend(SessionState state) throws MessagingException {
        state.getTransport().sendMessage(this.message, this.message.getAllRecipients());
    }
}
