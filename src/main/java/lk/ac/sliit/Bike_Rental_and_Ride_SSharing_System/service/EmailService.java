package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name:Bike Rental & Ride Sharing}")
    private String appName;

    /**
     * Send an HTML email asynchronously so it doesn't block the main thread.
     */
    @Async
    public void sendHtmlEmail(String toEmail, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, appName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = HTML

            mailSender.send(message);
            log.info("Email sent to {} | Subject: {}", toEmail, subject);

        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }

    /**
     * Build a styled HTML email from a title + message body.
     */
    public String buildEmailHtml(String username, String title, String messageBody) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="UTF-8"/>
                  <style>
                    body { font-family: Arial, sans-serif; background: #f4f4f4; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 40px auto; background: #ffffff;
                                 border-radius: 8px; overflow: hidden;
                                 box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
                    .header { background: #1a73e8; padding: 24px 32px; }
                    .header h1 { color: #ffffff; margin: 0; font-size: 22px; }
                    .body { padding: 32px; color: #333333; line-height: 1.6; }
                    .body h2 { color: #1a73e8; margin-top: 0; }
                    .footer { background: #f8f8f8; padding: 16px 32px;
                              font-size: 12px; color: #888888; text-align: center; }
                    .btn { display: inline-block; margin-top: 20px; padding: 12px 24px;
                           background: #1a73e8; color: #ffffff; text-decoration: none;
                           border-radius: 4px; font-weight: bold; }
                  </style>
                </head>
                <body>
                  <div class="container">
                    <div class="header">
                      <h1>🚲 Bike Rental &amp; Ride Sharing</h1>
                    </div>
                    <div class="body">
                      <h2>%s</h2>
                      <p>Hi <strong>%s</strong>,</p>
                      <p>%s</p>
                    </div>
                    <div class="footer">
                      &copy; 2026 Bike Rental &amp; Ride Sharing System. All rights reserved.<br/>
                      This is an automated message, please do not reply.
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(title, username, messageBody);
    }
}