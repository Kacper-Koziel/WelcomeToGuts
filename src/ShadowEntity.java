import java.util.Random;

public class ShadowEntity {
    private double x;
    private double y;
    private int type = 0;
    private boolean frozen;
    private final Random random = new Random();

    public enum AIState { WANDERING, CHASING }
    private AIState aiState = AIState.WANDERING;
    
    private double lastSeenX = -1;
    private double lastSeenY = -1;
    private int wanderTargetX = -1;
    private int wanderTargetY = -1;
    private boolean wasBlinking = false;
    private double stepCycle = 0.0;

    private int[][] pathBFS = null;
    private int lastBFSTargetX = -1;
    private int lastBFSTargetY = -1;

    public ShadowEntity(double x, double y, int type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.frozen = false;
    }

    public int getType() { return type; }

    public double getX() { return x; }
    public double getY() { return y; }
    public boolean isFrozen() { return frozen; }

    public void updateFrozenOnly(Player player, GameMap map, boolean isBlinking) {
        if (isBlinking) {
            frozen = false;
        } else {
            frozen = isPlayerLookingAtMe(player, map);
        }
    }

    private boolean hasLineOfSight(double px, double py, GameMap map) {
        double dx = px - x;
        double dy = py - y;
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist > 8.0) return false;

