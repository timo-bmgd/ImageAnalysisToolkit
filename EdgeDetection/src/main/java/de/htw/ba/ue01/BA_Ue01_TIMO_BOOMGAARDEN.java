/**
 * @author Nico Hezel
 */
package de.htw.ba.ue01;

import java.net.URL;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class BA_Ue01_TIMO_BOOMGAARDEN {

	public static void main(String[] args) {
		Application.launch(FXApplication.class);
	}
	
	/**
	 * We need a separate class in order to trick Java 11 to start our JavaFX application without any module-path settings.
	 * https://stackoverflow.com/questions/52144931/how-to-add-javafx-runtime-to-eclipse-in-java-11/55300492#55300492
	 * 
	 * @author Nico Hezel
	 *
	 */
	public static class FXApplication extends Application {
		@Override
		public void start(Stage stage) throws Exception {
			URL res = getClass().getResource("/de/htw/ba/ue01/view/EdgeDetectionView.fxml");
			Parent ui = new FXMLLoader(res).load();
			Scene scene = new Scene(ui);
			stage.setScene(scene);
			stage.setTitle("Kantendetektion - TIMO BOOMGAARDEN");
			stage.setOnCloseRequest((WindowEvent event) -> { Platform.exit(); });
			stage.show();
		}
	}
}