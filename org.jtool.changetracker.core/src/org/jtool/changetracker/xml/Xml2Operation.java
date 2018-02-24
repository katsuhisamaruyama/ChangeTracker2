/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.xml;

import org.jtool.changetracker.operation.IChangeOperation;
import org.jtool.changetracker.operation.ChangeOperation;
import org.jtool.changetracker.operation.CopyOperation;
import org.jtool.changetracker.operation.DocumentOperation;
import org.jtool.changetracker.operation.FileOperation;
import org.jtool.changetracker.operation.CommandOperation;
import org.jtool.changetracker.operation.RefactoringOperation;
import org.jtool.changetracker.operation.ResourceOperation;
import org.jtool.changetracker.repository.CTPath;
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
import java.time.ZonedDateTime;

/**
 * Converts the XML representation into the history of change operations recorded by ChangeTracker-v2.
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
        if (list.getLength() <= 0) {
            return new ArrayList<IChangeOperation>();
        }
        
        String version = getVersion(list);
        if (version.endsWith(HISTORY_VERSION2_EXT)) {
            return getOperations(doc); // ChangeTracker-v2
        } else if (version.endsWith(HISTORY_VERSION1_EXT)) {
            return Xml2OperationCT.getOperations(doc); // ChangeTracker-v1
        } else {
            return Xml2OperationOR.getOperations(doc); // OperationRecorder
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
    public static String getFileName(String path) {
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
     * Obtains change operations from the XML representation.
     * @param doc the DOM instance that has the XML representation
     * @return the collection of the change operations
     */
    private static List<IChangeOperation> getOperations(Document doc) {
        List<IChangeOperation> ops = new ArrayList<IChangeOperation>();
        NodeList operationList = doc.getElementsByTagName(XmlConstants.OperationsElem);
        if (operationList == null) {
            return ops;
        }
        Node operationsElem = operationList.item(0);
        if (operationsElem == null) {
            return ops;
        }
        
        NodeList childOperations = operationsElem.getChildNodes();
        for (int i = 0; i < childOperations.getLength(); i++) {
            Node node = childOperations.item(i);
            
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                IChangeOperation operation = getOperation(node);
                if (operation != null) {
                    ops.add(operation);
                }
            }
        }
        return ops;
    }
    
    /**
     * Obtains a change operation from the DOM element.
     * @param node the DOM element
     * @return the change operation
     */
    private static IChangeOperation getOperation(Node node) {
        Element elem = (Element)node;
        String elemName = elem.getNodeName();
        
        if (elemName.equals(XmlConstants.DocumentOperationElem)) {
            return getDocumentOperation(elem);
        } else if (elemName.equals(XmlConstants.CopyOperationElem)) {
            return getCopyOperation(elem);
        } else if (elemName.equals(XmlConstants.FileOperationElem)) {
            return getFileOperation(elem);
        } else if (elemName.equals(XmlConstants.CommandOperationElem)) {
            return getCommandOperation(elem);
        } else if (elemName.equals(XmlConstants.RefactorOperationElem)) {
            return getRefactoringOperation(elem);
        }  else if (elemName.equals(XmlConstants.ResourceOperationElem)) {
            return getResourceOperation(elem);
        }
        return null;
    }
    
    /**
     * Obtains a document operation from the DOM element.
     * @param elem the DOM element
     * @return the document operation
     */
    private static DocumentOperation getDocumentOperation(Element elem) {
        OperationAttribute attr = new OperationAttribute(elem);
        DocumentOperation op = new DocumentOperation(attr.time, attr.pathinfo, attr.action, attr.author);
        op.setDescription(attr.desc);
        op.setCompoundTime(attr.ctime);
        op.setStart(Integer.parseInt(elem.getAttribute(XmlConstants.OffsetAttr)));
        op.setInsertedText(getFirstChildText(elem.getElementsByTagName(XmlConstants.InsertedElem)));
        op.setDeletedText(getFirstChildText(elem.getElementsByTagName(XmlConstants.DeletedElem)));
        return op;
    }
    
    /**
     * Obtains a copy operation from the DOM element.
     * @param node the DOM element
     * @return the copy operation
     */
    private static CopyOperation getCopyOperation(Element elem) {
        OperationAttribute attr = new OperationAttribute(elem);
        CopyOperation op = new CopyOperation(attr.time, attr.pathinfo, attr.author);
        op.setDescription(attr.desc);
        op.setCompoundTime(attr.ctime);
        op.setStart(Integer.parseInt(elem.getAttribute(XmlConstants.OffsetAttr)));
        op.setCopiedText(getFirstChildText(elem.getElementsByTagName(XmlConstants.CopiedElem)));
        return op;
    }
    
    /**
     * Obtains a file operation from the DOM element.
     * @param node the DOM element
     * @return the file operation
     */
    private static FileOperation getFileOperation(Element elem) {
        OperationAttribute attr = new OperationAttribute(elem);
        FileOperation op = new FileOperation(attr.time, attr.pathinfo, attr.action, attr.author);
        op.setDescription(attr.desc);
        op.setCompoundTime(attr.ctime);
        op.setCharset(elem.getAttribute(XmlConstants.CharsetAttr));
        op.setSrcDstPath(elem.getAttribute(XmlConstants.SrcDstPathAttr));
        String code = getFirstChildCode(elem.getElementsByTagName(XmlConstants.CodeElem));
        if (code == null) {
            return null;
        }
        op.setCode(code);
        return op;
    }
    
    /**
     * Obtains a command operation from the DOM element.
     * @param node the DOM element
     * @return the command operation
     */
    private static CommandOperation getCommandOperation(Element elem) {
        OperationAttribute attr = new OperationAttribute(elem);
        CommandOperation op = new CommandOperation(attr.time, attr.pathinfo, attr.action, attr.author);
        op.setDescription(attr.desc);
        op.setCompoundTime(attr.ctime);
        op.setCommandId(elem.getAttribute(XmlConstants.CommandIdAttr));
        return op;
    }
    
    /**
     * Obtains a refactoring operation from the DOM element.
     * @param node the DOM element
     * @return the refactoring operation
     */
    private static RefactoringOperation getRefactoringOperation(Element elem) {
        OperationAttribute attr = new OperationAttribute(elem);
        RefactoringOperation op = new RefactoringOperation(attr.time, attr.pathinfo, attr.action, attr.author);
        op.setDescription(attr.desc);
        op.setCompoundTime(attr.ctime);
        op.setName(elem.getAttribute(XmlConstants.NameAttr));
        op.setSelectionStart(Integer.parseInt(elem.getAttribute(XmlConstants.OffsetAttr)));
        op.setArguments(elem.getAttribute(XmlConstants.ArgumentAttr));
        String code = getFirstChildCode(elem.getElementsByTagName(XmlConstants.CodeElem));
        if (code != null) {
            return null;
        }
        op.setSelectedText(code);
        return op;
    }
    
    /**
     * Obtains a resource operation from the DOM element.
     * @param node the DOM element
     * @return the resource operation
     */
    private static ResourceOperation getResourceOperation(Element elem) {
        OperationAttribute attr = new OperationAttribute(elem);
        ResourceOperation op = new ResourceOperation(attr.time, attr.pathinfo, attr.action, attr.author);
        op.setDescription(attr.desc);
        op.setTarget(elem.getAttribute(XmlConstants.TargetAttr));
        op.setSrcDstPath(elem.getAttribute(XmlConstants.SrcDstPathAttr));
        return op;
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
    private static String getFirstChildText(Node node) {
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
    
    /**
     * Reads and stores the values of basic attributes of code change operations.
     * @author Katsuhisa Maruyama
     */
    static class OperationAttribute {
        
        ZonedDateTime time;
        String action;
        String author;
        String desc;
        ZonedDateTime ctime;
        CTPath pathinfo;
        
        /**
         * Obtains basic attributes of a code change operation from the DOM element.
         * @param elem node the DOM element
         */
        OperationAttribute(Element elem) {
            time = ChangeOperation.getTime(elem.getAttribute(XmlConstants.TimeAttr));
            action = elem.getAttribute(XmlConstants.ActionAttr);
            author = elem.getAttribute(XmlConstants.AuthorAttr);
            desc = elem.getAttribute(XmlConstants.DescriptionAttr);
            String compoundTimeStr = elem.getAttribute(XmlConstants.CompoundTimeAttr);
            if (compoundTimeStr.length() > 0) {
                ctime = ChangeOperation.getTime(compoundTimeStr);
            } else {
                ctime = null;
            }
            
            String projectName = elem.getAttribute(XmlConstants.ProjectNameAttr);
            String packageName = elem.getAttribute(XmlConstants.PackageNameAttr);
            String path = elem.getAttribute(XmlConstants.PathAttr);
            String branch = elem.getAttribute(XmlConstants.BranchAttr);
            String fileName = getFileName(path);
            pathinfo = new CTPath(projectName, packageName, fileName, path, branch);
        }
    }
}

