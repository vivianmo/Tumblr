package me.vivianmo.tumblr;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
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

    private ListView listView;
    private LoaderAdapter adapter;
    int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        count = 1;

        adapter = new LoaderAdapter(this);
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);

        getLoaderManager().initLoader(0, null, loaderCallbacks);

        listView.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                Log.d("Scroll: ", "scrolled to the end");
                count++;
                getLoaderManager().restartLoader(0, null, loaderCallbacks);
                return true;
            }
        });

    }

    public void customLoadMoreDataFromApi(int offset) {

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

    private LoaderManager.LoaderCallbacks<List<Post>> loaderCallbacks = new LoaderManager.LoaderCallbacks<List<Post>>() {

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





















