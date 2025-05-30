package org.osmdroid.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import org.osmdroid.tileprovider.modules.SqlTileWriter;
import org.osmdroid.tileprovider.util.StorageUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants.HTTP_EXPIRES_HEADER_FORMAT;

/**
 * Default configuration provider for osmdroid
 * <a href="https://github.com/osmdroid/osmdroid/issues/481">Issue 481</a>
 * Created on 11/29/2016.
 * @author Alex O'Ree
 * @see IConfigurationProvider
 * @see Configuration
 */
public class DefaultConfigurationProvider implements IConfigurationProvider {

    /*public final String[] preferenceKeys = new String[]{
        "osmdroid.basePath",
        "osmdroid.cachePath",
        "osmdroid.DebugMode",
        "osmdroid.DebugDownloading",
        "osmdroid.DebugMapView",
        "osmdroid.DebugTileProvider",
        "osmdroid.HardwareAcceleration",
        "osmdroid.userAgentValue",
        "osmdroid.gpsWaitTime",
        "osmdroid.tileDownloadThreads",
        "osmdroid.tileFileSystemThreads",
        "osmdroid.tileDownloadMaxQueueSize",
        "osmdroid.tileFileSystemMaxQueueSize"
    };*/
    //<editor-fold>
    protected long gpsWaitTime =20000;
    protected boolean debugMode= false;
    protected boolean debugMapView = false;
    protected boolean debugTileProviders = false;
    protected boolean debugMapTileDownloader=false;
    protected boolean isMapViewHardwareAccelerated=false;
    protected String userAgentValue="osmdroid";
    protected String userAgentHttpHeader = "User-Agent";
    private final Map<String, String> mAdditionalHttpRequestProperties = new HashMap<>();
    protected short cacheMapTileCount = 9;
    protected short tileDownloadThreads = 2;
    protected short tileFileSystemThreads = 8;
    protected short tileDownloadMaxQueueSize = 40;
    protected short tileFileSystemMaxQueueSize = 40;
    protected long tileFileSystemCacheMaxBytes = 600L * 1024 * 1024;
    protected long tileFileSystemCacheTrimBytes = 500L * 1024 * 1024;
    protected SimpleDateFormat httpHeaderDateTimeFormat = new SimpleDateFormat(HTTP_EXPIRES_HEADER_FORMAT, Locale.US);
    protected File osmdroidBasePath;
    protected File osmdroidTileCache;
    protected long expirationAdder = 0;
    protected Long expirationOverride=null;

    public DefaultConfigurationProvider(){

        try {
            //StorageUtils.getStorage() can return null when android studio is "previewing" a layout
            osmdroidBasePath = new File(StorageUtils.getStorage().getAbsolutePath(), "osmdroid");
            osmdroidTileCache =  new File(getOsmdroidBasePath(), "tiles");
            osmdroidBasePath.mkdirs();
            osmdroidTileCache.mkdirs();
        }catch (Exception ex){
            //IO/permissions issue
            //trap for android studio layout editor and some for certain devices
            //see https://github.com/osmdroid/osmdroid/issues/508
        }
    }
    /**
     * default is 20 seconds
     * @return time in ms
     */
    @Override
    public long getGpsWaitTime() {
        return gpsWaitTime;
    }

    @Override
    public void setGpsWaitTime(long gpsWaitTime) {
        this.gpsWaitTime = gpsWaitTime;
    }

    @Override
    public boolean isDebugMode() {
        return debugMode;
    }

    @Override
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    @Override
    public boolean isDebugMapView() {
        return debugMapView;
    }

    @Override
    public void setDebugMapView(boolean debugMapView) {
        this.debugMapView = debugMapView;
    }

    @Override
    public boolean isDebugTileProviders() {
        return debugTileProviders;
    }

    @Override
    public void setDebugTileProviders(boolean debugTileProviders) {
        this.debugTileProviders = debugTileProviders;
    }

    @Override
    public boolean isDebugMapTileDownloader() {
        return debugMapTileDownloader;
    }

