package com.NewCenturyHotels.NewCentury.util;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyUtil {
    /**
     * app.properties配置文件中的所有配置属性
     *
     * @return Properties对象
     */
    public static Properties getConfigProperties(Context context) throws IOException {
        Properties props = new Properties();
        InputStream in = context.getAssets().open("app.properties");
        try {
            props.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return props;
    }

}