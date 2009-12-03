/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package yui.classes;

import java.io.UnsupportedEncodingException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import java.util.Calendar;
import java.util.Enumeration;
import java.util.GregorianCalendar;
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

    private AResourceGroup resourceGroup;
    HttpServletRequest request;
    HttpServletResponse response;

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
        resourceGroup = new AResourceGroup(request.getQueryString());
        init();


    }
    public String serverURI;

    private void parseRequest() {

        String g = "";
        for (Enumeration e = request.getParameterNames(); e.hasMoreElements();) {
            logger.info("Item: " + e.nextElement());
        }

    }

    private void parseRequestOld() {
    }


    private void init() {

        logger.info("starting init");

        if (!resourceGroup.getQueryString().equals("")) {

            //
            if (!cacheManager.cacheExists(cacheKey)) {
                cacheManager.addCache(cacheKey);
            }

            HttpServletResponse res = (HttpServletResponse) response;
            setCacheExpireDate(res, 315360000);
            res.setHeader("Content-Type", resourceGroup.getContentType());

            Cache c = cacheManager.getCache(cacheKey);
            if (c.isKeyInCache(serverURI + resourceGroup.getContentType())) {
                logger.info("we found cache " + (serverURI + resourceGroup.getContentType()));
                // TODO when we turn this into tag we send this  puppy to client.
                logger.info(c.get(serverURI + resourceGroup.getContentType()).toString());
            } else {
                logger.info("we dont have cache for  " + serverURI);
                YUI_util_Loader loader = new YUI_util_Loader(resourceGroup.getVersion());
                // todo do we need this? dont think so
//                    $base   = PATH_TO_LIB . $yuiVersion . "/build/";
//                    $loader->base = $base;


                //Detect and set a filter as needed (defaults to minified version)
                if (resourceGroup.isDebug()) {
                    logger.info("Found debug files ");
                    loader.filter = YUI_util_Loader.YUI_DEBUG;
                } else if (!resourceGroup.isDebug() && !resourceGroup.isMin()) {
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
                String yuiComponent = "";
                logger.info("Iterating through yuiFiles: " + resourceGroup.getGroup());
                for (String aResource : resourceGroup.getGroup()) {
                    String yuiFile = aResource;
                    logger.info("for yuiFile: " + yuiFile);
                    String parts[] = yuiFile.split("/");

                    if (parts != null && parts.length >= 3) {
                        logger.info("for yuiFile Parts : " + Arrays.toString(parts));
                        yuiComponent = parts[2];

                    } else {
                        logger.error("<!-- Unable to determine module name! -->");
                        throw new RuntimeException("<!-- Unable to determine module name! -->");
                    }

                    logger.info("loading following Components :  " + yuiComponent);
                    loader.loadSingle(yuiComponent);
                    logger.info("contentType is :  " + resourceGroup.getContentType());

                    if (resourceGroup.getContentType().equals("application/x-javascript")) {
                        raw += loader.script_raw();
                        logger.trace("fetching raw from loader :  " + raw);
                        // TODO display
                    } else {
                        Map cssResourceList = loader.css_data();

                        logger.info("fetching css_data from loader :  " + cssResourceList);

                        Map cssResourceListCSS = (Map) cssResourceList.get("css");

                        logger.info("cssResourceListCSS is :  " + cssResourceListCSS);
                        if (cssResourceListCSS != null) {
                            for (String key : (Set<String>) cssResourceListCSS.keySet()) {
                                // TODO finish
                                logger.info("key  is :  " + key);
                                crtResourceBase = key.substring(0, (key.lastIndexOf("/") + 1));
                                logger.info("crtResourceBase  is :  " + crtResourceBase);

                                String crtResourceContent = loader.getRemoteContent(key);
                                // TODO Image path correction

                                raw += crtResourceContent;

                            }
                        }
                        logger.trace("rawCSS before: " + raw);
                        raw = raw.replace("/build/build/", "/build/");
                        logger.trace("rawCSS after: " + raw);
                    }
                }
                c.put(new Element(serverURI + resourceGroup.getContentType(), raw));
//                        YUIcompressorAPI api =  new YUIcompressorAPI();
//                        YUIcompressorAPI.Config  conf= api .new Config(null);

            }
        }

    }

    public String getRaw() {
        logger.info("[getRaw] checking cache");
        if (serverURI == null || resourceGroup.getContentType() == null || !cacheManager.cacheExists(cacheKey)) {
            logger.info("[getRaw] we have to ReInit, something was wrong");
            init();
        }

        Cache c = cacheManager.getCache(cacheKey);

        if (c.isKeyInCache(serverURI + resourceGroup.getContentType())) {
            logger.info("[getRaw] we found cache for " + serverURI + resourceGroup.getContentType());
            return (String) ((Element) c.get(serverURI + resourceGroup.getContentType())).getValue();
        } else {
            logger.info("[getRaw] cache was not found, something is wrong" + serverURI + resourceGroup.getContentType());
            throw new RuntimeException("[getRaw] cache was not found, something is wrong" + serverURI + resourceGroup.getContentType());
        }
    }

    private void init2() {
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

    public static DateFormat htmlExpiresDateFormat() {
        DateFormat httpDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        httpDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return httpDateFormat;
    }
    //getQueryString() 
}
