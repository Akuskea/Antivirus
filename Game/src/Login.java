import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;


public class Login extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the login scene
        FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
        Parent root = loader.load();

        // Set the controller associated with the FXML file
        LoginController loginController = loader.getController();
        loginController.setRoot(root);

        // Create a scene with the root layout
        Scene scene = new Scene(root, 800, 600);

        // Set the scene to the primary stage
        primaryStage.setScene(scene);

        // Get the primary screen
        Screen screen = Screen.getPrimary();

        // Get the visual bounds of the primary screen
        Rectangle2D bounds = screen.getVisualBounds();

        // Set the stage dimensions to the screen dimensions
        primaryStage.setX(bounds.getMinX());
        primaryStage.setY(bounds.getMinY());
        primaryStage.setWidth(bounds.getWidth());
        primaryStage.setHeight(bounds.getHeight());

        // Show the stage
        primaryStage.show();



        // Set the event handler for the window close event
        primaryStage.setOnCloseRequest(event -> {
            String playerName = loginController.getPlayerName();
            int roomId = DatabaseHandler.findOrCreateRoom(playerName);
            // disconnect the player and update the room
            DatabaseHandler.disconnectPlayerAndUpdateRoom(playerName, roomId);
        });


    }
    public static void main(String[] args) {
        launch(args);
    }
}
