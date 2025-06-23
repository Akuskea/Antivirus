import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;
import javafx.scene.image.ImageView;

public class StartGameController {
    private Parent root;
    @FXML
    void startButtonPressed(ActionEvent event) {
        try {
            // Load the next FXML file
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("story.fxml"));
            Parent storyParent = fxmlLoader.load();
            ImageView imageView = (ImageView) storyParent.lookup("#storyImage");


            // Bind the fitWidth and fitHeight properties to the scene's width and height
            imageView.fitWidthProperty().bind(root.getScene().widthProperty());
            imageView.fitHeightProperty().bind(root.getScene().heightProperty());

            // Set the controller associated with the FXML file
            StoryController storyController = fxmlLoader.getController();
            storyController.setRoot(storyParent);
            root.getScene().setRoot(storyParent);


        } catch (IOException e) {
            System.out.println("Error starting game: " + e);
        }
    }
    public void setRoot(Parent root) {
        this.root = root;
    }
}
