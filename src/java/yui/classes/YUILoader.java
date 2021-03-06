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
package yui.classes;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yui.classes.utils.HTTPUtils;
import yui.classes.utils.IOUtils;

/**
 * The YUI Java loader base class which provides dynamic server-side loading for YUI
 * It is a port of YUI PHP loader
 * 
 * YUI Java loader is Used to specify JavaScript and CSS module requirements.  It maintains a dependency
 * tree for these modules so when a module is requested, all of the other modules it
 * depends on are included as well.  By default, the YUI Library is configured, and
 * other modules and their dependencies can be added.
 * 
 * @see http://developer.yahoo.com/yui/phploader/
 * @author leo
 */
public class YUILoader {
    Logger logger = LoggerFactory.getLogger(YUILoader.class);
    public boolean cacheBuster = true;

    /**
     * OUTPUT_TYPE enum that specifies all possible
     * output types supported
     */
    public enum OUTPUT_TYPE {
        /**
         * Full JSON output type
         */
        YUI_FULLJSON("FULLJSON"),
        /**
         * Data output type as JSON
         */
        YUI_DATA("DATA"),
        /**
         * JSON output type
         */
        YUI_JSON("JSON"),
        /**
         * combined html tags <script> and  <link>
         */
        YUI_TAGS("TAGS"),
        /**
         * EMBED output type
         */
        YUI_EMBED("EMBED"),
        /**
         * Raw output type direct js or css code
         */
        YUI_RAW("RAW");

        OUTPUT_TYPE(String _name) {
            this.name = _name;
        }
        private String name;

        @Override
        public String toString() {
            return name;
        }

        public boolean isJSONType() {
            return ((this.toString().indexOf("JSON") != -1) || this.equals(YUI_DATA));
        }
    }

    /**
     * MODULE_TYPE enum that specifies all possible
     * module types supported
     * currently only css and js
     */
    public enum MODULE_TYPE {

        ALL("ALL"),
        CSS("css"),
        JS("js");

        MODULE_TYPE(String _name) {
            this.name = _name;
        }
        private String name;

        @Override
        public String toString() {
            return name;
        }

        public static MODULE_TYPE getValue(String value) {

            return valueOf(value.trim().toUpperCase());

        }
    }


    public static final String YUI_AFTER = "after";
    public static final String YUI_BASE = "base";
    // public static final String YUI_CSS = "css";
    //public static final String YUI_DATA = "DATA";
    public static final String YUI_DEPCACHE = "depCache";

    //public static final String YUI_EMBED = "EMBED";
    public static final String YUI_FILTERS = "filters";
    public static final String YUI_FULLPATH = "fullpath";
    // public static final String YUI_FULLJSON = "FULLJSON";
    public static final String YUI_GLOBAL = "global";
    //public static final String YUI_JS = "js";
    //public static final String YUI_JSON = "JSON";
    public static final String YUI_MODULES = "modules";
    public static final String YUI_SUBMODULES = "submodules";
    public static final String YUI_EXPOUND = "expound";
    public static final String YUI_NAME = "name";
    public static final String YUI_OPTIONAL = "optional";
    public static final String YUI_OVERRIDES = "overrides";
    public static final String YUI_PATH = "path";
    public static final String YUI_PKG = "pkg";
    public static final String YUI_PREFIX = "prefix";
    public static final String YUI_PROVIDES = "provides";


    public static final String YUI_DEBUG = "DEBUG";
    public static final String YUI_RAW = "RAW";
    public static final String YUI_REPLACE = "replace";
    
    public static final String YUI_REQUIRES = "requires";
    public static final String YUI_ROLLUP = "rollup";
    public static final String YUI_SATISFIES = "satisfies";
    public static final String YUI_SEARCH = "search";
    public static final String YUI_SKIN = "skin";
    public static final String YUI_SKINNABLE = "skinnable";
    public static final String YUI_SUPERSEDES = "supersedes";
    //public static final String YUI_TAGS = "TAGS";
    public static final String YUI_TYPE = "type";
    public static final String YUI_URL = "url";
    /* public api variables TODO add set/get methods */

    /**
    * The base directory
    */
    public String base="";

    /**
    * A filter to apply to result urls. This filter will modify the default path for
    * all modules. The default path is the minified version of the files (e.g., event-min.js).
    * Changing the filter alows for picking up the unminified (raw) or debug sources.
    * The default set of valid filters are:  YUI_DEBUG & YUI_RAW
    */
    public String filter = "";
    /**
     * Map of filters keys & filter replacement rules (YUIFilter Objects).
     * Used with filter.
     *
     */
    private Map filters = new HashMap();
    /**
     * A list of modules to apply the filter to.  If not supplied, all
     * modules will have any defined filters applied.  Tip: Useful for debugging.
     */
    private List<String> filterList = new ArrayList<String>();


    /**
    * Should we allow rollups
    */
    public boolean allowRollups;
    /**
    * Whether or not to load optional dependencies for the requested modules
    */
    public boolean loadOptional;


    /**
    * Force rollup modules to be sorted as moved to the top of
    * the stack when performing an automatic rollup.  This has a very small performance consequence.
    * TODO: NOT TESTED
    */
    public boolean rollupsToTop;

    /**
    * The first time we output a module type we allow automatic rollups, this
    * array keeps track of module types we have processed
    */
    private Map processedModuleTypes = new HashMap();

    // all required modules
    private Map requests = new LinkedHashMap();

    // modules that have been been outputted via getLink()/ getComboLink()
    private Map loaded = new HashMap();

    // list of all modules superceded by the list of required modules
    private List<String> superceded;

    /**
     * keeps track of modules that were requested that are not defined
     */
    private Map undefined = new HashMap();

    /**
     * Number modules that were requested that are not defined
     * @return
     */
    public int howManyUndefined() {
        return undefined.size();
    }

    // module load count to catch circular dependencies
    // private  List<String> loadCount;

    /**
     * Used to determine if additional sorting of dependencies is required
     */
    private boolean dirty = true;

    /**
     * List of sorted modules
     * 
     */
    private Map sorted = new LinkedHashMap();
    
    /**
     * List of modules the loader has aleady accounted for
     */
    private List accountedFor = new ArrayList();



    /**
     * the list of required skins
     */
    private Map skins = new HashMap();

    /**
     * Contains the available module metadata
     */
    private Map modules = new HashMap();

    /**
     * cache key (currently ehcache only)
     */
    private String fullCacheKey;

    /**
     * List of modules that have had their base pathes overridden
     */
    private Map baseOverrides = new HashMap();

    /**
     * Used to delay caching of module data
     */
    private boolean delayCache = false;


    /* If the version is set, a querystring parameter is appended to the
     * end of all generated URLs.  This is a cache busting hack for environments
     * that always use the same path for the current version of the library.
     * @property version
     * @type string
     * @default null
     */
    private String yuiVersion = "";
    private String versionKey = "_yuiversion";

    /**
     * Holds the calculated skin definition
     */
    private Map skin = new HashMap();

    /**
     * Holds the module rollup metadata
     */
    private Map rollupModules = new LinkedHashMap();

    /**
     * Holds global module information.  Used for global dependency support.
     * Note: Does not appear to be in use by recent metadata.  Might be deprecated?
     * 
     */
    private List globalModules = new ArrayList();

    /**
     *  Holds information about what modules satisfy the requirements of others
     */
    private Map satisfactionMap = new HashMap();

    /**
     *
     * Holds a cached(memory cached, not ehcached) module dependency list
     */
    private Map depCache = new HashMap();

    /**
     * Combined into a single request using the combo service to pontentially reduce the number of
     * http requests required.  This option is not supported when loading custom modules,
     * TODO: Will be supported later on.
     */
    public boolean combine;

    /**
     * The base path to the combo service.  Uses the Yahoo! CDN service by default.
     * You do not have to set this property to use the combine option. YUI PHP Loader ships
     * with an intrinsic, lightweight combo-handler as well (see combo.php).
     * @property comboBase
     * @type string
     * @default http://yui.yahooapis.com/combo?
     */
    public String comboBase = "http://yui.yahooapis.com/combo?";

