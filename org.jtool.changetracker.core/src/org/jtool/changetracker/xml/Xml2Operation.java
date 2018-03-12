/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.xml;

import org.jtool.changetracker.operation.IChangeOperation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.io.File;

/**
 * Converts the XML representation into the history of change operations.
 * @author Katsuhisa Maruyama
 */
public class Xml2Operation {
    
    /**
     * The extension string of a history file.
     */
    public static String XML_FILE_EXTENTION = ".xml";
    
    /**
     * The string that indicates the version of stored change operations.
     */
    static final String HISTORY_VERSION1_EXT = "a";
    static final String HISTORY_VERSION2_EXT = "ct2";
    static final String HISTORY_VERSION2 = "2.0" + HISTORY_VERSION2_EXT;
    
    /**
     * Obtains change operations from the XML representation.
     * @param path the path name of a file to be read
     * @return the collection of the change operations, or empty set when failures in reading a file
     */
    public static List<IChangeOperation> getOperations(String path) {
        Document doc = XmlFileManager.readXML(path);
        NodeList list = doc.getElementsByTagName(XmlConstants.HistoryElem);
        
        if (list.getLength() == 0) {
            return new ArrayList<IChangeOperation>();
        }
        
        String version = getVersion(list);
        if (version.endsWith(HISTORY_VERSION2_EXT)) {
            return Xml2OperationCT2.getOperations(doc);
        } else if (version.endsWith(HISTORY_VERSION1_EXT)) {
            return Xml2OperationCT.getOperations(doc);
        } else {
            return Xml2OperationOR.getOperations(doc);
        }
    }
    
    /**
     * Obtains the version of the XML representation.
     * @param list the list of top elements
     * @return the version string
     */
    private static String getVersion(NodeList list) {
        Element topElem = (Element)list.item(0);
        return topElem.getAttribute(XmlConstants.VersionAttr);
    }
    
    /**
     * Tests if the version of the XML representation is compatible with "changeTracker v2"
     * @param dirpath the path of a directory that contains operation history files
     * @return <code>true</code> if the XML representation is compatible with "changeTracker v2", otherwise <code>false</code>
     */
    public static boolean isChangeTrackerVersion2(String dirpath) {
        List<File> files = getHistoryFiles(dirpath);
        if (files.size() == 0) {
            return false;
        }
        Document doc = XmlFileManager.readXML(files.get(0).getAbsolutePath());
        NodeList list = doc.getElementsByTagName(XmlConstants.HistoryElem);
        if (list.getLength() <= 0) {
            return false;
        }
        String version = getVersion(list);
        return version.endsWith(HISTORY_VERSION2_EXT);
    }
    
    /**
     * Returns all descendant history files of a directory.
     * @param path the path of the directory
     * @return the collection of all the descendant files
     */
    public static List<File> getHistoryFiles(String path) {
        return getHistoryFiles(path, null);
    }
    
    /**
     * Returns all descendant history files of a directory.
     * @param path the path of the directory
     * @param prefix a character that indicates the prefix of the name of a directory excluded
     * @return the collection of all the descendant files
     */
    public static List<File> getHistoryFiles(String path, String prefix) {
        List<File> files = new ArrayList<File>();
        String name = getFileName(path);
        if ((prefix != null && name.startsWith(prefix)) || name.startsWith(".")) {
            return files;
        }
        
        File dir = new File(path);
        if (dir.isFile()) {
            if (path.endsWith(XML_FILE_EXTENTION)) {
                files.add(dir);
            }
        } else if (dir.isDirectory() ) {
            File[] children = dir.listFiles();
            for (File f : children) {
                files.addAll(getHistoryFiles(f.getPath(), prefix));
            }
        }
        
        sortFiles(files);
        return files;
    }
    
    private static void sortFiles(List<File> files) {
        Collections.sort(files, new Comparator<File>() {
            
            public int compare(File file1, File file2) {
                return file1.getPath().compareTo(file2.getPath());
            }
        });
    }
    
    /**
     * Returns the name part of the path of the file.
     * @param path the path of the file
     * @return the file name
     */
    static String getFileName(String path) {
        if (path == null) {
            return "";
        }
        int index = path.lastIndexOf(File.separatorChar);
        if (index == -1) {
            return "";
        }
        return path.substring(index + 1);
    }
    
    /**
     * Obtains the text of stored in the first child of a node list.
     * @param nodeList the node list of nodes store the text
     * @return the text string, <code>null</code> if no text was found
     */
    static String getFirstChildText(NodeList nodeList) {
        if (nodeList == null || nodeList.getLength() == 0) {
            return "";
        }
        String text = getFirstChildText(nodeList.item(0));
        if (text == null) {
            return "";
        }
        return text;
    }
    
    /**
     * Obtains the code of stored in the first child of a node list.
     * @param nodeList the node list of nodes store the text
     * @return the text string of code, <code>null</code> if no text was found
     */
    static String getFirstChildCode(NodeList nodeList) {
        if (nodeList == null || nodeList.getLength() == 0) {
            return null;
        }
        
        return getFirstChildText(nodeList.item(0));
    }
    
    /**
     * Obtains the text of stored in the first child of a node.
     * @param node the node that stores the text
     * @return the text string, <code>null</code> if no text element was found
     */
    static String getFirstChildText(Node node) {
        if (node == null) {
            return null;
        }
        
        NodeList nodes = node.getChildNodes();
        
        if (nodes == null || nodes.getLength() == 0) {
            return "";
        }
        Node child = nodes.item(0);
        if (child.getNodeType() == Node.TEXT_NODE) {
            return ((Text)child).getNodeValue();
        }
        return null;
    }
}
