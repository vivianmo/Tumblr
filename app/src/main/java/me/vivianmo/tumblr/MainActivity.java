package me.vivianmo.tumblr;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.ListView;
import android.widget.TextView;


import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.Blog;
import com.tumblr.jumblr.types.Post;
import com.tumblr.jumblr.types.User;


import java.util.Collections;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private ListView listView;          //displays the posts
    private LoaderAdapter adapter;      //loads posts into list view
    int count;                          //what page we're on
    boolean dialogopen;                 //is a dialog box open

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        count = 1;
        dialogopen = false;

        adapter = new LoaderAdapter(this);
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);

        //will only start loading posts if network if available
        //will display dialog box if no network, shut down app once
        //dialog box is closed
        if (checkNetwork()) {
            getLoaderManager().initLoader(0, null, loaderCallbacks);
        }
        else alert();

        //implements infinite scroll
        //will restart loaderCallbacks to get next page of posts
        //displays dialog box if no network is available
        listView.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                Log.d("Scroll: ", "scrolled to the end");
                if (checkNetwork()) {
                    count++;
                    getLoaderManager().restartLoader(0, null, loaderCallbacks);
                    return true;
                }
                else if (!dialogopen) alert2();
                return false;
            }
        });

    }

    //check if network is available
    public boolean checkNetwork() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = connMgr.getActiveNetworkInfo();
        if (netinfo != null && netinfo.isConnected()) {
            return true;
        }
        else return false;
    }

    //alert for if there's no network when app is first opened
    //wil shut down app once dialog box is closed
    public void alert(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Please connect to the internet then reopen the app");

        alertDialogBuilder.setNegativeButton("Ok",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    //alert for it's there's no network when user is trying to load new page
    public void alert2(){
        dialogopen = true;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Please connect to the internet to load more posts");

        alertDialogBuilder.setNegativeButton("Ok",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                dialogopen = false;
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //loader manager that gets list of posts from PostLoader
    //calls PostLoader with count, which is what page we are currently on
    private LoaderManager.LoaderCallbacks<List<Post>> loaderCallbacks =
            new LoaderManager.LoaderCallbacks<List<Post>>() {

        @Override
        public Loader<List<Post>> onCreateLoader(int id, Bundle args) {
            Log.d("New Loader with count: ", ""+count);
            return new PostLoader((getApplicationContext()), count);
        }

        @Override
        public void onLoadFinished(Loader<List<Post>> loader, List<Post> data) {
            Log.d("Loader: ", "adding data");
            adapter.addData(data);
        }

        @Override
        public void onLoaderReset(Loader<List<Post>> loader) {
            adapter.swapData(Collections.<Post>emptyList());

        }

    };
}





















