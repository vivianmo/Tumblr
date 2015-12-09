package me.vivianmo.tumblr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tumblr.jumblr.types.Photo;
import com.tumblr.jumblr.types.PhotoPost;
import com.tumblr.jumblr.types.PhotoSize;
import com.tumblr.jumblr.types.Post;
import com.tumblr.jumblr.types.TextPost;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Created by Vivian Mo on 12/5/2015.
 */
public class LoaderAdapter extends BaseAdapter {

    private List<Post> posts = new ArrayList<>();   //list of posts
    private int[] pp;                               //array with photo ids
    private LayoutInflater layoutInflater;
    private MyDBHandler dbHandler;
    private BitmapCache cache;

    public LoaderAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
        pp = new int[] {
            R.id.photo1, R.id.photo2, R.id.photo3, R.id.photo4, R.id.photo5,
                R.id.photo6, R.id.photo7, R.id.photo8, R.id.photo9, R.id.photo10, R.id.photo11
        };
        dbHandler = new MyDBHandler(context);
        cache = BitmapCache.getInstance();

    }

    @Override
    public int getCount() {
        return posts.size();
    }

    @Override
    public Post getItem(int position) {
        return posts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //special methods for formatting photosets
    //will format 540/500px, 268/245px, and 177/194px photosets
    private View getPhotoset(PhotoPost post, ViewGroup parent) {
        View convertView = layoutInflater.inflate(R.layout.post_photoset540, parent, false);
        boolean resize = false;
        boolean odd = false;
        List<Photo> photos = post.getPhotos();

        //photo width used to determine photoset size and whether it needs resizing
        int size = photos.size();
        Log.d("Photoset size:", ""+size);
        Photo guinea = photos.get(0);
        int width = guinea.getOriginalSize().getWidth();
        Log.d("Photoset width: ", ""+width);
        if (width < 200) {
            convertView = layoutInflater.inflate(R.layout.post_photoset177, parent, false);
        }
        else if (width < 450) {
            convertView = layoutInflater.inflate(R.layout.post_photoset268, parent, false);
            if (size%2 != 0) odd = true;
        }
        else if (width > 500) resize = true;
        PhotoSize got;
        String extra = "";

        //iterates through all the photos in the photoset
        //uses url of photo as key
        //first looks in cache of photo, then database, then retrieves it from network
        //if it is larger than 500px, will retrieve the 500px sized photo from network
        //displays photo
        for (int i = 0; i<size; i++) {
            ImageView image = (ImageView) convertView.findViewById(pp[i]);
            Photo photo = photos.get(i);
            if (resize) {
                List<PhotoSize> sizes = photo.getSizes();
                got = sizes.get(1);
            }
            else {
                got = photo.getOriginalSize();
            }
            String purl = got.getUrl();
            extra = purl;
            Log.d("V", purl);
            Bitmap cbmp = cache.getBitmap(purl);
            if (cbmp != null) image.setImageBitmap(cbmp);
            else if(dbHandler.wasSaved(purl)) {
                Log.d("Database: ", "getting photoset image from database");
                byte[] b = dbHandler.getImage(purl);
                Bitmap bmp = BitmapFactory.decodeByteArray(b, 0, b.length);
                cache.putBitmap(purl, bmp);
                image.setImageBitmap(bmp);
            }
            else new DownloadImageTask(image, ""+post.getId(), true)
                    .execute(purl);
        }

        //if it is a 268/245px photoset and there is an odd number of elements
        //(why anyone would do this i don't know but someone did on the dash i got)
        //just displays the last photo twice so keep with formatting
        if (odd) {
            ImageView image = (ImageView) convertView.findViewById(pp[size]);
            Bitmap cbmp = cache.getBitmap(extra);
            image.setImageBitmap(cbmp);
        }

        //sets type as photoset and sets caption
        TextView type = (TextView) convertView.findViewById(R.id.type);
        type.setText("photoset");
        TextView caption = (TextView) convertView.findViewById(R.id.captionphoto);
        caption.setText(post.getCaption());
        return convertView;
    }

    //formats the current post into photo, photoset, text post, or other
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Post current = posts.get(position);
        String url = current.getPostUrl();
        String id = ""+current.getId();
        Log.d("URL:", url);
        String ctype = current.getType();

        //if the post if a photoset, will first check if is photoset
        //calls special photoset formatting method if it is a photoset
        //photo uses the url of the entire post as the key
        //first looks in cache of photo, then database, then retrieves it from network
        //if it is larger than 500px, will retrieve the 500px sized photo from network
        //displays photo, sets type as photo, sets caption
        if (ctype.equals("photo")) {
            PhotoPost pcurrent = (PhotoPost) current;
            if (pcurrent.isPhotoset()) {
                return getPhotoset(pcurrent,parent);
            }
            else {
                convertView = layoutInflater.inflate(R.layout.post_photo, parent, false);
                Log.d("Returned", "is a photo");
                List<Photo> photos = pcurrent.getPhotos();
                Photo photo = photos.get(0);

                TextView caption = (TextView) convertView.findViewById(R.id.captionphoto);
                TextView type = (TextView) convertView.findViewById(R.id.type);
                ImageView image = (ImageView) convertView.findViewById(R.id.photo);

                caption.setText(pcurrent.getCaption());
                type.setText(current.getType());

                Bitmap cbmp = cache.getBitmap(id);
                if (cbmp != null) {
                    Log.d("Cache: ", "getting image from cache");
                    image.setImageBitmap(cbmp);
                }

                else if (dbHandler.wasSaved(id)) {
                    Log.d("Database: ", "getting image from database");
                    byte[] b = dbHandler.getImage(id);
                    Bitmap bmp = BitmapFactory.decodeByteArray(b, 0, b.length);
                    cache.putBitmap(id, bmp);
                    image.setImageBitmap(bmp);
                }

                else {
                    PhotoSize got = photo.getOriginalSize();
                    if (got.getWidth() > 500) {
                        List<PhotoSize> sizes = photo.getSizes();
                        got = sizes.get(1);
                        }
                    String purl = got.getUrl();
                    Log.d("V", purl);
                    new DownloadImageTask(image, id, false)
                            .execute(purl);
                }
            }
        }

        //sets title and caption for text post
        else if (ctype.equals("text")) {
            convertView = layoutInflater.inflate(R.layout.post_text, parent, false);
            Log.d("Returned", "is a text post");
            TextPost tcurrent = (TextPost) current;
            TextView title = (TextView) convertView.findViewById(R.id.title);
            TextView body = (TextView) convertView.findViewById(R.id.body);
            title.setText(tcurrent.getTitle());
            body.setText(tcurrent.getBody());
        }

        //displays type of post for non photo and text posts
        else {
            convertView = layoutInflater.inflate(R.layout.post_other, parent, false);
            TextView type = (TextView) convertView.findViewById(R.id.type);
            type.setText(ctype);
        }
        return convertView;
    }

    public void swapData(Collection<Post> data) {
        this.posts.clear();
        this.posts.addAll(data);
        notifyDataSetChanged();
    }


    public void addData(List<Post> data) {
        this.posts.addAll(data);
        notifyDataSetChanged();
    }

    //downloads image from network
    //puts image in database and cache so we never have to call this method
    //on this image again for it takes too long
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        String id;
        boolean photoset;
        String urldisplay = "";

        public DownloadImageTask(ImageView bmImage, String id, boolean photoset) {
            this.bmImage = bmImage;
            this.id = id;
            this.photoset = photoset;
        }

        protected Bitmap doInBackground(String... urls) {
            Log.d("Getting image: ", urls[0]);
            this.urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }

            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            result.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            if (photoset) {
                dbHandler.addImage(byteArray, urldisplay);
                cache.putBitmap(urldisplay, result);
            }
            else {
                Log.d("Cache: ", "saving image to cache");
                dbHandler.addImage(byteArray, id);
                cache.putBitmap(id, result);
            }
            bmImage.setImageBitmap(result);
        }
    }


}
