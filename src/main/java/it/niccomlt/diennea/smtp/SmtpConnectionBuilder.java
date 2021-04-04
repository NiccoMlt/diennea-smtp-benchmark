package it.niccomlt.diennea.smtp;

import javax.mail.*;
import javax.net.ssl.SSLSocketFactory;
import java.util.Properties;

public class SmtpConnectionBuilder {
    private static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
    private static final String MAIL_SMTP_SOCKET_FACTORY_CLASS = "mail.smtp.socketFactory.class";
    private static final String MAIL_SMTP_HOST = "mail.smtp.socketFactory.host";
    private static final String MAIL_SMTP_PORT = "mail.smtp.port";

    public static AuthenticationStep newBuilder() {
        return new Steps();
    }

    private SmtpConnectionBuilder() {

    }

    public interface AuthenticationStep {
        SslStep authenticateAs(String email, String password);
    }

    public interface SslStep {
        ServerStep withSsl();
        ServerStep withoutSsl();
    }

    public interface ServerStep {
        LastStep onServer(String host, int port);
    }

    public interface LastStep {
        Session buildSession();

        Transport buildTransport() throws NoSuchProviderException;

        MessageBuilder.ReceiverStep sendMessage();
    }

    private static class Steps implements SslStep, AuthenticationStep, ServerStep, LastStep {
        private final Properties props = new Properties();
        private PasswordAuthentication authentication;

        @Override
        public ServerStep withSsl() {
            this.props.put(MAIL_SMTP_AUTH, "true");
            this.props.put(MAIL_SMTP_SOCKET_FACTORY_CLASS, SSLSocketFactory.class.getCanonicalName());
            return this;
        }

        @Override
        public ServerStep withoutSsl() {
            return this;
        }

        @Override
        public SslStep authenticateAs(final String email, final String password) {
            this.authentication = new PasswordAuthentication(email, password);
            return this;
        }

        @Override
        public LastStep onServer(final String host, final int port) {
            this.props.put(MAIL_SMTP_HOST, host);
            this.props.put(MAIL_SMTP_PORT, String.valueOf(port));
            return null;
        }

        @Override
        public Session buildSession() {
            return Session.getDefaultInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return authentication;
                }
            });
        }

        @Override
        public Transport buildTransport() throws NoSuchProviderException {
            return this.buildSession().getTransport();
        }

        @Override
        public MessageBuilder.ReceiverStep sendMessage() {
            return MessageBuilder
                .newBuilder(this.buildSession())
                .from(this.authentication.getUserName());
        }
    }
}
