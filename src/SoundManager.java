import javax.sound.sampled.*;

public class SoundManager {
    
    private static void write16BitSample(byte[] buffer, int offset, double sampleVal) {
        short val = (short) (Math.max(-1.0, Math.min(1.0, sampleVal)) * 32767.0);
        buffer[offset] = (byte) (val & 0xFF);
        buffer[offset + 1] = (byte) ((val >> 8) & 0xFF);
    }

    public static void playHeartbeat(float volume) {
        new Thread(() -> {
            try {
                int sampleRate = 8000;
                int duration = 3200;
                byte[] buffer = new byte[duration * 2];
                
                for (int i = 0; i < duration; i++) {
                    double t = (double) i / sampleRate;
                    double amp = 0.0;
                    if (t >= 0.0 && t < 0.12) {
                        double envelope = Math.sin(Math.PI * (t / 0.12));
                        amp = Math.sin(2.0 * Math.PI * 55.0 * t) * envelope;
                    } else if (t >= 0.18 && t < 0.30) {
                        double envelope = Math.sin(Math.PI * ((t - 0.18) / 0.12));
                        amp = Math.sin(2.0 * Math.PI * 48.0 * (t - 0.18)) * envelope;
                    }
                    write16BitSample(buffer, i * 2, amp * volume * 0.9);
                }
                AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
                SourceDataLine line = AudioSystem.getSourceDataLine(format);
                line.open(format);
                line.start();
                line.write(buffer, 0, buffer.length);
                line.drain();
                line.close();
            } catch (Exception e) {
            }
        }).start();
    }

    public static void playScreech() {
        new Thread(() -> {
            try {
                int sampleRate = 22050;
                int duration = sampleRate * 15 / 10;
                byte[] buffer = new byte[duration * 2];
                java.util.Random rand = new java.util.Random();
                
                for (int i = 0; i < duration; i++) {
                    double t = (double) i / sampleRate;
                    double tone1 = Math.sin(2.0 * Math.PI * 850.0 * t);
                    double tone2 = Math.sin(2.0 * Math.PI * 960.0 * t);
                    double eas = (tone1 + tone2) * 0.4;
                    double noise = (rand.nextDouble() * 2.0 - 1.0) * 0.3;
                    double mixed = Math.max(-1.0, Math.min(1.0, (eas + noise) * 2.0));
                    write16BitSample(buffer, i * 2, mixed * 0.75);
                }
                
                AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
                SourceDataLine line = AudioSystem.getSourceDataLine(format);
                line.open(format);
                line.start();
                line.write(buffer, 0, buffer.length);
                line.drain();
                line.close();
            } catch (Exception e) {
            }
        }).start();
    }

    public static void playKeyCollect() {
        new Thread(() -> {
            try {
                int sampleRate = 8000;
                int duration = 3600;
                byte[] buffer = new byte[duration * 2];
                for (int i = 0; i < duration; i++) {
                    double t = (double) i / sampleRate;
                    double freq = 523.25;
                    if (t > 0.12 && t <= 0.24) freq = 659.25;
                    if (t > 0.24) freq = 783.99;
                    double signal = Math.sin(2.0 * Math.PI * freq * t);
                    double envelope = Math.max(0.0, 1.0 - (t / 0.45));
                    write16BitSample(buffer, i * 2, signal * envelope * 0.4);
                }
                AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
                SourceDataLine line = AudioSystem.getSourceDataLine(format);
                line.open(format);
                line.start();
                line.write(buffer, 0, buffer.length);
                line.drain();
                line.close();
            } catch (Exception e) {
            }
        }).start();
    }

    public static void playDoorOpen() {
        new Thread(() -> {
            try {
                int sampleRate = 8000;
                int duration = 8000;
                byte[] buffer = new byte[duration * 2];
                java.util.Random rand = new java.util.Random();
                for (int i = 0; i < duration; i++) {
                    double t = (double) i / sampleRate;
                    double freq = 50.0 + rand.nextDouble() * 30.0;
                    double signal = Math.sin(2.0 * Math.PI * freq * t);
                    double envelope = Math.sin(Math.PI * t);
                    write16BitSample(buffer, i * 2, signal * envelope * 0.6);
                }
                AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
                SourceDataLine line = AudioSystem.getSourceDataLine(format);
                line.open(format);
                line.start();
                line.write(buffer, 0, buffer.length);
                line.drain();
                line.close();
            } catch (Exception e) {
            }
        }).start();
    }

    public static void playHeavyBreathing(float volume) {
        new Thread(() -> {
            try {
                int sampleRate = 8000;
                int duration = sampleRate * 15 / 10;
                byte[] buffer = new byte[duration * 2];
                java.util.Random rand = new java.util.Random();
                for (int i = 0; i < duration; i++) {
                    double t = (double) i / sampleRate;
                    double env = 0.0;
                    double centerFreq = 300.0;
                    if (t < 0.6) {
                        env = Math.sin(Math.PI * (t / 0.6)) * 0.7;
                        centerFreq = 250.0 + 100.0 * (t / 0.6);
                    } else if (t >= 0.7 && t < 1.4) {
                        double et = t - 0.7;
                        env = Math.sin(Math.PI * (et / 0.7)) * 0.9;
                        centerFreq = 350.0 - 150.0 * (et / 0.7);
                    }
                    
                    double noise = rand.nextDouble() * 2.0 - 1.0;
                    double resonance = Math.sin(2.0 * Math.PI * centerFreq * t);
                    double amp = noise * resonance * env;
                    
                    write16BitSample(buffer, i * 2, amp * volume * 0.35);
                }
                AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
                SourceDataLine line = AudioSystem.getSourceDataLine(format);
                line.open(format);
                line.start();
                line.write(buffer, 0, buffer.length);
                line.drain();
                line.close();
            } catch (Exception e) {
            }
        }).start();
    }

