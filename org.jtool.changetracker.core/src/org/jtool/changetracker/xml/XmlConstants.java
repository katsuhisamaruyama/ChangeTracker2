/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.xml;

/**
 * The elements and attributes appearing in XML documents that store the history of change operations.
 * @author Katsuhisa Maruyama
 */
class XmlConstants {
    
    static final String HistoryElem    = "OperationHistory";
    static final String OperationsElem = "operations";
    
    static final String DocumentOperationElem = "documentOperation";
    static final String CopyOperationElem     = "copyOperation";
    static final String FileOperationElem     = "fileOperation";
    static final String CommandOperationElem  = "commandOperation";
    static final String RefactorOperationElem = "refactoringOperation";
    
    static final String InsertedElem = "inserted";
    static final String DeletedElem  = "deleted";
    static final String CopiedElem   = "copied";
    static final String CodeElem     = "code";
    static final String SelectedElem = "selected";
    
    static final String VersionAttr = "version";
    
    static final String TimeAttr         = "time";
    static final String PathAttr         = "path";
    static final String BranchAttr       = "branch";
    static final String ProjectNameAttr  = "project";
    static final String PackageNameAttr  = "package";
    static final String ActionAttr       = "action";
    static final String AuthorAttr       = "author";
    static final String DescriptionAttr  = "desc";
    static final String CompoundTimeAttr = "ctime";
    
    static final String OffsetAttr      = "offset";
    static final String CharsetAttr     = "charset";
    static final String SrcDstPathAttr  = "srcdst";
    static final String CommandIdAttr   = "commandId";
    static final String NameAttr        = "name";
    static final String ArgumentAttr    = "args";
}
