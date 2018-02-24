/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.xml;

import org.jtool.changetracker.core.CTConsole;
import org.jtool.changetracker.operation.IChangeOperation;
import org.jtool.changetracker.operation.ChangeOperation;
import org.jtool.changetracker.operation.CopyOperation;
import org.jtool.changetracker.operation.DocumentOperation;
import org.jtool.changetracker.operation.FileOperation;
import org.jtool.changetracker.operation.CommandOperation;
import org.jtool.changetracker.operation.RefactoringOperation;
import org.jtool.changetracker.operation.ResourceOperation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.List;

/**
 * Converts the history of change operations into its XML representation.
 * @author Katsuhisa Maruyama
 */
public class Operation2Xml {
    
    /**
     * Converts the change operations into the XML representation.
     * @param operations the collections of change operations to be converted
     * @param filename the name of the history file, or <code>null</code> when failures in writing a file
     */
    public static boolean storeOperations(List<IChangeOperation> operations, String filename) {
        Document doc = getXML(operations);
        if (doc == null) {
            return false;
        }
        XmlFileManager.writeXML(doc, filename);
        return true;
    }
    
    /**
     * Converts the change operations into the XML representation.
     * @param operations the collections of change operations to be converted
     * @return the DOM instance that has the XML representation, or <code>null</code> if the conversion failed
     */
    private static Document getXML(List<IChangeOperation> operations) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();
            generateTree(doc, operations);
            return doc;
            
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Generates the DOM tree corresponding from change operations.
     * @param the DOM instance that has the XML representation
     * @param operations the collections of change operations to be converted
     */
    private static void generateTree(Document doc, List<IChangeOperation> operations) {
        Element rootElem = doc.createElement(XmlConstants.HistoryElem);
        rootElem.setAttribute(XmlConstants.VersionAttr, Xml2Operation.HISTORY_VERSION2);
        doc.appendChild(rootElem);
        Element operationsElem = doc.createElement(XmlConstants.OperationsElem);
        rootElem.appendChild(operationsElem);
        for (IChangeOperation op : operations) {
            createOperationsElement(doc, operationsElem, op);
        }
    }
    
    /**
     * Creates a DOM element corresponding to a change operation.
     * @param the DOM instance that has the XML representation
     * @param parent the parent of the DOM element
     * @param operation the change operation
     */
    private static void createOperationsElement(Document doc, Element parent, IChangeOperation operation) {
        if (operation.isDocument()) {
            Element opElem = appendDocumentOperationElement(doc, (DocumentOperation)operation);
            parent.appendChild(opElem);
        } else if (operation.isCopy()) {
            Element opElem = appendCopyOperationElement(doc, (CopyOperation)operation);
            parent.appendChild(opElem);
        } else if (operation.isFile()) {
            Element opElem = appendFileOperationElement(doc, (FileOperation)operation);
            parent.appendChild(opElem);
        } else if (operation.isCommand()) {
            Element opElem = appendCommandOperationElement(doc, (CommandOperation)operation);
            parent.appendChild(opElem);
        } else if (operation.isRefactor()) {
            Element opElem = appendRefactoringOperationElement(doc, (RefactoringOperation)operation);
            parent.appendChild(opElem);
        } else if (operation.isResource()) {
            Element opElem = appendResourceOperationElement(doc, (ResourceOperation)operation);
            parent.appendChild(opElem);
        } else {
            CTConsole.println("Unknown operation");
        }
    }
    
    /**
     * Creates a DOM element corresponding to a document operation.
     * @param the DOM instance that has the XML representation
     * @param operation the document operation
     * @return the DOM element corresponding to the document operation
     */
    private static void setOperationElement(Document doc, Element elem, IChangeOperation operation) {
        elem.setAttribute(XmlConstants.TimeAttr, ChangeOperation.getTimeAsString(operation.getTime()));
        elem.setAttribute(XmlConstants.PathAttr, operation.getPath());
        elem.setAttribute(XmlConstants.BranchAttr, operation.getBranch());
        elem.setAttribute(XmlConstants.ProjectNameAttr, operation.getProjectName());
        elem.setAttribute(XmlConstants.PackageNameAttr, operation.getPackageName());
        elem.setAttribute(XmlConstants.ActionAttr, operation.getAction());
        elem.setAttribute(XmlConstants.AuthorAttr, operation.getAuthor());
        elem.setAttribute(XmlConstants.DescriptionAttr, operation.getDescription());
        elem.setAttribute(XmlConstants.CompoundTimeAttr, ChangeOperation.getTimeAsString(operation.getCompoundTime()));
    }
    
