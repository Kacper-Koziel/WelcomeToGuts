import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameEngine {
    public enum GameState { MENU, PLAYING, PAUSED, JUMPSCARE, GAMEOVER, GAMEWON, SETTINGS, CONFIRM_EXIT }

    public enum Difficulty {
        EASY(29, 1, 0.90, 0.08, 1, 16, "ŁATWY"),
        MEDIUM(41, 2, 1.35, 0.125, 1, 10, "ŚREDNI"),
        HARD(53, 4, 1.80, 0.18, 2, 6, "TRUDNY"),
        NIGHTMARE(65, 6, 2.25, 0.26, 3, 3, "KOSZMAR");

        final int mazeSize;
        final int enemyCount;
        final double baseSpeed;
        final double strainRate;
        final int keysRequired;
        final int keySearchRange;
        final String displayName;

        Difficulty(int mazeSize, int enemyCount, double baseSpeed, double strainRate, int keysRequired, int keySearchRange, String displayName) {
            this.mazeSize = mazeSize;
            this.enemyCount = enemyCount;
            this.baseSpeed = baseSpeed;
            this.strainRate = strainRate;
            this.keysRequired = keysRequired;
            this.keySearchRange = keySearchRange;
            this.displayName = displayName;
        }
    }

    private Player player;
    private ShadowEntity[] shadows;
    private GameMap map;
    private final Raycaster raycaster;
    private BufferedImage screenImage;
    private final GamePanel panel;

    private final boolean[] keys = new boolean[256];
    private boolean isRoboticMove = false;
    private boolean windowFocused = true;

    private GameState gameState = GameState.MENU;
    private Difficulty difficulty = Difficulty.MEDIUM;
    private int collectedKeys = 0;
    private int selectedMenuOption = 1;
    private int confirmExitSelection = 1;
    private int selectedPauseOption = 0;
    private boolean showingRules = false;

    private int selectedSettingsOption = 0;
    private int currentResolutionIndex = 1;
    private boolean jumpscaresEnabled = true;
    private int currentSensitivityIndex = 3;
    private static final double[] SENSITIVITY_MULTIPLIERS = { 0.25, 0.50, 0.75, 1.00, 1.25, 1.50, 1.75, 2.00, 2.50, 3.00 };
    private static final String[] SENSITIVITY_NAMES = { "0.25x", "0.50x", "0.75x", "1.00x (Standardowa)", "1.25x", "1.50x", "1.75x", "2.00x", "2.50x", "3.00x" };

    private static final int[][] RESOLUTIONS = {
        {160, 60},
        {320, 120},
        {640, 240},
        {960, 360},
        {1280, 480}
    };

    private double eyeStrain = 0.0;
    private double blinkTimer = 0.0;
    private double stamina = 1.0;

    private double heartbeatTimer = 0.0;
    private double jumpscareTimer = 0.0;
    private double keyWarningTimer = 0.0;
    private double breathTimer = 0.0;
    private double stepCycle = 0.0;

    private Thread gameThread;
    private volatile boolean running = false;

    private final KeyAdapter keyAdapter = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            int code = e.getKeyCode();
            if (code >= 0 && code < keys.length) {
                keys[code] = true;
            }

            if (gameState == GameState.MENU) {
                if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W) {
                    selectedMenuOption = (selectedMenuOption - 1 + 5) % 5;
                }
                if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) {
                    selectedMenuOption = (selectedMenuOption + 1) % 5;
                }
                if (code == KeyEvent.VK_ENTER) {
                    if (selectedMenuOption < 4) {
                        startGame(Difficulty.values()[selectedMenuOption]);
                    } else {
                        gameState = GameState.SETTINGS;
                        selectedSettingsOption = 0;
                    }
                }
                if (code == KeyEvent.VK_ESCAPE) {
                    gameState = GameState.CONFIRM_EXIT;
                    confirmExitSelection = 1;
                }
            } else if (gameState == GameState.SETTINGS) {
                if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W) {
                    selectedSettingsOption = (selectedSettingsOption - 1 + 4) % 4;
                }
                if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) {
                    selectedSettingsOption = (selectedSettingsOption + 1) % 4;
                }
                if (selectedSettingsOption == 0) {
                    if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) {
                        changeResolution((currentResolutionIndex - 1 + 5) % 5);
                    }
                    if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) {
                        changeResolution((currentResolutionIndex + 1) % 5);
                    }
                } else if (selectedSettingsOption == 1) {
                    if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) {
                        currentSensitivityIndex = (currentSensitivityIndex - 1 + SENSITIVITY_MULTIPLIERS.length) % SENSITIVITY_MULTIPLIERS.length;
                    }
                    if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) {
                        currentSensitivityIndex = (currentSensitivityIndex + 1) % SENSITIVITY_MULTIPLIERS.length;
                    }
                } else if (selectedSettingsOption == 2) {
                    if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A || code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D || code == KeyEvent.VK_ENTER) {
                        jumpscaresEnabled = !jumpscaresEnabled;
                    }
                } else if (selectedSettingsOption == 3) {
                    if (code == KeyEvent.VK_ENTER) {
                        gameState = GameState.MENU;
                        SoundManager.stopAmbientMusic();
                    }
                }
                if (code == KeyEvent.VK_ESCAPE) {
                    gameState = GameState.MENU;
                    SoundManager.stopAmbientMusic();
                }
            } else if (gameState == GameState.CONFIRM_EXIT) {
                if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A || code == KeyEvent.VK_UP || code == KeyEvent.VK_W) {
                    confirmExitSelection = 0;
                }
                if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D || code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) {
                    confirmExitSelection = 1;
                }
                if (code == KeyEvent.VK_ENTER) {
                    if (confirmExitSelection == 0) {
                        running = false;
                        shutdownRaycaster();
                        System.exit(0);
                    } else {
                        gameState = GameState.MENU;
                    }
                }
                if (code == KeyEvent.VK_ESCAPE) {
                    gameState = GameState.MENU;
                }
            } else if (gameState == GameState.PAUSED) {
                if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A || code == KeyEvent.VK_UP || code == KeyEvent.VK_W) {
                    selectedPauseOption = 0;
                }
                if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D || code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) {
                    selectedPauseOption = 1;
                }
                if (code == KeyEvent.VK_ENTER) {
                    if (selectedPauseOption == 0) {
                        gameState = GameState.PLAYING;
                    } else {
                        gameState = GameState.MENU;
                        SoundManager.stopAmbientMusic();
                    }
                }
                if (code == KeyEvent.VK_ESCAPE) {
                    gameState = GameState.PLAYING;
                }
            } else if (gameState == GameState.PLAYING) {
                if (code == KeyEvent.VK_ESCAPE) {
                    gameState = GameState.PAUSED;
                    selectedPauseOption = 0;
                    return;
                }
                if (showingRules) {
                    if (code == KeyEvent.VK_SPACE) {
                        showingRules = false;
                    }
                    return;
                }
                if (code == KeyEvent.VK_E) {
                    interact();
                }
                if (code == KeyEvent.VK_SPACE && blinkTimer <= 0) {
                    blinkTimer = 0.25;
                    eyeStrain = 0.0;
                    SoundManager.playBlinkSound();
                }
            } else if (gameState == GameState.GAMEOVER || gameState == GameState.GAMEWON) {
                if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_ESCAPE) {
                    gameState = GameState.MENU;
                    SoundManager.stopAmbientMusic();
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            int code = e.getKeyCode();
            if (code >= 0 && code < keys.length) {
                keys[code] = false;
            }
        }
    };

    public GameEngine(BufferedImage screenImage) {
        this.screenImage = screenImage;
        this.raycaster = new Raycaster();
        
        this.map = new GameMap(15, 15, 1);
        this.player = new Player(1.5, 1.5, 0.0);
        this.shadows = new ShadowEntity[0];

        this.panel = new GamePanel(screenImage, player, map, shadows, this);

        initInput();
    }

    public GamePanel getPanel() { return panel; }
    public GameState getGameState() { return gameState; }
    public int getConfirmExitSelection() { return confirmExitSelection; }
    public int getSelectedPauseOption() { return selectedPauseOption; }
    public Difficulty getDifficulty() { return difficulty; }
    public int getCollectedKeys() { return collectedKeys; }
    public int getCurrentSensitivityIndex() { return currentSensitivityIndex; }
    public double getCurrentSensitivityMultiplier() { return SENSITIVITY_MULTIPLIERS[currentSensitivityIndex]; }
    public static String[] getSensitivityNames() { return SENSITIVITY_NAMES; }
    public int getSelectedMenuOption() { return selectedMenuOption; }
    public Player getPlayer() { return player; }
    public GameMap getMap() { return map; }
    public ShadowEntity[] getShadows() { return shadows; }
    public double getEyeStrain() { return eyeStrain; }
    public double getStamina() { return stamina; }
    public boolean isBlinking() { return blinkTimer > 0; }
    public double getBlinkTimer() { return blinkTimer; }
    public double getKeyWarningTimer() { return keyWarningTimer; }
    public boolean isShowingRules() { return showingRules; }

    public void start(JFrame frame) {
        frame.addKeyListener(keyAdapter);
        panel.requestFocusInWindow();

        running = true;
        gameThread = new Thread(this::gameLoop, "GameLoopThread");
        gameThread.start();
    }

    public void startGame(Difficulty diff) {
        this.difficulty = diff;
        this.collectedKeys = 0;
        this.map = new GameMap(diff.mazeSize, diff.mazeSize, diff.keysRequired);
        this.player = new Player(1.5, 1.5, 0.0);
        this.player.setLives(3);
        this.player.setHasKey(false);
        this.eyeStrain = 0.0;
        this.blinkTimer = 0.0;
        this.stamina = 1.0;
        this.keyWarningTimer = 0.0;
        this.heartbeatTimer = 1.0;
        this.showingRules = true;

        this.player = new Player(1.5, 1.5, 0.0);

        this.shadows = new ShadowEntity[diff.enemyCount];
        java.util.Random rand = new java.util.Random();

        for (int i = 0; i < diff.enemyCount; i++) {
            int rx = 1, ry = 1;
            boolean valid = false;
            while (!valid) {
                rx = rand.nextInt(map.getWidth() - 2) + 1;
                ry = rand.nextInt(map.getHeight() - 2) + 1;
                if (map.getCell(rx, ry) == 0 && (rx != 1 || ry != 1)) {
                    double d = Math.sqrt((rx - 1.5) * (rx - 1.5) + (ry - 1.5) * (ry - 1.5));
                    if (d > 10.0) {
                        valid = true;
                    }
                }
            }
            this.shadows[i] = new ShadowEntity(rx + 0.5, ry + 0.5, 9);
        }

        map.updatePlayerBFS(player.getX(), player.getY());
        map.updateTargetBFS(false, player.getX(), player.getY());

        panel.updateReferences(player, map, shadows);
        this.gameState = GameState.PLAYING;
        SoundManager.startAmbientMusic();
        SwingUtilities.invokeLater(() -> {
            panel.requestFocusInWindow();
        });
    }

    private void gameLoop() {
        long lastTime = System.nanoTime();
        double physicsAccumulator = 0.0;
        
        while (running) {
            long now = System.nanoTime();
            double dt = (now - lastTime) / 1_000_000_000.0;
            lastTime = now;

            if (dt > 0.1) dt = 0.1;

            if (gameState == GameState.PLAYING) {
                if (!showingRules) {
                    double fixedStep = 0.01;
                    physicsAccumulator += dt;
                    if (physicsAccumulator > 0.1) physicsAccumulator = 0.1;
                    while (physicsAccumulator >= fixedStep) {
                        updatePhysics(fixedStep);
                        physicsAccumulator -= fixedStep;
                    }
                }
                raycaster.render(screenImage, player, map, shadows, true);
            } else if (gameState == GameState.PAUSED) {
                raycaster.render(screenImage, player, map, shadows, true);
            } else if (gameState == GameState.JUMPSCARE) {
                updateJumpscare(dt);
                raycaster.render(screenImage, player, map, shadows, true);
            } else {
                if (gameState == GameState.MENU) {
                    player.setAngle(player.getAngle() + 0.15 * dt);
                    raycaster.render(screenImage, player, map, shadows, false);
                }
            }

            panel.repaint();

            long targetEnd = now + 16_666_666L;
            while (System.nanoTime() < targetEnd) {
                long remainingNs = targetEnd - System.nanoTime();
                if (remainingNs > 2_000_000L) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                    }
                } else {
                    Thread.yield();
                }
            }
        }
    }

    private void initInput() {
        panel.addKeyListener(keyAdapter);

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (!windowFocused || !panel.isShowing() || gameState != GameState.PLAYING) return;

                int w = panel.getWidth();
                int h = panel.getHeight();
                if (w <= 0 || h <= 0) return;

                int centerX = w / 2;
                int centerY = h / 2;

                if (isRoboticMove) {
                    isRoboticMove = false;
                    return;
                }

                int dx = e.getX() - centerX;
                if (dx != 0) {
                    double mouseSensitivity = 0.0025 * SENSITIVITY_MULTIPLIERS[currentSensitivityIndex];
                    player.setAngle(player.getAngle() + dx * mouseSensitivity);
                }

                try {
                    Point screenCenter = panel.getLocationOnScreen();
                    isRoboticMove = true;
                    Robot robot = new Robot();
                    robot.mouseMove(screenCenter.x + centerX, screenCenter.y + centerY);
                } catch (Exception ex) {
                    isRoboticMove = false;
                }
            }
        });

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                panel.requestFocusInWindow();
                if (gameState == GameState.MENU) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        if (selectedMenuOption < 4) {
                            startGame(Difficulty.values()[selectedMenuOption]);
                        } else {
                            gameState = GameState.SETTINGS;
                            selectedSettingsOption = 0;
                        }
                    }
                } else if (gameState == GameState.GAMEOVER || gameState == GameState.GAMEWON) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        gameState = GameState.MENU;
                    }
                }
            }
        });

        panel.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            @Override
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent e) {
                int rotation = e.getWheelRotation();
                if (gameState == GameState.MENU) {
                    if (rotation < 0) {
                        selectedMenuOption = (selectedMenuOption - 1 + 5) % 5;
                    } else if (rotation > 0) {
                        selectedMenuOption = (selectedMenuOption + 1) % 5;
                    }
                } else if (gameState == GameState.SETTINGS) {
                    if (rotation < 0) {
                        selectedSettingsOption = (selectedSettingsOption - 1 + 4) % 4;
                    } else if (rotation > 0) {
                        selectedSettingsOption = (selectedSettingsOption + 1) % 4;
                    }
                }
            }
        });
    }

    public void setWindowFocused(boolean focused) {
        this.windowFocused = focused;
        if (!focused && gameState == GameState.PLAYING) {
            gameState = GameState.PAUSED;
            selectedPauseOption = 0;
        }
    }

    private void interact() {
        double lookX = player.getX() + Math.cos(player.getAngle()) * 1.2;
        double lookY = player.getY() + Math.sin(player.getAngle()) * 1.2;

        int tx = (int) lookX;
        int ty = (int) lookY;

        if (tx >= 0 && tx < map.getWidth() && ty >= 0 && ty < map.getHeight()) {
            int cell = map.getCell(tx, ty);
            if (cell == 3) {
                if (player.hasKey()) {
                    map.setCell(tx, ty, 4);
                    gameState = GameState.GAMEWON;
                    SoundManager.stopAmbientMusic();
                    SoundManager.playDoorOpen();
                } else {
                    keyWarningTimer = 2.5;
                }
            }
        }
    }

    private void updatePhysics(double dt) {
        boolean isMoving = keys[KeyEvent.VK_W] || keys[KeyEvent.VK_UP] ||
                           keys[KeyEvent.VK_S] || keys[KeyEvent.VK_DOWN] ||
                           keys[KeyEvent.VK_A] || keys[KeyEvent.VK_D];

        boolean sprintPressed = keys[KeyEvent.VK_SHIFT] || keys[KeyEvent.VK_CONTROL];
        boolean isSprinting = isMoving && sprintPressed && stamina > 0.0;

        double moveSpeed = 1.8;
        if (isSprinting) {
            moveSpeed *= 1.6;
            stamina -= 0.35 * dt;
            if (stamina < 0.0) stamina = 0.0;
        } else {
            stamina += 0.18 * dt;
            if (stamina > 1.0) stamina = 1.0;
        }

        double currentSpeed = moveSpeed * dt;
        double rotSpeed = 2.4 * dt;

        double moveX = 0;
        double moveY = 0;

        if (keys[KeyEvent.VK_W] || keys[KeyEvent.VK_UP]) {
            moveX += Math.cos(player.getAngle());
            moveY += Math.sin(player.getAngle());
        }
        if (keys[KeyEvent.VK_S] || keys[KeyEvent.VK_DOWN]) {
            moveX -= Math.cos(player.getAngle());
            moveY -= Math.sin(player.getAngle());
        }
        if (keys[KeyEvent.VK_A]) {
            moveX += Math.cos(player.getAngle() - Math.PI / 2.0);
            moveY += Math.sin(player.getAngle() - Math.PI / 2.0);
        }
        if (keys[KeyEvent.VK_D]) {
            moveX += Math.cos(player.getAngle() + Math.PI / 2.0);
            moveY += Math.sin(player.getAngle() + Math.PI / 2.0);
        }

        double len = Math.sqrt(moveX * moveX + moveY * moveY);
        if (len > 0) {
            moveX = (moveX / len) * currentSpeed;
            moveY = (moveY / len) * currentSpeed;
        }

        if (keys[KeyEvent.VK_LEFT]) {
            player.setAngle(player.getAngle() - rotSpeed);
        }
        if (keys[KeyEvent.VK_RIGHT]) {
            player.setAngle(player.getAngle() + rotSpeed);
        }

        player.move(moveX, moveY, map, dt);

        int px = (int) player.getX();
        int py = (int) player.getY();
        if (map.getCell(px, py) == 6) {
            collectedKeys++;
            map.setCell(px, py, 0);
            if (collectedKeys >= difficulty.keysRequired) {
                player.setHasKey(true);
            }
            map.updateTargetBFS(player.hasKey(), player.getX(), player.getY());
            SoundManager.playKeyCollect();
        }

        if (keyWarningTimer > 0) {
            keyWarningTimer -= dt;
        }

        if (blinkTimer > 0) {
            blinkTimer -= dt;
        } else {
            eyeStrain += dt * difficulty.strainRate;
            if (eyeStrain >= 1.0) {
                blinkTimer = 0.40;
                eyeStrain = 0.0;
                SoundManager.playBlinkSound();
            }
        }

        map.updatePlayerBFS(player.getX(), player.getY());
        map.updateTargetBFS(player.hasKey(), player.getX(), player.getY());
        for (ShadowEntity shadow : shadows) {
            shadow.update(player, map, blinkTimer > 0, difficulty.baseSpeed, difficulty.keySearchRange, dt);

            double dx = shadow.getX() - player.getX();
            double dy = shadow.getY() - player.getY();
            double dist = Math.sqrt(dx * dx + dy * dy);

            if (dist < 0.45) {
                if (jumpscaresEnabled) {
                    SoundManager.playScreech();
                }
                player.setLives(player.getLives() - 1);
                if (jumpscaresEnabled) {
                    jumpscareTimer = 1.5;
                    gameState = GameState.JUMPSCARE;
                } else {
                    if (player.getLives() <= 0) {
                        gameState = GameState.GAMEOVER;
                    } else {
                        respawnPlayerAndShadows();
                    }
                }
                break;
            }
        }

        heartbeatTimer -= dt;
        if (heartbeatTimer <= 0) {
            double minDist = Double.MAX_VALUE;
            for (ShadowEntity shadow : shadows) {
                double dx = shadow.getX() - player.getX();
                double dy = shadow.getY() - player.getY();
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < minDist) minDist = dist;
            }

            if (minDist < 10.0) {
                float volume = (float) (1.0 - (minDist / 10.0));
                volume = Math.max(0.15f, Math.min(0.9f, volume));
                SoundManager.playHeartbeat(volume);
                heartbeatTimer = 0.45 + (minDist / 10.0) * 0.85;
            } else if (eyeStrain > 0.6) {
                SoundManager.playHeartbeat(0.25f);
                heartbeatTimer = 0.7;
            } else {
                heartbeatTimer = 1.5;
            }
        }

        if (stamina < 0.25) {
            breathTimer -= dt;
            if (breathTimer <= 0) {
                float volume = (float) (1.0 - (stamina / 0.25));
                volume = Math.max(0.2f, Math.min(1.0f, volume));
                SoundManager.playHeavyBreathing(volume);
                breathTimer = 1.6;
            }
        } else {
            breathTimer = 0.0;
        }

        if (isMoving) {
            double cycleIncrement = isSprinting ? 5.5 : 3.8;
            stepCycle += cycleIncrement * dt;
            if (stepCycle >= Math.PI) {
                SoundManager.playPlayerFootstep();
                stepCycle -= Math.PI;
            }
        } else {
            stepCycle = 0.0;
        }
    }

    private void updateJumpscare(double dt) {
        jumpscareTimer -= dt;
        if (jumpscareTimer <= 0) {
            if (player.getLives() <= 0) {
                gameState = GameState.GAMEOVER;
                SoundManager.stopAmbientMusic();
            } else {
                respawnPlayerAndShadows();
            }
        }
    }

    private void respawnPlayerAndShadows() {
        player.setX(1.5);
        player.setY(1.5);
        player.setAngle(0.0);

        List<int[]> candidates = new ArrayList<>();
        for (int y = 1; y < map.getHeight() - 1; y++) {
            for (int x = 1; x < map.getWidth() - 1; x++) {
                if (map.getCell(x, y) == 0 && (x != 1 || y != 1)) {
                    double dist = Math.sqrt((x - 1) * (x - 1) + (y - 1) * (y - 1));
                    if (dist > 5.5) {
                        candidates.add(new int[]{x, y});
                    }
                }
            }
        }
        if (!candidates.isEmpty()) {
            Collections.shuffle(candidates);
            for (int i = 0; i < shadows.length; i++) {
                int[] pos = candidates.get(i % candidates.size());
                shadows[i].resetPosition(pos[0] + 0.5, pos[1] + 0.5);
            }
        }

        eyeStrain = 0.0;
        blinkTimer = 0.0;
        keyWarningTimer = 0.0;
        heartbeatTimer = 1.0;

        map.updatePlayerBFS(player.getX(), player.getY());
        map.updateTargetBFS(player.hasKey(), player.getX(), player.getY());
        gameState = GameState.PLAYING;
    }

    public double getJumpscareTimer() {
        return jumpscareTimer;
    }

    public int getSelectedSettingsOption() {
        return selectedSettingsOption;
    }

    public int getCurrentResolutionIndex() {
        return currentResolutionIndex;
    }

    public boolean isJumpscaresEnabled() {
        return jumpscaresEnabled;
    }

    public void changeResolution(int index) {
        if (index < 0 || index >= RESOLUTIONS.length) return;
        currentResolutionIndex = index;
        int width = RESOLUTIONS[index][0];
        int height = RESOLUTIONS[index][1];

        BufferedImage newImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        this.screenImage = newImg;

        this.panel.setScreenImage(newImg);
        this.raycaster.setResolution(width, height);
    }

    public void shutdownRaycaster() {
        if (raycaster != null) {
            raycaster.shutdown();
        }
    }
}
