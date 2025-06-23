import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;
import javax.swing.SwingUtilities;


public class MultiUserServer {
    private static final int PORT = 8081;
    private static final int MAX_CLIENTS = 12;

    private static List<Player> players = new ArrayList<>();
    private static Player player;
    private static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private static List<ClientHandler> clients = new ArrayList<>();
    private static int connectedClients = 0;

    public static int[][] gameWorld = {
            {13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13},
            {13,0,0,0,0,0,0,0,13,13,13,0,0,0,0,0,0,0,15,15,15,15,15,13},
            {13,0,0,0,0,0,0,0,0,13,0,0,0,0,0,0,0,13,15,15,15,15,15,13},
            {13,0,13,13,0,0,0,0,0,13,0,0,13,0,0,0,0,13,13,15,15,15,15,13},
            {13,0,0,13,13,13,0,0,0,0,0,0,0,0,0,13,13,13,13,13,13,13,13,13},
            {13,0,0,0,0,13,13,0,0,13,13,13,0,0,0,0,13,0,0,0,0,0,13,13},
            {13,0,0,0,0,0,13,0,0,0,13,0,0,13,0,0,0,0,0,0,0,0,0,13},
            {13,0,0,0,13,13,13,0,0,0,13,0,0,13,0,0,0,0,0,0,0,0,0,13},
            {13,0,0,0,13,13,13,0,0,0,13,0,0,13,0,0,0,0,0,0,0,0,0,13},
            {13,0,0,0,13,13,13,0,0,0,13,0,0,13,0,0,0,0,0,0,0,0,0,13},
            {13,0,0,0,13,13,13,0,0,0,13,0,0,13,0,0,0,0,0,0,0,0,0,13},
            {13,0,0,0,13,13,13,0,0,0,13,0,0,13,0,0,0,0,0,0,0,0,0,13},
            {13,0,0,0,13,13,13,0,0,0,13,0,0,13,0,0,0,0,0,0,0,0,0,13},
            {13,0,0,0,13,13,13,0,0,0,13,0,0,13,0,0,0,0,0,0,0,0,0,13},
            {13,0,0,0,13,13,13,0,0,0,13,0,0,13,0,0,0,0,0,0,0,0,0,13},
            {13,0,0,0,13,13,13,0,0,0,13,0,0,13,0,0,0,0,0,0,0,0,0,13},
            {13,0,0,0,13,13,13,0,0,0,13,0,0,13,0,0,0,0,0,0,0,0,0,13},
            {13,0,13,0,0,0,0,0,0,0,0,0,0,0,0,0,13,13,13,13,13,13,13,13},
            {13,0,13,0,0,0,13,13,13,13,0,0,0,0,13,13,13,14,14,14,14,14,14,13},
            {13,0,0,0,0,0,0,0,0,13,0,0,0,13,13,14,14,14,14,14,14,14,14,13},
            {13,13,13,0,0,13,13,0,0,0,0,0,0,13,14,14,14,14,14,14,14,14,14,13},
            {13,0,0,0,0,13,0,0,0,0,0,0,0,0,14,14,14,14,14,14,14,14,14,13},
            {13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13}
    };
    public static int[][] getMatrix() {
        int[][] matrix = {
                {13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13},
                {13,0,0,0,0,0,0,0,13,13,13,0,0,0,0,0,0,0,15,15,15,15,15,13},
                {13,0,0,0,0,0,0,0,0,13,0,0,0,0,0,0,0,13,15,15,15,15,15,13},
                {13,0,13,13,0,0,0,0,0,13,0,0,13,0,0,0,0,13,13,15,15,15,15,13},
                {13,0,0,13,13,13,0,0,0,0,0,0,0,0,0,13,13,13,13,13,13,13,13,13},
                {13,0,0,0,0,13,13,0,0,13,13,13,0,0,0,0,13,0,0,0,0,0,13,13},
                {13,0,0,0,0,0,13,0,0,0,13,0,0,13,0,0,0,0,0,0,0,0,0,13},
                {13,0,0,0,13,13,13,0,0,0,13,0,0,13,0,0,0,0,0,0,0,0,0,13},
                {13,0,0,0,13,13,13,0,0,0,13,0,0,13,0,0,0,0,0,0,0,0,0,13},
                {13,0,0,0,13,13,13,0,0,0,13,0,0,13,0,0,0,0,0,0,0,0,0,13},
                {13,0,0,0,13,13,13,0,0,0,13,0,0,13,0,0,0,0,0,0,0,0,0,13},
                {13,0,0,0,13,13,13,0,0,0,13,0,0,13,0,0,0,0,0,0,0,0,0,13},
                {13,0,0,0,13,13,13,0,0,0,13,0,0,13,0,0,0,0,0,0,0,0,0,13},
                {13,0,0,0,13,13,13,0,0,0,13,0,0,13,0,0,0,0,0,0,0,0,0,13},
                {13,0,0,0,13,13,13,0,0,0,13,0,0,13,0,0,0,0,0,0,0,0,0,13},
                {13,0,0,0,13,13,13,0,0,0,13,0,0,13,0,0,0,0,0,0,0,0,0,13},
                {13,0,0,0,13,13,13,0,0,0,13,0,0,13,0,0,0,0,0,0,0,0,0,13},
                {13,0,13,0,0,0,0,0,0,0,0,0,0,0,0,0,13,13,13,13,13,13,13,13},
                {13,0,13,0,0,0,13,13,13,13,0,0,0,0,13,13,13,14,14,14,14,14,14,13},
                {13,0,0,0,0,0,0,0,0,13,0,0,0,13,13,14,14,14,14,14,14,14,14,13},
                {13,13,13,0,0,13,13,0,0,0,0,0,0,13,14,14,14,14,14,14,14,14,14,13},
                {13,0,0,0,0,13,0,0,0,0,0,0,0,0,14,14,14,14,14,14,14,14,14,13},
                {13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13}
        };
        return matrix;

    }
    public static Player getPlayer() {
        return player;
    }
    public static void setPlayer(Player player1) {
        player = player1;
    }
    public static List<Player> getPlayerList() {
        return players;
    }
    public void setPlayerList(List<Player> players1) {
        players = players1;
    }
    public static Map<Integer, Integer> playerAnswers = new HashMap<>();

