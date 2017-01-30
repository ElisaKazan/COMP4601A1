package edu.carleton.sdamobileapp.dao;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
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
    private Map<Integer, Document> documentsMap = new HashMap<Integer, Document>();
    private List<Document> documents = new ArrayList<Document>();

    public DocumentCollection() {

    }

    public void addDocumentsFromXml(XmlPullParser parser) {

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
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
