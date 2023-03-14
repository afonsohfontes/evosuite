package org.evosuite.coverage.privateMethod;

//import java.io.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class fileWriter {

    public fileWriter(String what, String where) throws IOException {
        write(what,where);
    }

    public void write(String what, String where) throws IOException {

        //creating the instance of file
        File path = new File(where);

        //passing file instance in filewriter
        FileWriter wr = new FileWriter(path);

        //calling writer.write() method with the string
        wr.write(what);

        //flushing the writer
        wr.flush();

        //closing the writer
        wr.close();
    }

}
