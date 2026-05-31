package hopital;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;

public class PatientCRUD {

    private final SessionFactory sf;

    private static final String DARK_BG     = "#1a1a2e";
    private static final String INPUT_STYLE = "-fx-background-color:#0f3460; -fx-text-fill:#0d1117;"
        + "-fx-border-color:#00b4d8; -fx-border-radius:6; -fx-background-radius:6; -fx-padding:8;";
    private static final String LABEL_STYLE = "-fx-text-fill:#90e0ef; -fx-font-size:13;";
    private static final String BTN_OK      = "-fx-background-color:#00b4d8; -fx-text-fill:#0d0d0d;"
        + "-fx-font-weight:bold; -fx-background-radius:8; -fx-padding:8 22 8 22; -fx-cursor:hand;";
    private static final String BTN_CANCEL  = "-fx-background-color:#2a2a4a; -fx-text-fill:#90e0ef;"
        + "-fx-border-color:#00b4d8; -fx-border-radius:8; -fx-background-radius:8;"
        + "-fx-padding:8 22 8 22; -fx-cursor:hand;";

    // Statuts normalises (sans accents, coherents avec la BDD)
    private static final String ST_ATTENTE = "En attente";
    private static final String ST_ENCOURS = "En cours";
    private static final String ST_TERMINE = "Termine";

    public PatientCRUD(SessionFactory sf) {
        this.sf = sf;
    }

    // =======================================================================
    //  OPERATIONS BASE DE DONNEES
    // =======================================================================

