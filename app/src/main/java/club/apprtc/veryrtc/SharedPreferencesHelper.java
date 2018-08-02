package club.apprtc.veryrtc;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

/**
 * 保存信息配置类
 * @author Vinton.Liu
 * @date 2018/7/25
 */
class SharedPreferencesHelper {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SharedPreferencesHelper(Context context, String FILE_NAME) {
        sharedPreferences = context.getSharedPreferences(FILE_NAME,
                                                        Context.MODE_PRIVATE);

        editor = sharedPreferences.edit();
    }

    /**
     * save key-value
     * @param key
     * @param object
     */
    public void put(String key, Object object) {
        if (object instanceof String) {
            editor.putString(key, (String) object);
        } else if (object instanceof Integer) {
            editor.putInt(key, (Integer) object);
        } else if (object instanceof Boolean) {
            editor.putBoolean(key, (Boolean) object);
        } else if (object instanceof Float) {
            editor.putFloat(key, (Float) object);
        } else if (object instanceof Long) {
            editor.putLong(key, (Long) object);
        } else {
            editor.putString(key, object.toString());
        }
        editor.commit();
    }

    /**
     * get key-value
     * @param key
     * @param defaultObject default value
     * @return value
     */
    public Object get(String key, Object defaultObject) {
        if (defaultObject instanceof String) {
            return sharedPreferences.getString(key, (String) defaultObject);
        } else if (defaultObject instanceof Integer) {
            return sharedPreferences.getInt(key, (Integer) defaultObject);
        } else if (defaultObject instanceof Boolean) {
            return sharedPreferences.getBoolean(key, (Boolean) defaultObject);
        } else if (defaultObject instanceof Float) {
            return sharedPreferences.getFloat(key, (Float) defaultObject);
        } else if (defaultObject instanceof Long) {
            return sharedPreferences.getLong(key, (Long) defaultObject);
        } else {
            return sharedPreferences.getString(key, null);
        }
    }

    public String getString(String key, String def) {
        return sharedPreferences.getString(key, def);
    }

    public boolean getBoolean(String key, Boolean def) {
        return sharedPreferences.getBoolean(key, def);
    }

    public int getInt(String key, int def) {
        return sharedPreferences.getInt(key, def);
    }

    public Float getFloat(String key, float def) {
        return sharedPreferences.getFloat(key, def);
    }

    public Long getLong(String key, long def) {
        return sharedPreferences.getLong(key, def);
    }

    /**
     * remove value for specific key
     * @param key
     */
    public void remove(String key) {
        editor.remove(key);
        editor.commit();
    }

    /**
     * clear all value
     */
    public void clear() {
        editor.clear();
        editor.commit();
    }

    /**
     * check if exist specific key
     * @param key
     * @return true for exist, else false
     */
    public Boolean contain(String key) {
        return sharedPreferences.contains(key);
    }

    /**
     * return all key-value pairs
     * @return
     */
    public Map<String, ?> getAll() {
        return sharedPreferences.getAll();
    }
}
