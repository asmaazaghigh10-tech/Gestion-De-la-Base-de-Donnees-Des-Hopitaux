package hopital;

import javax.persistence.*;
import java.util.Date;

/**
 * Représente un patient dans le système hospitalier.
 * Contient toutes les infos médicales et administratives.
 */
@Entity
@Table(name = "patients")
public class Patient {

    @Id
    @Column(name = "cin", nullable = false, length = 20)
    private String cin;

    @Column(name = "nom_patient", nullable = false, length = 100)
    private String nomPatient;

    @Column(name = "telephone", length = 20)
    private String telephone;

    @Column(name = "date_naissance")
    @Temporal(TemporalType.DATE)
    private Date dateNaissance;

    @Column(name = "maladie", length = 255)
    private String maladie;

    // "En attente" | "En cours" | "Terminé"
    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "ordonnance", columnDefinition = "TEXT")
    private String ordonnance;

    public Patient() {
        this.status = "En attente"; // Valeur par défaut à la création
    }

    // ── Getters & Setters ──────────────────────────────────────────────────

    public String getCin() { return cin; }
    public void setCin(String cin) { this.cin = cin; }

    public String getNomPatient() { return nomPatient; }
    public void setNomPatient(String nomPatient) { this.nomPatient = nomPatient; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public Date getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(Date dateNaissance) { this.dateNaissance = dateNaissance; }

    public String getMaladie() { return maladie; }
    public void setMaladie(String maladie) { this.maladie = maladie; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getOrdonnance() { return ordonnance; }
    public void setOrdonnance(String ordonnance) { this.ordonnance = ordonnance; }

    @Override
    public String toString() {
        return nomPatient + " (" + cin + ")";
    }
}