    @Override
    public void setDebugMapTileDownloader(boolean debugMapTileDownloader) {
        this.debugMapTileDownloader = debugMapTileDownloader;
    }

    @Override
    public boolean isMapViewHardwareAccelerated() {
        return isMapViewHardwareAccelerated;
    }

    @Override
    public void setMapViewHardwareAccelerated(boolean mapViewHardwareAccelerated) {
        isMapViewHardwareAccelerated = mapViewHardwareAccelerated;
    }

    @Override
    public String getUserAgentValue() {
        return userAgentValue;
    }

    @Override
    public void setUserAgentValue(String userAgentValue) {
        this.userAgentValue = userAgentValue;
    }

    @Override
    public Map<String, String> getAdditionalHttpRequestProperties() {
        return mAdditionalHttpRequestProperties;
    }

    @Override
    public short getCacheMapTileCount() {
        return cacheMapTileCount;
    }

    @Override
    public void setCacheMapTileCount(short cacheMapTileCount) {
        this.cacheMapTileCount = cacheMapTileCount;
    }

    @Override
    public short getTileDownloadThreads() {
        return tileDownloadThreads;
    }

    @Override
    public void setTileDownloadThreads(short tileDownloadThreads) {
        this.tileDownloadThreads = tileDownloadThreads;
    }

    @Override
    public short getTileFileSystemThreads() {
        return tileFileSystemThreads;
    }

    @Override
    public void setTileFileSystemThreads(short tileFileSystemThreads) {
        this.tileFileSystemThreads = tileFileSystemThreads;
    }

    @Override
    public short getTileDownloadMaxQueueSize() {
        return tileDownloadMaxQueueSize;
    }

    @Override
    public void setTileDownloadMaxQueueSize(short tileDownloadMaxQueueSize) {
        this.tileDownloadMaxQueueSize = tileDownloadMaxQueueSize;
    }

    @Override
    public short getTileFileSystemMaxQueueSize() {
        return tileFileSystemMaxQueueSize;
    }

    @Override
    public void setTileFileSystemMaxQueueSize(short tileFileSystemMaxQueueSize) {
        this.tileFileSystemMaxQueueSize = tileFileSystemMaxQueueSize;
    }

    @Override
    public long getTileFileSystemCacheMaxBytes() {
        return tileFileSystemCacheMaxBytes;
    }

    @Override
    public void setTileFileSystemCacheMaxBytes(long tileFileSystemCacheMaxBytes) {
        this.tileFileSystemCacheMaxBytes = tileFileSystemCacheMaxBytes;
    }

    @Override
    public long getTileFileSystemCacheTrimBytes() {
        return tileFileSystemCacheTrimBytes;
    }

    @Override
    public void setTileFileSystemCacheTrimBytes(long tileFileSystemCacheTrimBytes) {
        this.tileFileSystemCacheTrimBytes = tileFileSystemCacheTrimBytes;
    }

    @Override
    public SimpleDateFormat getHttpHeaderDateTimeFormat() {
        return httpHeaderDateTimeFormat;
    }

    @Override
    public void setHttpHeaderDateTimeFormat(SimpleDateFormat httpHeaderDateTimeFormat) {
        this.httpHeaderDateTimeFormat = httpHeaderDateTimeFormat;
    }

    @Override
    public File getOsmdroidBasePath() {
        return osmdroidBasePath;
    }

    @Override
    public void setOsmdroidBasePath(File osmdroidBasePath) {
        this.osmdroidBasePath = osmdroidBasePath;
    }

    @Override
    public File getOsmdroidTileCache() {
        return osmdroidTileCache;
    }

    @Override
    public void setOsmdroidTileCache(File osmdroidTileCache) {
        this.osmdroidTileCache = osmdroidTileCache;
    }


    @Override
    public String getUserAgentHttpHeader() {
        return userAgentHttpHeader;
    }

    @Override
    public void setUserAgentHttpHeader(String userAgentHttpHeader) {
        this.userAgentHttpHeader = userAgentHttpHeader;
    }

