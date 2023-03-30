import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Set;
import java.util.StringTokenizer;

public class Downloader implements Runnable {
    private boolean isAvailable;
    // private URL_Queue q;
    private Set<String> visited;
    private int myValue;
    private MulticastSocket socket;
    private InetAddress group;

    public Downloader( Set<String> visited, int myValue) throws Exception {
        
        this.visited = visited;
        this.isAvailable = true;
        this.myValue = myValue;
        this.socket = new MulticastSocket(4446);
        this.group = InetAddress.getByName("230.0.0.0");
        this.socket.joinGroup(group);
    }

    public boolean getIsAvailable() {
        synchronized (this) {
            return isAvailable;
        }
    }

   /*public URL_Queue getQ() {
        synchronized (q) {
            return q;
        }
    }*/

    public Set<String> getVisited() {
        synchronized (visited) {
            return visited;
        }
    }

    public int getMyValue() {
        synchronized (this) {
            return myValue;
        }
    }

    public void setIsAvailable(boolean isAvailable) {
        synchronized (this) {
            this.isAvailable = isAvailable;
        }
    }

    /*public void setQ(URL_Queue q) {
        synchronized (q) {
            this.q = q;
        }
    }*/

    public void setVisited(Set<String> visited) {
        synchronized (visited) {
            this.visited = visited;
        }
    }

    public void setMyValue(int myValue) {
        synchronized (this) {
            this.myValue = myValue;
        }
    }

    public void run() {
        String url = null;

        setIsAvailable(false);

        System.out.println("Thread " + getMyValue() + ": Downloader à espera!");

        int i = 0;

        while (++i < 10) {
                try {
                    socket.leaveGroup(group);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                
                socket.close();
              
                return;
            }
        

        if (i == 10) {
            System.out.println("Thread " + getMyValue() +
                    ": Downloader acabou de correr por falta de urls!");
            setIsAvailable(true);
            //q.setRunFlag(false);
            return;
        }
/*
        url = q.remove();

        synchronized (visited) {
            if (visited.contains(url)) {
                setIsAvailable(true);
                return;
            }

            visited.add(url);
        }
*/
        System.out.println("Thread " + getMyValue() + ": A analisar o url ");

        try {
            Document doc = Jsoup.connect(url).ignoreHttpErrors(true).get();

            Elements links = doc.select("a[href]");

            for (Element link : links) {

                synchronized (visited) {
                    if (!visited.contains(link.attr("abs:href"))) {
                        //q.add(link.attr("abs:href"));

                        String pointedUrl = "ORIGINAL_LINK | " + url + " | POINTED_LINK | " + link.attr("abs:href");
                        byte[] buffer = pointedUrl.getBytes();
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, 4446);
                        socket.send(packet);

                        System.out.println(pointedUrl);
                    }
                }
            }

            StringTokenizer tokens = new StringTokenizer(doc.text());

            if (url != "https://mooshak.dei.uc.pt/") {
                while (tokens.hasMoreElements()) {
                    String word = "ORIGINAL_LINK | " + url + " | WORD | " + tokens.nextToken().toLowerCase();
                    byte[] buffer = word.getBytes();
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, 8080);
                    socket.send(packet);
                    System.out.println(word);
                }
            }

        } catch (IOException e) {
            // e.printStackTrace();
            setIsAvailable(true);
            return;
        }
    }
}
