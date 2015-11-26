package com.example.home.myapplication;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by HOME on 2015-08-25.
 */
public class DataProvider extends ContentProvider {
    static final String TABLE_FRIENDS = "friends";
    static final String COL_EMAIL = "email";
    static final String COL_NAME = "name";
    static final String COL_PHONE = "phone";

    static final String COL_ID = "id";

    static final String TABLE_MESSAGES = "messages";
    static final String COL_MSG = "msg";
    static final String COL_GIVE = "give";
    static final String COL_TAKE = "take";
    static final String COL_AT = "at";
    static final String COL_NUMBER="number";


    static final String PROVIDER_NAME ="example.com.provider.Chat";

    static final String URL_FRIENDS = "content://"+PROVIDER_NAME+"/friends";
    static final String URL_MESSAGES = "content://"+PROVIDER_NAME+"/messages";

    static final Uri CONTENT_URI_FRIENDS = Uri.parse(URL_FRIENDS);
    static final Uri CONTENT_URI_MESSAGES = Uri.parse(URL_MESSAGES);

    static final int FRIENDS_ALLROW = 1;
    static final int FRIENDS_SINGLEROW = 2;
    static final int MESSAGES_ALLROW = 3;
    static final int MESSAGES_SINGLEROW = 4;

    static final UriMatcher uriMatcher;
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME,"friends",FRIENDS_ALLROW);
        uriMatcher.addURI(PROVIDER_NAME,"friends/#",FRIENDS_SINGLEROW);
        uriMatcher.addURI(PROVIDER_NAME,"messages/",MESSAGES_ALLROW);
        uriMatcher.addURI(PROVIDER_NAME,"messages/#",MESSAGES_SINGLEROW);
    }
    private DBHelper dbHelper;
    private SQLiteDatabase database;
    @Override
    public boolean onCreate() {
        Context context = getContext();
        dbHelper = new DBHelper(context);
        // permissions to be writable
        database = dbHelper.getWritableDatabase();
        if(database == null) return false;
        else return true;
    }
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        database = dbHelper.getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        //uri
        switch(uriMatcher.match(uri)) {
            case FRIENDS_ALLROW:
                qb.setTables(TABLE_FRIENDS);
                break;

            case FRIENDS_SINGLEROW:
                qb.setTables(TABLE_FRIENDS);
                qb.appendWhere("id = " + uri.getLastPathSegment());
                break;

            case MESSAGES_ALLROW:
                qb.setTables(TABLE_MESSAGES);
                break;

            case MESSAGES_SINGLEROW:
                qb.setTables(TABLE_MESSAGES);
                qb.appendWhere("id = " + uri.getLastPathSegment());
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        //projection : return 하는 col값
        //selection : where 절
        //selectionArgs : selection에서 ?를 썻을 때 사용하는 값
        Cursor c = qb.query(database, projection, selection, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        database = dbHelper.getWritableDatabase();

        long idd;
        //content://com.appsrox.instachat.provider/messages
        switch(uriMatcher.match(uri)) {
            case MESSAGES_ALLROW:
                idd = database.insertOrThrow(TABLE_MESSAGES, null, values);
                break;

            case FRIENDS_ALLROW:
                idd = database.insertOrThrow(TABLE_FRIENDS, null, values);
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        Uri insertUri = ContentUris.withAppendedId(uri, idd);//uri �� id�� ��ģ uri��ü�� ��ȯ�ϴ� �޼ҵ�
        getContext().getContentResolver().notifyChange(insertUri, null);//ContentProvider�鿡�� uri�� �ٲ���ٰ� �˸�
        return insertUri;
    }
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        database = dbHelper.getWritableDatabase();

        int count;
        switch(uriMatcher.match(uri)) {
            case MESSAGES_ALLROW:
                count = database.update(TABLE_MESSAGES, values, selection, selectionArgs);
                break;

            case MESSAGES_SINGLEROW:
                count = database.update(TABLE_MESSAGES, values, "_id = ?", new String[]{uri.getLastPathSegment()});
                break;

            case FRIENDS_ALLROW:
                count = database.update(TABLE_FRIENDS, values, selection, selectionArgs);
                break;

            case FRIENDS_SINGLEROW:
                count = database.update(TABLE_FRIENDS, values, "_id = ?", new String[]{uri.getLastPathSegment()});
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        database = dbHelper.getWritableDatabase();

        int count;
        switch(uriMatcher.match(uri)) {
            case MESSAGES_ALLROW:
                count = database.delete(TABLE_MESSAGES, selection, selectionArgs);
                break;

            case MESSAGES_SINGLEROW:
                count = database.delete(TABLE_MESSAGES, "_id = "+"'"+selection+"'", selectionArgs);
                break;

            case FRIENDS_ALLROW:
                count = database.delete(TABLE_FRIENDS, selection, selectionArgs);
                break;

            case FRIENDS_SINGLEROW:
                count = database.delete(TABLE_FRIENDS, "_id = ?", new String[]{uri.getLastPathSegment()});
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }
//--------------------------------------------------------------------------------------------------------------------------------
    private static class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "CHAT.db";
    private static final int DATABASE_VERSION = 1;
    private static final String CREATE_TABLE_FRIENDS=
              "CREATE TABLE "+TABLE_FRIENDS +" ("+
                      COL_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                      COL_EMAIL +" TEXT NOT NULL, "+
                      COL_NAME +" TEXT, "+
                      COL_PHONE+" TEXT);";
    private static final String CREATE_TABLE_MESSAGES=
            "CREATE TABLE "+TABLE_MESSAGES + " ("+
                    COL_NUMBER + " INTEGER PRIMARY KEY AUTOINCREMENT, "+
                    COL_TAKE +" TEXT, "+
                    COL_GIVE +" TEXT, "+
                    COL_AT+" TEXT,"+
                    COL_MSG + " TEXT, " +
                    COL_ID+" INTEGER);";

        public DBHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE_MESSAGES);
            db.execSQL(CREATE_TABLE_FRIENDS);

        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " +  TABLE_FRIENDS + ";");
            db.execSQL("DROP TABLE IF EXISTS " +  TABLE_MESSAGES + ";");
            onCreate(db);
        }


        }

}
