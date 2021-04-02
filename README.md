# SMTP Benchmarks

You have to implement a simple Java application which executes a bench on a SMTP service.

Requirements:

- The application MUST work with an SMTP service with username/password authentication
- The application MUST measure min/max/avg time of message delivery
- The application MUST measure the impact of the size of the message
- The application MAY evaluate the impact of sending multiple messages on the same SMTP connection
- The application MUST use JavaMail API