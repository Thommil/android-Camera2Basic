package com.thommil.animalsgo.gl;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.thommil.animalsgo.R;

import java.util.HashMap;
import java.util.Map;

public class PluginManager {

    private static final String TAG = "A_GO/PluginManager";

    private static PluginManager sPluginManagerInstance;

    private final Map<String, RendererPlugin> mPluginsMap;

    private final Context mContext;

    private PluginManager(final Context context){
        mPluginsMap = new HashMap<>();
        mContext = context;
        loadPlugins();
    }

    public static final PluginManager getInstance(final Context context){
        if(sPluginManagerInstance == null){
            sPluginManagerInstance = new PluginManager(context);
        }
        return sPluginManagerInstance;
    }

    public RendererPlugin getPlugin(final String pluginId){
        return mPluginsMap.get(pluginId);
    }

    private void loadPlugins(){
        Log.d(TAG, "initialize()");
        try {
            for (final String pluginClassname : mContext.getResources().getStringArray(R.array.plugins_list)) {
                final Class pluginClass = this.getClass().getClassLoader().loadClass(pluginClassname);
                final RendererPlugin plugin = (RendererPlugin) pluginClass.newInstance();
                plugin.setContext(mContext);
                Log.d(TAG, "Plugin "+ plugin.getId()+" created");
                mPluginsMap.put(plugin.getId(), plugin);
            }
        }catch(ClassNotFoundException cne){
            throw new RuntimeException("Missing plugin class : "+cne);
        }catch(InstantiationException ie){
            throw new RuntimeException("Failed to instanciate plugin : "+ie);
        }catch(IllegalAccessException iae){
            throw new RuntimeException("Failed to instanciate plugin : "+iae);
        }
    }

    public void initialize(){
        Log.d(TAG, "destroy()");
        for(final RendererPlugin plugin : mPluginsMap.values()){
            plugin.create();
        }
    }


    public void destroy(){
        Log.d(TAG, "destroy()");
        for(final RendererPlugin plugin : mPluginsMap.values()){
            plugin.delete();
        }
    }

}
