package hopital;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.hibernate.SessionFactory;

/**
 * Controleur principal de l'application.
 * Roles : SECRETAIRE (CRUD complet) et MEDECIN (CRUD + ordonnances).
 * Connexion par CIN uniquement — le profil en base determine l'espace affiche.
 */
public class FXMLDocumentController implements Initializable {

    // -- Connexion ----------------------------------------------------------
    @FXML private AnchorPane paneConnexion;
    @FXML private TextField  txtCin;
    @FXML private Label      lblLoginErreur;

    // -- Panneaux -----------------------------------------------------------
    @FXML private AnchorPane paneSecretaire;
    @FXML private AnchorPane paneMedecin;

    // -- Secretaire : en-tete & compteur ------------------------------------
    @FXML private Label lblNomSecretaire;
    @FXML private Label lblNbPatients;

    // -- Secretaire : tableau -----------------------------------------------
    @FXML private TableView<Patient>           tblSecretaire;
    @FXML private TableColumn<Patient, String> secColCin;
    @FXML private TableColumn<Patient, String> secColNom;
    @FXML private TableColumn<Patient, String> secColTel;
    @FXML private TableColumn<Patient, Object> secColDateNaiss;
    @FXML private TableColumn<Patient, String> secColMotif;
    @FXML private TableColumn<Patient, String> secColStatus;

    // -- Medecin : en-tete --------------------------------------------------
    @FXML private Label lblNomMedecin;

    // -- Medecin : tableau --------------------------------------------------
    @FXML private TableView<Patient>           tblMedecin;
    @FXML private TableColumn<Patient, String> medColCin;
    @FXML private TableColumn<Patient, String> medColNom;
    @FXML private TableColumn<Patient, Object> medColDateNaiss;
    @FXML private TableColumn<Patient, String> medColMaladie;
    @FXML private TableColumn<Patient, String> medColOrdonnance;
    @FXML private TableColumn<Patient, String> medColStatus;

    // -- Hibernate & etat global --------------------------------------------
    private SessionFactory  sessionFactory;
    private PatientCRUD     crud;
    private Utilisateur     utilisateurConnecte;

    // =======================================================================
    //  INITIALISATION
    // =======================================================================

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            Configuration config = new Configuration();
            config.setProperty("hibernate.connection.driver_class",
                "com.mysql.cj.jdbc.Driver");
            config.setProperty("hibernate.connection.url",
                "jdbc:mysql://localhost:3306/hopital_db"
                + "?zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=UTC");
            config.setProperty("hibernate.connection.username", "root");
            config.setProperty("hibernate.connection.password", "");
            config.setProperty("hibernate.dialect",
                "org.hibernate.dialect.MySQL5Dialect");
            config.setProperty("hibernate.show_sql",      "true");
            config.setProperty("hibernate.hbm2ddl.auto",  "update");

            config.addAnnotatedClass(Utilisateur.class);
            config.addAnnotatedClass(Patient.class);
            config.addAnnotatedClass(Consultation.class);
            config.addAnnotatedClass(Ordonnance.class);

            sessionFactory = config.buildSessionFactory();
            crud = new PatientCRUD(sessionFactory);

