import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
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
    private CascadeClassifier faceDetector;

    public VideoReceiver() {
        String xmlPath = "haarcascade_frontalface_alt.xml";
        this.faceDetector = new CascadeClassifier(xmlPath);

        if (faceDetector.empty()) {
            System.out.println("Error - Not able to load XML file.");
        }
    }

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

                        if (!frame.empty()) {
                            detectFaces(frame);
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

    private void detectFaces(Mat frame) {
        if (faceDetector.empty()) return;

        Mat grayFrame = new Mat();
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(grayFrame, grayFrame);

        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(grayFrame, faceDetections);

        for (Rect rect : faceDetections.toArray()) {
            Imgproc.rectangle(frame,
                    new Point(rect.x, rect.y),
                    new Point(rect.x + rect.width, rect.y + rect.height),
                    new Scalar(0, 255, 0), 3);
        }
    }

    private static BufferedImage matToBufferedImage(Mat matrix) {
        int cols = matrix.cols();
        int rows = matrix.rows();

        BufferedImage image = new BufferedImage(cols, rows, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                double[] data = matrix.get(y, x);
                int b = (int) data[0];
                int g = (int) data[1];
                int r = (int) data[2];
                int rgb = (r << 16) | (g << 8) | b;
                image.setRGB(x, y, rgb);
            }
        }
        return image;
    }

    public void stopServer() { running = false; }
    public BufferedImage getLatestFrame() { return latestFrame; }
}