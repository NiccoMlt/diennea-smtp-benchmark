package it.niccomlt.diennea.smtp.benchmarks;

import it.niccomlt.diennea.smtp.builders.MessageFactory;
import org.openjdk.jmh.annotations.*;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Benchmark the sending of multiple messages onto a single SMTP session transport.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(value = 1, warmups = 1)
@Warmup(iterations = 1)
@State(Scope.Benchmark)
public class MultiMessageDeliveryBenchmark {

    @Param({"1", "2", "5"/*, "10", "100"*/})
    private int messageCount;

    private List<Message> messages;

    @Setup
    public void setup(final SessionState sessionState, final MessageState messageState) {
        this.messages = IntStream
            .range(0, messageCount)
            .mapToObj(i -> {
                try {
                    return MessageFactory
                        .messageBuilder(sessionState.getSession())
                        .from(messageState.getSender())
                        .to(messageState.getRecipient())
                        .withSubject(messageState.getSubject() + " " + i)
                        .withTextBody(messageState.getBody())
                        .build();
                } catch (MessagingException e) {
                    throw new IllegalArgumentException(e);
                }
            })
            .collect(Collectors.toUnmodifiableList());
    }

    @Benchmark
    public void testSend(final SessionState state) throws MessagingException {
        var transport = state.getTransport();
        for (var message : this.messages) {
            transport.sendMessage(message, message.getAllRecipients());
        }
    }
}
