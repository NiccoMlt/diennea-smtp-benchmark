package it.niccomlt.diennea.smtp;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.*;

public class MessageBuilder {
    private String fromAddress;
    private String toAddress;
    private String subject;
    private String body;

    public MessageBuilder from(final String address) {
        this.fromAddress = address;
        return this;
    }

    public MessageBuilder to(final String address) {
        this.toAddress = address;
        return this;
    }

    public MessageBuilder withSubject(final String subject) {
        this.subject = subject;
        return this;
    }

    public MessageBuilder withBody(final String messageBody) {
        this.body = messageBody;
        return this;
    }

    public Message build(final Session session) throws MessagingException {
        var message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromAddress));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(toAddress));
        message.setSubject(this.subject);
        var bodyPart = new MimeBodyPart();
        bodyPart.setContent(this.body, "text/html");
        message.setContent(new MimeMultipart(bodyPart));
        return message;
    }
}
