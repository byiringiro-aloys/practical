-- ============================================================
--  Task 5 – Database Routine: Trigger + Function
--
--  JPA-generated column names used in this script:
--    employees.id          → PK of employees table
--    employees.first_name  → Employee.firstName
--    employment.emp_code   → Employment.employeeId (human-readable code)
--    employment.employee_fk → FK to employees.id
--    payslips.employee_id  → FK to employees.id
--    payslips.month / year / net_salary / status
--    messages.employee_id / month / year / content / sent_at
--
--  Behavior on INSERT into payslips:
--   1. Resolves the employee's first name and emp_code.
--   2. Inserts a salary-credited notification into messages.
--   3. Updates the new payslip's status to 'PAID'.
--
--  How to run (once, after Spring Boot has created the schema):
--    psql -U postgres -d erp_db -f src/main/resources/db/payslip_trigger.sql
-- ============================================================

-- ── 1. Trigger function ───────────────────────────────────────────────────

CREATE OR REPLACE FUNCTION fn_payslip_after_insert()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_first_name  VARCHAR;
    v_emp_code    VARCHAR;
    v_institution VARCHAR := 'Government of Rwanda';
    v_message     TEXT;
BEGIN
    -- Resolve employee first name
    SELECT e.first_name
      INTO v_first_name
      FROM employees e
     WHERE e.id = NEW.employee_id;

    -- Resolve human-readable employee code from employment
    SELECT em.emp_code
      INTO v_emp_code
      FROM employment em
     WHERE em.employee_fk = NEW.employee_id
     LIMIT 1;

    IF v_emp_code IS NULL THEN
        v_emp_code := 'N/A';
    END IF;

    -- Compose message
    v_message := FORMAT(
        'Dear %s, Your salary of %s/%s from %s %s has been credited to your %s account Successfully.',
        v_first_name,
        NEW.month::TEXT,
        NEW.year::TEXT,
        v_institution,
        NEW.net_salary::TEXT,
        v_emp_code
    );

    -- Persist notification
    INSERT INTO messages (employee_id, month, year, content, sent_at)
    VALUES (NEW.employee_id, NEW.month, NEW.year, v_message, NOW());

    -- Mark the newly inserted payslip as PAID
    UPDATE payslips
       SET status = 'PAID'
     WHERE id = NEW.id;

    RETURN NEW;
END;
$$;

-- ── 2. Drop old trigger (idempotent re-run) ───────────────────────────────

DROP TRIGGER IF EXISTS trg_payslip_after_insert ON payslips;

-- ── 3. Create trigger ─────────────────────────────────────────────────────

CREATE TRIGGER trg_payslip_after_insert
AFTER INSERT ON payslips
FOR EACH ROW
EXECUTE FUNCTION fn_payslip_after_insert();

-- ── Verification ──────────────────────────────────────────────────────────
-- SELECT tgname, tgenabled FROM pg_trigger WHERE tgname = 'trg_payslip_after_insert';
