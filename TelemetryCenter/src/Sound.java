import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Sound {

    private static final ExecutorService soundPool = Executors.newCachedThreadPool();

    static void play_beep_high() throws LineUnavailableException {
        int sampleRate = 44100; // CD Quality
        double frequency = 550.0; // Note A4
        double durationSeconds = 0.5;
        int numSamples = (int) (sampleRate * durationSeconds);
        byte[] buffer = new byte[numSamples];

        // Generate a Sine Wave
        for (int i = 0; i < numSamples; i++) {
            double angle = 2.0 * Math.PI * i / (sampleRate / frequency);
            buffer[i] = (byte) (Math.sin(angle) * 127); // Scale to byte range
        }

        // Setup Audio Format: 44100Hz, 8-bit, Mono, Signed, BigEndian
        AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, true);
        SourceDataLine line = AudioSystem.getSourceDataLine(format);

        line.open(format);
        line.start();
        line.write(buffer, 0, buffer.length);
        line.drain(); // Wait for it to finish playing
        line.close();
    }

    static void play_beep_low() throws LineUnavailableException {
        int sampleRate = 44100; // CD Quality
        double frequency = 300.0; // Note A4
        double durationSeconds = 0.5;
        int numSamples = (int) (sampleRate * durationSeconds);
        byte[] buffer = new byte[numSamples];

        // Generate a Sine Wave
        for (int i = 0; i < numSamples; i++) {
            double angle = 2.0 * Math.PI * i / (sampleRate / frequency);
            buffer[i] = (byte) (Math.sin(angle) * 127); // Scale to byte range
        }

        // Setup Audio Format: 44100Hz, 8-bit, Mono, Signed, BigEndian
        AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, true);
        SourceDataLine line = AudioSystem.getSourceDataLine(format);

        line.open(format);
        line.start();
        line.write(buffer, 0, buffer.length);
        line.drain(); // Wait for it to finish playing
        line.close();
    }

    static void play_beep_mid() throws LineUnavailableException {
        int sampleRate = 44100; // CD Quality
        double frequency = 400.0; // Note A4
        double durationSeconds = 0.5;
        int numSamples = (int) (sampleRate * durationSeconds);
        byte[] buffer = new byte[numSamples];

        // Generate a Sine Wave
        for (int i = 0; i < numSamples; i++) {
            double angle = 2.0 * Math.PI * i / (sampleRate / frequency);
            buffer[i] = (byte) (Math.sin(angle) * 127); // Scale to byte range
        }

        // Setup Audio Format: 44100Hz, 8-bit, Mono, Signed, BigEndian
        AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, true);
        SourceDataLine line = AudioSystem.getSourceDataLine(format);

        line.open(format);
        line.start();
        line.write(buffer, 0, buffer.length);
        line.drain(); // Wait for it to finish playing
        line.close();
    }

    static void packet_received() throws LineUnavailableException {
        soundPool.execute(()->{
                    try{
                        play_beep_high();
                    } catch (LineUnavailableException e){
                        System.err.println("Audio error: " + e.getMessage());
                    }
                }

        );
    }

    static void play_disconnected() throws LineUnavailableException {
        soundPool.execute(()->{
                    try{
                        play_beep_mid();
                        play_beep_low();
                        play_beep_mid();
                        play_beep_low();
                    } catch (LineUnavailableException e){
                        System.err.println("Audio error: " + e.getMessage());
                    }
                }

        );
    }
}
