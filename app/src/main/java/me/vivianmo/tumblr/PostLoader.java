package me.vivianmo.tumblr;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.Blog;
import com.tumblr.jumblr.types.Post;
import com.tumblr.jumblr.types.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Vivian Mo on 12/5/2015.
 */
public class PostLoader extends AsyncTaskLoader<List<Post>> {

    int offset;     //offset, used for deugging
    int limit;      //how many posts per page
    int count;      // which page we're on

    public PostLoader(Context context, int count) {
        super(context);
        offset = 0;
        limit = 5;
        this.count = count;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    //uses the authentication mumbo jumbo you gave to
    //gets posts from dashboard specified by count limit and offset
    @Override
    public List<Post> loadInBackground() {
        String consumerKey = "Omq1FerYKMWeZnlvrIH9Qy3r6YIbyVDPdkQSfU5obu8eJBnt5n";
        String consumerSecret = "GHqE8rxq6r0IXCbBkj9NPR4ed0EIBqb8xP9k6PdulMuwsxJfyo";
        String oAuthToken = "5AX3lj6EjPUVTbMOKvuMHBPb8M4NWZN5kerNXo4v7RYmzPKXCC";
        String oAuthSecret = "KsvGQwzzuKM1fv7jMNdEhDqH1NwpJL7JB6AUoxxBEfweLKh6np";
        JumblrClient client = new JumblrClient(consumerKey, consumerSecret);
        client.setToken(oAuthToken, oAuthSecret);
        Map<String, Integer> options = new HashMap<String, Integer>();
        options.put("limit", limit);
        options.put("offset", (count)*limit+offset);
        List<Post> posts = client.userDashboard(options);
        return posts;
    }

    @Override
    public void deliverResult(List<Post> posts) {
        super.deliverResult(posts);
    }
}
















