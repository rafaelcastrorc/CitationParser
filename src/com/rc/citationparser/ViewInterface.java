package com.rc.citationparser;

import java.awt.event.ActionListener;
import java.io.File;


/**
 * Created by rafaelcastro on 6/6/17.
 */
interface ViewInterface {

    /**
     * Returns a valid file based on user input. The method checks for any possible errors
     *
     * @return a file
     */
    File getFile();

    /**
     * Gets all files inside of a folder. Checks for any errors.
     *
     * @return a File[] with all the files inside the folder
     */
    File[] getFiles();


    /**
     * Used when the user needs to input a number
     * Retrieves a user input can only be a number >= 0
     *
     * @return an int based on the user input
     */
    int getUserChoice();

    /**
     * Displays a string s in the View
     *
     * @param s - String that needs to be printed
     */
    void displayToScreen(String s);

    /**
     * Displays a question as a pop up
     *
     * @param options - array with the possible options
     */

    String displayPopUp(String[] options);

    /**
     * Displays a string s in the View, as an instruction
     *
     * @param s - String that needs to be displayed
     */
    void displayInstruction(String s);

    /**
     * Use when the user makes an invalid choice.
     * Print error
     */
    void invalidChoice();

    /**
     * Retrieves the string input of the user
     *
     * @return a string with the input
     */
    String getInput();

    /**
     * Returns the name of the log file that the user wants to use
     *
     * @return the name
     */
    String getLogFile();

    /**
     * Prints an error into the comand line
     *
     * @param s - error that needs to be printed
     */
    void displayErrorToScreen(String s);


    //todo:
    void addSubmitListener(ActionListener pressed);
}

