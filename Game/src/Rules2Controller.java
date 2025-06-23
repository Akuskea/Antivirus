import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.scene.Parent;

public class Rules2Controller {
    private Parent root;
    private MultiUserClient gameRender;

    @FXML
    private ImageView rulesImage;

    @FXML
    void continueButtonPressed(ActionEvent event) {
        // Create an instance of the MultiUserClient class
        gameRender = new MultiUserClient();

        // Pass the current stage to the MultiUserClient
        Stage stage = (Stage) root.getScene().getWindow();
        gameRender.start(stage);
        root = null;


    }
    public void setRoot(Parent root) {
        this.root = root;
    }
}


