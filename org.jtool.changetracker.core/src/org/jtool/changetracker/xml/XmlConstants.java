/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.xml;

/**
 * The elements, attributes, and values of the attributes appearing in XML documents
 * that store information on the history of code change operations.
 * @author Katsuhisa Maruyama
 */
class XmlConstants {
    
    static final String HistoryElem    = "OperationHistory";
    static final String OperationsElem = "operations";
    
    static final String DocumentOperationElem = "documentOperation";
    static final String CopyOperationElem     = "copyOperation";
    static final String CommandOperationElem  = "commandOperation";
    static final String FileOperationElem     = "fileOperation";
    static final String ResourceOperationElem = "resourceOperation";
    static final String GitOperationElem      = "gitOperation";
    
    static final String InsertedElem = "inserted";
    static final String DeletedElem  = "deleted";
    static final String CopiedElem   = "copied";
    static final String CodeElem     = "code";
    
    static final String VersionAttr     = "version";
    
    static final String TimeAttr        = "time";
    static final String BranchAttr      = "branch";
    static final String PathAttr        = "path";
    static final String ActionAttr      = "action";
    static final String AuthorAttr      = "author";
    static final String DescriptionAttr = "desc";
    static final String BundleAttr      = "bid";
    
    static final String OffsetAttr        = "offset";
    static final String SrcDstPathAttr    = "srcdst";
    static final String AddedFilesAttr    = "added";
    static final String RemovedFilesAttr  = "removed";
    static final String ModifiedFilesAttr = "removed";
}
