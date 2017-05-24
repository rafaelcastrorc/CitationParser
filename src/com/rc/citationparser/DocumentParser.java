package com.rc.citationparser;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by rafaelcastro on 5/15/17.
 * Parses a pdf document, retrieves the relevant information.
 */
class DocumentParser {
    private final Logger log;
    private PDFParser parser;
    private PDFTextStripper pdfStripper;
    private COSDocument cosDoc;
    private PDDocument pdDoc;
    private String parsedText = "";
    private boolean isNumbered = false;
    protected float largestFont;
    protected float smallestFont;
    protected HashMap<Float, Integer> fontSizes;


    /**
     * Constructor. Takes 1 argument.
     *
     * @param fileToParse - pdf document that needs to be parsed.
     * @param parseEntireDoc - true if you want to parse the entire file, false to parse only the first page
     * @throws IOException - If there is an error reading the file
     */
    DocumentParser(File fileToParse, boolean parseEntireDoc) throws IOException {
        this.log = Logger.getInstance();
        this.pdfStripper = null;
        parser = new PDFParser(new RandomAccessBufferedFileInputStream(fileToParse));
        parser.parse();
        cosDoc = parser.getDocument();

        //To keep track of the fonts
        largestFont = 0;
        smallestFont = Float.POSITIVE_INFINITY;
        fontSizes = new HashMap<>();

        pdfStripper = new PDFTextStripper() {
            //Modifies the way the text is parsed by including font size
            float prevFontSize = 0;

            protected void writeString(String text, List<TextPosition> textPositions) throws IOException
            {

                StringBuilder builder = new StringBuilder();

                for (TextPosition position : textPositions)
                {
                    float baseSize = position.getFontSizeInPt();

                    if (baseSize != prevFontSize)
                    {
                        builder.append("{|").append(baseSize).append("|}");
                        prevFontSize = baseSize;
                        if (smallestFont > baseSize) {
                            smallestFont = baseSize;
                        }
                        if (largestFont < baseSize) {
                            largestFont = baseSize;
                        }
                        if (fontSizes.get(baseSize) == null) {
                            fontSizes.put(baseSize, 1);
                        }
                        else {
                            int prev = fontSizes.get(baseSize);
                            fontSizes.put(baseSize, prev + 1);
                        }
                    }
                     builder.append(position.getUnicode());
                }

                writeString(builder.toString());
            }
        };
        pdDoc = new PDDocument(cosDoc);
        pdfStripper.setStartPage(1);
        if (parseEntireDoc) {
            pdfStripper.setEndPage(pdDoc.getNumberOfPages());
        }
        else {
            pdfStripper.setEndPage(1);
        }
        this.parsedText = pdfStripper.getText(pdDoc);
        getText(); //delete

    }

    /**
     * Gets the plain text from a pdf document. Removes all formatting
     *
     * @return string with all the text
     */
    protected String getText() {
        System.out.print(parsedText);
        return parsedText;
    }

    /**
     * Gets the full reference used in a given paper to cite a given twin paper.
     * Works in the following cases:
     * For the case when the citations are numbered.
     * Ex: 1. Jacobson MD, Weil M, Raff MC: Programmed cell death in animal development. Cell 1997, 88:347-354.
     * For the case when the citations are not numbered.
     * Ex: Jacobson MD, Weil M, Raff MC: Programmed cell death in animal development. Cell 1997, 88:347-354.
     *
     * @param authorRegex - Regex based on the names of the authors of a given twin paper.
     * @param authors - authors of a given twin paper.
     * @return string with the reference used in the paper. Starts with author name and finishes with the year the paper was published.
     */
    String getReference(String authorRegex, String authors) {
        //Pattern use to capture the citation. Starts with the author name and ends with the year the paper was published.
        String patternCase1 = "[^.]*(\\d+(\\.( ).*))*(" + authorRegex + ")([^;)])*?((\\b((18|19|20)\\d{2}( )?([A-z])*(,( )?(((18|19|20)\\d{2}([A-z])*)|[A-z]))*)\\b)|unpublished data|data not shown)";
        Pattern pattern1 = Pattern.compile(patternCase1);
        Matcher matcher1 = pattern1.matcher(parsedText);
        ArrayList<String> result = new ArrayList<>();
        log.writeToLogFile("Citations found for paper");
        log.newLine();

        while (matcher1.find()) {
            System.out.println("Found " + matcher1.group());
            log.writeToLogFile("Found " + matcher1.group());
            log.newLine();
            result.add(matcher1.group());
        }
        if (result.isEmpty()) {
            log.writeToLogFile("No reference found");
            log.newLine();
            return "";
        }
        if (result.size() > 1) {
            log.writeToLogFile("There is a tie");
            log.newLine();
            result = solveReferenceTies(result, authors);
        }
        return result.get(0);


    }

