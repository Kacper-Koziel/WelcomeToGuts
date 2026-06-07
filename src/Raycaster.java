import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.List;

public class Raycaster {
    private int COLS = 320;
    private int VIEW_ROWS = 120;
    private static final double FOV = Math.PI / 3.0;
    private static final double DEPTH = 16.0;

    private int[] texture64Stand;
    private int[] texture64Walk1;
    private int[] texture64Walk2;

    public Raycaster() {
        texture64Stand = generateDetailed64x64(TEXTURE_REAPER_32_STAND);
        texture64Walk1 = generateDetailed64x64(TEXTURE_REAPER_32_WALK_1);
        texture64Walk2 = generateDetailed64x64(TEXTURE_REAPER_32_WALK_2);
    }

    private int[] generateDetailed64x64(String[] src32) {
        int[] pixels = new int[64 * 64];
        for (int y = 0; y < 64; y++) {
            int srcY = Math.min(y / 2, src32.length - 1);
            String row = src32[srcY];
            for (int x = 0; x < 64; x++) {
                int srcX = Math.min(x / 2, row.length() - 1);
                char c = row.charAt(srcX);
                int rgb = getPaletteRGB(c);

                if (c == '8') {
                    if ((x * 3 + y * 7) % 23 == 0 || (x * 7 - y * 3) % 29 == 0) {
                        rgb = getPaletteRGB('5');
                    }
                    if ((x * 13 + y * 17) % 41 == 0) {
                        rgb = getPaletteRGB('1');
                    }
                }

                if (c != '6') {
                    boolean nearEye = false;
                    for (int dy = -1; dy <= 1; dy++) {
                        int ny = Math.max(0, Math.min(src32.length - 1, srcY + dy));
                        for (int dx = -1; dx <= 1; dx++) {
                            int nx = Math.max(0, Math.min(src32[ny].length() - 1, srcX + dx));
                            if (src32[ny].charAt(nx) == '6') {
                                nearEye = true;
                                break;
                            }
                        }
                    }
                    if (nearEye && c == '1') {
                        rgb = getPaletteRGB('9');
                    }
                }
                
                pixels[y * 64 + x] = rgb;
            }
        }
        return pixels;
    }

    public void setResolution(int cols, int viewRows) {
        this.COLS = cols;
        this.VIEW_ROWS = viewRows;
    }

    public void shutdown() {
    }

    private static final String[] TEXTURE_REAPER_STAND = {
        ".....111111.....",
        "....11211211....",
        "...1188888811...",
        "..118811118811..",
        "..118181181811..",
        "..118161161811..",
        "..118811118811..",
        "...1188888811...",
        "....11111111....",
        "....11211211....",
        "...1121111211...",
        "..112111111211..",
        ".11211222211211.",
        "1121121111211211",
        "1121121111211211",
        "1111111111111111"
    };

    private static final String[] TEXTURE_REAPER_WALK_1 = {
        ".....111111.....",
        "....11211211....",
        "...1188888811...",
        "..118811118811..",
        "..118181181811..",
        "..118161161811..",
        "..118811118811..",
        "...1188888811...",
        "....11111111....",
        "....11211211....",
        "...1121111211...",
        "..11211111121...",
        ".1121122221121..",
        "11211211112112..",
        "11211...11211211",
        "1111.....1111111"
    };

    private static final String[] TEXTURE_REAPER_WALK_2 = {
        ".....111111.....",
        "....11211211....",
        "...1188888811...",
        "..118811118811..",
        "..118181181811..",
        "..118161161811..",
        "..118811118811..",
        "...1188888811...",
        "....11111111....",
        "....11211211....",
        "...1121111211...",
        "...11211111121..",
        "..1121122221121.",
        "..12112111121121",
        "11211211...11211",
        "1111111.....1111"
    };

