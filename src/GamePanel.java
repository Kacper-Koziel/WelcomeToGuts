import javax.swing.JPanel;
import java.awt.*;
import java.awt.image.BufferedImage;

public class GamePanel extends JPanel {
    private BufferedImage screenImage;
    private Player player;
    private GameMap map;
    private ShadowEntity[] shadows;
    private final GameEngine engine;
    private Image imgJumpscare = null;

    public GamePanel(BufferedImage screenImage, Player player, GameMap map, ShadowEntity[] shadows, GameEngine engine) {
        this.screenImage = screenImage;
        this.player = player;
        this.map = map;
        this.shadows = shadows;
        this.engine = engine;
        setBackground(Color.BLACK);
        setFocusable(true);

        try {
            imgJumpscare = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/jumpscare1.png"));
        } catch (Exception e) {
            System.err.println("Could not load jumpscare image: " + e.getMessage());
        }
    }

    public void updateReferences(Player player, GameMap map, ShadowEntity[] shadows) {
        this.player = player;
        this.map = map;
        this.shadows = shadows;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        int w = getWidth();
        int h = getHeight();

        g2.drawImage(screenImage, 0, 0, w, h, null);

        if (engine.getGameState() == GameEngine.GameState.MENU) {
            g2.setColor(new Color(0, 0, 0, 190));
            g2.fillRect(0, 0, w, h);

            double titlePulse = Math.sin(System.currentTimeMillis() * 0.004) * 0.12 + 0.88;
            g2.setColor(new Color((int) (240 * titlePulse), (int) (180 * titlePulse), (int) (40 * titlePulse)));
            g2.setFont(new Font("Consolas", Font.BOLD, (int) (h * 0.088)));
            drawCenteredString(g2, "WELCOME TO GUT'S", 0, (int) (-h * 0.34), w, h);

            g2.setColor(new Color(160, 50, 50));
            g2.setFont(new Font("Consolas", Font.ITALIC | Font.BOLD, (int) (h * 0.028)));
            drawCenteredString(g2, "Możesz zamknąć oczy... ale czy na pewno tego chcesz?", 0, (int) (-h * 0.27), w, h);

            String[] options = { "ŁATWY", "ŚREDNI", "TRUDNY", "KOSZMAR", "USTAWIENIA" };
            int selected = engine.getSelectedMenuOption();

            for (int i = 0; i < options.length; i++) {
                if (i == selected) {
                    g2.setColor(Color.YELLOW);
                    g2.setFont(new Font("Consolas", Font.BOLD, (int) (h * 0.050)));
                    drawCenteredString(g2, ">>  " + options[i] + "  <<", 0, (int) (-h * 0.13 + (i * h * 0.070)), w, h);
                } else {
                    g2.setColor(new Color(120, 120, 120));
                    g2.setFont(new Font("Consolas", Font.PLAIN, (int) (h * 0.040)));
                    drawCenteredString(g2, options[i], 0, (int) (-h * 0.13 + (i * h * 0.070)), w, h);
                }
            }

            String details = "";
            switch (selected) {
                case 0: details = "Obszar: 29x29  |  Cienie: 1 (Prędkość 0.50x)  |  Klucze: 1  |  Skupienie: Bardzo długie"; break;
                case 1: details = "Obszar: 41x41  |  Cienie: 2 (Prędkość 0.75x)  |  Klucze: 1  |  Skupienie: Standardowe"; break;
                case 2: details = "Obszar: 53x53  |  Cienie: 4 (Prędkość 1.00x)  |  Klucze: 2  |  Skupienie: Krótkie"; break;
                case 3: details = "Obszar: 65x65  |  Cienie: 6 (Prędkość 1.25x)  |  Klucze: 3  |  Skupienie: Minimalne"; break;
                case 4: details = "Ustawienia rozdzielczości renderowania 3D oraz włączenie/wyłączenie jumpscare."; break;
            }
            g2.setColor(new Color(220, 200, 140));
            g2.setFont(new Font("Consolas", Font.BOLD, (int) (h * 0.026)));
            drawCenteredString(g2, details, 0, (int) (h * 0.25), w, h);

            g2.setFont(new Font("Consolas", Font.BOLD, (int) (h * 0.026)));
            g2.setColor(Color.WHITE);
            drawCenteredString(g2, "Nawigacja: WS / ↑↓ / Scroll   |   Zacznij grę / Otwórz: ENTER / Kliknięcie   |   Wyjście: ESC", 0, (int) (h * 0.32), w, h);

            g2.setFont(new Font("Consolas", Font.PLAIN, (int) (h * 0.024)));
            g2.setColor(new Color(180, 180, 180));
            drawCenteredString(g2, "Ruch: WASD   |   Rozglądanie: Mysz   |   Mruganie: SPACJA   |   Interakcja: E", 0, (int) (h * 0.36), w, h);

            g2.setFont(new Font("Consolas", Font.ITALIC, (int) (h * 0.022)));
            g2.setColor(new Color(150, 150, 150));
            drawCenteredString(g2, "ZASADA PRZETRWANIA: Patrzenie na cienie paraliżuje je w bezruchu. Każde mrugnięcie pozwala im się zbliżyć – znacznie szybciej.", 0, (int) (h * 0.41), w, h);
            return;
        }

        if (engine.getGameState() == GameEngine.GameState.SETTINGS) {
            g2.setColor(new Color(0, 0, 0, 220));
            g2.fillRect(0, 0, w, h);

            g2.setColor(new Color(240, 180, 40));
            g2.setFont(new Font("Consolas", Font.BOLD, (int) (h * 0.08)));
            drawCenteredString(g2, "USTAWIENIA", 0, (int) (-h * 0.30), w, h);

            int selected = engine.getSelectedSettingsOption();

            String[] resNames = { "NAJSŁABSZA (160x60)", "NORMALNA (320x120)", "WYSOKA (640x240)", "BARDZO WYSOKA (960x360)", "ULTRA (1280x480)" };
            int currentResIndex = engine.getCurrentResolutionIndex();
            String resOptionText = "GRAFIKA 3D: [ " + resNames[currentResIndex] + " ]";

            String[] sensNames = GameEngine.getSensitivityNames();
            int currentSensIndex = engine.getCurrentSensitivityIndex();
            String sensOptionText = "CZUŁOŚĆ MYSZY: [ " + sensNames[currentSensIndex] + " ]";

            String jumpscareOptionText = "JUMPSCARES: [ " + (engine.isJumpscaresEnabled() ? "WŁĄCZONE" : "WYŁĄCZONE") + " ]";
            String backOptionText = "ZAPISZ I POWRÓĆ DO MENU GŁÓWNEGO";

            String[] settingOptions = { resOptionText, sensOptionText, jumpscareOptionText, backOptionText };

            for (int i = 0; i < settingOptions.length; i++) {
                if (i == selected) {
                    g2.setColor(Color.YELLOW);
                    g2.setFont(new Font("Consolas", Font.BOLD, (int) (h * 0.046)));
                    drawCenteredString(g2, ">>  " + settingOptions[i] + "  <<", 0, (int) (-h * 0.08 + (i * h * 0.085)), w, h);
                } else {
                    g2.setColor(new Color(150, 150, 150));
                    g2.setFont(new Font("Consolas", Font.PLAIN, (int) (h * 0.036)));
                    drawCenteredString(g2, settingOptions[i], 0, (int) (-h * 0.08 + (i * h * 0.085)), w, h);
                }
            }

            g2.setFont(new Font("Consolas", Font.BOLD, (int) (h * 0.028)));
            g2.setColor(Color.WHITE);
            drawCenteredString(g2, "Wybór: WS / ↑↓ / Scroll   |   Zmiana opcji: AD / ←→   |   Potwierdź / Wyjdź: ENTER / ESC", 0, (int) (h * 0.30), w, h);
            return;
        }

        if (engine.getGameState() == GameEngine.GameState.JUMPSCARE) {
            java.util.Random rand = new java.util.Random();

            int shakeX = rand.nextInt(60) - 30;
            int shakeY = rand.nextInt(60) - 30;

            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, w, h);

            if (imgJumpscare != null) {
                int ox = shakeX;
                int oy = shakeY;
                
                g2.drawImage(imgJumpscare, ox - 25, oy, w + 50, h, null);
                
                Composite oldComp = g2.getComposite();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
                g2.drawImage(imgJumpscare, ox + 25, oy, w + 50, h, null);
                g2.setComposite(oldComp);
            } else {
                g2.setColor(Color.RED);
                g2.fillRect(0, 0, w, h);
            }

            g2.setColor(new Color(40, 0, 0, 90));
            g2.fillRect(0, 0, w, h);

            java.awt.RadialGradientPaint p = new java.awt.RadialGradientPaint(
                new java.awt.geom.Point2D.Double(w / 2.0, h / 2.0),
                (float)(w * 0.75),
                new float[]{0.0f, 0.6f, 1.0f},
                new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 160), new Color(0, 0, 0, 245)}
            );
            Graphics2D g2d = (Graphics2D) g2;
            java.awt.Paint oldPaint = g2d.getPaint();
            g2d.setPaint(p);
            g2d.fillRect(0, 0, w, h);
            g2d.setPaint(oldPaint);

            drawGlitchyBloodyString(g2, "N I E  M R U G A J !", shakeX, (int) (h * 0.32) + shakeY, w, h, (int) (h * 0.12));

            for (int i = 0; i < 8; i++) {
                int gw = rand.nextInt(w / 3) + 20;
                int gh = rand.nextInt(h / 8) + 5;
                int gx = rand.nextInt(w - gw);
                int gy = rand.nextInt(h - gh);
                g2.setColor(new Color(255, 0, 0, 95));
                g2.fillRect(gx, gy, gw, gh);
            }

            g2.setColor(new Color(0, 0, 0, 80));
            for (int yCoord = 0; yCoord < h; yCoord += 4) {
                g2.fillRect(0, yCoord, w, 2);
            }
            return;
        }

        if (engine.getGameState() == GameEngine.GameState.GAMEOVER) {
            g2.setColor(new Color(15, 8, 8));
            g2.fillRect(0, 0, w, h);

            RadialGradientPaint gp = new RadialGradientPaint(
                new Point(w / 2, h / 2),
                (float) (w * 0.6),
                new float[]{0.0f, 1.0f},
                new Color[]{new Color(50, 15, 15, 120), new Color(0, 0, 0, 0)}
            );
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);

            String title = "STRACIŁEŚ ZMYSŁY";
            g2.setFont(new Font("Courier New", Font.BOLD, (int) (h * 0.065)));
            
            g2.setColor(new Color(255, 50, 50, 180));
            drawCenteredString(g2, title, -3, (int) (-h * 0.1) - 1, w, h);
            
            g2.setColor(new Color(50, 150, 255, 180));
            drawCenteredString(g2, title, 3, (int) (-h * 0.1) + 1, w, h);
            
            g2.setColor(new Color(255, 100, 100));
            drawCenteredString(g2, title, 0, (int) (-h * 0.1), w, h);

            g2.setFont(new Font("Courier New", Font.ITALIC, (int) (h * 0.026)));
            g2.setColor(new Color(190, 160, 160));
            drawCenteredString(g2, "Cienie Cię dopadły. Twój wzrok i zmysły zawiodły...", 0, (int) (h * 0.02), w, h);
            
            g2.setFont(new Font("Courier New", Font.PLAIN, (int) (h * 0.024)));
            g2.setColor(new Color(150, 120, 120));
            drawCenteredString(g2, "Zostałeś uwięziony w nieskończonych korytarzach Elektronika na zawsze.", 0, (int) (h * 0.08), w, h);

            int pulse = (int) (160 + 95 * Math.sin(System.currentTimeMillis() * 0.005));
            g2.setFont(new Font("Courier New", Font.BOLD, (int) (h * 0.024)));
            g2.setColor(new Color(220, 150, 150, pulse));
            drawCenteredString(g2, "[ NACIŚNIJ ENTER LUB ESC, ABY WRÓCIĆ DO MENU ]", 0, (int) (h * 0.22), w, h);

            g2.setColor(new Color(0, 0, 0, 90));
            for (int yCoord = 0; yCoord < h; yCoord += 4) {
                g2.fillRect(0, yCoord, w, 2);
            }
            return;
        }

        if (engine.getGameState() == GameEngine.GameState.GAMEWON) {
            g2.setColor(new Color(10, 12, 10));
            g2.fillRect(0, 0, w, h);

            RadialGradientPaint gp = new RadialGradientPaint(
                new Point(w / 2, h / 2),
                (float) (w * 0.6),
                new float[]{0.0f, 1.0f},
                new Color[]{new Color(15, 40, 15, 120), new Color(0, 0, 0, 0)}
            );
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);

            String title = "POWRÓT DO RZECZYWISTOŚCI";
            g2.setFont(new Font("Courier New", Font.BOLD, (int) (h * 0.065)));
            
            g2.setColor(new Color(255, 50, 50, 180));
            drawCenteredString(g2, title, -3, (int) (-h * 0.1) - 1, w, h);
            
            g2.setColor(new Color(50, 255, 255, 180));
            drawCenteredString(g2, title, 3, (int) (-h * 0.1) + 1, w, h);
            
            g2.setColor(new Color(180, 255, 180));
            drawCenteredString(g2, title, 0, (int) (-h * 0.1), w, h);

            g2.setFont(new Font("Courier New", Font.ITALIC, (int) (h * 0.026)));
            g2.setColor(new Color(160, 190, 160));
            drawCenteredString(g2, "Level 0 został za Tobą. Udało Ci się uciec...", 0, (int) (h * 0.02), w, h);
            
            g2.setFont(new Font("Courier New", Font.PLAIN, (int) (h * 0.024)));
            g2.setColor(new Color(120, 150, 120));
            drawCenteredString(g2, "Znalazłeś wyjście, lecz w uszach wciąż słyszysz brzęczenie świetlówek.", 0, (int) (h * 0.08), w, h);

            int pulse = (int) (160 + 95 * Math.sin(System.currentTimeMillis() * 0.005));
            g2.setFont(new Font("Courier New", Font.BOLD, (int) (h * 0.024)));
            g2.setColor(new Color(150, 220, 150, pulse));
            drawCenteredString(g2, "[ NACIŚNIJ ENTER LUB ESC, ABY WRÓCIĆ DO MENU ]", 0, (int) (h * 0.22), w, h);

            g2.setColor(new Color(0, 0, 0, 90));
            for (int yCoord = 0; yCoord < h; yCoord += 4) {
                g2.fillRect(0, yCoord, w, 2);
            }
            return;
        }

        if (engine.getGameState() == GameEngine.GameState.CONFIRM_EXIT) {
            g2.setColor(new Color(15, 12, 10));
            g2.fillRect(0, 0, w, h);

            RadialGradientPaint gp = new RadialGradientPaint(
                new Point(w / 2, h / 2),
                (float) (w * 0.6),
                new float[]{0.0f, 1.0f},
                new Color[]{new Color(50, 30, 15, 120), new Color(0, 0, 0, 0)}
            );
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);

            String title = "CZY NA PEWNO CHCESZ WYJŚĆ?";
            g2.setFont(new Font("Courier New", Font.BOLD, (int) (h * 0.055)));
            
            g2.setColor(new Color(255, 120, 50, 180));
            drawCenteredString(g2, title, -2, (int) (-h * 0.1) - 1, w, h);
            
            g2.setColor(new Color(50, 150, 255, 180));
            drawCenteredString(g2, title, 2, (int) (-h * 0.1) + 1, w, h);
            
            g2.setColor(new Color(240, 180, 50));
            drawCenteredString(g2, title, 0, (int) (-h * 0.1), w, h);

            g2.setFont(new Font("Courier New", Font.PLAIN, (int) (h * 0.026)));
            g2.setColor(new Color(180, 160, 140));
            drawCenteredString(g2, "Twój postęp w Elektronice zostanie utracony.", 0, (int) (h * 0.01), w, h);

            int sel = engine.getConfirmExitSelection();
            g2.setFont(new Font("Courier New", Font.BOLD, (int) (h * 0.04)));
            
            String takStr = (sel == 0) ? "> TAK <" : "  TAK  ";
            g2.setColor(sel == 0 ? new Color(255, 80, 80) : new Color(120, 100, 100));
            drawCenteredString(g2, takStr, (int) (-w * 0.15), (int) (h * 0.14), w, h);
            
            String nieStr = (sel == 1) ? "> NIE <" : "  NIE  ";
            g2.setColor(sel == 1 ? new Color(80, 255, 80) : new Color(100, 120, 100));
            drawCenteredString(g2, nieStr, (int) (w * 0.15), (int) (h * 0.14), w, h);

            g2.setFont(new Font("Courier New", Font.PLAIN, (int) (h * 0.022)));
            g2.setColor(new Color(130, 130, 130));
            drawCenteredString(g2, "[ Strzałki: Wybór  |  Enter: Potwierdź  |  Esc: Powrót ]", 0, (int) (h * 0.24), w, h);

            g2.setColor(new Color(0, 0, 0, 90));
            for (int yCoord = 0; yCoord < h; yCoord += 4) {
                g2.fillRect(0, yCoord, w, 2);
            }
            return;
        }

        if (!engine.isBlinking()) {
            double strain = engine.getEyeStrain();
            double minDist = Double.MAX_VALUE;
            for (ShadowEntity s : shadows) {
                double dx = s.getX() - player.getX();
                double dy = s.getY() - player.getY();
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < minDist) minDist = dist;
            }

            double baseStrain = strain > 0.3 ? (strain - 0.3) / 0.7 : 0.0;
            double proximityStrain = minDist < 5.0 ? (1.0 - (minDist / 5.0)) : 0.0;
            double maxAlphaFactor = Math.max(baseStrain, proximityStrain);

            if (maxAlphaFactor > 0) {
                double pulseFreq = minDist < 5.0 ? 12.0 : 5.0;
                double pulse = Math.sin(System.currentTimeMillis() * 0.001 * pulseFreq) * 0.15 + 0.85;
                int alpha = (int) (maxAlphaFactor * 160 * pulse);
                alpha = Math.max(0, Math.min(195, alpha));

                g2.setColor(new Color(40, 0, 0, alpha));
                g2.fillRect(0, 0, w, h);
            }
        }

        if (engine.getBlinkTimer() > 0) {
            double timer = engine.getBlinkTimer();
            double duration = (timer > 0.25) ? 0.40 : 0.25;
            double progress = 1.0 - (timer / duration);
            progress = Math.max(0.0, Math.min(1.0, progress));
            double eyelidFraction;
            if (progress < 0.5) {
                eyelidFraction = progress * 2.0;
            } else {
                eyelidFraction = (1.0 - progress) * 2.0;
            }

            int eyelidH = (int) ((h / 2.0) * eyelidFraction);
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, w, eyelidH);
            g2.fillRect(0, h - eyelidH, w, eyelidH);
        }

        if (engine.isShowingRules()) {
            g2.setColor(new Color(30, 25, 5, 130));
            g2.fillRect(0, 0, w, h);

            g2.setFont(new Font("Consolas", Font.BOLD, (int) (h * 0.03)));
            
            if ((System.currentTimeMillis() / 600) % 2 == 0) {
                g2.setColor(new Color(255, 40, 40));
                g2.drawString("● REC", (int) (w * 0.05), (int) (h * 0.08));
            } else {
                g2.setColor(new Color(255, 255, 255, 100));
                g2.drawString("  REC", (int) (w * 0.05), (int) (h * 0.08));
            }

            g2.setColor(Color.WHITE);
            g2.drawString("[||||] 100%", (int) (w * 0.80), (int) (h * 0.08));
            g2.drawString("▶ PLAY  SP", (int) (w * 0.05), (int) (h * 0.92));

            java.time.LocalDateTime ldt = java.time.LocalDateTime.now();
            String dateStr = ldt.format(java.time.format.DateTimeFormatter.ofPattern("MMM. dd yyyy").withLocale(java.util.Locale.US)).toUpperCase();
            String timeStr = ldt.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            g2.drawString(dateStr, (int) (w * 0.78), (int) (h * 0.88));
            g2.drawString(timeStr, (int) (w * 0.78), (int) (h * 0.92));

            int boxW = (int) (w * 0.78);
            int boxH = (int) (h * 0.68);
            int boxX = (w - boxW) / 2;
            int boxY = (h - boxH) / 2;

            g2.setColor(new Color(12, 10, 3, 235));
            g2.fillRect(boxX, boxY, boxW, boxH);

            g2.setColor(new Color(180, 140, 35));
            g2.setStroke(new java.awt.BasicStroke(3));
            g2.drawRect(boxX, boxY, boxW, boxH);
            g2.setStroke(new java.awt.BasicStroke(1));
            g2.drawRect(boxX + 5, boxY + 5, boxW - 10, boxH - 10);

            g2.setColor(new Color(0, 0, 0, 70));
            for (int yLines = boxY + 6; yLines < boxY + boxH - 6; yLines += 4) {
                g2.fillRect(boxX + 6, yLines, boxW - 12, 2);
            }

            java.util.Random rnd = new java.util.Random();
            if (rnd.nextDouble() < 0.15) {
                g2.setColor(new Color(255, 255, 255, 25));
                int noiseY = boxY + rnd.nextInt(boxH - 20);
                g2.fillRect(boxX + 6, noiseY, boxW - 12, rnd.nextInt(4) + 1);
            }

            String title = "LEVEL 0";
            g2.setFont(new Font("Consolas", Font.BOLD, (int) (h * 0.055)));
            
            g2.setColor(new Color(255, 0, 0, 160));
            drawCenteredString(g2, title, -2, (int) (-h * 0.24) - 1, w, h);
            g2.setColor(new Color(0, 0, 255, 160));
            drawCenteredString(g2, title, 2, (int) (-h * 0.24) + 1, w, h);
            g2.setColor(new Color(240, 180, 40));
            drawCenteredString(g2, title, 0, (int) (-h * 0.24), w, h);

            g2.setFont(new Font("Consolas", Font.PLAIN, (int) (h * 0.024)));
            g2.setColor(new Color(200, 200, 200));
            drawCenteredString(g2, "Twoim jedynym celem jest ucieczka z tego labiryntu.", 0, (int) (-h * 0.14), w, h);
            
            g2.setColor(Color.WHITE);
            drawCenteredString(g2, "Musisz odnaleźć złoty klucz, a potem czerwone drzwi wyjściowe.", 0, (int) (-h * 0.08), w, h);
            drawCenteredString(g2, "Kieruj się czerwonymi strzałkami na ścianach – wskażą Ci drogę.", 0, (int) (-h * 0.03), w, h);

            g2.setColor(new Color(230, 80, 80));
            drawCenteredString(g2, "--- ZAGROŻENIE: CIENIE ---", 0, (int) (h * 0.04), w, h);

            g2.setColor(new Color(220, 220, 220));
            drawCenteredString(g2, "1. Gdy patrzysz na cień, ten nie może się poruszyć.", 0, (int) (h * 0.10), w, h);
            drawCenteredString(g2, "2. Mruganie (automatyczne lub klawiszem SPACJA) pozwala mu się przemieścić.", 0, (int) (h * 0.16), w, h);

            String promptText = "[ NACIŚNIJ SPACJĘ, ABY ROZPOCZĄĆ GRĘ ]";
            g2.setFont(new Font("Consolas", Font.BOLD, (int) (h * 0.028)));
            if ((System.currentTimeMillis() / 450) % 2 == 0) {
                g2.setColor(new Color(255, 0, 0, 160));
                drawCenteredString(g2, promptText, -1, (int) (h * 0.26) - 1, w, h);
                g2.setColor(new Color(0, 0, 255, 160));
                drawCenteredString(g2, promptText, 1, (int) (h * 0.26) + 1, w, h);
                g2.setColor(new Color(240, 210, 100));
                drawCenteredString(g2, promptText, 0, (int) (h * 0.26), w, h);
            }
            return;
        }

        g2.setFont(new Font("Consolas", Font.BOLD, (int) (h * 0.026)));

        g2.setColor(Color.WHITE);
        drawStringWithShadow(g2, "ŻYCIA: " + player.getLives() + " / 3", (int) (w * 0.03), (int) (h * 0.06));

        int collected = engine.getCollectedKeys();
        int required = engine.getDifficulty().keysRequired;
        g2.setColor(new Color(240, 200, 40));
        drawStringWithShadow(g2, "KLUCZE: " + collected + " / " + required, (int) (w * 0.78), (int) (h * 0.06));

        double strain = engine.getEyeStrain();
        boolean critical = strain > 0.7;
        
        int hudX = (int) (w * 0.03);
        int eyeIconY = h - (int) (h * 0.095);
        int eyeW = 28;
        int eyeH = 18;
        
        drawEyeIcon(g2, hudX, eyeIconY, eyeW, eyeH, critical);
        
        int barX = hudX + eyeW + 12;
        int eyeBarY = eyeIconY - 4;
        int barW = 160;
        int barH = 8;
        
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRoundRect(barX, eyeBarY, barW, barH, 4, 4);
        g2.setColor(new Color(180, 180, 180, 180));
        g2.drawRoundRect(barX, eyeBarY, barW, barH, 4, 4);
        
        int fillW = (int) (barW * Math.min(1.0, Math.max(0.0, strain)));
        if (fillW > 0) {
            if (critical) {
                int pulse = (int) (140 + 115 * Math.abs(Math.sin(System.currentTimeMillis() * 0.008)));
                g2.setColor(new Color(255, 60, 60, pulse));
            } else {
                g2.setColor(new Color(230, 190, 40, 220));
            }
            g2.fillRoundRect(barX + 1, eyeBarY + 1, fillW - 2, barH - 2, 3, 3);
        }

        double stamina = engine.getStamina();
        boolean lowStamina = stamina < 0.25;
        
        int staminaIconY = h - (int) (h * 0.045);
        drawStaminaIcon(g2, hudX, staminaIconY, eyeW, eyeH, lowStamina);
        
        int staminaBarY = staminaIconY - 4;
        
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRoundRect(barX, staminaBarY, barW, barH, 4, 4);
        g2.setColor(new Color(180, 180, 180, 180));
        g2.drawRoundRect(barX, staminaBarY, barW, barH, 4, 4);
        
        int staminaFillW = (int) (barW * Math.min(1.0, Math.max(0.0, stamina)));
        if (staminaFillW > 0) {
            if (lowStamina) {
                int pulse = (int) (140 + 115 * Math.abs(Math.sin(System.currentTimeMillis() * 0.008)));
                g2.setColor(new Color(255, 60, 60, pulse));
            } else {
                g2.setColor(new Color(40, 180, 240, 220));
            }
            g2.fillRoundRect(barX + 1, staminaBarY + 1, staminaFillW - 2, barH - 2, 3, 3);
        }

        if (engine.getKeyWarningTimer() > 0) {
            g2.setColor(Color.RED);
            g2.setFont(new Font("Consolas", Font.BOLD, (int) (h * 0.045)));
            if ((System.currentTimeMillis() / 150) % 2 == 0) {
                drawCenteredString(g2, "[ POTRZEBUJESZ ZŁOTEGO KLUCZA, ABY OTWORZYĆ BRAMĘ! ]", 0, (int) (-h * 0.05), w, h);
            }
        }

        if (!engine.isBlinking()) {
            double minDist = Double.MAX_VALUE;
            ShadowEntity closestShadow = null;
            for (ShadowEntity s : shadows) {
                double dx = s.getX() - player.getX();
                double dy = s.getY() - player.getY();
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < minDist) {
                    minDist = dist;
                    closestShadow = s;
                }
            }

            if (closestShadow != null && minDist < 7.0) {
                if (closestShadow.isFrozen()) {
                    g2.setColor(Color.GREEN);
                    g2.setFont(new Font("Consolas", Font.BOLD, (int) (h * 0.035)));
                    drawCenteredString(g2, "PATRZYSZ NA NIEGO. UTRZYMAJ WZROK!", 0, (int) (-h * 0.35), w, h);
                } else {
                    g2.setColor(Color.RED);
                    g2.setFont(new Font("Consolas", Font.BOLD, (int) (h * 0.035)));
                    drawCenteredString(g2, "RUCH ZA PLECAMI! ODWRÓĆ SIĘ I SPÓJRZ NA NIEGO!", 0, (int) (-h * 0.35), w, h);
                }
            }
        }

        if (strain > 0.7 && !engine.isBlinking()) {
            g2.setColor(Color.RED);
            g2.setFont(new Font("Consolas", Font.BOLD, (int) (h * 0.035)));
            if ((System.currentTimeMillis() / 250) % 2 == 0) {
                drawCenteredString(g2, "[ ZAMKNIJ OCZY (SPACJA) ]", 0, (int) (-h * 0.15), w, h);
            }
        }
    }

    private void drawGlitchyBloodyString(Graphics2D g2, String text, int baseXOffset, int yOffset, int w, int h, int size) {
        Font originalFont = g2.getFont();
        Color originalColor = g2.getColor();
        java.util.Random rand = new java.util.Random();
        
        Font font = new Font("Chiller", Font.BOLD | Font.ITALIC, size);
        if (!font.getFamily().equalsIgnoreCase("Chiller")) {
            font = new Font("Impact", Font.BOLD, (int)(size * 0.85));
        }
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        
        int totalWidth = fm.stringWidth(text);
        int startX = (w - totalWidth) / 2 + baseXOffset;
        int y = ((h - fm.getHeight()) / 2) + fm.getAscent() + yOffset;
        
        int currentX = startX;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            String charStr = String.valueOf(c);
            int charWidth = fm.stringWidth(charStr);
            
            int jx = rand.nextInt(12) - 6;
            int jy = rand.nextInt(12) - 6;
            
            g2.setColor(new Color(50, 0, 0));
            g2.drawString(charStr, currentX + jx + 2, y + jy + 3);
            
            g2.setColor(new Color(230, 10, 10));
            g2.drawString(charStr, currentX + jx, y + jy);
            
            currentX += charWidth;
        }
        
        g2.setFont(originalFont);
        g2.setColor(originalColor);
    }

    private void drawCenteredString(Graphics2D g2, String text, int xOffset, int yOffset, int w, int h) {
        FontMetrics fm = g2.getFontMetrics();
        int x = (w - fm.stringWidth(text)) / 2 + xOffset;
        int y = ((h - fm.getHeight()) / 2) + fm.getAscent() + yOffset;
        g2.drawString(text, x, y);
    }

    private void drawStringWithShadow(Graphics2D g2, String text, int x, int y) {
        Color current = g2.getColor();
        g2.setColor(Color.BLACK);
        g2.drawString(text, x + 2, y + 2);
        g2.setColor(current);
        g2.drawString(text, x, y);
    }

    private void drawEyeIcon(Graphics2D g2, int x, int y, int w, int h, boolean critical) {
        g2.setColor(Color.BLACK);
        drawEyeShape(g2, x + 2, y + 2, w, h);
        g2.setColor(critical ? new Color(255, 60, 60) : Color.WHITE);
        drawEyeShape(g2, x, y, w, h);
    }

    private void drawEyeShape(Graphics2D g2, int x, int y, int w, int h) {
        g2.drawArc(x, y - h / 2, w, h, 0, 180);
        g2.drawArc(x, y - h / 2, w, h, 180, 180);
        int r = w / 3;
        g2.fillOval(x + w / 2 - r / 2, y - r / 2, r, r);
    }

    private void drawStaminaIcon(Graphics2D g2, int x, int y, int w, int h, boolean lowStamina) {
        g2.setColor(Color.BLACK);
        drawStaminaShape(g2, x + 2, y + 2, w, h);
        g2.setColor(lowStamina ? new Color(255, 60, 60) : new Color(40, 180, 240));
        drawStaminaShape(g2, x, y, w, h);
    }

    private void drawStaminaShape(Graphics2D g2, int x, int y, int w, int h) {
        int[] px = { x + w / 2 + 2, x + w - 4, x + w / 2 - 2, x + w / 2 + 4, x + 4, x + w / 2 - 2 };
        int[] py = { y - h / 2, y - 2, y - 2, y + h / 2, y + 2, y + 2 };
        g2.fillPolygon(px, py, px.length);
    }

    public void setScreenImage(BufferedImage screenImage) {
        this.screenImage = screenImage;
    }
}