    /**
     * Uses Levenshtein Distance to solve reference ties by using the names to find the most similar citation
     *
     * @param result  - list with all the possible references
     * @param authors - names of the authors of a given twin paper
     * @return arrayList with one element, which is the correct reference.
     */
    protected ArrayList<String> solveReferenceTies(ArrayList<String> result, String authors) {
        ArrayList<String> newResult = new ArrayList<>();
        int smallest = Integer.MAX_VALUE;

        for (String s : result) {

            int newDistance = StringUtils.getLevenshteinDistance(s, authors);
            if (newDistance == smallest) {
                log.writeToLogFile("ERROR: There was an error solving the tie");
                log.newLine();
                //Ties should not happen so throw an error
                System.out.print("Error coming from getting the reference");
                System.err.println("ERROR: THERE WAS AN ERROR FINDING THE CITATION IN THIS PAPER, PLEASE INCLUDE MORE THAN 3 AUTHORS' NAMES FOR EACH OF THE TWIN PAPERS" +
                        "\nIf the error persist, please inform the developer.");
            }
            if (newDistance < smallest) {
                smallest = newDistance;
                newResult.add(0, s);
            }
        }
        return newResult;
    }

    /**
     * Gets all the in-text citation of a given pdf document.
     * @return ArrayList with all the citations
     */
    protected ArrayList<String> getInTextCitations() {
        //For the case where in-text citations are displayed as numbers
        //Ex: [1] or [4,5] or [4,5•] or [4•] or [4-20]
        ArrayList<String> result1 = new ArrayList<>();
        String patternCase1 = "\\[\\d+(•)*(–\\d+(•)*)*(,( )*\\d+(•)*((–\\d+)(•)*)*)*]";
        Pattern pattern1 = Pattern.compile(patternCase1);
        Matcher matcher1 = pattern1.matcher(parsedText);
        System.out.println();
        while (matcher1.find()) {
            String answer = matcher1.group();
            log.writeToLogFile("Found CASE 1 " + answer);
            log.newLine();
            //If citation contains a '–', it needs to be modified
            if (answer.contains("–")) {
                answer = inTextCitationContainsDash(answer);
            }

            result1.add(answer);

        }
        if (result1.isEmpty()) {
            log.writeToLogFile("WARNING - Could not find in-text citations for document - CASE 1");
            log.newLine();
            System.out.println("WARNING - Could not find in-text citations for document - CASE 1"); //delete
        }
        ArrayList<String> result2 = new ArrayList<>();
        String patternCase2 = "\\(\\D*(unpublished data|data not shown|\\d{4})([a-zA-Z](,[a-zA-Z])*)*(, (unpublished data|data not shown|\\d{4}))*(;\\D*\\d{4}([a-zA-Z](,[a-zA-Z])*)*)*\\)";
        Pattern pattern2 = Pattern.compile(patternCase2);
        Matcher matcher2 = pattern2.matcher(parsedText);

        System.out.println();
        while (matcher2.find()) {
            String answer = matcher2.group();
            log.writeToLogFile("Found CASE 2 " + answer);
            log.newLine();
            result2.add(answer);

        }

        if (result2.isEmpty()) {
            System.out.println("WARNING - Could not find in-text citations for document - CASE 2");//delete
            log.writeToLogFile("WARNING - Could not find in-text citations for document - CASE 2");
            log.newLine();
        }


        if (result1.isEmpty() && result2.isEmpty()) {
            System.err.println("ERROR - Could not find in-text citations in this document"); //delete
            log.writeToLogFile("ERROR - Could not find in-text citations in this document");
            log.newLine();
            return new ArrayList<>();
        }


        if (result1.size() > result2.size()) {
            //Case 1 is going to be used
            isNumbered = true;
            return result1;
        } else {
            //Case 2 is going to be used
            //Might change in the future Todo
            isNumbered = false;
            return result2;

        }

    }