    /**
     * Creates a DOM element corresponding to a document operation.
     * @param the DOM instance that has the XML representation
     * @param op the document operation
     * @return the DOM element corresponding to the document operation
     */
    private static Element appendDocumentOperationElement(Document doc, DocumentOperation op) {
        Element elem = doc.createElement(XmlConstants.DocumentOperationElem);
        setOperationElement(doc, elem, op);
        
        elem.setAttribute(XmlConstants.OffsetAttr, String.valueOf(op.getStart()));
        Element insElem = doc.createElement(XmlConstants.InsertedElem);
        elem.appendChild(insElem);
        insElem.appendChild(doc.createTextNode(op.getInsertedText()));
        Element delElem = doc.createElement(XmlConstants.DeletedElem);
        elem.appendChild(delElem);
        delElem.appendChild(doc.createTextNode(op.getDeletedText()));
        return elem;
    }
    
    /**
     * Creates a DOM element corresponding to a copy operation.
     * @param the DOM instance that has the XML representation
     * @param op the copy operation
     * @return the DOM element corresponding to the copy operation
     */
    private static Element appendCopyOperationElement(Document doc, CopyOperation op) {
        Element elem = doc.createElement(XmlConstants.CopyOperationElem);
        setOperationElement(doc, elem, op);
        
        elem.setAttribute(XmlConstants.OffsetAttr, String.valueOf(op.getStart()));
        Element copiedElem = doc.createElement(XmlConstants.CopiedElem);
        elem.appendChild(copiedElem);
        copiedElem.appendChild(doc.createTextNode(op.getCopiedText()));
        return elem;
    }
    
    /**
     * Creates a DOM element corresponding to a file operation.
     * @param the DOM instance that has the XML representation
     * @param op the file operation
     * @return the DOM element corresponding to the file operation
     */
    private static Element appendFileOperationElement(Document doc, FileOperation op) {
        Element elem = doc.createElement(XmlConstants.FileOperationElem);
        setOperationElement(doc, elem, op);
        
        elem.setAttribute(XmlConstants.CharsetAttr, op.getCharset());
        elem.setAttribute(XmlConstants.SrcDstPathAttr, op.getSrcDstPath());
        String code = op.getCode();
        if (code != null) {
            Element codeElem = doc.createElement(XmlConstants.CodeElem);
            elem.appendChild(codeElem);
            codeElem.appendChild(doc.createTextNode(code));
        }
        return elem;
    }
    
    /**
     * Creates a DOM element corresponding to a command operation.
     * @param the DOM instance that has the XML representation
     * @param op the command operation
     * @return the DOM element corresponding to the command operation
     */
    private static Element appendCommandOperationElement(Document doc, CommandOperation op) {
        Element elem = doc.createElement(XmlConstants.CommandOperationElem);
        setOperationElement(doc, elem, op);
        
        elem.setAttribute(XmlConstants.CommandIdAttr, op.getCommandId());
        return elem;
    }
    
    /**
     * Creates a DOM element corresponding to a refactoring operation.
     * @param the DOM instance that has the XML representation
     * @param op the refactoring operation
     * @return the DOM element corresponding to the refactoring operation
     */
    private static Element appendRefactoringOperationElement(Document doc, RefactoringOperation op) {
        Element elem = doc.createElement(XmlConstants.RefactorOperationElem);
        setOperationElement(doc, elem, op);
        
        elem.setAttribute(XmlConstants.NameAttr, op.getName());
        elem.setAttribute(XmlConstants.OffsetAttr, String.valueOf(op.getSelectionStart()));
        elem.setAttribute(XmlConstants.ArgumentAttr, op.getArgumentText());
        Element selElem = doc.createElement(XmlConstants.SelectedElem);
        elem.appendChild(selElem);
        selElem.appendChild(doc.createTextNode(op.getSelectedText()));
        return elem;
    }
    
    /**
     * Creates a DOM element corresponding to a resource operation.
     * @param the DOM instance that has the XML representation
     * @param op the resource operation
     * @return the DOM element corresponding to the resource operation
     */
    private static Element appendResourceOperationElement(Document doc, ResourceOperation op) {
        Element elem = doc.createElement(XmlConstants.ResourceOperationElem);
        setOperationElement(doc, elem, op);
        
        elem.setAttribute(XmlConstants.TargetAttr, op.getTarget());
        elem.setAttribute(XmlConstants.SrcDstPathAttr, op.getSrcDstPath());
        return elem;
    }
}
