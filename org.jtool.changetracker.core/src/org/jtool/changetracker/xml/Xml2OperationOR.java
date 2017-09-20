/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.xml;

import org.jtool.changetracker.operation.IChangeOperation;
import org.jtool.changetracker.operation.ChangeOperation;
import org.jtool.changetracker.operation.CopyOperation;
import org.jtool.changetracker.operation.DocumentOperation;
import org.jtool.changetracker.operation.FileOperation;
import org.jtool.changetracker.operation.ICodeOperation;
import org.jtool.changetracker.repository.CTPath;
import org.jtool.changetracker.operation.CommandOperation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.List;
import java.util.ArrayList;
import java.time.ZonedDateTime;

/**
 * Converts the XML representation into the history of change operations recorded by OperationRecorder.
 * @author Katsuhisa Maruyama
 */
public class Xml2OperationOR {
    
    /**
     *  The elements and attributes appearing in XML documents for OperationRecorder.
     */
    private static final String DeveloperElem           = "developer";
    private static final String FileElem                = "file";
    private static final String SourceCodeElem          = "sourceCode";
    private static final String NormalOperationElem     = "normalOperation";
    private static final String CompoundedOperationElem = "compoundedOperations";
    private static final String CopyOperationElem       = "copyOperation";
    private static final String FileOperationElem       = "fileOperation";
    private static final String MenuOperationElem       = "menuOperation";
    private static final String CCPAttr                 = "ccp";
    private static final String CCPTypeAttr             = "cptype";
    private static final String TypeAttr                = "type";
    private static final String FileAttr                = "file";
    private static final String LabelAttr               = "label";
    
    /**
     * Converts a <code>long</code> value into time information.
     * @param time the <code>long</code> value that represents time. 
     * @return the time with zone information
     */
    static ZonedDateTime getTime(long time) {
        return Xml2OperationCT.getTime(time);
    }
    
    /**
     * The name of the default branch.
     * ChangeTracker-v1 does not deal with branch information.
     */
    static final String BRANCH = "";
    
    /**
     * The name of a developer who performed a change operation.
     */
    private static String developer = "";
    
    /**
     * The path of a file on which a change operation was performed.
     */
    private static String path = "";
    
    /**
     * Returns the path of a file on which a change operation was performed.
     * @param str the attribute value of a path.
     * @return the path name of the file
     */
    private static String getPath(String str) {
        if (str.length() != 0) {
            return str;
        } else {
            return path;
        }
    }
    
