/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.repository;

import org.jtool.changetracker.operation.IChangeOperation;
import org.jtool.changetracker.operation.FileOperation;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.time.ZonedDateTime;

/**
 * Represents the history of code change operations.
 * @author Katsuhisa Maruyama
 */
public class OperationHistory {
    
    /**
     * The code change operations stored in the history.
     */
    private List<IChangeOperation> operations;
    
    /**
     * Create an empty instance.
     */
    OperationHistory() {
        operations = new ArrayList<IChangeOperation>();
    }
    
    /**
     * Clears the history of code change operations.
     */
    void clear() {
        operations.clear();
    }
    
    /**
     * Adds code change operations in the history.
     * @param operation the code change operations to be added
     */
    void add(IChangeOperation operation) {
        operations.add(operation);
    }
    
    /**
     * Obtains all the code change operations stored in the history.
     * @return the collection of the code change operations
     */
    List<IChangeOperation> getOperations() {
        return new ArrayList<IChangeOperation>(operations);
    }
    
    /**
     * Returns the size of the history.
     * @return the number of the code change operations stored in the history
     */
    int size() {
        return operations.size();
    }
    
    /**
     * Obtains the first code change operation from the history.
     * @return the first code change operation, or <code>null</code> if none
     */
    IChangeOperation getFirstOperation() {
        if (operations.size() > 0) {
            return operations.get(0);
        }
        return null;
    }
    
    /**
     * Obtains the last code change operation from this operation history.
     * @return the last code change operation, or <code>null</code> if none
     */
    IChangeOperation getLastOperation() {
        if (operations.size() > 0) {
            return operations.get(operations.size() - 1);
        }
        return null;
    }
    
    /**
     * Returns a code change operation at the specified position in the history.
     * @param index the index of the code change operation to be returned
     * @return the code change operation at the specified position 
     */
    IChangeOperation getOperation(int index) {
        if (index < 0 || index >= operations.size()) {
            return null;
        }
        return operations.get(index);
    }
    
    /**
     * Finds a file operation at the restoration point immediately before the specified index.
     * @param index the index of the code change operation
     * @return the file operation at the restoration point, or <code>-1</code> if it was not found
     */
    int getFormerRestorationPoint(int index) {
        for (int idx = index; idx >= 0; idx--) {
            IChangeOperation operation = operations.get(idx);
            if (operation.isFile()) {
                FileOperation foperation = (FileOperation)operation;
                if (foperation.getCode() != null) {
                    return idx;
                }
            }
        }
        return -1;
    }
    
    /**
     * Finds a file operation at the restoration point immediately after the specified index.
     * @param index the index of the code change operation
     * @return the file operation at the restoration point, or <code>-1</code> if it was not found
     */
    int getLatterRestorationPoint(int index) {
        for (int idx = index; idx < operations.size(); idx++) {
            IChangeOperation operation = operations.get(idx);
            if (operation.isFile()) {
                FileOperation foperation = (FileOperation)operation;
                if (foperation.getCode() != null) {
                    return idx;
                }
            }
        }
        return -1;
    }
    
    /**
     * Finds the index of the former code change operation
     * that was performed at the specified time or immediately before within the time period.
     * @param from the index that indicates the start point of the time period
     * @param to the index that indicates the end point of the time period
     * @param time the specified time
     * @return the index of the found operation, or <code>-1</code> if none
     */
    private int getFormerBy(int from, int to, ZonedDateTime time) {
        if (from > to) {
            return -1;
        }
        
        if (time.isBefore(operations.get(from).getTime()) || time.isEqual(operations.get(from).getTime())) {
            return -1;
        }
        
        if (time.isAfter(operations.get(to).getTime())) {
            return to;
        }
        
        if (from + 1 == to) {
            return from;
        }
        
        int mid = (from + to) / 2;
        if (time.isAfter(operations.get(mid).getTime())) {
            return getFormerBy(mid, to, time);
        } else {
            return getFormerBy(from, mid - 1, time);
        }
    }
    
    /**
     * Finds the index of the former code change operation that was performed at the specified time or immediately before.
     * @param time the specified time
     * @return the index of the found operation, or <code>-1</code> if none
     */
    public int getFormerBy(ZonedDateTime time) {
        return getFormerBy(0, operations.size() - 1, time);
    }
    
