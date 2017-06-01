package com.rc.citationparser;

import org.junit.jupiter.api.Test;

import javax.print.Doc;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by rafaelcastro on 5/30/17.
 */
class ControllerTest {
    @Test
    void containsCitation() throws IOException {
        Controller controller = new Controller(new View(false), false);
        String authors = "Kerr, Searle";
        String regex = controller.generateReferenceRegex(authors, false);
        String ans = controller.containsCitation("(Kerr and Searle, 1972a and b)", regex, authors);

        assertTrue(!ans.isEmpty());

        authors = "Kerr, Searle";
        regex = controller.generateReferenceRegex(authors, false);
        ans = controller.containsCitation("(Kerr and Searle, 1972a and b)", regex, authors);

        assertTrue(!ans.isEmpty());

    }

    @Test
    void generateReferenceRegex() {
    }

    @Test
    void containsYear() {

        Controller controller = new Controller(new View(false), false);
        String authors = "Kerr, Searle";

        String regex = controller.generateReferenceRegex(authors, false);
        String ans = controller.containsCitation("(Kerr and Searle, 1972a and b)", regex, authors);

        assertTrue(controller.containsYear(ans, "1972a" ));
        assertTrue(controller.containsYear(ans, "1972b" ));
    }





}