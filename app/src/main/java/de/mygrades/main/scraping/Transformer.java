package de.mygrades.main.scraping;

import android.provider.MediaStore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import javax.xml.xpath.XPathExpressionException;

import de.mygrades.database.dao.TransformerMapping;
import de.mygrades.util.exceptions.ParseException;

/**
 * Created by Jonas on 22.09.2015.
 */
public class Transformer {
    public static String table = "";

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public static String getStringFromFile (String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
    }


    public static void main(String args[]) {
        try {
            table = getStringFromFile("C:\\Users\\Jonas\\Desktop\\mygrades\\grades-app\\app\\src\\main\\java\\de\\mygrades\\main\\scraping\\table.txt");
            //System.out.print(table);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Parser parser = null;
        try {
            parser = new Parser();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        try {
            parser.testIterator(table);
        } catch (ParseException | XPathExpressionException e) {
            e.printStackTrace();
        }
    }

    /**
     * Parser to extract values into Models.
     */
    private Parser parser;

    private String xml;

    private TransformerMapping transformerMapping;

    public Transformer(String xml, Parser parser) {
        this.parser = parser;
        this.xml = xml;
    }


    public void transform() {
        // get List to iterate through and respectively extract grade values

    }

}
