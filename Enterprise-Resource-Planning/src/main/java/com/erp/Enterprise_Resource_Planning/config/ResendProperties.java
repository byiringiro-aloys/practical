package com.erp.Enterprise_Resource_Planning.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ResendProperties {

    @Value("${resend.api-key}")
    private String apiKey;

    /**
     * Sender address shown on the email, e.g.:
     *   ERP Payroll <onboarding@resend.dev>
     * Must match a domain verified in your Resend account.
     * The default onboarding@resend.dev works on the free plan for testing.
     */
    @Value("${resend.from}")
    private String from;

    public String getApiKey() { return apiKey; }
    public String getFrom()   { return from; }
}
