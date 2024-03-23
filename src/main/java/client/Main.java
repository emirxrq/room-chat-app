package client;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class Main extends Application {
	@Override
	public void start(Stage stage) throws Exception
	{
		Parent root = FXMLLoader.load(getClass().getResource("scenes/Main.fxml"));
		stage.setScene(new Scene(root));
		stage.setOnCloseRequest(event -> {
	            event.consume(); 

	            System.exit(0);
	        });
		stage.show();
        
		
	}
	public static void main(String[] args) {
		launch(args);
	}

}
