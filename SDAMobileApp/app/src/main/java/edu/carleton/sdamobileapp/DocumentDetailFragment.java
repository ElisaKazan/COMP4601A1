package edu.carleton.sdamobileapp;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import edu.carleton.sdamobileapp.dao.Document;
import edu.carleton.sdamobileapp.dao.DocumentCollection;

import static edu.carleton.sdamobileapp.dao.DocumentCollection.PREFIX;

/**
 * A fragment representing a single Document detail screen.
 * This fragment is either contained in a {@link DocumentListActivity}
 * in two-pane mode (on tablets) or a {@link DocumentDetailActivity}
 * on handsets.
 */
public class DocumentDetailFragment extends Fragment {

    private int newId;

    private class SaveDocumentTask extends AsyncTask<Void, Void, Boolean> {
        String errorText = null;
        ArrayList<String> tags = null;
        ArrayList<String> links = null;
        String name = null;
        String text = null;
        int id;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            View rootView = getView();

            tags = new ArrayList<>(Arrays.asList(((EditText) rootView.findViewById(R.id.document_tags)).getText().toString().split(", ?")));
            links = new ArrayList<>(Arrays.asList(((EditText) rootView.findViewById(R.id.document_links)).getText().toString().split(", ?")));
            name = ((EditText) rootView.findViewById(R.id.document_name)).getText().toString();
            text = ((EditText) rootView.findViewById(R.id.document_text)).getText().toString();
            if (mItem == null) {
                id = newId;
            }
            else {
                id = mItem.getId();
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            // Connect to server
            try {
                URL url = new URL(PREFIX + "/" + (mItem == null ? "" : id));
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                // POST works for either
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                PrintStream os = new PrintStream(urlConnection.getOutputStream());

                StringBuilder queryStringBuilder = new StringBuilder("id=" + String.valueOf(id) +
                        "&name=" + URLEncoder.encode(name, "UTF-8") + "&text=" + URLEncoder.encode(text, "UTF-8"));

                for (String tag : tags) {
                    queryStringBuilder.append("&tags=").append(URLEncoder.encode(tag, "UTF-8"));
                }

                for (String link : links) {
                    queryStringBuilder.append("&links=").append(URLEncoder.encode(link, "UTF-8"));
                }

                os.print(queryStringBuilder.toString());

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

                // It succeeded
            } catch (IOException e) {
                errorText = e.getMessage();
                e.printStackTrace();
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                if (mItem == null) {
                    mItem = new Document();
                }

                mItem.setName(name);
                mItem.setText(text);
                mItem.setTags(tags);
                mItem.setLinks(links);
                mItem.setId(id);

                Toast.makeText(getActivity(), "Upload successful!", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(getActivity(), errorText, Toast.LENGTH_LONG).show();
            }
            super.onPostExecute(result);
        }
    }

    private class DeleteDocumentTask extends AsyncTask<Void, Void, Boolean> {

        String errorText;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            // Connect to server
            try {
                URL url = new URL(PREFIX + "/" + mItem.getId());
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                // POST works for either
                urlConnection.setRequestMethod("DELETE");
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

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
                // It succeeded
            } catch (IOException e) {
                errorText = e.getMessage();
                e.printStackTrace();
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                Toast.makeText(getActivity(), "Delete successful!", Toast.LENGTH_LONG).show();
                getActivity().setResult(0);
                getActivity().finish();
            }
            else {
                Toast.makeText(getActivity(), errorText, Toast.LENGTH_LONG).show();
            }

            DocumentCollection.getMainInstance().removeDocument(mItem);
            super.onPostExecute(result);
        }
    }

    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private Document mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DocumentDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItem = DocumentCollection.getMainInstance().get(Integer.valueOf(getArguments().getString(ARG_ITEM_ID)));

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle("Document " + mItem.getId());
            }
        }
        else if (getArguments().containsKey("newID")) {
            newId = Integer.valueOf(getArguments().getString("newID"));
        }
    }

    public void saveDetails() {
        new SaveDocumentTask().execute();
    }

    public void delete() {
        new DeleteDocumentTask().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.document_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            ((EditText) rootView.findViewById(R.id.document_name)).setText(mItem.getName());
            ((EditText) rootView.findViewById(R.id.document_tags)).setText(TextUtils.join(", ", mItem.getTags()));
            ((EditText) rootView.findViewById(R.id.document_links)).setText(TextUtils.join(", ", mItem.getLinks()));
            ((EditText) rootView.findViewById(R.id.document_text)).setText(mItem.getText());
        }
        else {
            // Creating a new document

            // Use default data
            ((EditText) rootView.findViewById(R.id.document_name)).setText("Document Name");
            ((EditText) rootView.findViewById(R.id.document_tags)).setText("tag1, tag2");
            ((EditText) rootView.findViewById(R.id.document_links)).setText("www.example.com");
            ((EditText) rootView.findViewById(R.id.document_text)).setText("This is the default text for a document.");

            // Make editable
            ((EditText) rootView.findViewById(R.id.document_name)).setEnabled(true);
            ((EditText) rootView.findViewById(R.id.document_tags)).setEnabled(true);
            ((EditText) rootView.findViewById(R.id.document_links)).setEnabled(true);
            ((EditText) rootView.findViewById(R.id.document_text)).setEnabled(true);

            // Save will happen when the stop is clicked
        }

        return rootView;
    }
}
