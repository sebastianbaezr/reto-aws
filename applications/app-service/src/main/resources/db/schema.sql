-- =====================================================
-- Schema initialization for RetoAws application
-- Database: reto_aws
-- DBMS: PostgreSQL 10+
-- =====================================================

-- Drop table if exists (use with caution in production)
-- DROP TABLE IF EXISTS people CASCADE;

-- Create people table
CREATE TABLE IF NOT EXISTS people (
    id BIGSERIAL PRIMARY KEY,
    identification VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT uk_people_identification UNIQUE (identification),
    CONSTRAINT uk_people_email UNIQUE (email),
    CONSTRAINT ck_people_identification_not_empty CHECK (identification <> ''),
    CONSTRAINT ck_people_name_not_empty CHECK (name <> ''),
    CONSTRAINT ck_people_email_not_empty CHECK (email <> '')
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_people_identification ON people(identification);
CREATE INDEX IF NOT EXISTS idx_people_email ON people(email);
CREATE INDEX IF NOT EXISTS idx_people_created_at ON people(created_at);

-- Grant permissions (adjust user as needed)
-- GRANT SELECT, INSERT, UPDATE, DELETE ON people TO reto_user;
-- GRANT USAGE, SELECT ON SEQUENCE people_id_seq TO reto_user;

-- Comment on table
COMMENT ON TABLE people IS 'Stores person information for the RetoAws application';
COMMENT ON COLUMN people.id IS 'Primary key - auto-generated identifier';
COMMENT ON COLUMN people.identification IS 'Unique identification number (document/ID)';
COMMENT ON COLUMN people.name IS 'Full name of the person';
COMMENT ON COLUMN people.email IS 'Unique email address';
COMMENT ON COLUMN people.created_at IS 'Timestamp when the record was created (auto-generated)';
COMMENT ON COLUMN people.updated_at IS 'Timestamp when the record was last updated (auto-generated)';
