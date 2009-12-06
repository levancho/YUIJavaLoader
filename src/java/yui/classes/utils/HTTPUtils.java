/*
 *  Copyright (c) 2009, Amostudio,inc
 *  All rights reserved.
 *  Code licensed under the BSD License:
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *    * Neither the name of the Amostudio,inc  nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 *   THIS SOFTWARE IS PROVIDED BY Amostudio,inc ''AS IS'' AND ANY
 *   EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *   WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *   DISCLAIMED. IN NO EVENT SHALL Amostudio,inc  BE LIABLE FOR ANY
 *   DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *   SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package yui.classes.utils;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yui.classes.AClientCachable;

/**
 *
 * @author leo
 *
 * TODO response.addHeader("Vary", "Accept-Encoding"); // Handle proxies
 */
public class HTTPUtils {

    private static final Logger logger = LoggerFactory.getLogger(HTTPUtils.class);

    public final static String GZIP = "gzip";
     public final static String XGZIP = "x-gzip";
    public final static String DEFLATE = "deflate";

    public static final String CACHE_NO_CACHE = "no-cache";
    public static final String CACHE_NO_STORE = "no-store";
    public static final String CACHE_MAX_AGE = "max-age";
    public static final String CACHE_MAX_STALE = "max-stale";
    public static final String CACHE_MIN_FRESH = "min-fresh";
    public static final String CACHE_NO_TRANSFORM = "no-transform";
    public static final String CACHE_ONLY_IF_CACHED = "only-if-cached";
    public static final String CACHE_PUBLIC = "public";
    public static final String CACHE_PRIVATE = "private";
    public static final String CACHE_MUST_REVALIDATE = "must-revalidate";
    public static final String CACHE_PROXY_REVALIDATE = "proxy-revalidate";
    public static final String CACHE_SHARED_MAX_AGE = "s-maxage";

    public static String eTagDefaultPrefix = "_YUIJL_";

    public static String getDefaultEtagPrefix() {
        return eTagDefaultPrefix;
    }

    public static String getEtag(long Postffix) {
        // TODO
        return eTagDefaultPrefix + String.valueOf(Postffix);
    }


  /**
 * HTTP Content_Type(MIME type) Enums
 *
 */
    public enum CONTENT_TYPE {

        JAVASCRIPT("application/x-javascript"),
        CSS("text/css");

        CONTENT_TYPE(String _name) {
            this.name = _name;
        }
        private String name;

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     *
     * HTTP request and response Headers, request headers are
     * prefixed with REQ_ and response Headers with RES_
     * Shared Headers(used in request and response)  have no prefix
     *
     * @see http://en.wikipedia.org/wiki/List_of_HTTP_headers
     */
    public enum Headers {

        REQ_ACCEPT("Accept", "*.*"),
        REQ_ACCEPT_CHARSET("Accept-Charset", ""),
        REQ_ACCEPT_ENCODING("Accept-Encoding", ""),
        REQ_IF_MODIFIED_SINCE("If-Modified-Since", ""),
        REQ_IF_NONE_MATCH("If-None-Match", ""),
        REQ_IF_UNMODIFIED_SINCE("If-Unmodified-Since", ""),
        CACHE_CONTROL("Cache-Control", "no-cache"),
        PRAGMA("Pragma", "no-cache"),
        CONTENT_TYPE("Content-Type", ""),
        CONTENT_LENGTH("Content-Length", ""),
        RES_E_TAG("ETag", ""),
        RES_EXPIRES("Expires", ""),
        RES_LAST_MODIFIED("Last-Modified", "");

        Headers(String _name, String _defaultValue) {
            this.name = _name;
            this.defaultValue = _defaultValue;

        }
        private String name;
        private String defaultValue;

        @Override
        public String toString() {
            return name;
        }

        public String toValue() {
            return defaultValue;
        }
    }

    public static boolean canGZip(HttpServletRequest req) {
        String e = req.getHeader(Headers.REQ_ACCEPT_ENCODING + "");
        //req.getHeader("---------------") for Norton Scanner
        return ((e != null) && (GZIP.indexOf(e) != -1 || req.getHeader("---------------") != null));
    }



    public static String getNormalizedServletPath(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        String pathInfo = request.getPathInfo();
        String requestURI = request.getRequestURI();

        if (requestURI == null && pathInfo == null && requestURI == null) {
            throw new RuntimeException("Invalid Requst,  request.getServletPath() , request.getPathInfo()  "
                    + "and  request.getRequestURI() are all null ");
        }
        if (servletPath == null) {
            if (pathInfo == null) {
                servletPath = requestURI;
            } else {
                servletPath = requestURI.substring(0, requestURI.indexOf(pathInfo));
            }
        } else if (servletPath.trim().equals("")) {
            servletPath = pathInfo;
        }

        return servletPath;
    }

    public static String getServerURL(HttpServletRequest request) {
        return getServerURL(request, true);
    }

    public static String getServerURL(HttpServletRequest request, boolean includeRequestURI) {

        String path = request.getContextPath();
        logger.info("calculating server url path" + path);
        String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        logger.info("calculating server url basePath" + basePath);
        if (includeRequestURI) {
            basePath += path + "/";
        }
        logger.info("calculating server url all" + basePath);
        // we could maybe try request.getRemoteHost()

        return basePath;
    }

    // TODO
    public String localPath(HttpServletRequest request, String path) {
        char seperator=File.separatorChar;
        String absolutePath = request.getSession().getServletContext().getRealPath(request.getRequestURI());
        absolutePath = absolutePath.substring(0, absolutePath.lastIndexOf(seperator) + 1);
        return absolutePath + path.replace('/',seperator );
    }

