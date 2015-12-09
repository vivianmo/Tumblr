package me.vivianmo.tumblr;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.util.Log;

public class MyDBHandler extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "images.db";
    public static final String TABLE_IMAGES = "images";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_IMAGE = "img";
    public static final String COLUMN_POSTID = "postId";

    public MyDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_IMAGES + "(" +
                COLUMN_POSTID + " TEXT PRIMARY KEY, " +
                COLUMN_IMAGE + " BLOB"
                +");";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGES);
        onCreate(db);
    }

    //adds image as a byte array blob
    //id is either url of post or url of image
    public void addImage (byte[] img, String id) {
        Log.d("Database: ", "trying to add image: " + id);
        if (!wasSaved(id)) {
            Log.d("Database: ", "adding image: " + id);
            ContentValues values = new ContentValues();
            values.put(COLUMN_IMAGE, img);
            values.put(COLUMN_POSTID, id);
            SQLiteDatabase db = getWritableDatabase();
            db.insert(TABLE_IMAGES, null, values);
            db.close();
        }
    }

    //gets image as a byte[]
    public byte[] getImage(String id) {
        Log.d("Database: ", "getting image");
        String[] columns = new String[]{COLUMN_POSTID, COLUMN_IMAGE};
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.query(TABLE_IMAGES, columns, COLUMN_POSTID + "=?", new String[] {id}, null, null, null);
        if(c != null) {
            c.moveToFirst();
            byte[] img = c.getBlob(1);
            return img;
        }
        return null;
    }

    //checks if there is an image for a certain url id
    public boolean wasSaved(String id) {
        String[] columns = new String[]{COLUMN_POSTID, COLUMN_IMAGE};
        SQLiteDatabase db = getWritableDatabase();
        Cursor cur1=db.query(TABLE_IMAGES, columns, COLUMN_POSTID + "=?", new String[] {id}, null, null, null, null);
        cur1.moveToLast();
        int count1=cur1.getCount();
        if(count1==0)
        {
            return false;

        }
        else
        {
            return true;
        }
    }

    //updates an image, which shouldn't happen
    public void update(String id, byte[] img) {
        ContentValues c = new ContentValues();
        c.put(COLUMN_IMAGE, img);
        SQLiteDatabase db = getWritableDatabase();
        db.update(TABLE_IMAGES, c, COLUMN_POSTID + "=" + id, null);
    }

    //counts how many images are in db
    public int count() {
        SQLiteDatabase db = getWritableDatabase();
        String count = "SELECT count(*) FROM " + TABLE_IMAGES;
        Cursor mcursor = db.rawQuery(count, null);
        mcursor.moveToFirst();
        int icount = mcursor.getInt(0);
        return icount;
    }

    //checks if db is empty
    public boolean isEmpty() {
        SQLiteDatabase db = getWritableDatabase();
        String count = "SELECT count(*) FROM " + TABLE_IMAGES;
        Cursor mcursor = db.rawQuery(count, null);
        mcursor.moveToFirst();
        int icount = mcursor.getInt(0);
        if (icount > 0) return false;
        else return true;

    }

}