            paneConnexion.setVisible(true);
            paneSecretaire.setVisible(false);
            paneMedecin.setVisible(false);

        } catch (Exception e) {
            System.err.println("Hibernate ne demarre pas : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =======================================================================
    //  CONNEXION / DECONNEXION
    // =======================================================================

    @FXML
    private void handleConnexion(ActionEvent event) {
        String cin = txtCin.getText().trim();
        lblLoginErreur.setVisible(false);

        if (cin.isEmpty()) {
            afficherErreurLogin("Veuillez saisir votre CIN.");
            return;
        }
        if (sessionFactory == null) {
            afficherErreurLogin("Base de donnees inaccessible. Verifiez que MySQL est lance.");
            return;
        }

        Session session = sessionFactory.openSession();
        try {
            Utilisateur user = (Utilisateur) session.get(Utilisateur.class, cin);
            if (user == null) {
                afficherErreurLogin("CIN non reconnu. Contactez l'administrateur.");
                return;
            }
            utilisateurConnecte = user;

            if ("SECRETAIRE".equalsIgnoreCase(user.getProfil())) {
                ouvrirEspaceSecretaire(user);
            } else if ("MEDECIN".equalsIgnoreCase(user.getProfil())) {
                ouvrirEspaceMedecin(user);
            } else {
                afficherErreurLogin("Profil inconnu : " + user.getProfil());
            }
        } catch (Exception e) {
            afficherErreurLogin("Erreur base de donnees : " + e.getMessage());
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    @FXML
    public void handleDeconnexion(ActionEvent event) {
        utilisateurConnecte = null;
        paneSecretaire.setVisible(false);
        paneMedecin.setVisible(false);
        paneConnexion.setVisible(true);
        txtCin.clear();
        lblLoginErreur.setVisible(false);
    }

    private void ouvrirEspaceSecretaire(Utilisateur user) {
        lblNomSecretaire.setText(user.getNomComplet());
        paneConnexion.setVisible(false);
        paneSecretaire.setVisible(true);
        rafraichirTableauSecretaire();
    }

    private void ouvrirEspaceMedecin(Utilisateur user) {
        lblNomMedecin.setText(user.getNomComplet());
        paneConnexion.setVisible(false);
        paneMedecin.setVisible(true);
        rafraichirTableauMedecin();
    }

    // =======================================================================
    //  SECRETAIRE — CRUD
    // =======================================================================

    @FXML
    private void handleAjouterPatient(ActionEvent event) {
        if (crud.afficherFormulaireAjout()) {
            rafraichirTableauSecretaire();
        }
    }

    @FXML
    private void handleModifierPatient(ActionEvent event) {
        Patient sel = tblSecretaire.getSelectionModel().getSelectedItem();
        if (crud.afficherFormulaireModifier(sel)) {
            rafraichirTableauSecretaire();
        }
    }

    @FXML
    private void handleSupprimerPatient(ActionEvent event) {
        Patient sel = tblSecretaire.getSelectionModel().getSelectedItem();
        if (sel == null) {
            afficherInfo("Selection requise", "Selectionnez un patient dans le tableau.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer " + sel.getNomPatient() + " ? Cette action est irreversible.");
        confirm.getDialogPane().setStyle("-fx-background-color:#1a1a2e;");
        confirm.showAndWait().ifPresent(rep -> {
            if (rep == ButtonType.OK) {
                try {
                    crud.supprimer(sel);
                    rafraichirTableauSecretaire();
                } catch (Exception e) {
                    afficherInfo("Erreur", "Suppression impossible : " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleChercherPatient(ActionEvent event) {
        crud.afficherFormulaireChercher();
    }

    @FXML
    private void handleActualiserSec(ActionEvent event) {
        rafraichirTableauSecretaire();
    }

    // =======================================================================
    //  MEDECIN — CRUD
    // =======================================================================

    @FXML
    private void handleOuvrirDossier(ActionEvent event) {
        Patient sel = tblMedecin.getSelectionModel().getSelectedItem();
        if (crud.afficherFormulaireOrdonnance(sel)) {
            rafraichirTableauMedecin();
        }
    }

    @FXML
    private void handleMedAjouter(ActionEvent event) {
        if (crud.afficherFormulaireAjout()) {
            rafraichirTableauMedecin();
        }
    }

    @FXML
    private void handleMedModifier(ActionEvent event) {
        Patient sel = tblMedecin.getSelectionModel().getSelectedItem();
        if (crud.afficherFormulaireModifier(sel)) {
            rafraichirTableauMedecin();
        }
    }

    @FXML
    private void handleMedSupprimer(ActionEvent event) {
        Patient sel = tblMedecin.getSelectionModel().getSelectedItem();
        if (sel == null) {
            afficherInfo("Selection requise", "Selectionnez un patient dans le tableau.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer " + sel.getNomPatient() + " ?");
        confirm.getDialogPane().setStyle("-fx-background-color:#1a1a2e;");
        confirm.showAndWait().ifPresent(rep -> {
            if (rep == ButtonType.OK) {
                try {
                    crud.supprimer(sel);
                    rafraichirTableauMedecin();
                } catch (Exception e) {
                    afficherInfo("Erreur", "Suppression impossible : " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleMedChercher(ActionEvent event) {
        crud.afficherFormulaireChercher();
    }

    @FXML
    private void handleActualiserMed(ActionEvent event) {
        rafraichirTableauMedecin();
    }

    // =======================================================================
    //  RAFRAICHISSEMENT DES TABLEAUX
    // =======================================================================

    private void rafraichirTableauSecretaire() {
        secColCin.setCellValueFactory(new PropertyValueFactory<Patient, String>("cin"));
        secColNom.setCellValueFactory(new PropertyValueFactory<Patient, String>("nomPatient"));
        secColTel.setCellValueFactory(new PropertyValueFactory<Patient, String>("telephone"));
        secColDateNaiss.setCellValueFactory(new PropertyValueFactory<Patient, Object>("dateNaissance"));
        secColMotif.setCellValueFactory(new PropertyValueFactory<Patient, String>("maladie"));
        secColStatus.setCellValueFactory(new PropertyValueFactory<Patient, String>("status"));

        secColStatus.setCellFactory(new javafx.util.Callback<TableColumn<Patient,String>, TableCell<Patient,String>>() {
            @Override
            public TableCell<Patient,String> call(TableColumn<Patient,String> col) {
                return new TableCell<Patient,String>() {
                    @Override
                    protected void updateItem(String status, boolean empty) {
                        super.updateItem(status, empty);
                        if (empty || status == null) { setText(null); setStyle(""); return; }
                        setText(status);
                        if ("En attente".equals(status))      setStyle("-fx-text-fill:#ffd60a; -fx-font-weight:bold;");
                        else if ("En cours".equals(status))   setStyle("-fx-text-fill:#00b4d8; -fx-font-weight:bold;");
                        else if ("Termine".equals(status))    setStyle("-fx-text-fill:#38b000; -fx-font-weight:bold;");
                        else                                   setStyle("-fx-text-fill:#e0e0e0;");
                    }
                };
            }
        });

        List<Patient> liste = crud.listerTout();
        tblSecretaire.setItems(FXCollections.observableArrayList(liste));
        if (lblNbPatients != null) lblNbPatients.setText(String.valueOf(liste.size()));
    }

    private void rafraichirTableauMedecin() {
        medColCin.setCellValueFactory(new PropertyValueFactory<Patient, String>("cin"));
        medColNom.setCellValueFactory(new PropertyValueFactory<Patient, String>("nomPatient"));
        medColDateNaiss.setCellValueFactory(new PropertyValueFactory<Patient, Object>("dateNaissance"));
        medColMaladie.setCellValueFactory(new PropertyValueFactory<Patient, String>("maladie"));
        medColOrdonnance.setCellValueFactory(new PropertyValueFactory<Patient, String>("ordonnance"));
        medColStatus.setCellValueFactory(new PropertyValueFactory<Patient, String>("status"));

        medColStatus.setCellFactory(new javafx.util.Callback<TableColumn<Patient,String>, TableCell<Patient,String>>() {
            @Override
            public TableCell<Patient,String> call(TableColumn<Patient,String> col) {
                return new TableCell<Patient,String>() {
                    @Override
                    protected void updateItem(String status, boolean empty) {
                        super.updateItem(status, empty);
                        if (empty || status == null) { setText(null); setStyle(""); return; }
                        setText(status);
                        if ("En attente".equals(status))      setStyle("-fx-text-fill:#ffd60a; -fx-font-weight:bold;");
                        else if ("En cours".equals(status))   setStyle("-fx-text-fill:#00b4d8; -fx-font-weight:bold;");
                        else if ("Termine".equals(status))    setStyle("-fx-text-fill:#38b000; -fx-font-weight:bold;");
                        else                                   setStyle("-fx-text-fill:#e0e0e0;");
                    }
                };
            }
        });

        tblMedecin.setItems(FXCollections.observableArrayList(crud.listerTout()));
    }

    // =======================================================================
    //  UTILITAIRES
    // =======================================================================

    private void afficherErreurLogin(String message) {
        lblLoginErreur.setText(message);
        lblLoginErreur.setVisible(true);
    }

    private void afficherInfo(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setStyle("-fx-background-color:#1a1a2e;");
        alert.showAndWait();
    }
}