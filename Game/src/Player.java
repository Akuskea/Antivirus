public class Player {
    private int playerId;
    private int x;
    private int y;
    private String role;
    private String infected = "not infected";
    private Boolean dead = false;

    public Player(int playerId, int x, int y) {
        this.playerId = playerId;
        this.x = x;
        this.y = y;
    }

    public int getPlayerId() {
        return playerId;
    }
    public String getRole() {
        return role;
    }
    public String getInfected() {
        return infected;
    }
    public void setInfected(String infected) {
        this.infected = infected;
    }

    public Boolean getDead() {
        return dead;
    }
    public void setDead(boolean dead) {
        this.dead = dead;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void move(int newX, int newY) {
        x = newX;
        y =newY;
    }
}
