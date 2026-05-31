package hopital;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("MEDECIN")
public class Medecin extends Utilisateur {
    public Medecin() { 
        super(); 
    }
}