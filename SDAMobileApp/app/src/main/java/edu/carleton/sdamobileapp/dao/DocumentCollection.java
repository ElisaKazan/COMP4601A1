package edu.carleton.sdamobileapp.dao;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DocumentCollection {
    public static final String PREFIX = "http://10.0.2.2:8080/COMP4601SDA/rest/sda";
    private Map<Integer, Document> documentsMap = new HashMap<>();
    private List<Document> documents = new ArrayList<>();

    private static DocumentCollection documentCollection;

    public DocumentCollection() {
//        add(new Document(0, "Elisa's Doc", "HELLLLLLLLOOOOOO", new ArrayList<>(Arrays.asList("thing1", "thing3")), new ArrayList<String>(Arrays.asList("http://www.ofnc.ca/breports.php","http://www.birdscanada.org/"))));
//        add(new Document(1, "Jack's Doc", "HAIIIIIII", new ArrayList<>(Arrays.asList("thing1", "thing55")), new ArrayList<String>(Arrays.asList("https://en.wikipedia.org/wiki/Bird","https://www.getpostman.com/docs/introduction"))));
    }

    public void add(Document d) {
        documents.add(d);
        documentsMap.put(d.getId(), d);
    }

    public void removeDocument(Document d) {
        System.out.println(documents.remove(d));
        documentsMap.remove(d);
    }

    public void addDocumentsFromXml(XmlPullParser parser) {
        documents.clear();
        documentsMap.clear();

        try {
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }

                if (parser.getName().equalsIgnoreCase(Document.XML_DOC_TAG)) {
                    Document document = new Document(parser);

                    documentsMap.put(document.getId(), document);
                    documents.add(document);
                }
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
    }

    public Document get(int id) {
        return documentsMap.get(id);
    }

    public static DocumentCollection getMainInstance() {
        if (documentCollection == null) {
            documentCollection = new DocumentCollection();
        }

        return documentCollection;
    }

    public List<Document> getItems() {
        return documents;
    }
}