    /**
     * Creates information about the path of a resource on which a change operation was performed.
     * @param elem the DOM element
     * @return the path information
     */
    private static CTPath createPathInfo(Element elem) {
        String path = getPath(elem.getAttribute(FileAttr));
        String projectName = Xml2OperationCT.getProjectName(path);
        String packageName = Xml2OperationCT.getPackageName(path);
        String fileName = Xml2Operation.getFileName(path);
        return new CTPath(projectName, packageName, fileName, path, BRANCH);
    }
    /**
     * Obtains change operations from the XML representation.
     * @param doc the DOM instance that has the XML representation
     * @return the collection of the change operations
     */
    static List<IChangeOperation> getOperations(Document doc) {
        List<IChangeOperation> ops = new ArrayList<IChangeOperation>();
        NodeList operationList = doc.getElementsByTagName(XmlConstants.OperationsElem);
        if (operationList == null) {
            return ops;
        }
        Node operationsElem = operationList.item(0);
        if (operationsElem == null) {
            return ops;
        }
        NodeList developers = doc.getElementsByTagName(DeveloperElem);
        if (developers == null) {
            developer = "Unknown";
        } else {
            developer = Xml2Operation.getFirstChildText(developers);
        }
        NodeList paths = doc.getElementsByTagName(FileElem);
        if (paths == null) {
            path = "Unknown";
        } else {
            path = Xml2Operation.getFirstChildText(paths);
        }
        
        NodeList childOperations = operationsElem.getChildNodes();
        for (int i = 0; i < childOperations.getLength(); i++) {
            Node node = childOperations.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                ops.addAll(getOperation(node));
            }
        }
        return ops;
    }
    
    /**
     * Obtains a change operation from the DOM element.
     * @param node the DOM element
     * @return the collection of change operations
     */
    private static List<IChangeOperation> getOperation(Node node) {
        Element elem = (Element)node;
        String elemName = elem.getNodeName();
        
        List<IChangeOperation> ops = new ArrayList<IChangeOperation>();
        if (elemName.equals(NormalOperationElem)) {
            ops.add(getDocumentOperation(elem));
        } else if (elemName.equals(CompoundedOperationElem)) {
            ops.addAll(getCompoundOperation(elem));
        } else if (elemName.equals(CopyOperationElem)) {
            ops.add(getCopyOperation(elem));
        } else if (elemName.equals(FileOperationElem)) {
            ops.add(getFileOperation(elem));
        } else if (elemName.equals(MenuOperationElem)) {
            ops.add(getCommandOperation(elem));
        }
        return ops;
    }
    
    /**
     * Obtains a document operation from the DOM element.
     * @param elem the DOM element
     * @return the document operation
     */
    private static DocumentOperation getDocumentOperation(Element elem) {
        ZonedDateTime time = getTime(Long.parseLong(elem.getAttribute(XmlConstants.TimeAttr)));
        CTPath pathinfo = createPathInfo(elem);
        String action = elem.getAttribute(CCPTypeAttr);
        if (action.length() == 0) {
            action = elem.getAttribute(CCPAttr);
        }
        if ("NONE".equals(action) || "NO".equals(action)) {
            action = ICodeOperation.Action.TYPING.toString();
        }
        
        DocumentOperation op = new DocumentOperation(time, pathinfo, action, developer);
        op.setStart(Integer.parseInt(elem.getAttribute(XmlConstants.OffsetAttr)));
        op.setInsertedText(Xml2Operation.getFirstChildText(elem.getElementsByTagName(XmlConstants.InsertedElem)));
        op.setDeletedText(Xml2Operation.getFirstChildText(elem.getElementsByTagName(XmlConstants.DeletedElem)));
        return op;
    }
    
    /**
     * Parses a compound operation from the DOM element.
     * @param elem the DOM element
     * @return the collection of change operations in the compound operation
     */
    private static List<IChangeOperation> getCompoundOperation(Element elem) {
        ZonedDateTime time = getTime(Long.parseLong(elem.getAttribute(XmlConstants.TimeAttr)));
        NodeList childList = elem.getChildNodes();
        List<IChangeOperation> ops = new ArrayList<IChangeOperation>();
        for (int i = 0; i < childList.getLength(); i++) {
            if (childList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                for (IChangeOperation op : getOperation((Element)childList.item(i))) {
                    if (time == null) {
                        time = op.getTime();
                    }
                    if (op instanceof ChangeOperation) {
                        ((ChangeOperation)op).setCompoundTime(time);
                        ops.add(op);
                    }
                }
            }
        }
        return ops;
    }
    
    /**
     * Obtains a copy operation from the DOM element.
     * @param node the DOM element
     * @return the copy operation
     */
    private static CopyOperation getCopyOperation(Element elem) {
        ZonedDateTime time = getTime(Long.parseLong(elem.getAttribute(XmlConstants.TimeAttr)));
        CTPath pathinfo = createPathInfo(elem);
        
        CopyOperation op = new CopyOperation(time, pathinfo, developer);
        op.setStart(Integer.parseInt(elem.getAttribute(XmlConstants.OffsetAttr)));
        op.setCopiedText(Xml2Operation.getFirstChildText(elem.getElementsByTagName(XmlConstants.CopiedElem)));
        return op;
    }
    
    /**
     * Obtains a file operation from the DOM element.
     * @param node the DOM element
     * @return the file operation
     */
    private static FileOperation getFileOperation(Element elem) {
        ZonedDateTime time = getTime(Long.parseLong(elem.getAttribute(XmlConstants.TimeAttr)));
        CTPath pathinfo = createPathInfo(elem);
        String action = parseFileAction(elem.getAttribute(TypeAttr));
        
        FileOperation op = new FileOperation(time, pathinfo, action, developer);
        String code = Xml2Operation.getFirstChildText(elem.getElementsByTagName(XmlConstants.CodeElem));
        if (code == null) {
            code = Xml2Operation.getFirstChildText(elem.getElementsByTagName(SourceCodeElem));
            if (code == null) {
                code = "";
            }
        }
        op.setCode(code);
        return op;
    }
    
    /**
     * Obtains the action of a file operation.
     * @param str the string indicating the action
     * @return the file action, or <code>NONE</code> if none
     */
    static String parseFileAction(String str) {
        if ("NEW".equals(str)) {
            return FileOperation.Action.ADD.toString();
        } else if ("OPEN".equals(str)) {
            return FileOperation.Action.OPEN.toString();
        } else if ("CLOSE".equals(str)) {
            return FileOperation.Action.CLOSE.toString();
        } else if ("SAVE".equals(str)) {
            return FileOperation.Action.SAVE.toString();
        } else if ("DELETE".equals(str)) {
            return FileOperation.Action.REMOVE.toString();
        } else if ("ACT".equals(str)) {
            return FileOperation.Action.ACTIVATE.toString();
        }
        return "NONE";
    }
    
    /**
     * Obtains a command operation from the DOM element.
     * @param node the DOM element
     * @return the command operation
     */
    private static CommandOperation getCommandOperation(Element elem) {
        ZonedDateTime time = getTime(Long.parseLong(elem.getAttribute(XmlConstants.TimeAttr)));
        CTPath pathinfo = createPathInfo(elem);
        
        CommandOperation op = new CommandOperation(time, pathinfo, developer);
        op.setCommandId(elem.getAttribute(LabelAttr));
        return op;
    }
}
