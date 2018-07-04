import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.logging.Logger;

public class Response {
    /* Logger */
    private static Logger logger = Logger.getLogger("Response");
    /* user name & password supposed */
    private static final String user = "2242";
    private static final String pass = "396218";
    /* get system root dir */
    private static final String ROOT = System.getProperty("user.dir");
    /* add the content type */
    private static final HashMap<String, String> TYPE = new HashMap<>(9);

    static {
        TYPE.put("html", "text/html");
        TYPE.put("txt", "text/plain");
        TYPE.put("java", "text/plain");
        TYPE.put("jpg", "image/jpeg");
        TYPE.put("class", "text/plain");
        TYPE.put("png", "application/x-png");
        TYPE.put("xml", "text/xml");
        TYPE.put("log", "text/plain");
        TYPE.put("unknown", "text/plain");
    }

    /* response file */
    private File file;
    /* set the logical URI */
    private String URI;
    /* bind the request */
    private Request request;
    /* static resource dir */
    private static final String RES = ROOT + "/src/res/";

    private SocketChannel socketChannel;

    /**
     * bind with a socket channel & request, init URI
     *
     * @param socketChannel socket channel
     */
    public Response(SocketChannel socketChannel, Request request) {
        this.socketChannel = socketChannel;
        this.request = request;
    }


    /**
     * main function, try to respond
     *
     * @throws Exception
     */
    public void respond() throws Exception {
        URI = request.getHeaderMap().get("URI");
        ByteBuffer responseHeader;
        /* response message */
        String str;
        /* deal with login */
        if (URI.equals("/dopost")) {
            if (!request.getPara("login").equals(user)
                    || !request.getPara("pass").equals(pass)) {
                logger.info("login failed!");
                str = "HTTP/1.1 404 Bad Request\r\n Content-Type: text/html; charset=UTF-8\r\n\r\n";
            } else {
                logger.info("login success!");
                str = "HTTP/1.1 200 OK\r\n Content-Type: text/html; charset=UTF-8\r\n\r\n";
            }
            byte[] bytesHead = str.getBytes("UTF-8");
            responseHeader = ByteBuffer.allocate(bytesHead.length);
            responseHeader.put(bytesHead);
            handWrite(responseHeader);
        }

        /* visit the static html file */
        file = new File(RES, URI);
        if (file.exists() && !(file.isDirectory())) {
            String contentType = TYPE.get(getExtName());
            if (contentType == null) {
                contentType = TYPE.get("unknown");
            }
            str = "HTTP/1.1 200 OK\r\nContent-Type: " + contentType + "; charset=UTF-8\r\n\r\n";
            logger.info(str);

            byte[] headBytes = str.getBytes("UTF-8");
            responseHeader = ByteBuffer.allocate(headBytes.length);
            responseHeader.put(headBytes, 0, headBytes.length);
            handWrite(responseHeader);
            sendFile();
        }
        /* visit the root */
        else if (file.exists()) {
            if (file.isDirectory()) {
                sendCatalog();
            } else {
                String contentType = TYPE.get(getExtName());
                if (contentType == null) {
                    contentType = TYPE.get("unknown");
                }
                str = "HTTP/1.1 200 OK\r\nContent-Type: " + contentType + "; charset=UTF-8\r\n\r\n";
                logger.info(str);
                byte[] bytesHead = str.getBytes("UTF-8");
                responseHeader = ByteBuffer.allocate(bytesHead.length);
                responseHeader.put(bytesHead, 0, bytesHead.length);
                handWrite(responseHeader);
                sendFile();
            }
        }
        /* file doesn't exist */
        else {
            str = "HTTP/1.1 404 Bad Request\r\n Content-Type: text/html; charset=UTF-8\r\n\r\n";
            logger.info(str);
            byte[] bytesHead = str.getBytes("UTF-8");
            responseHeader = ByteBuffer.allocate(bytesHead.length);
            responseHeader.put(bytesHead);
            handWrite(responseHeader);
        }
    }

    /**
     * get the tail dex of one file
     *
     * @return unknown/tail dex
     */
    private String getExtName() {
        int length = URI.length();
        int index = URI.indexOf('.');
        if (index >= 0 && index < length) {
            return URI.substring(index + 1, length);
        }
        return "unknown";
    }

    /**
     * send file to the channel
     *
     * @throws Exception
     */
    private void sendFile() throws Exception {
        InputStream is = new FileInputStream(file);
        byte[] bytes = new byte[1024];
        ByteBuffer returnFile = ByteBuffer.allocate(1024);
        int numRead;
        while ((numRead = is.read(bytes, 0, 1024)) >= 0) {
            returnFile.clear();
            returnFile.put(bytes, 0, numRead);
            handWrite(returnFile);
        }
        is.close();
    }

    /**
     * deal with all the files under the dir
     *
     * @throws Exception
     */
    private void sendCatalog() throws Exception {
        File[] tempList = file.listFiles();
        StringBuilder returnString = new StringBuilder(64 + tempList.length * 64);
        returnString.append("HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=UTF-8\r\n\r\n<html><head><title>");
        returnString.append(URI);
        returnString.append("</title></head><body>目录下文件/文件夹个数:");
        returnString.append(tempList.length);
        int length = tempList.length;
        String parentPath = null;
        String rootPath = "WebHttpServer";
        if (length > 0) {
            parentPath = tempList[0].getParentFile().getName();
        }
        for (int i = 0; i < length; i++) {
            returnString.append("<li><a href=\"");
            if (!rootPath.equals(parentPath)) {
                returnString.append(parentPath + "/");
            }
            returnString.append(tempList[i].getName());
            returnString.append("\">");
            returnString.append(tempList[i].getPath());
            returnString.append("</a>");
        }
        returnString.append("</body></html>");
        byte[] bytes = returnString.toString().getBytes("UTF-8");
        ByteBuffer reaponseByte = ByteBuffer.allocate(bytes.length);
        reaponseByte.put(bytes);
        handWrite(reaponseByte);
    }

    /**
     * send the byte stream to the channel
     *
     * @param reaponseByte byte stream
     * @throws Exception
     */
    private void handWrite(ByteBuffer reaponseByte) throws Exception {
        reaponseByte.flip();
        while (reaponseByte.hasRemaining()) {
            socketChannel.write(reaponseByte);
        }
    }
}
