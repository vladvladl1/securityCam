import javax.swing.*;
import java.io.IOException;
import java.io.Serializable;
import  java.net.DatagramPacket;
import java.net.DatagramSocket;


public class Frame implements Serializable {
    public byte[] bytes;
    public Frame(byte[] bytes)
    {
        this.bytes = bytes;
    }

    public int size()
    {
        return bytes.length;
    }
}
