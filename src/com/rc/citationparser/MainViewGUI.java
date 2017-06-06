package com.rc.citationparser;

import com.sun.javaws.exceptions.InvalidArgumentException;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Created by rafaelcastro on 6/4/17.
 */
public class MainViewGUI implements ViewInterface {
    private JPanel mainPanel;
    private JPanel topPanel;
    private JPanel centerPanel;
    private JTextField input;
    private JPanel top;
    private JTextArea instructions;
    private JTextArea outputTextArea;
    private JButton a1SetTwinsButton;
    private JButton a2SetFolderToButton;
    private JButton a3AnalyzeDataButton;
    private JButton a4OutputResultButton;
    private JProgressBar progressBar1;
    private JButton submit;
    private JButton a5ExitButton;
    private int userChoice;

    public MainViewGUI() {
        JFrame frame = new JFrame("TwinFinder");
        frame.setContentPane(mainPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        submit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        a1SetTwinsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                userChoice = 1;
            }
        });
        a2SetFolderToButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                userChoice = 2;

            }
        });
        a3AnalyzeDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                userChoice = 3;
            }
        });
        a4OutputResultButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                userChoice = 4;
            }
        });
        a5ExitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
    }

    @Override
    public File getFile() throws IllegalArgumentException {

        String fileName = input.getText();
        File file = new File(fileName);
        if (fileName.isEmpty()) {
            throw new IllegalArgumentException("Please write the name of the file");
        }
        else if (!fileName.isEmpty()) {

            file = new File(fileName);
            if (!file.exists()) {
                throw new IllegalArgumentException("File does not exist");
            } else if (!file.canRead()) {
                throw new IllegalArgumentException("File cannot be read");
            }
        }
        return file;
    }

    @Override
    public File[] getFiles() throws IllegalArgumentException {
        File[] list = null;
        String fileName = input.getText();
        if (fileName.isEmpty()) {
            throw new IllegalArgumentException("Please write the name of the file");
        }
        else if (!fileName.isEmpty()) {
            File folder = new File(fileName);
            if (!folder.exists()) {
                throw new IllegalArgumentException("Folder does not exist");
                    } else if (!folder.canRead()) {
                throw new IllegalArgumentException("Folder cannot be read");
                    } else {
                        File[] listOfFiles = folder.listFiles();
                        if (listOfFiles == null) {
                            throw new IllegalArgumentException("The file need to be inside of a folder");
                        }
                        else if  (listOfFiles.length < 1) {
                            throw new IllegalArgumentException("There are no files in this folder");
                        } else {
                            for (File curr : listOfFiles) {
                                if (!curr.getName().equals(".DS_Store")) {
                                    if (!curr.exists() || !curr.canRead()) {
                                        throw new IllegalArgumentException(curr.getName() + " is not a valid file");
                                    } else {
                                        list = listOfFiles;
                                    }
                                }
                            }
                        }
                    }

            }
        return list;
    }

    @Override
    public int getUserChoice() throws IllegalArgumentException {
        return userChoice;
    }

    @Override
    public void displayToScreen(String s) {
        StringBuilder sb = new StringBuilder();
        sb.append(s).append("\n").append(outputTextArea.getText());
        outputTextArea.setText(sb.toString());
    }

    @Override
    public void invalidChoice() {
        displayErrorToScreen("Invalid Input");

    }

    @Override
    public String getInput() throws IllegalArgumentException{
        String nInput = input.getText();
        if (nInput.isEmpty()) {
            throw new IllegalArgumentException("Please type the year");
        }
        return input.getText();
    }

    @Override
    public String getLogFile() {
        return input.getText();
    }

    /**
     * Display error as pop up message
     *
     * @param s String with error message
     */
    @Override
    public void displayErrorToScreen(String s) {
        JOptionPane.showMessageDialog(null, s, "Error",
                JOptionPane.ERROR_MESSAGE);
    }


    @Override
    public String displayPopUp(String[] options) {
        JOptionPane jop = new JOptionPane(null, JOptionPane.PLAIN_MESSAGE,
                JOptionPane.YES_NO_OPTION, null, options, options[2]);
        JDialog dialog = jop.createDialog(null, "Please select an option");
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        dialog.setVisible(true);
        dialog.dispose();

        return (String) jop.getValue();

    }




    public void displayInstruction(String s) {
        instructions.setText(s);
    }


    /**
     * Adds a listener to the submit button
     *
     * @param pressed ActionListener
     */
    public void addSubmitListener(ActionListener pressed) {
        submit.addActionListener(pressed);
    }

    /**
     * Adds a listener to the submit button
     * @param pressed ActionListener
     */
    void removeSubmitListener(ActionListener pressed) {
        submit.removeActionListener(pressed);
    }
}