    public static void playPlayerFootstep() {
        new Thread(() -> {
            try {
                int sampleRate = 8000;
                int duration = 800;
                byte[] buffer = new byte[duration * 2];
                java.util.Random rand = new java.util.Random();
                for (int i = 0; i < duration; i++) {
                    double t = (double) i / sampleRate;
                    double noise = rand.nextDouble() * 2.0 - 1.0;
                    double thud = Math.sin(2.0 * Math.PI * 55.0 * t);
                    double env = Math.exp(-40.0 * t);
                    write16BitSample(buffer, i * 2, (thud * 0.5 + noise * 0.3) * env * 0.04);
                }
                AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
                SourceDataLine line = AudioSystem.getSourceDataLine(format);
                line.open(format);
                line.start();
                line.write(buffer, 0, buffer.length);
                line.drain();
                line.close();
            } catch (Exception e) {
            }
        }).start();
    }

    public static void playMonsterFootstep(float volume) {
        new Thread(() -> {
            try {
                int sampleRate = 8000;
                int duration = 2000;
                byte[] buffer = new byte[duration * 2];
                java.util.Random rand = new java.util.Random();
                for (int i = 0; i < duration; i++) {
                    double t = (double) i / sampleRate;
                    double noise = (rand.nextDouble() * 2.0 - 1.0) * 0.20;
                    double thud = Math.sin(2.0 * Math.PI * 40.0 * t) * 0.75;
                    double echo = Math.sin(2.0 * Math.PI * 130.0 * t) * 0.3 * Math.exp(-12.0 * t);
                    double env = Math.exp(-18.0 * t);
                    write16BitSample(buffer, i * 2, (thud + echo + noise) * env * volume * 0.85);
                }
                AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
                SourceDataLine line = AudioSystem.getSourceDataLine(format);
                line.open(format);
                line.start();
                line.write(buffer, 0, buffer.length);
                line.drain();
                line.close();
            } catch (Exception e) {
            }
        }).start();
    }

    public static void playBlinkSound() {
        new Thread(() -> {
            try {
                int sampleRate = 16000;
                int duration = sampleRate * 15 / 100;
                byte[] buffer = new byte[duration * 2];
                java.util.Random rand = new java.util.Random();
                for (int i = 0; i < duration; i++) {
                    double t = (double) i / sampleRate;
                    double noise = rand.nextDouble() * 2.0 - 1.0;
                    double env = Math.sin(Math.PI * (t / 0.15)) * Math.exp(-25.0 * t);
                    double sample = noise * env * 0.12;
                    write16BitSample(buffer, i * 2, sample);
                }
                AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
                SourceDataLine line = AudioSystem.getSourceDataLine(format);
                line.open(format);
                line.start();
                line.write(buffer, 0, buffer.length);
                line.drain();
                line.close();
            } catch (Exception e) {
            }
        }).start();
    }

    private static SourceDataLine musicLine = null;
    private static volatile boolean musicRunning = false;

    public static synchronized void startAmbientMusic() {
        if (musicRunning) return;
        musicRunning = true;
        new Thread(() -> {
            try {
                int sampleRate = 22050;
                AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
                musicLine = AudioSystem.getSourceDataLine(format);
                musicLine.open(format, 8192);
                musicLine.start();

                byte[] buffer = new byte[2048];
                double phase1 = 0;
                double phase2 = 0;
                double phase3 = 0;
                double lfoPhase = 0;

                while (musicRunning) {
                    for (int i = 0; i < 1024; i++) {
                        double f1 = 55.0;
                        double f2 = 65.4;
                        double f3 = 41.2;

                        double lfo1 = 0.5 + 0.3 * Math.sin(lfoPhase * 0.00008);
                        double lfo2 = 0.4 + 0.3 * Math.cos(lfoPhase * 0.00004);
                        double lfo3 = 0.3 + 0.2 * Math.sin(lfoPhase * 0.00002);

                        double sample = (Math.sin(phase1) * lfo1 + 
                                         Math.sin(phase2) * lfo2 + 
                                         Math.sin(phase3) * lfo3) / 3.0;

                        double noise = (Math.random() * 2.0 - 1.0) * 0.04;
                        sample += noise;

                        write16BitSample(buffer, i * 2, sample * 0.15);

                        phase1 += 2.0 * Math.PI * f1 / sampleRate;
                        phase2 += 2.0 * Math.PI * f2 / sampleRate;
                        phase3 += 2.0 * Math.PI * f3 / sampleRate;
                        lfoPhase += 1.0;
                    }
                    musicLine.write(buffer, 0, buffer.length);
                    Thread.sleep(5);
                }
            } catch (Exception e) {
            }
        }).start();
    }

    public static synchronized void stopAmbientMusic() {
        musicRunning = false;
        if (musicLine != null) {
            try {
                musicLine.stop();
                musicLine.close();
            } catch (Exception e) {}
            musicLine = null;
        }
    }
}
