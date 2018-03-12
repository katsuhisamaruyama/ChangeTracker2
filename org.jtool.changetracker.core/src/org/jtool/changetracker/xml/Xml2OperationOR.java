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
import org.jtool.changetracker.operation.ICodeOperation;
import org.jtool.changetracker.repository.CTPath;
import org.jtool.changetracker.operation.CommandOperation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.List;
import java.util.Stack;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Converts the XML representation of OperationRecorder into the history of change operations.
 * @author Katsuhisa Maruyama
 */
public class Xml2OperationOR {
    
    /**
     *  The elements and attributes appearing in XML documents for OperationRecorder.
     */
    public static final String DeveloperElem           = "developer";
    public static final String FileElem                = "file";
    public static final String SourceCodeElem          = "sourceCode";
    public static final String NormalOperationElem     = "normalOperation";
    public static final String CompoundedOperationElem = "compoundOperation";
    public static final String CopyOperationElem       = "copyOperation";
    public static final String FileOperationElem       = "fileOperation";
    public static final String MenuOperationElem       = "menuOperation";
    public static final String CCPAttr                 = "ccp";
    public static final String CCPTypeAttr             = "cptype";
    public static final String CopyAttr                = "copy";
    public static final String TypeAttr                = "type";
    public static final String FileAttr                = "file";
    public static final String LabelAttr               = "label";
    
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
     * A counter that counts change operations with the same time
     */
    private static Map<String, Integer> timeCount;
    
    /**
     * A stack that stores document operations for undoing.
     */
    private static Stack<DocumentOperation> undoStack;
    
    /**
     * A stack that stores document operations for redoing.
     */
    private static Stack<DocumentOperation> redoStack;
    
    /**
     * Initializes this converter.
     */
    static void init() {
        timeCount = new HashMap<String, Integer>();
        undoStack = new Stack<DocumentOperation>();
        redoStack = new Stack<DocumentOperation>();
    }
    
