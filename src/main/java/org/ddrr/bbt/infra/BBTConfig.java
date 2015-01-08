package org.ddrr.bbt.infra;

import org.ddrr.bbt.BaseApplication;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;
import org.simpleframework.xml.core.Persister;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by PhoebeHuyi on 2014.12.19.
 */
public class BBTConfig {
    private static Reference<Config> configReference;

    private static Config getConfig() {
        try {
            Config c = null;
            if (null != configReference) {
                c = configReference.get();
            }
            if (null == c) {
                c = new Persister().read(Config.class, BaseApplication.getAppInstance().getAssets().open("config/base_config.xml"));
                configReference = new SoftReference<Config>(c);
            }
            return c;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static String getDefaultValue(String name) {
        return getConfig().defaultValueInfo.valueMap.get(name);
    }


    @Root(name = "root")
    private static class Config {
        @Element(name = "default-values")
        private DefaultValueInfo defaultValueInfo;

        @Root(name = "default-values")
        private static class DefaultValueInfo {
            private Map<String, String> valueMap = new HashMap<>();
            @ElementList(entry = "item", inline = true)
            private List<DefaultValue> valueList;

            private DefaultValueInfo(@ElementList(entry = "item", inline = true) List<DefaultValue> values) {
                for (DefaultValue dv : values) {
                    valueMap.put(dv.name, dv.value);
                }
            }

            @Root(name = "item")
            private static class DefaultValue {
                @Attribute
                private String name;
                @Text
                private String value;
            }
        }
    }
}
