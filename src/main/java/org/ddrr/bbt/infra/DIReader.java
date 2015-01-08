package org.ddrr.bbt.infra;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import org.ddrr.bbt.BaseApplication;
import org.ddrr.bbt.persistent.DIParser;
import org.ddrr.bbt.persistent.DIPersister;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Persister;

import java.lang.ref.Reference;
import java.util.List;
import java.util.Map;

/**
 * Created by PhoebeHuyi on 2014.12.25.
 */
public class DIReader {
    private static Reference<List<DIProfile>> sProfileCache;

    public static List<DIProfile> getProfiles() {
        List<DIProfile> result = null;
        Context context = BaseApplication.getAppInstance();
        if (null != sProfileCache) {
            result = sProfileCache.get();
        }
        if (null == result) {
            try {
                result = new Persister().read(DIProfileInfo.class, context.getAssets().open("interface/meta-data.xml")).profiles;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @Root(name = "root")
    public static class DataExporter {
        @Element(name = "sql", required = false)
        private String sqlStr;
        @Element(name = "persister")
        private String persisterPath;
        @Element(name = "header", required = false)
        String header;
        @Element(name = "database-name")
        String dbName;
        private String filePath;


        private DataExporter() {
        }

        private static DataExporter from(Context context, DIProfile profile) {
            String diPath = profile.exporterPath;
            DataExporter dataExporter = null;
            try {
                dataExporter = new Persister().read(DataExporter.class, context.getAssets().open("interface/" + diPath + ".xml"));
                dataExporter.filePath = profile.filePath;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return dataExporter;
        }


        private boolean export(Context context) {
//            Log.e("fileTest", context.getDir("test.txt", Context.MODE_PRIVATE).getAbsolutePath() + " dbName = " + dbName);
            SQLiteDatabase db = context.openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null);
            Cursor c = db.rawQuery(constructSql(), null);
            List<Map<String, String>> result = new CursorReader().read(c);
            c.close();
            db.close();
            return write(result);
        }

        private boolean write(List<Map<String, String>> result) {
            try {
                DIPersister p = (DIPersister) Class.forName(persisterPath).newInstance();
                return p.write(filePath, header, result);
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return false;
        }


        private String constructSql() {
            return sqlStr;
        }

    }

    @Root(name = "root")
    private static class DataImporter {
        @ElementList(name = "col-names")
        List<String> colNames;
        @ElementList(name = "id-cols")
        List<Integer> idCols;
        @Element(name = "uri")
        String contentUri;
        @Element(name = "skip-line")
        int numSkipLine;
        @Element(name = "parser")
        String parserPath;
        String filePath;

        private DataImporter() {

        }

        private static DataImporter from(Context context, DIProfile profile) {
            DataImporter dataImporter = null;
            try {
                dataImporter = new Persister().read(DataImporter.class, context.getAssets().open("interface/" + profile.importerPath + ".xml"));
                dataImporter.filePath = profile.filePath;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return dataImporter;
        }

        private boolean execImport(Context context) {
            try {
                Uri uri = Uri.parse(contentUri);
                DIParser parser = (DIParser) Class.forName(parserPath).newInstance();
                parser.start(filePath, numSkipLine);
                String[] values;
                ContentResolver cr = context.getContentResolver();
                ContentValues cv = new ContentValues();
                String[] selectionArgs = new String[idCols.size()];
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < idCols.size(); i++) {
                    String colName = colNames.get(idCols.get(i) - 1);
                    if (i > 0) {
                        sb.append(" AND ");
                    }
                    sb.append(colName).append(" = ?");
                }
                String where = sb.toString();
                while (null != (values = parser.read())) {
                    cv.clear();
                    for (int i = 0; i < colNames.size(); i++) {
                        String colName = colNames.get(i);
                        cv.put(colName, values[i]);
                    }
                    for (int i = 0; i < selectionArgs.length; i++) {
                        selectionArgs[i] = values[idCols.get(i) - 1];
                    }
                    cr.update(uri, cv, where, selectionArgs);
                    Log.e("update","where = " + where);
                    for(String arg : selectionArgs) {
                        Log.e("update", "arg = " + arg);
                    }
                }
                return true;
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            return false;
        }
    }

    @Root(name = "root")
    public static class DIProfileInfo {
        @ElementList(entry = "profile", inline = true)
        List<DIProfile> profiles;
    }

    @Root(name = "profile")
    public static class DIProfile {
        @Attribute(name = "name")
        private String name;
        @Attribute(name = "exporter")
        String exporterPath;
        @Attribute(name = "importer")
        String importerPath;
        @Attribute(name = "file")
        String filePath;

        public String getName() {
            return name;
        }

        public boolean execExport() {
            Context context = BaseApplication.getAppInstance();
            return DataExporter.from(context, this).export(context);
        }

        public boolean execImport() {
            Context context = BaseApplication.getAppInstance();
            return DataImporter.from(context, this).execImport(context);
        }

        public interface DIProgressReceiver {

        }
    }
}
