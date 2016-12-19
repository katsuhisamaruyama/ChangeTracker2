/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.xml;

import org.jtool.changetracker.operation.IChangeOperation;
import org.jtool.changetracker.operation.ChangeOperation;
import org.jtool.changetracker.operation.CommandOperation;
import org.jtool.changetracker.operation.CopyOperation;
import org.jtool.changetracker.operation.DocumentOperation;
import org.jtool.changetracker.operation.FileOperation;
import org.jtool.changetracker.operation.ResourceOperation;
import org.jtool.changetracker.operation.GitOperation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import java.util.List;
import java.util.ArrayList;
import java.time.ZonedDateTime;

/**
 * Converts the XML representation into the history of code change operations.
 * @author Katsuhisa Maruyama
 */
public class Xml2Operation {
    
    /**
     * Obtains code change operations from the XML representation.
     * @param path the path name of a file to be read
     * @return the history of the code change operations
     * @throws Exception if the collection of code change operations fails
     */
    public static List<IChangeOperation> getOperations(String path) throws Exception {
        Document doc = XmlFileManager.readXML(path);
        return getOperations(doc);
    }
    
    /**
     * Obtains code change operations from the XML representation.
     * @param doc the DOM instance that has the XML representation
     * @return the history of the code change operations
     * @exception Exception if the format of the operation history file is invalid
     */
    private static List<IChangeOperation> getOperations(Document doc) throws Exception {
        NodeList list = doc.getElementsByTagName(XmlConstants.HistoryElem);
        if (list.getLength() <= 0) {
            throw new Exception("invalid operation history format");
        }
        
        // Element rootElem = (Element)list.item(0);
        // String version = rootElem.getAttribute(XmlConstants.VersionAttr);
        
        NodeList operationList = doc.getElementsByTagName(XmlConstants.OperationsElem);
        if (operationList == null) {
            throw new Exception("invalid operation history format");
        }
        
        Node operationsElem = operationList.item(0);
        if (operationsElem == null) {
            throw new Exception("invalid operation history format");
        }
        
        List<IChangeOperation> operations = new ArrayList<IChangeOperation>();
        NodeList childOperations = operationsElem.getChildNodes();
        for (int i = 0; i < childOperations.getLength(); i++) {
            Node node = childOperations.item(i);
            
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                IChangeOperation operation = getOperation(node);
                if (operation != null) {
                    operations.add(operation);
                }
            }
        }
        
