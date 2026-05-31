package hopital;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "ordonnances")
public class Ordonnance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ordonnance")
    private int idOrdonnance;

    @Column(name = "prescription", nullable = false)
    private String prescription;

    @Column(name = "date_prescription")
    @Temporal(TemporalType.TIMESTAMP)
    private Date datePrescription;

    @OneToOne
    @JoinColumn(name = "id_consultation", nullable = false, unique = true)
    private Consultation consultation;

    public Ordonnance() {}

    public int getIdOrdonnance() { return idOrdonnance; }
    public void setIdOrdonnance(int idOrdonnance) { this.idOrdonnance = idOrdonnance; }

    public String getPrescription() { return prescription; }
    public void setPrescription(String prescription) { this.prescription = prescription; }

    public Date getDatePrescription() { return datePrescription; }
    public void setDatePrescription(Date datePrescription) { this.datePrescription = datePrescription; }

    public Consultation getConsultation() { return consultation; }
    public void setConsultation(Consultation consultation) { this.consultation = consultation; }
}