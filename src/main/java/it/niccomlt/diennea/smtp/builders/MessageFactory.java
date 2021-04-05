package it.niccomlt.diennea.smtp.builders;

import javax.mail.*;
import javax.mail.internet.*;

/**
 * Step builder for JavaMail {@link Message} objects.
 */
public final class MessageFactory {

    /**
     * Private constructor.
     */
    private MessageFactory() {
        throw new AssertionError(); // this constructor is meant not to be called
    }

    /**
     * Get a new builder.
     *
     * @param session an instance of JavaMail {@link Session}.
     * @return a new builder.
     */
    public static SenderStep messageBuilder(final Session session) {
        return new Steps(session);
    }

    /**
     * First step of the builder: specify the sender of the email.
     */
    public interface SenderStep {
        /**
         * Set the sender of the email.
         *
         * @param address the sender email address.
         * @return this builder at the next step.
         * @throws AddressException   if address cannot be parsed as a JavaMail {@link Address}.
         * @throws MessagingException if another error happens during {@link Message} configuration.
         */
        ReceiverStep from(final String address) throws MessagingException;
    }

    /**
     * Second step of the builder: specify the recipient of the email.
     */
    public interface ReceiverStep {
        /**
         * Set the recipient of the email.
         *
         * @param address the recipient email address.
         * @return this builder at the next step.
         * @throws AddressException   if address cannot be parsed as a JavaMail {@link Address}.
         * @throws MessagingException if another error happens during {@link Message} configuration.
         */
        SubjectStep to(final String address) throws MessagingException;
    }

    /**
     * Third step of the builder: specify the subject of the email.
     */
    public interface SubjectStep {
        /**
         * Set the subject of the email.
         *
         * @param subject the subject of the email.
         * @return this builder at the next step.
         * @throws MessagingException if some error happens during {@link Message} configuration.
         */
        BodyStep withSubject(final String subject) throws MessagingException;
    }

    /**
     * Fourth step of the builder: specify the body of the email.
     */
    public interface BodyStep {
        /**
         * Set a plain text body for the email.
         *
         * @param messageBody the body of the email as plain text.
         * @return this builder at the last step.
         * @throws MessagingException if some error happens during {@link Message} configuration.
         */
        LastStep withTextBody(final String messageBody) throws MessagingException;

        /**
         * Set an HTML body for the email.
         *
         * @param messageBody the body of the email as HTML.
         * @return this builder at the last step.
         * @throws MessagingException if some error happens during {@link Message} configuration.
         */
        LastStep withHtmlBody(final String messageBody) throws MessagingException;
    }

    /**
     * Last step of the builder: build the message.
     */
    public interface LastStep {
        /**
         * Build and get a new message.
         *
         * @return a new message.
         */
        Message build();

        /**
         * Build and send a new message.
         *
         * @throws SendFailedException if {@link Message} could not be sent.
         * @throws MessagingException  if some error happens during {@link Message} configuration or send.
         */
        void buildAndSend() throws MessagingException;
    }

    private static class Steps implements SenderStep, ReceiverStep, SubjectStep, BodyStep, LastStep {
        private final Message message;

        public Steps(final Session session) {
            this.message = new MimeMessage(session);
        }

        @Override
        public ReceiverStep from(String address) throws MessagingException {
            this.message.setFrom(new InternetAddress(address));
            return this;
        }

        @Override
        public SubjectStep to(String address) throws MessagingException {
            this.message.setRecipient(Message.RecipientType.TO, new InternetAddress(address));
            return this;
        }

        @Override
        public BodyStep withSubject(String subject) throws MessagingException {
            this.message.setSubject(subject);
            return this;
        }

        @Override
        public LastStep withHtmlBody(String messageBody) throws MessagingException {
            var bodyPart = new MimeBodyPart();
            bodyPart.setContent(messageBody, "text/html");
            this.message.setContent(new MimeMultipart(bodyPart));
            return this;
        }

        @Override
        public LastStep withTextBody(String messageBody) throws MessagingException {
            this.message.setText(messageBody);
            return this;
        }

        @Override
        public Message build() {
            return message; // TODO: introduce defensive copy
        }

        @Override
        public void buildAndSend() throws MessagingException {
            Transport.send(this.build());
        }
    }
}
