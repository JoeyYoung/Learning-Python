import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Deal with the request information
 * Corresponding to certain channel
 */
public class Request {
    /* Logger */
    private static Logger logger = Logger.getLogger("Request");
    /* request from the channel */
    private final SocketChannel socketChannel;
    /* store the header name-value information */
    private HashMap<String, String> headerMap = new HashMap<>();
    /* store the body name-value information */
    private HashMap<String, String> paraMap = new HashMap<>();

    /* analysis status */
    private boolean requestLineStatus;
    private boolean headerStatus;
    private boolean bodyStatus;

    /**
     * Constructed from this channel
     *
     * @param socketChannel assigned from server thread
     */
    public Request(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
        requestLineStatus = false;
        headerStatus = false;
        bodyStatus = false;
    }

    /**
     * top- method uri version
     * header- name:value
     * body- name=value
     */
    public void analyze() {
        logger.info("ENTER Request analysis!");
        /* create a buffer area with 1024 */
        ByteBuffer byteBuf = ByteBuffer.allocate(1024);
        ByteCarrier byteCarrier = new ByteCarrier();
        /* bytes read from channel to buffer */
        int bytesLen;

        /* keep reading */
        try {
            while (socketChannel.isOpen() && (bytesLen = socketChannel.read(byteBuf)) > 0) {
                /* move the data in buffer to the carrier */
                byteBuf.flip();
                byteCarrier.fill(byteBuf, bytesLen);
                byteBuf.clear();
                if (!requestLineStatus) {
                    /* extract the requestLine */
                    extractRequestLine(byteCarrier);
                }
                if (requestLineStatus && !headerStatus) {
                    /* extract the http header */
                    extractHeader(byteCarrier);
                }
                if (headerStatus && !bodyStatus) {
                    /* extract the http body */
                    extractBody(byteCarrier);
                }
            }
        } catch (Exception e) {
            logger.info("ERROR in Request read!");
        }
    }

    /**
     * Extract Request Line with format METHOD URI VERSION
     * Separate with \s-ascii 32, End with \r\n-ascii 13
     * Set the requestLine analysis status
     *
     * @param byteCarrier request information to analysis
     */
    private void extractRequestLine(ByteCarrier byteCarrier) {
        logger.info("Begin Analysis Request Line!");

        /* single byte */
        byte b;
        /* length read in (for each part in this line) */
        int len = 0;
        /* last position in carrier */
        int lastPos = 0;
        /* split character */
        byte lineEnd = (byte) 13;
        byte space = (byte) 32;

        /* still read in the request line information */
        while (byteCarrier.notEnd() && (b = byteCarrier.getOne()) != lineEnd) {
            /* valid byte, length inc */
            if (b != space) {
                len++;
            }
            /* meet space */
            else {
                /* method */
                if (getHeader("Method") == null) {
                    String sMethod = new String(byteCarrier.getBytes(), 0, len);
                    headerMap.put("Method", sMethod);
                    lastPos = byteCarrier.getPosition();
                    logger.info("Method read!");
                }
                /* uri */
                else {
                    extractURI(byteCarrier, lastPos, len);
                    lastPos = byteCarrier.getPosition();
                    logger.info("URI read!");
                }
                len = 0;
            }
        }
        /* Http Version, if done, update status */
        if (!byteCarrier.notEnd()) {
            byteCarrier.setPosition(lastPos);
            requestLineStatus = false;
            logger.info("Failed in analyze Request Line!");
        } else {
            String version = new String(byteCarrier.getBytes(), lastPos, len);
            headerMap.put("HttpVersion", version);
            requestLineStatus = true;
            logger.info("Http Version read!");
        }
    }

    /**
     * Extract information from URI, Call ParaMap Function if has form
     *
     * @param byteCarrier bytes to read in
     * @param offset      the begin position
     * @param length      total length of the field
     */
    private void extractURI(ByteCarrier byteCarrier, int offset, int length) {
        logger.info("Begin Analysis URI!");

        byte[] bytes = byteCarrier.getBytes();
        int len = 0;
        boolean hasForm = false;
        /* ? begin of form */
        byte quesMark = (byte) 63;

        int index;
        for (index = offset; index < offset + length; index++) {
            /* valid char before ? */
            if (bytes[index] != quesMark) {
                len++;
            }
            /* meet ? */
            else {
                try {
                    headerMap.put("URI", URLDecoder.decode(new String(bytes, offset, len), "UTF-8"));
                } catch (Exception e) {
                    logger.info("ERROR in URL read!");
                }
                offset = offset + len + 1;
                len = length - len - 1;
                hasForm = true;
                break;
            }
        } // for

        /* bytes end || meet ? break */
        if (hasForm) {
            extractForm(byteCarrier, offset, len);
        } else {
            try {
                headerMap.put("URI", URLDecoder.decode(new String(bytes, offset, len), "UTF-8"));
            } catch (Exception e) {
                logger.info("ERROR in URL read!");
            }
        }
    }