    public static void main(String[] args) {
        // Seed for the random number generator
        long seed = 12345; // Use any long value

        // Create a list of roles
        List<String> roles = new ArrayList<>(List.of(
                "Police",
                "Civilian", "Civilian", "Civilian", "Civilian", "Civilian",
                "Immune",
                "Researcher",
                "Killer",
                "Spy", "Spy",
                "Reporter"
        ));
        // Shuffle the roles using the seeded random number generator
        Collections.shuffle(roles, new Random(seed));
        ExecutorService executor = Executors.newFixedThreadPool(MAX_CLIENTS);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);

            while (true) {

                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                if (connectedClients < MAX_CLIENTS) {
                    ClientHandler clientHandler = new ClientHandler(clientSocket);
                    clients.add(clientHandler);
                    executor.execute(clientHandler);

                    // Send the initial game state to the newly connected client
                    clientHandler.sendGameState(gameWorld);

                    connectedClients++;

                    if (connectedClients == MAX_CLIENTS) {
                        // Perform any necessary actions when all players are connected
                        System.out.println("All players connected!");
                        assignRoles(roles);
                        connectedClients = 0;
                        player = clientHandler.getPlayer();
                        for (ClientHandler client : clients) {
                            players.add(client.getPlayer());
                        }
                        scheduler.scheduleAtFixedRate(() -> {
                            performPeriodicActions();

                            //sendSwitchScreensMessageToClients();
                        }, 0, 3, TimeUnit.MINUTES);

                    }
                } else {
                    // Handle the case when more clients try to connect
                    System.out.println("Connection refused. Maximum players reached.");

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
    }
    private static void performPeriodicActions() {
        for (Player currentPlayer : players) {
            if(currentPlayer.getRole().equals("Civilian")){
                continue;
            }if(currentPlayer.getRole().equals("Spy")){
                continue;
            }
            askQuestion(currentPlayer);
        }
    }

    private static void askQuestion(Player currentPlayer) {
        SwingUtilities.invokeLater(() -> {
            // Pass the current player to the PlayerGUI constructor
            new PlayerGUI(players, currentPlayer);
        });
    }

    private static void assignRoles(List<String> roles) {
        // Assign roles based on the order of connection
        for (int i = 0; i < MAX_CLIENTS; i++) {
            ClientHandler client = clients.get(i);
            String role = roles.get(i); // Append the connected client number
            clients.get(i).getPlayer().setRole(role);
            // Assign the role to the client
            client.setRole(role);
        }

        // Optionally, you can broadcast the roles to all clients or perform other actions
        broadcastRoles();
    }
    public static synchronized void changeQuestionForAll() {
        for (ClientHandler clientHandler : clients) {
            clientHandler.setCurrentQuestionIndex(clientHandler.getCurrentQuestionIndex() + 1);
        }
    }

    private static synchronized void broadcastRoles() {
        // Broadcast the assigned roles to all clients
        for (ClientHandler client : clients) {
            client.sendRoleAssignment(client.getRole());
        }
    }
    public static synchronized void broadcastGameState() {
        for (ClientHandler client : clients) {
            client.sendGameState(gameWorld);
        }
    }
    public static synchronized void updatePlayerPosition(int playerId, int newX, int newY) {
        int [][]matrix = getMatrix();
        // Clear the old position
        gameWorld[clients.get(playerId).getPlayer().getY()][clients.get(playerId).getPlayer().getX()] = matrix[clients.get(playerId).getPlayer().getY()][clients.get(playerId).getPlayer().getX()];

        // Update the new position
        gameWorld[newY][newX] = playerId + 1;

        // Update player position
        clients.get(playerId).getPlayer().move(newX, newY);

        // Broadcast the updated game state to all clients
        broadcastGameState();
    }
}