    private static final String[] TEXTURE_REAPER_32_STAND = {
        "............11111111............",
        "..........111122221111..........",
        "........1111223333221111........",
        ".......111223344443322111.......",
        "......11122344111144322111......",
        ".....1112234118888114322111.....",
        "....111223411888888114322111....",
        "...11122341188888888114322111...",
        "...11223411885555558811432211...",
        "..1122341188511111158811432211..",
        "..1122341188119119118811432211..",
        "..1122341188196116918811432211..",
        "..1122341188119119118811432211..",
        "..1122341188511111158811432211..",
        "...11223411888555588811432211...",
        "...11122341118888881114322111...",
        "....111111111111111111111111....",
        "....111111221111111122111111....",
        "...11111122111111111122111111...",
        "...11112221111111111112221111...",
        "..1111222111122222211112221111..",
        "..1112221111222222221111222111..",
        ".111222111122333333221111222111.",
        ".112221111223311113322111122211.",
        "11222111122331111113322111122211",
        "11221111223111111111132211112211",
        "11221111221111111111112211112211",
        "11221111211111111111111211112211",
        "11111112211111111111111122111111",
        "11111112211111111111111122111111",
        "11111111111111111111111111111111",
        "11111111111111111111111111111111"
    };

    private static final String[] TEXTURE_REAPER_32_WALK_1 = {
        "............11111111............",
        "..........111122221111..........",
        "........1111223333221111........",
        ".......111223344443322111.......",
        "......11122344111144322111......",
        ".....1112234118888114322111.....",
        "....111223411888888114322111....",
        "...11122341188888888114322111...",
        "...11223411885555558811432211...",
        "..1122341188511111158811432211..",
        "..1122341188119119118811432211..",
        "..1122341188196116918811432211..",
        "..1122341188119119118811432211..",
        "..1122341188511111158811432211..",
        "...11223411888555588811432211...",
        "...11122341118888881114322111...",
        "....111111111111111111111111....",
        "....111111221111111122111111....",
        "...11111122111111111122111111...",
        "...11112221111111111112221111...",
        "..111122211112222221111222111...",
        "..111222111122222222111122211...",
        ".11122211112233333322111122211..",
        ".11222111122331111332211112221..",
        "112221111223311111133221111222..",
        "112211112231111111111322111122..",
        "112211112211111111111122111122..",
        "112211112111111111111112111122..",
        "1111111......1111111111122111111",
        "1111111......1111111111122111111",
        "111111........111111111111111111",
        "111111........111111111111111111"
    };

    private static final String[] TEXTURE_REAPER_32_WALK_2 = {
        "............11111111............",
        "..........111122221111..........",
        "........1111223333221111........",
        ".......111223344443322111.......",
        "......11122344111144322111......",
        ".....1112234118888114322111.....",
        "....111223411888888114322111....",
        "...11122341188888888114322111...",
        "...11223411885555558811432211...",
        "..1122341188511111158811432211..",
        "..1122341188119119118811432211..",
        "..1122341188196116918811432211..",
        "..1122341188119119118811432211..",
        "..1122341188511111158811432211..",
        "...11223411888555588811432211...",
        "...11122341118888881114322111...",
        "....111111111111111111111111....",
        "....111111221111111122111111....",
        "...11111122111111111122111111...",
        "...11112221111111111112221111...",
        "...111222111122222211112221111..",
        "...112221111222222221111222111..",
        "..11222111122333333221111222111.",
        "..11222111122331111332211112221.",
        "..112221111223311111133221111222",
        "..112211112231111111111322111122",
        "..112211112211111111111122111122",
        "..112211112111111111111112111122",
        "1111112211111111111......1111111",
        "1111112211111111111......1111111",
        "111111111111111111........111111",
        "111111111111111111........111111"
    };

    private static final String[] TEXTURE_KEY = {
        "......7777......",
        "....77....77....",
        "....77....77....",
        "......7777......",
        "......7777......",
        "........77......",
        "........77......",
        "........7777....",
        "........77......",
        "........7777....",
        "........77......",
        "................",
        "................",
        "................",
        "................",
        "................"
    };

    private static class SpriteInstance {
        double x;
        double y;
        int type;
        boolean frozen;
        double dist;

        public SpriteInstance(double x, double y, int type, boolean frozen, double dist) {
            this.x = x;
            this.y = y;
            this.type = type;
            this.frozen = frozen;
            this.dist = dist;
        }
    }

    private int getPaletteRGB(char c) {
        switch (c) {
            case '1': return 0x080808;
            case '2': return 0x181818;
            case '3': return 0x2A2A2A;
            case '4': return 0x444444;
            case '5': return 0x6A6A6A;
            case '6': return 0xFFFFFF;
            case '7': return 0xDDCC33;
            case '8': return 0xE4E1D5;
            case '9': return 0x333333;
            case 'A': return 0x880000;
            case 'B': return 0xFF1111;
            default: return -1;
        }
    }

