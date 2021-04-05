package it.niccomlt.diennea.smtp.benchmarks;

import it.niccomlt.diennea.smtp.builders.SmtpSessionFactory;
import org.openjdk.jmh.annotations.*;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;

@State(Scope.Benchmark)
public class SessionState {

    @Param({"HSJO2P74VLCUD3WDOAAEWAK6JU"})
    private String username;

    @Param({"XTGPG3JJHKV3FFI37PHN2F5KWI"})
    private String password;

    @Param({"smtp.develmail.com"})
    private String host;

    @Param({"25"})
    private int port;

    private Session session;
    private Transport transport;

    @Setup
    public void setup() throws MessagingException {
        this.session = SmtpSessionFactory
            .sessionBuilder()
            .authenticateAs(username, password)
            .withTLS()
            .onServer(host, port)
            .buildSession();
        this.transport = session.getTransport();
        this.transport.connect();
    }

    public Transport getTransport() {
        return transport;
    }

    @TearDown
    public void tearDown() throws MessagingException {
        this.transport.close();
    }

    public Session getSession() {
        return this.session;
    }
}
