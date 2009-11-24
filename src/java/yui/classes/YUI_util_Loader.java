/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package yui.classes;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.servlet.jsp.PageContext;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author leo
 */
public class YUI_util_Loader {

    Logger logger = LoggerFactory.getLogger(YUI_util_Loader.class);
    public static final String YUI_AFTER = "after";
    public static final String YUI_BASE = "base";
    public static final String YUI_CSS = "css";
    public static final String YUI_DATA = "DATA";
    public static final String YUI_DEPCACHE = "depCache";
    public static final String YUI_DEBUG = "DEBUG";
    public static final String YUI_EMBED = "EMBED";
    public static final String YUI_FILTERS = "filters";
    public static final String YUI_FULLPATH = "fullpath";
    public static final String YUI_FULLJSON = "FULLJSON";
    public static final String YUI_GLOBAL = "global";
    public static final String YUI_JS = "js";
    public static final String YUI_JSON = "JSON";
    public static final String YUI_MODULES = "modules";
    public static final String YUI_NAME = "name";
    public static final String YUI_OPTIONAL = "optional";
    public static final String YUI_OVERRIDES = "overrides";
    public static final String YUI_PATH = "path";
    public static final String YUI_PKG = "pkg";
    public static final String YUI_PREFIX = "prefix";
    public static final String YUI_PROVIDES = "provides";
    public static final String YUI_RAW = "RAW";
    public static final String YUI_REPLACE = "replace";
    public static final String YUI_REQUIRES = "requires";
    public static final String YUI_ROLLUP = "rollup";
    public static final String YUI_SATISFIES = "satisfies";
    public static final String YUI_SEARCH = "search";
    public static final String YUI_SKIN = "skin";
    public static final String YUI_SKINNABLE = "skinnable";
    public static final String YUI_SUPERSEDES = "supersedes";
    public static final String YUI_TAGS = "TAGS";
    public static final String YUI_TYPE = "type";
    public static final String YUI_URL = "url";
    /* public api variables TODO add set/get methods */
    public String base;
    public String filter = "";
    public String target;
    public boolean combine;
    public boolean allowRollups;
    public boolean loadOptional;
    public boolean rollupsToTop;
    private Map processedModuleTypes = new HashMap();
    // all required modules
    private Map requests = new HashMap();
    // modules that have been been outputted via getLink()
    private Map loaded = new HashMap();
    // list of all modules superceded by the list of required modules
    private List<String> superceded;
    // module load count to catch circular dependencies
    // private  List<String> loadCount;
    // keeps track of modules that were requested that are not defined
    private Map undefined = new HashMap();
    private boolean dirty = true;
    private Map sorted = new HashMap();
    private Map accountedFor = new HashMap();
    /**
     * A list of modules to apply the filter to.  If not supplied, all
     * modules will have any defined filters applied.  Tip: Useful for debugging.
     * @property filterList
     * @type array
     * @default null
     */
    private List<String> filterList;
    // the list of required skins
    private Map skins = new HashMap();
    private Map modules = new HashMap();
    private String fullCacheKey;
    private Map baseOverrides = new HashMap();
    private boolean cacheFound = false;
    private boolean delayCache = false;
    private String versionKey = "_yuiversion";
    // the skin definition
    private Map skin = new HashMap();
    private Map rollupModules = new HashMap();
    private List  globalModules = new ArrayList();
    private Map satisfactionMap = new HashMap();
    private Map depCache = new HashMap();
    private Map filters = new HashMap();
    /**
     * The base path to the combo service.  Uses the Yahoo! CDN service by default.
     * You do not have to set this property to use the combine option. YUI PHP Loader ships
     * with an intrinsic, lightweight combo-handler as well (see combo.php).
     * @property comboBase
     * @type string
     * @default http://yui.yahooapis.com/combo?
     */
    private String comboBase = "http://yui.yahooapis.com/combo?";
    // additional vars used to assist with combo handling
    private String cssComboLocation = null;
    private String jsComboLocation = null;
    private String comboDefaultVersion;
    private JSONObject yui_current;

    /* If the version is set, a querystring parameter is appended to the
     * end of all generated URLs.  This is a cache busting hack for environments
     * that always use the same path for the current version of the library.
     * @property version
     * @type string
     * @default null
     */
    private String yuiVersion = "";
    private PageContext _context;
    private String cacheKey;
    private Map userSuppliedModules;
    private boolean _noYUI;
    private String _jsonConfigFilePrefix = "config";
    private String _jsonConfigFile;
    CacheManager cacheManager;

    public YUI_util_Loader(String version, PageContext context) {
        this(version, context, "");
    }

    public YUI_util_Loader(String version, PageContext context, String cacheKey) {
        this(version, context, cacheKey, null);
    }

    public YUI_util_Loader(String version, PageContext context, String cacheKey, Map modules) {
        this(version, context, cacheKey, modules, false);
    }

    public YUI_util_Loader(String version, PageContext context, String cacheKey, Map modules, boolean noYUI) {

        if (version == null || version.trim().equals("")) {
            throw new RuntimeException("Error: The first parameter of YAHOO_util_Loader must specify which version of YUI to use!");
        }
        this.yuiVersion = version;
        this._context = context;
        this.cacheKey = cacheKey;
        this.userSuppliedModules = modules;
        this._noYUI = noYUI;
        this._jsonConfigFile = "json_" + this.yuiVersion + ".txt";

        this.cacheManager = CacheManager.create();
        this.init();
    }
    private String j;

