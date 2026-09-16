CREATE TABLE IF NOT EXISTS app_users (username VARCHAR(100) PRIMARY KEY,password_hash VARCHAR(100) NOT NULL,role VARCHAR(20) NOT NULL,subject_id VARCHAR(36));
CREATE TABLE IF NOT EXISTS patients (id VARCHAR(36) PRIMARY KEY,name VARCHAR(120) NOT NULL,email VARCHAR(254) NOT NULL);
CREATE TABLE IF NOT EXISTS doctors (id VARCHAR(36) PRIMARY KEY,name VARCHAR(120) NOT NULL);
CREATE TABLE IF NOT EXISTS appointments (
 id VARCHAR(36) PRIMARY KEY,patient_id VARCHAR(36) NOT NULL REFERENCES patients(id),doctor_id VARCHAR(36) NOT NULL REFERENCES doctors(id),
 starts_at TIMESTAMP WITH TIME ZONE NOT NULL,duration_minutes INTEGER NOT NULL,reason VARCHAR(500) NOT NULL,clinical_notes VARCHAR(5000) NOT NULL,
 version BIGINT NOT NULL,created_at TIMESTAMP WITH TIME ZONE NOT NULL,updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT appointment_duration CHECK (duration_minutes BETWEEN 10 AND 240));
CREATE INDEX IF NOT EXISTS idx_appointments_patient_time ON appointments(patient_id,starts_at);
CREATE INDEX IF NOT EXISTS idx_appointments_doctor_time ON appointments(doctor_id,starts_at);
CREATE TABLE IF NOT EXISTS outbox_events (id VARCHAR(36) PRIMARY KEY,payload TEXT NOT NULL,created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 next_attempt_at TIMESTAMP WITH TIME ZONE NOT NULL,attempts INTEGER NOT NULL,sent_at TIMESTAMP WITH TIME ZONE);
CREATE INDEX IF NOT EXISTS idx_outbox_pending ON outbox_events(sent_at,next_attempt_at);
