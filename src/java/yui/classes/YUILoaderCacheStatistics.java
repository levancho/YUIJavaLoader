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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.Statistics;

/**
 * Utility class that gathers information about cache, its members, and hit/mis ration etc ..
 * @author leo
 */
public class YUILoaderCacheStatistics {

     private static final long  MEGABYTE = 1024L * 1024L;


      private static final long  KILOBYTE = 1024L ;


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
        sb.append("<br>");
        sb.append("<span style=\"font-size:20px\">Stats For Cache: <span style=\"color:green;font-weight:bold;font-size:20px\">[" + cache.getName() + "]</span> -------------------------------------</span>");
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
           

            if(el!=null){
                sb.append("Stats For Element: <span style=\"color:green;font-weight:bold\"> [" + e + "]</span> ");
                       sb.append("<br>");
                sb.append("- LastAccessTime: " + formatDate(el.getLastAccessTime()));
                       sb.append("<br>");
                sb.append("- LastUpdateTime:  " +formatDate( el.getLastUpdateTime()));
                       sb.append("<br>");
                sb.append("- Hit Count: " + el.getHitCount());
                       sb.append("<br>");
                     sb.append("- Serialized Size: " + formatBytes(el.getSerializedSize()));
                       sb.append("<br>");    

                sb.append("- ExpirationTime: " + formatDate(el.getExpirationTime()));
                       sb.append("<br>");
               sb.append("<hr>");
            } else {
                sb.append("<br>");
                sb.append("Inconsistent/Missing Cache Element for: "+e);
                sb.append("<br>");
            }
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

    private String formatBytes (long b){
        if(b>KILOBYTE)return b/KILOBYTE+" KB";

        return b+" Bytes";
    }
}