    public void render(BufferedImage screenImage, Player player, GameMap map, ShadowEntity[] shadows, boolean renderEnemy) {
        double[] depthBuffer = new double[COLS];

        double playerX = player.getX();
        double playerY = player.getY();
        double playerA = player.getAngle();

        double camHeight = VIEW_ROWS / 2.0;

        double fSwayX = 0.0;
        double fSwayY = 0.0;

        int[] pixels = ((DataBufferInt) screenImage.getRaster().getDataBuffer()).getData();

        double[] vertLightLookup = new double[VIEW_ROWS];
        double[] ceilingDistLookup = new double[VIEW_ROWS];
        double[] floorDistLookup = new double[VIEW_ROWS];

        for (int y = 0; y < VIEW_ROWS; y++) {
            double vertDiff = ((double) y - camHeight) / VIEW_ROWS;
            vertLightLookup[y] = Math.max(0.0, Math.cos(vertDiff * 2.8));

            double ceilingRowRatio = (double) (camHeight - y) / (VIEW_ROWS / 2.0);
            if (ceilingRowRatio <= 0) ceilingRowRatio = 1e-4;
            ceilingDistLookup[y] = 0.8 / ceilingRowRatio;

            double floorRowRatio = (double) (y - camHeight) / (VIEW_ROWS / 2.0);
            if (floorRowRatio <= 0) floorRowRatio = 1e-4;
            floorDistLookup[y] = 0.8 / floorRowRatio;
        }

        for (int x = 0; x < COLS; x++) {
            double rayAngle = (playerA - FOV / 2.0) + ((double) x / (double) COLS) * FOV;

            double eyeX = Math.cos(rayAngle);
            double eyeY = Math.sin(rayAngle);
            if (eyeX == 0) eyeX = 1e-30;
            if (eyeY == 0) eyeY = 1e-30;

            int mapX = (int) playerX;
            int mapY = (int) playerY;

            double deltaDistX = Math.abs(1.0 / eyeX);
            double deltaDistY = Math.abs(1.0 / eyeY);

            int stepX;
            int stepY;
            double sideDistX;
            double sideDistY;

            if (eyeX < 0) {
                stepX = -1;
                sideDistX = (playerX - mapX) * deltaDistX;
            } else {
                stepX = 1;
                sideDistX = (mapX + 1.0 - playerX) * deltaDistX;
            }
            if (eyeY < 0) {
                stepY = -1;
                sideDistY = (playerY - mapY) * deltaDistY;
            } else {
                stepY = 1;
                sideDistY = (mapY + 1.0 - playerY) * deltaDistY;
            }

            boolean hitWall = false;
            int wallType = 0;
            int side = 0;

            while (!hitWall) {
                if (sideDistX < sideDistY) {
                    sideDistX += deltaDistX;
                    mapX += stepX;
                    side = 0;
                } else {
                    sideDistY += deltaDistY;
                    mapY += stepY;
                    side = 1;
                }

                if (mapX < 0 || mapX >= map.getWidth() || mapY < 0 || mapY >= map.getHeight()) {
                    hitWall = true;
                    wallType = 1;
                } else {
                    int cell = map.getCell(mapX, mapY);
                    if (cell > 0 && cell != 4 && cell != 6) {
                        hitWall = true;
                        wallType = cell;
                    }
                }
            }

            double perpWallDist;
            if (side == 0) {
                perpWallDist = (sideDistX - deltaDistX);
            } else {
                perpWallDist = (sideDistY - deltaDistY);
            }

            double straightDist = perpWallDist / Math.cos(rayAngle - playerA);
            if (straightDist < 0.1) straightDist = 0.1;

            double correctedDist = perpWallDist;
            if (correctedDist < 0.1) correctedDist = 0.1;

            depthBuffer[x] = correctedDist;

            int wallHeight = (int) (VIEW_ROWS / correctedDist);
            int ceiling = (int) (camHeight - wallHeight / 2.0);
            int floor = (int) (camHeight + wallHeight / 2.0);

            int ceilingClamped = Math.max(0, ceiling);
            int floorClamped = Math.min(VIEW_ROWS, floor);

            double exactWallHeight = (double) VIEW_ROWS / correctedDist;
            double exactCeiling = camHeight - exactWallHeight / 2.0;

            double hitFraction;
            if (side == 0) {
                double wallYCoord = playerY + straightDist * eyeY;
                hitFraction = wallYCoord - Math.floor(wallYCoord);
            } else {
                double wallXCoord = playerX + straightDist * eyeX;
                hitFraction = wallXCoord - Math.floor(wallXCoord);
            }

            double angleDiff = rayAngle - (playerA + fSwayX);
            double cosAngle = Math.cos(angleDiff);
            double spotlight = Math.pow(cosAngle, 12.0);

            for (int y = 0; y < ceilingClamped; y++) {
                double ceilingDist = ceilingDistLookup[y];
                double vertLight = vertLightLookup[y];

                double lightFactor = 0.08 + (spotlight * vertLight * (1.0 - (ceilingDist / 12.0)));
                if (lightFactor < 0.08) lightFactor = 0.08;
                if (lightFactor > 1.0) lightFactor = 1.0;

                int r = (int) (28 * lightFactor);
                int g = (int) (26 * lightFactor);
                int b = (int) (22 * lightFactor);
                pixels[y * COLS + x] = (r << 16) | (g << 8) | b;
            }

            boolean isWallType5 = (wallType == 5);
            boolean pointsRight = false;
            if (isWallType5) {
                double[] target = map.getTargetCellCenter(mapX, mapY);
                double dxArrow = target[0] - (mapX + 0.5);
                double dyArrow = target[1] - (mapY + 0.5);

                if (side == 0) {
                    pointsRight = (eyeX > 0) ? (dyArrow > 0) : (dyArrow < 0);
                } else {
                    pointsRight = (eyeY > 0) ? (dxArrow < 0) : (dxArrow > 0);
                }
            }

            for (int y = ceilingClamped; y < floorClamped; y++) {
                double wallY = (y - exactCeiling) / exactWallHeight;

                int baseR, baseG, baseB;

                if (wallType == 3) {
                    if (wallY > 0.92) {
                        baseR = 60; baseG = 10; baseB = 10;
                    } else if (hitFraction < 0.08 || hitFraction > 0.92 || wallY < 0.08) {
                        baseR = 100; baseG = 15; baseB = 15;
                    } else {
                        baseR = 240; baseG = 30; baseB = 30;
                    }
                } else {
                    double globalCoord = (side == 0) ? (playerY + straightDist * eyeY) : (playerX + straightDist * eyeX);
                    int stripeIndex = (int) Math.floor(globalCoord * 14.0);
                    if (stripeIndex % 2 == 0) {
                        baseR = 215; baseG = 200; baseB = 125;
                    } else {
                        baseR = 195; baseG = 180; baseB = 110;
                    }
                }

                if (isWallType5) {
                    boolean isArrow;
                    if (pointsRight) {
                        isArrow = (wallY > 0.45 && wallY < 0.53 && hitFraction > 0.25 && hitFraction < 0.55) ||
                                  (hitFraction >= 0.55 && hitFraction <= 0.75 && Math.abs(wallY - 0.49) < (0.75 - hitFraction) * 0.6);
                    } else {
                        isArrow = (wallY > 0.45 && wallY < 0.53 && hitFraction > 0.45 && hitFraction < 0.75) ||
                                  (hitFraction >= 0.25 && hitFraction <= 0.45 && Math.abs(wallY - 0.49) < (hitFraction - 0.25) * 0.6);
                    }

                    if (isArrow) {
                        baseR = 170; baseG = 20; baseB = 20;
                    }
                }

                double shadeFactor = (side == 0) ? 1.0 : 0.75;
                double vertLight = vertLightLookup[y];

                double lightFactor = 0.08 + (spotlight * vertLight * (1.0 - (straightDist / 12.0)));
                if (lightFactor < 0.08) lightFactor = 0.08;
                if (lightFactor > 1.0) lightFactor = 1.0;

                int r = (int) (baseR * lightFactor * shadeFactor);
                int g = (int) (baseG * lightFactor * shadeFactor);
                int b = (int) (baseB * lightFactor * shadeFactor);
                pixels[y * COLS + x] = (r << 16) | (g << 8) | b;
            }

            for (int y = floorClamped; y < VIEW_ROWS; y++) {
                double floorDist = floorDistLookup[y];
                double vertLight = vertLightLookup[y];

                double lightFactor = 0.08 + (spotlight * vertLight * (1.0 - (floorDist / 12.0)));
                if (lightFactor < 0.08) lightFactor = 0.08;
                if (lightFactor > 1.0) lightFactor = 1.0;

                int r = (int) (90 * lightFactor);
                int g = (int) (80 * lightFactor);
                int b = (int) (55 * lightFactor);
                pixels[y * COLS + x] = (r << 16) | (g << 8) | b;
            }
        }

        List<SpriteInstance> sprites = new ArrayList<>();
        
        if (renderEnemy && shadows != null) {
            for (ShadowEntity shadow : shadows) {
                double sdx = shadow.getX() - playerX;
                double sdy = shadow.getY() - playerY;
                double sdist = Math.sqrt(sdx * sdx + sdy * sdy);
                sprites.add(new SpriteInstance(shadow.getX(), shadow.getY(), shadow.getType(), shadow.isFrozen(), sdist));
            }
        }

        int px = (int) playerX;
        int py = (int) playerY;
        int range = 12;
        for (int y = Math.max(0, py - range); y < Math.min(map.getHeight(), py + range); y++) {
            for (int x = Math.max(0, px - range); x < Math.min(map.getWidth(), px + range); x++) {
                if (map.getCell(x, y) == 6) {
                    double kdx = (x + 0.5) - playerX;
                    double kdy = (y + 0.5) - playerY;
                    double kdist = Math.sqrt(kdx * kdx + kdy * kdy);
                    sprites.add(new SpriteInstance(x + 0.5, y + 0.5, 10, false, kdist));
                }
            }
        }

        sprites.sort((s1, s2) -> Double.compare(s2.dist, s1.dist));

        for (SpriteInstance sprite : sprites) {
            renderSprite(pixels, player, sprite, depthBuffer, camHeight, fSwayX, fSwayY, vertLightLookup);
        }
    }