    //</editor-fold>
    @Override
    public void load(Context ctx, SharedPreferences prefs) {
        //cache management starts here

        //check to see if the shared preferences is set for the tile cache
        if (!prefs.contains("osmdroid.basePath")){
            //this is the first time startup. run the discovery bit
            File discoveredBestPath = getOsmdroidBasePath();
            File discoveredCachPath = getOsmdroidTileCache();
            if (!discoveredBestPath.exists() || !StorageUtils.isWritable(discoveredBestPath)) {
                //this should always be writable...
                discoveredCachPath=discoveredBestPath=new File("/data/data/" + ctx.getPackageName() + "/osmdroid/");
                discoveredCachPath.mkdirs();
            }

            SharedPreferences.Editor edit = prefs.edit();
            edit.putString("osmdroid.basePath",discoveredBestPath.getAbsolutePath());
            edit.putString("osmdroid.cachePath",discoveredCachPath.getAbsolutePath());
            edit.commit();
            setOsmdroidBasePath(discoveredBestPath);
            setOsmdroidTileCache(discoveredCachPath);
            setUserAgentValue(ctx.getPackageName());
            save(ctx,prefs);
        } else {
            //normal startup, load user preferences and populate the config object
            setOsmdroidBasePath(new File(prefs.getString("osmdroid.basePath", getOsmdroidBasePath().getAbsolutePath())));
            setOsmdroidTileCache(new File(prefs.getString("osmdroid.cachePath", getOsmdroidTileCache().getAbsolutePath())));
            setDebugMode(prefs.getBoolean("osmdroid.DebugMode",false));
            setDebugMapTileDownloader(prefs.getBoolean("osmdroid.DebugDownloading", false));
            setDebugMapView(prefs.getBoolean("osmdroid.DebugMapView", false));
            setDebugTileProviders(prefs.getBoolean("osmdroid.DebugTileProvider",false));
            setMapViewHardwareAccelerated(prefs.getBoolean("osmdroid.HardwareAcceleration",false));
            setUserAgentValue(prefs.getString("osmdroid.userAgentValue",ctx.getPackageName()));
            load(prefs, mAdditionalHttpRequestProperties, "osmdroid.additionalHttpRequestProperty.");
            setGpsWaitTime(prefs.getLong("osmdroid.gpsWaitTime", gpsWaitTime));
            setTileDownloadThreads((short)(prefs.getInt("osmdroid.tileDownloadThreads",tileDownloadThreads)));
            setTileFileSystemThreads((short)(prefs.getInt("osmdroid.tileFileSystemThreads",tileFileSystemThreads)));
            setTileDownloadMaxQueueSize((short)(prefs.getInt("osmdroid.tileDownloadMaxQueueSize",tileDownloadMaxQueueSize)));
            setTileFileSystemMaxQueueSize((short)(prefs.getInt("osmdroid.tileFileSystemMaxQueueSize",tileFileSystemMaxQueueSize)));
            setExpirationExtendedDuration((long)prefs.getLong("osmdroid.ExpirationExtendedDuration", expirationAdder));
            if (prefs.contains("osmdroid.ExpirationOverride")) {
                expirationOverride = prefs.getLong("osmdroid.ExpirationOverride",-1);
                if (expirationOverride!=null && expirationOverride==-1)
                    expirationOverride=null;
            }
        }


        long cacheSize=-1;
        if (Build.VERSION.SDK_INT >= 9) {
            //unfortunately API 8 doesn't support File.length()

            //https://github/osmdroid/osmdroid/issues/435
            //On startup, we auto set the max cache size to be the current cache size + free disk space
            //this reduces the chance of osmdroid completely filling up the storage device

            //if the default max cache size is greater than the available free space
            //reduce it to 95% of the available free space + the size of the cache
            File dbFile = new File(getOsmdroidTileCache().getAbsolutePath() + File.separator + SqlTileWriter.DATABASE_FILENAME);
            if (dbFile.exists()) {
                cacheSize = dbFile.length();
                long freeSpace = getOsmdroidTileCache().getFreeSpace();

                //Log.i(TAG, "Current cache size is " + cacheSize + " free space is " + freeSpace);
                if (getTileFileSystemCacheMaxBytes() > (freeSpace + cacheSize)){
                    setTileFileSystemCacheMaxBytes((long)((freeSpace + cacheSize) * 0.95));
                    setTileFileSystemCacheTrimBytes((long)((freeSpace + cacheSize) * 0.90));
                }
            } else {
                //this is probably the first time running osmdroid
                long freeSpace = getOsmdroidTileCache().length();
                if (getTileFileSystemCacheMaxBytes() > (freeSpace)){
                    setTileFileSystemCacheMaxBytes((long)((freeSpace) * 0.95));
                    setTileFileSystemCacheMaxBytes((long)((freeSpace) * 0.90));
                }
            }
        }
    }

