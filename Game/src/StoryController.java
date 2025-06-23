import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;


import java.io.IOException;

public class StoryController {
    private Parent root;

    @FXML
    void continueButtonPressed(ActionEvent event) {
        try {
            // Load the next FXML file
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("rules1.fxml"));
            Parent rules1Parent = fxmlLoader.load();
            ImageView imageView = (ImageView) rules1Parent.lookup("#rulesImage");

            // Bind the fitWidth and fitHeight properties to the scene's width and height
            imageView.fitWidthProperty().bind(root.getScene().widthProperty());
            imageView.fitHeightProperty().bind(root.getScene().heightProperty());

            // Set the controller associated with the FXML file
            Rules1Controller rules1Controller = fxmlLoader.getController();
            rules1Controller.setRoot(rules1Parent);
            root.getScene().setRoot(rules1Parent);

        } catch (IOException e) {
            System.err.println("Error continuing flow: " + e.getMessage());
        }
    }

    public void setRoot(Parent root) {
        this.root = root;
    }
}
