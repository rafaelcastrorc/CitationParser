package com.rc.citationparser;

import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
       View view = new View();
       Controller controller = new Controller(view);
    }
}
