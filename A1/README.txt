COMP4601 - A1 : Searchable Document Archive (SDA)
Elisa Kazan - 100965140
Jack McCracken - 100962692

DESCRIPTION:
This app saves an archive of documents and allows one to create new documents, delete and edit old documents along with searching and deleting by tags.

HOW TO TEST:
1. Start MongoDB (on Mac: mongod --config /usr/local/etc/mongod.conf)
2. Start the Eclipse Server by importing the A1 project into an Eclipse workspace
configured as in class.
3. Run the Android App by opening the project in Android Studio and hitting the run button.
4. Now to test functionality...
    A. Create a Document: Click the plus, add an id and hit okay. Fill in fields and click the "checkbox" when done. Hit back to view your new document in the list.
    B. View Document: Click on the document to view it.
    C. Edit Document: Click the pencil, make your changes, click the "checkbox". Click back to view your changes in the list.
    D. Delete Document: Click on a document, click the "x", see the document has disappeared.
    E. Search Tags: Make sure you have sufficient test documents to search from. Click the 3 dots in the top right, click "search tags", enter your desired tags to search (separated by colons), click okay. Observe list. Press backwards.
    F. Delete Tags: Make sure you have sufficient test documents to delete from. Click the 3 dots in the top right, click "delete tags", enter your desired tags to delete (separated by colons), click okay. Observe the documents have gone.


