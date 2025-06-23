import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;
import javafx.scene.image.ImageView;


public class Rules1Controller {
    private Parent root;
    @FXML
    private ImageView rulesImage;
    @FXML
    void continueButtonPressed(ActionEvent event) {
        try {
            // Load the next FXML file
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("rules2.fxml"));
            Parent rules2Parent = fxmlLoader.load();

            // fx:id="firstImage" in startGame.fxml
            ImageView imageView = (ImageView) rules2Parent.lookup("#rulesImage");

            // Bind the fitWidth and fitHeight properties to the scene's width and height
            imageView.fitWidthProperty().bind(root.getScene().widthProperty());
            imageView.fitHeightProperty().bind(root.getScene().heightProperty());

            // Set the controller associated with the FXML file
            Rules2Controller rules2Controller = fxmlLoader.getController();
            rules2Controller.setRoot(rules2Parent);
            root.getScene().setRoot(rules2Parent);

        } catch (IOException e) {
            System.out.println("Error continuing flow: " + e);
        }
    }
    public void setRoot(Parent root) {
        this.root = root;
    }

}
