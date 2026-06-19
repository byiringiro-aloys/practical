# Government of Rwanda - Enterprise Resource Planning (ERP)
## Employee & Payroll Management - Backend

Spring Boot 3 · Java 21 · PostgreSQL · JWT · Swagger UI

---

## Roles

| Role | Responsibilities |
|------|-----------------|
| **ADMIN** | Manage employees, deductions, **approve** payroll, view all payslips & messages |
| **MANAGER** | **Start / generate** payroll for a given month/year, view payslips & messages |
| **EMPLOYEE** | View own payslips and salary-credited messages |

> Payroll workflow: **MANAGER** calls `/api/payroll/generate` → **ADMIN** calls `/api/payroll/approve`

---

## ERD (Entity-Relationship Diagram)

```
+----------------+       +------------------+
|   employees    | 1---1 |   employment     |
|----------------|       |------------------|
| id (PK)        |       | id (PK)          |
| first_name     |       | emp_code (unique)|
| last_name      |       | department       |
| email          |       | position         |
| district       |       | salary (base)    |
| mobile         |       | status           |
| date_of_birth  |       | joining_date     |
+------+---------+       | employee_fk (FK) |
       |                 +------------------+
       | 1
       |
       | N           +----------------------+
       +-------------+      payslips        |
       |             |----------------------|
       |             | id (PK)              |
       |             | employee_id (FK)     |
       |             | month / year         |
       |             | base_salary          |
       |             | house_amount         |
       |             | transport_amount     |
       |             | gross_salary         |
       |             | employee_tax         |
       |             | pension              |
       |             | medical_insurance    |
       |             | other_deductions     |
       |             | net_salary           |
       |             | status (PENDING/PAID)|
       |             | UNIQUE(emp,month,yr) |
       |             +----------------------+
       |
       | N           +----------------------+
       +-------------+      messages        |
       |             |----------------------|
       |             | id (PK)              |
       |             | employee_id (FK)     |
       |             | month / year         |
       |             | content (TEXT)       |
       |             | sent_at              |
       |             +----------------------+
       |
       | 1           +----------------------+
       +-------------+       users          |
                     |----------------------|
                     | id (PK)              |
                     | email (unique)       |
                     | password (bcrypt)    |
                     | role (ADMIN /        |
                     |       MANAGER /      |
                     |       EMPLOYEE)      |
                     | employee_id (FK,opt) |
                     +----------------------+

+----------------------+
|      deductions      |
|----------------------|
| id (PK)              |
| name (unique)        |
| percentage           |
+----------------------+
```

---

## Spring Boot Flow Diagram

```
HTTP Request
     |
     v
JwtAuthFilter  ---- validates Bearer token ---> SecurityContext
     |
     v
DispatcherServlet
     |
     +---> AuthController      -> AuthService      -> UserRepository + JwtUtil
     |
     +---> EmployeeController  -> EmployeeService  -> EmployeeRepository
     |                                             -> EmploymentRepository
     |                                             -> UserRepository
     |
     +---> DeductionController -> DeductionService -> DeductionRepository
     |
     +---> PayrollController
              |
              +-- POST /generate  [MANAGER | ADMIN]
              |       -> PayrollService -> EmploymentRepository (ACTIVE filter)
              |                        -> DeductionRepository  (rate lookup)
              |                        -> PayslipRepository    (duplicate guard)
              |
              +-- POST /approve   [ADMIN only]
              |       -> PayrollService -> PayslipRepository   (PENDING -> PAID)
              |                        -> MessageRepository    (notifications)
              |
              +-- GET  /           [ADMIN | MANAGER]
              +-- GET  /messages   [ADMIN | MANAGER]
              +-- GET  /my-payslips       [Authenticated]
              +-- GET  /my-messages       [Authenticated]

DB Trigger (fn_payslip_after_insert)
  ON INSERT INTO payslips
  +-- INSERT INTO messages  (salary-credited notification)
  +-- UPDATE payslips SET status = 'PAID'
```

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 21      |
| Maven | 3.9+   |
| PostgreSQL | 14+ |

Create the database before starting the app:
```sql
CREATE DATABASE erp_db;
```

---

## Configuration

`src/main/resources/application.properties` - update DB credentials if needed:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/erp_db
spring.datasource.username=postgres
spring.datasource.password=root@123
app.institution.name=Government of Rwanda
```

---

## Run

```bash
./mvnw spring-boot:run
```

On first boot the `DataSeeder` automatically creates:
- **Admin account** - `admin@erp.rw` / `admin123`
- **Manager account** - `manager@erp.rw` / `manager123`
- **2 sample employees** with default password = their `emp_code`
  - Mugabo Javis (`EMP-0001`) - ACTIVE, 700,000 RWF
  - Michou Michell (`EMP-0002`) - ACTIVE, 850,000 RWF
- **6 default deductions** (EmployeeTax 30%, Pension 6%, MedicalInsurance 5%, Others 5%, House 14%, Transport 14%)

---

## Install DB Trigger (Task 5)

After the app has started once (so JPA creates the schema), run:
```bash
psql -U postgres -d erp_db -f src/main/resources/db/payslip_trigger.sql
```

This installs `trg_payslip_after_insert` which fires on every `INSERT INTO payslips` and:
1. Writes the salary-credited message to `messages`.
2. Sets `payslip.status = 'PAID'`.

---

## Swagger UI

`http://localhost:8080/swagger-ui.html`

