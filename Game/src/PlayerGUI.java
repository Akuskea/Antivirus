import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.List;
import java.util.Objects;

public class PlayerGUI extends JFrame {

    private JLabel questionLabel;
    private ScheduledExecutorService infectionScheduler = Executors.newScheduledThreadPool(1);

    private JTextField answerField;
    private JButton submitButton;
    private List <Player> players; // Reference to the list of Player objects

    private Player player;
    private boolean immunityChecked = false;
    private boolean playersEliminated = false;

    public PlayerGUI(List<Player> players, Player currentPlayer) {
        this.players = players;
        this.player = currentPlayer;

        if (player == null) {
            // If the player is null, do not create the GUI
            return;
        }

        setTitle("Player " + player.getPlayerId());
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        questionLabel = new JLabel(getQuestionText(player.getRole()));
        questionLabel.setBounds(20, 20, 300, 20);

        answerField = new JTextField();
        answerField.setBounds(20, 50, 300, 20);
        answerField.setPreferredSize(new Dimension(200, 30));

        submitButton = new JButton("Submit");
        submitButton.setBounds(20, 80, 100, 30);
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleAnswerSubmission();
            }
        });

        add(questionLabel);
        add(answerField);
        add(submitButton);

        setVisible(true);
    }

    private String policeAction = ""; // Variable to track the ongoing police action

    // Modify the getQuestionText method
    private String getQuestionText(String role) {
        switch (role) {
            case "Police":
                if (policeAction.isEmpty()) {
                    return "As a Police officer, choose an action:\n1. Check immunity (enter a player number, 1 to 12)\n2. Eliminate up to two players (enter 'kill' followed by player numbers, separated by space):";
                } else {
                    return "As a Police officer, you previously chose to " + policeAction + ". Choose the next action:\n1. Check immunity (enter a player number, 1 to 12)\n2. Eliminate up to two players (enter 'kill' followed by player numbers, separated by space):";
                }
            case "Reporter":
                return "As a Reporter, choose a player number (1 to 12) to reveal their role:";
            case "Killer":
                return "As the Killer, choose a player number (1 to 12) to eliminate:";
            case "Spy":
                return "As a Spy, choose a player number (1 to 12) to infect:";
            default:
                return "Please answer the question (0 to 12):";
        }
    }

    private void handleAnswerSubmission() {
        try {
            int answer = Integer.parseInt(answerField.getText());
            // Perform actions based on the player's role
            switch (player.getRole()) {
                case "Police":
                    handlePoliceAction(answer);
                    break;
                case "Reporter":
                    handleReporterAction(answer);
                    break;
                case "Killer":
                    handleKillerAction(answer);
                    break;
                case "Spy":
                    handleSpyAction(answer);
                    break;
            }
            dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid input. Please enter a number.");
        }
    }
    private void handleKillerAction(int answer) {
        if (answer >= 1 && answer <= 12) {
            for (Player p : players) {
                if(p.getPlayerId() == answer){
                    JOptionPane.showMessageDialog(this, "You kill "+ answer);
                    p.setDead(true);
                } else {
                    JOptionPane.showMessageDialog(this, "You chose incorrectly.");
                }
            }
        }
    }

    private void handlePoliceAction(int answer) {
        if (answer >= 1 && answer <= 12) {
            if (!immunityChecked) {
                // Check immunity
                for (Player p : players) {
                    if (p.getPlayerId() == answer) {
                        if (Objects.equals(p.getRole(), "Immune")) {
                            JOptionPane.showMessageDialog(this, "You chose correctly. Player is immune!");
                        } else {
                            JOptionPane.showMessageDialog(this, "You chose incorrectly. Player is not immune!");
                        }
                    }
                }
                // Set the ongoing action to "Check immunity"
                immunityChecked = true;
            } else if (immunityChecked && !playersEliminated && "kill".equalsIgnoreCase(answerField.getText())) {
                // Eliminate up to two players
                String[] killTargets = answerField.getText().split(" ");
                int eliminatedCount = 0;

                for (String target : killTargets) {
                    try {
                        int playerId = Integer.parseInt(target);
                        for (Player p : players) {
                            if (p.getPlayerId() == playerId && !p.getDead()) {
                                p.setDead(true);
                                eliminatedCount++;
                                JOptionPane.showMessageDialog(this, "You eliminated player " + playerId);
                                if (eliminatedCount >= 2) {
                                    playersEliminated = true;
                                }
                                break;
                            }
                        }
                    } catch (NumberFormatException ignored) {

                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid input or action already performed.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Invalid input. Please enter a valid player number or 'kill' command.");
        }
    }


    private void handleSpyAction(int answer) {
        if (answer >= 1 && answer <= 12) {
            for (Player p : players) {
                if(p.getPlayerId() == answer){
                    if(!Objects.equals(p.getRole(), "Immune")){
                        p.setInfected("infected");
                        // Schedule infection after 3 minutes
                        infectionScheduler.schedule(() -> {
                            p.setInfected("infected");

                            // Schedule death after 6 minutes
                            infectionScheduler.schedule(() -> {
                                if (!p.getDead()) {
                                    p.setDead(true);
                                    JOptionPane.showMessageDialog(this, "Player " + p.getPlayerId() + " died due to infection.");
                                }
                            }, 6, TimeUnit.MINUTES);

                            for (Player p1 : players) {
                                if (p1.getPlayerId() == p.getPlayerId()+1 || (p1.getPlayerId() == p.getPlayerId() -1) ) {
                                    if (!Objects.equals(p1.getRole(), "Immune")) {
                                        p1.setInfected("infected");
                                    }
                                }
                            }

                            JOptionPane.showMessageDialog(this, "Player " + p.getPlayerId() + " is now infected. They will die in 6 minutes.");
                        }, 3, TimeUnit.MINUTES);

                        JOptionPane.showMessageDialog(this, "Player " + p.getPlayerId() + " is now infected. They will die in 6 minutes.");
                    }
                    }
                }
            }

       }

    private void handleReporterAction(int answer) {
        if (answer >= 1 && answer <= 12) {
            for (Player p : players) {
                if(p.getPlayerId() == answer){

                    JOptionPane.showMessageDialog(this, p.getRole());

                } else {
                    JOptionPane.showMessageDialog(this, "You chose incorrectly. Player is not immune!");
                }
            }
        }
    }
}
