import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.ServerSocket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.Socket;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import static org.opencv.imgproc.Imgproc.COLOR_BGRA2GRAY;
import static org.opencv.imgproc.Imgproc.cvtColor;

public class Camera extends JFrame {
    private Mat videoCap;
    private ServerSocket sersocket;
    private ServerSocket sersocketstring;
    private Socket socket;
    private Socket socketString;
    private String       ip;
    private int          port;
    private boolean      calling;



    private JLabel label;
    private ImageIcon icon;
    private VideoCapture capture;
    private Mat image;
    private Mat diffFrame;
    private Mat tamponFrame;
    private Mat image2;
    private Mat gri1;
    private Mat gri2;
    private ObjectOutputStream out = null;
    private ObjectOutputStream outString = null;
    private String salut = "salut";

    private boolean clicked = false, closed = false;

    public Camera() {
        setLayout(null);

        label = new JLabel();
        label.setBounds(0, 0, 640, 480);
        add(label);

        JButton btn = new JButton("capture");
        btn.setBounds(300, 480, 80, 40);
        add(btn);

        btn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                clicked = true;
            }
        });

        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing (WindowEvent e) {
                super.windowClosed(e);
                // capture.release();
                image.release();
                closed = true;
                System.out.println("closed");
                System.exit(0);

            }

            @Override
            public void windowDeactivated(WindowEvent e) {
                super.windowDeactivated(e);
                System.out.println("closed");
            }

        });

        setFocusable(false);
        setSize(640, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      //  setVisible(true);
    }

    public static void main(String[] args)  {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                Camera d = new Camera();
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            d.startCamera();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

    }
    public static ArrayList<Rect> detection_contours(Mat outmat) {
        Mat v = new Mat();
        Mat vv = outmat.clone();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(vv, contours, v, Imgproc.RETR_LIST,
                Imgproc.CHAIN_APPROX_SIMPLE);

        double maxArea = 100;
        int maxAreaIdx = -1;
        Rect r = null;
        ArrayList<Rect> rect_array = new ArrayList<Rect>();

        for (int idx = 0; idx < contours.size(); idx++) { Mat contour = contours.get(idx); double contourarea = Imgproc.contourArea(contour); if (contourarea > maxArea) {
            maxAreaIdx = idx;
            r = Imgproc.boundingRect(contours.get(maxAreaIdx));
            rect_array.add(r);
        }

        }

        v.release();

        return rect_array;

    }

    public void startCamera() throws IOException {
        int inp=0;
        capture = new VideoCapture(0);
        image = new Mat();
        image2 = new Mat();
        gri1 = new Mat();
        gri2 = new Mat();
        diffFrame = new Mat();
        tamponFrame = new Mat();

        sersocket = new ServerSocket(1234);
        sersocketstring = new ServerSocket(1235);
        socket = sersocket.accept();
        socketString = sersocketstring.accept();
        Frame f;
        out = new ObjectOutputStream(socket.getOutputStream());
        outString = new ObjectOutputStream(socketString.getOutputStream());

        int i=0;
        byte[] imageData;
        while (true) {
            capture.read(image);
            Mat outerBox = new Mat(image.size(), CvType.CV_8UC1);
            Imgproc.cvtColor(image, outerBox, Imgproc.COLOR_BGR2GRAY);
            Imgproc.GaussianBlur(outerBox, outerBox, new Size(3, 3), 0);


            if (i == 0) {

                diffFrame = new Mat(outerBox.size(), CvType.CV_8UC1);
                tamponFrame = new Mat(outerBox.size(), CvType.CV_8UC1);
                diffFrame = outerBox.clone();
            }

            if (i == 1) {
                Core.subtract(outerBox, tamponFrame, diffFrame);
                Imgproc.adaptiveThreshold(diffFrame, diffFrame, 255,
                        Imgproc.ADAPTIVE_THRESH_MEAN_C,
                        Imgproc.THRESH_BINARY_INV, 5, 2);
                   if(detection_contours(diffFrame).size()>0){
                       inp++;
                       if(inp>1) {
                           //JOptionPane.showMessageDialog(this, "S-a detectat miscare");
                           salut = "miscare";
                           System.out.println("miscare" + i);
                           inp = 0;
                       }
                   }
            }

            i = 1;
            tamponFrame = outerBox.clone();
            final MatOfByte buf = new MatOfByte();
            Imgcodecs.imencode(".jpg", image, buf);

            imageData = buf.toArray();

            f = new Frame(imageData);
            out.writeObject(f);

            outString.writeObject(salut);
            outString.flush();
            out.flush();
            salut = "salut";
            //icon = new ImageIcon(imageData);
           // label.setIcon(icon);



            if (clicked) {
                String name = JOptionPane.showInputDialog(this, "Enter image name");
                if(name == null) {
                    name = new SimpleDateFormat("yyyy-mm-dd-hh-mm-ss").format(new Date());
                }
                Imgcodecs.imwrite("images/" + name +".jpg", image);
                clicked = false;
            }
            if (closed) {
                break;
            }
        }
    }

}