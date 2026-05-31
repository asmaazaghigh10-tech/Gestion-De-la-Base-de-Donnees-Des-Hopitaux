CREATE DATABASE IF NOT EXISTS hopital_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE hopital_db;

-- ── Table des utilisateurs (secrétaires et médecins) ──────────────────────
CREATE TABLE IF NOT EXISTS utilisateurs (
    cin_med_sec VARCHAR(20)  NOT NULL PRIMARY KEY,
    nom_complet VARCHAR(100) NOT NULL,
    profil      VARCHAR(30)  NOT NULL   -- 'SECRETAIRE' ou 'MEDECIN'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Table des patients ────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS patients (
    cin            VARCHAR(20)  NOT NULL PRIMARY KEY,
    nom_patient    VARCHAR(100) NOT NULL,
    telephone      VARCHAR(20),
    date_naissance DATE,
    maladie        VARCHAR(255),
    status         VARCHAR(50)  DEFAULT 'En attente',
    ordonnance     TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Table des consultations ───────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS consultations (
    id_consultation INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    date_heure      DATETIME,
    motif           VARCHAR(255) NOT NULL,
    id_patient      VARCHAR(20)  NOT NULL,
    id_medecin      VARCHAR(20)  NOT NULL,
    FOREIGN KEY (id_patient) REFERENCES patients(cin)        ON DELETE CASCADE,
    FOREIGN KEY (id_medecin) REFERENCES utilisateurs(cin_med_sec)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Table des ordonnances ─────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS ordonnances (
    id_ordonnance     INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    prescription      TEXT         NOT NULL,
    date_prescription DATETIME,
    id_consultation   INT          NOT NULL UNIQUE,
    FOREIGN KEY (id_consultation) REFERENCES consultations(id_consultation) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ═══════════════════════════════════════════════════════════════
--  Données de test — Utilisateurs
-- ═══════════════════════════════════════════════════════════════

INSERT  INTO utilisateurs (cin_med_sec, nom_complet, profil) VALUES
    ('SEC001',  'Sanaa El Amrani',   'SECRETAIRE'),
    ('SEC002',  'Nadia Benali',      'SECRETAIRE'),
    ('MED001',  'Dr. Karim Tazi',    'MEDECIN'),
    ('MED002',  'Dr. Fatima Zahra',  'MEDECIN');

-- ═══════════════════════════════════════════════════════════════
--  Données de test — Patients
-- ═══════════════════════════════════════════════════════════════

INSERT  INTO patients (cin, nom_patient, telephone, date_naissance, maladie, status) VALUES
    ('P001', 'Ahmed Benkirane',  '0661234567', '1985-03-15', 'Hypertension',     'En attente'),
    ('P002', 'Khadija Moussaoui','0672345678', '1992-07-22', 'Diabète type 2',   'En cours'),
    ('P003', 'Youssef Alami',    '0683456789', '1978-11-08', 'Grippe','En attente'),
    ('P004', 'Meriem Cherkaoui', '0694567890', '2001-01-30', 'Allergie',         'Terminé');