    private void init() {

        InputStream in = loadResource(this._jsonConfigFile);
        if (in == null) {
            throw new RuntimeException("suitable YUI metadata file: [" + this._jsonConfigFile + "]  Could not be found or Loaded");
        }
        // convert inputStream to String
        j = convertStreamToString(in);
        // convert json String to Java Object

        JSONParser parser = new JSONParser();

        try {
            logger.debug("Starting to Parse JSON String to Java ");
            Object obj = parser.parse(j);
            yui_current = (JSONObject) obj;
            logger.debug("YUI configuration file contains: \n\r  " + yui_current);

            // TODO cache yui_current ? to save parse time.


        } catch (ParseException pe) {
            logger.error("position: " + pe.getPosition());
            logger.error(pe.toString());
            throw new RuntimeException("Error Occured while parsing YUI json configuration file position: " + pe.getPosition() + " \n stack trace", pe);
        }

        this.base = (String) yui_current.get(YUI_BASE);
        logger.debug("base is " + this.base);

        this.comboDefaultVersion = this.yuiVersion;

        // testing  what caches we have so far.
        String[] cacheNames = cacheManager.getCacheNames();
        logger.debug("We have Folowing Caches available: " + Arrays.toString(cacheNames));

        this.fullCacheKey = this.base + this.cacheKey;
        Cache c = cacheManager.getCache(this.fullCacheKey);

        if (c != null) {
            logger.debug("we have found Cache item" + c + " for Key " + this.fullCacheKey);
            this.cacheFound = true;


            this.modules = (Map) c.get(YUI_MODULES).getValue();
            this.skin = (Map) c.get(YUI_SKIN).getValue();
            this.rollupModules = (Map) c.get(YUI_ROLLUP).getValue();
            this.globalModules = (List) c.get(YUI_GLOBAL).getValue();
            this.satisfactionMap = (Map) c.get(YUI_SATISFIES).getValue();
            this.depCache = (Map) c.get(YUI_DEPCACHE).getValue();
            this.filters = (Map) c.get(YUI_FILTERS).getValue();

        } else {
            logger.debug("we have NOT  found Cache item  for Key " + this.fullCacheKey);

            if (this._noYUI) {
                this.modules = new HashMap();
            } else {
                JSONObject a1 = (JSONObject) this.yui_current.get("moduleInfo");


                // TODO check for null
                this.modules = a1;
                logger.debug("moduleInfo config is " + this.modules);
                if (this.modules == null) {
                    throw new RuntimeException("Mising \'moduleInfo\'  property from config file");
                }

                if (this.userSuppliedModules != null && this.userSuppliedModules.size() > 0) {
                    this.modules.putAll(this.userSuppliedModules);
                }

                this.skin = (JSONObject) this.yui_current.get(YUI_SKIN);
                logger.debug("skin config is " + this.skin);
                if (this.skin == null) {
                    throw new RuntimeException("Mising" + YUI_SKIN + " property from config file");
                }
                this.skin.put("overrides", new ArrayList());
                this.skin.put(YUI_PREFIX, "skin-");

                this.filters = new HashMap();
                this.filters.put(YUI_RAW, new YUIFilter("-min\\.js", ".js"));
                this.filters.put(YUI_DEBUG, new YUIFilter("-min\\.js", "-debug.js"));


                Iterator it = this.modules.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pairs = (Map.Entry) it.next();
                    String name = (String) pairs.getKey();
                    Object m = pairs.getValue();
                    if (m instanceof Map) {
                        logger.debug("M is instance of Map" + m);
                        if (((Map) m).containsKey(YUI_GLOBAL)) {
                            logger.debug("We found " + YUI_GLOBAL + " in M ");
                            this.globalModules.add(name);
                        }


                        if (((Map) m).containsKey(YUI_SUPERSEDES)) {
                            logger.debug("We found " + YUI_SUPERSEDES + " in M ");
                            if (this.rollupModules == null) {
                                this.rollupModules = new HashMap();
                            }
                            logger.debug("adding to rollupModules key=" + name + " value=" + m);
                            this.rollupModules.put(name, m);

                            JSONArray sups = (JSONArray) ((Map) m).get(YUI_SUPERSEDES);

                            for (Object asup : sups) {
                                this.mapSatisfyingModule((String) asup, name);
                            }
                        }

                    }

                    logger.debug("[key:]" + pairs.getKey() + " =  [Value:] " + pairs.getValue() + "  \n\r");
                }
                logger.debug("[init:] done first pass over Modules ");
            }
        }
    }

    private void updateCache() {

        if (this.fullCacheKey != null) {
            cacheManager.addCache(this.fullCacheKey);
            Cache cache = cacheManager.getCache(this.fullCacheKey);
            cache.put(new Element(YUI_MODULES, this.modules));
            cache.put(new Element(YUI_SKIN, this.skin));
            cache.put(new Element(YUI_ROLLUP, this.rollupModules));
            cache.put(new Element(YUI_GLOBAL, this.globalModules));
            cache.put(new Element(YUI_DEPCACHE, this.depCache));
            cache.put(new Element(YUI_SATISFIES, this.satisfactionMap));
            cache.put(new Element(YUI_FILTERS, this.filters));
            logger.info("[updateCache] Cache has been successfully updated ");
        } else {
            logger.info("[updateCache] cound not initiate Cache because cacheKey is " + fullCacheKey);
        }
    }

    public void load(String... arguments) {

        for (String arg : arguments) {
            this.loadSingle(arg);
        }
    }



    public void setLoaded(String... args) {

        logger.debug(" [setLoaded]  arguments " + Arrays.toString(args));

        for (String arg : args) {
            if (this.modules.containsKey(arg)) {
                logger.debug(" [setLoaded]  module contains " + arg);
                this.loaded.put(arg, arg);
                Object mod = this.modules.get(arg);
                Map sups = this.getSuperceded(arg);

                Iterator it = sups.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pairs = (Map.Entry) it.next();
                    String supname = (String) pairs.getKey();
                    Object val = pairs.getValue();
                    this.loaded.put(supname, supname);
                }

                this.setProcessedModuleType((String) ((Map) mod).get(YUI_TYPE));

            } else {
                logger.debug("YUI_LOADER: undefined module name provided to setLoaded(): " + arg);
                throw new RuntimeException("YUI_LOADER: undefined module name provided to setLoaded(): " + arg);
            }

        }

        //var_export($this->loaded);
    }

    private String formatSkin(String skin, String moduleName) {

        //$prefix = $this->skin[YUI_PREFIX];
        logger.debug("[formatSkin] skin:" + skin + "moduleName:" + moduleName);
        String prefix = (String) this.skin.get(YUI_PREFIX);

        //$prefix = (isset($this->skin[YUI_PREFIX])) ? $this->skin[YUI_PREFIX] : 'skin-';
        // $s = $prefix . $skin;

        String s = prefix + skin;
        if (moduleName != null) {
            s = s + '-' + moduleName;
        }
        logger.debug("[formatSkin] returning :" + s);
        return s;
    }

    private String skinSetup(String name) {
        String skinName = null;
        Map dep = (Map) this.modules.get(name);
        //$this->modules[$name];

        // $this->log("Checking skin for " . $name);

        if (dep != null && dep.containsKey(YUI_SKINNABLE)) {
            Map s = this.skin;
            if (s.containsKey(YUI_OVERRIDES)) {
                Map o = (Map) s.get(YUI_OVERRIDES);
                if (o.containsKey(name)) {
                    Map _names = (Map) o.get(name);
                    Iterator it = _names.entrySet().iterator();
                    // TODO  while is really needed here?
                    while (it.hasNext()) {
                        Map.Entry pairs = (Map.Entry) it.next();
                        String name2 = (String) pairs.getKey();
                        Object over2 = pairs.getValue();

                        skinName = this.formatSkin((String) over2, name2);
                        logger.info("skin if: "+skinName);
                    }

                } else {
                    skinName = this.formatSkin((String) s.get("defaultSkin"), name);
                     logger.info("skin else: "+skinName);
                }

            }
             logger.info("skin before: "+skinName);

            logger.info("this.skins before  "+this.skins);
            this.skins.put(skinName, skinName);

            logger.info("this.skins after  "+this.skins);

            String _skin[] = this.parseSkin(skinName);

            logger.info("_skin[] after parse  "+Arrays.toString(_skin));

            if (_skin != null && _skin.length > 2) {
                String aSkin = _skin[2];
                dep = (Map) this.modules.get(aSkin);
                // this is probably a Bug
                String _package = dep.containsKey(YUI_PKG) ? (String) dep.get(YUI_PKG) : aSkin;
                String path = _package + "/" + s.get(YUI_BASE) + _skin[1] + "/" + _skin[2] + ".css";

                Map skinMap = new HashMap();
                skinMap.put("name", skinName);
                skinMap.put("type", YUI_CSS);
                skinMap.put("path", path);
                skinMap.put("after", s.get(YUI_AFTER));
                this.modules.put(skinName, skinMap);
            } else {
                String path = s.get(YUI_BASE) + _skin[1] + "/" + s.get(YUI_PATH);
                Map skinMap = new HashMap();
                skinMap.put("name", skinName);
                skinMap.put("type", YUI_CSS);
                skinMap.put("path", path);
                skinMap.put("rollup", 3);
                skinMap.put("after", s.get(YUI_AFTER));
                this.modules.put(skinName, skinMap);
                this.rollupModules.put(skinName, skinMap);
            }
        }

        return skinName;

    }

    private void addSkin(String skin) {
        throw new RuntimeException("This method is not Implemented");
    }

    /**
     * Used to output each of the required script tags
     * @method script
     * @return {string}
     */
    public String script() {
        return this.tags(YUI_JS, false);


    }

    /**
     * Used to output each of the required link tags
     * @method css
     * @return {string} (e.g.)
     */
    public String css() {
        return this.tags(YUI_CSS, false);
    }

    /**
     * Used to output each of the required html tags (i.e.) script or link
     * @method tags
     * @param {string} moduleType Type of html tag to return (i.e.) js or css.  Default is both.
     * @param {boolean} skipSort
     * @return {string}
     */
    public String tags(String moduleType, boolean skipSort) {

        return this.processDependencies(YUI_TAGS, moduleType, skipSort, false);
    }

    /**
     * Used to embed the raw JavaScript inline
     * @method script_embed
     * @return {string} Returns the script tag(s) with the JavaScript inline
     */
    public String script_embed() {
        return this.embed(YUI_JS, false);
    }

    /**
     * Used to embed the raw CSS
     * @method css_embed
     * @return {string} (e.g.) Returns the style tag(s) with the CSS inline
     */
    public String gcss_embed() {
        return this.embed(YUI_CSS, false);
    }

    /**
     * Used to output each of the required html tags inline (i.e.) script and/or style
     * @method embed
     * @param {string} moduleType Type of html tag to return (i.e.) js or css.  Default is both.
     * @param {boolean} skipSort
     * @return {string} Returns the style tag(s) with the CSS inline and/or the script tag(s) with the JavaScript inline
     */
    public String embed(String moduleType, boolean skipSort) {

        return this.processDependencies(YUI_EMBED, moduleType, skipSort, false);
    }

    /**
     * Used to output an Array which contains data about the required JavaScript & CSS components
     * @method data
     * @param {string} moduleType Type of html tag to return (i.e.) js or css.  Default is both.
     * @param {boolean} allowRollups
     * @param {boolean} skipSort
     * @return {string}
     */
    public String data(String moduleType, boolean allowRollups, boolean skipSort) {
        if (allowRollups) {
            this.setProcessedModuleType(moduleType);
        }

        String type = YUI_DATA;

        return this.processDependencies(type, moduleType, skipSort, false);
    }

    /**
     * Used to fetch an array of the required JavaScript components
     * @method script_data
     * @return {array} Returns an array of data about each of the identified JavaScript components
     */
    public String script_data() {
        return this.data(YUI_JS, false, false);
    }

    /**
     * Used to fetch an array of the required CSS components
     * @method css_data
     * @return {array} Returns an array of data about each of the identified JavaScript components
     */
    public String css_data() {
        return this.data(YUI_CSS, false, false);
    }

    private Map prune(Map deps, String moduleType) {

        if (moduleType != null) {

            Map newdeps = new HashMap();

            Iterator it = deps.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();
                String name = (String) pairs.getKey();
                //Object val= pairs.getValue();
                newdeps.put(name, true);
            }
            return newdeps;
        } else {
            return deps;
        }
    }

    // @todo restore global dependency support
    private List  getGlobalDependencies(String moduleType) {
        // TODO parameter not used

        return this.globalModules;
    }

    private Map getAllDependencies(String mname, boolean loadOptional, Map completed) {


        String key = YUI_REQUIRES + mname;
        if (loadOptional) {
            key += YUI_OPTIONAL;
        }


        if (this.depCache.containsKey(key)) {
            logger.info("Using cache " + mname);
            return (Map) this.depCache.get(key);
        }


        Map m = (Map) this.modules.get(mname);
        Map reqs = new HashMap();

        logger.debug("M is instance of Map" + m);
        if (m.containsKey(YUI_REQUIRES)) {
            List origreqs = (List) m.get(YUI_REQUIRES);
            for (Object r : origreqs) {
                reqs.put(r, true);
            }

        }


        if (loadOptional && m.containsKey(YUI_OPTIONAL)) {
            List o = (List) m.get(YUI_OPTIONAL);
            for (Object opt : o) {
                reqs.put(opt, true);
            }
        }



        Map _reqs = new HashMap();
        Iterator it = reqs.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            String name = (String) pairs.getKey();

            
            String skinName = this.skinSetup(name);
            if (skinName != null) {
                _reqs.put(skinName, true);
            }

            if (!completed.containsKey(name) && this.modules.containsKey(name)) {
                Map dep = (Map) this.modules.get(name);

                Map newreqs = this.getAllDependencies(name, loadOptional, completed);
                _reqs.putAll(newreqs);
            } else {
            }
        }

        reqs.putAll(_reqs);

        this.depCache.put(key, reqs);

        //$this->depCache[$key] = $reqs;

        return reqs;
    }

    private boolean checkThreshold(Map module, Map moduleList) {

        if (moduleList.size() > 0 && module.containsKey(YUI_ROLLUP)) {
            int matched = 0;
            int thresh = (Integer) module.get(YUI_ROLLUP);
            Iterator it = moduleList.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();
                String moduleName = (String) pairs.getKey();
                Object moddef = pairs.getValue();
                Map m = (Map) module.get(YUI_SUPERSEDES);
                if (m.containsValue(moduleName)) {
                    matched++;
                }

            }
            return (matched >= thresh);
        }
        return false;
    }

    // TODO optimize, currently we just porting one2one
    private Map sortDependencies(String moduleType, boolean skipSort) {
        // only call this if the loader is dirty

        Map reqs = new HashMap();
        Map top = new HashMap();
        Map bot = new HashMap();
        Map notdone = new HashMap();
        Map sorted = new HashMap();
        Map found = new HashMap();

        // add global dependenices so they are included when calculating rollups
        List  globals = this.getGlobalDependencies(moduleType);
        for (Object g : globals) {
            reqs.put(g , true);
        }

        Iterator it2 = this.requests.entrySet().iterator();
        while (it2.hasNext()) {
            Map.Entry pairs = (Map.Entry) it2.next();
            String name = (String) pairs.getKey();
            reqs.put(name, true);
            Object dep = this.modules.get(name);
            Map newreqs = this.getAllDependencies(name, this.loadOptional, new HashMap());
            Iterator it3 = newreqs.entrySet().iterator();
            while (it3.hasNext()) {
                Map.Entry pairs3 = (Map.Entry) it3.next();
                String newname = (String) pairs.getKey();
                if (!reqs.containsKey(newname)) {
                    reqs.put(newname, true);
                }
            }
        }

        if (skipSort) {
            return this.prune(reqs, moduleType);
        }

        if (this.accountedFor.size() > 0 || this.loaded.size() > 0) {
            Iterator it4 = this.accountedFor.entrySet().iterator();
            while (it4.hasNext()) {
                Map.Entry pairs = (Map.Entry) it4.next();
                String name = (String) pairs.getKey();
                if (reqs.containsKey(name)) {
                    logger.info("removing satisfied req (accountedFor) " + name + "\n");
                    reqs.remove(name);
                }
            }

            Iterator it5 = this.loaded.entrySet().iterator();
            while (it5.hasNext()) {
                Map.Entry pairs = (Map.Entry) it5.next();
                String name = (String) pairs.getKey();
                if (reqs.containsKey(name)) {
                    logger.info("removing satisfied req (loaded) " + name + "\n");
                    reqs.remove(name);
                }
            }

        } else if (this.allowRollups) {
            // First we go through the meta-modules we know about to
            // see if the replacement threshold has been met.

            Map _rollups = this.rollupModules;
            if (_rollups.size() > 0) {
                Iterator it6 = _rollups.entrySet().iterator();
                while (it6.hasNext()) {
                    Map.Entry pairs6 = (Map.Entry) it6.next();
                    String name = (String) pairs6.getKey();
                    Object rollup = pairs6.getValue();
                    if (reqs.containsKey(name) && this.checkThreshold((Map) rollup, reqs)) {
                        Iterator it7 = _rollups.entrySet().iterator();
                        while (it7.hasNext()) {
                            Map.Entry pairs7 = (Map.Entry) it7.next();
                            String newname = (String) pairs7.getKey();
                            if (!reqs.containsKey(newname)) {
                                reqs.put(newname, true);
                            }
                        }
                    }
                }
            }

        }


        Iterator it8 = reqs.entrySet().iterator();
        while (it8.hasNext()) {
            Map.Entry pairs8 = (Map.Entry) it8.next();
            String name = (String) pairs8.getKey();
            Object val = pairs8.getValue();

            Map dep = (Map) this.modules.get(name);

            if (dep.containsKey(YUI_SUPERSEDES)) {
                Map override = (Map) dep.get(YUI_SUPERSEDES);
                logger.info("override " + name + ", val: " + val + "\n");
                Iterator it9 = override.entrySet().iterator();
                while (it9.hasNext()) {
                    Map.Entry pairs9 = (Map.Entry) it9.next();
                    String i = (String) pairs9.getKey();
                    Object val2 = pairs9.getValue();

                    if (reqs.containsKey(val2)) {
                        logger.info("Removing (superceded by val) " + val2 + "\n");
                        reqs.remove(val2);
                    }

                    if (reqs.containsKey(i)) {
                        logger.info("Removing (superceded by i) " + val2 + "\n");
                        reqs.remove(i);
                    }
                }
            }
        }


        // move globals to the top
        Iterator it11 = reqs.entrySet().iterator();
        while (it11.hasNext()) {
            Map.Entry pairs11 = (Map.Entry) it11.next();
            String name = (String) pairs11.getKey();
            Object val = pairs11.getValue();
            Map dep = (Map) this.modules.get(name);
            if (dep.containsKey(YUI_GLOBAL) && ((Boolean) dep.get(YUI_GLOBAL)).booleanValue()) {
                top.put(name, name);
            } else {
                notdone.put(name, name);
            }
        }

        if (top.size() > 0) {
            notdone.putAll(top);
        }



        Iterator it12 = this.loaded.entrySet().iterator();
        while (it12.hasNext()) {
            Map.Entry pairs12 = (Map.Entry) it12.next();
            String name = (String) pairs12.getKey();
            this.accountFor(name);
        }

        // $this->log("done: " . var_export($this->loaded, true));

        // keep going until everything is sorted

        int count = 0;
        while (notdone.size() > 0) {
            if (count++ > 200) {
                logger.error("YUI_LOADER ERROR: sorting could not be completed, there may be a circular dependency");
                sorted.putAll(notdone);
                return sorted;
            }

            Map _notdone = new HashMap(notdone);
            Iterator it14 =  _notdone.entrySet().iterator();
            while (it14.hasNext()) {
                Map.Entry pairs14 = (Map.Entry) it14.next();
                String name = (String) pairs14.getKey();

                Map dep = (Map) this.modules.get(name);
                Map newreqs = this.getAllDependencies(name, loadOptional, new HashMap());
                boolean failed = false;
                if (newreqs.size() == 0) {
                    sorted.put(name, name);
                    this.accountFor(name);
                    notdone.remove(name);
                } else {

                    Iterator it15 = newreqs.entrySet().iterator();
                    mainLoop:
                    while (it15.hasNext()) {
                        Map.Entry pairs15 = (Map.Entry) it15.next();
                        String depname = (String) pairs15.getKey();
                        Object depval = pairs15.getValue();
                        if (this.accountedFor.containsKey(depname)) {
                        } else {
                            failed = true;

                            Map tmp = new HashMap();
                            boolean _found = false;

                            Iterator it17 = notdone.entrySet().iterator();
                            while (it17.hasNext()) {
                                Map.Entry pairs17 = (Map.Entry) it17.next();
                                String newname = (String) pairs17.getKey();
                                Object newval = pairs17.getValue();
                                if (this.moduleSatisfies(depname, newname)) {
                                    tmp.put(newname, newname);
                                    notdone.remove(newname);
                                    _found = true;
                                    break;
                                }
                            }

                            if (_found) {
                                notdone.putAll(tmp);
                            } else {

                                logger.error("YUI_LOADER ERROR: requirement for " + depname + " (needed for " + name + ") not found when sorting");
                                notdone.put(name, name);
                                sorted.putAll(notdone);
                                return sorted;
                            }
                            // TODO looks like a bug
                            break mainLoop;
                        }
                    }

                    if (!failed) {
                        sorted.put(name, name);
                        this.accountFor(name);
                        notdone.remove(name);
                    }
                }
            }

        }

        Iterator it18 = sorted.entrySet().iterator();
        while (it18.hasNext()) {
            Map.Entry pairs18 = (Map.Entry) it18.next();
            String name = (String) pairs18.getKey();
            String skinName = this.skinSetup(name);
        }


        //print_r("mid skin");
        //print_r($this->skins);

//        if ( count($this->skins) > 0 ) {
//            foreach ($this->skins as $name => $val) {
//                $sorted[$val] = true;
//            }
//        }

        if (this.skins.size() > 0) {
            Iterator it19 = skins.entrySet().iterator();
            while (it19.hasNext()) {
                Map.Entry pairs19 = (Map.Entry) it19.next();
                String name = (String) pairs19.getKey();
                Object value = pairs19.getValue();
                sorted.put(value, true);
            }
        }

        //print_r("after skin");

        //$this->log("skins " . $this->skins);
        //print_r(" <br /><br /><br /> skin");
        //print_r($this->skins);
        //print_r(" <br /><br /><br /> skin");

        //$this->log("iterations" + $count);
//        $this->dirty = false;
//        $this->sorted = $sorted;

        this.dirty = false;
        this.sorted = sorted;

        return this.prune(sorted, moduleType);
        // store the results, set clear the diry flag
        // return $this->prune($sorted, $moduleType);

    }

    /**
     * Returns true if the supplied $satisfied module is satisfied by the
     * supplied $satisfier module
     */
    public boolean moduleSatisfies(String satisfied, String satisfier) {
        //$this->log("moduleSatisfies: " . $satisfied . ", " . $satisfier);
        if (satisfied.equals(satisfier)) {
            //$this->log("true");
            return true;
        }

        if (this.satisfactionMap.containsKey(satisfied)) {
            Map satisfiers = (Map) this.satisfactionMap.get(satisfied);
            return satisfiers.containsKey(satisfier);
        }

        //$this->log("false");
        return false;
    }

    private void accountFor(String name) {

//        //$this->log("accountFor: " . $name);
//       // $this->accountedFor[$name] = $name;
//        if (isset($this->modules[$name])) {
//            $dep = $this->modules[$name];
//            $sups = $this->getSuperceded($name);
//            foreach ($sups as $supname=>$val) {
//                // $this->log("accounting for by way of supersede: " . $supname);
//                $this->accountedFor[$supname] = true;
//            }
//        }
        this.accountedFor.put(name, name);
        if (this.modules.containsKey(name)) {
            Map dep = (Map) this.modules.get(name);
            Map sups = (Map) this.getSuperceded(name);

            Iterator it = sups.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();
                String supname = (String) pairs.getKey();
                this.accountedFor.put(supname, true);
            }
        }


    }

    /**
     * Retrieve the contents of a remote resource
     * @method getRemoteContent
     * @param {string} url URL to fetch data from
     * @return
     */
    public String getRemoteContent(String urlString) {
        logger.debug("[getRemoteContent] getting Remote Content for url"+urlString);
        Cache c = cacheManager.getCache(this.fullCacheKey);
        Element remote_content = c.get(urlString);
        String content = null;
        HttpURLConnection connection = null;
        DataInputStream in= null;
        BufferedReader d= null;

          logger.debug("[getRemoteContent] Lets check if we have Content for "+urlString+" cached");
        if(remote_content==null || remote_content.getValue()==null){
            try {
                logger.debug("[getRemoteContent]  Nope, No cache, so lets crank up HTTP Connection");
                URL url = new URL(urlString);
                connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("POST");
                connection.setInstanceFollowRedirects(true);
                //connection.setDoOutput(true);
                connection.setReadTimeout(10000);
                connection.connect();

                in = new DataInputStream(connection.getInputStream());
                d = new BufferedReader(new InputStreamReader(in));
                StringBuffer sb = new StringBuffer();
                while (d.ready()) {
                    sb.append(d.readLine());
                    
                }
               content= sb.toString();
                logger.debug("HTML we got  is"+content);
               c.put(new Element(urlString, content));
               return content;

            } catch (IOException ex) {
                logger.error( "IO Exception "+ex.getMessage());
            } catch (Exception e) {
                 logger.error(e.getMessage());
            }
             finally {
                  if(connection!=null){
                    connection.disconnect();
                     connection =null;
                  }
                  
                  if(in!=null) {
                    try {
                        in.close();
                        in=null;
                    } catch (IOException ex) {
                        // do nothing
                    }
                  }

                  if(d!=null) {
                    try {
                        d.close();
                        d=null;
                    } catch (IOException ex) {
                        // do nothing
                    }
                  }

              }
        }else {
           content =(String)remote_content.getValue();
        }
        return content;
    }

    private List getProvides(String name) {
        List l = new ArrayList();
        if (this.modules.containsKey(name)) {
            Map m = (Map) this.modules.get(name);
            if (m.containsKey(YUI_SUPERSEDES)) {
                l = (List) m.get(YUI_SUPERSEDES);
            }
        }
        l.add(name);
        return l;
    }

    /**
     * Retrieve the calculated url for the component in question
     * @method getUrl
     * @param {string} name YUI component name
     */
    public String getUrl(String name) {
        // figure out how to set targets and filters
        String url = "";

        String b = this.base;

        if (this.baseOverrides.containsKey(name)) {
            b = (String) this.baseOverrides.get(name);
        }

        if (this.modules.containsKey(name)) {
            Map m = (Map) this.modules.get(name);
            if (m.containsKey(YUI_FULLPATH)) {
                url = (String) m.get(YUI_FULLPATH);
            } else {
                url = b + m.get(YUI_PATH);
            }
        } else {
            url = b + name;
        }

        if (this.filter != null && !this.filter.trim().equals("")) {


            if (this.filterList.size() > 0 && this.filterList.contains(name)) {
                // skip the filter
            } else if (this.filters.containsKey(this.filter)) {
                YUIFilter yuif = (YUIFilter) this.filters.get(this.filter);
                url.replaceAll(yuif.getSearch(), yuif.getReplace());
            }

        }

        if (this.yuiVersion != null && !yuiVersion.trim().equals("")) {
            String pre = (url.indexOf("?") == -1) ? "?" : "&";
            url += (pre + this.versionKey + "=" + this.yuiVersion);
        }

        logger.debug("[getUrl] returning url:" + url);

        return url;
    }

    public String getComboLink(String type) {
        String url = "";

        if (type.equals(YUI_CSS)) {
            if (this.cssComboLocation != null) {
                url = "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + this.cssComboLocation + "\" />";
            } else {
                url = "<!-- NO YUI CSS COMPONENTS IDENTIFIED -->";
            }
        } else if (type.equals(YUI_JS)) {
            if (this.jsComboLocation != null) {
                if (this.cssComboLocation != null) {
                    url = "\n";
                }
                url += "<script type=\"text/javascript\" src=\"" + this.jsComboLocation + "\"></script>";
            } else {
                url = "<!-- NO YUI JAVASCRIPT COMPONENTS IDENTIFIED -->";
            }
        }

        //Allow for RAW & DEBUG over minified default
        if (this.filter != null && !this.filter.trim().equals("")) {
            if (this.filters.containsKey(this.filter)) {
                YUIFilter yuif = (YUIFilter) this.filters.get(this.filter);
                url.replaceAll(yuif.getSearch(), yuif.getReplace());
            }
        }
        return url;
    }

    public void addToCombo(String name, String type) {
        Map m = (Map) this.modules.get(name);
        String pathToModule = this.comboDefaultVersion + "/build/" + m.get(YUI_PATH);

        if (type.equals(YUI_CSS)) {
            //If this is the first css component then add the combo base path
            if (this.cssComboLocation == null) {
                this.cssComboLocation = this.comboBase + pathToModule;
            } else {
                //Prep for next component
                this.cssComboLocation += "&" + pathToModule;
            }

        } else {
            //If this is the first js component then add the combo base path
            if (this.jsComboLocation == null) {
                this.jsComboLocation = this.comboBase + pathToModule;
            } else {
                //Prep for next component
                this.jsComboLocation += "&" + pathToModule;
            }
        }
    }

    public String getLink(String name, String type) {

        String url = this.getUrl(name);

        if (url == null) {
            return "<!-- PATH FOR " + name + " NOT SPECIFIED -->";
        } else if (type.equals(YUI_CSS)) {
            return "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + url + "\"  />";
        } else {
            return "<script type=\"text/javascript\" src=\"" + url + "\" ></script>";
        }
    }

    private String getRaw(String name) {
        if (!this.embedAvail) {
            return "cURL and/or APC was not detected, so the content can't be embedded";
        }

        //$url = $this->getUrl($name);
        return null;
    }
    private boolean embedAvail = true;

    private String getContent(String name, String type) {

        if (!this.embedAvail) {
            return ("<!--// Curl, and HTTP client is not implemented yet its on my TODO list,  --> \n" + this.getLink(name, type));
        }

        String url = this.getUrl(name);


        //$this->log("URL: " . $url);

        if (url == null) {
            return "<!-- PATH FOR " + name + " NOT SPECIFIED -->";
        } else if (type.equals(YUI_CSS)) {
            return "<style type=\"text/css\">" + this.getRemoteContent(url) + "</style>";
        } else {
            return "<script type=\"text/javascript\">" + this.getRemoteContent(url) + "</script>";
        }

    }

