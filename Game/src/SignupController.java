import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.Node;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SignupController {

    @FXML
    private PasswordField passwordSignUp;

    @FXML
    private TextField email;

    // Database connection details
    private static final String DB_URL = "jdbc:mysql://s-l112.engr.uiowa.edu:3306/engr_class013";
    private static final String DB_USER = "engr_class013";
    private static final String DB_PASSWORD = "engr_class013-xyz";

    @FXML
    private void signUpButtonAction(ActionEvent event) {
        try {
            // Perform sign-up logic with the stored email and password values
            String enteredEmail = email.getText();
            String enteredPassword = passwordSignUp.getText();

            // Validate the email format
            if (isValidEmail(enteredEmail)) {
                storeUserInDatabase(enteredEmail, enteredPassword);
                // Close the signup window after successful signup
                Node source = (Node) event.getSource();
                javafx.stage.Stage stage = (javafx.stage.Stage) source.getScene().getWindow();
                stage.close();
            } else {
                // Display an error message for an invalid email
                email.setText("Invalid email");
                email.selectAll();
                email.requestFocus();

            }
        } catch (NumberFormatException e) {
            //Other exception
            email.setText("Invalid email");
        }
    }

    private boolean isValidEmail(String email) {
        // Basic email validation using a regular expression
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private void storeUserInDatabase(String email, String password) {
        try {
            // Establish a database connection
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            // Insert user information into the database
            String insertQuery = "INSERT INTO users (email, password) VALUES (?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                preparedStatement.setString(1, email);
                preparedStatement.setString(2, password);
                preparedStatement.executeUpdate();
            }

            // Close the database connection
            connection.close();
        } catch (SQLException e) {
            // Handle database-related exceptions
            System.out.println("Error storing user in database: " + e);
        }
    }
}

