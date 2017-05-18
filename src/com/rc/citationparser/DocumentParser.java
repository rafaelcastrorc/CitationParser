package com.rc.citationparser;

import org.apache.commons.lang3.StringUtils;
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
    private COSDocument cosDoc;
    private PDDocument pdDoc;
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
    protected String getReference(String authorRegex, String authors) {
        //For the case when the citations are numbered.
        //Ex: 1. Jacobson MD, Weil M, Raff MC: Programmed cell death in animal development. Cell 1997, 88:347-354.
        String patternCase1 = "\\d+(\\.( ).*)\\b" + authorRegex + "\\b(.*)";
        Pattern pattern1 = Pattern.compile(patternCase1);
        Matcher matcher1 = pattern1.matcher(parsedText);
        ArrayList<String> result = new ArrayList<>();
        System.out.println("Citations found for paper");
        while (matcher1.find()) {
            System.out.println("Found " + matcher1.group());
            result.add(matcher1.group());
        }
        if (result.isEmpty()) {
            return "";
        }
        if (result.size() > 1) {
            result = solveReferenceTies(result, authors);
        }
        return result.get(0);
    }

    //Based on LevenshteinDistance, uses the authors names to find the most similar citation
    private ArrayList<String> solveReferenceTies(ArrayList<String> result, String authors) {
        ArrayList<String> newResult = new ArrayList<>();
        int smallest = Integer.MAX_VALUE;

        for(String s : result) {

            int newDistance = StringUtils.getLevenshteinDistance(s, authors);
            if ( newDistance < smallest) {
                smallest = newDistance;
                newResult.add(0, s);
            }
        }
        return newResult;
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
                answer = inTextCitationContainsDash(answer);
            }

            result.add(answer);

        }
        if (result.isEmpty()) {
            System.out.println("ERROR - Could not find citations for document");
        }
        return result;
    }

    private String inTextCitationContainsDash(String answer) {

        //Ex: [20-23] It needs to be displayed as [20,21,22,23]
        //Ex: [20,23-25, 27] needs to be displayed as [20, 23, 24, 25, 27]
        int counter = 0;
        ArrayList<String> newAnswer = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean thereIsADash = false;
        for (Character c : answer.toCharArray()) {
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
                    }
                    else {
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
        answer = " ";
        int counter2 = 0;
        for (String s : newAnswer) {
            if (counter2 > 0) {
                answer = answer + ',' + s;
            } else {
                answer = answer + s;
            }
            counter2++;
        }
        return answer;
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
