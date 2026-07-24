package uk.ndc.csa.utilities.common;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class PDFHelper {


    public static PDDocument getDoc(String path) {
        PDDocument doc = null;
        try {
            doc = Loader.loadPDF(new File(path));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return doc;
    }

    public static PDDocument getDoc(byte[] contents) {
        PDDocument doc = null;
        try {
            doc = Loader.loadPDF(contents);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return doc;
    }

    public static PDPage getPDFPage(PDDocument doc, int num) {
        return doc.getPage(num);
    }

    public static PDPageTree getPDFPages(PDDocument doc) {
        return doc.getPages();
    }

    public static String getPDFText(PDDocument doc) {
        String extractText = null;
        try {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            extractText = pdfStripper.getText(doc);
        } catch (IOException e) {
            e.printStackTrace();
            extractText = "error: extract not posible";
        }

        return extractText;
    }

    public static 	String getPDFText(PDDocument doc, int startPage, int...endPage) {
        String extractText = null;
        try {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            pdfStripper.setStartPage(startPage);
            if (endPage.length>0) {
                pdfStripper.setEndPage(endPage[0]);
            }else {
                pdfStripper.setEndPage(startPage);
            }
            extractText = pdfStripper.getText(doc);
        } catch (IOException e) {
            e.printStackTrace();
            extractText = "error: extract not posible";
        }

        return extractText;
    }

    public static String[] getPDFText(PDDocument doc, String token, int startPage, int... endPage) {
        String extractText = null;

        try {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            pdfStripper.setSortByPosition( true );
            pdfStripper.setStartPage(startPage);
            if (endPage.length > 0) {
                pdfStripper.setEndPage(endPage[0]);
            } else {
                pdfStripper.setEndPage(startPage);
            }

            extractText = pdfStripper.getText(doc);
        } catch (IOException var6) {
            var6.printStackTrace();
            extractText = "error: extract not posible";
        }

        return extractText.split(token);
    }

    public static String getPDFText(PDDocument doc, int pageNum, Rectangle rectangle) {
        String extractText = null;
        try {
            PDPage page = doc.getPage(pageNum-1);
            PDFTextStripperByArea pdfStripper = new PDFTextStripperByArea();
            pdfStripper.setSortByPosition( true );
            pdfStripper.addRegion( "area", rectangle );
            pdfStripper.extractRegions( page );
            extractText = pdfStripper.getTextForRegion("area");
        } catch (IOException e) {
            e.printStackTrace();
            extractText = "error: extract not posible";
        }

        return extractText;
    }

    public static String scanFile(String pathname) throws IOException {

        File file = new File(pathname);
        StringBuilder fileContents = new StringBuilder((int)file.length());
        Scanner scanner = new Scanner(file);
        String lineSeparator = System.getProperty("line.separator");

        try {
            while(scanner.hasNextLine()) {
                fileContents.append(scanner.nextLine() + lineSeparator);
            }
            return fileContents.toString().trim();
        } finally {
            scanner.close();
        }
    }



}