Click **Authorize**, enter:
```
Bearer <token from /api/auth/login>
```

---

## REST API Reference

### Authentication

| Method | Endpoint | Body | Auth |
|--------|----------|------|------|
| POST | `/api/auth/login` | `{email, password}` | Public |
| POST | `/api/auth/register` | `{email, password, role}` | ADMIN only |
| PUT | `/api/auth/change-password` | `{currentPassword, newPassword}` | Authenticated |

**How each role enters the system:**
- **ADMIN** – seeded on first boot (`admin@erp.rw` / `admin123`), or created by an existing ADMIN via `/api/auth/register`
- **MANAGER** – created by ADMIN via `/api/auth/register` with `"role": "MANAGER"`
- **EMPLOYEE** – account is created automatically when ADMIN adds an employee via `POST /api/employees`. Default password is their `emp_code` (e.g. `EMP-0001`). Employee should change it on first login via `PUT /api/auth/change-password`

### Employee Management

| Method | Endpoint | Auth |
|--------|----------|------|
| POST | `/api/employees` | ADMIN |
| GET | `/api/employees` | Authenticated |
| GET | `/api/employees/{id}` | Authenticated |
| PUT | `/api/employees/{id}` | ADMIN |
| DELETE | `/api/employees/{id}` | ADMIN |

**Create/Update body:**
```json
{
  "firstName": "Peter", "lastName": "Mugisha",
  "email": "peter@example.com", "district": "Kigali",
  "mobile": "+250788000001", "dateOfBirth": "1990-03-15",
  "employeeId": "EMP-0001", "department": "Finance",
  "position": "Finance Officer", "salary": 700000,
  "status": "ACTIVE", "joiningDate": "2020-01-10"
}
```

### Deductions & Taxes

| Method | Endpoint | Auth |
|--------|----------|------|
| POST | `/api/deductions` | ADMIN |
| GET | `/api/deductions` | ADMIN |
| GET | `/api/deductions/{id}` | ADMIN |
| PUT | `/api/deductions/{id}` | ADMIN |
| DELETE | `/api/deductions/{id}` | ADMIN |

### Payroll

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/payroll/generate` | Generate payroll for all ACTIVE employees | **MANAGER** or ADMIN |
| POST | `/api/payroll/approve` | Approve payroll - mark PAID + send messages | **ADMIN only** |
| GET | `/api/payroll?month=6&year=2026` | View all payslips for a period | ADMIN or MANAGER |
| GET | `/api/payroll/messages?month=6&year=2026` | View messages for a period | ADMIN or MANAGER |
| GET | `/api/payroll/my-payslips` | View own payslips | Authenticated |
| GET | `/api/payroll/my-payslips/period?month=6&year=2026` | View own payslip for period | Authenticated |
| GET | `/api/payroll/my-messages` | View own salary notifications | Authenticated |

**Generate/Approve body:**
```json
{ "month": 6, "year": 2026 }
```

---

## Payroll Computation Formulas

```
House         = baseSalary x 14 / 100
Transport     = baseSalary x 14 / 100
Gross Salary  = baseSalary + House + Transport

EmployeeTax   = baseSalary x 30 / 100
Pension       = baseSalary x 6  / 100
MedicalIns.   = baseSalary x 5  / 100
Others        = baseSalary x 5  / 100

Net Salary    = baseSalary - (EmployeeTax + Pension + MedicalIns + Others)
```

**Worked example - Peter (base = 700,000 RWF):**

| Field | Calculation | Amount (RWF) |
|-------|-------------|-------------|
| House | 700,000 x 14% | 98,000 |
| Transport | 700,000 x 14% | 98,000 |
| **Gross** | 700,000 + 98,000 + 98,000 | **896,000** |
| EmployeeTax | 700,000 x 30% | 210,000 |
| Pension | 700,000 x 6% | 42,000 |
| Medical | 700,000 x 5% | 35,000 |
| Others | 700,000 x 5% | 35,000 |
| **Net** | 700,000 - (210,000+42,000+35,000+35,000) | **378,000** |

---

## Message Format

```
Dear Peter, Your salary of 6/2026 from Government of Rwanda 378000.00 has been credited to your EMP-0001 account Successfully.
```

---

## Security

- JWT Bearer tokens (24-hour expiry)
- BCrypt password hashing
- Role-based access: `ROLE_ADMIN` / `ROLE_MANAGER` / `ROLE_EMPLOYEE`
- Stateless sessions (no cookies)
