package com.rc.citationparser;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

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

    private com.rc.citationparser.DocumentParser documentParser;
    private Controller controller = new Controller(null, false);



    @org.junit.jupiter.api.Test
    void testInvalidDocument() throws IOException {
        //File does not exist
        File file = new File("./testingFiles/Test999.pdf");
        Throwable exception = assertThrows(IOException.class, () -> {
            new DocumentParser(file, true, false);
        });
        assertEquals("ERROR: File does not exist", exception.getMessage());
    }

    @org.junit.jupiter.api.Test
    void testGetNumericalReferenceAuthorDoesNotAppear() throws IOException {
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
    void testGetNumericalReference() throws IOException {
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

        file = new File("./testingFiles/Test3.pdf");
        documentParser = new DocumentParser(file, true, false);
        author = "Thome M, Hofmann K, Burns K, Martinon F,";
        authorRegex = controller.generateReferenceRegex(author, true);
        result = documentParser.getReference(authorRegex, author);
        assertEquals("37. Thome M, Hofmann K, Burns K, Martinon F, Bodmer JL, Mattmann C and\n" +
                "Tschopp J (1998", result);
        documentParser.close();


    }

    @org.junit.jupiter.api.Test
    void testGetReferenceNonNumerical() {
        //Get the reference and include the number
        File file = new File("./testingFiles/Test4.pdf");
        try {
            documentParser = new DocumentParser(file, true, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Last name of author appears twice
        String author = "Li P., D. Nijhawan, I. Budihardjo";
        String authorRegex = controller.generateReferenceRegex(author, true);
        String result = documentParser.getReference(authorRegex, author);
        assertEquals("Li, P., D. Nijhawan, I. Budihardjo, S.M. Srinivasula, M. Ahmad, E.S. Alnemri,\n" +
                "and X. Wang. 1997", result);

       //Year contains lettter
        author = "Kluck R.M., E. Bossy-Wetzel, D.R. Green";
        authorRegex = controller.generateReferenceRegex(author, true);
        result = documentParser.getReference(authorRegex, author);
        assertEquals("Kluck, R.M., E. Bossy-Wetzel, D.R. Green, and D.D. Newmeyer. 1997a", result);
        documentParser.close();
    }

    @org.junit.jupiter.api.Test
    void testGetInTextCitationsNumeric() {
        //Get the in-text citations when they are expressed as numbers between []
        File file = new File("./testingFiles/Test2.pdf");
        try {
            documentParser = new DocumentParser(file, true, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayList<String> result = documentParser.getInTextCitations(true);
        assertEquals(66, result.size());
        //Simple
        assertTrue(result.contains("[5]"));
        //Two
        assertTrue(result.contains("[1,2]"));
        //three
        assertTrue(result.contains("[9,11,12]"));
        //Special symbol
        assertTrue(result.contains("[3,4••]"));
        //Has dash [8-10]
        assertTrue(result.contains("8,9,10"));
        //Has dash and normal [24,26,29–32]
        assertTrue(result.contains("24,26,29,30,31,32"));
        //Does not exist
        assertFalse(result.contains("[240]"));
        documentParser.close();
    }

    @org.junit.jupiter.api.Test
    void testGetInTextCitationsNumericSuperScript() {
        //Get the in-text citations when they are expressed as numbers between []
        File file = new File("./testingFiles/Test3.pdf");
        try {
            documentParser = new DocumentParser(file, true, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayList<String> result = documentParser.getInTextCitations(true);
        assertEquals(110, result.size());
        System.out.println(result);
        //Simple
        assertTrue(result.contains("3"));
        assertTrue(result.contains("28"));
        //Two
        assertTrue(result.contains("1,2"));
        //three
        assertTrue(result.contains("12,22,23"));
        //Has dash 7-9
        assertTrue(result.contains("7,8,9"));

        //Different font
        assertTrue(result.contains("76,77,78,79,80,81,82"));

        //Invalid
        assertFalse(result.contains("283"));
        assertFalse(result.contains("283"));
        assertFalse(result.contains("341"));

        //Does not exist
        assertFalse(result.contains("3000"));
        documentParser.close();
    }

    @org.junit.jupiter.api.Test
    void testSolveReferenceTies() {
        File file = new File("./testingFiles/Test1.pdf");
        try {
            documentParser = new DocumentParser(file, true, false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Basic case
        ArrayList<String> possibilities = new ArrayList<>();
        possibilities.add("Rafael Castro");
        possibilities.add("Jose Castro");
        ArrayList<String> answer = documentParser.solveReferenceTies(possibilities, "Rafael Castro.");
        assertEquals(1, answer.size());
        assertEquals("Rafael Castro", answer.get(0));

        possibilities.add("Rafael Castro");

        //Base where there is an unsolvable tie so should throw error
        Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
            documentParser.solveReferenceTies(possibilities, "Rafael Castro.");
        });
        assertEquals("ERROR: THERE WAS AN ERROR FINDING THE CITATION IN THIS PAPER, PLEASE INCLUDE MORE THAN 3 AUTHORS' NAMES FOR EACH OF THE TWIN PAPERS" +
                "\nIf the error persist, please inform the developer.", exception.getMessage());

        documentParser.close();
    }


    @org.junit.jupiter.api.Test
    void testInTextCitationContainsDash() {
        //The method prints white space at beginning of string.
        File file = new File("./testingFiles/Test1.pdf");
        try {
            documentParser = new DocumentParser(file, true, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String answer = documentParser.inTextCitationContainsDash("[15–20]");
        assertEquals("15,16,17,18,19,20", answer);
        answer = documentParser.inTextCitationContainsDash("[5, 15–20]");
        assertEquals("5,15,16,17,18,19,20", answer);
        answer = documentParser.inTextCitationContainsDash("15–20");
        assertEquals("15,16,17,18,19,20", answer);
        documentParser.close();
    }

    @org.junit.jupiter.api.Test
    void getInTextCitationsBetweenParenthesis() {
        //Get the in-text citations when they are expressed as numbers between []
        File file = new File("./testingFiles/Test4.pdf");
        try {
            documentParser = new DocumentParser(file, true, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayList<String> result = documentParser.getInTextCitations(true);
        assertEquals(49, result.size());
        assertTrue(result.contains("(cys-\n" +
                "teine aspartate–specific proteases) as the molecu-\n" +
                "lar instigators of apoptosis (Yuan et al., 1993;\n" +
                "Gagliardini et al., 1994; Kumar et al., 1994; Lazebnik et al.,\n" +
                "1994; Wang et al., 1994; Nicholson et al., 1995; Tewari et\n" +
                "al., 1995; Kuida et al., 1996)"));
        assertTrue(result.contains("(Martin and Green,\n" +
                "1995)"));
        assertTrue(result.contains("(Liu et al., 1996; Kluck et al., 1997a,b;\n" +
                "Deveraux et al., 1998; Pan et al., 1998a)"));
        assertTrue(result.contains("(Zou et al.,\n" +
                "1997)"));
        assertTrue(result.contains("(Martin et al., 1995b, 1996)"));

        assertTrue(result.contains("(Slee, E.A., and\n" +
                "S.J. Martin, data not shown)"));
        documentParser.close();

    }



    @org.junit.jupiter.api.Test
    void testGetTitle() {
        //Gets the title, based on analysis of text and font size

        //Case 1: Document has no text
        File file = new File("./testingFiles/Test1.pdf");
        try {
            //Does not need to parse the entire doc and needs format
            documentParser = new DocumentParser(file, false, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String answer = documentParser.getTitle();
        documentParser.close();
        assertEquals("No title found", answer);

        //Case 2: Files with titles
        //Testing file 2.pdf
        file = new File("./testingFiles/Test2.pdf");
        try {
            //Does not need to parse the entire doc and needs format
            documentParser = new DocumentParser(file, false, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        answer = documentParser.getTitle();
        documentParser.close();
        assertEquals("Apoptosis control by death and decoy receptors", answer);

        //Testing file 4.pdf
        file = new File("./testingFiles/Test4.pdf");
        try {
            //Does not need to parse the entire doc and needs format
            documentParser = new DocumentParser(file, false, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        answer = documentParser.getTitle();
        documentParser.close();
        assertEquals("Ordering the Cytochrome c–initiated Caspase Cascade: Hierarchical Activation of " +
                "Caspases-2, -3, -6, -7, -8, and -10 in a Caspase-9–dependent Manner", answer);

        //Testing twin A.pdf
        file = new File("./A.pdf");
        try {
            //Does not need to parse the entire doc and needs format
            documentParser = new DocumentParser(file, false, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        answer = documentParser.getTitle();
        documentParser.close();
        assertEquals("Bid, a Bcl2 Interacting Protein, Mediates Cytochrome c Release from Mitochondria in Response to Activation of Cell Surface Death Receptors", answer);

        //Testing file 3.pdf
        file = new File("./testingFiles/Test3.pdf");
        try {
            //Does not need to parse the entire doc and needs format
            documentParser = new DocumentParser(file, false, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        answer = documentParser.getTitle();
        documentParser.close();
        assertEquals("Caspase structure, proteolytic substrates, and function during apoptotic cell death", answer);

    }


    @org.junit.jupiter.api.Test
    void testGetAuthors() throws IOException {
        //to get authors, it is necessary to first get the title

        //Case 1: Document has no text
        File file = new File("./testingFiles/Test1.pdf");
        try {
            //Does not need to parse the entire doc and needs format
            documentParser = new DocumentParser(file, false, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        documentParser.getTitle();
        String answer = documentParser.getAuthors();
        assertEquals("No authors found", answer);
        documentParser.close();


        //Case 2: Files with titles

        file = new File("./testingFiles/Test2.pdf");
        try {
            //Does not need to parse the entire doc and needs format
            documentParser = new DocumentParser(file, false, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        documentParser.getTitle();
        answer = documentParser.getAuthors();
        assertEquals("Avi Ashkenazi and Vishva M Dixit", answer);
        documentParser.close();


        file = new File("./testingFiles/Test3.pdf");
        try {
            //Does not need to parse the entire doc and needs format
            documentParser = new DocumentParser(file, false, true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        documentParser.getTitle();
        answer = documentParser.getAuthors();
        assertEquals("DW Nicholson", answer);
        documentParser.close();


        file = new File("./testingFiles/Test4.pdf");
        try {
            //Does not need to parse the entire doc and needs format
            documentParser = new DocumentParser(file, false, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        documentParser.getTitle();
        answer = documentParser.getAuthors();
        assertEquals("Elizabeth A. Slee, Mary T. Harte, Ruth M. Kluck", answer);
        documentParser.close();

        file = new File("./B.pdf");
        try {
            //Does not need to parse the entire doc and needs format
            documentParser = new DocumentParser(file, false, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        documentParser.getTitle();
        answer = documentParser.getAuthors();
        assertEquals("Honglin Li, Hong Zhu, Chi-jie Xu, and Junying Yuan", answer);
        documentParser.close();


        file = new File("./A.pdf");
        try {
            //Does not need to parse the entire doc and needs format
            documentParser = new DocumentParser(file, false, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        documentParser.getTitle();
        answer = documentParser.getAuthors();
        assertEquals("Xu Luo, Imawati Budihardjo, Hua Zou", answer);
        documentParser.close();


    }


    @org.junit.jupiter.api.Test
    void testGetInTextCitationsCase1() {
    }


    @org.junit.jupiter.api.Test
    void testGetInTextCitationsCase2() {
    }


    @org.junit.jupiter.api.Test
    void testGetSuperScriptSize() {
        File file = new File("./testingFiles/Test1.pdf");
        try {
            //Does not need to parse the entire doc and needs format
            documentParser = new DocumentParser(file, false, true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Test 1: PDF with no text
        String answer = documentParser.getSuperScriptSize(new HashMap<>(), 0);
        assertEquals("", answer);

        //Test 2: PDF with no text body
        HashMap<Float, Integer> map = new HashMap<>();
        map.put((float) 2, 4);
        documentParser.getSuperScriptSize(map, 0);
        assertEquals(Float.POSITIVE_INFINITY, documentParser.textBodySize);

        //Test 3: PDF with text body
        map = new HashMap<>();
        map.put((float) 8, 2);
        documentParser.getSuperScriptSize(map, 0);
        assertEquals((float) 8, documentParser.textBodySize);

        //Just one size
        map = new HashMap<>();
        map.put((float) 8, 2);
        map.put((float) 7, 100);
        map.put((float) 6, 100);
        map.put((float) 5, 40);
        answer = documentParser.getSuperScriptSize(map, 5);
        assertEquals((float) 7, documentParser.textBodySize);
        assertEquals("6.0", answer);


        //Two possible sizes
        map.put((float) 5, 50);
        answer = documentParser.getSuperScriptSize(map, 5);
        assertEquals((float) 7, documentParser.textBodySize);
        assertEquals("6.0|5.0", answer);

        //Three possible sizes
        map.put((float) 4, 100);
        answer = documentParser.getSuperScriptSize(map, 4);
        assertEquals((float) 7, documentParser.textBodySize);
        assertEquals("6.0|4.0|5.0", answer);

    }

    @org.junit.jupiter.api.Test
    void testFormatSuperScript() {
    }




}