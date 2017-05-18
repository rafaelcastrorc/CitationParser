package com.rc.citationparser;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import java.io.File;
import java.io.IOException;

/**
 * Created by Rafael Castro on 5/15/17.
 * Formats the metadata of any pdf document.
 * Used to retrieve the information from a given document, as well as to correctly format it.
 */
class FileFormatter {
    protected static File file;
    protected static COSDocument cosDoc;
    protected static PDDocument pdDoc;
    //Since pdf files not always contains the title, it is necessary to manually assign it


    /**
     * Sets the file that will be formated
     * @param file - file to be formatted
     */
    static void setFile(File file) {
        FileFormatter.file = file;
        formatFile();
    }

    /**
     * Constructor. Takes no args.
     */
    static void formatFile() {
        PDFParser parser;
        try {
            parser = new PDFParser(new RandomAccessBufferedFileInputStream(file));
            parser.parse();

            cosDoc = parser.getDocument();
            pdDoc = new PDDocument(cosDoc);

        } catch (IOException e) {
        }
    }

    /**
     * Adds author to a given pdf document
     *
     * @param s - the name of the authors.
     */
    static void addAuthors(String s) {
        PDDocumentInformation currInfo = pdDoc.getDocumentInformation();
        currInfo.setAuthor(s);
        pdDoc.setDocumentInformation(currInfo);
        try {
            pdDoc.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Adds a title to a given pdf document
     *
     * @param s - the title of the document
     */
    static void addTitle(String s) {

        PDDocumentInformation currInfo = pdDoc.getDocumentInformation();
        currInfo.setTitle(s);
        pdDoc.setDocumentInformation(currInfo);
        try {
            pdDoc.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the title and the author of the current document, based on the metadata
     *
     * @return a string with the information
     */
     static String getCurrentInfo() {
        return "The title of the paper: " + pdDoc.getDocumentInformation().getTitle() + "\n" +
                "The authors: " + pdDoc.getDocumentInformation().getAuthor();
    }

    /**
     * Gets the authors of the current document, based on the metadata
     *
     * @return a string with the authors
     */
    static String getAuthors() {
        return pdDoc.getDocumentInformation().getAuthor();
    }

    /**
     * Gets the title of the current document, based on the metadata
     *
     * @return a string with the title
     */
    static String getTitle() {
        return pdDoc.getDocumentInformation().getTitle();
    }




    /**
     * Closes the current file
     */
    static void closeFile() {
        try {
            pdDoc.close();
            cosDoc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