        return operations;
    }
    
    /**
     * Obtains a code change operation from the DOM element.
     * @param node the DOM element
     * @return the code change operation
     */
    private static IChangeOperation getOperation(Node node) {
        Element elem = (Element)node;
        String elemName = elem.getNodeName();
        
        if (elemName.equals(XmlConstants.DocumentOperationElem)) {
            return getDocumentOperation(elem);
            
        } else if (elemName.equals(XmlConstants.CopyOperationElem)) {
            return getCopyOperation(elem);
            
        } else if (elemName.equals(XmlConstants.CommandOperationElem)) {
            return getCommandOperations(elem);
            
        } else if (elemName.equals(XmlConstants.FileOperationElem)) {
            return getFileOperation(elem);
            
        } else if (elemName.equals(XmlConstants.ResourceOperationElem)) {
            return getResourceOperation(elem);
            
        } else if (elemName.equals(XmlConstants.GitOperationElem)) {
            return getGitOperation(elem);
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
        DocumentOperation operation = new DocumentOperation(attr.time, attr.branch, attr.path, attr.action, attr.author);
        operation.setDescription(attr.desc);
        operation.setBundleId(attr.bid);
        
        operation.setStart(Integer.parseInt(elem.getAttribute(XmlConstants.OffsetAttr)));
        operation.setInsertedText(getFirstChildText(elem.getElementsByTagName(XmlConstants.InsertedElem)));
        operation.setDeletedText(getFirstChildText(elem.getElementsByTagName(XmlConstants.DeletedElem)));
        
        return operation;
    }
    
    /**
     * Obtains a copy operation from the DOM element.
     * @param node the DOM element
     * @return the copy operation
     */
    private static CopyOperation getCopyOperation(Element elem) {
        OperationAttribute attr = new OperationAttribute(elem);
        CopyOperation operation = new CopyOperation(attr.time, attr.branch, attr.path, attr.author);
        operation.setDescription(attr.desc);
        operation.setBundleId(attr.bid);
        
        operation.setStart(Integer.parseInt(elem.getAttribute(XmlConstants.OffsetAttr)));
        operation.setCopiedText(getFirstChildText(elem.getElementsByTagName(XmlConstants.CopiedElem)));
        
        return operation;
    }
    
    /**
     * Obtains a command operation from the DOM element.
     * @param node the DOM element
     * @return the command operation
     */
    private static CommandOperation getCommandOperations(Element elem) {
        OperationAttribute attr = new OperationAttribute(elem);
        CommandOperation operation = new CommandOperation(attr.time, attr.path,  attr.branch, attr.action, attr.author);
        operation.setDescription(attr.desc);
        operation.setBundleId(attr.bid);
        
        return operation;
    }
    
    /**
     * Obtains a file operation from the DOM element.
     * @param node the DOM element
     * @return the file operation
     */
    private static FileOperation getFileOperation(Element elem) {
        OperationAttribute attr = new OperationAttribute(elem);
        FileOperation operation = new FileOperation(attr.time, attr.path, attr.branch, attr.action, attr.author);
        operation.setDescription(attr.desc);
        operation.setBundleId(attr.bid);
        
        String code = getFirstChildText(elem.getElementsByTagName(XmlConstants.CodeElem));
        operation.setCode(code);
        
        return operation;
    }
    
    /**
     * Obtains a resource operation from the DOM element.
     * @param node the DOM element
     * @return the resource operation
     */
    private static ResourceOperation getResourceOperation(Element elem) {
        OperationAttribute attr = new OperationAttribute(elem);
        ResourceOperation operation = new ResourceOperation(attr.time, attr.branch, attr.path, attr.action, attr.author);
        operation.setDescription(attr.desc);
        operation.setBundleId(attr.bid);
        
        operation.setSrcDstPath(elem.getAttribute(XmlConstants.SrcDstPathAttr));
        
        return operation;
    }
    
    /**
     * Obtains a git operation from the DOM element.
     * @param node the DOM element
     * @return the git operation
     */
    private static GitOperation getGitOperation(Element elem) {
        OperationAttribute attr = new OperationAttribute(elem);
        GitOperation operation = new GitOperation(attr.time, attr.branch, attr.path, attr.action, attr.author);
        operation.setDescription(attr.desc);
        operation.setBundleId(attr.bid);
        
        operation.setAddedFiles(GitOperation.getNameSet(elem.getAttribute(XmlConstants.AddedFilesAttr)));
        operation.setRemovedFiles(GitOperation.getNameSet(elem.getAttribute(XmlConstants.RemovedFilesAttr)));
        operation.setModifiedFiles(GitOperation.getNameSet(elem.getAttribute(XmlConstants.ModifiedFilesAttr)));
        
        return operation;
    }
    
    /**
     * Obtains the text of stored in the first child of a node list.
     * @param nodes the node list of nodes that stores the text
     * @return the text string, <code>null</code> if no text element was found
     */
    private static String getFirstChildText(NodeList nodeList) {
        if (nodeList == null || nodeList.getLength() == 0) {
            return null;
        }
        
        Node node = nodeList.item(0);
        if (node == null) {
            return null;
        }
        
        NodeList nodes = node.getChildNodes();
        if (nodes == null || nodes.getLength() == 0) {
            return null;
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
        String branch;
        String path;
        String action;
        String author;
        String desc;
        long bid;
        
        /**
         * Obtains basic attributes of a code change operation from the DOM element.
         * @param elem node the DOM element
         */
        OperationAttribute(Element elem) {
            time = ChangeOperation.getTime(elem.getAttribute(XmlConstants.TimeAttr));
            branch = elem.getAttribute(XmlConstants.BranchAttr);
            path = elem.getAttribute(XmlConstants.PathAttr);
            action = elem.getAttribute(XmlConstants.ActionAttr);
            author = elem.getAttribute(XmlConstants.AuthorAttr);
            desc = elem.getAttribute(XmlConstants.DescriptionAttr);
            bid = Long.parseLong(elem.getAttribute(XmlConstants.BundleAttr));
        }
    }
}
