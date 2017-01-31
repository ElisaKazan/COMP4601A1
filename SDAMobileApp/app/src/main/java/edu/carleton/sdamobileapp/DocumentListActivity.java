package edu.carleton.sdamobileapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import edu.carleton.sdamobileapp.dao.Document;
import edu.carleton.sdamobileapp.dao.DocumentCollection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
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
    private DocumentCollection collection;

    private class DeleteDocuments extends AsyncTask<Void, Void, Boolean> {
        private String tags;
        private String errorText;

        public DeleteDocuments(String tags) {
            this.tags = tags;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                URL url = new URL(PREFIX + "/delete/" + tags);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                if (urlConnection.getResponseCode() != 200) {
                    StringBuilder builder = new StringBuilder();

                    String line;

                    while ((line = r.readLine()) != null) {
                        builder.append(line).append("\n");
                    }

                    errorText = builder.toString();

                    return false;
                }



                return true;
            } catch (IOException e) {
                errorText = e.getMessage();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                // Success
                Toast.makeText(DocumentListActivity.this, "Delete successful!", Toast.LENGTH_LONG).show();
                // Reload the documents
                if (getIntent().getExtras() == null) {
                    collection = DocumentCollection.getMainInstance();
                    new DownloadDocumentsTask().execute();
                }
                else {
                    collection = new DocumentCollection();
                    new DownloadDocumentsTask(getIntent().getExtras().getStringArray("tags")).execute();
                }
            }
            else {
                Toast.makeText(DocumentListActivity.this, "Delete by tags failed: " + errorText, Toast.LENGTH_LONG).show();
            }
        }
    }

    private class DownloadDocumentsTask extends AsyncTask<Void, Void, Boolean> {
        String errorText;
        String[] tags;

        public DownloadDocumentsTask() {

        }

        public DownloadDocumentsTask(String[] tags) {
            this.tags = tags;
        }

        @Override
        protected Boolean doInBackground(Void... voids)
        {
            URL url;
            // Connect to server
            try {
                if (tags == null) {
                    url = new URL(PREFIX + "/documents");
                }
                else {
                    url = new URL(PREFIX + "/search/" + TextUtils.join(":", tags));
                }
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Accept", "application/xml");
                BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                if (urlConnection.getResponseCode() != 200) {
                    StringBuilder builder = new StringBuilder();

                    String line;

                    while ((line = r.readLine()) != null) {
                        builder.append(line).append("\n");
                    }

                    errorText = builder.toString();
                    return false;
                }

                XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(urlConnection.getInputStream(), null);
                collection.addDocumentsFromXml(parser);
            } catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
                errorText = e.getMessage();
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                recyclerView.removeAllViews();
                recyclerView.getAdapter().notifyDataSetChanged();
            }
            else {
                Toast.makeText(DocumentListActivity.this, "Download failed: " + errorText, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        if (getIntent().getExtras() == null) {
            collection = DocumentCollection.getMainInstance();
            new DownloadDocumentsTask().execute();
        }
        else {
            collection = new DocumentCollection();
            new DownloadDocumentsTask(getIntent().getExtras().getStringArray("tags")).execute();
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Alert box for id
                AlertDialog.Builder builder = new AlertDialog.Builder(DocumentListActivity.this);
                builder.setTitle("Add New Document");

                final EditText input = new EditText(DocumentListActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent launchNewDoc = new Intent(DocumentListActivity.this, DocumentDetailActivity.class);
                        launchNewDoc.putExtra("newID", input.getText().toString());
                        startActivity(launchNewDoc);
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.show();
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
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(collection.getItems()));
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.deleteTags:
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Please enter a colon-separated list of tags to delete");

            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new DeleteDocuments(input.getText().toString()).execute();
                }
            });

            builder.setNegativeButton("Cancel", null);
            builder.show();
            return true;
        }
        case R.id.searchTags:
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Please enter a colon-separated list of tags to search for");

            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent launchDocumentList = new Intent(DocumentListActivity.this, DocumentListActivity.class);
                    launchDocumentList.putExtra("tags", input.getText().toString().split(":"));
                    startActivity(launchDocumentList);
                }
            });

            builder.setNegativeButton("Cancel", null);
            builder.show();
            return true;
        }
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
