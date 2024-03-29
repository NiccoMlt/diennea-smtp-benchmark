package it.niccomlt.diennea.smtp.builders;

import javax.mail.*;
import javax.net.ssl.SSLSocketFactory;
import java.util.Properties;

/**
 * Factory class for SMTP {@link Session}s and {@link Transport}s.
 * <p>
 * The build processed is done via a {@link SmtpSessionFactory#sessionBuilder() step builder}.
 */
public final class SmtpSessionFactory {
    private static final String MAIL_SMTP_SOCKET_FACTORY_FALLBACK = "mail.smtp.socketFactory.fallback";
    private static final String MAIL_SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable";
    private static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
    private static final String MAIL_SMTP_SOCKET_FACTORY_CLASS = "mail.smtp.socketFactory.class";
    private static final String SOCKET_FACTORY_CLASS_NAME = SSLSocketFactory.class.getCanonicalName();
    private static final String MAIL_SMTP_SOCKET_FACTORY_HOST = "mail.smtp.socketFactory.host";
    private static final String MAIL_SMTP_HOST = "mail.smtp.host";
    private static final String MAIL_SMTP_PORT = "mail.smtp.port";
    private static final String MAIL_SMTP_SOCKET_FACTORY_PORT = "mail.smtp.socketFactory.port";

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
        EncryptionStep authenticateAs(final String email, final String password);

        /**
         * Set the connection not to use authentication.
         *
         * @return this builder at the next step.
         */
        ServerStep doNotAuthenticate();
    }

    /**
     * Second step of the build: configure encryption for authentication.
     */
    public interface EncryptionStep {
        /**
         * Configure the connection to use TLS.
         *
         * @return this builder at the next step.
         */
        ServerStep withTLS();

        /**
         * Configure the connection to use SSL.
         *
         * @return this builder at the next step.
         */
        ServerStep withSSL();
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
         */
        MessageFactory.SenderStep sendMessage();
    }

    private static class Steps implements EncryptionStep, AuthenticationStep, ServerStep, LastStep {
        private final Properties props = new Properties();
        private PasswordAuthentication authentication;
        private boolean useSsl = false;

        @Override
        public ServerStep withSSL() {
            this.useSsl = true;
            this.props.put(MAIL_SMTP_SOCKET_FACTORY_CLASS, SOCKET_FACTORY_CLASS_NAME);
            this.props.put(MAIL_SMTP_SOCKET_FACTORY_FALLBACK, "false");
            return this;
        }

        @Override
        public ServerStep withTLS() {
            this.props.put(MAIL_SMTP_STARTTLS_ENABLE, "true");
            return this;
        }

        @Override
        public EncryptionStep authenticateAs(final String email, final String password) {
            this.props.put(MAIL_SMTP_AUTH, "true");
            this.authentication = new PasswordAuthentication(email, password);
            return this;
        }

        @Override
        public ServerStep doNotAuthenticate() {
            return this;
        }

        @Override
        public LastStep onServer(final String host, final int port) {
            this.props.put(MAIL_SMTP_HOST, host);
            this.props.put(MAIL_SMTP_PORT, String.valueOf(port));
            if (this.useSsl) {
                this.props.put(MAIL_SMTP_SOCKET_FACTORY_HOST, host);
                this.props.put(MAIL_SMTP_SOCKET_FACTORY_PORT, String.valueOf(port));
            }
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
        public MessageFactory.SenderStep sendMessage() {
            return MessageFactory.messageBuilder(this.buildSession());
        }
    }
}
