/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.xml;

import org.apache.commons.io.FileUtils;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

/**
 * Reads and writes the contents of a file.
 * @author Katsuhisa Maruyama
 */
public class XmlFileManager {
    
    private static String DEFALUT_CHARSET = "UTF-8";
    
    /**
     * Reads and returns the contents of a file.
     * @param filename the name of the file to be read
     * @return the contents of the file, or <code>null</code> if reading the file fails
     */
    public static String read(String filename) {
        File file = new File(filename);
        try {
            return FileUtils.readFileToString(file);
            
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }
    
    /**
     * Reads the contents of a file and returns the DOM instance created from them.
     * @param filename the name of the file to be read
     * @return the created DOM instance, or <code>null</code> if reading the XML file fails
     */
    public static Document readXML(final String filename) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringElementContentWhitespace(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            builder.setErrorHandler(new ErrorHandler() {
                
                /**
                 * Receives a recoverable error.
                 * @param exception the error information
                 * @exception possibly wrapping another exception.
                 */
                public void error(SAXParseException e) throws SAXException {
                    printException(filename, e);
                }
                
                /**
                 * Receives a non-recoverable error.
                 * @param exception the error information
                 * @exception possibly wrapping another exception
                 */
                public void fatalError(SAXParseException e) throws SAXException {
                    printException(filename, e);
                }
                
                /**
                 * Receives a warning.
                 * @param exception the warning information
                 * @exception possibly wrapping another exception
                 */
                public void warning(SAXParseException e) throws SAXException {
                    printException(filename, e);
                }
            });
            
            File file = new File(filename);
            return builder.parse(file);
            
        } catch (Exception e) {
            System.err.println("DOM: Parse error occurred: " + e.getMessage() + ".");
            return null;
        }
    }
    
    /**
     * Prints the errors during paring the contents of the XML file. 
     * @param filename the name of the file to be read
     * @param e the occurred exception
     */
    private static void printException(String filename, SAXParseException e) {
        System.err.println("[FATAL]file:" + filename + "line:" + e.getLineNumber() + ", " +
          "column:" + e.getColumnNumber() + ", " + e.getMessage());
    }
    
    /**
     * Writes the content into a file.
     * @param filename the name of a file which the contents are written into
     * @param contents the contents to be written, or <code>null</code> if writing the file fails
     */
    public static String write(String filename, String contents) {
        File file = new File(filename);
        boolean resultMakeDir = makeDir(file.getParentFile());
        if (!resultMakeDir) {
            return null;
        }
        
        try {
            FileUtils.writeStringToFile(file, contents);
            return contents;
            
        } catch (final IOException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }
    
    /**
     * 
     * Writes the content of the DOM instance into an XML file.
     * @param doc the content of the DOM instance to be written
     * @param filename the name of a file which the contents are written into
     * @return the wrote DOM instance, or <code>null</code> if writing the XML file fails
     */
    public static Document writeXML(Document doc, String filename) {
        return writeXML(doc, filename, DEFALUT_CHARSET);
    }
    
    /**
     * 
     * Writes the content of the DOM instance into an XML file.
     * @param doc the content of the DOM instance to be written
     * @param filename the name of a file which the contents are written into
     * @param charset the name of a charset of the file
     * @return the wrote DOM instance, or <code>null</code> if writing the XML file fails
     */
    public static Document writeXML(Document doc, String filename, String charset) {
        filename.replace('/', File.separatorChar);
        
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, charset);
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource src = new DOMSource(doc);
            
            StringWriter writer = new StringWriter();
            transformer.transform(src, new StreamResult(writer));
            
            write(filename, writer.toString());
            
            return doc;
            
        } catch (TransformerException e) {
            System.out.println("DOM: Write error occurred: " + e.getMessage() + ".");
            return null;
        }
    }
    
    /**
     * Makes a directory.
     * @param directory the directory to be made
     * @return <code>true</code> making the directory succeeds, otherwise <code>false</code>
     */
    public static boolean makeDir(File directory) {
        try {
            FileUtils.forceMkdir(directory);
            return true;
            
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }
}
