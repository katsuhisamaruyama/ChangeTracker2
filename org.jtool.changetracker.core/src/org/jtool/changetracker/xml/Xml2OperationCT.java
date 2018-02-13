/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.xml;

import org.jtool.changetracker.operation.IChangeOperation;
import org.jtool.changetracker.operation.ChangeOperation;
import org.jtool.changetracker.operation.ICodeOperation;
import org.jtool.changetracker.operation.CopyOperation;
import org.jtool.changetracker.operation.DocumentOperation;
import org.jtool.changetracker.operation.FileOperation;
import org.jtool.changetracker.operation.CommandOperation;
import org.jtool.changetracker.repository.CTPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.io.File;
import java.time.ZonedDateTime;
import java.time.ZoneId;

/**
 * Converts the XML representation into the history of change operations recorded by ChangeTracker-v1.
 * @author Katsuhisa Maruyama
 */
public class Xml2OperationCT {
    
    /**
     *  The elements and attributes appearing in XML documents for ChangeTracker-v1.
     */
    private static final String NormalOperationElem   = "normalOperation";
    private static final String CompoundOperationElem = "compoundOperation";
    private static final String CopyOperationElem     = "copyOperation";
    private static final String FileOperationElem     = "fileOperation";
    private static final String MenuOperationElem     = "menuOperation";
    private static final String ResourceOperationElem = "resourceOperation";
    private static final String FileAttr              = "file";
    private static final String LabelAttr             = "label";
    private static final String TargetAttr            = "target";
    private static final String APathAttr             = "apath";
    
