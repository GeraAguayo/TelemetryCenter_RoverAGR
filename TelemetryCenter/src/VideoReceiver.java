import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;


public class VideoReceiver {
    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    private volatile BufferedImage latestFrame;
    private boolean running = false;

    public void startServer(int port) {
        running = true;
        new Thread(() -> {
            int maxLength = 65000;
            try (DatagramSocket socket = new DatagramSocket(port)) {
                byte[] buffer = new byte[maxLength];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                while (running) {
                    socket.receive(packet);
                    byte[] data = Arrays.copyOf(packet.getData(), packet.getLength());

                    if (data.length < 100) {
                        int packNums = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getInt();
                        ByteArrayOutputStream frameBuffer = new ByteArrayOutputStream();

                        for (int i = 0; i < packNums; i++) {
                            socket.receive(packet);
                            frameBuffer.write(packet.getData(), 0, packet.getLength());
                        }

                        byte[] frameData = frameBuffer.toByteArray();

                        MatOfByte mob = new MatOfByte(frameData);
                        Mat frame = Imgcodecs.imdecode(mob, Imgcodecs.IMREAD_COLOR);

                        if (frame.empty()) {
                            System.out.println("Frame couldn't be decoded");
                        } else {
                            Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2RGB);
                            Core.flip(frame, frame, 1);
                            latestFrame = matToBufferedImage(frame);
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void stopServer() {
        running = false;
    }

    private static BufferedImage matToBufferedImage(Mat matrix) {
        int type = BufferedImage.TYPE_3BYTE_BGR;
        byte[] data = new byte[matrix.width() * matrix.height() * (int) matrix.elemSize()];
        matrix.get(0, 0, data);
        BufferedImage image = new BufferedImage(matrix.width(), matrix.height(), type);
        image.getRaster().setDataElements(0, 0, matrix.width(), matrix.height(), data);
        return image;
    }

    public BufferedImage getLatestFrame() {
        return latestFrame;
    }

    public ImageIcon getLatestFrameAsIcon(int width, int height) {
        if (latestFrame == null) return null;
        Image scaled = latestFrame.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }
}