//            foreach ($this->loaded as $name=>$val) {
//                if (isset($reqs[$name])) {
//                    // $this->log( "removing satisfied req (loaded) " . $name . "\n");
//                    unset($reqs[$name]);
//                }
//            }
    public String processDependencies(String outputType, String moduleType, boolean skipSort, boolean showLoaded) {

        if (outputType == null) {
            throw new RuntimeException(" outputType can no tbe Null " + outputType);
        }
        // $html = '';

        StringBuffer html = new StringBuffer();


        // sort the output with css on top unless the output type is json
//        if ((!$moduleType) && (strpos($outputType, YUI_JSON) === false) && $outputType != YUI_DATA) {
//            $this->delayCache = true;
//            $css = $this->processDependencies($outputType, YUI_CSS, $skipSort, $showLoaded);
//            $js  = $this->processDependencies($outputType, YUI_JS, $skipSort, $showLoaded);
//
//            // If the data has not been cached, cache what we have
//            if (!$this->cacheFound) {
//                $this->updateCache();
//            }
//
//            return $css . $js;
//        }

        if ((moduleType == null) && (outputType.indexOf(YUI_JSON) == -1) && !YUI_DATA.equalsIgnoreCase(outputType)) {
            this.delayCache = true;
            String css = processDependencies(outputType, YUI_CSS, skipSort, showLoaded);
            String js = processDependencies(outputType, YUI_JS, skipSort, showLoaded);

            if (!this.cacheFound) {
                this.updateCache();
            }
            return (css + js);
        }


        //  $json = array();

        Map json = new HashMap();
        Map _sorted = new HashMap();

//        if ($showLoaded || (!$this->dirty && count($this->sorted) > 0)) {
//            $sorted = $this->prune($this->sorted, $moduleType);
//        } else {
//            $sorted = $this->sortDependencies($moduleType, $skipSort);
//        }

        if (showLoaded || (this.dirty && this.sorted.size() > 0)) {
            _sorted = this.prune(this.sorted, moduleType);
        } else {
            _sorted = this.sortDependencies(moduleType, skipSort);
        }

        Iterator it = _sorted.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            String name = (String) pairs.getKey();
            if (showLoaded || !this.loaded.containsKey(name)) {
                Map dep = (Map) this.modules.get(name);

                // Java does not support switch on String
                // so untill we refactor this into enum lets cook some spagetti

                if (outputType.equals(YUI_EMBED)) {
                    html.append(this.getContent(name, (String) dep.get(YUI_TYPE)));

                } else if (outputType.equals(YUI_RAW)) {
                    html.append(this.getRaw(name));

                } else if (outputType.equals(YUI_JSON) || outputType.equals(YUI_DATA)) {
                    //html.append(this.getRaw(name));

                    //TODO TASk need to verify this
                    Map subdeps = (Map) dep.get(YUI_TYPE);
                    String _url = this.getUrl(name);
                    List prov = this.getProvides(name);
                    Map subsubsub = new HashMap();
                    subsubsub.put(_url, prov);
                    json.put(subdeps, subsubsub);

                } else if (outputType.equals(YUI_FULLJSON)) {
                    String _name = (String) dep.get(YUI_NAME);
                    Map item = new HashMap();
                    item.put(YUI_TYPE, dep.get(YUI_TYPE));
                    item.put(YUI_URL, dep.get(YUI_URL));
                    item.put(YUI_PROVIDES, dep.get(YUI_TYPE));
                    item.put(YUI_REQUIRES, dep.get(YUI_TYPE));
                    item.put(YUI_OPTIONAL, dep.get(YUI_TYPE));
                    json.put(_name, item);
                } else if (outputType.equals(YUI_TAGS)) {

                    if (this.combine == true) {
                        this.addToCombo(name, (String) dep.get(YUI_TYPE));
                        html.append(this.getComboLink((String) dep.get(YUI_TYPE)));

                    } else {
                        html.append(this.getLink(name, (String) dep.get(YUI_TYPE)));
                        html.append("\n");
                    }
                }

            }

        }

        if (this.cacheFound && !this.delayCache) {
            this.updateCache();
        }
        // If the data has not been cached, and we are not running two
        // rotations for separating css and js, cache what we have