    private void renderSprite(int[] pixels, Player player, SpriteInstance sprite, double[] depthBuffer, double camHeight, double fSwayX, double fSwayY, double[] vertLightLookup) {
        double dx = sprite.x - player.getX();
        double dy = sprite.y - player.getY();
        double dist = sprite.dist;

        double spriteAngle = Math.atan2(dy, dx) - player.getAngle();
        while (spriteAngle < -Math.PI) spriteAngle += 2 * Math.PI;
        while (spriteAngle >= Math.PI) spriteAngle -= 2 * Math.PI;

        if (dist < DEPTH && Math.abs(spriteAngle) < FOV) {
            double correctedDist = dist * Math.cos(spriteAngle);
            if (correctedDist < 0.1) correctedDist = 0.1;

            int spriteHeight = (int) (VIEW_ROWS / correctedDist);
            int spriteWidth = spriteHeight;

            if (sprite.type == 10) {
                spriteHeight = (int) (VIEW_ROWS / (correctedDist * 1.5));
                spriteWidth = spriteHeight;
            }

            int centerX = (int) ((COLS / 2.0) + (spriteAngle / (FOV / 2.0)) * (COLS / 2.0));

            int startX = Math.max(0, centerX - spriteWidth / 2);
            int endX = Math.min(COLS - 1, centerX + spriteWidth / 2);

            double floatOffset = 0.0;
            if (sprite.type == 10) {
                floatOffset = Math.sin(System.currentTimeMillis() * 0.006) * 0.08;
            }

            int ceiling = (int) (camHeight + (floatOffset * VIEW_ROWS) - spriteHeight / 2.0);
            if (sprite.type != 10) {
                int wallHeight = (int) (VIEW_ROWS / correctedDist);
                int floor = (int) (camHeight + wallHeight / 2.0);
                ceiling = floor - spriteHeight;
            }

            int floor = ceiling + spriteHeight;
            int ceilingClamped = Math.max(0, ceiling);
            int floorClamped = Math.min(VIEW_ROWS, floor);

            for (int sx = startX; sx <= endX; sx++) {
                if (correctedDist < depthBuffer[sx]) {
                    for (int y = ceilingClamped; y < floorClamped; y++) {
                        int rgbColor = -1;

                        if (sprite.type == 10) {
                            int texY = (int) (((double) (y - ceiling) / spriteHeight) * 16.0);
                            int texX = (int) (((double) (sx - (centerX - spriteWidth / 2)) / spriteWidth) * 16.0);
                            if (texX >= 0 && texX < 16 && texY >= 0 && texY < 16) {
                                char c = TEXTURE_KEY[texY].charAt(texX);
                                rgbColor = getPaletteRGB(c);
                            }
                        } else {
                            if (COLS <= 160) {
                                String[] texture;
                                if (sprite.frozen) {
                                    texture = TEXTURE_REAPER_STAND;
                                } else {
                                    long time = System.currentTimeMillis();
                                    boolean isFrame1 = (time / 220) % 2 == 0;
                                    texture = isFrame1 ? TEXTURE_REAPER_WALK_1 : TEXTURE_REAPER_WALK_2;
                                }
                                int texY = (int) (((double) (y - ceiling) / spriteHeight) * 16.0);
                                int texX = (int) (((double) (sx - (centerX - spriteWidth / 2)) / spriteWidth) * 16.0);
                                if (texX >= 0 && texX < 16 && texY >= 0 && texY < 16) {
                                    char c = texture[texY].charAt(texX);
                                    rgbColor = getPaletteRGB(c);
                                }
                            } else if (COLS <= 320) {
                                String[] texture;
                                if (sprite.frozen) {
                                    texture = TEXTURE_REAPER_32_STAND;
                                } else {
                                    long time = System.currentTimeMillis();
                                    boolean isFrame1 = (time / 220) % 2 == 0;
                                    texture = isFrame1 ? TEXTURE_REAPER_32_WALK_1 : TEXTURE_REAPER_32_WALK_2;
                                }
                                int texY = (int) (((double) (y - ceiling) / spriteHeight) * 32.0);
                                int texX = (int) (((double) (sx - (centerX - spriteWidth / 2)) / spriteWidth) * 32.0);
                                if (texX >= 0 && texX < 32 && texY >= 0 && texY < 32) {
                                    char c = texture[texY].charAt(texX);
                                    rgbColor = getPaletteRGB(c);
                                }
                            } else {
                                int[] texture;
                                if (sprite.frozen) {
                                    texture = texture64Stand;
                                } else {
                                    long time = System.currentTimeMillis();
                                    boolean isFrame1 = (time / 220) % 2 == 0;
                                    texture = isFrame1 ? texture64Walk1 : texture64Walk2;
                                }
                                int texY = (int) (((double) (y - ceiling) / spriteHeight) * 64.0);
                                int texX = (int) (((double) (sx - (centerX - spriteWidth / 2)) / spriteWidth) * 64.0);
                                if (texX >= 0 && texX < 64 && texY >= 0 && texY < 64) {
                                    rgbColor = texture[texY * 64 + texX];
                                }
                            }

                            if (sprite.frozen && rgbColor != -1) {
                                int pr = (rgbColor >> 16) & 0xFF;
                                int pg = (rgbColor >> 8) & 0xFF;
                                int pb = rgbColor & 0xFF;
                                int gray = (int) (0.299 * pr + 0.587 * pg + 0.114 * pb);
                                pr = gray;
                                pg = (int) (gray * 1.05);
                                if (pg > 255) pg = 255;
                                pb = (int) (gray * 0.95);
                                rgbColor = (pr << 16) | (pg << 8) | pb;
                            }
                        }

                        if (rgbColor != -1) {
                            double angleDiff = (player.getAngle() + spriteAngle) - (player.getAngle() + fSwayX);
                            double cosAngle = Math.cos(angleDiff);
                            double spotlight = Math.pow(cosAngle, 12.0);

                            double vertLight = vertLightLookup[y];

                            double lightFactor = 0.08 + (spotlight * vertLight * (1.0 - (dist / 12.0)));
                            if (lightFactor < 0.08) lightFactor = 0.08;
                            if (lightFactor > 1.0) lightFactor = 1.0;

                            int r = (int) (((rgbColor >> 16) & 0xFF) * lightFactor);
                            int g = (int) (((rgbColor >> 8) & 0xFF) * lightFactor);
                            int b = (int) ((rgbColor & 0xFF) * lightFactor);

                            pixels[y * COLS + sx] = (r << 16) | (g << 8) | b;
                        }
                    }
                }
            }
        }
    }
}