    /**
     * Extract information from form, name=value
     *
     * @param byteCarrier bytes to read in
     * @param offset      the begin position
     * @param length      total length of the field
     */
    private void extractForm(ByteCarrier byteCarrier, int offset, int length) {
        logger.info("Begin Analysis Form!");
        byte[] bytes = byteCarrier.getBytes();

        /* char mark */
        byte andMark = (byte) 38;
        byte eqMark = (byte) 61;
        /* set flag to be name field */
        boolean nameField = true;
        /* length of char field */
        int nameLen = 0;
        int valLen = 0;
        int lastPos = offset;

        int index;
        try {
            for (index = offset; index < offset + length; index++) {
            /* name1=value1&name2=value2&... */
                if (bytes[index] == eqMark) {
                    nameField = false;
                } else if (bytes[index] == andMark) {
                    String name = URLDecoder.decode(new String(bytes, lastPos, nameLen), "UTF-8");
                    String value = URLDecoder.decode(new String(bytes, lastPos + nameLen + 1, valLen), "UTF-8");

                    paraMap.put(name, value);
                    nameLen = valLen = 0;
                    lastPos = index + 1;
                    nameField = true;
                } else if (nameField) {
                    nameLen++;
                } else {
                    valLen++;
                }
            }
            /* deal with the tail  */
            String name = URLDecoder.decode(new String(bytes, lastPos, nameLen), "UTF-8");
            String value = URLDecoder.decode(new String(bytes, lastPos + nameLen + 1, valLen), "UTF-8");

            paraMap.put(name, value);
        } catch (Exception e) {
            logger.info("ERROR read Form!");
        }
    }


    /**
     * Extract Header with format Name:Value
     *
     * @param byteCarrier request information to analysis
     */
    private void extractHeader(ByteCarrier byteCarrier) {
        logger.info("Begin Analysis Header!");
        byte[] bytes = byteCarrier.getBytes();

        /* length */
        int nameLen = 0;
        int valLen = 0;
        /* mark */
        byte lineEnd = (byte) 13;
        byte space = (byte) 32;
        byte colon = (byte) 58;
        /* position */
        int pos = byteCarrier.getPosition();
        int lastPos = 0;
        /* name field flag */
        boolean nameField = true;

        byte beforeByte = byteCarrier.getOne();
        byte afterByte;
        while (byteCarrier.notEnd() && !((afterByte = byteCarrier.getOne()) == lineEnd && nameField)) {
            if (afterByte == lineEnd) {
                String name = new String(bytes, pos, nameLen);
                String value = new String(bytes, pos + nameLen + 2, valLen);
                headerMap.put(name, value);
                nameField = true;
                afterByte = byteCarrier.getOne();
                lastPos = byteCarrier.getPosition();
                nameLen = valLen = 0;
                pos = lastPos;
            } else if (beforeByte == colon && afterByte == space) {
                nameField = false;
                nameLen--;
            } else if (nameField) {
                nameLen++;
            } else {
                valLen++;
            }
            beforeByte = afterByte;
        }//while
        if (!byteCarrier.notEnd()) {
            byteCarrier.setPosition(lastPos);
            headerStatus = false;
            logger.info("Failed in header read!");
        } else {
            byteCarrier.getOne();
            headerStatus = true;
            logger.info("Header read!");
        }
    }


    /**
     * Extract Body with format like username=xx & password=xx
     * Split by & and send each field to extractForm
     *
     * @param byteCarrier request information to analysis
     */
    private void extractBody(ByteCarrier byteCarrier) {
        logger.info("Begin Analysis Body!");
        /* Only post need send message body, check Method */
        if (getHeader("Method").equals("POST")) {
            /* mark */
            byte andMark = (byte) 38;
            /* length */
            int contentLen = Integer.parseInt(getHeader("content-length"));
            int readLen = 0;
            int len = 0;
            /* position */
            int lastPos = byteCarrier.getPosition();
            while (byteCarrier.notEnd() && readLen < contentLen) {
                byte bt = byteCarrier.getOne();
                readLen++;
                /* finish one field */
                if (bt == andMark) {
                    extractForm(byteCarrier, lastPos, len);
                    lastPos = byteCarrier.getPosition();
                } else {
                    len++;
                }
            }
            extractForm(byteCarrier, lastPos, len);
            bodyStatus = true;
            logger.info("body read!");
//            if (!byteCarrier.notEnd()) {
//                byteCarrier.setPosition(lastPos);
//                bodyStatus = false;
//                logger.info("failed in body read!");
//            } else {
//
//            }
        } else {
            /* no post body */
            bodyStatus = true;
            logger.info("body read!");
        }
    }


    /**
     * Get the value of a certain header parameter
     *
     * @param name headerName
     * @return corresponding value
     */
    public String getHeader(String name) {
        return headerMap.get(name);
    }

    /**
     * Get the value of a certain body parameter
     *
     * @param name parameter name
     * @return corresponding value
     */
    public String getPara(String name) {
        return paraMap.get(name);
    }


    /**
     * Get the value stored for test
     *
     * @return HashMap of header and parameters
     */
    public HashMap<String, String> getHeaderMap() {
        return headerMap;
    }

    public HashMap<String, String> getParaMap() {
        return paraMap;
    }

    /**
     * As the information bytes Carrier
     * Send to functions to extract
     */
    private class ByteCarrier {
        /* main subject */
        private byte[] bytes = new byte[2048];
        /* limitation for the bytes array */
        private int length;
        /* current moving position, max valid length-1 */
        private int position;

        private ByteCarrier() {
            length = 0;
            position = 0;
        }

        /**
         * Get the bytes array carried
         *
         * @return self
         */
        public byte[] getBytes() {
            return bytes;
        }

        /**
         * Get one byte & increase position by 1
         *
         * @return the current byte
         */
        public byte getOne() {
            byte ret = bytes[position];
            position++;
            return ret;
        }

        /**
         * if the array has been read to the end
         * position <= length-1
         *
         * @return true/false
         */
        public boolean notEnd() {
            return position <= length - 1;
        }

        /**
         * Get the current position, add new byte buf to the end
         * move backward the old-unread part
         *
         * @param byteBuffer the bytes to be stored
         * @param buflen     bytes length
         */
        public void fill(ByteBuffer byteBuffer, int buflen) {
            byte[] buf = byteBuffer.array();
            System.arraycopy(bytes, position, bytes, 0, buflen - position);
            System.arraycopy(buf, 0, bytes, position, buflen);
            length = length - position + buflen;
            position = 0;
        }


        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }
    }

}
