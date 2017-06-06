package com.rc.citationparser;

import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by rafaelcastro on 5/16/17.
 * Controls the view and retrieves information from the parser
 */

class Controller extends Observer {
    private final Logger log;
    private ViewInterface cView;
    private File twinFile1;
    private File twinFile2;
    private File[] comparisonFiles;
    private TreeMap<Integer, ArrayList<Object>> dataGathered;
    //Input receieved when using GUI
    String guiInput = "";
    boolean isGUI;
    SubmitListener submit;
    String holder = "";

    //Is gui is if it is a graphical ui, else false

    Controller(ViewInterface cView, boolean start, boolean isGUI) {
        this.cView = cView;
        this.log = Logger.getInstance();
        if (isGUI) {
            this.isGUI = isGUI;
        }
        if (start) {
            startProgram();
        }
    }

    /**
     * Displays the main menu options
     */
    private void displayOptions() {
        cView.displayInstruction("Please select an option:" +
                "\nPress 1 to set to twin files" +
                "\nPress 2 to set the folder that contains the files that will be analyzed" +
                "\nPress 3 to analyze the number of files that cite both" +
                "\nPress 4 to output the result" +
                "\nPress 5 to Exit");
    }


    /**
     * Starts the program. Works for GUI as well as command line
     */
    protected void startProgram() {
        displayOptions();
        int choice = cView.getUserChoice();
        log.writeToLogFile("Input: " + Integer.toString(choice));
        boolean end = false;
        while (!end) {

            if (choice == 1) {
                setTwinFiles();
                cView.displayToScreen("Twin files have been set.");
                if (isGUI) {
                    choice = 0;
                }

            } else if (choice == 2) {
                setComparisonFiles();
                cView.displayToScreen("The folder has been set.");


            } else if (choice == 3) {
                analyzeFiles();

            } else if (choice == 4) {
                ArrayList<Object> list = new ArrayList<>();
                //Headers of the excel output file
                list.add("Paper");
                list.add("Number cites A");
                list.add("Number cites B");
                list.add("Number cites A&B");
                list.add("Adjacent-Cit Rate");
                dataGathered.put(0, list);
                outputToFile();

            } else if (choice == 5) {
                end = true;
            }
            if (!end) {
                displayOptions();
                choice = cView.getUserChoice();
                log.newLine();
                log.writeToLogFile("Input: " + Integer.toString(choice));
            }
        }
        log.closeLogger();
        System.exit(1);

    }


    /**
     * Sets the metadata information of the twin files
     */
    private void setTwinFiles() {
        log.newLine();
        cView.displayInstruction("Not all pdf files are formatted the same. The program will try to " +
                "get the necessary data, but you will have to verify its accuracy");

        //For first tween file
        cView.displayInstruction("Please type the name of the first twin file. Ex: doc1.pdf");
        File file1;
        if (isGUI) {
            waitForInput("getFile");
            file1 = (File) submit.getResult();
            holder = "";
        } else {
            file1 = cView.getFile();
        }


        log.writeToLogFile("File 1 name " + file1.getName());
        log.newLine();
        twinFile1 = setTwinFileHelper(file1, 1);
        try {
            FileFormatter.closeFile();
        } catch (IOException e) {
            cView.displayErrorToScreen(e.getMessage());
        }

        //For file 2
        cView.displayInstruction("Please type the name of the second twin file. Ex: doc2.pdf");
        File file2 = null;
        if (isGUI) {
            waitForInput("getFile");
            file2 = (File) submit.getResult();
        }
        else {
            file2 = cView.getFile();
        }


        log.writeToLogFile("File 2 name " + file2.getName());
        log.newLine();
        twinFile2 = setTwinFileHelper(file2, 2);
        try {
            FileFormatter.closeFile();
        } catch (IOException e) {
            cView.displayErrorToScreen(e.getMessage());
        }
    }


