CREATE TABLE IF NOT EXISTS inbox_lock (id INTEGER PRIMARY KEY);
INSERT INTO inbox_lock(id) SELECT 1 WHERE NOT EXISTS (SELECT 1 FROM inbox_lock WHERE id=1);
CREATE TABLE IF NOT EXISTS inbox_events (id VARCHAR(36) PRIMARY KEY,received_at TIMESTAMP WITH TIME ZONE NOT NULL);
CREATE TABLE IF NOT EXISTS appointment_snapshots (id VARCHAR(36) PRIMARY KEY,version BIGINT NOT NULL,patient_email VARCHAR(254) NOT NULL,starts_at TIMESTAMP WITH TIME ZONE NOT NULL);
CREATE TABLE IF NOT EXISTS notification_jobs (id VARCHAR(36) PRIMARY KEY,appointment_id VARCHAR(36) NOT NULL REFERENCES appointment_snapshots(id),
 appointment_version BIGINT NOT NULL,kind VARCHAR(20) NOT NULL,due_at TIMESTAMP WITH TIME ZONE NOT NULL,status VARCHAR(20) NOT NULL,
 attempts INTEGER NOT NULL,sent_at TIMESTAMP WITH TIME ZONE,CONSTRAINT unique_appointment_job UNIQUE(appointment_id,appointment_version,kind));
CREATE INDEX IF NOT EXISTS idx_jobs_pending ON notification_jobs(status,due_at);
