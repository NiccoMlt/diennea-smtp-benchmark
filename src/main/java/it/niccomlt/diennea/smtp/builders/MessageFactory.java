package it.niccomlt.diennea.smtp.builders;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Factory class for JavaMail {@link Message} objects.
 * <p>
 * The build processed is done via a {@link MessageFactory#messageBuilder(Session) step builder}.
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
    }

    /**
     * Last step of the builder: build the message.
     */
    public interface LastStep {
        /**
         * Build and get a new message with the specified parameters.
         *
         * @return a new message.
         */
        Message build() throws MessagingException;

        /**
         * Build and send a new message.
         *
         * @throws SendFailedException if {@link Message} could not be sent.
         * @throws MessagingException  if some error happens during {@link Message} configuration or send.
         */
        void buildAndSend() throws MessagingException;
    }

    private static class Steps implements SenderStep, ReceiverStep, SubjectStep, BodyStep, LastStep {
        private final Session session;
        private String sender;
        private String recipient;
        private String subject;
        private String body;

        public Steps(final Session session) {
            this.session = session;
        }

        @Override
        public ReceiverStep from(final String address) throws AddressException {
            this.sender = validateAddress(address);
            return this;
        }

        @Override
        public SubjectStep to(final String address) throws AddressException {
            this.recipient = validateAddress(address);
            return this;
        }

        @Override
        public BodyStep withSubject(final String subject) {
            this.subject = subject;
            return this;
        }

        @Override
        public LastStep withTextBody(final String messageBody) {
            this.body = messageBody;
            return this;
        }

        @Override
        public Message build() throws MessagingException {
            var message = new MimeMessage(session);
            message.setFrom(new InternetAddress(this.sender));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(this.recipient));
            message.setSubject(this.subject);
            message.setText(this.body);
            return message;
        }

        @Override
        public void buildAndSend() throws MessagingException {
            Transport.send(this.build());
        }

        /**
         * Validate email address.
         *
         * @param address the email address.
         * @return the email address, if valid.
         * @throws AddressException if address cannot be parsed as a JavaMail {@link Address}.
         */
        private String validateAddress(final String address) throws AddressException {
            return new InternetAddress(address).getAddress();
        }
    }
}