    /**
     * Converts a <code>long</code> value into time information.
     * @param time the <code>long</code> value that represents time. 
     * @return the time with zone information
     */
    static ZonedDateTime getTime(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return ZonedDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault());
    }
    
    /**
     * Returns the name of the project under a given path.
     * @param the path for the file
     * @return the project name
     */
    static String getProjectName(String path) {
        if (path == null || path.length() == 0) {
            return "Unknown";
        }
        int index = path.indexOf(File.separatorChar, 1);
        if (index == -1) {
            return "Unknown";
        }
        return path.substring(1, index);
    }
    
    /**
     * Return the name of the package under a given path.
     * @param path the path for the file
     * @return the package name
     */
    static String getPackageName(String path) {
        final String SRCDIR = File.separatorChar + "src" + File.separatorChar;
        int findex = path.indexOf(SRCDIR);
        int lindex = path.lastIndexOf(File.separatorChar);
        if (findex == -1 || lindex == -1) {
            return "Unknown";
        }
        if (findex + SRCDIR.length() > lindex) {
            return "(default package)";
        }
        String name = path.substring(findex + SRCDIR.length(), lindex);
        return name.replace(File.separatorChar, '.');
    }
    
    /**
     * Return the name of the file under a given path.
     * @param path the path for the file
     * @return the file name without its path information
     */
    static String getFileName(String path) {
        if (path == null || path.length() == 0) {
            return "Unknown";
        }
        int index = path.lastIndexOf(File.separatorChar);
        if (index == -1) {
            return "Unknown";
        }
        return path.substring(index + 1);
    }
    
    /**
     * The name of the default branch.
     * ChangeTracker-v1 does not deal with branch information.
     */
    static final String BRANCH = "";
    
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
        } else if (elemName.equals(CompoundOperationElem)) {
            ops.addAll(getCompoundOperation(elem));
        } else if (elemName.equals(CopyOperationElem)) {
            ops.add(getCopyOperation(elem));
        } else if (elemName.equals(FileOperationElem)) {
            ops.add(getFileOperation(elem));
        } else if (elemName.equals(MenuOperationElem)) {
            ops.add(getCommandOperation(elem));
        } else if (elemName.equals(ResourceOperationElem)) {
            ops.add(getResourceOperation(elem));
        }
        return ops;
    }
    
    /**
     * Creates information about the path of a resource on which a change operation was performed.
     * @param elem the DOM element
     * @return the path information
     */
    private static CTPath createPathInfo(Element elem) {
        String path = elem.getAttribute(FileAttr);
        String projectName = getProjectName(path);
        String packageName = getPackageName(path);
        String fileName = Xml2Operation.getFileName(path);
        return new CTPath(projectName, packageName, fileName, path, BRANCH);
    }
    
    /**
     * Obtains a document operation from the DOM element.
     * @param elem the DOM element
     * @return the document operation
     */
    private static DocumentOperation getDocumentOperation(Element elem) {
        ZonedDateTime time = getTime(Long.parseLong(elem.getAttribute(XmlConstants.TimeAttr)));
        CTPath pathinfo = createPathInfo(elem);
        String action = parseDocumentAction(elem.getAttribute(XmlConstants.ActionAttr));
        String author = elem.getAttribute(XmlConstants.AuthorAttr);
        
        DocumentOperation op = new DocumentOperation(time, pathinfo, action, author);
        op.setStart(Integer.parseInt(elem.getAttribute(XmlConstants.OffsetAttr)));
        op.setInsertedText(Xml2Operation.getFirstChildText(elem.getElementsByTagName(XmlConstants.InsertedElem)));
        op.setDeletedText(Xml2Operation.getFirstChildText(elem.getElementsByTagName(XmlConstants.DeletedElem)));
        return op;
    }
    
    /**
     * Obtains the action of a document operation.
     * @param str the string indicating the action
     * @return the document action, or <code>NONE</code> if none
     */
    static String parseDocumentAction(String str) {
        if ("EDIT".equals(str) || "NO".equals(str)) {
            return ICodeOperation.Action.TYPING.toString();
        } else if ("CUT".equals(str)) {
            return ICodeOperation.Action.CUT.toString();
        } else if ("PASTE".equals(str)) {
            return ICodeOperation.Action.PASTE.toString();
        } else if ("UNDO".equals(str)) {
            return ICodeOperation.Action.UNDO.toString();
        } else if ("REDO".equals(str)) {
            return ICodeOperation.Action.REDO.toString();
        } else if ("DIFF".equals(str) || "OFF_EDIT".equals(str)) {
            return ICodeOperation.Action.CONTENT_CHANGE.toString();
        }
        return "NONE";
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
        String author = elem.getAttribute(XmlConstants.AuthorAttr);
        
        CopyOperation op = new CopyOperation(time, pathinfo, author);
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
        String action = parseFileAction(elem.getAttribute(XmlConstants.ActionAttr));
        String author = elem.getAttribute(XmlConstants.AuthorAttr);
        
        FileOperation op = new FileOperation(time, pathinfo, action, author);
        String code = Xml2Operation.getFirstChildText(elem.getElementsByTagName(XmlConstants.CodeElem));
        if (code == null) {
            code = "";
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
        if ("ADDED".equals(str) || "ADDED".equals(str) || "NEW".equals(str)) {
            return FileOperation.Action.ADDED.toString();
        } else if ("OPEN".equals(str) || "OPENED".equals(str)) {
            return FileOperation.Action.OPENED.toString();
        } else if ("CLOSE".equals(str) || "CLOSED".equals(str)) {
            return FileOperation.Action.CLOSED.toString();
        } else if ("SAVE".equals(str) || "SAVED".equals(str)) {
            return FileOperation.Action.SAVED.toString();
        } else if ("DELETE".equals(str) || "DELETED".equals(str)) {
            return FileOperation.Action.REMOVED.toString();
        } else if ("ACT".equals(str) || "ACTIVATE".equals(str) || "ACTIVATED".equals(str)) {
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
        ZonedDateTime time = getTime(Long.parseLong(elem.getAttribute(XmlConstants.TimeAttr)));
        CTPath pathinfo = createPathInfo(elem);
        String author = elem.getAttribute(XmlConstants.AuthorAttr);
        
        CommandOperation op = new CommandOperation(time, pathinfo, author);
        op.setCommandId(elem.getAttribute(LabelAttr));
        return op;
    }
    
    /**
     * Obtains a command operation from the DOM element.
     * @param node the DOM element
     * @return the command operation
     */
    private static FileOperation getResourceOperation(Element elem) {
        String target = elem.getAttribute(TargetAttr);
        String action = parseResourceAction(target, elem.getAttribute(XmlConstants.ActionAttr));
        if (action != null) {
            return null;
        }
        ZonedDateTime time = getTime(Long.parseLong(elem.getAttribute(XmlConstants.TimeAttr)));
        CTPath pathinfo = createPathInfo(elem);
        
        String author = elem.getAttribute(XmlConstants.AuthorAttr);
        FileOperation op = new FileOperation(time, pathinfo, action, author);
        op.setSrcDstPath(elem.getAttribute(APathAttr));
        return op;
    }
    
    /**
     * Obtains the action of a resource operation.
     * @param str the string indicating the action
     * @return the file action, or <code>NONE</code> if none
     */
    static String parseResourceAction(String target, String str) {
        if ("JFILE".equals(target)) {
            if ("ADD".equals(str) || "ADDED".equals(str)) {
                return FileOperation.Action.ADDED.toString();
            } else if ("REMOVE".equals(str) || "REMOVED".equals(str)) {
                return FileOperation.Action.REMOVED.toString();
            } else if ("MOVE_FROM".equals(str) || "MOVED_FROM".equals(str)) {
                return FileOperation.Action.MOVED_FROM.toString();
            } else if ("MOVE_TO".equals(str) || "MOVED_TO".equals(str)) {
                return FileOperation.Action.MOVED_TO.toString();
            } else if ("DELETE".equals(str) || "DELETED".equals(str)) {
                return FileOperation.Action.REMOVED.toString();
            } else if ("RENAME_FROM".equals(str) || "RENAMED_FROM".equals(str)) {
                return FileOperation.Action.RENAMED_FROM.toString();
            } else if ("RENAME_TO".equals(str) || "RENAMED_TO".equals(str)) {
                return FileOperation.Action.RENAMED_TO.toString();
            }
        }
        return null;
    }
}