    public void ajouter(Patient p) throws Exception {
        Session session = sf.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(p);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public void modifier(Patient p) throws Exception {
        Session session = sf.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.merge(p);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public void supprimer(Patient p) throws Exception {
        Session session = sf.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Patient attache = (Patient) session.get(Patient.class, p.getCin());
            if (attache != null) session.delete(attache);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public Patient trouverParCin(String cin) {
        Session session = sf.openSession();
        try {
            return (Patient) session.get(Patient.class, cin);
        } finally {
            session.close();
        }
    }

    @SuppressWarnings("unchecked")
    public List<Patient> listerTout() {
        Session session = sf.openSession();
        try {
            return session.createQuery("FROM Patient ORDER BY nomPatient").list();
        } finally {
            session.close();
        }
    }

    @SuppressWarnings("unchecked")
    public List<Patient> listerEnAttenteOuEnCours() {
        Session session = sf.openSession();
        try {
            return session.createQuery(
                "FROM Patient WHERE status IN ('En attente','En cours') ORDER BY nomPatient"
            ).list();
        } finally {
            session.close();
        }
    }

    // =======================================================================
    //  FORMULAIRE : AJOUTER
    // =======================================================================

    public boolean afficherFormulaireAjout() {
        try {
            Dialog<ButtonType> dialog = creerDialog("Nouveau Patient");
            GridPane grid = creerGrille();

            TextField txtCin   = champ("Ex : AB123456");
            TextField txtNom   = champ("Nom et Prenom");
            TextField txtTel   = champ("Ex : 06xxxxxxxx");
            DatePicker dp      = creerDatePicker();
            TextField txtMotif = champ("Motif / Maladie");
            ComboBox<String> cbStatus = creerComboStatus();

            ajouterLigne(grid, "CIN",            txtCin,   0);
            ajouterLigne(grid, "Nom Complet",    txtNom,   1);
            ajouterLigne(grid, "Telephone",      txtTel,   2);
            ajouterLigne(grid, "Date Naissance", dp,       3);
            ajouterLigne(grid, "Motif/Maladie",  txtMotif, 4);
            ajouterLigne(grid, "Statut",         cbStatus, 5);

            ButtonType btnSave = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(btnSave, ButtonType.CANCEL);
            styleButtons(dialog, btnSave);
            dialog.getDialogPane().setContent(grid);

            Optional<ButtonType> res = dialog.showAndWait();
            if (res.isPresent() && res.get() == btnSave) {
                if (txtCin.getText().trim().isEmpty() || txtNom.getText().trim().isEmpty()) {
                    afficherErreur("Le CIN et le nom sont obligatoires.");
                    return false;
                }
                Patient p = new Patient();
                p.setCin(txtCin.getText().trim());
                p.setNomPatient(txtNom.getText().trim());
                p.setTelephone(txtTel.getText().trim());
                p.setMaladie(txtMotif.getText().trim());
                p.setStatus(cbStatus.getValue() != null ? cbStatus.getValue() : ST_ATTENTE);
                if (dp.getValue() != null)
                    p.setDateNaissance(java.sql.Date.valueOf(dp.getValue()));
                ajouter(p);
                return true;
            }
        } catch (Exception e) {
            afficherErreur("Impossible d'ajouter : " + e.getMessage());
        }
        return false;
    }

    // =======================================================================
    //  FORMULAIRE : MODIFIER
    // =======================================================================

    public boolean afficherFormulaireModifier(Patient p) {
        if (p == null) {
            afficherErreur("Veuillez selectionner un patient dans le tableau.");
            return false;
        }
        try {
            Dialog<ButtonType> dialog = creerDialog("Modifier — " + p.getNomPatient());
            GridPane grid = creerGrille();

            TextField txtNom   = champ("");
            TextField txtTel   = champ("");
            TextField txtMotif = champ("");

            txtNom.setText(p.getNomPatient() != null ? p.getNomPatient() : "");
            txtTel.setText(p.getTelephone() != null ? p.getTelephone() : "");
            txtMotif.setText(p.getMaladie() != null ? p.getMaladie() : "");

            DatePicker dp = creerDatePicker();
            if (p.getDateNaissance() != null) {
                try {
                    dp.setValue(p.getDateNaissance().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate());
                } catch (Exception ex) {
                    dp.setValue(LocalDate.now());
                }
            }

            ComboBox<String> cbStatus = creerComboStatus();
            // Normalisation du statut avant setValue
            String statusActuel = normaliserStatus(p.getStatus());
            cbStatus.setValue(statusActuel);

            ajouterLigne(grid, "Nom Complet",    txtNom,   0);
            ajouterLigne(grid, "Telephone",      txtTel,   1);
            ajouterLigne(grid, "Date Naissance", dp,       2);
            ajouterLigne(grid, "Motif/Maladie",  txtMotif, 3);
            ajouterLigne(grid, "Statut",         cbStatus, 4);

            ButtonType btnSave = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(btnSave, ButtonType.CANCEL);
            styleButtons(dialog, btnSave);
            dialog.getDialogPane().setContent(grid);

            Optional<ButtonType> res = dialog.showAndWait();
            if (res.isPresent() && res.get() == btnSave) {
                p.setNomPatient(txtNom.getText().trim());
                p.setTelephone(txtTel.getText().trim());
                p.setMaladie(txtMotif.getText().trim());
                p.setStatus(cbStatus.getValue() != null ? cbStatus.getValue() : ST_ATTENTE);
                if (dp.getValue() != null)
                    p.setDateNaissance(java.sql.Date.valueOf(dp.getValue()));
                modifier(p);
                return true;
            }
        } catch (Exception e) {
            afficherErreur("Impossible de modifier : " + e.getMessage());
        }
        return false;
    }

    // =======================================================================
    //  FORMULAIRE : CHERCHER
    // =======================================================================

    public void afficherFormulaireChercher() {
        TextField txtCin = champ("Saisir le CIN du patient");
        VBox contenu = new VBox(10, labelStyle("Entrez le CIN a rechercher :"), txtCin);
        contenu.setPadding(new Insets(20));
        contenu.setStyle("-fx-background-color:" + DARK_BG + ";");

        Dialog<ButtonType> dialog = creerDialog("Rechercher un Patient");
        ButtonType btnChercher = new ButtonType("Rechercher", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnChercher, ButtonType.CANCEL);
        styleButtons(dialog, btnChercher);
        dialog.getDialogPane().setContent(contenu);

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == btnChercher && !txtCin.getText().trim().isEmpty()) {
            Patient trouve = trouverParCin(txtCin.getText().trim());
            if (trouve != null) afficherFiche(trouve);
            else afficherErreur("Aucun patient avec le CIN : " + txtCin.getText().trim());
        }
    }

    // =======================================================================
    //  FORMULAIRE : ORDONNANCE (MEDECIN)
    // =======================================================================

    public boolean afficherFormulaireOrdonnance(Patient p) {
        if (p == null) {
            afficherErreur("Veuillez selectionner un patient dans le tableau.");
            return false;
        }
        // Passage automatique en "En cours" a l'ouverture du dossier
        if (ST_ATTENTE.equals(p.getStatus())) {
            try {
                p.setStatus(ST_ENCOURS);
                modifier(p);
            } catch (Exception e) {
                afficherErreur("Erreur mise a jour statut : " + e.getMessage());
            }
        }

        try {
            Dialog<ButtonType> dialog = creerDialog("Dossier Medical — " + p.getNomPatient());
            GridPane grid = creerGrille();

            ajouterLigne(grid, "CIN",            labelValeur(p.getCin()), 0);
            ajouterLigne(grid, "Nom Complet",    labelValeur(p.getNomPatient()), 1);
            ajouterLigne(grid, "Date Naissance", labelValeur(
                p.getDateNaissance() != null ? p.getDateNaissance().toString() : "--"), 2);
            ajouterLigne(grid, "Maladie/Motif",  labelValeur(
                p.getMaladie() != null ? p.getMaladie() : "--"), 3);

            TextArea txtOrdo = new TextArea(p.getOrdonnance() != null ? p.getOrdonnance() : "");
            txtOrdo.setPromptText("Rediger l'ordonnance ici...");
            txtOrdo.setPrefRowCount(4);
            txtOrdo.setWrapText(true);
            txtOrdo.setStyle(INPUT_STYLE + "-fx-font-size:13;");

            ComboBox<String> cbStatus = creerComboStatus();
            cbStatus.setValue(normaliserStatus(p.getStatus()));

            ajouterLigne(grid, "Ordonnance", txtOrdo,  4);
            ajouterLigne(grid, "Statut",     cbStatus, 5);

            ButtonType btnSave = new ButtonType("Sauvegarder", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(btnSave, ButtonType.CANCEL);
            styleButtons(dialog, btnSave);
            dialog.getDialogPane().setContent(grid);

            Optional<ButtonType> res = dialog.showAndWait();
            if (res.isPresent() && res.get() == btnSave) {
                p.setOrdonnance(txtOrdo.getText().trim());
                p.setStatus(cbStatus.getValue() != null ? cbStatus.getValue() : ST_ENCOURS);
                modifier(p);
                return true;
            }
        } catch (Exception e) {
            afficherErreur("Impossible de sauvegarder : " + e.getMessage());
        }
        return false;
    }

    // =======================================================================
    //  HELPERS PRIVES
    // =======================================================================

    /**
     * Normalise le statut venant de la BDD vers les valeurs exactes de la liste.
     * Gere les accents, majuscules, espaces differents.
     */
    private String normaliserStatus(String status) {
        if (status == null) return ST_ATTENTE;
        String s = status.trim().toLowerCase();
        if (s.contains("cours")) return ST_ENCOURS;
        if (s.contains("termin")) return ST_TERMINE;
        return ST_ATTENTE;
    }

    private void afficherFiche(Patient p) {
        Dialog<ButtonType> dialog = creerDialog("Dossier Patient");
        GridPane grid = creerGrille();
        ajouterLigne(grid, "CIN",            labelValeur(p.getCin()), 0);
        ajouterLigne(grid, "Nom",            labelValeur(p.getNomPatient()), 1);
        ajouterLigne(grid, "Telephone",      labelValeur(p.getTelephone() != null ? p.getTelephone() : "--"), 2);
        ajouterLigne(grid, "Date Naissance", labelValeur(p.getDateNaissance() != null ? p.getDateNaissance().toString() : "--"), 3);
        ajouterLigne(grid, "Maladie/Motif",  labelValeur(p.getMaladie() != null ? p.getMaladie() : "--"), 4);
        ajouterLigne(grid, "Statut",         labelValeur(p.getStatus() != null ? p.getStatus() : "--"), 5);
        ajouterLigne(grid, "Ordonnance",     labelValeur(p.getOrdonnance() != null ? p.getOrdonnance() : "--"), 6);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        styleButtons(dialog, ButtonType.OK);
        dialog.getDialogPane().setContent(grid);
        dialog.showAndWait();
    }

    private Dialog<ButtonType> creerDialog(String titre) {
        Dialog<ButtonType> d = new Dialog<ButtonType>();
        d.setTitle(titre);
        d.setHeaderText(null);
        d.getDialogPane().setStyle("-fx-background-color:" + DARK_BG
            + "; -fx-border-color:#00b4d8; -fx-border-width:1;");
        return d;
    }

    private GridPane creerGrille() {
        GridPane g = new GridPane();
        g.setHgap(14);
        g.setVgap(12);
        g.setPadding(new Insets(20, 30, 20, 20));
        g.setStyle("-fx-background-color:" + DARK_BG + ";");
        return g;
    }

    private TextField champ(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle(INPUT_STYLE);
        tf.setPrefWidth(240);
        return tf;
    }

    private DatePicker creerDatePicker() {
        DatePicker dp = new DatePicker(LocalDate.now());
        dp.setStyle(INPUT_STYLE);
        dp.setPrefWidth(240);
        return dp;
    }

    private ComboBox<String> creerComboStatus() {
        ComboBox<String> cb = new ComboBox<String>();
        cb.getItems().add(ST_ATTENTE);
        cb.getItems().add(ST_ENCOURS);
        cb.getItems().add(ST_TERMINE);
        cb.setValue(ST_ATTENTE);
        cb.setStyle(INPUT_STYLE + "-fx-min-width:240;");
        return cb;
    }

    private Label labelStyle(String texte) {
        Label l = new Label(texte);
        l.setStyle(LABEL_STYLE);
        return l;
    }

    private Label labelValeur(String texte) {
        Label l = new Label(texte);
        l.setStyle("-fx-text-fill:#caf0f8; -fx-font-size:13; -fx-font-weight:bold;");
        l.setPrefWidth(240);
        return l;
    }

    private void ajouterLigne(GridPane grid, String libelle, javafx.scene.Node champ, int ligne) {
        grid.add(labelStyle(libelle), 0, ligne);
        grid.add(champ, 1, ligne);
    }

    private void styleButtons(Dialog<?> dialog, ButtonType principal) {
        dialog.getDialogPane().lookupButton(principal).setStyle(BTN_OK);
        for (ButtonType bt : dialog.getDialogPane().getButtonTypes()) {
            if (!bt.equals(principal)) {
                dialog.getDialogPane().lookupButton(bt).setStyle(BTN_CANCEL);
            }
        }
    }

    private void afficherErreur(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.getDialogPane().setStyle("-fx-background-color:" + DARK_BG
            + "; -fx-border-color:#ef233c;");
        alert.showAndWait();
    }
}