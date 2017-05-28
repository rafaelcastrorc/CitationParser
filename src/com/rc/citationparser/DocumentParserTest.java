package com.rc.citationparser;

import org.junit.jupiter.api.BeforeAll;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by rafaelcastro on 5/27/17.
 * Test cases for DocumentParser class
 * For testing purposes:
 *  Test2.pdf = numerical references with in-text citations numbered formatted between []
 *  Test3.pdf = numerical references with in-text citations numbered formatted as superscript
 *  Test4.pdf = references without numbers with in text citations between parenthesis
 */
class DocumentParserTest {

    com.rc.citationparser.DocumentParser documentParser;
    Controller controller = new Controller(null, false);



    @org.junit.jupiter.api.Test
    void invalidDocument() throws IOException {
        //File does not exist
        File file = new File("./testingFiles/Test999.pdf");
        Throwable exception = assertThrows(IOException.class, () -> {
            new DocumentParser(file, true, false);
        });
        assertEquals("ERROR: File does not exist", exception.getMessage());
    }


    @org.junit.jupiter.api.Test
    void getNumericalReferenceAuthorDoesNotAppear() throws IOException {
        //Get the reference and include the number
        File file = new File("./testingFiles/Test2.pdf");
        documentParser = new DocumentParser(file, true, false);
        //Names have to be separated with comma.
        String author = "Rafael Castro";
        String authorRegex = controller.generateReferenceRegex(author, true);
        String result = documentParser.getReference(authorRegex, author);
        assertEquals("", result);
    }

    @org.junit.jupiter.api.Test
    void getNumericalReference() throws IOException {
        //Get the reference and include the number
        File file = new File("./testingFiles/Test2.pdf");
        documentParser = new DocumentParser(file, true, false);
        //Names have to be separated with comma.
        String author = "Woo M, Hakem R, Soengas MS, Duncan GS, Shahinian A";
        String authorRegex = controller.generateReferenceRegex(author, true);
        String result = documentParser.getReference(authorRegex, author);
        assertEquals("57. Woo M, Hakem R, Soengas MS, Duncan GS, Shahinian A, Kagi D,\n" +
                "• Hakem A, McCurrach M, Khoo W, Kaufman SA et al.: Essential\n" +
                "contribution of caspase-3/CPP32 to apoptosis and its associated\n" +
                "nuclear changes. Genes Dev 1998", result);

        author = "Steller H";
        authorRegex = controller.generateReferenceRegex(author, true);
        result = documentParser.getReference(authorRegex, author);
        assertEquals("1. Steller H: Mechanisms and genes of cellular suicide. Science\n" +
                "1995", result);
        documentParser.close();
    }

    @org.junit.jupiter.api.Test
    void getReferenceNonNumerical() {

    }

    @org.junit.jupiter.api.Test
    void getInTextCitationsNumeric() {
    }


    @org.junit.jupiter.api.Test
    void getInTextCitationsNumericSuperScript() {
    }

    @org.junit.jupiter.api.Test
    void getInTextCitations() {
    }

    @org.junit.jupiter.api.Test
    void close() {
    }

    @org.junit.jupiter.api.Test
    void getTitle() {
    }

    @org.junit.jupiter.api.Test
    void getTitle2() {
    }

}