package com.erp.Enterprise_Resource_Planning.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title       = "Government of Rwanda – ERP Payroll API",
        version     = "1.0.0",
        description = """
            Enterprise Resource Planning – Employee & Payroll Management System.

            **Roles & access:**
            | Role     | Capabilities |
            |----------|-------------|
            | ADMIN    | Full access: employees, deductions, payroll approval, all reports |
            | MANAGER  | Generate payroll, view all payslips & messages |
            | EMPLOYEE | Own profile, payslips, messages, change password |

            **Authentication flow:**
            1. `POST /api/auth/login` – receive a JWT token
            2. Click **Authorize** above, enter `Bearer <token>`
            3. All secured endpoints now work automatically

            **Employee first-login:** default password is the employee code (e.g. `EMP-0001`).
            Change it immediately via `PUT /api/me/change-password`.
            """,
        contact = @Contact(
            name  = "ERP Support",
            email = "admin@erp.rw"
        )
    ),
    servers = @Server(url = "http://localhost:8080", description = "Local development server"),
    security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
    name        = "bearerAuth",
    type        = SecuritySchemeType.HTTP,
    scheme      = "bearer",
    bearerFormat = "JWT",
    in          = SecuritySchemeIn.HEADER,
    description = "Paste the JWT token obtained from POST /api/auth/login"
)
public class OpenApiConfig {

    /**
     * Controls the display order of tag groups in Swagger UI.
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .tags(List.of(
                new Tag().name("Authentication")
                         .description("Login (all roles) and account registration (ADMIN only)"),
                new Tag().name("My Profile")
                         .description("Employee self-service – profile, payslips, messages, password"),
                new Tag().name("Employee Management")
                         .description("Full employee CRUD – ADMIN and MANAGER"),
                new Tag().name("Deductions & Taxes")
                         .description("Configure deduction types and rates – ADMIN only"),
                new Tag().name("Payroll Management")
                         .description("Generate and approve payroll runs – ADMIN and MANAGER")
            ));
    }
}
