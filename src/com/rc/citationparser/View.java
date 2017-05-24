package com.rc.citationparser;

import java.io.File;
 import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Created by rafaelcastro on 5/15/17.
 * The view is an output representation of the application.
 */
class View {

    private final Scanner scanner;

    public View() {
        scanner = new Scanner(System.in);
        System.out.println("Welcome!");
    }


    /**
     * Returns a valid file based on user input. The method checks for any possible errors
     * @return a file
     */
    protected File getFile() {
        boolean isValid = false;
        File file = null;
        while (!isValid) {
            if (scanner.hasNextLine()) {
                String fileName = scanner.nextLine();
                if (!fileName.isEmpty()) {

                    if (fileName == null) {
                        System.out.println("File name is not valid. Please write the correct file name.");
                    } else {
                        file = new File(fileName);
                        if (!file.exists()) {
                            System.out.println("File does not exist");
                        } else if (!file.canRead()) {
                            System.out.println("File cannot be read");
                        } else {
                            isValid = true;
                        }
                    }
            }
            }
        }
        return file;
    }

    /**
     * Gets all files inside of a folder. Checks for any errors.
     * @return a File[] with all the files inside the folder
     */
    protected File[] getFiles() {
        boolean isValid = false;
        File[] list = null;
        while (!isValid) {
            String fileName = scanner.nextLine();
            if (!fileName.isEmpty()) {
                if (fileName == null) {
                    System.out.println("Folder name is not valid. Please write the correct folder name.");
                } else {
                    File folder = new File(fileName);
                    if (!folder.exists()) {
                        System.out.println("Folder does not exist");
                    } else if (!folder.canRead()) {
                        System.out.println("Folder cannot be read");
                    } else {
                        File[] listOfFiles = folder.listFiles();
                        if (listOfFiles == null) {
                            System.out.println("The file need to be inside of a folder");
                        }
                        else if  (listOfFiles.length < 1) {
                            System.out.println("There are no files in this folder");
                        } else {
                            for (int i = 0; i < listOfFiles.length; i++) {
                                File curr = listOfFiles[i];
                                if (!curr.getName().equals(".DS_Store")) {
                                    if (!curr.exists() || !curr.canRead()) {
                                        System.out.println(curr.getName() + " is not a valid file");
                                        break;
                                    } else {
                                        list = listOfFiles;
                                        isValid = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return list;
    }


    /**
     * Used when the user needs to input a number
     * Retrieves a user input can only be a number >= 0
     * @return an int based on the user input
     */
    protected int getUserChoice() {
        int choice = 0;
        boolean valid = false;
        while (!valid) {
            try {
                choice = scanner.nextInt();
                if (choice > 0) {
                    valid = true;
                }
            } catch (InputMismatchException e) {
                invalidChoice();
                scanner.nextLine();
            }
        }
        return choice;
    }

    /**
     * Prints a string into the comand line
     * @param s - String that needs to be printed
     */
    protected void displayToScreen(String s) {
        System.out.println(s);
    }

    /**
     * Use when the user makes an invalid choice.
     */
    protected void invalidChoice() {
        System.out.println("Invalid input");

    }

    /**
     * Retrieves the input of the user
     * @return a string with the input
     */
    protected String getInput() {
        boolean done = false;
        String input = null;
        while (!done) {
            if (scanner.hasNextLine()) {
                input = scanner.nextLine();
                if (!input.isEmpty()) {
                    done = true;
                }
            }
        }
        return input;

    }

    /**
     * Returns the name of the log file that the user wants to use
     * @return the name
     */
    public String getLogFile() {
        boolean isValid = false;
        String logFile = null;
        while (isValid) {
            logFile = scanner.nextLine();
            if (logFile != null && !logFile.isEmpty()) {
                isValid = true;
            }
        }
        return logFile;
    }

    /**
     * Prints an error into the comand line
     * @param s - error that needs to be printed
     */
    public void displayErrorToScreen(String s) {
        System.err.println(s);
    }
}
