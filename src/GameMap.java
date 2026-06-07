import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class GameMap {
    private int width;
    private int height;
    private int[][] grid;

    private int[][] playerBFS;
    private int[][] targetBFS;

    private int exitX;
    private int exitY;

    private int lastPlayerX = -1;
    private int lastPlayerY = -1;
    private int lastTargetX = -1;
    private int lastTargetY = -1;
    private boolean lastPlayerHasKey = false;

    public GameMap(int width, int height, int totalKeysRequired) {
        this.width = width % 2 == 0 ? width + 1 : width;
        this.height = height % 2 == 0 ? height + 1 : height;
        this.grid = new int[this.height][this.width];
        
        generateMaze(totalKeysRequired);
        updateTargetBFS(false, 1.5, 1.5);
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public int getCell(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return 1;
        return grid[y][x];
    }

    public void setCell(int x, int y, int value) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            grid[y][x] = value;
        }
    }

    public boolean isWalkable(double x, double y) {
        int tx = (int) x;
        int ty = (int) y;
        int cell = getCell(tx, ty);
        return cell == 0 || cell == 4 || cell == 6;
    }

    public int getExitX() { return exitX; }
    public int getExitY() { return exitY; }

    public int[][] computeBFS(int startX, int startY) {
        int[][] dist = new int[height][width];
        for (int y = 0; y < height; y++) {
            Arrays.fill(dist[y], -1);
        }

        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{startX, startY});
        dist[startY][startX] = 0;

        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};

        while (!queue.isEmpty()) {
            int[] curr = queue.poll();
            int cx = curr[0];
            int cy = curr[1];

            for (int i = 0; i < 4; i++) {
                int nx = cx + dx[i];
                int ny = cy + dy[i];

                if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                    int cell = grid[ny][nx];
                    if (cell == 0 || cell == 4 || cell == 6 || cell == 3) {
                        if (dist[ny][nx] == -1) {
                            dist[ny][nx] = dist[cy][cx] + 1;
                            queue.add(new int[]{nx, ny});
                        }
                    }
                }
            }
        }
        return dist;
    }

    public void updatePlayerBFS(double px, double py) {
        int pCellX = (int) px;
        int pCellY = (int) py;

        if (pCellX != lastPlayerX || pCellY != lastPlayerY) {
            lastPlayerX = pCellX;
            lastPlayerY = pCellY;
            playerBFS = computeBFS(pCellX, pCellY);
        }
    }

    public void updateTargetBFS(boolean playerHasKey, double px, double py) {
        int targetX;
        int targetY;

        if (!playerHasKey) {
            int bestKeyX = -1;
            int bestKeyY = -1;
            int minDist = Integer.MAX_VALUE;

            updatePlayerBFS(px, py);

            for (int y = 1; y < height - 1; y++) {
                for (int x = 1; x < width - 1; x++) {
                    if (grid[y][x] == 6) {
                        int dist = getPlayerDistance(x, y);
                        if (dist >= 0 && dist < minDist) {
                            minDist = dist;
                            bestKeyX = x;
                            bestKeyY = y;
                        }
                    }
                }
            }

            if (bestKeyX != -1) {
                targetX = bestKeyX;
                targetY = bestKeyY;
            } else {
                targetX = exitX;
                targetY = exitY;
            }
        } else {
            targetX = exitX;
            targetY = exitY;
        }

        if (targetX != lastTargetX || targetY != lastTargetY || playerHasKey != lastPlayerHasKey || targetBFS == null) {
            lastTargetX = targetX;
            lastTargetY = targetY;
            lastPlayerHasKey = playerHasKey;
            targetBFS = computeBFS(targetX, targetY);
        }
    }

    public int getPlayerDistance(int x, int y) {
        if (playerBFS == null || x < 0 || x >= width || y < 0 || y >= height) return -1;
        return playerBFS[y][x];
    }

    public int[] getNextPathStep(int x, int y) {
        if (targetBFS == null) return new int[]{x, y};

        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};

        int bestX = x;
        int bestY = y;
        int minDist = Integer.MAX_VALUE;

        for (int i = 0; i < 4; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];

            if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                int cell = grid[ny][nx];
                if (cell == 0 || cell == 4 || cell == 6 || cell == 3) {
                    int dist = targetBFS[ny][nx];
                    if (dist >= 0 && dist < minDist) {
                        minDist = dist;
                        bestX = nx;
                        bestY = ny;
                    }
                }
            }
        }
        return new int[]{bestX, bestY};
    }

    public double[] getTargetCellCenter(int x, int y) {
        if (targetBFS == null) return new double[]{x + 0.5, y + 0.5};

        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};

        int bestX = x;
        int bestY = y;
        int minDist = Integer.MAX_VALUE;

        for (int i = 0; i < 4; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];

            if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                int cell = grid[ny][nx];
                if (cell == 0 || cell == 4 || cell == 6) {
                    int dist = targetBFS[ny][nx];
                    if (dist >= 0 && dist < minDist) {
                        minDist = dist;
                        bestX = nx;
                        bestY = ny;
                    }
                }
            }
        }

        return new double[]{bestX + 0.5, bestY + 0.5};
    }

    private void generateMaze(int totalKeysRequired) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x == 0 || x == width - 1 || y == 0 || y == height - 1) {
                    grid[y][x] = 1;
                } else {
                    grid[y][x] = 2;
                }
            }
        }

        boolean[][] visited = new boolean[height][width];
        dfs(1, 1, visited);

        grid[1][1] = 0;

        Random rand = new Random();
        int side = rand.nextInt(4);
        if (side == 0) {
            exitY = 0;
            int maxO = (width - 2 - 3) / 2;
            exitX = (maxO > 0) ? (2 * rand.nextInt(maxO + 1) + 3) : (width - 2);
            grid[exitY][exitX] = 3;
            grid[1][exitX] = 0;
        } else if (side == 1) {
            exitY = height - 1;
            int maxO = (width - 2 - 1) / 2;
            exitX = 2 * rand.nextInt(maxO + 1) + 1;
            grid[exitY][exitX] = 3;
            grid[exitY - 1][exitX] = 0;
        } else if (side == 2) {
            exitX = 0;
            int maxO = (height - 2 - 3) / 2;
            exitY = (maxO > 0) ? (2 * rand.nextInt(maxO + 1) + 3) : (height - 2);
            grid[exitY][exitX] = 3;
            grid[exitY][1] = 0;
        } else {
            exitX = width - 1;
            int maxO = (height - 2 - 1) / 2;
            exitY = 2 * rand.nextInt(maxO + 1) + 1;
            grid[exitY][exitX] = 3;
            grid[exitY][width - 2] = 0;
        }

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (grid[y][x] == 2) {
                    boolean horizCorridors = (grid[y][x - 1] == 0 && grid[y][x + 1] == 0);
                    boolean vertCorridors = (grid[y - 1][x] == 0 && grid[y + 1][x] == 0);
                    if ((horizCorridors || vertCorridors) && rand.nextDouble() < 0.40) {
                        grid[y][x] = 0;
                    }
                }
            }
        }

        java.util.List<int[]> placedKeys = new java.util.ArrayList<>();
        for (int k = 0; k < totalKeysRequired; k++) {
            java.util.List<KeyCandidate> candidates = new java.util.ArrayList<>();

            for (int y = 1; y < height - 1; y++) {
                for (int x = 1; x < width - 1; x++) {
                    boolean isExitAdj = (side == 0 && y == 1 && x == exitX) ||
                                        (side == 1 && y == exitY - 1 && x == exitX) ||
                                        (side == 2 && y == exitY && x == 1) ||
                                        (side == 3 && y == exitY && x == exitX - 1);

                    if (grid[y][x] == 0 && (x != 1 || y != 1) && !isExitAdj) {
                        boolean alreadyKey = false;
                        for (int[] pk : placedKeys) {
                            if (pk[0] == x && pk[1] == y) {
                                alreadyKey = true;
                                break;
                            }
                        }
                        if (alreadyKey) continue;

                        double distToPlayer = Math.sqrt((x - 1) * (x - 1) + (y - 1) * (y - 1));
                        double distToExit = Math.sqrt((x - exitX) * (x - exitX) + (y - exitY) * (y - exitY));

                        double distToOtherKeys = 1.0;
                        for (int[] pk : placedKeys) {
                            double d = Math.sqrt((x - pk[0]) * (x - pk[0]) + (y - pk[1]) * (y - pk[1]));
                            distToOtherKeys *= d;
                        }

                        double score = distToPlayer * distToExit * distToOtherKeys;
                        candidates.add(new KeyCandidate(x, y, score));
                    }
                }
            }

            if (!candidates.isEmpty()) {
                Collections.sort(candidates, (c1, c2) -> Double.compare(c2.score, c1.score));
                int limit = Math.min(candidates.size(), 6);
                int chosenIdx = rand.nextInt(limit);
                KeyCandidate chosen = candidates.get(chosenIdx);
                grid[chosen.y][chosen.x] = 6;
                placedKeys.add(new int[]{chosen.x, chosen.y});
            }
        }

    }

    private static class KeyCandidate {
        int x, y;
        double score;
        KeyCandidate(int x, int y, double score) {
            this.x = x;
            this.y = y;
            this.score = score;
        }
    }

    private void dfs(int x, int y, boolean[][] visited) {
        visited[y][x] = true;
        grid[y][x] = 0;

        int[][] dirs = {{2, 0}, {-2, 0}, {0, 2}, {0, -2}};
        Random rand = new Random();
        for (int i = dirs.length - 1; i > 0; i--) {
            int index = rand.nextInt(i + 1);
            int[] temp = dirs[index];
            dirs[index] = dirs[i];
            dirs[i] = temp;
        }

        for (int[] dir : dirs) {
            int nx = x + dir[0];
            int ny = y + dir[1];

            if (nx > 0 && nx < width - 1 && ny > 0 && ny < height - 1) {
                if (!visited[ny][nx]) {
                    grid[y + dir[1] / 2][x + dir[0] / 2] = 0;
                    dfs(nx, ny, visited);
                }
            }
        }
    }
}
