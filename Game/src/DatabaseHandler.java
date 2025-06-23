import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseHandler {
    private static final String DB_URL = "jdbc:mysql://s-l112.engr.uiowa.edu:3306/engr_class013";
    private static final String DB_USER = "engr_class013";
    private static final String DB_PASSWORD = "engr_class013-xyz";
    public static boolean isValidUser(String email, String password) {
         try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, email);
                statement.setString(2, password);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next(); // Returns true if user is found
                }
            }
        } catch (SQLException e) {
             System.out.println("Error validating user: " + e);
            return false;
        }
    }
    public static int findOrCreateRoom(String playerName) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // First, check if the player is already in a room
            String checkPlayerRoomQuery = "SELECT room_id FROM rooms WHERE " +
                    "player1 = ? OR player2 = ? OR player3 = ? OR player4 = ? OR " +
                    "player5 = ? OR player6 = ? OR player7 = ? OR player8 = ? OR " +
                    "player9 = ? OR player10 = ? OR player11 = ? OR player12 = ?";
            try (PreparedStatement checkStatement = connection.prepareStatement(checkPlayerRoomQuery)) {
                for (int i = 1; i <= 12; i++) {
                    checkStatement.setString(i, playerName);
                }
                try (ResultSet resultSet = checkStatement.executeQuery()) {
                    if (resultSet.next()) {
                        // Player is already in a room, return the existing room ID
                        int roomId = resultSet.getInt("room_id");
                        System.out.println("Player " + playerName + " is already in room " + roomId);
                        return roomId;
                    }
                }
            }


            // If the player is not in a room, find an open room or create a new one
            String findOpenRoomQuery = "SELECT room_id FROM rooms WHERE player1 IS NULL OR player2 IS NULL OR player3 IS NULL OR player4 IS NULL OR " +
                    "player5 IS NULL OR player6 IS NULL OR player7 IS NULL OR player8 IS NULL OR " +
                    "player9 IS NULL OR player10 IS NULL OR player11 IS NULL OR player12 IS NULL";

             try (PreparedStatement findOpenRoomStatement = connection.prepareStatement(findOpenRoomQuery)) {
                try (ResultSet resultSet = findOpenRoomStatement.executeQuery()) {
                    while (resultSet.next()) {
                        // Found an open room, check and update with the player
                        int roomId = resultSet.getInt("room_id");
                        if (hasEmptyPlayerSlot(connection, roomId)) {
                            System.out.println("Found an open room " + roomId + " for player " + playerName);
                            updateRoomWithPlayer(connection, roomId, playerName);
                            return roomId;
                        }
                    }
                }
            }

            // No open room found, create a new room
            System.out.println("No open room found, creating a new room for player " + playerName);
            return createNewRoom(connection, playerName);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1; // Return -1 if an error occurs
        }
    }

    private static boolean hasEmptyPlayerSlot(Connection connection, int roomId) throws SQLException {
        // Check if the room has at least one empty player slot
        String checkEmptySlotQuery = "SELECT COUNT(*) AS emptySlots FROM rooms WHERE room_id = ? AND " +
                "(player1 IS NULL OR player2 IS NULL OR player3 IS NULL OR player4 IS NULL OR " +
                "player5 IS NULL OR player6 IS NULL OR player7 IS NULL OR player8 IS NULL OR " +
                "player9 IS NULL OR player10 IS NULL OR player11 IS NULL OR player12 IS NULL)";
        try (PreparedStatement statement = connection.prepareStatement(checkEmptySlotQuery)) {
            statement.setInt(1, roomId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int emptySlots = resultSet.getInt("emptySlots");
                    return emptySlots > 0;
                }
            }
        }
        return false;
    }
    public static void updateRoomWithPlayer(Connection connection, int roomId, String playerName) throws SQLException {
        for (int i = 1; i <= 12; i++) {
            String updateQuery = "UPDATE rooms SET player" + i + " = ? WHERE room_id = ? AND player" + i + " IS NULL";
            try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                updateStatement.setString(1, playerName);
                updateStatement.setInt(2, roomId);
                int updatedRows = updateStatement.executeUpdate();

                if (updatedRows > 0) {
                    // If the update was successful, exit the loop
                    break;
                }
            }
        }
    }

    private static int createNewRoom(Connection connection, String playerName) throws SQLException {
        // Create a new room and update it with the player's name
        String insertRoomQuery = "INSERT INTO rooms (player1, status) VALUES (?, 'waiting')";
        try (PreparedStatement insertStatement = connection.prepareStatement(insertRoomQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
            insertStatement.setString(1, playerName);
            insertStatement.executeUpdate();

            // Get the auto-generated room ID
            try (ResultSet generatedKeys = insertStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating room failed, no ID obtained.");
                }
            }
        }
    }
    public static void disconnectPlayerAndUpdateRoom(String playerName, int roomId) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Update the player's status (disconnect) and clear the corresponding player column in the room
            for (int i = 1; i <= 12; i++) {
                String disconnectQuery = "UPDATE rooms SET player" + i + " = NULL WHERE room_id = ? AND player" + i + " = ?";
                try (PreparedStatement disconnectStatement = connection.prepareStatement(disconnectQuery)) {
                    disconnectStatement.setInt(1, roomId);
                    disconnectStatement.setString(2, playerName);
                    disconnectStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.out.println("Error updating room: " + e);
            // Handle database-related exceptions
        }
    }
    public static boolean has12Players(int roomId) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String countPlayersQuery = "SELECT COUNT(*) AS playerCount FROM rooms WHERE room_id = ? " ;
            try (PreparedStatement statement = connection.prepareStatement(countPlayersQuery)) {
                statement.setInt(1, roomId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        int playerCount = resultSet.getInt("playerCount");
                        return playerCount == 12;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error counting players: " + e);
        }
        return false;
    }
}
