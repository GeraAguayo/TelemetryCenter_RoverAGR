import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
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
    private CascadeClassifier bodyDetector;
    private CascadeClassifier faceDetector;

    public VideoReceiver() {
        String xmlPathBody = "haarcascade_fullbody.xml";
        String xmlPathFace = "haarcascade_frontalface_alt.xml";
        this.bodyDetector = new CascadeClassifier(xmlPathBody);
        this.faceDetector = new CascadeClassifier(xmlPathFace);

        if (bodyDetector.empty() && faceDetector.empty()) {
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
                            Core.flip(frame, frame, 1);
                            detectBody(frame);
                            detectFace(frame);
                            latestFrame = matToBufferedImage(frame);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void detectBody(Mat frame) {
        if (bodyDetector.empty()) return;
        Mat grayFrame = new Mat();
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(grayFrame, grayFrame);
        MatOfRect faceDetections = new MatOfRect();
        bodyDetector.detectMultiScale(grayFrame, faceDetections, 1.1, 3, 0, new Size(30, 70), new Size());

        for (Rect rect : faceDetections.toArray()) {
            Imgproc.rectangle(frame,
                    new Point(rect.x, rect.y),
                    new Point(rect.x + rect.width, rect.y + rect.height),
                    new Scalar(0, 255, 0), 3);

            Imgproc.putText(frame, "TARGET", new Point(rect.x, rect.y - 10),
                    Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 255, 0), 2);
        }
    }

    private void detectFace(Mat frame) {
        if (faceDetector.empty()) return;
        Mat grayFrame = new Mat();
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(grayFrame, grayFrame);
        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(grayFrame, faceDetections);
        //bodyDetector.detectMultiScale(grayFrame, faceDetections, 1.1, 3, 0, new Size(30, 70), new Size());

        for (Rect rect : faceDetections.toArray()) {
            Imgproc.rectangle(frame,
                    new Point(rect.x, rect.y),
                    new Point(rect.x + rect.width, rect.y + rect.height),
                    new Scalar(0, 0, 255), 3);

            Imgproc.putText(frame, "TARGET", new Point(rect.x, rect.y - 10),
                    Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 0, 255), 2);
        }
    }

    private static BufferedImage matToBufferedImage(Mat matrix){
        int width = matrix.cols();
        int height = matrix.rows();
        int channels = matrix.channels();

        byte[] sourcePixels = new byte[width * height * channels];
        matrix.get(0,0,sourcePixels);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);

        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(sourcePixels, 0 , targetPixels, 0 , sourcePixels.length);

        return image;
    }

    public void stopServer() { running = false; }
    public BufferedImage getLatestFrame() { return latestFrame; }
}