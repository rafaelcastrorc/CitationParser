package com.rc.citationparser;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by rafaelcastro on 5/15/17.
 * Parses a pdf and converts it to pdf.
 */
class DocumentParser {
    private PDFParser parser;
    private PDFTextStripper pdfStripper;
    private COSDocument cosDoc ;
    private  PDDocument pdDoc;
    private String parsedText = "";




    DocumentParser(File fileToParse) throws IOException {

        this.pdfStripper = null;
        parser = new PDFParser(new RandomAccessBufferedFileInputStream(fileToParse)); // update for PDFBox V 2.0
        parser.parse();
        cosDoc = parser.getDocument();
        pdfStripper = new PDFTextStripper();
        pdDoc = new PDDocument(cosDoc);

        pdfStripper.setStartPage(1);
        pdfStripper.setEndPage(pdDoc.getNumberOfPages());
        this.parsedText = pdfStripper.getText(pdDoc);
    }

    protected String getText() {
        System.out.print(parsedText);
        return parsedText;


    }

    //Retrieves how a twin paper was referenced
    protected String getReference(String authorRegex) {
        //For the case when the citations are numbered.
        //Ex: 1. Jacobson MD, Weil M, Raff MC: Programmed cell death in animal development. Cell 1997, 88:347-354.
        String patternCase1 = "\\d+(\\.( ).*)\\b"+authorRegex+"\\b(.*)";
        Pattern pattern1 = Pattern.compile(patternCase1);
        Matcher matcher1 = pattern1.matcher(parsedText);
        ArrayList<String> result = new ArrayList<>();
        System.out.println("Citations");
        while (matcher1.find()) {
            System.out.println("Found " + matcher1.group());
            result.add(matcher1.group());
        }
        if (result.isEmpty()) {
            return "";
        }
        if (result.size() > 1) {
            result = solveReferenceTies();
        }
        return result.get(0);
    }

    private ArrayList<String> solveReferenceTies() {
        //Todo: Implement this method
        return null;
    }

    protected ArrayList<String> getInTextCitations() {
        //For the case where in-text citations are displayed as numbers
        //Ex: [1] or [4,5] or [4,5•] or [4•] or [4-20]
        ArrayList<String> result = new ArrayList<>();
        String patternCase1 = "\\[\\d+(•)*(–\\d+(•)*)*(,( )*\\d+(•)*((–\\d+)(•)*)*)*]";
        Pattern pattern1 = Pattern.compile(patternCase1);
        Matcher matcher1 = pattern1.matcher(parsedText);
        System.out.println();
        while (matcher1.find()) {
            System.out.println("Found " + matcher1.group());
            //If citation contains a '–', it needs to be modified
            String answer = matcher1.group();
            if (answer.contains("–")) {
                //Todo: Do something
            }
            result.add(matcher1.group());

        }
        if (result.isEmpty()) {
            System.out.println("ERROR - Could not find citations for document");
        }
        return result;


    }

    protected boolean isValidCitation() {
        return false;
    }

    protected void close() {
        try {
            pdDoc.close();
            cosDoc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }




}
