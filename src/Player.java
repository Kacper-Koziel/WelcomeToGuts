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
        double radius = 0.28;
        
        x += dx;
        resolveCollisions(map, radius);

        y += dy;
        resolveCollisions(map, radius);
    }

    private void resolveCollisions(GameMap map, double radius) {
        int cellX = (int) x;
        int cellY = (int) y;

        for (int wy = cellY - 1; wy <= cellY + 1; wy++) {
            for (int wx = cellX - 1; wx <= cellX + 1; wx++) {
                if (wx < 0 || wx >= map.getWidth() || wy < 0 || wy >= map.getHeight()) {
                    continue;
                }
                
                if (!map.isWalkable(wx + 0.5, wy + 0.5)) {
                    double cx = Math.max(wx, Math.min(x, wx + 1.0));
                    double cy = Math.max(wy, Math.min(y, wy + 1.0));

                    double distDX = x - cx;
                    double distDY = y - cy;
                    double dist = Math.sqrt(distDX * distDX + distDY * distDY);

                    if (dist < radius) {
                        if (dist > 0.0001) {
                            x += (distDX / dist) * (radius - dist);
                            y += (distDY / dist) * (radius - dist);
                        } else {
                            x += radius;
                        }
                    }
                }
            }
        }
    }
}
