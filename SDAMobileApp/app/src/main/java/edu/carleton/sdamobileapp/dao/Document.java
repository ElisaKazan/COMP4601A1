package edu.carleton.sdamobileapp.dao;

import android.os.Parcel;
import android.os.Parcelable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Document {
    private Integer id;
    private Float score;
    private String name;
    private String text;
    private ArrayList<String> tags;
    private ArrayList<String> links;

    private static final String ns = null;
    public static final String XML_DOC_TAG = "document";

    private static final String XML_TEXT_TAG = "text";
    private static final String XML_ID_TAG = "id";
    private static final String XML_TAGS_TAG = "tags";
    private static final String XML_NAME_TAG = "name";
    private static final String XML_LINKS_TAG = "links";

    public Document() {
        tags = new ArrayList<>();
        links = new ArrayList<>();
    }

    public Document(Integer id) {
        this();
        this.id = id;
    }

    public Document(int id, String name, String text, ArrayList<String> tags, ArrayList<String> links) {
        this.id = id;
        this.name = name;
        this.text = text;
        this.tags = tags;
        this.links = links;
    }

    public Document(XmlPullParser parser) throws IOException, XmlPullParserException {
        // If it doesn't start with a <document> tag, there's a major problem
        parser.require(XmlPullParser.START_TAG, ns, XML_DOC_TAG);

        // Obviously wrong ID so we know if something went wrong
        int id = -500;
        ArrayList<String> tags = new ArrayList<>();
        ArrayList<String> links = new ArrayList<>();
        String name = null;
        String text = null;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String tagName = parser.getName();

            if (tagName.equalsIgnoreCase(XML_TAGS_TAG)) {
                tags.add(readText(parser));
            }
            else if (tagName.equalsIgnoreCase(XML_LINKS_TAG)) {
                links.add(readText(parser));
            }
            else if (tagName.equalsIgnoreCase(XML_ID_TAG)) {
                id = Integer.valueOf(readText(parser));
            }
            else if (tagName.equalsIgnoreCase(XML_TEXT_TAG)) {
                text = readText(parser);
            }
            else if (tagName.equalsIgnoreCase(XML_NAME_TAG)) {
                name = readText(parser);
            }
        }

        // The tags we can actually check
        if (id != -500 && name != null && text != null) {
            this.id = id;
            this.name = name;
            this.tags = tags;
            this.links = links;
            this.text = text;
        }
        else {
            throw new XmlPullParserException("Missing elements in <document>");
        }
    }

    // For the tags title and summary, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public Document(Map<?, ?> map) {
        this();
        this.id = (Integer) map.get("id");
        this.score = (Float) map.get("score");
        this.name = (String) map.get("name");
        this.text = (String) map.get("text");
        this.tags = (ArrayList<String>) map.get("tags");
        this.links = (ArrayList<String>) map.get("links");
    }

    public Integer getId() {
        return id;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    public Float getScore() {
        return score;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    public ArrayList<String> getLinks() {
        return links;
    }

    public void setLinks(ArrayList<String> links) {
        this.links = links;
    }

    public void addTag(String tag) {
        tags.add(tag);
    }

    public void removeTag(String tag) {
        tags.remove(tag);
    }

    public void addLink(String link) {
        links.add(link);
    }

    public void removeLink(String link) {
        links.remove(link);
    }

    @Override
    public boolean equals(Object d) {
        return d == null || !(d instanceof Document) ? false : ((Document)d).id == this.id;
    }
}