//        if (!$this-  > cacheFound && !$this ->  delayCache) {
//            $this ->  updateCache();
//        }


        // TODO TODO  Finish This
//        if (!empty($json)) {
//            if ($this ->  canJSON()) {
//                $html.  = json_encode($json);
//            } else {
//                $html.  = "<!-- JSON not available, request failed -->";
//            }
//        }
        // TODO TODO  Finish above

        // after the first pass we no longer try to use meta modules
        this.setProcessedModuleType(moduleType);

        // keep track of all the stuff we loaded so that we don't reload
        // scripts if the page makes multiple calls to tags
        this.loaded.putAll(sorted);
        //$this ->  loaded = array_merge($this ->  loaded, $sorted);

        // return the raw data structure
        // TODO TODO  Finish above
//        if ($outputType == YUI_DATA) {
//            return $json;
//        }
        // TODO TODO  Finish above

        if (this.undefined.size() > 0) {
            html.append("<!-- The following modules were requested but are not defined: ");
            html.append("\n");
            html.append(this.undefined);
            html.append("-->\n");
        }
//        if (count($this ->  undefined) > 0) {
//            $html.  = "<!-- The following modules were requested but are not defined: ".join($this ->  undefined, ",").
//
//        " -->\n";
//        }

        return html.toString();
    }

    private Map getSuperceded(String name) {
        logger.debug(" [getSuperceded]  module name " + name);
//        $key = YUI_SUPERSEDES . $name;
        String key = YUI_SUPERSEDES + name;

//
//        if (isset($this->depCache[$key])) {
//            return $this->depCache[$key];
//        }

        if (this.depCache.containsKey(key)) {
            logger.debug(" [getSuperceded]  found key in cache " + key);
            return (Map) this.depCache.get(key);
        }

//        $sups = array();
//        if (isset($this->modules[$name])) {
//            $m = $this->modules[$name];
//            if (isset($m[YUI_SUPERSEDES])) {
//                foreach ($m[YUI_SUPERSEDES] as $supName) {
//                    $sups[$supName] = true;
//                    if (isset($this->modules[$supName])) {
//                        $supsups = $this->getSuperceded($supName);
//                        if (count($supsups) > 0) {
//                            $sups = array_merge($sups, $supsups);
//                        }
//                    }
//                }
//            }
//        }

        Map _sups = new HashMap();
        if (this.modules.containsKey(name)) {
            Object m = this.modules.get(name);

            logger.debug(" [getSuperceded]  Module does contains key= " + name + " value=" + m);
            if (m instanceof Map) {
                logger.debug(" [getSuperceded]  M is instance of Map" + m);

                if (((Map) m).containsKey(YUI_SUPERSEDES)) {
                    logger.debug("[getSuperceded]  We found " + YUI_SUPERSEDES + " in M ");

                    JSONArray sups = (JSONArray) ((Map) m).get(YUI_SUPERSEDES);

                    for (Object supName : sups) {
                        logger.debug(" [getSuperceded]  supName: " + supName);
                        _sups.put(supName, true);
                        if (this.modules.containsKey(supName)) {
                            logger.debug(" [getSuperceded]  Module does contains key= " + supName);
                            Map supsups = this.getSuperceded((String) supName);
                            if (supsups.size() > 0) {
                                logger.debug(" [getSuperceded]  merging Maps");
                                _sups.putAll(supsups);
                            }
                        }
                    }
                }
            }
        } else {
            logger.debug(" [getSuperceded]  Module does not contain " + name);
        }

        this.depCache.put(key, _sups);
        //$this->depCache[$key] = $sups;
        return _sups;
    }

    private String[] parseSkin(String moduleName) {
        String yui_prefix = (String) this.skin.get(YUI_PREFIX);

//        if (strpos( $moduleName, $this->skin[YUI_PREFIX] ) === 0) {
//            return split('-', $moduleName);
//        }
        logger.info("[parseSkin] parsing moduleName :" + moduleName);
        if (moduleName.indexOf(yui_prefix) == 0) {
            String[] retval = moduleName.split("-");
            logger.info("returning splited String :" + Arrays.toString(retval));
            return retval;
        }
        return null;
    }

    public boolean loadSingle(String name) {

        logger.debug("loading single: " + name);
        String[] skinz = this.parseSkin(name);

           logger.debug("skinz are: " + Arrays.toString(skinz));


        if (skinz != null && skinz.length>0) {
                this.skins.put(name, name);
            this.dirty = true;
            return true;
        }

        if (!this.modules.containsKey(name)) {
            this.undefined.put(name, name);
            return false;
        }

//        if (isset($this->loaded[$name]) || isset($this->accountedFor[$name])) {
//            // skip
//            //print_r($name);
//            //var_export($this->loaded);
//            //var_export($this->accountedFor);
//        } else {
//            $this->requests[$name] = $name;
//            $this->dirty = true;
//        }

        if (this.loaded.containsKey(name) || this.accountedFor.containsKey(name)) {
            // skip
            //            //print_r($name);
            //            //var_export($this->loaded);
            //            //var_export($this->accountedFor);
        } else {
            this.requests.put(name, name);
            this.dirty = true;
        }

        return true;
    }

    private void mapSatisfyingModule(String satisfied, String satisfier) {
        logger.debug("[mapSatisfyingModule] with Params satisfied=" + satisfied + " and satisfier=" + satisfier);

        //        if (!isset($this->satisfactionMap[$satisfied])) {
        //            $this->satisfactionMap[$satisfied] = array();
        //        }
        if (this.satisfactionMap == null) {
            this.satisfactionMap = new HashMap();
        }

        if (!this.satisfactionMap.containsKey(satisfied)) {

            this.satisfactionMap.put(satisfied, new HashMap());
        }

        //
        //        $this->satisfactionMap[$satisfied][$satisfier] = true;
        ((Map) this.satisfactionMap.get(satisfied)).put(satisfier, true);
        logger.trace("debuggin satisfactionMap Map \n\r " + this.satisfactionMap);
    }

    private String convertStreamToString(InputStream is) {
        /*
         * To convert the InputStream to String we use the BufferedReader.readLine()
         * method. We iterate until the BufferedReader return null which means
         * there's no more data to read. Each line will appended to a StringBuilder
         * and returned as String.
         */

        logger.debug("Converting Input Stream to String : ");
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }

    private InputStream loadResource(String name) {
        logger.debug("Trying to Load Resource : " + name);
        InputStream in = getClass().getResourceAsStream(name);
        if (in == null) {
            in = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
            if (in == null) {
                in = getClass().getClassLoader().getResourceAsStream(name);
            }
        }
        return in;
    }


       // TODO Possibly depricated Stuff

    
   public void setProcessedModuleType() {
        this.setProcessedModuleType("ALL");
    }
    public void setProcessedModuleType(String moduleType) {
        this.processedModuleTypes.put(moduleType, true);
    }
public boolean hasProcessedModuleType() {
    return hasProcessedModuleType("ALL");
}
  public boolean hasProcessedModuleType(String moduleType) {
        return this.processedModuleTypes.containsKey(moduleType);
 }



}
