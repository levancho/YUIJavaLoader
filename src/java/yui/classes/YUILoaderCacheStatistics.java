/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package yui.classes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.Statistics;
import net.sf.ehcache.management.CacheStatistics;

/**
 *
 * @author leo
 */
public class YUILoaderCacheStatistics {

    private CacheManager cacheManager;
    private StringBuffer stats;

    public YUILoaderCacheStatistics() {
        cacheManager = CacheManager.create();
        //init();
    }

    private void init() {
        String[] names = cacheManager.getCacheNames();
        stats = new StringBuffer();
        for (String name : names) {
            Cache c = cacheManager.getCache(name);
            try {
                testStatistics(c, stats);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    public String getStats () {
         init();
        return stats.toString();
    }

    private String formatDate (long mil){
        if(mil==0)return "not updated";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
        Date resultdate = new Date(mil);
        return(sdf.format(resultdate));
    }

    public void testStatistics(Cache cache, StringBuffer sb) throws InterruptedException {
        //Set size so the second element overflows to disk.
        Statistics stats = cache.getStatistics();
        sb.append("---------Stats For Cache: <span style=\"color:green;font-weight:bold\">[" + cache.getName() + "]</span> -------------------------------------");
        sb.append("<br>");
        sb.append("ObjectCount: " + stats.getObjectCount());
         sb.append("<br>");
        sb.append(" Hit Count: " + stats.getCacheHits());
         sb.append("<br>");
        sb.append("Miss Count: " + stats.getCacheMisses());
         sb.append("<br>");
        sb.append(" Eviction Count: " + stats.getEvictionCount());
         sb.append("<br>");
        sb.append(" In Memory Hits: " + stats.getInMemoryHits());
         sb.append("<br>");
       
        List<String> k = cache.getKeys();
        for (String e : k) {
             sb.append("<hr>");
            Element el = cache.get(e);
            sb.append("Stats For Element: <span style=\"color:green;font-weight:bold\"> [" + e + "]</span> ");
                   sb.append("<br>");
            sb.append("- LastAccessTime: " + formatDate(el.getLastAccessTime()));
                   sb.append("<br>");
            sb.append("- LastUpdateTime:  " +formatDate( el.getLastUpdateTime()));
                   sb.append("<br>");
            sb.append("- Hit Count: " + el.getHitCount());
                   sb.append("<br>");
            sb.append("- ExpirationTime: " + formatDate(el.getExpirationTime()));
                   sb.append("<br>");
           sb.append("<hr>");
        }
     sb.append("<br>");
      sb.append("<br>");
      sb.append("===============================================================");
//               assertEquals(1, cache.getDiskStoreHitCount());
//              assertEquals(0, cache.getMemoryStoreHitCount());
//            assertEquals(0, cache.getMissCountExpired());
//           assertEquals(0, cache.getMissCountNotFound());
//
//            //key 1 should now be in the LruMemoryStore
//                cache.get("key1");
//        assertEquals(2, cache.getHitCount());
//             assertEquals(1, cache.getDiskStoreHitCount());
//          assertEquals(1, cache.getMemoryStoreHitCount());
//             assertEquals(0, cache.getMissCountExpired());
//             assertEquals(0, cache.getMissCountNotFound());
//
//                //Let the idle expire
//          Thread.sleep(5020);
//
//             //key 1 should now be expired
//             cache.get("key1");
//              assertEquals(2, cache.getHitCount());
//             assertEquals(1, cache.getDiskStoreHitCount());
//              assertEquals(1, cache.getMemoryStoreHitCount());
//             assertEquals(1, cache.getMissCountExpired());
//             assertEquals(1, cache.getMissCountNotFound());
    }
}
