import javafx.scene.input.KeyCode;
import java.util.*;
import java.io.*;
import java.net.Socket;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class ClientHandler implements Runnable {
    private static int playerCount = 0;
    private static JFrame surveyResultsFrame;
    private static List<Integer> playersUsedQuestion = new ArrayList<>();
    private String[] questions = {
        "Who is a Civilian?",
                "Who is the Policeman?",
                "Who do you trust the most?",
                "Who is Reporter?",
                "Who is the Researcher?",
                "Who is the Spy?",
                "Who is the Immune?",
                "Who is the next to die?",
                "Who is infected?",
                "Who do you want to kill?",
                "Who do you trust the most?",
                "Who is acting suspiciously?",
                "Who is the most manipulative?",
                "Who do you want to alliance with?",
                "Who is lying the most?",
                "Who is lying the most?",
                "Who is easier to manipulate?"
    };

    private Socket clientSocket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private Player player;
    private String role;
    private static ScheduledExecutorService scheduler;
    private static final int TIMER_DELAY = 3; // 30 seconds

    private int currentQuestionIndex;
    private Map<Integer, Integer> answerCounts;

    public void setCurrentQuestionIndex(int currentQuestionIndex) {
        this.currentQuestionIndex = currentQuestionIndex;
    }
    public int getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.player = new Player(playerCount++, 5, 1 );


        MultiUserServer.gameWorld[1][5] = 1;
        startTimer();

        try {
            this.reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public Player getPlayer() {
        return player;
    }

    // Add this method to set the role for the client
    public void setRole(String role) {
        this.role = role;
    }

    // Add this method to get the assigned role for the client
    public String getRole() {
        return role;
    }

    @Override
    public void run() {
        try {
            // Handle communication with the client
            String input;
            while ((input = reader.readLine()) != null) {
                System.out.println(input);
                // Process the received message
                handleClientInput(input);
            }
            System.out.println("Client disconnected" );
        } catch (IOException e) {
            // Handle IOException - this occurs when the client disconnects
            System.out.println("Client disconnected" + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void handleClientInput(String input) {
        // Assuming the input format is "KEY_EVENT keyCode"
        String[] parts = input.split(" ", 2);
        String command = parts[0];

        switch (command) {
            case "KEY_EVENT":
                if (parts.length == 2) {
                    handleKeyEvent(KeyCode.valueOf(parts[1]));
                }
                break;
            case "DISCONNECT":
                MultiUserServer.gameWorld[player.getY()][player.getX()] = 0;
                break;
            case "SURVEY_ANSWER":
                handleSurveyAnswer(parts[1]);
                break;
            case "TOP_3":
                List<Map<Integer, Integer>> result = convertStringToTop3Results(parts[1]);
                displaySurveyResults(result);
                break;
        }
    }
    private void handleSurveyAnswer(String answerMessage) {
        String[] parts = answerMessage.split(" ");

        if (parts.length == 2) {
            int playerId = Integer.parseInt(parts[0]);
            int playerAnswer = Integer.parseInt(parts[1]);

            // Assuming players is a Map<Integer, Integer> in MultiUserServer
            MultiUserServer.playerAnswers.put(playerId, playerAnswer);

        }
    }

    private void handleKeyEvent(KeyCode keyCode) {
        // Handle key events and update player position
        int newX = player.getX();
        int newY = player.getY();

        System.out.println("Before Update - X: " + newX + ", Y: " + newY);

        switch (keyCode) {
            case UP:
                newY = newY - 1;
                break;
            case DOWN:
                newY =  newY+ 1;
                break;
            case LEFT:
                newX = newX- 1;
                break;
            case RIGHT:
                newX = newX + 1;
                break;
        }
        System.out.println("After Update - X: " + newX + ", Y: " + newY);


        // Check if the new position is a valid move (e.g., not a wall)
        if (MultiUserServer.gameWorld[newY][newX] == 0) {
            // Update player position and notify other clients
            MultiUserServer.updatePlayerPosition(player.getPlayerId(), newX, newY);
        }
        //question room
        if (MultiUserServer.gameWorld[newY][newX] == 14) {
            // Update player position and notify other clients
            MultiUserServer.updatePlayerPosition(player.getPlayerId(), newX, newY);
            if(scheduler == null){
                askQuestionWindow();
            }
        }if (MultiUserServer.gameWorld[newY][newX] == 15) {
            // Update player position and notify other clients
            MultiUserServer.updatePlayerPosition(player.getPlayerId(), newX, newY);
            askNurserySentenceWindow();
        }
    }
    private void askNurserySentenceWindow() {
        // Check if the player has already used their question
        if (playersUsedQuestion.contains(player.getPlayerId())) {
            // Display a message indicating that the player has already used their question
            JOptionPane.showMessageDialog(null, "You have already used your question for this game.");
            return;
        }

        // Display a confirmation window to ask if the player wants to use their question
        int option = JOptionPane.showConfirmDialog(null, "Would you like to use your only question?", "Question Confirmation", JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            // Set the flag to indicate that the player has used their question
            playersUsedQuestion.add(player.getPlayerId());

            // Display the nursery sentence window
            displayNurserySentence();
        }
    }
    private void displayNurserySentence() {
        // Create a new JFrame for the nursery sentence window
        JFrame sentenceFrame = new JFrame("Nursery Sentence Window");
        sentenceFrame.setLayout(new BoxLayout(sentenceFrame.getContentPane(), BoxLayout.Y_AXIS));

        // Get the current sentence (e.g., infected or not infected)
        String currentSentence = getCurrentNurserySentence();
        JLabel sentenceLabel = new JLabel(currentSentence);
        sentenceFrame.add(sentenceLabel);

        // Set up the JFrame
        sentenceFrame.setSize(300, 150);
        sentenceFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        sentenceFrame.setLocationRelativeTo(null);
        sentenceFrame.setVisible(true);
    }

    private String getCurrentNurserySentence() {
        String sentence = player.getInfected();
        return "You are +" + sentence;
    }
    private static void startTimer() {
        if (scheduler == null) {
            scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(() -> {
                System.out.println("Timer task executed.");
                // Change the question for all players
                MultiUserServer.changeQuestionForAll();
            }, 0, TIMER_DELAY, TimeUnit.MINUTES);
        }
    }
    private void askQuestionWindow() {
        SwingUtilities.invokeLater(() -> {
            // Get the current question
            String currentQuestion = getCurrentQuestion();

            // Create a new JFrame for the question window
            JFrame questionFrame = new JFrame("Question Window");
            questionFrame.setLayout(new BoxLayout(questionFrame.getContentPane(), BoxLayout.Y_AXIS));

            JLabel questionLabel = new JLabel(currentQuestion);
            questionFrame.add(questionLabel);

            JTextField answerField = new JTextField(10);
            questionFrame.add(answerField);

            JButton submitButton = new JButton("Submit Answer");
            questionFrame.add(submitButton);

            submitButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String answerText = answerField.getText().trim();

                    try {
                        int answer = Integer.parseInt(answerText);

                        if (answer >= 0 && answer <= 12) {

                            sendSurveyAnswer(answer);
                            questionFrame.dispose();
                        } else {
                            JOptionPane.showMessageDialog(null, "Please enter a number between 0 and 12.");
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "Please enter a valid integer.");
                    }
                }
            });

            // Set up the JFrame
            questionFrame.setSize(300, 150);
            questionFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            questionFrame.setLocationRelativeTo(null);
            questionFrame.setVisible(true);
        });
    }
    public void sendTop3Answers(List<Map.Entry<Integer, Integer>> top3) {
        if (!clientSocket.isClosed()) {
            try {
                // Send the top 3 answers to the client
                writer.write("TOP_3_ANSWERS " + convertTop3AnswersToString(top3));
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void sendSurveyAnswer(int answer) {
        if (!clientSocket.isClosed()) {
            try {
                // Send the survey answer to the server
                writer.write("SURVEY_ANSWER " + answer);
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static List<Map<Integer, Integer>> convertStringToTop3Results(String input) {
        List<Map<Integer, Integer>> top3Results = new ArrayList<>();
        String[] parts = input.split(" ");

        for (int i = 0; i < parts.length; i += 2) {
            if (i + 1 < parts.length) {
                int key = Integer.parseInt(parts[i]);
                int value = Integer.parseInt(parts[i + 1]);
                Map<Integer, Integer> result = new HashMap<>();
                result.put(key, value);
                top3Results.add(result);
            }
        }

        return top3Results;
    }
    private void displaySurveyResults(List<Map<Integer, Integer>> resultsList) {
        // Close the existing survey results frame if it's open
        if (surveyResultsFrame != null) {
            surveyResultsFrame.dispose();
        }

        // Create a new JFrame for the survey results
        surveyResultsFrame = new JFrame("Survey Results");
        surveyResultsFrame.setLayout(new BoxLayout(surveyResultsFrame.getContentPane(), BoxLayout.Y_AXIS));

        // Sort the results based on votes (descending order)
        resultsList.sort((result1, result2) -> result2.entrySet().iterator().next().getValue()
                .compareTo(result1.entrySet().iterator().next().getValue()));

        // Display only the top 3 results
        int count = 0;
        for (Map<Integer, Integer> results : resultsList) {
            for (Map.Entry<Integer, Integer> entry : results.entrySet()) {
                JLabel resultLabel = new JLabel("Option " + entry.getKey() + ": " + entry.getValue() + " votes");
                surveyResultsFrame.add(resultLabel);
                count++;

                // Display only the top 3 results
                if (count >= 3) {
                    break;
                }
            }

            // Display only the top 3 results
            if (count >= 3) {
                break;
            }
        }

        // Set up the JFrame
        surveyResultsFrame.setSize(300, 150);
        surveyResultsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        surveyResultsFrame.setLocationRelativeTo(null);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                surveyResultsFrame.dispose();
            }
        });
        surveyResultsFrame.add(closeButton);

        surveyResultsFrame.setVisible(true);
    }

    private String convertTop3AnswersToString(List<Map.Entry<Integer, Integer>> top3) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Integer> entry : top3) {
            sb.append(entry.getKey()).append(":").append(entry.getValue()).append(" ");
        }
        return sb.toString();
    }
    private String getCurrentQuestion() {
        String question = questions[currentQuestionIndex];
        currentQuestionIndex = (currentQuestionIndex + 1) % questions.length;
        return question;
    }
    public void sendGameState(int[][] gameWorld) {
        if (!clientSocket.isClosed()) {
            try {
                // Send the updated game state to the client
                writer.write("GAME_STATE " + convertGameStateToString(gameWorld));
                writer.newLine();
                writer.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendRoleAssignment(String role) {
        if (!clientSocket.isClosed()) {
            try {
                writer.write("ROLE " + role);
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private String convertGameStateToString(int[][] gameWorld) {
        StringBuilder sb = new StringBuilder();
        for (int[] row : gameWorld) {
            for (int cell : row) {
                sb.append(cell).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

}