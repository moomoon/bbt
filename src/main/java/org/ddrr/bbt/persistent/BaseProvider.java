package org.ddrr.bbt.persistent;

import android.content.ContentProvider;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.support.v4.util.Pair;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class BaseProvider extends ContentProvider {
    public final static String DB_NAME = "mainentries.db";
    public final static int DB_VERSION_MASK = 0XFFFF;
    public final static int DICT_VERSION = 1;
    public final static int DB_VERSION = 1;
    public final static int VERSION = DICT_VERSION << 16 + DB_VERSION;

    public final static String ID = "_id";
    public final static String TABLE_BBT_ENTRY = "bbt_entry";
    public final static String TABLE_BBT_DICT_ENTRY_TIME_POINT = "bbt_dict_entry_time_point";
    public final static String TABLE_BBT_DICT_UNIT = "bbt_dict_unit";
    public final static String VIEW_BBT_ENTRY = "v_bbt_entry";
    public final static String ID_BBT_ENTRY = ID;
    public final static String KEY_BBT_RECORD_DATE_TIME = "record_date_time";
    public final static String KEY_BBT_ENTRY_DATE = "entry_date";
    public final static String KEY_BBT_ENTRY_TIME = "entry_time";
    public final static String KEY_BBT_VALUE = "value";
    public final static String KEY_BBT_NOTE = "note";
    public final static String ID_BBT_DICT_ENTRY_TIME_POINT = ID;
    public final static String KEY_BBT_TIME_POINT = "time_point";
    public final static String KEY_BBT_TIME_POINT_LABEL = "time_point_label";
    public final static String ID_BBT_UNIT = ID;
    public final static String KEY_BBT_UNIT_CODE = "unit_code";
    public final static String KEY_BBT_UNIT_TITLE = "unit_title";

    protected DbHelper mDb;

    @Override
    public boolean onCreate() {
        mDb = new DbHelper(getContext());

        return (mDb != null);
    }

    protected static class DbHelper extends SQLiteOpenHelper {
        private final String CREATE_TABLE_BBT_ENTRY = "CREATE TABLE IF NOT EXISTS "
                + TABLE_BBT_ENTRY
                + " ( "
                + ID_BBT_ENTRY
                + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_BBT_RECORD_DATE_TIME
                + " TEXT NOT NULL, "
                + KEY_BBT_ENTRY_DATE
                + " TEXT NOT NULL, "
                + KEY_BBT_ENTRY_TIME
                + " TEXT NOT NULL, "
                + KEY_BBT_TIME_POINT
                + " INTEGER NOT NULL, "
                + KEY_BBT_VALUE
                + " FLOAT NOT NULL, "
                + KEY_BBT_UNIT_CODE
                + " INTEGER NOT NULL, " + KEY_BBT_NOTE + " TEXT);";
        private final String CREATE_TABLE_BBT_TIME_POINT = "CREATE TABLE IF NOT EXISTS "
                + TABLE_BBT_DICT_ENTRY_TIME_POINT
                + " ( "
                + ID_BBT_DICT_ENTRY_TIME_POINT
                + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_BBT_TIME_POINT
                + " INTEGER NOT NULL, "
                + KEY_BBT_TIME_POINT_LABEL
                + " TEXT NOT NULL);";
        private final String CREATE_TABLE_BBT_UNIT = "CREATE TABLE IF NOT EXISTS "
                + TABLE_BBT_DICT_UNIT
                + " ( "
                + ID_BBT_UNIT
                + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_BBT_UNIT_CODE
                + " INTEGER NOT NULL, "
                + KEY_BBT_UNIT_TITLE
                + " TEXT NOT NULL);";
        private final String CREATE_VIEW_BBT_ENTRY = "CREATE VIEW IF NOT EXISTS "
                + VIEW_BBT_ENTRY
                + " AS SELECT "
                + "T1." + ID + ", "
                + "T1." + KEY_BBT_RECORD_DATE_TIME + ", "
                + "T1." + KEY_BBT_ENTRY_DATE + ", "
                + "T1." + KEY_BBT_ENTRY_TIME + ", "
                + "(SELECT T2." + KEY_BBT_TIME_POINT_LABEL
                + " FROM " + TABLE_BBT_DICT_ENTRY_TIME_POINT + " T2"
                + " WHERE T2." + KEY_BBT_TIME_POINT + " = " + " T1." + KEY_BBT_TIME_POINT + ") AS " + KEY_BBT_TIME_POINT_LABEL + ", "
                + "T1." + KEY_BBT_TIME_POINT + ", "
                + "T1." + KEY_BBT_VALUE + ", "
                + "(SELECT T2." + KEY_BBT_UNIT_TITLE
                + " FROM " + TABLE_BBT_DICT_UNIT + " T2"
                + " WHERE T2." + KEY_BBT_UNIT_CODE + " = " + " T1." + KEY_BBT_UNIT_CODE + ") AS " + KEY_BBT_UNIT_TITLE + ", "
                + "T1." + KEY_BBT_UNIT_CODE + ", "
                + "T1." + KEY_BBT_NOTE + " "
                + "FROM " + TABLE_BBT_ENTRY + " T1;";

        public DbHelper(Context context) {
            super(context, DB_NAME, null, VERSION);
        }

        public void forceCreate() {
            SQLiteDatabase db = getWritableDatabase();
            onCreate(db);
        }

        private static List<String> getTableAndViewNames(SQLiteDatabase db) {

            Log.e("db", "onCreate");
            try {
                List<String> tableNames = new ArrayList<>();
                StringBuilder sb = new StringBuilder();
                sb.append("SELECT NAME FROM SQLITE_MASTER ");
                sb.append("WHERE TYPE IN ('TABLE','VIEW') ");
                sb.append("UNION ALL ");
                sb.append("SELECT name FROM SQLITE_TEMP_MASTER ");
                sb.append("WHERE TYPE IN ('TABLE','VIEW') ");

                Cursor c = db.rawQuery(sb.toString(), null);
                c.moveToFirst();
                int colNum = c.getColumnIndex("name");
                while (c.moveToNext()) {
                    tableNames.add(c.getString(colNum));
                }
                c.close();

                return tableNames;
            } catch (SQLiteException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            List<String> tableNames = getTableAndViewNames(db);
            db.execSQL(CREATE_TABLE_BBT_ENTRY);
            db.execSQL(CREATE_TABLE_BBT_TIME_POINT);
            db.execSQL(CREATE_TABLE_BBT_UNIT);
            db.execSQL(CREATE_VIEW_BBT_ENTRY);
            final String[] dictNames = {TABLE_BBT_DICT_ENTRY_TIME_POINT, TABLE_BBT_DICT_UNIT};
            SQLiteStatement statement;
            if (!tableNames.containsAll(Arrays.asList(dictNames))) {
                List<Pair<String, PredefReader.Predef>> predefs = PredefReader.read();
                StringBuilder colSb = new StringBuilder();
                for (Pair<String, PredefReader.Predef> pair : predefs) {
                    String tableName = pair.first;
                    PredefReader.Predef p = pair.second;
                    colSb.delete(0, colSb.length());

                    colSb.append("INSERT INTO ").append(tableName).append(" (");
                    int i = 0;
                    final int size = p.colNames.size();
                    for (; i < size - 1; i++) {
                        colSb.append(p.colNames.get(i)).append(",");
                    }
                    colSb.append(p.colNames.get(i)).append(") VALUES (");
                    for (i = 0; i < size - 1; i++) {
                        colSb.append("?,");
                    }
                    colSb.append("?)");
                    for (PredefReader.Predef.Entry e : p.entryList) {
                        statement = db.compileStatement(colSb.toString());
                        for (i = 0; i < size; i++)
                            statement.bindString(i + 1, e.colValues.get(i));
                        statement.executeInsert();

                    }
                }


            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            int oldDictVersion = oldVersion >>> 16;
            int oldDBVersion = oldVersion & DB_VERSION_MASK;
            int newDictVersion = newVersion >>> 16;
            int newDBVersion = newVersion & DB_VERSION_MASK;
            boolean dbUpgraded = newDBVersion > oldDBVersion;
            boolean dictUpgraded = newDictVersion > oldDictVersion;
            if (dbUpgraded) {
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_BBT_ENTRY);
                db.execSQL("DROP VIEW IF EXISTS " + VIEW_BBT_ENTRY);
            }
            if (dictUpgraded) {
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_BBT_DICT_UNIT);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_BBT_DICT_ENTRY_TIME_POINT);
            }
            if (dbUpgraded || dictUpgraded) {
                onCreate(db);
            }
        }
    }
}
