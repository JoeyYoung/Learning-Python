import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * HttpSever Main Class
 */
public class HttpServer {
    /* server port 2242 */
    private final int serverPort;
    /* Log Recorder */
    private static Logger logger = Logger.getLogger("ServerLog");
    /* socket channel */
    private ServerSocketChannel ssc;
    /* manager selector */
    private Selector selector;
    /* threads pool */
    private ExecutorService threadsPool;
    /* server works normally */
    private boolean running;

    /**
     * constructor with port 2242 (3150102242)
     * @param serverPort 2242
     */
    private HttpServer(int serverPort) {
        this.serverPort = serverPort;
        running = true;
    }

    /**
     * main entry of the program
     * @param args ins
     */
    public static void main(String[] args) {
        new HttpServer(2242).start();
    }

    /**
     * Sever Start Function
     */
    private void start() {
        /* init socket and pool configure */
        try {
            /* build the socket channel */
            ssc = ServerSocketChannel.open();
            selector = Selector.open();

            /* get the channel according socket and bind port */
            ServerSocket socket = ssc.socket();
            socket.bind(new InetSocketAddress(serverPort));

            /* set the channel to be unblock */
            ssc.configureBlocking(false);
            /* register a selector to manager all channels */
            ssc.register(selector, SelectionKey.OP_ACCEPT);
            /* set threads pool to contain total 8 web requests */
            threadsPool = Executors.newFixedThreadPool(8);
        } catch (Exception e) {
            logger.info("ERROR in Server Start");
        }
        logger.info("Success init server!");

        /* Main listen threads */
        new MainListener().start();

        /* loop to wait for a request */
        try {
            while (running) {
                logger.info("Waiting for a connection!");
            /* get all interested accept ready channel */
                if (selector.select() > 0) {
                    logger.info("Channel ready!");
                    /* Set of ready channel */
                    Set<SelectionKey> keys = selector.selectedKeys();
                    Iterator<SelectionKey> itor = keys.iterator();
                    while (itor.hasNext()) {
                        SelectionKey key = itor.next();
                        /* ready to accept */
                        if (key.isAcceptable()) {
                            ServerSocketChannel ssc_t = (ServerSocketChannel) key.channel();
                            /* get the linked client socket channel */
                            SocketChannel sc = ssc_t.accept();
                            if (sc != null) {
                                InetSocketAddress isa = (InetSocketAddress) sc.getRemoteAddress();
                                String host = isa.getHostString();
                                logger.info("Receive connection request from " + host);

                                /* configure the socket channel in client */
                                sc.configureBlocking(false);
                                /* interested in ready to reading data from the channel */
                                sc.register(selector, SelectionKey.OP_READ);
                            }
                        } else if (key.isReadable()) {
                            logger.info("Channel readable!");
                            SocketChannel sc = (SocketChannel) key.channel();
                            /* cancel the read event */
                            key.interestOps(key.interestOps() & (~SelectionKey.OP_READ));
                            /* new server sub thread for this channel */
                            threadsPool.execute(new ServerThread(sc));
                        }
                        itor.remove();
                    } //while
                }
            }
        } catch (Exception e) {
            logger.info("ERROR in waiting requests!");
        }
        logger.info("Server Quit!");
        threadsPool.shutdown();
    }

    /**
     * The Main thread
     * control the exit of the server
     */
    private class MainListener extends Thread {
        public void run() {
            BufferedReader bufin = new BufferedReader(new InputStreamReader(System.in));
            while (running) {
                logger.info("Main Thread Start to listen!");
                System.out.println("Enter quit to stop the server!");
                try {
                    String sin = bufin.readLine();
                    if (sin.equals("quit")) {
                        running = false;
                        /* make the selector to be free from block */
                        selector.wakeup();
                    }
                } catch (IOException e) {
                    logger.info("ERROR to read instruction!");
                }
            }
        }
    }
}
