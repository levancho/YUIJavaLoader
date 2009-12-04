/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package yui.classes.utils;

import java.util.Calendar;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yui.classes.AClientCachable;

/**
 *
 * @author leo
 */
public class HTTPUtils {

    private static final Logger logger = LoggerFactory.getLogger(HTTPUtils.class);
    public final static String GZIP = "gzip";
    public final static String DEFLATE = "deflate";
    public final static String CACHE_PRIVATE = "PRIVATE";
    public final static String CACHE_PUBLIC = "PUBLIC";
    public final static String NO_CACHE = "NO-CACHE";
    public final static String CACHE_STORE = "NO-STORE";
    public static String eTagDefaultPrefix = "_YUIJL_";

    public static String getDefaultEtagPrefix() {
        return eTagDefaultPrefix;
    }

    public static String getEtag(long Postffix) {
        // TODO
        return eTagDefaultPrefix + String.valueOf(Postffix);
    }

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
        return ((e != null) && (GZIP.indexOf(e) != -1));
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

    public static void addHeader(HttpServletResponse res, Headers name, String... values) {
        for (String aHeaderValue : values) {
            res.addHeader(name + "", aHeaderValue);
        }
    }

    public static boolean containsHeaders(HttpServletRequest req, Headers... headernames) {
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
        if (containsHeaders(req, Headers.REQ_IF_MODIFIED_SINCE, Headers.REQ_IF_NONE_MATCH)) {

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
}
