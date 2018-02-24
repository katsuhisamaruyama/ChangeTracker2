/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.repository;

import org.jtool.changetracker.operation.IChangeOperation;
import org.jtool.changetracker.operation.ChangeOperation;
import org.jtool.changetracker.operation.FileOperation;
import org.jtool.changetracker.convert.ConsistencyCheker;
import java.util.List;
import java.util.ArrayList;
import java.time.ZonedDateTime;

/**
 * Represents the history of change operations.
 * @author Katsuhisa Maruyama
 */
public class OperationHistory {
    
    /**
     * The change operations stored in the history.
     */
    private List<IChangeOperation> operations = new ArrayList<IChangeOperation>();;
    
    /**
     * Create an empty instance.
     */
    public OperationHistory() {
    }
    
    /**
     * Create an instance that stores change operations.
     * @param ops the collection of change operations
     */
    public OperationHistory(List<? extends IChangeOperation> ops) {
        operations.addAll(ops);
    }
    
    /**
     * Clears the history of change operations.
     */
    public void clear() {
        operations.clear();
    }
    
    /**
     * Adds a change operation in the history.
     * @param operation the change operation to be added
     */
    public void add(IChangeOperation op) {
        operations.add(op);
    }
    
    /**
     * Adds change operations in the history.
     * @param operation the change operations to be added
     */
    public void addAll(List<? extends IChangeOperation> ops) {
        operations.addAll(ops);
    }
    
    /**
     * Obtains all the change operations stored in the history.
     * @return the collection of the change operations
     */
    public List<IChangeOperation> getOperations() {
        return new ArrayList<IChangeOperation>(operations);
    }
    
    /**
     * Returns the size of the history.
     * @return the number of the change operations stored in the history
     */
    public int size() {
        return operations.size();
    }
    
    /**
     * Obtains the first change operation from the history.
     * @return the first change operation, or <code>null</code> if none
     */
    public IChangeOperation getFirstOperation() {
        if (operations.size() > 0) {
            return operations.get(0);
        }
        return null;
    }
    
    /**
     * Obtains the last change operation from this operation history.
     * @return the last change operation, or <code>null</code> if none
     */
    public IChangeOperation getLastOperation() {
        if (operations.size() > 0) {
            return operations.get(operations.size() - 1);
        }
        return null;
    }
    
    /**
     * Returns a change operation with a given index number in the history.
     * @param index the index number of the change operation to be returned
     * @return the found change operation, or <code>null</code> if none
     */
    public IChangeOperation getOperation(int index) {
        if (index < 0 || index >= operations.size()) {
            return null;
        }
        return operations.get(index);
    }
    
    /**
     * Finds a restoration point immediately before when the specified change operation was performed.
     * @param index the index number of the change operation
     * @return the index number of the file operation at the restoration point, or <code>-1</code> if none
     */
    public int getRestorationIndexBefore(int index) {
        for (int idx = index; idx >= 0; idx--) {
            IChangeOperation op = operations.get(idx);
            if (op.isFile()) {
                FileOperation fop = (FileOperation)op;
                if (fop.getCode() != null) {
                    return idx;
                }
            }
        }
        return -1;
    }
    
    /**
     * Finds a restoration point immediately after when the specified change operation was performed.
     * @param index the index number of the change operation
     * @return the index number of the file operation at the restoration point, or <code>-1</code> if none
     */
    public int getRestorationIndexAfter(int index) {
        for (int idx = index; idx < operations.size(); idx++) {
            IChangeOperation op = operations.get(idx);
            if (op.isFile()) {
                FileOperation fop = (FileOperation)op;
                if (fop.getCode() != null) {
                    return idx;
                }
            }
        }
        return -1;
    }
    
    /**
     * Finds a restoration point immediately before or after when the specified change operation was performed.
     * @param index the index number of the change operation
     * @return the index number of the file operation at the restoration point, or <code>-1</code> if none
     */
    public int getRestorationIndex(int index) {
        int findex = getRestorationIndexBefore(index);
        if (findex != -1) {
            return findex;
        }
        return getRestorationIndexAfter(index);
    }
    
    /**
     * Finds the last change operation that was performed at the specified time or immediately before within the time range.
     * @param from the index number of the starting change operation within the time range
     * @param to the index number of the ending change operation within the time range
     * @param time the specified time
     * @return the index number of the found operation, or <code>-1</code> if none
     */
    public int getLastOperationIndexBefore(int from, int to, ZonedDateTime time) {
        if (from > to) {
            return -1;
        }
        
        for (int idx = to; idx >= from; idx--) {
            IChangeOperation op = operations.get(idx);
            if (op.getTime().isBefore(time) || time.isEqual(op.getTime())) {
                return idx;
            }
        }
        return -1;
    }
    
    /**
     * Finds the last change operation that was performed at the specified time or immediately before.
     * @param time the specified time
     * @return the index number of the found operation, or <code>-1</code> if none
     */
    public int getLastOperationIndexBefore(ZonedDateTime time) {
        return getLastOperationIndexBefore(0, operations.size() - 1, time);
    }
    
    /**
     * Finds the first change operation that was performed at the specified time or immediately after within the time range.
     * @param from the index number of the starting change operation within the time range
     * @param to the index number of the ending change operation within the time range
     * @param time the specified time
     * @return the index number of the found operation, or <code>-1</code> if none
     */
    public int getFirstOperationIndexAfter(int from, int to, ZonedDateTime time) {
        if (from > to) {
            return -1;
        }
        
        for (int idx = from; idx <= to; idx++) {
            IChangeOperation op = operations.get(idx);
            if (op.getTime().isAfter(time) || time.isEqual(op.getTime())) {
                return idx;
            }
        }
        return -1;
    }
    
