/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.convert;

import org.jtool.changetracker.operation.DocumentOperation;
import org.jtool.changetracker.repository.CTPath;
import org.jtool.changetracker.xml.XmlConstants;
import org.jtool.changetracker.xml.Xml2OperationOR;
import java.time.ZonedDateTime;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.OutputKeys;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.io.StringWriter;

/**
 * Stores information about an operation that denote a difference between two contents of source code.
 * @author Katsuhisa Maruyama
 */
public class DiffOperation extends DocumentOperation {
    
    /**
     * Creates an instance storing information about this diff operation.
     * @param time the time when the diff operation was artificially performed
     * @param pathinfo information about path of a resource on which the copy operation was performed
     */
    public DiffOperation(ZonedDateTime time, CTPath pathinfo) {
        super(time, pathinfo, "FIXED", "ChangeTracker");
    }
    
    /**
     * Obtains the XML representation of this diff operation.
     * @return a string for the XML representation
     */
    public String toXML() {
        try {
            DocumentBuilderFactory bfactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = bfactory.newDocumentBuilder();
            Document doc = builder.newDocument();
            
            Element elem = doc.createElement(Xml2OperationOR.NormalOperationElem);
            doc.appendChild(elem);
            elem.setAttribute(Xml2OperationOR.CCPAttr, "NO");
            elem.setAttribute(Xml2OperationOR.FileAttr, pathinfo.getPath());
            elem.setAttribute("hash", "");
            elem.setAttribute(XmlConstants.OffsetAttr, String.valueOf(start));
            elem.setAttribute(XmlConstants.TimeAttr, String.valueOf(getTimeAsLong()));
            
            Element insElem = doc.createElement(XmlConstants.InsertedElem);
            elem.appendChild(insElem);
            insElem.appendChild(doc.createTextNode(insertedText));
            Element delElem = doc.createElement(XmlConstants.DeletedElem);
            elem.appendChild(delElem);
            delElem.appendChild(doc.createTextNode(deletedText));
            
            StringWriter writer = new StringWriter();
            TransformerFactory tfactory = TransformerFactory.newInstance(); 
            Transformer transformer = tfactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return getElementXML(writer.toString());
            
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return "";
    }
    
    /**
     * Obtains the XML representation of the element for this diff operation.
     * @param str the whole of the XML representation
     * @return a string for the XML representation of the element
     */
    private String getElementXML(String str) {
        int index = str.indexOf('\n');
        return str.substring(index + 1);
    }
}
