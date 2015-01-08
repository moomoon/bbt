package org.ddrr.bbt.persistent;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class EntryBaseProvider extends BaseProvider {
	public final static String AUTHORITY = "ddrr.bbt.entrybase";
	public final static int ENTRY_BASE = 100;
	public final static int ENTRY_BASE_ID = 110;

	private static final String ENTRY_BASE_PATH = "entrybase";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + ENTRY_BASE_PATH);

	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/entrybase";
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/entrybase";

	private static final UriMatcher sURIMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, ENTRY_BASE_PATH, ENTRY_BASE);
		sURIMatcher.addURI(AUTHORITY, ENTRY_BASE_PATH + "/#", ENTRY_BASE_ID);
	}

	private static final String TAG = "EntryBaseProvider";

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = mDb.getWritableDatabase();
		int rowsAffected = 0;
		switch (uriType) {
		case ENTRY_BASE:
			rowsAffected = sqlDB.delete(TABLE_BBT_ENTRY, selection,
					selectionArgs);
			break;
		case ENTRY_BASE_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsAffected = sqlDB.delete(TABLE_BBT_ENTRY, ID_BBT_ENTRY
						+ " = " + id, null);
			} else {
				rowsAffected = sqlDB.delete(TABLE_BBT_ENTRY, ID_BBT_ENTRY
						+ " = " + id + " and " + selection, selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown or Invalid URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsAffected;
	}

	@Override
	public String getType(Uri uri) {
		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case ENTRY_BASE:
			return CONTENT_TYPE;
		case ENTRY_BASE_ID:
			return CONTENT_ITEM_TYPE;
		default:
			return null;
		}
	}

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        return super.bulkInsert(uri, values);
    }

    @Override
	public Uri insert(Uri uri, ContentValues values) {
        Log.e("test","provider insert");
		int uriType = sURIMatcher.match(uri);
		if (uriType != ENTRY_BASE) {
			throw new IllegalArgumentException("Invalid URI for insert");
		}
		SQLiteDatabase sqlDB = mDb.getWritableDatabase();
		try {
			long newID = sqlDB.insertOrThrow(TABLE_BBT_ENTRY, null, values);
			if (newID > 0) {
				Uri newUri = ContentUris.withAppendedId(uri, newID);
				getContext().getContentResolver().notifyChange(uri, null);
				return newUri;
			} else {
				throw new SQLException("Failed to insert row into " + uri + " contentValues = "  + values);
			}
		} catch (SQLiteConstraintException e) {
			Log.i(TAG, "Ignoring constraint failure.");
		}
		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(VIEW_BBT_ENTRY);

		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case ENTRY_BASE_ID:
			queryBuilder.appendWhere(ID_BBT_ENTRY + "="
					+ uri.getLastPathSegment());
			break;
		case ENTRY_BASE:
			// no filter
			break;
		default:
			throw new IllegalArgumentException("Unknown URI");
		}

		Cursor cursor = queryBuilder.query(mDb.getReadableDatabase(),
				projection, selection, selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
        Log.e("test", "provider update");
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = mDb.getWritableDatabase();

        int rowsAffected;

        switch (uriType) {
            case ENTRY_BASE_ID:
                String id = uri.getLastPathSegment();
                StringBuilder modSelection = new StringBuilder(ID_BBT_ENTRY + " = "
                        + id);

                if (!TextUtils.isEmpty(selection)) {
                    modSelection.append(" AND " + selection);
                }

                rowsAffected = sqlDB.update(TABLE_BBT_ENTRY, values,
                        modSelection.toString(), selectionArgs);
                break;
            case ENTRY_BASE:
                rowsAffected = sqlDB.update(TABLE_BBT_ENTRY, values, selection,
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI");
        }
        if (rowsAffected == 0) {
            insert(CONTENT_URI, values);
            rowsAffected = -1;
        } else {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsAffected;

    }
}
