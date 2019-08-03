/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.xml;

import org.jtool.changetracker.core.CTConsole;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;

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
    @SuppressWarnings("deprecation")
    public static String read(String filename) {
        File file = new File(filename);
        try {
            return FileUtils.readFileToString(file);
            
        } catch (IOException e) {
            CTConsole.println(e.getMessage());
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
            CTConsole.println("DOM: Parse error occurred: " + e.getMessage() + ".");
            return null;
        }
    }
    
    /**
     * Prints the errors during paring the contents of the XML file.
     * @param filename the name of the file to be read
     * @param e the occurred exception
     */
    private static void printException(String filename, SAXParseException e) {
        CTConsole.println("[FATAL]file: " + filename + "line:" + e.getLineNumber() + ", " +
          "column:" + e.getColumnNumber() + ", " + e.getMessage());
    }
    
    /**
     * Writes the content into a file.
     * @param filename the name of a file which the contents are written into
     * @param contents the contents to be written, or <code>null</code> if writing the file fails
     */
    @SuppressWarnings("deprecation")
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
            CTConsole.println(e.getMessage());
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
            CTConsole.println("DOM: Write error occurred: " + e.getMessage() + ".");
            return null;
        }
    }
    
    /**
     * Makes a directory.
     * @param dir the directory to be made
     * @return <code>true</code> making the directory succeeds, otherwise <code>false</code>
     */
    public static boolean makeDir(File dir) {
        try {
            FileUtils.forceMkdir(dir);
            return true;
            
        } catch (IOException e) {
            CTConsole.println(e.getMessage());
            return false;
        }
    }
    
    /**
     * Deletes a directory.
     * @param dir the directory to be deleted
     */
    public static void deleteDir(File dir) {
        try {
            FileUtils.deleteDirectory(dir);
        } catch (IOException e) {
            CTConsole.println(e.getMessage());
        }
    }
    
    /**
     * Tests if a given path indicates a directory.
     * @param dirpath the path of a directory
     * @return <code>true</code> if the path indicates a directory, otherwise <code>false</code>
     */
    public static boolean isDir(String dirpath) {
        File dir = new File(dirpath);
        return dir.isDirectory();
    }
    
    /**
     * Creates a zip archive for all files and directories under a specified directory.
     * @param zipname the name of the created zip archive
     * @param dir the directory that contains the added files and directories
     * @return <code>true</code> if the creation of the zip archive succeeded, otherwise <code>false</code>
     */
    public static boolean makeZip(String zipname, File dir) {
        File zipfile = new File(zipname);
        try (OutputStream ostream = new BufferedOutputStream(new FileOutputStream(zipfile));
             ZipArchiveOutputStream zstream = new ZipArchiveOutputStream(ostream)) {
            zstream.setEncoding(DEFALUT_CHARSET);
            addAll(zstream, dir.getAbsolutePath(), dir);
            zstream.finish();
            zstream.flush();
            ostream.flush();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Adds all files and directories under a specified file or directory into the zip archive.
     * @param zstream the output stream of the zip archive 
     * @param basePath the path of the base directory
     * @param file the file or directory that contains added files and directories
     * @throws IOException if any input/output exception was occurred
     */
    private static void addAll(ZipArchiveOutputStream zstream, String basePath, File file) throws IOException {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children.length == 0) {
                addDir(zstream, basePath, file);
            } else {
                for (File dir : children) {
                    addAll(zstream, basePath, dir);
                }
            }
        } else {
            addFile(zstream, basePath, file);
        }
    }
    
    /**
     * Adds a directory into the zip archive.
     * @param zstream the output stream of the zip archive 
     * @param basePath the path of the base directory
     * @param file the file to be added
     * @throws IOException if any input/output exception was occurred
     */
    private static void addDir(ZipArchiveOutputStream zstream, String basePath, File file) throws IOException {
        String path = file.getAbsolutePath();
        String name = path.substring(basePath.length());
        zstream.putArchiveEntry(new ZipArchiveEntry(name + "/"));
        zstream.closeArchiveEntry();
    }
    
    /**
     * Adds a file into the zip archive.
     * @param zstream the output stream of the zip archive
     * @param basePath the path of the base directory
     * @param file the file to be added
     * @throws IOException if any input/output exception was occurred
     */
    private static void addFile(ZipArchiveOutputStream zstream, String basePath, File file) throws IOException {
        String path = file.getAbsolutePath();
        String name = path.substring(basePath.length());
        zstream.putArchiveEntry(new ZipArchiveEntry(name));
        IOUtils.copy(new FileInputStream(file), zstream);
        zstream.closeArchiveEntry();
    }
}
