package it.niccomlt.diennea.smtp.benchmarks;

import com.thedeanda.lorem.LoremIpsum;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class MessageState {

    @Param({"niccolo.maltoni@gmail.com"})
    private String sender;

    @Param({"niccolo.maltoni@gmail.com"})
    private String recipient;

    @Param({"1", "100", "1000"/*, "10000"*/})
    private int wordCount;

    private String subject;

    private String body;

    @Setup
    public void setup() {
        this.subject = "Test subject";
        this.body = LoremIpsum
            .getInstance()
            .getWords(wordCount);
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }
}
