package com.rc.citationparser;

import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
//        try {
//            DocumentParser dp = new DocumentParser(new File("3.pdf"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        View view = new View();
       Controller controller = new Controller(view);
    }
}
