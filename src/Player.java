public class Player {
    private double x;
    private double y;
    private double angle;
    private boolean hasKey;
    private int lives;

    public Player(double x, double y, double angle) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.hasKey = false;
        this.lives = 3;
    }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public double getAngle() { return angle; }
    public void setAngle(double angle) {
        this.angle = angle;
        if (this.angle < 0) this.angle += 2 * Math.PI;
        if (this.angle >= 2 * Math.PI) this.angle -= 2 * Math.PI;
    }

    public boolean hasKey() { return hasKey; }
    public void setHasKey(boolean hasKey) { this.hasKey = hasKey; }

    public int getLives() { return lives; }
    public void setLives(int lives) { this.lives = lives; }

    public void move(double dx, double dy, GameMap map, double dt) {
        double radius = 0.32;
        
        double newX = x + dx;
        double newY = y + dy;

        boolean canMoveX = map.isWalkable(newX - radius, y - radius) &&
                           map.isWalkable(newX - radius, y + radius) &&
                           map.isWalkable(newX + radius, y - radius) &&
                           map.isWalkable(newX + radius, y + radius) &&
                           map.isWalkable(newX, y);
        if (canMoveX) {
            x = newX;
        }

        boolean canMoveY = map.isWalkable(x - radius, newY - radius) &&
                           map.isWalkable(x - radius, newY + radius) &&
                           map.isWalkable(x + radius, newY - radius) &&
                           map.isWalkable(x + radius, newY + radius) &&
                           map.isWalkable(x, newY);
        if (canMoveY) {
            y = newY;
        }
    }
}
