package it.niccomlt.diennea.smtp.builders;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.net.ssl.SSLSocketFactory;
import java.util.Properties;

/**
 * Factory class for SMTP {@link Session}s and {@link Transport}s.
 * <p>
 * The build processed is done via a {@link SmtpSessionFactory#sessionBuilder() step builder}.
 */
public final class SmtpSessionFactory {
    private static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
    private static final String MAIL_SMTP_SOCKET_FACTORY_CLASS = "mail.smtp.socketFactory.class";
    private static final String SOCKET_FACTORY_CLASS_NAME = SSLSocketFactory.class.getCanonicalName();
    private static final String MAIL_SMTP_HOST = "mail.smtp.socketFactory.host";
    private static final String MAIL_SMTP_PORT = "mail.smtp.port";

    /**
     * Private constructor.
     */
    private SmtpSessionFactory() {
        throw new AssertionError(); // this constructor is meant not to be called
    }

    /**
     * Get a new builder.
     *
     * @return a new builder.
     */
    public static AuthenticationStep sessionBuilder() {
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
        SslStep authenticateAs(final String email, final String password);
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

    /**
     * Third step of the build: configure the SMTP server.
     */
    public interface ServerStep {
        /**
         * Configure the host and the port of the SMTP server.
         *
         * @param host the host name.
         * @param port the port on the host.
         * @return this builder at the next step.
         */
        LastStep onServer(final String host, final int port);
    }

    /**
     * Last step of the build: build the session.
     */
    public interface LastStep {
        /**
         * Build and get a new SMTP session with the specified parameters.
         *
         * @return a new SMTP session.
         */
        Session buildSession();

        /**
         * Build a new SMTP session with the specified parameters and get the transport.
         *
         * @return a new SMTP transport.
         * @throws NoSuchProviderException if the provider is not found.
         */
        Transport buildTransport() throws NoSuchProviderException;

        /**
         * Build a new SMTP session with the specified parameters and start building a new message to send.
         *
         * @return a new message builder configured with the built SMTP session.
         * @throws AddressException   if {@link MessageFactory.SenderStep#from(String) sender}
         *                            cannot be parsed as a JavaMail {@link Address}.
         * @throws MessagingException if another error happens during {@link Message} configuration.
         */
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
            var sessionProperties = new Properties();
            sessionProperties.putAll(this.props);
            return Session.getDefaultInstance(sessionProperties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    // the authentication object is immutable, so a defensive copy is not needed
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