    /**
     * Converts a <code>long</code> value into time information.
     * @param time the <code>long</code> value that represents time. 
     * @return the time with zone information
     */
    static ZonedDateTime getTime(String timeStr) {
        int plus = 0;
        Integer count = timeCount.get(timeStr);
        if (count != null) {
            plus = count.intValue();
        }
        timeCount.put(timeStr, new Integer(plus + 1));
        long time = Long.parseLong(timeStr) + plus;
        return Xml2OperationCT.getTime(time);
    }
    
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
        developer = Xml2Operation.getFirstChildText(developers);
        if (developer.length() == 0) {
            developer = "Unknown";
        }
        NodeList paths = doc.getElementsByTagName(FileElem);
        path = Xml2Operation.getFirstChildText(paths);
        if (path.length() == 0) {
            path = "Unknown";
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
            DocumentOperation op = getDocumentOperation(elem);
            addOperation(ops, op);
            undoStack.push(op);
            
        } else if (elemName.equals(CompoundedOperationElem)) {
            for (IChangeOperation op : getCompoundOperation(elem)) {
                addOperation(ops, op);
                if (op.isDocument()) {
                    undoStack.push((DocumentOperation)op);
                }
            }
            
        } else if (elemName.equals(CopyOperationElem)) {
            addOperation(ops,getCopyOperation(elem));
            
        } else if (elemName.equals(FileOperationElem)) {
            addOperation(ops,getFileOperation(elem));
            
        } else if (elemName.equals(MenuOperationElem)) {
            CommandOperation op = getCommandOperation(elem);
            
            if (op != null && op.getCommandId().endsWith("undo")) {
                DocumentOperation dop = getUndoRedoOperation(op, undoStack, ICodeOperation.Action.UNDO.toString());
                if (dop != null) {
                    addOperation(ops, op);
                    addOperation(ops, dop);
                }
            } else if (op != null && op.getCommandId().endsWith("redo")) {
                DocumentOperation dop = getUndoRedoOperation(op, redoStack, ICodeOperation.Action.REDO.toString());
                if (dop != null) {
                    addOperation(ops, op);
                    addOperation(ops, dop);
                }
            } else {
                addOperation(ops, op);
            }
        }
        return ops;
    }
    
    /**
     * Adds a change operation into the collection
     * @param ops the collection of change operations
     * @param op the operation to be added
     */
    private static void addOperation(List<IChangeOperation> ops, IChangeOperation op) {
        if (op != null) {
            ops.add(op);
        }
    }
    
    /**
     * Obtains a document operation from the DOM element.
     * @param elem the DOM element
     * @return the document operation
     */
    private static DocumentOperation getDocumentOperation(Element elem) {
        ZonedDateTime time = getTime(elem.getAttribute(XmlConstants.TimeAttr));
        CTPath pathinfo = createPathInfo(elem);
        String action = elem.getAttribute(CCPTypeAttr);
        if (action.length() == 0) {
            action = elem.getAttribute(CCPAttr);
            if (action.length() == 0) {
                action = elem.getAttribute(CopyAttr);
            }
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
        ZonedDateTime time = getTime(elem.getAttribute(XmlConstants.TimeAttr));
        NodeList childList = elem.getChildNodes();
        List<IChangeOperation> ops = new ArrayList<IChangeOperation>();
        for (int i = 0; i < childList.getLength(); i++) {
            if (childList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                for (IChangeOperation op : getOperation((Element)childList.item(i))) {
                    if (op instanceof ChangeOperation) {
                        ((ChangeOperation)op).setCompoundTime(time);
                        time = time.plus(1, ChronoUnit.NANOS);
                        ((ChangeOperation)op).setTime(time);
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
        ZonedDateTime time = getTime(elem.getAttribute(XmlConstants.TimeAttr));
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
        ZonedDateTime time = getTime(elem.getAttribute(XmlConstants.TimeAttr));
        CTPath pathinfo = createPathInfo(elem);
        String action = parseFileAction(elem.getAttribute(TypeAttr));
        
        FileOperation op = new FileOperation(time, pathinfo, action, developer);
        String code = Xml2Operation.getFirstChildCode(elem.getElementsByTagName(XmlConstants.CodeElem));
        if (code == null) {
            code = Xml2Operation.getFirstChildCode(elem.getElementsByTagName(SourceCodeElem));
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
            return FileOperation.Action.ADDED.toString();
        } else if ("OPEN".equals(str)) {
            return FileOperation.Action.OPENED.toString();
        } else if ("CLOSE".equals(str)) {
            return FileOperation.Action.CLOSED.toString();
        } else if ("SAVE".equals(str)) {
            return FileOperation.Action.SAVED.toString();
        } else if ("DELETE".equals(str)) {
            return FileOperation.Action.REMOVED.toString();
        } else if ("ACT".equals(str)) {
            return FileOperation.Action.ACTIVATED.toString();
        }
        return "NONE";
    }
    
    /**
     * Obtains a command operation from the DOM element.
     * @param node the DOM element
     * @return the command operation
     */
    private static CommandOperation getCommandOperation(Element elem) {
        ZonedDateTime time = getTime(elem.getAttribute(XmlConstants.TimeAttr));
        CTPath pathinfo = createPathInfo(elem);
        
        if (pathinfo.getPath() == null || "null".equals(pathinfo.getPath())) {
            return null;
        }
        
        CommandOperation op = new CommandOperation(time, pathinfo, CommandOperation.Action.EXECUTE.toString(), developer);
        op.setCommandId(elem.getAttribute(LabelAttr));
        return op;
    }
    
    /**
     * Obtains a document operation corresponding to undoing or redoing.
     * @param cop the commend operation for the undo or redo action
     * @param stack a stack that contains the target of undoing or redoing
     * @param action action string that represents the undo or redo action
     * @return the document operation corresponding to undoing or redoing
     */
    private static DocumentOperation getUndoRedoOperation(CommandOperation cop, Stack<DocumentOperation> stack, String action) {
        if (stack.isEmpty()) {
            return null;
        }
        DocumentOperation dop = stack.pop();
        if (dop == null) {
            return null;
        }
        
        ZonedDateTime time = cop.getTime().plus(1, ChronoUnit.NANOS);
        CTPath pathinfo = new CTPath(dop.getProjectName(), dop.getPackageName(), dop.getFileName(), dop.getPath(), BRANCH);
        DocumentOperation op = new DocumentOperation(time, pathinfo, action, developer);
        op.setStart(dop.getStart());
        op.setInsertedText(dop.getDeletedText());
        op.setDeletedText(dop.getDeletedText());
        redoStack.push(op);
        
        System.out.println(action + " " + op.toString());
        return op;
    }
}
