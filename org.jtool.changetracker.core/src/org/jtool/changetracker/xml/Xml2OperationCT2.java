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
import java.util.List;
import java.util.ArrayList;
import java.time.ZonedDateTime;

/**
 * Converts the XML representation of ChangeTracker-v2 into the history of change operations.
 * @author Katsuhisa Maruyama
 */
class Xml2OperationCT2 {
    
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
        op.setInsertedText(Xml2Operation.getFirstChildText(elem.getElementsByTagName(XmlConstants.InsertedElem)));
        op.setDeletedText(Xml2Operation.getFirstChildText(elem.getElementsByTagName(XmlConstants.DeletedElem)));
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
        op.setCopiedText(Xml2Operation.getFirstChildText(elem.getElementsByTagName(XmlConstants.CopiedElem)));
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
        String code = Xml2Operation.getFirstChildCode(elem.getElementsByTagName(XmlConstants.CodeElem));
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
        String code = Xml2Operation.getFirstChildCode(elem.getElementsByTagName(XmlConstants.CodeElem));
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
            String fileName = Xml2Operation.getFileName(path);
            pathinfo = new CTPath(projectName, packageName, fileName, path, branch);
        }
    }
}

