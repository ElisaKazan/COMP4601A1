package edu.carleton.sdamobileapp;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import edu.carleton.sdamobileapp.dao.Document;
import edu.carleton.sdamobileapp.dao.DocumentCollection;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static edu.carleton.sdamobileapp.dao.DocumentCollection.PREFIX;

/**
 * An activity representing a list of Documents. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link DocumentDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class DocumentListActivity extends AppCompatActivity {
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private RecyclerView recyclerView;

    private class DownloadDocumentsTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids)
        {
            // Connect to server
            try {
                URL url = new URL(PREFIX + "/documents");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Accept", "application/xml");
                BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                if (urlConnection.getResponseCode() != 200) {
                    StringBuilder builder = new StringBuilder();

                    String line;

                    while ((line = r.readLine()) != null) {
                        builder.append(line).append("\n");
                    }

                    String text = builder.toString();
                    Toast.makeText(DocumentListActivity.this, "Cannot get documents: " + text, Toast.LENGTH_LONG).show();
                }

                XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(urlConnection.getInputStream(), null);
                DocumentCollection.getInstance().addDocumentsFromXml(parser);
            } catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            recyclerView.removeAllViews();
            recyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        new DownloadDocumentsTask().execute();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Switch to Detail Fragment where we will create a new document
                Context context = view.getContext();
                Intent intent = new Intent(context, DocumentDetailActivity.class);

                context.startActivity(intent);
            }
        });

        View recyclerViewView = findViewById(R.id.document_list);
        assert recyclerViewView != null;

        this.recyclerView = (RecyclerView)recyclerViewView;

        setupRecyclerView(this.recyclerView);

        if (findViewById(R.id.document_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(DocumentCollection.getInstance().getItems()));
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<Document> documents;

        public SimpleItemRecyclerViewAdapter(List<Document> docs) {
            documents = docs;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.document_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = documents.get(position);
            holder.mIdView.setText(String.valueOf(documents.get(position).getId()));
            holder.mNameView.setText(documents.get(position).getName());
            holder.mTagsView.setText("(" + TextUtils.join(", ", documents.get(position).getTags()) + ")");

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(DocumentDetailFragment.ARG_ITEM_ID, String.valueOf(holder.mItem.getId()));
                        DocumentDetailFragment fragment = new DocumentDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.document_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, DocumentDetailActivity.class);
                        intent.putExtra(DocumentDetailFragment.ARG_ITEM_ID, String.valueOf(holder.mItem.getId()));

                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return documents.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mIdView;
            public final TextView mNameView;
            public final TextView mTagsView;
            public Document mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mIdView = (TextView) view.findViewById(R.id.id);
                mNameView = (TextView) view.findViewById(R.id.content);
                mTagsView = (TextView) view.findViewById(R.id.tags);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mNameView.getText() + "'";
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return true;
    }
}
