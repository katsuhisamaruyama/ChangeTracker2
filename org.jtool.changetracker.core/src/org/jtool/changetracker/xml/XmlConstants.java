/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.xml;

/**
 * The elements and attributes appearing in XML documents that store the history of change operations.
 * @author Katsuhisa Maruyama
 */
public class XmlConstants {
    
    public static final String HistoryElem    = "OperationHistory";
    public static final String OperationsElem = "operations";
    
    public static final String DocumentOperationElem = "documentOperation";
    public static final String CopyOperationElem     = "copyOperation";
    public static final String FileOperationElem     = "fileOperation";
    public static final String CommandOperationElem  = "commandOperation";
    public static final String RefactorOperationElem = "refactoringOperation";
    public static final String ResourceOperationElem = "resourceOperation";
    
    public static final String InsertedElem = "inserted";
    public static final String DeletedElem  = "deleted";
    public static final String CopiedElem   = "copied";
    public static final String CodeElem     = "code";
    public static final String SelectedElem = "selected";
    
    public static final String VersionAttr = "version";
    
    public static final String TimeAttr         = "time";
    public static final String PathAttr         = "path";
    public static final String BranchAttr       = "branch";
    public static final String ProjectNameAttr  = "project";
    public static final String PackageNameAttr  = "package";
    public static final String ActionAttr       = "action";
    public static final String AuthorAttr       = "author";
    public static final String DescriptionAttr  = "desc";
    public static final String CompoundTimeAttr = "ctime";
    
    public static final String OffsetAttr     = "offset";
    public static final String CharsetAttr    = "charset";
    public static final String SrcDstPathAttr = "srcdst";
    public static final String CommandIdAttr  = "commandId";
    public static final String NameAttr       = "name";
    public static final String ArgumentAttr   = "args";
    public static final String TargetAttr     = "target";
}
