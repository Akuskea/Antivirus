import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;
import javafx.scene.image.ImageView;

public class LoginController {

    @FXML
    private PasswordField password;

    @FXML
    private TextField username;

    @FXML
    private Text errorText;

    private Parent root;
    private  String playerName;
    public  String getPlayerName(){
        return playerName;
    }
    public void setPlayerName(String playerName){
        this.playerName = playerName;
    }

    @FXML
    private void initialize() {
        errorText.setVisible(false);
    }

    @FXML
    private void handleLoginButtonAction(ActionEvent event) {
        try {
            String enteredUsername = username.getText();
            setPlayerName(enteredUsername);
            String enteredPassword = password.getText();


            if (isValidInput(enteredUsername,enteredPassword) ) {

                // Assuming you have a method to find or create a room
                int roomId = DatabaseHandler.findOrCreateRoom(enteredUsername);
                if (roomId != -1) {
                    try {
                        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("startGame.fxml"));
                        Parent startGameParent = fxmlLoader.load();
                        ImageView imageView = (ImageView) startGameParent.lookup("#imagefirst");


                        // Bind the fitWidth and fitHeight properties to the scene's width and height
                        imageView.fitWidthProperty().bind(root.getScene().widthProperty());
                        imageView.fitHeightProperty().bind(root.getScene().heightProperty());
                        // Set the controller associated with the FXML file
                        StartGameController startController = fxmlLoader.getController();
                        startController.setRoot(startGameParent);

                        root.getScene().setRoot(startGameParent);

                    } catch (IOException e) {
                        System.out.println("Error logging player in: " + e);
                    }
                }
            } else {
                errorText.setText("Invalid email or password");
                errorText.setVisible(true);
            }
        } catch (NumberFormatException e) {
            //Other exception
            errorText.setText("Invalid email or password");
            errorText.setVisible(true);
        }
    }

    @FXML
    private void handleSignUpLinkAction(ActionEvent event) {
        // Load the sign-up page and pass the stored username and password
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("signup.fxml"));
            Parent signUpParent = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(signUpParent));
            stage.setTitle("Sign Up");
            stage.show();
        } catch (IOException e) {
            System.out.println("Error presenting sign-up view: " + e);
        }
    }
    public void setRoot(Parent root) {
        this.root = root;
    }
    private boolean isValidInput(String email, String password) {
        return DatabaseHandler.isValidUser(email, password);
    }

}