    public static void addHeader(HttpServletResponse res, Headers name, String... values) {
        for (String aHeaderValue : values) {
            res.addHeader(name + "", aHeaderValue);
        }
    }

    
    public static boolean containsAllHeaders(HttpServletRequest req, Headers... headernames) {
        for (Headers header : headernames) {
            if (req.getHeader(header + "") == null) {
                logger.trace("[containsHeaders] could not locate Header  " + header);
                return false;
            } else {
                logger.trace("[containsHeaders] found Header  " + header);
            }
        }
        return true;
    }

    public static Date getNormalizedDate(long miliseconds) {

        logger.trace("[getNormalizedDate] Generating Date for  " + miliseconds);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(miliseconds);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static boolean isModified(HttpServletRequest req, long fileSize, long modifiedTime) {

        logger.trace("[isModified] Generating Date for   fileSize: " + fileSize + " modifiedTime:" + modifiedTime);
        if (containsAllHeaders(req, Headers.REQ_IF_MODIFIED_SINCE, Headers.REQ_IF_NONE_MATCH)) {

            String byteTag = getEtag(fileSize);
            String eTag = req.getHeader("If-None-Match");
            int datecompare = getNormalizedDate(req.getDateHeader("If-Modified-Since")).
                    compareTo(getNormalizedDate(modifiedTime));

            boolean datesmatch = (datecompare == 0);
            boolean tagsmatch = eTag.equalsIgnoreCase(byteTag);

            return !datesmatch || !tagsmatch;

        } else {
            return true;
        }
    }

    public static boolean isModified(HttpServletRequest req, AClientCachable a) {
        return isModified(req, a.getFileSize(), a.getModified());
    }

    public static void setCacheExpireDate(HttpServletResponse response,
            int seconds) {
        if (response != null) {
            Calendar cal = new GregorianCalendar();
            cal.roll(Calendar.SECOND, seconds);
            response.setHeader("Cache-Control", "PRIVATE, max-age=" + seconds + ", must-revalidate");
            response.setHeader("Expires", htmlExpiresDateFormat().format(cal.getTime()));
        }
    }

    /**
     * returns null if compression is not allowed.
     * @param req
     * @return
     */
    public static String  getGZIPHeaderValue(HttpServletRequest req ){
        if(!canGZip(req)) return null;
            String e = req.getHeader(Headers.REQ_ACCEPT_ENCODING + "");
            return  (e.indexOf(XGZIP) != -1 ? XGZIP : GZIP);
    }

    public static DateFormat htmlExpiresDateFormat() {
        DateFormat httpDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        httpDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return httpDateFormat;
    }

    public static String getRemoteContent(String urlString) {

        HttpURLConnection connection = null;
        DataInputStream in = null;
        BufferedReader d = null;
        StringBuffer sb = new StringBuffer();
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            //connection.setRequestMethod("POST");
            connection.setInstanceFollowRedirects(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setReadTimeout(10000);
            connection.connect();

            in = new DataInputStream(connection.getInputStream());
            d = new BufferedReader(new InputStreamReader(in));

            while (d.ready()) {
                sb.append(d.readLine());

            }
            logger.debug("HTML we got  is" + sb.toString());
        } catch (IOException ex) {
            logger.error("IO Exception " + ex.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
                connection = null;
            }

            if (in != null) {
                try {
                    in.close();
                    in = null;
                } catch (IOException ex) {
                    // do nothing
                }
            }

            if (d != null) {
                try {
                    d.close();
                    d = null;
                } catch (IOException ex) {
                    // do nothing
                }
            }
        }
        return sb.toString();
    }

    // Experimental
    public static String getRemoteContentNIO(String urlString) {
        return getRemoteContentNIO(urlString, "ISO-8859-1");
    }

    //"ISO-8859-1"
     // Experimental
    public static String getRemoteContentNIO(String urlString, String encoding) {


        String CRLF = "\r\n\r\n";
        SocketChannel channel = null;
        CharBuffer charBuffer = null;
        ByteBuffer buffer = null;
        StringBuffer sb = null;
        String ret = null;
        try {

            URL url = new URL(urlString);
            // Setup
            InetSocketAddress socketAddress =
                    new InetSocketAddress(url.getHost(), 80);



            String request = "GET " + url.getFile() + " HTTP/1.1\r\n" + "User-Agent: HTTPGrab\r\n"
                    + "Accept: text/*\r\n" + "Connection: close\r\n" + "Host: " + url.getHost() + "\r\n" + "\r\n";


            Charset charset =
                    Charset.forName(encoding);
            CharsetDecoder decoder =
                    charset.newDecoder();
            CharsetEncoder encoder =
                    charset.newEncoder();

            // Allocate buffers
            buffer =
                    ByteBuffer.allocateDirect(1024);
            charBuffer =
                    CharBuffer.allocate(1024);
            // Connect
            channel = SocketChannel.open();
            channel.connect(socketAddress);

            // Send request
            logger.debug(request);
            channel.write(encoder.encode(CharBuffer.wrap(request)));
            sb = new StringBuffer();
            // Read response
            while ((channel.read(buffer)) != -1) {
                buffer.flip();
                // Decode buffer
                decoder.decode(buffer, charBuffer, false);
                // Display
                charBuffer.flip();
                sb.append(charBuffer);
                buffer.clear();
                charBuffer.clear();
            }

            // removing Header
            ret = sb.substring(sb.indexOf(CRLF));

            logger.info("Buffer 1" + sb);
        } catch (UnknownHostException e) {
            logger.error(e.getMessage());
        } catch (MalformedURLException ex) {
               logger.error(ex.getMessage());
        } catch (IOException e) {
            System.err.println(e);
        } finally {
            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException ignored) {
                }
            }
        }
        return ret;
    }
}