    /**
     * Helper method to set the twin files
     * @param file file to set
     * @param num 1 if its the first twin, 2 if its the second twin
     * @return File
     */
    private File setTwinFileHelper(File file, int num) {
        try {
            FileFormatter.setFile(file);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        cView.displayToScreen(FileFormatter.getCurrentInfo());
        cView.displayInstruction("If the information is correct press 1. \nIf you want the program " +
                "to try to get the right information press 2. \nTo write the information manually press 3.");
        int choice;
        if (isGUI) {
            String[] options = new String[]{"3", "2", "1"};
            choice = Integer.valueOf(cView.displayPopUp(options));
        }
        else {
            choice = cView.getUserChoice();
        }
        boolean valid = false;
        while (!valid) {

            if (choice == 1) {
                //If information is correct
                if (num == 1) {
                    twinFile1 = file;
                } else {
                    twinFile2 = file;
                }
                valid = true;

            } else if (choice == 2) {
                cView.displayToScreen("Loading");
                DocumentParser documentParser = null;
                try {
                    documentParser = new DocumentParser(file, false, true);
                } catch (IOException e) {
                    cView.displayErrorToScreen("There was an error parsing the file");
                }

                System.out.println(documentParser.smallestFont);
                System.out.println(documentParser.largestFont);
                String possTitle = documentParser.getTitle();
                boolean end = false;
                while (!end) {
                    cView.displayInstruction("Is this title of the document:\n" + possTitle +
                            "\nPress Y to save this as the title, press N to manually input the name");
                    if (isGUI) {
                        String[] options = new String[]{"3", "2", "1"};
                        choice =Integer.valueOf(cView.displayPopUp(options));
                    }
                    else {
                        String ans;
                        if (isGUI) {
                            String[] options = new String[]{"Yes", "No"};
                            ans = cView.displayPopUp(options);
                        }
                        else {
                             ans = cView.getInput();
                        }

                        if (ans.equals("Y") || ans.equals("y") || ans.equals("Yes")) {
                            FileFormatter.addTitle(possTitle);
                            end = true;
                            valid = true;
                        } else {
                            end = true;
                            choice = 3;
                        }
                    }
                }

                end = false;
                String possAuthors = "";
                try {
                    //Get the possible authors names
                    possAuthors = documentParser.getAuthors();
                } catch (IOException e) {
                    cView.displayErrorToScreen(e.getMessage());
                }
                while (!end) {
                    cView.displayInstruction("Are these the authors of the document:\n" + possAuthors + "" +
                            "\nPress Y to save this as the authors names, press N to manually input the names");

                    String ans;
                        if (isGUI) {
                            String[] options = new String[]{"Yes", "No"};
                            ans = cView.displayPopUp(options);
                        }
                        else {
                            ans = cView.getInput();
                        }


                    if (ans.equals("Y") || ans.equals("y") || ans.equals("Yes")) {
                        FileFormatter.addAuthors(possAuthors);
                        //Check if there are more than 1 authors
                        setYear(documentParser);
                        end = true;
                        valid = true;
                    } else {
                        end = true;
                        choice = 3;
                        valid = false;
                    }
                }


                try {
                    documentParser.close();
                } catch (IOException e) {
                    cView.displayErrorToScreen(e.getMessage());
                }

            } else if (choice == 3) {
                //Add title to the document
                boolean end = false;

                String title = null;
                while (!end) {
                    cView.displayInstruction("Please write the title of the document. You don't need to write more than the first 10 words. \n" +
                            "Ex: Apoptosis control by death and decoy receptors");
                    title = cView.getInput();
                    cView.displayInstruction("You typed: " + title + "" +
                            "\nAre you sure you want to use this title? (Y/N)");
                    String ans;
                    if (isGUI) {
                        String[] options = new String[]{"Yes", "No"};
                        ans = cView.displayPopUp(options);
                    }
                    else {
                        ans = cView.getInput();
                    }

                    if (ans.equals("Y") || ans.equals("y") || ans.equals("Yes")) {
                        end = true;
                    }
                }

                FileFormatter.addTitle(title);
                end = false;
                String authors = null;
                while (!end) {
                    cView.displayInstruction("Please write the names of the authors, as it appears on the paper, but without special" +
                            " symbols. Please include at least 3 authors." +
                            "\nIf there are more than 3, write only the first 3 in the EXACT order that they appear on the paper. SEPARATE them with ','" +
                            "\nPlease write the names in the following format: Author1Name Author1LastName, Author2Name Author2LastName" +
                            "\nEx: Elizabeth Slee, Mary Harte, Ruth Kluck");
                    authors = cView.getInput();
                    cView.displayToScreen("You typed: " + authors +
                            "\nAre you sure you want to use this? (Y/N)");
                    String ans;
                    if (isGUI) {
                        String[] options = new String[]{"Yes", "No"};
                        ans = cView.displayPopUp(options);
                    }
                    else {
                        ans = cView.getInput();
                    }


                    if (ans.equals("Y") || ans.equals("y") || ans.equals("Yes")) {
                        if (authors.split(",").length <= 1) {
                            cView.displayErrorToScreen("You only wrote one name. Please include more names or the year the paper was published");
                        } else {
                            authors = authorNamesValidator(authors);
                            end = true;

                        }
                    }
                }
                setYear(null);
                FileFormatter.addAuthors(authors);
                valid = true;
            }

        }
        return file;
    }

    //Ask for year the doc was published
    private void setYear(DocumentParser documentParser) {
            if (documentParser == null) {
                //Manually input info
                cView.displayInstruction("Please write the year the paper was published");
                String year;
                if (isGUI) {
                    waitForInput("numericalInput");
                    year = (String) submit.result;
                }
                else {
                    year = cView.getInput();
                }
                FileFormatter.addYear(year);



            }
            else {
                //Ask program to analyze the info
                cView.displayToScreen("Trying to find the year the paper was published...");
                cView.displayInstruction("Is this the year of the document? Press Y or N");


                String year = documentParser.getYear();
                cView.displayToScreen(year);
                String ans;
                if (isGUI) {
                    String[] options = new String[]{"Yes", "No"};
                    ans = cView.displayPopUp(options);
                }
                else {
                    ans = cView.getInput();
                }


                if (ans.equals("Y") || ans.equals("y") || ans.equals("Yes")) {
                    FileFormatter.addYear(year);
                }
                else {
                    //If it could not find year, then manually do it.
                    cView.displayInstruction("Please input the year");
                    if (isGUI) {
                        waitForInput("numericalInput");
                        year = (String) submit.result;
                    }
                    else {
                        year = cView.getInput();
                    }
                    FileFormatter.addYear(year);
                }
            }

    }


    private void outputToFile() {
        FileOutput output = new FileOutput();
        try {
            output.writeToFile(dataGathered);
            cView.displayToScreen("This is the information: ");
            output.readFile();

        } catch (IOException e) {
            cView.displayErrorToScreen("There was an error trying to open the file. Make sure the file exists. ");
        }
    }

    private void analyzeFiles() {

        //Error checking to enforce that user has completed step 1 and 2
        if (comparisonFiles == null) {
            cView.displayToScreen("You have not set the folder with the files that will be analyzed");
        } else if (twinFile1 == null || twinFile2 == null) {
            cView.displayToScreen("You have not set the twin files");
        } else {

            dataGathered = new TreeMap<>();


            //Goes through each document inside of the folder
            for (int i = 0; i < comparisonFiles.length; i++) {
                File curr = comparisonFiles[i];
                if (!curr.getName().equals(".DS_Store")) {
                    try {

                        DocumentParser parser = new DocumentParser(curr, true, false);

                        //For Twin1
                        FileFormatter.setFile(twinFile1);
                        //Retrieve the authors of the paper
                        String authorsNamesTwin1 = FileFormatter.getAuthors();
                        //Get a regex with all possible name combinations of the authors
                        String regexReadyTwin1 = generateReferenceRegex(authorsNamesTwin1, true, false);
                        //Get regex for just the main author (in case reference only contains his name)
                        String mainAuthorTwin1 = generateReferenceRegex(authorsNamesTwin1, false, false);
                        //Gets the citation for twin1 found in this paper
                        String citationTwin1 = "";
                        //Get the year the twin file was published
                        int yearPublished = FileFormatter.getYear();
                        try {
                            citationTwin1 = parser.getReference(regexReadyTwin1, authorsNamesTwin1, mainAuthorTwin1, yearPublished);
                        } catch (Exception e) {
                            cView.displayErrorToScreen(e.getMessage());
                        }
                        FileFormatter.closeFile();


                        //For Twin2
                        FileFormatter.setFile(twinFile2);
                        String authorsNamesTwin2 = FileFormatter.getAuthors();
                        String RegexReadyTwin2 = generateReferenceRegex(authorsNamesTwin2, true, false);
                        String mainAuthorTwin2 = generateReferenceRegex(authorsNamesTwin2, false, false);
                        String citationTwin2 = "";
                        int yearPublished2 = FileFormatter.getYear();
                        try {
                            citationTwin2 = parser.getReference(RegexReadyTwin2, authorsNamesTwin2, mainAuthorTwin2, yearPublished2);
                        } catch (Exception e) {
                            cView.displayErrorToScreen(e.getMessage());
                        }
                        FileFormatter.closeFile();

                        System.out.println(citationTwin1); //delete
                        System.out.println(citationTwin2);


                        //Number of times A and B are each cited
                        int xA = 0;
                        int xB = 0;
                        //Number of times A and B are cited together
                        int xC = 0;

                        if (citationTwin1.isEmpty() || citationTwin2.isEmpty()) {
                            //If one of the papers is not cited, do not process anything else
                            System.out.println("One of the papers is not cited so result is 0");

                        }

                        //Todo: case when both are emtpy

                        String testCitation = citationTwin1;

                        if (citationTwin1.isEmpty()) {
                            testCitation = citationTwin2;
                        }
                        //Check if the references are numbered
                        StringBuilder number = new StringBuilder();
                        for (Character c : testCitation.toCharArray()) {
                            if (c == '.') {
                                break;
                            }
                            number.append(c);

                        }
                        boolean areRefNumbered = false;
                        String referenceNumberOfTwin = number.toString();
                        try {
                            //The reference are number
                            Integer.parseInt(referenceNumberOfTwin);
                            areRefNumbered = true;
                        } catch (NumberFormatException e) {
                            //References are not numbered
                        }

                        //Gets all citations of curr doc
                        ArrayList<String> citationsCurrDoc = parser.getInTextCitations(areRefNumbered);


                        if (areRefNumbered) {
                            //Reference are in this format
                            //4. Stewart, John 2010.

                            //Can parse the following cases:
                            //Case 1: When in text citations are numbers between brackets
                            //Ex: [4, 5] or  [5]
                            //Case 2: When in text citations are numbers, but in the format of superscript
                            //Ex: word^(5,6)

                            StringBuilder number1 = new StringBuilder();
                            for (Character c : citationTwin1.toCharArray()) {
                                if (c == '.') {
                                    break;
                                }
                                number1.append(c);

                            }
                            String referenceNumberOfTwin1 = number1.toString();

                            StringBuilder number2 = new StringBuilder();
                            for (Character c : citationTwin2.toCharArray()) {
                                if (c == '.') {
                                    break;
                                }
                                number2.append(c);
                            }
                            String referenceNumberOfTwin2 = number2.toString();
                            System.out.println("Reference number of twin 1: " + referenceNumberOfTwin1);
                            System.out.println("Reference number of twin 2: " + referenceNumberOfTwin2);


                            String patter1S = "\\b" + referenceNumberOfTwin1 + "\\b";
                            String pattern2S = "\\b" + referenceNumberOfTwin2 + "\\b";

                            Pattern pattern1 = Pattern.compile(patter1S);
                            Pattern pattern2 = Pattern.compile(pattern2S);


                            //Get number
                            for (String citation : citationsCurrDoc) {
                                Matcher matcher1 = null;
                                if (!citationTwin1.isEmpty()) {
                                    matcher1 = pattern1.matcher(citation);
                                }
                                Matcher matcher2 = null;
                                if (!citationTwin2.isEmpty()) {
                                    matcher2 = pattern2.matcher(citation);
                                }

                                boolean aFound = false, bFound = false;

                                if (!citationTwin1.isEmpty() && matcher1.find()) {
                                    xA = xA + 1;
                                    aFound = true;
                                    System.out.println("-Valid");

                                }
                                if (!citationTwin2.isEmpty() && matcher2.find()) {
                                    xB = xB + 1;
                                    bFound = true;
                                    System.out.println("-Valid");

                                }

                                //If citation contains both twin files, then increase counter
                                if (aFound && bFound) {
                                    System.out.println("Twin citations found: " + citation);
                                    xC = xC + 1;
                                }
                            }

                        } else {
                            //Do it based on authors and year
                            //Gets the year that appears on the reference of the twin
                            String yearTwin1 = "";
                            if (!citationTwin1.isEmpty()) {
                                yearTwin1 = getYear(citationTwin1);
                            }
                            String yearTwin2 = "";
                            if (!citationTwin2.isEmpty()) {
                                yearTwin2 = getYear(citationTwin2);
                            }


                            //Generates a regex based online on the first author of the paper
                            String authorRegexTwin1 = generateReferenceRegex(authorsNamesTwin1, false, true);
                            String authorRegexTwin2 = generateReferenceRegex(authorsNamesTwin2, false, true);


                            for (String citation : citationsCurrDoc) {
                                boolean aFound = false, bFound = false;

                                //If citations matches pattern, return the pattern and compare the year
                                String containsCitationResult = "";
                                if (!citationTwin1.isEmpty()) {
                                    containsCitationResult = containsCitation(citation, authorRegexTwin1, authorsNamesTwin1);
                                }

                                if (!containsCitationResult.isEmpty() && containsYear(containsCitationResult, yearTwin1)) {
                                    xA = xA + 1;
                                    aFound = true;
                                    System.out.println("----Valid");
                                }

                                containsCitationResult = "";
                                if (!citationTwin2.isEmpty()) {
                                    containsCitationResult = containsCitation(citation, authorRegexTwin2, authorsNamesTwin2);
                                }
                                if (!containsCitationResult.isEmpty() && containsYear(containsCitationResult, yearTwin2)) {
                                    xB = xB + 1;
                                    bFound = true;
                                    System.out.println("----Valid");
                                }
                                if (aFound && bFound) {
                                    xC = xC + 1;
                                }
                            }


                        }

                        //Calculation for finding percentage
                        //rN=xC/[(xA+xB)/2]
                        double rN = 0.0;
                        if (xA + xB == 0) {
                            rN = 0;
                        } else {
                            rN = ((double) xC / ((double) (xA + xB) / 2.0)) * 100;
                        }
                        ArrayList<Object> list = new ArrayList<>();
                        list.add(curr.getName());
                        list.add((double) xA);
                        list.add((double) xB);
                        list.add((double) xC);
                        list.add(rN);
                        dataGathered.put(i, list);

                        cView.displayToScreen("RESULT: Document " + curr.getName() + " Cites both papers together " + rN + "%");
                        parser.close();
                        cView.displayToScreen("---------------------------------------------------");


                    } catch (IOException e) {
                        cView.displayErrorToScreen("ERROR: There was an error parsing document " + curr.getName());
                    }
                    System.out.println(dataGathered);
                }
            }

        }

    }

    //Todo: modify this

    String containsCitation(String citation, String authorRegex, String authorNamesTwin) {

        String pattern = (authorRegex) + "([^;)])*((\\b((18|19|20)\\d{2}([A-z])*(,( )?(((18|19|20)\\d{2}([A-z])*)|[A-z]))*)\\b)|unpublished data|data not shown)";
        pattern = (authorRegex) + "([^;)])*((\\b((18|19|20)\\d{2}([A-z])*((,|( and))( )?(((18|19|20)\\d{2}([A-z])*)|[A-z]))*)\\b)|unpublished data|data not shown)";

        Pattern pattern1 = Pattern.compile(pattern);
        Matcher matcher = pattern1.matcher(citation);
        ArrayList<String> results = new ArrayList<>();

        while (matcher.find()) {
            String answer = matcher.group();
            System.out.println("Found the following citation " + answer);
            results.add(answer);
        }
        if (results.size() == 1) {
            return results.get(0);
        } else if (results.size() > 1) {
            System.out.println("Citation with tie " + citation);
            return solveReferenceTies(results, authorNamesTwin).get(0);
        } else {
            return "";
        }
    }


    //Based on Levenshtein Distance, uses the authors names to find the most similar citation
    private ArrayList<String> solveReferenceTies(ArrayList<String> result, String authors) {
        ArrayList<String> newResult = new ArrayList<>();
        int smallest = Integer.MAX_VALUE;

        for (String s : result) {

            int newDistance = StringUtils.getLevenshteinDistance(s, authors);
            if (newDistance == smallest) {
                //Do it based on the first authors name only
                cView.displayErrorToScreen("There was an error finding one of the citations, please report to developer");
            }
            if (newDistance < smallest) {
                smallest = newDistance;
                newResult.add(0, s);
            }
        }
        return newResult;
    }


    boolean containsYear(String citation, String year) {
        StringBuilder yearNumber = new StringBuilder();
        StringBuilder yearLetter = new StringBuilder();
        try {
            String yearNumber1 = String.valueOf(Integer.valueOf(year));
            yearNumber.append(yearNumber1);
        } catch (NumberFormatException e) {
            //If year contains a letter, or is unpublished data or data not shown
            if (year.equals("unpublished data")) {
                yearNumber.append("0000");
            } else if (year.equals("data not shown")) {
                yearNumber.append("0000");
            } else {
                boolean parsingNumber = true;
                for (Character c : year.toCharArray()) {

                    if (Character.isDigit(c) && parsingNumber) {
                        yearNumber.append(c);
                    } else {
                        parsingNumber = false;
                        if (c != ' ') {
                            yearLetter = yearLetter.append(c);
                        }
                    }

                }

            }
        }
        String yearString = yearNumber.toString();
        String letterString = yearLetter.toString();
        String yearAndLetterString = yearString + letterString;

        String pattern;

        //If there is no letter, do this
        if (letterString.isEmpty()) {
            pattern = "((\\b" + yearAndLetterString + "\\b)|(unpublished data)|(data not shown))";
        } else {
            pattern = "((\\b" + yearAndLetterString + "\\b)|(\\b(" + yearString + "( )?([A-z]=?[^" + letterString + "])*((,( )?([A-z]=?[^" + letterString + "])*)*(( )?)" + letterString + ")+)\\b)|(unpublished data)|(data not shown))";
        }
        Pattern yearPattern = Pattern.compile(pattern);
        Matcher matcher = yearPattern.matcher(citation);
        if (matcher.find()) {
            return true;
        }
        return false;
    }


    private String getYear(String citationTwin1) {
        String pattern = "(\\b((18|19|20)\\d{2}([A-z])*(,( )?(((18|19|20)\\d{2}([A-z])*)|[A-z]))*)\\b)|unpublished data|data not shown";
        Pattern yearPattern = Pattern.compile(pattern);
        Matcher matcher = yearPattern.matcher(citationTwin1);
        String answer = "";
        while (matcher.find()) {
            //Year if the first 4 digit number to appear
            answer = matcher.group();
            break;
        }
        return answer;
    }

    protected String generateReferenceRegex(String authors, boolean usesAnd, boolean isInText) {
        //Generates all possible references that could be found in a bibliography based on authors names
        //Ex: Xu Luo, X Luo, X. Luo, Luo X. Luo X
        //Splits string by authors names

        List<String> holder = Arrays.asList(authors.split("\\s*,\\s*"));
        ArrayList<String> authorsNames = new ArrayList<>(holder);
        for (int i = 0; i < authorsNames.size(); i++) {
            //Remove single characters like A. or X. or L because it can make the regex produce the wrong result
            String author = authorsNames.get(i);
            author = author.replaceAll("( )?[A-z](\\.)( )?", " ");
            author = author.replaceAll("^[ \\t]+|[ \\t]+$", "");
            authorsNames.remove(i);
            if (!author.isEmpty()) {
                authorsNames.add(i, author);
            }
        }
        int authorCounter = 0;
        //Do it based on only the first author's name IMPORTANT ASSUMPTION
        if (!usesAnd) {
            String temp = authorsNames.get(0);
            authorsNames = new ArrayList<>();
            authorsNames.add(temp);
        }

        StringBuilder authorsRegex = new StringBuilder();
        for (String currAuthor : authorsNames) {
            if (authorCounter == 0 && !usesAnd) {
                authorsRegex.append("(");
            }
            if ((authorsNames.size() > 1) && (authorCounter < authorsNames.size())) {
                if (usesAnd) {
                    authorsRegex.append("(?=.*");
                } else {
                    if (authorCounter > 0) {
                        authorsRegex.append("|(");
                    } else {
                        //if it is starting parenthesis
                        authorsRegex.append("(");

                    }
                }
            }
            if (authorsNames.size() == 1) {
                authorsRegex.append("(");
            }
            String[] splited = currAuthor.split("\\s+");
            StringBuilder possibleCombinations = new StringBuilder();

            possibleCombinations.append("(");
            for (int i = 0; i < splited.length; i++) {
                if (i > 0 && i < splited.length) {
                    possibleCombinations.append('|');
                }
                StringBuilder boundaries = new StringBuilder();
                boundaries.append("\\b").append(splited[i]).append("\\b");
                possibleCombinations.append(boundaries.toString());
            }
            possibleCombinations.append("))");
            authorsRegex.append(possibleCombinations.toString());


            authorCounter++;
        }
        if (!usesAnd) {
            authorsRegex.append(")");

        }

        if (usesAnd || isInText) {
            return authorsRegex.toString();
        } else {
            //In case we are getting the reference, but we only need one author, them  we search using and for both names of the same author
            String author = authorsRegex.toString();
            author = author.replaceAll("\\(", "");
            author = author.replaceAll("\\)", "");
            String[] mainAuthorName = author.split("\\|");
            //If there is only one author return
            if (mainAuthorName.length == 1) {
                return author;
            }
            StringBuilder newAuthorName = new StringBuilder();
            int counter = 0;
            for (String s : mainAuthorName) {
                if (!s.isEmpty()) {
                    if (counter == 0) {
                        newAuthorName.append("(?=.*").append("(").append(s).append(")").append(")?");
                    } else {
                        newAuthorName.append("(?=.*").append("(").append(s).append(")").append(")");
                    }
                    counter++;
                }
            }
            return newAuthorName.toString();

        }

    }

    private void setComparisonFiles() {
        cView.displayToScreen("Make sure that all the files are in the same folder");
        cView.displayToScreen("Please write the name of the folder");
        comparisonFiles = cView.getFiles();
    }




    private String authorNamesValidator(String ans) {
        ans = ans.replaceAll("[\\n\\r]", "");
        ans = ans.replaceAll("[^A-z\\s-.,]", "");
        ans = ans.replaceAll("^[ \\t]+|[ \\t]+$", "");
        ans = ans.replaceAll(",\\.", "");
        ans = ans.replaceAll(" and\\b", "");
        while (ans.endsWith(",")) {
            ans = ans.substring(0, ans.lastIndexOf(","));
        }
        while (ans.endsWith(".")) {
            ans = ans.substring(0, ans.lastIndexOf("."));
        }
        return ans;
    }

    @Override
    protected void update() {

    }

    private void waitForInput (String type) {
        if (isGUI) {
            submit = new SubmitListener(type);
            cView.addSubmitListener(submit);
            //wait until user input
            synchronized (this) {
                // wait for button press
                while (holder.isEmpty()) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }





//
//    private void simulateTournament() {
//        cView.displayToScreen("Please enter name of output file, or write  N/A"
//                + " to display everything on the screen");
//        String input = cView.getInput();
//        log.newLine();
//        log.writeToLogFile("Input: " + input);
//        cView.displayToScreen("How do you want to simulate tournament?");
//        int choice = cView.getUserChoice();
//        log.newLine();
//        log.writeToLogFile("Input: " + Integer.toString(choice));
//        // Games are both observers and subjects
//        boolean isValid = false;
//        if (choice == 1) {
//            setGetWinnerMethod(new CoinFlipStrategy());
//            isValid = true;
//        } else if (choice == 2) {
//            setGetWinnerMethod(new EloScoreStrategy(cModel));
//            isValid = true;
//        } else if (choice == 3) {
//            setGetWinnerMethod(new FavoriteWinsStrategy(cModel));
//            isValid = true;
//        } else {
//            cView.displayToScreen("Invalid Selection");
//        }
//        // If valid selection go here
//        if (isValid) {
//            parseGameFile(new File(cView.getGamesFile()));
//            if (!input.equals("N/A")) {
//                log.addOutputFile(input);
//            }
//
//            for (int i = 1; i <= cModel.getNumberOfGames(); i++) {
//                Game currGame = cModel.getGame(i);
//                if (!input.equals("N/A")) {
//                    log.writeToOutputFile(currGame.getGameNumber() + "," +
//                            currGame.getState() + '\n');
//                } else {
//                    cView.displayToScreen(currGame.getGameNumber() + " " +
//                            currGame.getState());
//                }
//                log.newLine();
//                log.writeToLogFile("Game " + currGame.getGameNumber() + ": " +
//                        currGame.getState() + " won");
//            }
//        }
//    }
//


    /**
     * Listens to a button press event on the submit button located in the view class
     */
    class SubmitListener implements ActionListener, Runnable {
        String type;
        Object result;
        private String[] options;

        public SubmitListener(String type) {
             this.type = type;
        }

        public void actionPerformed(ActionEvent e) {
            holder = "";
            if (type.equals("getFile")) {

                    try {
                        result = cView.getFile();
                        synchronized (Controller.this) {
                            holder = "userHasInputSMT";
                            Controller.this.notify();
                        }
                    } catch (IllegalArgumentException error) {
                        cView.displayErrorToScreen(error.getMessage());
                    }

            }
            else if (type.equals("numericalInput")) {
                try {
                    result = cView.getInput();
                    try {
                        Integer.valueOf((String) result);
                        //If it is a number, then notify
                        synchronized (Controller.this) {
                            holder = "userHasInputSMT";
                            Controller.this.notify();
                        }
                    } catch (NumberFormatException error) {
                            cView.displayErrorToScreen("Please write only numbers");
                        }
                } catch (IllegalArgumentException error) {
                    cView.displayErrorToScreen(error.getMessage());
                }
            }

        }
        Object getResult() {
            return result;
        }



        @Override
        public void run() {

        }
    }



}
