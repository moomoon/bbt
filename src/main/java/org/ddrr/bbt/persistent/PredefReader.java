package org.ddrr.bbt.persistent;

import android.content.res.AssetManager;
import android.support.v4.util.Pair;
import android.util.Log;

import org.ddrr.bbt.BaseApplication;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;
import org.simpleframework.xml.core.Persister;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by PhoebeHuyi on 2014.12.16.
 */
final public class PredefReader {
    public static List<Pair<String,Predef>> read() {
        final AssetManager am = BaseApplication.getAppInstance().getAssets();
        try {
            PredefMetaData pmd = new Persister().read(PredefMetaData.class, am.open("predef/meta-data.xml"));
            List<Pair<String,Predef>> result = new ArrayList<>();
            for(PredefMetaData.Table table : pmd.tableList){
                String tableName = table.tableName;
                String fileName = table.fileName;
                Predef p = new Persister().read(Predef.class, am.open("predef/" + fileName + ".xml"));
                result.add(new Pair<>(tableName, p));
            }
            for(Pair<String, Predef> p : result){
                Log.e("read","tableName = " + p.first);
                Log.e("read",p.second.toString());
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Root(name = "root")
    private static class PredefMetaData {
        @ElementList(inline = true)
        List<Table> tableList;
        @Root(name="table")
        private static class Table{
            @Attribute(name="name")
            String tableName;
            @Text
            String fileName;
        }
    }

    @Root(name = "root")
    public static class Predef {
        private Predef() {

        }

        @ElementList(name = "meta")
        public List<String> colNames;
        @ElementList(name="entry", inline = true)
        public List<Entry> entryList;

        @Root(name="entry")
        public static class Entry {
            @ElementList(entry="col", inline = true)
            List<String> colValues;

            @Override
            public String toString() {
                StringBuffer sb = new StringBuffer("entry[");
                for(int i = 0; i < colValues.size(); i ++){
                    if(i > 0){
                        sb.append('|');
                    }
                    sb.append(colValues.get(i));
                }
                sb.append(']');
                return sb.toString();
            }
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("col[");
            for(int i = 0; i < colNames.size(); i ++){
                if(i > 0){
                    sb.append('|');
                }
                sb.append(colNames.get(i));
            }
            sb.append("]");
            for(Entry entry : entryList){
                sb.append('\n');
                sb.append(entry.toString());
            }
            return sb.toString();
        }
    }

}
