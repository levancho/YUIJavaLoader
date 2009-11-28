/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package yui.classes;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author leo
 */
public class Combo {

    Logger logger = LoggerFactory.getLogger(Combo.class);
    private PageContext context;
    HttpServletRequest request;
    HttpServletResponse response;
    private String queryString;
    private String cacheKey = "yuiconfigLFU";
    CacheManager cacheManager;

    public String server(boolean includeRequestURI) {
        logger.info("calculating server url");
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
    String crtResourceBase;

    public void alphaImageLoaderPathCorrection(String... matches) {
        // TODO
//    $matchedFile  = substr($matches[1], strrpos($matches[1], "/") + 1);
//    $newFilePath = 'AlphaImageLoader(src=\'' . $crtResourceBase . $matchedFile . '\'';
//
//    return $newFilePath;
    }

    public Combo(HttpServletRequest _request, HttpServletResponse _response) {

        this.request = _request;
        this.response = _response;
        serverURI = server(true);
        cacheManager = CacheManager.create();
        init();


    }
    public String serverURI;
    String contentType = "";

    private void init() {

        String _q = request.getQueryString();

        boolean cache = false;
        String[] yuiFiles = null;
        String[] metainfo = null;
        String yuiVersion = null;
        logger.info("starting init");
        try {
            if (_q != null && !_q.trim().equals("")) {
                this.queryString = URLDecoder.decode(_q, "UTF-8");
            }

            logger.info("queryStringis " + queryString);
            if (!queryString.equals("")) {
                yuiFiles = queryString.split("&");
                if (yuiFiles == null || yuiFiles.length == 0) {
                    logger.info("thre is nothing in query?" + queryString);
                    return;
                }


                contentType = (yuiFiles[0].indexOf(".js") != -1) ? "application/x-javascript" : "text/css";

                //
                if (!cacheManager.cacheExists(cacheKey)) {
                    cacheManager.addCache(cacheKey);
                }

                HttpServletResponse res = (HttpServletResponse) response;
                setCacheExpireDate(res, 315360000);
                res.setHeader("Content-Type", contentType);

                Cache c = cacheManager.getCache(cacheKey);
                if (c.isKeyInCache(serverURI + contentType)) {
                    logger.info("we found cache " + (serverURI + contentType));
                    // TODO when we turn this into tag we send this  puppy to client.



                    logger.info(c.get(serverURI + contentType).toString());

                } else {
                    logger.info("we dont have cache for  " + serverURI);
                    metainfo = yuiFiles[0].split("/");
                    logger.info("metainfo:  " + Arrays.toString(metainfo));

                    yuiVersion = metainfo[0];
                    logger.info("yuiVersion:  " + yuiVersion);

                    YUI_util_Loader loader = new YUI_util_Loader(yuiVersion, context);
                    // todo do we need this? dont think so
//                    $base   = PATH_TO_LIB . $yuiVersion . "/build/";
//                    $loader->base = $base;


                    //Detect and set a filter as needed (defaults to minified version)
                    if (queryString.indexOf("-debug.js") != -1) {
                        logger.info("Found debug files ");
                        loader.filter = YUI_util_Loader.YUI_DEBUG;
                    } else if ((queryString.indexOf("-min.js") == -1) && (queryString.indexOf("-debug.js") == -1)) {
                        logger.info("assuming raw files");
                        loader.filter = YUI_util_Loader.YUI_RAW;
                    }

                    //Verify this version of the library exists locally
                    // TODO later
                    //        $localPathToBuild = "../lib/" . $yuiVersion . "/build/";
                    //        if (file_exists($localPathToBuild) === false || is_readable($localPathToBuild ) === false) {
                    //            die('<!-- Unable to locate the YUI build directory! -->');
                    //        }
                    String raw = "";
                    String[] yuiComponents = new String[1];
                    logger.info("Iterating through yuiFiles: " + Arrays.toString(yuiFiles));
                    for (int i = 0; i < yuiFiles.length; i++) {
                        String yuiFile = yuiFiles[i];
                        logger.info("for yuiFile: " + yuiFile);

                        String parts[] = yuiFile.split("/");


                        if (parts != null && parts.length >= 3) {
                            logger.info("for yuiFile Parts : " + Arrays.toString(parts));
                            yuiComponents[0] = parts[2];

                        } else {
                            logger.info("<!-- Unable to determine module name! -->");
                            throw new RuntimeException("<!-- Unable to determine module name! -->");
                        }

                        logger.info("loading following Components :  " + yuiComponents);
                        loader.load(yuiComponents);
                        logger.info("contentType is :  " + contentType);

                        if (contentType.equals("application/x-javascript")) {
                            raw += loader.script_raw();
                            logger.info("fetching raw from loader :  " + raw);
                            // TODO display
                        } else {

                            Map cssResourceList = loader.css_data();
                            logger.info("fetching css_data from loader :  " + cssResourceList);

                            Map cssResourceListCSS = (Map) cssResourceList.get("css");

                            logger.info("cssResourceListCSS is :  " + cssResourceListCSS);
                            for (String key : (Set<String>) cssResourceListCSS.keySet()) {
                                // TODO finish
                                logger.info("key  is :  " + key);
                                crtResourceBase = key.substring(0, (key.lastIndexOf("/") + 1));
                                logger.info("crtResourceBase  is :  " + crtResourceBase);

                                String crtResourceContent = loader.getRemoteContent(key);
                                // TODO Image path correction

                                raw += crtResourceContent;

                            }
                            logger.info("rawCSS before: " + raw);
                            raw = raw.replace("/build/build/", "/build/");
                            logger.info("rawCSS after: " + raw);
                        }
                    }
                    c.put(new Element(serverURI + contentType, raw));
                }
            }
        } catch (UnsupportedEncodingException ex) {
            logger.info(ex.getMessage());
            ex.printStackTrace();
        }
    }

    public String getRaw() {
        logger.info("[getRaw] checking cache");
        if (serverURI == null || contentType == null || !cacheManager.cacheExists(cacheKey)) {
            logger.info("[getRaw] we have to ReInit, something was wrong");
            init();
        }

        Cache c = cacheManager.getCache(cacheKey);

        if (c.isKeyInCache(serverURI + contentType)) {
            logger.info("[getRaw] we found cache for " + serverURI + contentType);
            return (String) ((Element) c.get(serverURI + contentType)).getValue();
        } else {
            logger.info("[getRaw] cache was not found, something is wrong" + serverURI + contentType);
            throw new RuntimeException("[getRaw] cache was not found, something is wrong" + serverURI + contentType);
        }
    }

    private void init2() {
    }

    public static void setCacheExpireDate(HttpServletResponse response,
            int seconds) {
        if (response != null) {
            Calendar cal = new GregorianCalendar();
            cal.roll(Calendar.SECOND, seconds);
            response.setHeader("Cache-Control", "PUBLIC, max-age=" + seconds + ", must-revalidate");
            response.setHeader("Expires", htmlExpiresDateFormat().format(cal.getTime()));
        }
    }

    public static DateFormat htmlExpiresDateFormat() {
        DateFormat httpDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        httpDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return httpDateFormat;
    }
    //getQueryString() 
}
