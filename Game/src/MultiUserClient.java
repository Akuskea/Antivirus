import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;
import java.io.IOException;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MultiUserClient extends Application {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8081;
    private GridPane gridPane;
    private List<String> availableCharacterImages;
    private Client client;
    private boolean isRunning = true;
    private volatile boolean gamePaused = false;
    private final Object pauseLock = new Object();
    private Timeline pauseTimeline;

    private TextArea chatArea;
    private static List<Player> players = new ArrayList<>();
    private Player player;


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);

        staticBackground(MultiUserServer.gameWorld);
        Scene scene = new Scene(gridPane, 800, 600);

        scene.setOnKeyPressed(event -> handleKeyPress(event.getCode()));
        availableCharacterImages = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            availableCharacterImages.add("Charact_pic/Charact" + i + ".jpg");
        }

        primaryStage.setTitle("Multi-User Game Client");
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> {
            if (client != null) {
                client.disconnect();
            }
            isRunning = false;
            Platform.exit(); // Close the JavaFX application
        });
        client = createClient();
        player = MultiUserServer.getPlayer();
        players = MultiUserServer.getPlayerList();
        startDeathCheckTimer();
        initializePauseTimeline();
        new Thread(() -> {
            // Before the loop where you process received messages
            StringBuilder accumulatedMessage = new StringBuilder();
            while (isRunning) {
                if (!gamePaused && client != null) {
                    try {
                        // Inside the loop that processes received messages in MultiUserClient class
                        String receivedMessage = client.receiveMessage();

                        if (receivedMessage == null) {
                            // Handle the case where the server connection is closed
                            break;
                        }
                        accumulatedMessage.append(receivedMessage);
                        accumulatedMessage.append("\n");

                        // Check if the received message contains a new line
                        if (receivedMessage.equals("") ) {
                            // Process the accumulated message (e.g., update the UI)
                            System.out.println("Received from server: " + accumulatedMessage.toString());

                            String fullMessage = accumulatedMessage.toString();
                            if (fullMessage.startsWith("GAME_STATE")) {
                                // Extract the game state string from the accumulated message
                                String gameStateString = accumulatedMessage.toString().substring("GAME_STATE".length()).trim();

                                // Convert the received matrix string to a 2D int matrix
                                int[][] receivedMatrix = convertStringToMatrix(gameStateString);
                                MultiUserServer.gameWorld = receivedMatrix;
                                Platform.runLater(() -> updateCharacters(MultiUserServer.gameWorld));
                            }

                            accumulatedMessage.setLength(0);
                        } else if (receivedMessage.startsWith("ROLE")){
                            String role = accumulatedMessage.toString().substring("ROLE".length()).trim();
                            System.out.println(role);
                            if(role.equals("Police")) {
                                showImageForSecondsWithPause("Role_pic/IPolice.png", 2);
                            } else if(role.equals("Spy")) {
                                showImageForSecondsWithPause("Role_pic/Spy.png", 2);
                            }else if(role.equals("Reporter")) {
                                showImageForSecondsWithPause("Role_pic/Reporter.png", 2);
                            }else if(role.equals("Immune")) {
                                showImageForSecondsWithPause("Role_pic/Immune.png", 2);
                            }else if(role.equals("Civilian")) {
                                showImageForSecondsWithPause("Role_pic/Civilian.png", 2);

                            } else if(role.equals("Killer")) {
                                showImageForSecondsWithPause("Role_pic/Killer.png", 5);
                            }
                            accumulatedMessage.setLength(0);

                        } else if (receivedMessage.startsWith("SURVEY")) {
                            client.sendMessage(receivedMessage);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        // Handle the exception (e.g., show an error message)
                    }

                } else {
                    // If the game is paused, wait until it's resumed
                    synchronized (pauseLock) {
                        try {
                            pauseLock.wait();
                        } catch (InterruptedException e) {
                            // Handle interruption if needed
                        }
                    }
                }
                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt(); // Preserve the interruption status
                }
            }

        }).start();
    }

    private void startDeathCheckTimer() {
        Timeline deathCheckTimeline = new Timeline(
                new KeyFrame(Duration.seconds(5), event -> playerDead())
        );
        deathCheckTimeline.setCycleCount(Timeline.INDEFINITE);
        deathCheckTimeline.play();
    }

    private void playerDead() {
        for (Player player : players) {
            if (player.getDead()) {
                gridPane.getChildren().clear();
                showImageForSecondsWithPause("End/Dead.png", 2);
                showImageForSecondsWithPause("End/GameOver.png", 999999999);
            }
        }
    }
    private void checkGameOutcome() {
        int livingResearcher = 0;
        int livingPolice = 0;
        int livingSpy = 0;
        for (Player player : players) {
            if (!player.getDead()) {
                 if (player.getRole().equals("Spy")) {
                    livingSpy++;
                }else if (player.getRole().equals("Police")) {
                    livingPolice++;
                }else if (player.getRole().equals("Researcher")) {
                    livingResearcher++;
                }
            }
        }

        if (livingSpy == 0) {
            for (Player player : players) {
                gridPane.getChildren().clear();
                if (!Objects.equals(player.getRole(), "Spy")) {
                    showImageForSecondsWithPause("End/WinSpy.png", 999999999);
                } else {
                    showImageForSecondsWithPause("End/GameOver.png", 999999999);
                }
            }

        }

        if (livingPolice == 0 && livingResearcher == 0) {
            for (Player player : players) {
                gridPane.getChildren().clear();
                if (Objects.equals(player.getRole(), "Spy")) {
                    showImageForSecondsWithPause("End/WinSpy.png", 999999999);
                } else {
                    showImageForSecondsWithPause("End/GameOver.png", 999999999);
                }
            }
        }
    }
    private void showImageForSecondsWithPause(String imagePath, int seconds) {
        try {
            // Pause the game world logic
            pauseGameWorld();

            // Create an ImageView and load the image dynamically
            ImageView imageView = new ImageView(new Image(imagePath));

            // Bind the fitWidth and fitHeight properties to the scene's width and height
            imageView.fitWidthProperty().bind(gridPane.getScene().widthProperty());
            imageView.fitHeightProperty().bind(gridPane.getScene().heightProperty());

            Platform.runLater(() -> {
                gridPane.getChildren().clear(); // Clear existing content
                gridPane.getChildren().add(imageView);
            });
            PauseTransition pause = new PauseTransition(Duration.seconds(seconds));
            pause.setOnFinished(event -> {
                // After specified seconds, remove the ImageView and resume the game world
                gridPane.getChildren().remove(imageView);
                resumeGameWorld();
            });
            pause.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializePauseTimeline() {
        // Create a timeline to pause the game every 3 minutes
        pauseTimeline = new Timeline(new KeyFrame(Duration.minutes(3), event -> {
            // Pause the game
            gamePaused = true;

            // Show a different paused image for 5 seconds (adjust the image path as needed)
            showImageForSecondsWithPause("Timeline/Night.png", 5);


            // Schedule a task to resume the game after the pause duration
            PauseTransition resumeTransition = new PauseTransition(Duration.seconds(5));
            resumeTransition.setOnFinished(e -> resumeGameWorld());
            resumeTransition.play();
        }));
        pauseTimeline.setCycleCount(Timeline.INDEFINITE);
        pauseTimeline.play();
    }
    private void pauseGameWorld() {
        synchronized (pauseLock) {
            gamePaused = true;
        }
    }

    private void resumeGameWorld() {
        Platform.runLater(() -> {
            gridPane.getChildren().clear(); // Clear any existing content

            // Add your logic to restore the initial state of the scene
            staticBackground(MultiUserServer.getMatrix());
            updateCharacters(MultiUserServer.gameWorld);
        });
        synchronized (pauseLock) {
            gamePaused = false;
            pauseLock.notifyAll(); // Notify waiting threads (if any) that the game is resumed
        }
    }

    private void staticBackground(int[][] gameWorld) {
        for (int i = 0; i < gameWorld.length; i++) {
            for (int j = 0; j < gameWorld[i].length; j++) {
                int cellValue = gameWorld[i][j];
                ImageView imageView;
                switch (cellValue) {
                    case 0:
                        imageView = new ImageView(new Image("Room_pic/floor.jpg"));
                        imageView.setFitWidth(32);
                        imageView.setFitHeight(32);
                        gridPane.add(imageView, j, i);
                        break;
                    case 13:
                        // Wall
                        imageView = new ImageView(new Image("Room_pic/wall.jpg"));
                        imageView.setFitWidth(32);
                        imageView.setFitHeight(32);
                        gridPane.add(imageView, j, i);
                        break;
                    case 14:
                        imageView = new ImageView(new Image("Room_pic/floor.jpg"));
                        imageView.setFitWidth(32);
                        imageView.setFitHeight(32);
                        gridPane.add(imageView, j, i);
                        break;
                    case 15:
                        imageView = new ImageView(new Image("Room_pic/floor.jpg"));
                        imageView.setFitWidth(32);
                        imageView.setFitHeight(32);
                        gridPane.add(imageView, j, i);
                        break;
                }
            }
        }
    }

    public void updateCharacters(int[][] gameWorld) {

        // Update the GUI based on the received game state
        gridPane.getChildren().removeIf(node -> node instanceof ImageView && isCharacterElement((ImageView) node));
        // Draw the game world
        for (int i = 0; i < gameWorld.length; i++) {
            for (int j = 0; j < gameWorld[i].length; j++) {
                int cellValue = gameWorld[i][j];
                if (cellValue >= 1 && cellValue <= 12) {
                    ImageView imageView;
                    String characterImagePath = availableCharacterImages.get(cellValue - 1);
                    imageView = new ImageView(new Image(characterImagePath));
                    imageView.setFitWidth(32);
                    imageView.setFitHeight(32);
                    gridPane.add(imageView, j, i);
                }

            }
        }
    }

    private boolean isCharacterElement(ImageView imageView) {
        // Check if the image source contains the substring "charact"
        String imageSource = imageView.getImage().getUrl();
        return imageSource.contains("Charact");
    }


    private Client createClient() {
        try {
            return new Client(SERVER_HOST, SERVER_PORT);
        } catch (IOException e) {
            Platform.runLater(() -> {
                // Display an error dialog
                new Alert(Alert.AlertType.ERROR, "Connection failed").showAndWait();
                Platform.exit();
            });
            return null;
        }
    }

   /* private void addChatPanel() {

        chatArea = new TextArea();
        chatArea.setEditable(false);

        TextField chatInput = new TextField();
        chatInput.setOnAction(event -> {
            String message = chatInput.getText();
            chatInput.clear();

            // Send message to server
            String serverMessage = ""; // not sure how to format this
            client.sendMessage(serverMessage);

            chatArea.appendText("You: " + message + "\n");
        });
        VBox chatPanel = new VBox(5, chatArea, chatInput);
        gridPane.add(chatPanel, 26, 0, 1, 1);
    }*/


    private void handleKeyPress(KeyCode keyCode) {
        if (client != null) {
            client.sendKeyPress(keyCode);
        } else {
            System.out.println("Client not available. Check the connection.");
        }
    }

    public static int[][] convertStringToMatrix(String matrixString) {
        String[] matrixRows = matrixString.trim().split("\n");
        int rowCount = matrixRows.length;

        if (rowCount == 0) {
            // Handle empty matrix or invalid input
            return new int[0][0];
        }

        String[] col = matrixRows[0].trim().split("\\s+");
        int colCount = col.length;
        int[][] matrix = new int[rowCount][colCount];

        for (int i = 0; i < rowCount; i++) {
            String [] cells = matrixRows[i].trim().split("\\s+");
            for (int j = 0; j < colCount; j++) {
                matrix[i][j] = Integer.parseInt(cells[j]);
            }
        }

        return matrix;
    }
}