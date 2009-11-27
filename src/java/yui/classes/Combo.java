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
import java.util.Collections;
import java.util.GregorianCalendar;
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
    private String queryString;
    private String cacheKey = "yuiconfigLFU";
    CacheManager cacheManager;

    public String server(boolean includeRequestURI) {

        String path = request.getContextPath();
        String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();

        if (includeRequestURI) {
            basePath += path + "/";
        }
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

    public Combo(PageContext _context) {
        this.context = _context;
        this.request = (HttpServletRequest) context.getRequest();

        cacheManager = CacheManager.create();
        init();


    }

    private void init() {
        String _q = request.getQueryString();
        String contentType = "";
        boolean cache = false;
        String[] yuiFiles = null;
        String[] metainfo = null;
        String yuiVersion = null;
        String s = server(true);
        try {
            this.queryString = (_q != null && _q.trim().equals("")) ? URLDecoder.decode(_q, "UTF-8") : "";
            logger.info("queryStringis" + queryString);
            if (!queryString.equals("")) {
                yuiFiles = queryString.split("&");
                if (yuiFiles == null || yuiFiles.length == 0) {
                    logger.info("thre is nothing in query?" + queryString);
                    return;
                }


                contentType = (yuiFiles[0].indexOf(".js") != -1) ? "application/x-javascript" : "text/css";
                
                Cache c = cacheManager.getCache(cacheKey);


                HttpServletResponse res = (HttpServletResponse) context.getResponse();
                setCacheExpireDate(res, 315360000);
                res.setHeader("Content-Type", contentType);
                if (c.isKeyInCache(s)) {
                    logger.info("we found cache " + s);
                    // TODO when we turn this into tag we send this  puppy to client.
                    logger.info(c.get(s).toString());

                } else {
                    logger.info("we dont have cache for  " + s);
                    metainfo = yuiFiles[0].split("/");
                    yuiVersion = metainfo[0];

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

                    List yuiComponents = new ArrayList();
                    logger.info("Iterating through yuiFiles: " + Arrays.toString(yuiFiles));
                    for (int i = 0; i < yuiFiles.length; i++) {
                        String yuiFile = yuiFiles[i];
                        logger.info("for yuiFile: " + yuiFile);

                        String parts[] = yuiFile.split("/");


                        if (parts != null && parts.length >= 3) {
                            logger.info("for yuiFile Parts : " + Arrays.toString(parts));
                            yuiComponents.add(parts[2]);

                        } else {
                            logger.info("<!-- Unable to determine module name! -->");
                            throw new RuntimeException("<!-- Unable to determine module name! -->");
                        }

                        loader.load((String[]) yuiComponents.toArray());

                        if(contentType.equals("application/x-javascript")){

                            String raw = loader.script_raw();
                             c.put(new Element(s, raw));

                        } else  {
                            String rawCSS="";
                            Map  cssResourceList = loader.css_data();
                            Map cssResourceListCSS= (Map)cssResourceList.get("css");

                             for (String cssResource : (Set<String>) cssResourceListCSS.keySet()) {
                                 // TODO continue from here
                                //Map cssResourceListCSSkey= (Map)cssResourceListCSS.get(cssResource);
                             }
                            
                        }

                    }

                }
            }




        } catch (UnsupportedEncodingException ex) {
            logger.error(ex.getMessage());
            ex.printStackTrace();
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
