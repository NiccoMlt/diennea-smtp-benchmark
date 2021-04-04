package it.niccomlt.diennea.smtp;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.*;

public class MessageBuilder {

    public static SenderStep newBuilder(final Session session) {
        return new Steps(session);
    }

    private MessageBuilder() {

    }

    public interface SenderStep {
        ReceiverStep from(final String address);
    }

    public interface ReceiverStep {
        SubjectStep to(final String address);
    }

    public interface SubjectStep {
        BodyStep withSubject(final String subject);
    }

    public interface BodyStep {
        LastStep withBody(final String messageBody);
    }

    public interface LastStep {
        Message build() throws MessagingException;

        void buildAndSend() throws MessagingException;
    }

    private static class Steps implements SenderStep, ReceiverStep, SubjectStep, BodyStep, LastStep {

        private final Session session;
        private String fromAddress;
        private String toAddress;
        private String subject;
        private String body;

        public Steps(final Session session) {
            this.session = session;
        }

        @Override
        public ReceiverStep from(String address) {
            this.fromAddress = address;
            return this;
        }

        @Override
        public SubjectStep to(String address) {
            this.toAddress = address;
            return this;
        }

        @Override
        public BodyStep withSubject(String subject) {
            this.subject = subject;
            return this;
        }

        @Override
        public LastStep withBody(String messageBody) {
            this.body = messageBody;
            return this;
        }

        @Override
        public Message build() throws MessagingException {
            var message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromAddress));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toAddress));
            message.setSubject(this.subject);
            var bodyPart = new MimeBodyPart();
            bodyPart.setContent(this.body, "text/html");
            message.setContent(new MimeMultipart(bodyPart));
            return message;
        }

        @Override
        public void buildAndSend() throws MessagingException {
            Transport.send(this.build());
        }
    }
}