    /**
     * Holds the current combo url for the loaded CSS resources.  This is
    *  built with addToCombo and retrieved with getComboLink.  Only used when the combine
    *  is enabled.
     */
    private String cssComboLocation = null;

    /**
     *  Holds the current combo url for the loaded JavaScript resources.  This is
    * built with addToCombo and retrieved with getComboLink.  Only used when the combine
     */
    private String jsComboLocation = null;

    // private stuff and undocumented
    private String comboDefaultVersion;
    private JSONObject yui_current;
    private Map userSuppliedModules;
    private boolean _noYUI;
    private String _jsonConfigFilePrefix = "config";
    private String _jsonConfigFile;
    protected CacheManager cacheManager;
    private boolean customModulesInUse;



    public String target;
    public boolean isCacheEnabled;
    YUILoader() {
    }

    public YUILoader(String version) {
        this(version, "");
    }

    public YUILoader(String version, String cacheKey) {
        this(version, cacheKey, null);
    }

    public YUILoader(String version, String cacheKey, Map modules) {
        this(version, cacheKey, modules, false);
    }

    public YUILoader(String version, String cacheKey, Map modules, boolean noYUI) {

        if (version == null || version.trim().equals("")) {
            throw new RuntimeException("Error: The first parameter of YAHOO_util_Loader must specify which version of YUI to use!");
        }

        this.yuiVersion = version;
        this.userSuppliedModules = modules;
        this.customModulesInUse = (modules != null && modules.size() > 0) ? true : false;
        this._noYUI = noYUI;
        this._jsonConfigFile = "json_" + this.yuiVersion + ".txt";
        this.isCacheEnabled = cacheKey != null ? true : false;
        this.comboDefaultVersion = this.yuiVersion;
        this.parser = new JSONParser();
        this.yui_current = this.loadCachedJSONConfObject(_jsonConfigFile);
        this.base = (String) yui_current.get(YUI_BASE);
        this.fullCacheKey = this.base + cacheKey;
        initCache(this.fullCacheKey);

        logger.debug("base is " + this.base);
        this.init();
    }

    private void cacheConfigObject(String key, JSONAware a) {
    }

    private JSONObject loadCachedJSONConfObject(String configFileName) {
        // TODO cache yui_current ? to save parse time.
        JSONObject obj = null;
        logger.debug("[loadCachedJSONConfObject] ... ");
        if (isCacheEnabled) {
            logger.debug("[loadCachedJSONConfObject] Cache is Enabled ... ");
            Cache c = initCache(_jsonConfigFile);
            Element e = c.get(configFileName);
            if (e != null) {
                logger.debug("[loadCachedJSONConfObject] Found In Cache ... ");
                obj = (JSONObject) e.getValue();
            } else {
                logger.debug("[loadCachedJSONConfObject] Could not Find In Cache ... ");
                obj = _loadJSONConfObject(configFileName);
                logger.debug("[loadCachedJSONConfObject] Updating Cache with newly Parsed JSONConfObject ... ");
                c.put(new Element(configFileName, obj));
            }
        } else {
            logger.debug("[loadCachedJSONConfObject] Cache is Disbled ... ");
            obj = _loadJSONConfObject(configFileName);
        }

        return obj;
    }

    private JSONObject _loadJSONConfObject(String configFile) {
        InputStream in = IOUtils.loadResource(configFile);
        Object obj = null;
        if (in == null) {
            throw new RuntimeException("suitable YUI metadata file: [" + this._jsonConfigFile + "]  Could not be found or Loaded");
        }
        // convert inputStream to String
        String j = IOUtils.convertStreamToString(in);
        // convert json String to Java Object
        try {
            logger.debug("Starting to Parse JSON String to Java ");
            obj = parser.parse(j);
            if (obj == null) {
                throw new RuntimeException("Parsing Resulted in null or 0 sized Object, that means configFile " + configFile
                        + "probably is empty");
            }

            if (!(obj instanceof JSONObject)) {
                throw new RuntimeException("parse JSON file resulted in Object other than JSONObject," + obj.getClass().getName()
                        + " but,this method strictly expects JSONObject ");
            }
            //yui_current = (JSONObject) obj;
            logger.trace("configuration file contains: \n\r  " + obj);

        } catch (ParseException pe) {
            logger.error("position: " + pe.getPosition());
            logger.error(pe.toString());
            throw new RuntimeException("Error Occured while parsing YUI json configuration file position: " + pe.getPosition() + " \n stack trace", pe);
        }

        return (JSONObject) obj;
    }

    private boolean validateCacheMembers(Cache c) {
        logger.info("[validateCacheMembers]  Validating ...");
        if (c == null) {
            logger.debug("[validateCacheMembers]  Cache does not exist...");
            return false;
        }
        try {
            this.modules = (Map) c.get(YUI_MODULES).getValue();
            this.skin = (Map) c.get(YUI_SKIN).getValue();
            this.rollupModules = (Map) c.get(YUI_ROLLUP).getValue();
            this.globalModules = (List) c.get(YUI_GLOBAL).getValue();
            this.satisfactionMap = (Map) c.get(YUI_SATISFIES).getValue();
            this.depCache = (Map) c.get(YUI_DEPCACHE).getValue();
            this.filters = (Map) c.get(YUI_FILTERS).getValue();
            logger.debug("[validateCacheMembers] OK. Cache Members seem to be consistent ...");
        } catch (NullPointerException npe) {
            logger.debug("[validateCacheMembers] Found inconsistency in Cache:" + c.getName() + ", thats ok, Clearing  Cache ....");
            c.removeAll();
            return false;
        }
        return true;
    }
    private JSONParser parser;
    int recoveryCounter = 0;

    private Cache initCache(String key) {
        // testing  what caches we have so far.
        // String[] cacheNames = cacheManager.getCacheNames();
        //logger.debug("We have Folowing Caches available: " + Arrays.toString(cacheNames));

        logger.debug("[initCache] looking for cache: " + key);
        if (cacheManager == null) {
            logger.debug("[initCache] Creating CacheManager");
            cacheManager = CacheManager.create();
        }
        if (!cacheManager.cacheExists(key)) {
            logger.debug("[initCache] Cache not found for: " + key);
            cacheManager.addCache(key);
        } else {
            logger.debug("[initCache] Cache found for: " + key);
        }
        Cache cache = cacheManager.getCache(key);
        return cache;
    }