    @Override
    public void save(Context ctx, SharedPreferences prefs) {
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("osmdroid.basePath",getOsmdroidBasePath().getAbsolutePath());
        edit.putString("osmdroid.cachePath",getOsmdroidTileCache().getAbsolutePath());
        edit.putBoolean("osmdroid.DebugMode",isDebugMode());
        edit.putBoolean("osmdroid.DebugDownloading",isDebugMapTileDownloader());
        edit.putBoolean("osmdroid.DebugMapView",isDebugMapView());
        edit.putBoolean("osmdroid.DebugTileProvider",isDebugTileProviders());
        edit.putBoolean("osmdroid.HardwareAcceleration", isMapViewHardwareAccelerated());
        edit.putString("osmdroid.userAgentValue", getUserAgentValue());
        save(prefs, edit, mAdditionalHttpRequestProperties, "osmdroid.additionalHttpRequestProperty.");
        edit.putLong("osmdroid.gpsWaitTime",gpsWaitTime);
        edit.putInt("osmdroid.cacheMapTileCount", cacheMapTileCount);
        edit.putInt("osmdroid.tileDownloadThreads", tileDownloadThreads);
        edit.putInt("osmdroid.tileFileSystemThreads",tileFileSystemThreads);
        edit.putInt("osmdroid.tileDownloadMaxQueueSize",tileDownloadMaxQueueSize);
        edit.putInt("osmdroid.tileFileSystemMaxQueueSize",tileFileSystemMaxQueueSize);
        edit.putLong("osmdroid.ExpirationExtendedDuration",expirationAdder);
        if (expirationOverride!=null)
            edit.putLong("osmdroid.ExpirationOverride",expirationOverride);
        //TODO save other fields?

        edit.commit();
    }

    /**
     * Loading a map from preferences, using a prefix for the prefs keys
     *
     * @since 5.6.5
     * @param pPrefs
     * @param pMap
     * @param pPrefix
     */
    private static void load(final SharedPreferences pPrefs,
                             final Map<String, String> pMap, final String pPrefix) {
        pMap.clear();
        for (final String key : pPrefs.getAll().keySet()) {
            if (key.startsWith(pPrefix)) {
                pMap.put(key.substring(pPrefix.length()), pPrefs.getString(key, null));
            }
        }
    }

    /**
     * Saving a map into preferences, using a prefix for the prefs keys
     *
     * @since 5.6.5
     * @param pPrefs
     * @param pEdit
     * @param pMap
     * @param pPrefix
     */
    private static void save(final SharedPreferences pPrefs, final SharedPreferences.Editor pEdit,
                             final Map<String, String> pMap, final String pPrefix) {
        for (final String key : pPrefs.getAll().keySet()) {
            if (key.startsWith(pPrefix)) {
                pEdit.remove(key);
            }
        }
        for (final Map.Entry<String, String> entry : pMap.entrySet()) {
            final String key = pPrefix + entry.getKey();
            pEdit.putString(key, entry.getValue());
        }
    }

    @Override
    public long getExpirationExtendedDuration() {
        return expirationAdder ;
    }

    @Override
    public void setExpirationExtendedDuration(final long period) {
        if (period < 0)
            expirationAdder=0;
        else
            expirationAdder=period;
    }

    @Override
    public void setExpirationOverrideDuration(Long period) {
        expirationOverride=period;
    }

    @Override
    public Long getExpirationOverrideDuration() {
        return expirationOverride;
    }
}
