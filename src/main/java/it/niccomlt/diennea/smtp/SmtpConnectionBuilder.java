package it.niccomlt.diennea.smtp;

import javax.mail.*;
import javax.net.ssl.SSLSocketFactory;
import java.util.Properties;

public class SmtpConnectionBuilder {
    private static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
    private static final String MAIL_SMTP_SOCKET_FACTORY_FALLBACK = "mail.smtp.socketFactory.fallback";
    private static final String MAIL_SMTP_SOCKET_FACTORY_CLASS = "mail.smtp.socketFactory.class";
    private static final String MAIL_SMTP_HOST = "mail.smtp.socketFactory.host";
    private static final String MAIL_SMTP_PORT = "mail.smtp.port";

    private final Properties props = new Properties();
    private PasswordAuthentication authentication;

    private SmtpConnectionBuilder() {
    }

    public static SmtpConnectionBuilder unauthenticated() {
        var builder = new SmtpConnectionBuilder();
        builder.props.put(MAIL_SMTP_AUTH, "false");
        builder.authentication = null; // TODO
        return builder;
    }

    public static SmtpConnectionBuilder authenticated(String username, String password) {
        var builder = new SmtpConnectionBuilder();
        builder.props.put(MAIL_SMTP_AUTH, "true");
        builder.props.put(MAIL_SMTP_SOCKET_FACTORY_FALLBACK, "false");
        builder.props.put(MAIL_SMTP_SOCKET_FACTORY_CLASS, SSLSocketFactory.class.getCanonicalName());
        builder.authentication = new PasswordAuthentication(username, password);
        return builder;
    }

    public SmtpConnectionBuilder setHost(String host) {
        this.props.put(MAIL_SMTP_HOST, host);
        return this;
    }

    public SmtpConnectionBuilder setPort(int port) {
        this.props.put(MAIL_SMTP_PORT, String.valueOf(port));
        return this;
    }

    public Transport build() throws NoSuchProviderException {
        var session = Session.getDefaultInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return authentication;
            }
        });
        return session.getTransport();
    }
}