    private void init() {

        if (yui_current == null) {
            logger.warn("There is inconsistency, yui_current is null, we need to reload and reparse JSON config file");
            this.yui_current = loadCachedJSONConfObject(this._jsonConfigFile);
        }

        Cache c = initCache(this.fullCacheKey);

        if (isCacheEnabled && validateCacheMembers(c)) {
            logger.debug("we have found Cache " + c + " for Key " + this.fullCacheKey);

        } else {
            logger.debug("we have NOT found Consistent Cache Members for " + this.fullCacheKey + " that means we are about to populate cache with members");

            if (this._noYUI) {
                this.modules = new HashMap();
            } else {
                this.modules = (JSONObject) this.yui_current.get("moduleInfo");
            }

            logger.trace("moduleInfo config is " + this.modules);
            if (this.modules == null) {
                throw new RuntimeException("Mising \'moduleInfo\'  property from config file");
            }

            if (this.userSuppliedModules != null && this.userSuppliedModules.size() > 0) {
                this.modules.putAll(this.userSuppliedModules);
            }

            this.skin = (JSONObject) this.yui_current.get(YUI_SKIN);
            logger.trace("skin config is " + this.skin);
            if (this.skin == null) {
                throw new RuntimeException("Mising" + YUI_SKIN + " property from config file");
            }
            this.skin.put("overrides", new ArrayList());
            this.skin.put(YUI_PREFIX, "skin-");

            this.filters = new HashMap();
            this.filters.put(YUI_RAW, new YUIFilter("-min.js", ".js"));
            this.filters.put(YUI_DEBUG, new YUIFilter("-min.js", "-debug.js"));


            Iterator it = this.modules.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();
                String name = (String) pairs.getKey();
                Object m = pairs.getValue();
                if (m instanceof Map) {
                    logger.trace("M is instance of Map" + m);
                    if (((Map) m).containsKey(YUI_GLOBAL)) {
                        logger.trace("We found " + YUI_GLOBAL + " in M ");
                        this.globalModules.add(name);
                    }


                    if (((Map) m).containsKey(YUI_SUPERSEDES)) {
                        logger.trace("We found " + YUI_SUPERSEDES + " in M ");
                        if (this.rollupModules == null) {
                            this.rollupModules = new HashMap();
                        }
                        logger.trace("add to rollupModules key=" + name + " value=" + m);
                        this.rollupModules.put(name, m);

                        JSONArray sups = (JSONArray) ((Map) m).get(YUI_SUPERSEDES);

                        for (Object asup : sups) {
                            this.mapSatisfyingModule((String) asup, name);
                        }
                    }

                }

                logger.trace("[key:]" + pairs.getKey() + " =  [Value:] " + pairs.getValue() + "  \n\r");
            }
            logger.trace("[init:] done first pass over Modules ");

        }
    }

    private void resetCache(String key) {
        logger.info("reseting cache");
        cacheManager.removeCache(key);
        updateCache();
    }

    private Cache updateCache() {
        if (fullCacheKey == null) {
            logger.info("Cache is Turned off");
            return null;
        }
        Cache cache = initCache(this.fullCacheKey);
        if (this.fullCacheKey != null) {
            cache.put(new Element(YUI_MODULES, this.modules));
            cache.put(new Element(YUI_SKIN, this.skin));
            cache.put(new Element(YUI_ROLLUP, this.rollupModules));
            cache.put(new Element(YUI_GLOBAL, this.globalModules));
            cache.put(new Element(YUI_DEPCACHE, this.depCache));
            cache.put(new Element(YUI_SATISFIES, this.satisfactionMap));
            cache.put(new Element(YUI_FILTERS, this.filters));
            logger.debug("[updateCache] Cache has been successfully updated ");
        } else {
            logger.debug("[updateCache] cound not initiate Cache because cacheKey is " + fullCacheKey);
        }
        return cache;
    }

    /**
     * Used to load YUI and/or custom components 
     * @param arguments
     */
    public void load(String... arguments) {

        for (String arg : arguments) {
            this.loadSingle(arg);
        }
    }

    /**
     * Used to mark a module type as processed
     * this method defaults to MODULE_TYPE.ALL
     */
    private void setProcessedModuleType() {
        this.setProcessedModuleType(MODULE_TYPE.ALL);
    }

    /**
     * Used to mark a module type as processed
     * @param moduleType
     */
    private void setProcessedModuleType(MODULE_TYPE moduleType) {
        this.processedModuleTypes.put(moduleType + "", true);
    }

    /**
     * Used to determine if a module type has been processed
     * this method defaults to: MODULE_TYPE.ALL
     */
    private boolean hasProcessedModuleType() {
        return hasProcessedModuleType(MODULE_TYPE.ALL);
    }

    /**
     * Used to determine if a module type has been processed
     * @param moduleType
     */
    private boolean hasProcessedModuleType(MODULE_TYPE moduleType) {
        return this.processedModuleTypes.containsKey(moduleType + "");
    }

    /**
     * Used to specify modules that are already on the page that should not be loaded again
     * @param args
     */
    public void setLoaded(String... args) {

        logger.trace(" [setLoaded]  arguments " + Arrays.toString(args));

        for (String arg : args) {
            if (this.modules.containsKey(arg)) {
                logger.trace(" [setLoaded]  module contains " + arg);
                this.loaded.put(arg, arg);
                Object mod = this.modules.get(arg);


                Map sups = this.getSuperceded(arg);
                Iterator it = sups.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pairs = (Map.Entry) it.next();
                    String supname = (String) pairs.getKey();

                    this.loaded.put(supname, supname);
                }
                String forEnum = (String) ((Map) mod).get(YUI_TYPE);
                logger.info("Found EnumString module type: " + forEnum);

                this.setProcessedModuleType(MODULE_TYPE.getValue(forEnum));

            } else {
                logger.debug("YUI_LOADER: undefined module name provided to setLoaded(): " + arg);
                throw new RuntimeException("YUI_LOADER: undefined module name provided to setLoaded(): " + arg);
            }

        }

    }

    private String[] parseSkin(String moduleName) {
        String yui_prefix = (String) this.skin.get(YUI_PREFIX);

        logger.debug("[parseSkin] parsing moduleName :" + moduleName);
        if (moduleName.indexOf(yui_prefix) == 0) {
            String[] retval = moduleName.split("-");
            logger.debug("returning splited String :" + Arrays.toString(retval));
            return retval;
        }
        return null;
    }

    /**
     * Sets up skin for skinnable modules
     * @method skinSetup
     * @param string $name module name
     * @return {string}
     */
    private String skinSetup(String name) {
        String skinName = null;
        Map dep = (Map) this.modules.get(name);
        //$this->modules[$name];

        logger.debug("Checking skin for " + name);

        if (dep != null && dep.containsKey(YUI_SKINNABLE)) {
            Map s = this.skin;
            if (s.containsKey(YUI_OVERRIDES)) {
                List o = (List) s.get(YUI_OVERRIDES);
                if (o.contains(name)) {
                    Map _names = (Map) o.get(o.indexOf(o));
                    Iterator it = _names.entrySet().iterator();
                    // TODO  while is really needed here?
                    while (it.hasNext()) {
                        Map.Entry pairs = (Map.Entry) it.next();
                        //String name2 = (String) pairs.getKey();
                        Object over2 = pairs.getValue();

                        skinName = this.formatSkin((String) over2, name);

                        logger.debug("skin if: " + skinName);
                    }

                } else {
                    skinName = this.formatSkin((String) s.get("defaultSkin"), name);
                    logger.debug("skin else: " + skinName);
                }

            }

            this.skins.put(skinName, skinName);
            String _skin[] = this.parseSkin(skinName);

            if (_skin != null && _skin.length > 2) {
                String aSkin = _skin[2];
                dep = (Map) this.modules.get(aSkin);
                // this is probably a Bug
                String _package = dep.containsKey(YUI_PKG) ? (String) dep.get(YUI_PKG) : aSkin;
                String path = _package + "/" + s.get(YUI_BASE) + _skin[1] + "/" + _skin[2] + ".css";

                Map skinMap = new HashMap();
                skinMap.put("name", skinName);
                skinMap.put("type", MODULE_TYPE.CSS + "");
                skinMap.put("path", path);
                skinMap.put("after", s.get(YUI_AFTER));
                this.modules.put(skinName, skinMap);
            } else {
                String path = s.get(YUI_BASE) + _skin[1] + "/" + s.get(YUI_PATH);
                Map skinMap = new HashMap();
                skinMap.put("name", skinName);
                skinMap.put("type", MODULE_TYPE.CSS + "");
                skinMap.put("path", path);
                skinMap.put("rollup", 3);
                skinMap.put("after", s.get(YUI_AFTER));
                this.modules.put(skinName, skinMap);
                this.rollupModules.put(skinName, skinMap);
            }
        }

        return skinName;

    }

    private String formatSkin(String skin, String moduleName) {

        logger.debug("[formatSkin] skin:" + skin + "moduleName:" + moduleName);
        String prefix = (String) this.skin.get(YUI_PREFIX);

        String s = prefix + skin;
        if (moduleName != null) {
            s = s + '-' + moduleName;
        }
        logger.debug("[formatSkin] returning :" + s);
        return s;
    }

    private void addSkin(String skin) {
        throw new RuntimeException("This method is not Implemented");
    }

    /**
     *
     * Loads the requested module
     * @param name module the name of a module to load
     * @return
     */
    public boolean loadSingle(String name) {

        logger.debug("loading single: " + name);
        String[] skinz = this.parseSkin(name);

        logger.debug("skinz are: " + Arrays.toString(skinz));


        if (skinz != null && skinz.length > 0) {
            this.skins.put(name, name);
            this.dirty = true;
            return true;
        }

        if (!this.modules.containsKey(name)) {
            this.undefined.put(name, name);
            return false;
        }

        if (this.loaded.containsKey(name) || this.accountedFor.contains(name)) {
        } else {
            logger.trace("putting into requests: " + name);
            this.requests.put(name, name);
            this.dirty = true;
        }

        return true;
    }

    /**
     * Used to output each of the required script tags
     * @return String representation of script tags
     */
    public String script() {
        return this.tags(MODULE_TYPE.JS, false);


    }

    /**
     * Used to output each of the required link tags
     *  @return String representation of link(css) tags
     */
    public String css() {
        return this.tags(MODULE_TYPE.CSS, false);
    }

    /**
     * Used to output each of the required html tags (i.e.) script or link
     * @param moduleType Type of html tag to return (i.e.) js or css.  Default is both.
     * @param skipSort turn off sorting.
     * @return String representation of  tags (script or link)
     */
    public String tags(MODULE_TYPE moduleType, boolean skipSort) {

        return (String)this.processDependencies(OUTPUT_TYPE.YUI_TAGS, moduleType, skipSort, false);
    }

    /**
     * Used to output each of the required html tags (i.e.) script or link
     * defaults to both css and js tags.
     * @param skipSort turn off sorting.
     * @return String representation of  tags (script or link)
     */
    public String tags() {
        // return this.processDependencies(YUI_TAGS, null, false, false);

        return tags(null, false);
    }

    /**
     * Used to embed the raw JavaScript inline
     * @return Returns the script tag(s) with the JavaScript inline
     */
    public String script_embed() {
        return this.embed(MODULE_TYPE.JS, false);
    }

    /**
     * Used to embed the raw CSS
     * @return  (e.g.) Returns the style tag(s) with the CSS inline
     */
    public String css_embed() {
        return this.embed(MODULE_TYPE.CSS, false);
    }

    /**
     * Used to output each of the required html tags inline (i.e.) script and/or style
     * @param moduleType Type of html tag to return (i.e.) js or css.  Default is both.
     * @param skipSort
     * @return Returns the style tag(s) with the CSS inline and/or the script tag(s) with the JavaScript inline
     */
    public String embed(MODULE_TYPE moduleType, boolean skipSort) {

        return (String)this.processDependencies(OUTPUT_TYPE.YUI_EMBED, moduleType, skipSort, false);
    }

    /**
     * Used to fetch an array of the required JavaScript components
     * @return Returns Map of data about each of the identified JavaScript components
     */
    public Map script_data() {
        return this.data(MODULE_TYPE.JS, false, false);
    }

    /**
     * Used to fetch an array of the required CSS components
     * @return Returns Map of data about each of the identified JavaScript components
     */
    public Map css_data() {
        return this.data(MODULE_TYPE.CSS, false, false);
    }

    /**
     * Used to output Map which contains data about the required JavaScript & CSS components
     * @method data
     * @param moduleType Type of html tag to return (i.e.) js or css.  Default is both.
     * @param allowRollups
     * @param skipSort
     * @return Returns Map of data about each of the identified  components
     */
    public Map data(MODULE_TYPE moduleType, boolean allowRollups, boolean skipSort) {
        if (allowRollups) {
            this.setProcessedModuleType(moduleType);
        }

        // TOCO cache

        OUTPUT_TYPE type = OUTPUT_TYPE.YUI_DATA;
        Object res = this.processDependencies(type, moduleType, skipSort, false);


        if(res instanceof Map){
            logger.info("We got JSNO object");
            return (Map)res;
        }else {
             throw new RuntimeException("This should not happen, processDependencies should Return Map instead it Returned: "+res.getClass());
//                logger.info("REMOVE this");
//            try {
//                JSONObject obj = (JSONObject) parser.parse((String)res);
//                return obj;
//            } catch (ParseException ex) {
//
//            } catch (Exception ex) {
//                throw new RuntimeException("something went wrong" + res + " to JSONObject");
//            }
        }
    }

    /**
     * Used to fetch a JSON object with the required JavaScript components
     * @return Returns a JSON String containing urls for each JavaScript component
     */
    public String script_json() {
        Map json =(Map) this.json(MODULE_TYPE.CSS, false, false, false);
        return JSONValue.toJSONString(json);
    }

    /**
     * Used to fetch a JSON object with the required css components
     * @return Returns a JSON String containing urls for each JavaScript component
     */
    public String css_json() {

        Map json = this.json(MODULE_TYPE.CSS, false, false, false);
        return JSONValue.toJSONString(json);
    }

    /**
     * Used to fetch a JSON object with the required JavaScript and CSS components
     * @param moduleType
     * @param allowRollups
     * @param skipSort
     * @param full
     * @return Returns a JSON Map with the required JavaScript and CSS components
     */
    public Map json(MODULE_TYPE moduleType, boolean allowRollups, boolean skipSort, boolean full) {
        if (allowRollups) {
            this.setProcessedModuleType(moduleType);
        }

        // the original JSON output only sent the provides data, not the requires
        OUTPUT_TYPE type = OUTPUT_TYPE.YUI_JSON;

        if (full) {
            type = OUTPUT_TYPE.YUI_FULLJSON;
        }

        return (Map)this.processDependencies(type, moduleType, skipSort, false);

    }

    /**
    * Used to produce the raw JavaScript code inline without the actual script tags
    * @return Returns the raw JavaScript code inline without the actual script tags
    */
    public String script_raw() {
        return this.raw(MODULE_TYPE.JS, false, false);
    }

    /**
    * Used to produce the raw CSS code inline without the actual style tags
    * @return Returns the raw CSS code inline without the actual style tags
    */
    public String css_raw() {
        return this.raw(MODULE_TYPE.CSS, false, false);
    }
    /**
     * Used to produce the raw Javacript and CSS code inline without the actual script or style tags
     * @param moduleType
     * @param allowRollups
     * @param skipSort
     * @return Returns the raw JavaScript and/or CSS code inline without the actual style tags
     */
    public String raw(MODULE_TYPE moduleType, boolean allowRollups, boolean skipSort) {
        return (String)this.processDependencies(OUTPUT_TYPE.YUI_RAW, moduleType, skipSort, false);
    }


    private void accountFor(String name) {
        logger.debug("adding " + name);
        this.accountedFor.add(name);
        if (this.modules.containsKey(name)) {
            Map dep = (Map) this.modules.get(name);
            Map sups = (Map) this.getSuperceded(name);

            for (String supname : (Set<String>) sups.keySet()) {
                this.accountedFor.add(supname);
            }
        }
    }

    //Used during dependecy processing to prune modules from the list of modules requiring further processing
    private Map prune(Map deps, MODULE_TYPE moduleType) {

        if (moduleType != null) {

            Map newdeps = new LinkedHashMap();
            for (String name : (Set<String>) deps.keySet()) {

                Map dep = (Map) this.modules.get(name);
                String d = (String) dep.get(YUI_TYPE);
                if (moduleType.toString().equals(d)) {
                    newdeps.put(name, true);
                }
            }
            return newdeps;
        } else {
            return deps;
        }
    }

    private Map getSuperceded(String name) {
        logger.trace(" [getSuperceded]  module name " + name);
        String key = YUI_SUPERSEDES + name;

        if (this.depCache.containsKey(key)) {
            logger.trace(" [getSuperceded]  found key in cache " + key);
            return (Map) this.depCache.get(key);
        }

        Map _sups = new HashMap();

        if (this.modules.containsKey(name)) {
            Object m = this.modules.get(name);

            logger.trace(" [getSuperceded]  Module does contains key= " + name + " value=" + m);
            if (m instanceof Map) {
                logger.trace(" [getSuperceded]  M is instance of Map" + m);

                if (((Map) m).containsKey(YUI_SUPERSEDES)) {
                    logger.trace("[getSuperceded]  We found " + YUI_SUPERSEDES + " in M ");

                    JSONArray sups = (JSONArray) ((Map) m).get(YUI_SUPERSEDES);

                    for (Object supName : sups) {
                        logger.trace(" [getSuperceded]  supName: " + supName);
                        _sups.put(supName, true);
                        if (this.modules.containsKey(supName)) {
                            logger.trace(" [getSuperceded]  Module does contains key= " + supName);
                            Map supsups = this.getSuperceded((String) supName);
                            if (supsups.size() > 0) {
                                logger.trace(" [getSuperceded]  merging Maps");
                                _sups.putAll(supsups);
                            }
                        }
                    }
                }
            }
        } else {
            logger.trace(" [getSuperceded]  Module does not contain " + name);
        }

        this.depCache.put(key, _sups);
        //$this->depCache[$key] = $sups;
        return _sups;
    }
    int counter = 0;

    private Map getAllDependencies(String mname, boolean loadOptional, Map completed) {
        counter++;
        logger.trace(" [getAllDependencies]  [" + (counter) + "] getting for mname: " + mname + " and Map " + completed);

        String key = YUI_REQUIRES + mname;
        if (loadOptional) {
            key += YUI_OPTIONAL;
        }


        if (this.depCache.containsKey(key)) {
            logger.trace("Using depCache cache for: " + mname);
            return (Map) this.depCache.get(key);
        }


        Map m = (Map) this.modules.get(mname);
        List mProvides = (List) this.getProvides(mname);
        Map reqs = new HashMap();


        // NEW
        if (m.containsKey(YUI_EXPOUND)) {
            if (!completed.containsKey(mname)) {
                Map _cParam = new HashMap();
                _cParam.put(mname, true);
                completed.putAll(this.getAllDependencies((String) m.get(YUI_EXPOUND), loadOptional, _cParam));
                reqs = completed;
            }
        }

        logger.trace("M is instance of Map" + m);
        if (m.containsKey(YUI_REQUIRES)) {
            List origreqs = (List) m.get(YUI_REQUIRES);
            for (Object r : origreqs) {
                reqs.put(r, true);
                reqs.putAll(this.getAllDependencies((String) r, loadOptional, reqs));
            }
        }

        //Add any submodule requirements not provided by the rollups
        if (m.containsKey(YUI_SUBMODULES)) {
            logger.trace("M contains YUI_SUBMODULES " + YUI_SUBMODULES);

            Map submodules = (Map) m.get(YUI_SUBMODULES);


            if (submodules != null && submodules.size() > 0) {
                logger.trace(" submodules " + submodules);

                for (Iterator it = submodules.entrySet().iterator(); it.hasNext();) {
                    Map.Entry pairs = (Map.Entry) it.next();
                    String name = (String) pairs.getKey();
                    Map submodule = (Map) pairs.getValue();

                    List subreqs = (List) submodule.get(YUI_REQUIRES);
                    if (subreqs != null && subreqs.size() > 0) {
                        logger.trace(" subreqs " + subreqs);
                        for (Object sr : subreqs) {
                            if (!mProvides.contains(sr) && !this.accountedFor.contains(sr)) {
                                if (!reqs.containsKey(sr)) {
                                    reqs.put(sr, true);
                                    reqs.putAll(this.getAllDependencies((String) sr, loadOptional, reqs));
                                }
                            }

                        }
                    }
                }
            }
        }


        //Add any superseded requirements not provided by the rollup and/or rollup submodules
        if (m.containsKey(YUI_SUPERSEDES)) {
            List<String> supersededModules = (List<String>) m.get(YUI_SUPERSEDES);
            for (String supersededModule : supersededModules) {
                logger.trace("supersededModule: ", supersededModule);
                Map _supModules = (Map) this.modules.get(supersededModule);
                logger.trace("supersededModules All: ", _supModules);
                if (_supModules != null && _supModules.containsKey(YUI_REQUIRES)) {
                    List yuireqs = (List) _supModules.get(YUI_REQUIRES);
                    logger.trace("supersededModules yuireqs ", yuireqs);
                    for (Object supersededModuleReq : yuireqs) {
                        if (!mProvides.contains(supersededModuleReq)) {
                            if (!reqs.containsKey(supersededModuleReq)) {
                                reqs.put(supersededModuleReq, true);
                                reqs.putAll(this.getAllDependencies((String) supersededModuleReq, loadOptional, reqs));
                            }
                        }
                    }
                }

                if (_supModules != null && _supModules.containsKey(YUI_SUBMODULES)) {
                    List<Map> supersededSubmodules = (List<Map>) _supModules.get(YUI_SUBMODULES);
                    for (Map supersededSubmodule : supersededSubmodules) {
                        List ssmProvides = (List) this.getProvides(supersededModule);
                        List supersededSubreqs = (List) supersededSubmodule.get(YUI_REQUIRES);
                        for (Object ssr : supersededSubreqs) {
                            if (!ssmProvides.contains(ssr)) {
                                if (!reqs.containsKey(ssr)) {
                                    reqs.put(ssr, true);
                                    reqs.putAll(this.getAllDependencies((String) ssr, loadOptional, reqs));
                                }
                            }
                        }
                    }
                }
            }
        }

        if (loadOptional && m.containsKey(YUI_OPTIONAL)) {
            List o = (List) m.get(YUI_OPTIONAL);
            for (Object opt : o) {
                reqs.put(opt, true);
            }
        }

        this.depCache.put(key, reqs);

        return reqs;
    }

    // @todo restore global dependency support
    private List getGlobalDependencies(MODULE_TYPE moduleType) {
        // TODO parameter not used
        return this.globalModules;
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

    /**
     * Used to override the base dir for specific set of modules (Note: not supported when using the combo service)
     * @method overrideBase
     * @param base Base path (e.g.) 2.6.0/build
     * @param modules Module names of which to override base
     */
    public void overrideBase(String base, List<String> modules) {

        for (String name : modules) {
            this.baseOverrides.put(name, base);
        }
    }

    /**
     * Used to determine if one module is satisfied by provided array of modules
     * @method listSatisfies
     * @param {string} satisfied Module name
     * @param {array} moduleList List of modules names
     * @return {boolean}
     */
    public boolean listSatisfies(String satisfied, Map moduleList) {
        logger.debug("listSatisfies for" + satisfied);
        logger.debug("listSatisfies " + moduleList);
        if (moduleList.containsKey(satisfied)) {
            return true;
        } else {
            if (this.satisfactionMap.containsKey(satisfied)) {
                Map satisfiers = (Map) satisfactionMap.get(satisfied);
                for (String name : (Set<String>) satisfiers.keySet()) {
                    if (moduleList.containsKey(name)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean checkThreshold(Map module, Map moduleList) {

        if (moduleList.size() > 0 && module.containsKey(YUI_ROLLUP)) {
            long matched = 0;
            long thresh = (Long) module.get(YUI_ROLLUP);

            for (String moduleName : (Set<String>) moduleList.keySet()) {

                List m = (List) module.get(YUI_SUPERSEDES);
                if (m.contains(moduleName)) {
                    matched++;
                }
            }
            return (matched >= thresh);
        }
        return false;
    }

    private void sortDependencies_fillRequests(MODULE_TYPE moduleType, Map reqs) {
        // add global dependenices so they are included when calculating rollups
        List globals = this.getGlobalDependencies(moduleType);

        for (Object g : globals) {
            reqs.put(g, true);
        }


        for (String name : (Set<String>) this.requests.keySet()) {
            reqs.put(name, true);
            Object dep = this.modules.get(name);
            Map newreqs = this.getAllDependencies(name, this.loadOptional, new HashMap());

            for (String newname : (Set<String>) newreqs.keySet()) {
                if (!reqs.containsKey(newname)) {
                    reqs.put(newname, true);
                }
            }
        }
    }

    private void sortDependencies_removeAccountedFor(Map reqs) {

        for (Object name : this.accountedFor) {
            if (reqs.containsKey(name)) {
                logger.debug("removing satisfied req (accountedFor) " + name + "\n");
                reqs.remove(name);
            }
        }

        for (String name : (Set<String>) this.loaded.keySet()) {
            if (reqs.containsKey(name)) {
                logger.debug("removing satisfied req (loaded) " + name + "\n");
                reqs.remove(name);
            }
        }

    }
    // TODO optimize, currently we just porting one2one

    private Map sortDependencies(MODULE_TYPE moduleType, boolean skipSort) {
        // only call this if the loader is dirty

        Map reqs = new LinkedHashMap();
        List top = new LinkedList();
        List<String> notdone = new LinkedList();
        Map _sorted = new LinkedHashMap();

        sortDependencies_fillRequests(moduleType, reqs);
        logger.trace("reqs are:  " + reqs);

        if (skipSort) {
            return this.prune(reqs, moduleType);
        }

        if (this.accountedFor.size() > 0 || this.loaded.size() > 0) {
            sortDependencies_removeAccountedFor(reqs);

        } else if (this.allowRollups) {
            // First we go through the meta-modules we know about to
            // see if the replacement threshold has been met.

            Map _rollups = this.rollupModules;
            // logger.debug("_rollups "+rollupModules);

            logger.debug("reqs before " + reqs);
            if (_rollups.size() > 0) {
                for (Iterator it = _rollups.entrySet().iterator(); it.hasNext();) {
                    Map.Entry pairs = (Map.Entry) it.next();
                    String name = (String) pairs.getKey();
                    Object rollup = pairs.getValue();
                    if (!reqs.containsKey(name) && this.checkThreshold((Map) rollup, reqs)) {
                        reqs.put(name, true);
                        // Object dep = this.modules.get(name);
                        Map newreqs = this.getAllDependencies(name, loadOptional, reqs);

                        for (String newname : (Set<String>) newreqs.keySet()) {
                            if (!reqs.containsKey(newname)) {
                                reqs.put(newname, true);
                            }
                        }
                    }
                }
            }

            logger.debug("reqs after " + reqs);
        }

        Map _reqs = new LinkedHashMap(reqs);
        for (Iterator it = _reqs.entrySet().iterator(); it.hasNext();) {
            Map.Entry pairs = (Map.Entry) it.next();
            String name = (String) pairs.getKey();
            logger.trace("getting module for " + name);
            Map dep = (Map) this.modules.get(name);

            if (dep.containsKey(YUI_SUPERSEDES)) {
                List<String> override = (List<String>) dep.get(YUI_SUPERSEDES);

                logger.trace("override " + name);
                logger.trace("overrides are " + override);

                for (String val2 : override) {
                    //String i = (String) pairs2.getKey();
                    //Object val2 = pairs2.getValue();

                    if (reqs.containsKey(val2)) {
                        logger.trace("Removing (superceded by val) " + val2 + "\n");
                        reqs.remove(val2);
                    }

//                    if (reqs.containsKey(i)) {
//                        logger.debug("Removing (superceded by i) " + val2 + "\n");
//                        reqs.remove(i);
//                    }
                }
            }
        }

        // move globals to the top
        for (String name : (Set<String>) reqs.keySet()) {
            Map dep = (Map) this.modules.get(name);
            // TODO is it boolean or just check if it exitst?
            if (dep.containsKey(YUI_GLOBAL) && ((Boolean) dep.get(YUI_GLOBAL)).booleanValue()) {
                top.add(name);
            } else {
                notdone.add(name);
            }
        }

        if (top.size() > 0) {
            notdone.addAll(top);
        }

        for (String name : (Set<String>) this.loaded.keySet()) {
            logger.debug("sortDependencies 1 add to accountFor " + name);
            this.accountFor(name);
        }

        logger.debug("printing notdone: [" + notdone + "] ");
        int count = 0;
        while (notdone.size() > 0) {
            ++count;
            if (count > 200) {
                logger.error("YUI_LOADER ERROR: sorting could not be completed, there may be a circular dependency");
                for (Object name : notdone) {
                    _sorted.put(name, name);
                }
                //_sorted.putAll(notdone);
                return _sorted;
            }


            logger.debug("mainLoop: [" + count + "] ");
            logger.debug("notdone size: [" + notdone.size() + "] ");

            LinkedList<String> _notdone = new LinkedList(notdone);
            logger.debug("notdone begin : [" + notdone + "] ");


            mainLoop:
            for (String name : _notdone) {
                logger.debug(" _notdone name: [" + name + "] ");
                Map dep = (Map) this.modules.get(name);
                Map newreqs = this.getAllDependencies(name, loadOptional, new HashMap());



                this.accountFor(name);

                if (dep.containsKey(YUI_AFTER)) {
                    List after = (List) dep.get(YUI_AFTER);
                    for (Object a : after) {
                        newreqs.put(a, true);
                    }
                }

                logger.debug("<br>");
                logger.debug("printing newreqs  for " + name);
                logger.debug("newreqs size: [" + newreqs.size() + "] ");
                logger.debug("newreqs: " + newreqs);
                logger.debug("<br>");


                if (newreqs.size() > 0) {

                    newreqs:
                    for (String depname : (Set<String>) newreqs.keySet()) {
                        if (this.accountedFor.contains(depname) || this.listSatisfies(depname, _sorted)) {
                            logger.debug("we have acounted : [" + depname + "] ");
                        } else {
                            List<String> tmp = new LinkedList<String>();
                            boolean _found = false;
                            for (String newname : notdone) {
                                if (this.moduleSatisfies(depname, newname)) {
                                    tmp.add(newname);
                                    logger.debug("notdone Old  size: [" + notdone.size() + "] ");
                                    Object retval = notdone.remove(newname);
                                    logger.debug("removing from notdone: [" + newname + "]  " + retval + " and was remove successfull : " + (retval != null));
                                    logger.debug("notdone New size: [" + notdone.size() + "] ");
                                    _found = true;
                                    break;
                                }
                            }
                            if (_found) {
                                logger.debug("notdone before: [" + notdone + "] ");
                                notdone.addAll(0, tmp);
                                logger.debug("notdone after: [" + notdone + "] ");
                            } else {
                                logger.error("YUI_LOADER ERROR: requirement for " + depname + " (needed for " + name + ") not found when sorting");
                                notdone.add(depname);
                            }
                            logger.debug("BEFORE BREAK:  ");
                            break mainLoop;
                        }
                    }
                }
                _sorted.put(name, name);
                logger.debug("removing 2 :  " + name);
                notdone.remove(name);
            }
        }
        for (String name : (Set<String>) _sorted.keySet()) {
            String skinName = this.skinSetup(name);
        }


        if (this.skins.size() > 0) {
            for (String value : (Collection<String>) skins.values()) {
                logger.trace("[sortDependencies] putting into sorted value is " + value);
                _sorted.put(value, true);
            }
        }
        this.dirty = false;
        this.sorted = _sorted;
        Map retval = this.prune(_sorted, moduleType);
        logger.trace("_sorted" + this.sorted);
        logger.trace("retval " + retval);
        return retval;

    }

    private void mapSatisfyingModule(String satisfied, String satisfier) {
        logger.trace("[mapSatisfyingModule] with Params satisfied=" + satisfied + " and satisfier=" + satisfier);


        if (this.satisfactionMap == null) {
            this.satisfactionMap = new HashMap();
        }

        if (!this.satisfactionMap.containsKey(satisfied)) {

            this.satisfactionMap.put(satisfied, new HashMap());
        }

        ((Map) this.satisfactionMap.get(satisfied)).put(satisfier, true);
        logger.trace("debuggin satisfactionMap Map \n\r " + this.satisfactionMap);
    }

    // TODO refactor into this
    private String processDependenciesAsString(OUTPUT_TYPE outputType, MODULE_TYPE moduleType, boolean skipSort, boolean showLoaded) {
         if (outputType == null || (outputType.isJSONType())) {
            throw new RuntimeException(" outputType can not be Null or non String type, JSON types are not proccessed by this method: " + outputType);
        }

        Map _sorted = new HashMap();
        StringBuffer html = new StringBuffer();
        

        if (showLoaded || (!this.dirty && this.sorted.size() > 0)) {
            _sorted = this.prune(this.sorted, moduleType);
        } else {
            _sorted = this.sortDependencies(moduleType, skipSort);
        }

        logger.info("------------------SORTED-------------------------");
        logger.info("--[" +_sorted+"]-");

        logger.debug("-------------------------------------------");
        logger.debug("dependencies moduleType  :" + moduleType + "  output type " + outputType);

        Iterator it = _sorted.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            String name = (String) pairs.getKey();
            if (showLoaded || !this.loaded.containsKey(name)) {
                Map dep = (Map) this.modules.get(name);
                String modTypeString  =(String)  dep.get(YUI_TYPE);
                switch (outputType) {
                    case YUI_EMBED:
                        html.append(this.getContent(name, MODULE_TYPE.getValue(modTypeString)));
                        break;

                    case YUI_RAW:
                        html.append(this.getRaw(name));
                        break;
                    case YUI_TAGS:
                    default:
                        
                        if (this.combine == true && !this.customModulesInUse) {
                            
                            this.addToCombo(name, MODULE_TYPE.getValue(modTypeString));
                            html = new StringBuffer();
                            html.append(this.getComboLink(MODULE_TYPE.getValue(modTypeString)));
                            //logger.info("combo html"+html.toString());
                        } else {
                            html.append(this.getLink(name, MODULE_TYPE.getValue(modTypeString)));
                            html.append("\n");
                        }
                        break;
                }
            }

        }


        if (!this.delayCache) {
            this.updateCache();
        }

      

        // after the first pass we no longer try to use meta modules
        this.setProcessedModuleType(moduleType);

        this.loaded.putAll(_sorted);

        if (this.combine) {
            this.clearComboLink(moduleType);
        }


        if (this.undefined.size() > 0) {
            html.append("<!-- The following modules were requested but are not defined: ");
            html.append("\n");
            html.append(this.undefined);
            html.append("-->\n");
        }

        return html.toString();

    }

    // TODO refactor into this
    private Map processDependenciesAsJSON(OUTPUT_TYPE outputType, MODULE_TYPE moduleType, boolean skipSort, boolean showLoaded) {
        if (outputType == null || (!outputType.isJSONType())) {
            throw new RuntimeException(" outputType can not be Null or non JSON type: " + outputType);
        }

        Map json = new LinkedHashMap();
        Map _sorted = new HashMap();

        if (showLoaded || (!this.dirty && this.sorted.size() > 0)) {
            _sorted = this.prune(this.sorted, moduleType);
        } else {
            _sorted = this.sortDependencies(moduleType, skipSort);
        }

        logger.info("------------------SORTED-------------------------");
        logger.info("--[" +_sorted+"]-");

        logger.debug("-------------------------------------------");
        logger.debug("dependencies moduleType  :" + moduleType + "  output type " + outputType);

        Iterator it = _sorted.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            String name = (String) pairs.getKey();
            if (showLoaded || !this.loaded.containsKey(name)) {
                Map dep = (Map) this.modules.get(name);
                //String modTypeString  =(String)  dep.get(YUI_TYPE);
                switch (outputType) {
                    case YUI_JSON:
                    case YUI_DATA:
                        //html.append(this.getRaw(name));
                        //TODO TASk need to verify this

                        String subdeps = (String) dep.get(YUI_TYPE);
                        String _url = this.getUrl(name);
                        List prov = this.getProvides(name);
                        Map subsubsub = new LinkedHashMap();
                        subsubsub.put(_url, prov);
                        Object m = json.get(subdeps);
                        if (m != null) {
                            ((Map) m).putAll(subsubsub);
                        } else {
                            json.put(subdeps, subsubsub);
                        }

                        logger.info("JSOn for YUI_JSON or YUI_DATA is" +json.toString());
                        break;

                    case YUI_FULLJSON:
                        String _name = (String) dep.get(YUI_NAME);
                        logger.info("name for Dep " + dep + " is " + _name);

                        Map item = new HashMap();
                        item.put(YUI_TYPE, dep.get(YUI_TYPE));
                        item.put(YUI_URL, dep.get(YUI_URL));
                        item.put(YUI_PROVIDES, dep.get(YUI_TYPE));
                        item.put(YUI_REQUIRES, dep.get(YUI_TYPE));
                        item.put(YUI_OPTIONAL, dep.get(YUI_TYPE));
                        json.put(_name, item);
                        logger.info("name for Dep " + json.size());
                        logger.info("JSOn for YUI_FULLJSON is" +json.toString());
                        break;
                    default:
                        throw new RuntimeException("Invalid outputType for processDependenciesAsJSON " + outputType);
                }
            }

        }
       this.updateCache();

        if (!json.isEmpty()) {
            if (this.canJSON()) {
                logger.info("JSON DEBUG"+json.toString());
            }
        }else {

            logger.info("JSON Object was empty");
        }

        // after the first pass we no longer try to use meta modules
        this.setProcessedModuleType(moduleType);

        this.loaded.putAll(_sorted);
        return json;
    }

    private Object processDependencies(OUTPUT_TYPE outputType, MODULE_TYPE moduleType, boolean skipSort, boolean showLoaded) {

        if (outputType == null) {
            throw new RuntimeException(" outputType can not be " + outputType);
        }

        if ((moduleType == null) && (!outputType.isJSONType())) {
            String css = (String)processDependenciesAsString(outputType, MODULE_TYPE.CSS, skipSort, showLoaded);
            String js = (String)processDependenciesAsString(outputType, MODULE_TYPE.JS, skipSort, showLoaded);
            logger.debug("CSS dependencies are :" + css);
            logger.debug("JS dependencies are :" + js);
            this.updateCache();
            return (css + js);
        }

        Object retVal = null;
            switch (outputType) {
                    case YUI_DATA:
                    case YUI_JSON:
                    case YUI_FULLJSON:
                       retVal = processDependenciesAsJSON(outputType, moduleType, skipSort, showLoaded);
                     break;
                default:
                       retVal = processDependenciesAsString(outputType, moduleType, skipSort, showLoaded);
                break;
            }

          return retVal;
//        Map json = new JSONObject();
//        Map _sorted = new HashMap();
//
//        if (showLoaded || (!this.dirty && this.sorted.size() > 0)) {
//            _sorted = this.prune(this.sorted, moduleType);
//        } else {
//            _sorted = this.sortDependencies(moduleType, skipSort);
//        }
//
//        logger.debug("-------------------------------------------");
//        logger.debug("dependencies moduleType  :" + moduleType + "  output type " + outputType);
//
//        Iterator it = _sorted.entrySet().iterator();
//        while (it.hasNext()) {
//            Map.Entry pairs = (Map.Entry) it.next();
//            String name = (String) pairs.getKey();
//            if (showLoaded || !this.loaded.containsKey(name)) {
//                Map dep = (Map) this.modules.get(name);
//                String modTypeString  =(String)  dep.get(YUI_TYPE);
//                switch (outputType) {
//                    case YUI_EMBED:
//                        html.append(this.getContent(name, MODULE_TYPE.getValue(modTypeString)));
//                        break;
//
//                    case YUI_RAW:
//                        html.append(this.getRaw(name));
//                        break;
//
//                    case YUI_JSON:
//                    case YUI_DATA:
//                        //html.append(this.getRaw(name));
//                        //TODO TASk need to verify this
//
//                        String subdeps = (String) dep.get(YUI_TYPE);
//                        String _url = this.getUrl(name);
//                        List prov = this.getProvides(name);
//                        Map subsubsub = new HashMap();
//                        subsubsub.put(_url, prov);
//                        Object m = json.get(subdeps);
//                        if (m != null) {
//                            ((Map) m).putAll(subsubsub);
//                        } else {
//                            json.put(subdeps, subsubsub);
//                        }
//
//                        break;
//
//                    case YUI_FULLJSON:
//                        String _name = (String) dep.get(YUI_NAME);
//                        logger.info("name for Dep " + dep + " is " + _name);
//
//                        Map item = new HashMap();
//                        item.put(YUI_TYPE, dep.get(YUI_TYPE));
//                        item.put(YUI_URL, dep.get(YUI_URL));
//                        item.put(YUI_PROVIDES, dep.get(YUI_TYPE));
//                        item.put(YUI_REQUIRES, dep.get(YUI_TYPE));
//                        item.put(YUI_OPTIONAL, dep.get(YUI_TYPE));
//                        json.put(_name, item);
//                        logger.info("name for Dep " + json.size());
//                        logger.info("name for Dep " + json);
//                        break;
//                    case YUI_TAGS:
//                    default:
//
//                        if (this.combine == true && !this.customModulesInUse) {
//
//                            this.addToCombo(name, MODULE_TYPE.getValue(modTypeString));
//                            html = new StringBuffer();
//                            html.append(this.getComboLink(MODULE_TYPE.getValue(modTypeString)));
//                            //logger.info("combo html"+html.toString());
//                        } else {
//                            html.append(this.getLink(name, MODULE_TYPE.getValue(modTypeString)));
//                            html.append("\n");
//                        }
//                        break;
//                }
//            }
//
//        }
//
//        // TODO there is a bug if we try to load same resource secod time
//        // inside loaded and never gets fetched so JSOn is emtyp need to fix.
//        // right now I just check for NPE
//        if (!this.delayCache) {
//            this.updateCache();
//        }
//
//        if (!json.isEmpty()) {
//            if (this.canJSON()) {
//                html.append(json.toString());
//
//                logger.info("JSON DEBUG"+html.toString());
//            } else {
//                html.append("Can not ENCODE to JSON, this should not happen");
//            }
//        }
//
//        // after the first pass we no longer try to use meta modules
//        this.setProcessedModuleType(moduleType);
//
//        this.loaded.putAll(_sorted);
//
//        if (this.combine) {
//            this.clearComboLink(moduleType);
//        }
//
//        if (outputType.equals(OUTPUT_TYPE.YUI_DATA)) {
//            return json.toString();
//        }
//
//        if (this.undefined.size() > 0) {
//            html.append("<!-- The following modules were requested but are not defined: ");
//            html.append("\n");
//            html.append(this.undefined);
//            html.append("-->\n");
//        }
//
//
//        return html.toString();
    }

    public boolean canJSON() {
        return true;
    }

    /**
     * Clears the combo url of already loaded modules for a specific resource type.  Prevents
     * duplicate loading of modules if the page makes multiple calls to tags, css, or script.
     * @method clearComboLink
     * @param {string} type Resource type (i.e.) YUI_JS or YUI_CSS
     */
    private void clearComboLink(MODULE_TYPE type) {
        switch (type) {
            case CSS:
                this.cssComboLocation = null;
                break;
            case JS:
                this.jsComboLocation = null;
                break;
            default:
                this.cssComboLocation = null;
                this.jsComboLocation = null;
                break;
        }
    }

    /**
     * Retrieve the contents of a remote resource
     * @method getRemoteContent
     * @param {string} url URL to fetch data from
     * @return string
     */
    public String getRemoteContent(String urlString) {
        logger.debug("[getRemoteContent] getting Remote Content for url" + urlString);
        Cache c = updateCache();
        Element el = null;
        if (c != null) {
            el = c.get(urlString);
        }
        String content = null;

        logger.debug("[getRemoteContent] Lets check if we have Content for " + urlString + " cached");
        if (el == null) {
            content = HTTPUtils.getRemoteContent(urlString);
            // Experimenting: content =HTTPUtils.getRemoteContentNIO(urlString);

        } else {
            content = (String) el.getValue();
        }
        return content;
    }

    private List getProvides(String name) {
        List l = new LinkedList();
        l.add(name);

        if (this.modules.containsKey(name)) {
            Map m = (Map) this.modules.get(name);
            if (m.containsKey(YUI_SUPERSEDES)) {
                List _l = (List) m.get(YUI_SUPERSEDES);
                l.addAll(_l);
            }
        }

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
                // logger.info("url before"+url+" search for "+yuif.getSearch()+ " and replace it with "+yuif.getReplace());
                url = url.replace(yuif.getSearch(), yuif.getReplace());
                // logger.info("url after"+url);
            }

        }


        if (this.cacheBuster && this.yuiVersion != null && !yuiVersion.trim().equals("")) {
            String pre = (url.indexOf("?") == -1) ? "?" : "&";
            url += (pre + this.versionKey + "=" + this.yuiVersion);
        }

        logger.debug("[getUrl] returning url:" + url);

        return url;
    }

    public String getComboLink(MODULE_TYPE type) {
        String url = "";

        switch (type) {
            case CSS:
                if (this.cssComboLocation != null) {
                    url = "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + this.cssComboLocation + "\" />";
                } else {
                    url = "<!-- NO YUI CSS COMPONENTS IDENTIFIED -->";
                }
                break;
            case JS:
                if (this.jsComboLocation != null) {
                    if (this.cssComboLocation != null) {
                        url = "\n";
                    }
                    url += "<script type=\"text/javascript\" src=\"" + this.jsComboLocation + "\"></script>";
                } else {
                    url = "<!-- NO YUI JAVASCRIPT COMPONENTS IDENTIFIED -->";
                }

                break;
            default:
                break;
        }

        //Allow for RAW & DEBUG over minified default
        if (this.filter != null && !this.filter.trim().equals("")) {
            if (this.filters.containsKey(this.filter)) {
                YUIFilter yuif = (YUIFilter) this.filters.get(this.filter);
                url = url.replaceAll(yuif.getSearch(), yuif.getReplace());
            }
        }
        return url;
    }
    public String ComboDelimeter = "&";

    public void addToCombo(String name, MODULE_TYPE type) {
        Map m = (Map) this.modules.get(name);
        String pathToModule = this.comboDefaultVersion + "/build/" + m.get(YUI_PATH);


        if (type.equals(MODULE_TYPE.CSS)) {
            //If this is the first css component then add the combo base path
            if (this.cssComboLocation == null) {
                this.cssComboLocation = this.comboBase + pathToModule;
            } else {
                //Prep for next component
                this.cssComboLocation += ComboDelimeter + pathToModule;
            }

        } else {
            //If this is the first js component then add the combo base path
            if (this.jsComboLocation == null) {
                this.jsComboLocation = this.comboBase + pathToModule;
            } else {
                //Prep for next component
                this.jsComboLocation += ComboDelimeter + pathToModule;
            }
        }
    }

    public String getLink(String name, MODULE_TYPE type) {

        String url = this.getUrl(name);

        if (url == null) {
            return "<!-- PATH FOR " + name + " NOT SPECIFIED -->";
        } else if (type.equals(MODULE_TYPE.CSS)) {
            return "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + url + "\"  />";
        } else {
            return "<script type=\"text/javascript\" src=\"" + url + "\" ></script>";
        }
    }

    private String getRaw(String name) {

        String url = this.getUrl(name);
        return this.getRemoteContent(url);
        //$url = $this->getUrl($name);
    }

    private String getContent(String name, MODULE_TYPE type) {

        String url = this.getUrl(name);
        if (url == null) {
            return "<!-- PATH FOR " + name + " NOT SPECIFIED -->";
        } else if (type.equals(MODULE_TYPE.CSS)) {
            return "<style type=\"text/css\">" + this.getRemoteContent(url) + "</style>";
        } else {
            return "<script type=\"text/javascript\">" + this.getRemoteContent(url) + "</script>";
        }

    }
}