    /**
     * Formats a number citation that contains a dash
     * Ex: [20-23] It needs to be displayed as [20,21,22,23]
     * Ex: [20,23-25, 27] needs to be displayed as [20, 23, 24, 25, 27]
     * @param citationWithDash - citation that contains the dash
     * @return string with the citation formatted correctly
     */
    private String inTextCitationContainsDash(String citationWithDash) {
        int counter = 0;
        ArrayList<String> newAnswer = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean thereIsADash = false;
        for (Character c : citationWithDash.toCharArray()) {
            if (c == '–') {
                newAnswer.add(sb.toString());
                sb = new StringBuilder();
                counter++;
                thereIsADash = true;

            } else {
                if (c == ',') {
                    if (counter > 0 && thereIsADash) {
                        int leftSide = Integer.parseInt(newAnswer.get(counter - 1));
                        int rightSide = Integer.parseInt(sb.toString());
                        while (leftSide < rightSide) {
                            leftSide = leftSide + 1;
                            newAnswer.add(String.valueOf(leftSide));
                            counter++;
                        }
                        thereIsADash = false;
                    } else {
                        newAnswer.add(sb.toString());
                        sb = new StringBuilder();
                        counter++;
                    }


                } else {
                    if (c != ' ' && c != '[' && c != ']') {
                        sb.append(c);
                    }
                }
            }
        }
        int leftSide = Integer.parseInt(newAnswer.get(counter - 1));
        int rightSide = Integer.parseInt(sb.toString());
        while (leftSide < rightSide) {
            leftSide = leftSide + 1;
            newAnswer.add(String.valueOf(leftSide));
        }
        citationWithDash = " ";
        int counter2 = 0;
        for (String s : newAnswer) {
            if (counter2 > 0) {
                citationWithDash = citationWithDash + ',' + s;
            } else {
                citationWithDash = citationWithDash + s;
            }
            counter2++;
        }
        return citationWithDash;
    }


    /**
     * Closes the file that is being parsed.
     */
    void close() {
        try {
            log.writeToLogFile("Closing file");
            log.newLine();
            pdDoc.close();
            cosDoc.close();
        } catch (IOException e) {
            System.err.println("ERROR: There was an error closing the file");
            log.writeToLogFile("There was a problem closing the file");
            log.newLine();
        }

    }

    /**
     * True if the bibliography citations are numbers. Ex: [4], [5, 7]
     * False if the citations are between parenthesis and written out. Ex (Ellis et al 2010).
     * @return - boolean
     */
    boolean bibliographyIsNumbered() {
        return isNumbered;
    }


    protected String getTitle() {
        String pattern = "(\\{\\|"+ largestFont +")([^{])*";
        Pattern pattern1 = Pattern.compile(pattern);
        Matcher matcher = pattern1.matcher(parsedText);
        String result ="";
        if (matcher.find()) {
            result = matcher.group();
        }
        if (result.isEmpty()) {
            return "No title found";
        }
        result = result.replace("\n", " ").replace("\r", " ");
        boolean bracketIsVisible = false;
        boolean startParsingTitle = false;
        StringBuilder sb = new StringBuilder();
        for (Character c :result.toCharArray()) {
            if (c == '|') {
                bracketIsVisible = true;
            }
            else if (bracketIsVisible) {
                if (c == '}') {
                    bracketIsVisible = false;
                    startParsingTitle = true;
                }
            }
            else if (startParsingTitle) {
                sb.append(c);
            }
        }

        return sb.toString();
    }
}
