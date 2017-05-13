/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.dependencyanalyzer;

import org.jtool.changetracker.repository.ChangeTrackerFile;
import org.jtool.changetracker.operation.IChangeOperation;
import java.util.List;
import java.util.ArrayList;
import java.time.ZonedDateTime;
import java.time.ZoneId;

/**
 * Stores information about a parse-able snapshot.
 * @author Katsuhisa Maruyama
 */
public class ParseableSnapshot {
    
    /**
     * The file path of this parse-able snapshot.
     */
    private ChangeTrackerFile fileInfo;
    
    /**
     * The index number of a change operation that generates this snapshot.
     */
    private int index;
    
    /**
     * The contents of source code for this parse-able snapshot.
     */
    private String code;
    
    /**
     * The collections of Java constructs within this parse-able snapshot.
     */
    private List<JavaConstruct> constructs;
    
    /**
     * The previous parse-able snapshot of this snapshot.
     */
    private ParseableSnapshot prevParseableSnapshot = null;
    
    /**
     * The next parse-able snapshot of this snapshot.
     */
    private ParseableSnapshot nextParseableSnapshot = null;
    
    /**
     * Creates an instance that stores information about this parse-able snapshot.
     * @param finfo information about a file for this parse-able snapshot
     * @param index the index number of the change operation that generates this parse-able snapshot
     * @param code the contents of source code for the this parse-able snapshot
     * @param cons Java constructs within this parse-able snapshot
     */
    public ParseableSnapshot(ChangeTrackerFile finfo,int index, String code, List<JavaConstruct> cons) {
        this.fileInfo = finfo;
        this.index = index;
        this.code = code;
        this.prevParseableSnapshot = finfo.getLastSnapshot();
        if (prevParseableSnapshot != null) {
            prevParseableSnapshot.setNextParseableSnapshot(this);
        }
        this.constructs = cons;
    }
    
    /**
     * Returns information about a file for this parse-able snapshot.
     * @return the information about the file
     */
    public ChangeTrackerFile getFile() {
        return fileInfo;
    }
    
    /**
     * Returns the path of the file for this parse-able snapshot.
     * @return the path of the file.
     */
    public String getPath() {
        return fileInfo.getPath();
    }
    
    /**
     * Returns the time when this parse-able snapshot was generated.
     * @return the time of this parse-able snapshot,
     * or <code>0001/01/01 00:00:00.000</code> if the snapshot appears before no change operation is recorded
     */
    public ZonedDateTime getTime() {
        IChangeOperation op = fileInfo.getOperation(index);
        if (op != null) {
            return op.getTime();
        }
        return ZonedDateTime.of(0, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault());
    }
    
    /**
     * Returns the index number of the change operation that generates this parse-able snapshot
     * @return the index number
     */
    public int getIndex() {
        return index;
    }
    
    /**
     * Returns the source code for this parse-able snapshot.
     * @return the source code
     */
    public String getCode() {
        return code;
    }
    
    /**
     * Returns a previous snapshot of this parse-able snapshot.
     * @return the previous snapshot
     */
    public ParseableSnapshot getPrevParseableSnapshot() {
        return prevParseableSnapshot;
    }
    
    /**
     * Returns a next snapshot of this parse-able snapshot.
     * @return the next snapshot
     */
    public ParseableSnapshot getNextParseableSnapshot() {
        return nextParseableSnapshot;
    }
    
    /**
     * Sets a next snapshot of this parse-able snapshot.
     * @param sn the next snapshot
     */
    public void setNextParseableSnapshot(ParseableSnapshot sn) {
        nextParseableSnapshot = sn;
    }
    
    /**
     * Returns the Java constructs within this parse-able snapshot.
     * @return the collection of the Java constructs
     */
    public List<JavaConstruct> getJavaConstructs() {
        return constructs;
    }
    
    /**
     * Returns the Java class members within this parse-able snapshot.
     * @return the collection of the Java class members
     */
    public List<JavaConstruct> getJavaClassMembers() {
        List<JavaConstruct> cons = new ArrayList<JavaConstruct>();
        for (JavaConstruct con : constructs) {
            if (con.isMethod() || con.isField() || con.isInnerClass()) {
                cons.add(con);
            }
        }
        return cons;
    }
}