    /**
     * Finds the first change operation that was performed at the specified time or immediately after within the time range.
     * @param time the specified time
     * @return the index number of the found operation, or <code>-1</code> if none
     */
    public int getFirstOperationIndexAfter(ZonedDateTime time) {
        return getFirstOperationIndexAfter(0, operations.size() - 1, time);
    }
    
    /**
     * Finds the change operation that was performed at the specified time within the time range.
     * @param from the index number of the starting change operation within the time range
     * @param to the index number of the ending change operation within the time range
     * @param time the specified time
     * @return the index number of the found operation, or <code>-1</code> if none
     */
    public int getOperationIndexAt(int from, int to, ZonedDateTime time) {
        if (from > to) {
            return -1;
        }
        
        for (int idx = from; idx <= to; idx++) {
            IChangeOperation op = operations.get(idx);
            if (time.isEqual(op.getTime())) {
                return idx;
            }
        }
        return -1;
    }
    
    /**
     * Finds the change operation that was performed at the specified time.
     * @param time the specified time
     * @return the index number of the found operation, or <code>-1</code> if none
     */
    public int getOperationIndexAt(ZonedDateTime time) {
        return getOperationIndexAt(0, operations.size() - 1, time);
    }
    
    /**
     * Obtains change operations with their index numbers that are between specified two index numbers.
     * @param from the index number of the first operation
     * @param to the index number of the last operation
     * @return the collection of the change operations
     */
    public List<IChangeOperation> getOperations(int from, int to) {
        List<IChangeOperation> ops = new ArrayList<IChangeOperation>();
        for (int idx = from; idx <= to; idx++) {
            ops.add(operations.get(idx));
        }
        return ops;
    }
    
    /**
     * Obtains change operations with their index numbers that are between 0 and a specified index number.
     * @param to the index number of the last operation
     * @return the collection of the change operations
     */
    public List<IChangeOperation> getOperationsBefore(int to) {
        return getOperations(0, to);
    }
    
    /**
     * Obtains change operations with their index numbers that are between 0 and a specified index number.
     * @param from the index number of the first operation
     * @return the collection of the change operations
     */
    public List<IChangeOperation> getOperationsAfter(int from) {
        return getOperations(from, operations.size() - 1);
    }
    
    /**
     * Obtains the contents of source code restored at the time when a specified change operation was performed.
     * @param index the index of the code change operation at the restoration point
     * @return the contents of the restored source code, <code>null</code> if the restoration fails
     */
    public String getCode(int index) {
        int findex = getRestorationIndex(index);
        if (findex == -1) {
            return null;
        }
        
        FileOperation fop  = (FileOperation)getOperations().get(findex);
        return CodeRestorer.applyOperations(this, fop.getCode(), findex, index);
    }
    
    /**
     * Obtains the contents of source code restored at the time when a specified change operation was performed.
     * @param curCode the contents of the current code
     * @param curIndex the index of the current code
     * @param index the index of the code change operation at the restoration point
     * @return the contents of restored source code, <code>null</code> if the restoration fails
     */
    public String getCode(String curCode, int curIndex, int index) {
        IChangeOperation op  = operations.get(index);
        if (op.isFile()) {
            return ((FileOperation)op).getCode();
        }
        return CodeRestorer.applyOperations(this, curCode, curIndex, index);
    }
    
    /**
     * Restores the contents of source code on file operations.
     * @param fop the file operation
     */
    void restoreCodeOnFileOperation(FileOperation fop) {
        if (fop.getCode() == null || fop.getCode().length() == 0) {
            int idx = getOperationIndexAt(fop.getTime());
            if (idx > 0) {
                String code = getCode(idx - 1);
                if (code != null) {
                    fop.setCode(code);
                } else {
                    fop.setCode("");
                }
            }
        }
    }
    
    /**
     * Compacts the history of change operations.
     */
    public void compact() {
        if (operations.size() > 0) {
            operations = OperationCompactor.compact(operations);
        }
    }
    
    /**
     * Checks if change operations are consistent with restored code.
     * @return <code>true</code> if all the change operations are consistent with the restored code, otherwise <code>false</code>
     */
    public boolean checkOperationConsistency() {
        sort();
        return ConsistencyCheker.run(this);
    }
    
    /**
     * Tests if this operation history is the same as a given one.
     * @param resource the operation history
     * @return <code>true</code> if the two operation histories are the same, otherwise <code>false</code>
     */
    public boolean equals(OperationHistory history) {
        if (history == null) {
            return false;
        }
        
        if (size() != history.size()) {
            return false;
        }
        
        List<IChangeOperation> ops1 = getOperations();
        List<IChangeOperation> ops2 = history.getOperations();
        
        for (int i = 0; i < ops1.size(); i++) {
            IChangeOperation op1 = ops1.get(i);
            IChangeOperation op2 = ops2.get(i);
            if (!op1.equals(op2)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Tests if this object is the same as a given object.
     * @param obj the object
     * @return <code>true</code> if the two objects are the same, otherwise <code>false</code>
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OperationHistory) {
            return equals((OperationHistory)obj);
        }
        return false;
    }
    
    /**
     * Returns a hash code value for this object.
     * @return always <code>0</code> that means all objects have the same hash code
     */
    @Override
    public int hashCode() {
        return 0;
    }
    
    /**
     * Sorts change operations stored in this history in time order.
     */
    public void sort() {
        ChangeOperation.sort(operations);
    }
    
    /**
     * Returns the string for printing.
     * @return the string for printing
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("+--------------------------+\n");
        for (IChangeOperation op : operations) {
            buf.append(op.toString());
            buf.append("\n");
        }
        buf.append("+--------------------------+\n");
        
        return buf.toString();
    }
}
