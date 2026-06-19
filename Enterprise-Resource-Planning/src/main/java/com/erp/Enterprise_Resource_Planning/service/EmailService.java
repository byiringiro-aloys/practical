package com.erp.Enterprise_Resource_Planning.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import com.erp.Enterprise_Resource_Planning.config.ResendProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Sends transactional emails via the Resend API.
 *
 * All send methods are best-effort: a delivery failure logs a warning
 * but never throws so it cannot roll back a payroll approval transaction.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final Resend resend;
    private final String from;

    public EmailService(ResendProperties props) {
        this.resend = new Resend(props.getApiKey());
        this.from   = props.getFrom();
    }

    /**
     * Sends the salary-credited notification to the employee.
     *
     * @param toEmail   recipient email address
     * @param firstName employee first name (used in subject and body)
     * @param message   the formatted message text already built by PayrollService
     */
    public void sendSalaryCreditedEmail(String toEmail, String firstName, String message) {
        String subject = "Your Salary Has Been Credited – " + firstName;
        String html    = buildHtml(firstName, message);

        CreateEmailOptions options = CreateEmailOptions.builder()
                .from(from)
                .to(toEmail)
                .subject(subject)
                .html(html)
                .build();

        try {
            CreateEmailResponse response = resend.emails().send(options);
            log.info("Salary email sent to {} | Resend id: {}", toEmail, response.getId());
        } catch (ResendException ex) {
            // Non-fatal – payroll is already committed; log and continue
            log.warn("Failed to send salary email to {}: {}", toEmail, ex.getMessage());
        }
    }

    // ── HTML template ─────────────────────────────────────────────────────

    private String buildHtml(String firstName, String message) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8" />
                  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                  <title>Salary Notification</title>
                  <style>
                    body  { font-family: Arial, sans-serif; background: #f4f4f4; margin: 0; padding: 0; }
                    .card { max-width: 520px; margin: 40px auto; background: #ffffff;
                            border-radius: 8px; overflow: hidden;
                            box-shadow: 0 2px 8px rgba(0,0,0,.12); }
                    .header { background: #1a56a0; color: #fff; padding: 28px 32px; }
                    .header h1 { margin: 0; font-size: 20px; }
                    .body   { padding: 28px 32px; color: #333; line-height: 1.6; }
                    .footer { background: #f9f9f9; padding: 16px 32px;
                              font-size: 12px; color: #888; text-align: center; }
                    .badge  { display: inline-block; background: #e6f2e6; color: #2a7a2a;
                              border-radius: 4px; padding: 2px 10px; font-weight: bold; }
                  </style>
                </head>
                <body>
                  <div class="card">
                    <div class="header">
                      <h1>Government of Rwanda &mdash; ERP Payroll</h1>
                    </div>
                    <div class="body">
                      <p>Dear <strong>%s</strong>,</p>
                      <p>%s</p>
                      <p>Status: <span class="badge">PAID</span></p>
                      <p>If you have any questions regarding your payslip, please contact the HR department.</p>
                    </div>
                    <div class="footer">
                      This is an automated message from the ERP Payroll System.
                      Please do not reply to this email.
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(firstName, message);
    }
}
