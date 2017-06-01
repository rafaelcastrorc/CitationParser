package com.rc.citationparser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

public class Main {

    public static void main(String[] args) {
        View view = new View(true);
        Controller controller = new Controller(view, true);
    }
}
