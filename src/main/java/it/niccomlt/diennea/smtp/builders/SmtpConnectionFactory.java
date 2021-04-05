package it.niccomlt.diennea.smtp.builders;

import javax.mail.*;
import javax.net.ssl.SSLSocketFactory;
import java.util.Properties;

public final class SmtpConnectionFactory {
    private static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
    private static final String MAIL_SMTP_SOCKET_FACTORY_CLASS = "mail.smtp.socketFactory.class";
    private static final String SOCKET_FACTORY_CLASS_NAME = SSLSocketFactory.class.getCanonicalName();
    private static final String MAIL_SMTP_HOST = "mail.smtp.socketFactory.host";
    private static final String MAIL_SMTP_PORT = "mail.smtp.port";

    /**
     * Private constructor.
     */
    private SmtpConnectionFactory() {
        throw new AssertionError(); // this constructor is meant not to be called
    }

    /**
     * Get a new builder.
     *
     * @return a new builder.
     */
    public static AuthenticationStep connectionBuilder() {
        return new Steps();
    }

    /**
     * First step of the builder: configure authentication credentials.
     */
    public interface AuthenticationStep {
        /**
         * Set the username and password to use to authenticate on SMTP server.
         *
         * @param email    the email used to authenticate.
         * @param password the password used to authenticate.
         * @return this builder at the next step.
         */
        SslStep authenticateAs(String email, String password);
    }

    /**
     * Second step of the build: configure SSL usage.
     */
    public interface SslStep {
        /**
         * Configure the connection to use SSL.
         *
         * @return this builder at the next step.
         */
        ServerStep withSsl();

        /**
         * Configure the connection not to use SSL.
         *
         * @return this builder at the next step.
         */
        ServerStep withoutSsl();
    }

    public interface ServerStep {
        LastStep onServer(String host, int port);
    }

    public interface LastStep {
        Session buildSession();

        Transport buildTransport() throws NoSuchProviderException;

        MessageFactory.ReceiverStep sendMessage() throws MessagingException;
    }

    private static class Steps implements SslStep, AuthenticationStep, ServerStep, LastStep {
        private final Properties props = new Properties();
        private PasswordAuthentication authentication;

        @Override
        public ServerStep withSsl() {
            this.props.put(MAIL_SMTP_AUTH, "true");
            this.props.put(MAIL_SMTP_SOCKET_FACTORY_CLASS, SOCKET_FACTORY_CLASS_NAME);
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
            return this;
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
        public MessageFactory.ReceiverStep sendMessage() throws MessagingException {
            return MessageFactory
                .messageBuilder(this.buildSession())
                .from(this.authentication.getUserName());
        }
    }
}