    /**
     * Finds the index of the latter code change operation
     * that was performed at the specified time or immediately after within the time period.
     * @param from the index that indicates the start point of the time period
     * @param to the index that indicates the end point of the time period
     * @param time the specified time
     * @return the index of the found operation, or <code>-1</code> if none
     */
    private int getLatterBy(int from, int to, ZonedDateTime time) {
        if (from > to) {
            return -1;
        }
        
        if (time.isBefore(operations.get(from).getTime())) {
            return from;
        }
        if (time.isAfter(operations.get(to).getTime()) || time.isEqual(operations.get(to).getTime())) {
            return -1;
        }
        if (from == to - 1) {
            return to;
        }
        
        int mid = (from + to) / 2;
        if (time.isBefore(operations.get(mid).getTime())) {
            return getLatterBy(from, mid, time);
        } else {
            return getLatterBy(mid + 1, to, time);
        }
    }
    
    /**
     * Finds the index of the latter code change operation that was performed at the specified time or immediately after.
     * @param time the specified time
     * @return the sequence number of the found operation, or <code>-1</code> if none
     */
    public int getLatterBy(ZonedDateTime time) {
        return getLatterBy(0, operations.size() - 1, time);
    }
    
    /**
     * Finds the index of the code change operation that was performed at the specified time within the time period.
     * @param from the index that indicates the start point of the time period
     * @param to the index that indicates the end point of the time period
     * @param time the specified time
     * @return the index of the found operation, or <code>-1</code> if none
     */
    private int getBy(int from, int to, ZonedDateTime time) {
        if (from > to) {
            return -1;
        }
        
        int mid = (from + to) / 2;
        ZonedDateTime t = operations.get(mid).getTime();
        
        if (t.isEqual(time)) {
            return mid;
            
        } else if (t.isBefore(time)) {
            if (mid + 1 >= operations.size()) {
                return -1;
            }
            return getBy(mid + 1, to, time);
            
        } else {
            if (mid - 1 < 0) {
                return -1;
            }
            return getBy(from, mid - 1, time);
        }
    }
    
    /**
     * Finds the index of the code change operation that was performed at the specified time.
     * @param time the specified time
     * @return the index of the found operation, or <code>-1</code> if none
     */
    public int getBy(ZonedDateTime time) {
        return getBy(0, operations.size() - 1, time);
    }
    
    /**
     * Obtains the contents of source code restored at the time when a specified code change operation was performed.
     * @param index the index of the code change operation at the restoration point
     * @return the contents of restored source code, <code>null</code> if the restoration fails
     */
    public String restore(int index) {
        int idx = getFormerRestorationPoint(index);
        if (idx == -1) {
            return null;
        }
        
        FileOperation op  = (FileOperation)operations.get(idx);
        return CodeRestorer.applyOperations(operations, op.getCode(), idx, index);
    }
    
    /**
     * Obtains the contents of source code restored at the time when a specified code change operation was performed.
     * @param curCode the current code
     * @param curIndex the index of the current code
     * @param index the index of the code change operation at the restoration point
     * @return the contents of restored source code, <code>null</code> if the restoration fails
     */
    public String restore(String curCode, int curIndex, int index) {
        return CodeRestorer.applyOperations(operations, curCode, curIndex, index);
    }
    
    /**
     * Sorts code change operations in time order.
     */
    public void sort() {
        OperationHistory.sort(operations);
    }
    
    /**
     * Sorts code change operations in time order.
     * @param operations the the collection of code change operations
     */
    public static void sort(List<IChangeOperation> operations) {
        Collections.sort(operations, new Comparator<IChangeOperation>() {
            
            /**
             * Compares two code change operations for order.
             * @param operation1 the first code change operation to be compared
             * @param operation2 the second code change operation to be compared
             */
            public int compare(IChangeOperation operation1, IChangeOperation operation2) {
                ZonedDateTime time1 = operation1.getTime();
                ZonedDateTime time2 = operation2.getTime();
                
                if (time1.isAfter(time2)) {
                    return 1;
                } else if (time1.isBefore(time2)) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
    }
    
    /**
     * Forms code change operations.
     * @param ops the collection of code change operations to be formed
     * @return the collection of code change operations
     */
    static List<IChangeOperation> form(List<IChangeOperation> ops) {
        OperationHistory.sort(ops);
        ops = HistoryFabricator.fabricate(ops);
        
        return ops;
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