        int steps = (int) (dist * 8);
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            double cx = x + dx * t;
            double cy = y + dy * t;
            if (!map.isWalkable(cx, cy)) {
                return false;
            }
        }
        return true;
    }

    private int getDistanceToTarget(int cellX, int cellY) {
        if (pathBFS == null || cellY < 0 || cellY >= pathBFS.length || cellX < 0 || cellX >= pathBFS[0].length) {
            return -1;
        }
        return pathBFS[cellY][cellX];
    }

    private void updateTargetBFS(int tx, int ty, GameMap map) {
        if (pathBFS == null || tx != lastBFSTargetX || ty != lastBFSTargetY) {
            lastBFSTargetX = tx;
            lastBFSTargetY = ty;
            pathBFS = map.computeBFS(tx, ty);
        }
    }

    public void update(Player player, GameMap map, boolean isBlinking, double baseSpeed, int keySearchRange, double dt) {
        if (isBlinking) {
            frozen = false;
        } else {
            frozen = isPlayerLookingAtMe(player, map);
        }

        if (frozen) return;

        double px = player.getX();
        double py = player.getY();
        double dx = px - x;
        double dy = py - y;
        double playerDist = Math.sqrt(dx * dx + dy * dy);

        boolean canSeePlayer = hasLineOfSight(px, py, map);
        boolean canHearPlayer = playerDist < 2.5;

        boolean blinkStarted = isBlinking && !wasBlinking;
        wasBlinking = isBlinking;

        if (blinkStarted && canSeePlayer && random.nextDouble() < 0.35) {
            double bestTX = -1;
            double bestTY = -1;
            double bestDist = playerDist;
            int ex = (int) x;
            int ey = (int) y;

            for (int dyTile = -3; dyTile <= 3; dyTile++) {
                for (int dxTile = -3; dxTile <= 3; dxTile++) {
                    int nx = ex + dxTile;
                    int ny = ey + dyTile;
                    if (nx > 0 && nx < map.getWidth() - 1 && ny > 0 && ny < map.getHeight() - 1) {
                        if (map.getCell(nx, ny) == 0) {
                            double nDist = Math.sqrt((nx + 0.5 - px) * (nx + 0.5 - px) + (ny + 0.5 - py) * (ny + 0.5 - py));
                            if (nDist < playerDist && nDist > 1.5) {
                                boolean visible = isCellVisibleToPlayer(nx + 0.5, ny + 0.5, player, map);
                                if (!visible) {
                                    bestTX = nx + 0.5;
                                    bestTY = ny + 0.5;
                                    bestDist = nDist;
                                }
                            }
                        }
                    }
                }
            }

            if (bestTX != -1) {
                this.x = bestTX;
                this.y = bestTY;
                dx = px - x;
                dy = py - y;
                playerDist = bestDist;
                canSeePlayer = hasLineOfSight(px, py, map);
            }
        }

        if (canSeePlayer || canHearPlayer) {
            aiState = AIState.CHASING;
            lastSeenX = px;
            lastSeenY = py;
        }

        int tx = -1;
        int ty = -1;

        if (aiState == AIState.CHASING) {
            if (canSeePlayer || canHearPlayer) {
                tx = (int) px;
                ty = (int) py;
            } else {
                tx = (int) lastSeenX;
                ty = (int) lastSeenY;

                double distToLastSeen = Math.sqrt((lastSeenX - x) * (lastSeenX - x) + (lastSeenY - y) * (lastSeenY - y));
                if (distToLastSeen < 0.6) {
                    aiState = AIState.WANDERING;
                    wanderTargetX = -1;
                }
            }
        }

        if (aiState == AIState.WANDERING) {
            if (player.hasKey()) {
                if (wanderTargetX == -1 || Math.sqrt((wanderTargetX + 0.5 - x) * (wanderTargetX + 0.5 - x) + (wanderTargetY + 0.5 - y) * (wanderTargetY + 0.5 - y)) < 0.6 || random.nextDouble() < 0.05) {
                    int pTileX = (int) px;
                    int pTileY = (int) py;
                    boolean found = false;
                    for (int attempt = 0; attempt < 30; attempt++) {
                        int dxTile = random.nextInt(2 * keySearchRange + 1) - keySearchRange;
                        int dyTile = random.nextInt(2 * keySearchRange + 1) - keySearchRange;
                        
                        if (dxTile == 0 && dyTile == 0) {
                            if (random.nextBoolean()) {
                                dxTile = random.nextBoolean() ? 1 : -1;
                            } else {
                                dyTile = random.nextBoolean() ? 1 : -1;
                            }
                        }
                        
                        int rx = pTileX + dxTile;
                        int ry = pTileY + dyTile;
                        if (rx > 0 && rx < map.getWidth() - 1 && ry > 0 && ry < map.getHeight() - 1) {
                            if (map.getCell(rx, ry) == 0) {
                                wanderTargetX = rx;
                                wanderTargetY = ry;
                                found = true;
                                break;
                            }
                        }
                    }
                    if (!found) {
                        wanderTargetX = pTileX + (random.nextBoolean() ? 1 : -1);
                        wanderTargetY = pTileY + (random.nextBoolean() ? 1 : -1);
                    }
                }
            } else {
                if (wanderTargetX == -1 || Math.sqrt((wanderTargetX + 0.5 - x) * (wanderTargetX + 0.5 - x) + (wanderTargetY + 0.5 - y) * (wanderTargetY + 0.5 - y)) < 0.6) {
                    int rx = (int) x;
                    int ry = (int) y;
                    boolean found = false;
                    for (int attempt = 0; attempt < 50; attempt++) {
                        rx = random.nextInt(map.getWidth() - 2) + 1;
                        ry = random.nextInt(map.getHeight() - 2) + 1;
                        if (map.getCell(rx, ry) == 0) {
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        wanderTargetX = rx;
                        wanderTargetY = ry;
                    } else {
                        wanderTargetX = (int) px;
                        wanderTargetY = (int) py;
                    }
                }
            }
            tx = wanderTargetX;
            ty = wanderTargetY;
        }

        updateTargetBFS(tx, ty, map);

        double speed = baseSpeed * (isBlinking ? 1.5 : 1.0) * dt;

        if (playerDist < 1.3 && canSeePlayer) {
            if (playerDist > 0.05) {
                x += (dx / playerDist) * speed;
                y += (dy / playerDist) * speed;
            }
            return;
        }

        int ex = (int) x;
        int ey = (int) y;

        int[] ndx = {0, 0, 1, -1};
        int[] ndy = {1, -1, 0, 0};

        int bestNX = ex;
        int bestNY = ey;
        int minBFS = getDistanceToTarget(ex, ey);
        if (minBFS == -1) minBFS = Integer.MAX_VALUE;

        for (int i = 0; i < 4; i++) {
            int nx = ex + ndx[i];
            int ny = ey + ndy[i];
            if (map.isWalkable(nx + 0.5, ny + 0.5)) {
                int distVal = getDistanceToTarget(nx, ny);
                if (distVal >= 0 && distVal < minBFS) {
                    minBFS = distVal;
                    bestNX = nx;
                    bestNY = ny;
                }
            }
        }

        double targetX = bestNX + 0.5;
        double targetY = bestNY + 0.5;

        double tdx = targetX - x;
        double tdy = targetY - y;
        double tdist = Math.sqrt(tdx * tdx + tdy * tdy);

        if (tdist > 0.05) {
            double moveX = (tdx / tdist) * speed;
            double moveY = (tdy / tdist) * speed;

            double buffer = 0.25;
            double checkX = x + moveX + (moveX > 0 ? buffer : -buffer);
            double checkY = y + moveY + (moveY > 0 ? buffer : -buffer);

            if (map.isWalkable(checkX, y)) {
                x += moveX;
            } else {
                x = targetX;
            }
            if (map.isWalkable(x, checkY)) {
                y += moveY;
            } else {
                y = targetY;
            }
        } else {
            x = targetX;
            y = targetY;
        }

        if (!frozen) {
            double currentSpeed = baseSpeed * (isBlinking ? 1.5 : 1.0);
            stepCycle += 3.8 * currentSpeed * dt;
            if (stepCycle >= Math.PI) {
                double diffX = player.getX() - x;
                double diffY = player.getY() - y;
                double dist = Math.sqrt(diffX * diffX + diffY * diffY);
                if (dist < 12.0) {
                    float vol = (float) (1.0 - (dist / 12.0));
                    vol = Math.max(0.1f, Math.min(0.85f, vol));

                    double angleToEnemy = Math.atan2(y - player.getY(), x - player.getX()) - player.getAngle();
                    while (angleToEnemy < -Math.PI) angleToEnemy += 2 * Math.PI;
                    while (angleToEnemy >= Math.PI) angleToEnemy -= 2 * Math.PI;

                    boolean behind = Math.abs(angleToEnemy) > (Math.PI / 2.0);
                    if (behind) {
                        vol *= 1.25f;
                    }

                    SoundManager.playMonsterFootstep(vol);
                }
                stepCycle -= Math.PI;
            }
        } else {
            stepCycle = 0.0;
        }
    }

    private boolean isPlayerLookingAtMe(Player player, GameMap map) {
        double dx = x - player.getX();
        double dy = y - player.getY();
        double dist = Math.sqrt(dx * dx + dy * dy);

        double angleToEnemy = Math.atan2(dy, dx) - player.getAngle();
        while (angleToEnemy < -Math.PI) angleToEnemy += 2 * Math.PI;
        while (angleToEnemy >= Math.PI) angleToEnemy -= 2 * Math.PI;

        boolean inFOV = Math.abs(angleToEnemy) < (Math.PI / 6.0);

        if (inFOV) {
            return lineOfSightCheck(player, map, dist);
        }

        return false;
    }

    private boolean lineOfSightCheck(Player player, GameMap map, double dist) {
        double dx = x - player.getX();
        double dy = y - player.getY();

        double step = 0.05;
        double steps = dist / step;
        double cos = dx / dist;
        double sin = dy / dist;

        for (int i = 1; i < steps; i++) {
            double checkX = player.getX() + cos * i * step;
            double checkY = player.getY() + sin * i * step;
            int cell = map.getCell((int) checkX, (int) checkY);
            if (cell > 0 && cell != 4 && cell != 6) {
                return false;
            }
        }
        return true;
    }

    public void resetPosition(double rx, double ry) {
        this.x = rx;
        this.y = ry;
        this.frozen = false;
    }

    private boolean isCellVisibleToPlayer(double targetX, double targetY, Player player, GameMap map) {
        double dx = targetX - player.getX();
        double dy = targetY - player.getY();
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist > 8.0) return false;

        int steps = (int) (dist * 8);
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            double cx = player.getX() + dx * t;
            double cy = player.getY() + dy * t;
            if (!map.isWalkable(cx, cy)) {
                return false;
            }
        }

        double angleToTarget = Math.atan2(dy, dx);
        double diff = angleToTarget - player.getAngle();
        while (diff < -Math.PI) diff += 2 * Math.PI;
        while (diff > Math.PI) diff -= 2 * Math.PI;

        return Math.abs(diff) < (Math.PI / 3.0);
    }
}
