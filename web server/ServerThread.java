import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.logging.Logger;

public class ServerThread extends Thread {
    private static Logger logger = Logger.getLogger("ServerThread");

    /* which channel is used for this thread */
    private final SocketChannel socketChannel;

    /**
     * Construct with corresponding socket channel
     *
     * @param socketChannel get from Main Server
     */
    public ServerThread(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    /**
     * thread run function
     */
    public void run() {
        Request request = new Request(socketChannel);
        Response response = new Response(socketChannel, request);
        request.analyze();

        HashMap<String, String> headerMap = request.getHeaderMap();
        HashMap<String, String> parMap = request.getParaMap();

        System.out.println(headerMap);
        System.out.println(parMap);
        String uri = request.getHeader("URI");
        if (uri != null) {//防止心跳包
            try{
                response.respond();
            }catch (Exception e){
                logger.info("ERROR in response!");
            }
        }

    }
}
