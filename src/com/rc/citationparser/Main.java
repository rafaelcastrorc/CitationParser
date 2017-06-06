package com.rc.citationparser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

public class Main {

    public static void main(String[] args) {
        MainViewGUI viewGUI = new MainViewGUI();
        View viewCommandLine = new View(true);
       // Controller controller = new Controller(viewGUI, true, true);
        Controller controller = new Controller(viewCommandLine, true, false);

    }
}
