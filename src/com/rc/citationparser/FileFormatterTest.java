package com.rc.citationparser;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;

/**
 * Created by rafaelcastro on 5/27/17.
 * Testing for the FileFormatter class
 */
class FileFormatterTest {
    private PDDocument test;

    /**
     * Create new empty pdf with no metadata
     * @throws IOException unable to read file
     */
    private void createEmptyFile() throws IOException {
        // Create a new empty document
        test = new PDDocument();
        // Create a new blank page and add it to the document
        PDPage blankPage = new PDPage();
        test.addPage(blankPage);
        // Save the newly created document
        test.save("./testingFiles/Test1.pdf");
        test.close();
    }



    @Test
    void testSetFile() throws IOException {
        createEmptyFile();
        FileFormatter.setFile(new File("./testingFiles/Test1.pdf"));
        assertEquals(new File("./testingFiles/Test1.pdf"), FileFormatter.file);
    }

    @Test
    void testAddAuthors() {
        FileFormatter.addAuthors("Rafael Castro.");
        assertEquals("Rafael Castro.", FileFormatter.getAuthors());
    }

    @Test
    void testAddTitle() {
        FileFormatter.addTitle("Testing Document.");
        assertEquals("Testing Document.", FileFormatter.getTitle());
    }

    @Test
    void testAddYear() {
        FileFormatter.addYear("2019");
        assertEquals(2019, FileFormatter.getYear());
    }

    @Test
    void testGetCurrentInfo() {
       assertEquals("The title of the paper: Testing Document.\nThe authors: Rafael Castro.",
               FileFormatter.getCurrentInfo());
    }

    @Test
    void testCloseFile() {
        try {
            FileFormatter.closeFile();
        } catch (IOException e) {
            fail("Was not able to close the file");
        }
    }



}