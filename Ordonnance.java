package hopital;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Hopital extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        // Chargement du fichier FXML central
        Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));
        
        Scene scene = new Scene(root);
        
        // Titre de la fenêtre de notre application
        stage.setTitle("Système de Gestion Hospitalière - NetBeans & Hibernate");
        stage.setScene(scene);
        stage.setResizable(false); // Empêche de déformer l'interface
        stage.show();
        // Dans ta méthode start de Hopital.java :
    }

    public static void main(String[] args) {
        
        
        launch(args);
    }
}
