package com.rc.citationparser;

import javax.swing.text.Document;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by rafaelcastro on 5/16/17.
 */

class Controller {
    private final Logger log;
    private View cView;
    private File twinFile1;
    private File twinFile2;
    private File[] comparisonFiles;


    Controller(View cView) {
        this.cView = cView;
        this.log = Logger.getInstance();
        startProgram();
    }

    private void displayOptions() {
        cView.displayToScreen("Please select an option:");
        cView.displayToScreen("Press 1 to set to twin files");
        cView.displayToScreen("Press 2 to set the folder that contains the files that will be analyzed");
        cView.displayToScreen("Press 3 to check the number of files that cite both");
        cView.displayToScreen("Press 4 to see the instructions");
        cView.displayToScreen("Press 5 to Exit");
        ///        log.addLogFile(cView.getLogFile());

    }

    protected void startProgram() {
        displayOptions();
        int choice = cView.getUserChoice();
        log.writeToLogFile("Input: " + Integer.toString(choice));
        boolean end = false;
        while (!end) {

            if (choice == 1) {
                setTwinFiles();
                cView.displayToScreen("Twin files have been set.");

            } else if (choice == 2) {
                setComparisonFiles();
                cView.displayToScreen("The folder has been set.");


            } else if (choice == 3) {
                analyzeFiles();

            } else if (choice == 4) {

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

    private void analyzeFiles() {
        if (comparisonFiles == null) {
            cView.displayToScreen("You have not set the folder with the files that will be analyzed");
        } else if (twinFile1 == null || twinFile2 == null) {
            cView.displayToScreen("You have not set the twin files");
        } else {
            HashMap<String, Integer> nameOfDocToText = new HashMap<>();

            //Parse every document, convert it to text. Map doc name to text
            for (int i = 0; i < comparisonFiles.length; i++) {
                File curr = comparisonFiles[i];
                if (!curr.getName().equals(".DS_Store")) {
                    try {
                        DocumentParser parser = new DocumentParser(curr);
                        String docText = parser.getText();
                        ArrayList<String> citationsCurrDoc = parser.getInTextCitations();

                        //For Twin1
                        FileFormatter.setFile(twinFile1);
                        String authorsRegexReady = generateReference(FileFormatter.getAuthors());
                        String citationTwin1 = parser.getReference(authorsRegexReady);
                        FileFormatter.closeFile();

                        //For Twin2
                        FileFormatter.setFile(twinFile2);
                        authorsRegexReady = generateReference(FileFormatter.getAuthors());
                        String citationTwin2 = parser.getReference(authorsRegexReady);
                        FileFormatter.closeFile();
                        //If one of the files is not referenced at all, return 0
                        if (citationTwin1.isEmpty() || citationTwin2.isEmpty()) {
                            System.out.println("One of the papers is not cited so result is 0");
                            nameOfDocToText.put(curr.getName(), 0);

                        } else {

                            //Case 1: When in text citations are numbers
                            //Ex: [4, 5] or  [5]
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
                            System.out.println(referenceNumberOfTwin1);
                            System.out.println(referenceNumberOfTwin2);

                            int counter = 0;

                            //Get number
                            for (String citation : citationsCurrDoc) {
                                if (citation.contains(referenceNumberOfTwin1) && citation.contains(referenceNumberOfTwin2)) {
                                    counter++;
                                }
                            }
                            //Adds to map
                            nameOfDocToText.put(curr.getName(), counter);

                            System.out.println(counter);
                            parser.close();
                        }

                    } catch (IOException e) {
                        cView.displayToScreen("There was an error parsing document " + curr.getName());
                    }

                }
            }


            //Check type of citation format used

            //Check how many times the couple appears.

        }

    }

    private String generateReference(String authors) {
        //Generates all possible references that could be found in a bibliography based on authors names
        //Ex: Xu Luo, X Luo, X. Luo, Luo X. Luo X
        //Splits string by authors names
        List<String> authorsNames = Arrays.asList(authors.split("\\s*,\\s*"));
        int authorCounter = 0;

        StringBuilder authorsRegex = new StringBuilder();
        for (String currAuthor : authorsNames) {
            if ((authorsNames.size() > 1) && (authorCounter < authorsNames.size())) {
                authorsRegex.append("(?=.*");
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
                possibleCombinations.append(splited[i]);
            }
            possibleCombinations.append("))");
            authorsRegex.append(possibleCombinations.toString());


            authorCounter++;
        }
        return authorsRegex.toString();

    }

    private void setComparisonFiles() {
        cView.displayToScreen("Make sure that all the files are in the same folder");
        cView.displayToScreen("Please write the name of the folder");
        comparisonFiles = cView.getFiles();
    }


    //Ask user to write name of paper, authors names and possibly year it was published
    private void setTwinFiles() {
        log.newLine();
        cView.displayToScreen("Not all pdf files are formatted the same. The program will try to " +
                "get the necessary data, but you will have to verify its accuracy");

        //For first tween file
        cView.displayToScreen("Please type the name of the first twin file. Ex: doc1.pdf");
        File file1 = cView.getFile();
        log.writeToLogFile("File 1 name " + file1.getName());
        log.newLine();
        twinFile1 = setTwinFileHelper(file1, 1);
        FileFormatter.closeFile();

        //For file 2
        cView.displayToScreen("Please type the name of the second twin file. Ex: doc2.pdf");
        File file2 = cView.getFile();
        log.writeToLogFile("File 2 name " + file2.getName());
        log.newLine();
        twinFile2 = setTwinFileHelper(file2, 2);
        FileFormatter.closeFile();
    }

    private File setTwinFileHelper(File file, int num) {
        FileFormatter.setFile(file);

        cView.displayToScreen(FileFormatter.getCurrentInfo());
        cView.displayToScreen("If the information is correct press 1. \nIf you want the program " +
                "to try to get the right information press 2. \nTo write the information manually press 3.");
        int choice = cView.getUserChoice();
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
                cView.displayToScreen("Not yet implemented");
                valid = true;
                //Todo: Implement this feature

            } else if (choice == 3) {
                //Add title to the document
                boolean end = false;

                String title = null;
                while (!end) {
                    cView.displayToScreen("Please write the exact title of the document. \n" +
                            "Ex: Apoptosis control by death and decoy receptors");
                    title = cView.getInput();
                    cView.displayToScreen("You typed: " + title);
                    cView.displayToScreen("Are you sure you want to use this title? (Y/N)");
                    String ans = cView.getInput();

                    if (ans.equals("Y") || ans.equals("y")) {
                        end = true;
                    }
                }

                FileFormatter.addTitle(title);
                end = false;
                String authors = null;
                while (!end) {
                    cView.displayToScreen("Please write the name of the authors,  as it appears on the paper, but without special" +
                            " symbols." +
                            "\nIf there are more than 3, write only the first 3. Please separate them with ','" +
                            "\nPlease write the names in the following format: Name LastName");
                    cView.displayToScreen("Ex: Elizabeth Slee, MaryHarte, Ruth M. Kluck");
                    authors = cView.getInput();
                    cView.displayToScreen("You typed: " + authors);
                    cView.displayToScreen("Are you sure you want to use this? (Y/N)");
                    String ans = cView.getInput();

                    if (ans.equals("Y") || ans.equals("y")) {
                        end = true;
                    }
                }
                FileFormatter.addAuthors(authors);
                valid = true;
            }

        }
        return file;
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


//
//    private void getWinnerMethod(Game game) {
//        strategy.getWinnerMethod(game);
//    }
//
//    private void setGetWinnerMethod(MethodStrategy strategy) {
//        this.strategy = strategy;
//    }

}
