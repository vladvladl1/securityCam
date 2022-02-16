import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.io.IOException;
public class Client extends JFrame{


    public JLabel label;
    public ImageIcon icon;
    private Socket       socket;
    private Socket       socketString;
    private String       ip= "127.0.0.1";
    private int          port=1234;
    private boolean bool = true;
    private ByteArrayOutputStream boss = new ByteArrayOutputStream();
    private ObjectInputStream oin;
    private ObjectInputStream stringoin;
    Frame f;

    public Client(){
        setLayout(null);
        label = new JLabel();
        label.setBounds(0, 0, 640, 480);
        add(label);
        setFocusable(false);
        setSize(640, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                Client d = new Client();
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            d.go();
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });



    }
    public void go() throws IOException, ClassNotFoundException {
        socket = new Socket(ip,port);
        socket.setSoTimeout(5000);
        socketString = new Socket(ip,1235);
        socketString.setSoTimeout(5000);
        InputStream instring = socketString.getInputStream();
        InputStream in = socket.getInputStream();
        oin = new ObjectInputStream(in);
        stringoin = new ObjectInputStream(instring);
        while (bool){
            String s = stringoin.readObject().toString();
            if(s.equals("miscare")){
                JOptionPane.showMessageDialog(this, "S-a detectat miscare");
            }
            f = (Frame) oin.readObject();
            icon = new ImageIcon(f.bytes);
            label.setIcon(icon);
        }
    }


}
