import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.HashSet;

public class Index_Storage_Barrel implements Runnable {
    HashMap<String, HashSet<String>> wordStorageBarrel;
    HashMap<String, HashSet<String>> referencesStorageBarrel;

    MulticastSocket socket;
    InetAddress group;

    public Index_Storage_Barrel() throws Exception {
        socket = new MulticastSocket(8080);
        group = InetAddress.getByName("127.0.0.1");
        socket.joinGroup(group);
    }

    public HashMap<String, HashSet<String>> getWordStorageBarrel() {
        synchronized (wordStorageBarrel) {
            return wordStorageBarrel;
        }
    }

    public void setWordStorageBarrel(HashMap<String, HashSet<String>> wordStorageBarrel) {
        synchronized (wordStorageBarrel) {
            this.wordStorageBarrel = wordStorageBarrel;
        }
    }

    public HashMap<String, HashSet<String>> getReferencesStorageBarrel() {
        synchronized (referencesStorageBarrel) {
            return referencesStorageBarrel;
        }
    }

    public void setReferencesStorageBarrel(HashMap<String, HashSet<String>> referencesStorageBarrel) {
        synchronized (referencesStorageBarrel) {
            this.referencesStorageBarrel = referencesStorageBarrel;
        }
    }

    public MulticastSocket getSocket() {
        return socket;
    }

    public void setSocket(MulticastSocket socket) {
        this.socket = socket;
    }

    public InetAddress getGroup() {
        return group;
    }

    public void setGroup(InetAddress group) {
        this.group = group;
    }

    public void addWord(String url, String word) {
        synchronized (wordStorageBarrel) {
            if (wordStorageBarrel.containsKey(word)) {
                wordStorageBarrel.get(word).add(url);
            } else {
                HashSet<String> urls = new HashSet<>();
                urls.add(url);
                wordStorageBarrel.put(word, urls);
            }
        }
    }

    public void addLink(String currUrl, String pointedUrl) {
        synchronized (referencesStorageBarrel) {
            if (referencesStorageBarrel.containsKey(pointedUrl)) {
                referencesStorageBarrel.get(pointedUrl).add(currUrl);
            } else {
                HashSet<String> urls = new HashSet<>();
                urls.add(currUrl);
                referencesStorageBarrel.put(pointedUrl, urls);
            }
        }
    }

    public void run() {
        byte[] buffer = new byte[1024];
        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String message = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Received message: " + message);
        }

    }

    public static void main(String[] args) throws Exception {
        Index_Storage_Barrel isb = new Index_Storage_Barrel();
        Thread t = new Thread(isb);
        t.start();
    }
}
