/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.replayer;

import org.jtool.changetracker.core.CTConsole;
import org.jtool.changetracker.repository.CTFile;
import org.jtool.changetracker.repository.IRepositoryListener;
import org.jtool.changetracker.repository.RepositoryEvent;
import org.jtool.changetracker.operation.IChangeOperation;
import org.jtool.changetracker.replayer.ViewStateChangedEvent;

import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.time.ZonedDateTime;

/**
 * Provides facilities that are needed to visualize change operations.
 * @author Katsuhisa Maruyama
 */
public class OperationVisualizer implements IRepositoryListener {
    
    /**
     * The information about a file related to the operation history.
     */
    protected CTFile fileInfo = null;
    
    /**
     * The index number of a present change operation of interest in the operation history.
     */
    protected int presentIndex = -1;
    
    /**
     * The present contents of the source code when a present change operation was performed.
     */
    protected String presentCode = "";
    
    /**
     * An array that memorizes which change operations are marked.
     */
    protected boolean[] presentMarks;
    
    /**
     * The percentage for the visible time range.
     */
    protected int timeScale = 100;
    
    /**
     * The storage that memorizing the state of replay.
     */
    protected Map<String, ViewState> viewStates = new HashMap<String, ViewState>();
    
    /**
     * The collection of views to be opened when a file is specified.
     */
    protected Set<String> views = new HashSet<String>();
    
    /**
     * The collection of listeners that receives an event the replay state change.
     */
    protected Set<ViewStateChangedListener> listeners = new HashSet<ViewStateChangedListener>();
    
    /**
     * A flag that indicates if change operations are ready to be visualized.
     */
    protected boolean readyToVisualize = false;
    
    /**
     * Prohibits the creation of an instance.
     */
    protected OperationVisualizer() {
        ViewManager.getInstance().setOperationVisualizer(this);
    }
    
    /**
     * Opens this operation visualizer.
     * @param finfo information about the file
     */
    public void open(CTFile finfo) {
        if (fileInfo != null && !fileInfo.getQualifiedName().equals(finfo.getQualifiedName())) {
            storeViewState(fileInfo);
        }
        fileInfo = finfo;
        
        presentMarks = new boolean[fileInfo.getOperations().size()];
        setAllMarks(true);
        
        readyToVisualize = true;
        ViewManager.getInstance().fire(ViewStateChangedEvent.Type.FILE_OPENED);
        restoreViewState(fileInfo);
    }
    
    /**
     * Closes this operation visualizer.
     */
    public void close() {
        if (readyToVisualize()) {
            storeViewState(fileInfo);
            ViewManager.getInstance().fire(ViewStateChangedEvent.Type.RESET);
            
            fileInfo = null;
            presentIndex = -1;
            presentCode = "";
            presentMarks = null;
            readyToVisualize = false;
        }
    }
    
    /**
     * Tests if change operations are ready to be visualized.
     * @return <code>true</code> if change operations are ready to be visualized, otherwise <code>false</code>
     */
    public boolean readyToVisualize() {
        return readyToVisualize;
    }
    
    /**
     * Updates the array that memorizes which change operations are marked.
     */
    public void updatePresentMarks() {
        boolean[] presentMarks2 = new boolean[fileInfo.getOperations().size()];
        for (int i = 0; i < presentMarks.length; i++) {
            presentMarks2[i] = presentMarks[i];
        }
        presentMarks = null;
        presentMarks = presentMarks2;
    }
    
    /**
     * Returns information about a file related to the operation history.
     * @return the file information
     */
    public CTFile getFile() {
        return fileInfo;
    }
    
    /**
     * Returns the index number of the present change operation in the operation history.
     * @return the index number of the change operation
     */
    public int getPresentIndex() {
        return presentIndex;
    }
    
    /**
     * Sets the index number of a present change operation of interest.
     * @param index the index number of the change operation
     * @return <code>true</code> if the index number is successfully set, otherwise <code>false</code>
     */
    public boolean setPresentIndex(int index) {
        if (fileInfo == null) {
            return false;
        }
        if (index < 0 || index >= fileInfo.getOperations().size()) {
            return false;
        }
        
        setPresentIndexWithoutCheck(index);
        return true;
    }
    
    /**
     * Sets the index number of a present change operation of interest.
     * This method is intended to be called after checking the index number to be set.
     * @param index the index number of the change operation
     */
    private void setPresentIndexWithoutCheck(int index) {
        presentCode = getCode(index);
        presentIndex = index;
        ViewManager.getInstance().fire(ViewStateChangedEvent.Type.INDEX_CHANGED);
    }
    
    /**
     * Returns the time when the present change operation was performed.
     * @return the time of the change operation, or <code>null</code> if the present time is invalid.
     */
    public ZonedDateTime getPresentTime() {
        if (fileInfo == null || presentIndex < 0) {
            return null;
        }
        
        IChangeOperation op = fileInfo.getOperation(presentIndex);
        if (op != null) {
            return op.getTime();
        }
        return null;
    }
    
    /**
     * Obtains the contents of the source code when a present change operation was performed.
     * @param index the index number of the change operation
     * @return the contents of the source code, or the empty string if there is no precedent source code found.
     */
    public String getCode(int index) {
        if (fileInfo != null) {
            String code;
            if (index == presentIndex) {
                code = presentCode;
            } else if (index == presentIndex - 1) {
                code = fileInfo.getCode(presentCode, presentIndex, presentIndex - 1);
            } else if (index == presentIndex + 1) {
                code = fileInfo.getCode(presentCode, presentIndex, presentIndex + 1);
            } else {
                code = fileInfo.getCode(index);
            }
            if (code != null) {
                return code;
            } else {
                CTConsole.println("### Error occurred during the replay = " + index);
            }
        }
        return "";
    }
    
    /**
     * Returns the present contents of the source code.
     * @return the present source code.
     */
    public String getPresentCode() {
        return presentCode;
    }
    
    /**
     * Returns the precedent contents of the source code.
     * @return the precedent source code, or the empty string if there is no precedent source code found.
     */
    public String getPrecedentCode() {
        if (fileInfo != null && 0 < presentIndex && presentIndex < fileInfo.getOperations().size()) {
            String code = fileInfo.getCode(presentCode, presentIndex, presentIndex - 1);
            if (code != null) {
                return code;
            }
        }
        return "";
    }
    
    /**
     * Returns the successive contents of the source code.
     * @return the successive source code, or the empty string if there is no successive source code found.
     */
    public String getSucessiveCode() {
        if (fileInfo != null && 0 <= presentIndex && presentIndex < fileInfo.getOperations().size() - 1) {
            String code = fileInfo.getCode(presentCode, presentIndex, presentIndex + 1);
            if (code != null) {
                return code;
            }
        }
        return "";
    }
    
    /**
     * Goes to a specified change operation.
     * @param index the index number of the change operation
     */
    public void goTo(int index) {
        if (fileInfo == null) {
            return;
        }
        setPresentIndexWithoutCheck(index);
    }
    
    /**
     * Returns the index number of the precedent change operation.
     * @return the index number of the precedent change operation, <code>-1</code> if node
     */
    public int getPrecedentOperationIndex() {
        if (fileInfo == null) {
            return -1;
        }
        if (presentIndex > 0) {
            return presentIndex - 1;
        } else {
            return -1;
        }
    }
    
    /**
     * Returns the index number of the successive change operation.
     * @return the index number of the successive change operation, <code>-1</code> if node
     */
    public int getSuccessiveOperationIndex() {
        if (fileInfo == null) {
            return -1;
        }
        if (presentIndex < fileInfo.getOperations().size() - 1) {
            return presentIndex + 1;
        } else {
            return -1;
        }
    }
    
    /**
     * Returns the first change operation.
     * @return the first change operation, <code>-1</code> if node
     */
    public int getFirstOperationIndex() {
        if (fileInfo == null) {
            return -1;
        }
        if (fileInfo.getOperations().size() > 0) {
            return 0;
        } else {
            return -1;
        }
    }
    
    /**
     * Returns the last change operation.
     * @return the last change operation, <code>-1</code> if node
     */
    public int getLastOperationIndex() {
        if (fileInfo == null) {
            return -1;
        }
        int size = fileInfo.getOperations().size();
        if (size > 0) {
            return size - 1;
        } else {
            return -1;
        }
    }
    
    /**
     * Finds the index number of a previous change operation with a mark.
     * @return the index number of the previous change operation, or <code>-1</code> if none
     */
    public int getPreviousMarkedOperationIndex() {
        for (int idx = presentIndex - 1; idx >= 0; idx--) {
            if (presentMarks[idx]) {
                return idx;
            }
        }
        return -1;
    }
    
    /**
     * Finds the index number of a next change operation with a mark.
     * @return the index number of the next change operation, or <code>-1</code> if none
     */
    public int getNextMarkedOperationIndex() {
        for (int idx = presentIndex + 1 ; idx < presentMarks.length; idx++) {
            if (presentMarks[idx]) {
                return idx;
            }
        }
        return -1;
    }
    
    /**
     * Return information about marked and unmarked change operations.
     * @return the array that memorizes which change operations are marked
     */
    public boolean[] getPresentMarks() {
        return presentMarks;
    }
    
    /**
     * Marks or unmarks all the change operations.
     * @param mark <code>true</code> if the change operations will be marked, otherwise <code>false</code>
     */
    public void setAllMarks(boolean mark) {
        for (int index = 0; index < presentMarks.length; index ++) {
            presentMarks[index] = mark;
        }
    }
    
    /**
     * Marks and unmarks all the change operations.
     * @param marks an array that memorizes which change operations are marked.
     */
    public void setAllMarks(boolean[] marks) {
        for (int index = 0; index < marks.length; index++) {
            presentMarks[index] = marks[index];
        }
        
        ViewManager.getInstance().fire(ViewStateChangedEvent.Type.MARK_CHANGED);
    }
    
    /**
     * Marks a change operation.
     * @param index the index number of the change operation to be marked
     */
    public void markOperation(int index) {
        if (index < 0 && index >= presentMarks.length) {
            return;
        }
        presentMarks[index] = true;
        
        ViewManager.getInstance().fire(ViewStateChangedEvent.Type.MARK_CHANGED);
    }
    
    /**
     * Unmarks a change operation.
     * @param index the index number of the change operation to be unmarked
     */
    public void unmarkOperation(int index) {
        if (index < 0 && index >= presentMarks.length) {
            return;
        }
        presentMarks[index] = false;
        
        ViewManager.getInstance().fire(ViewStateChangedEvent.Type.MARK_CHANGED);
    }
    
    /**
     * Returns the scale for the time range.
     * @return the percentage of the scale
     */
    public int getTimeScale() {
        return timeScale;
    }
    
    /**
     * Sets the scale for the time range.
     * @param scale the percentage of the scale
     */
    public void setTimeScale(int scale) {
        timeScale = scale;
    }
    
    /**
     * Zooms in the time range.
     */
    public void zoominTimeScale() {
        if (timeScale > 20) {
            setTimeScale(timeScale - 20);
        }
    }
    
    /**
     * Zooms out the time range.
     */
    public void zoomoutTimeScale() {
        if (timeScale < 1000) {
            setTimeScale(timeScale + 20);
        }
    }
    
    /**
     * Stores the current state of this view for each file.
     * @param finfo information about the file
     * 
     */
    protected void storeViewState(CTFile finfo) {
        ViewState state = new ViewState(presentIndex, timeScale);
        viewStates.put(finfo.getQualifiedName(), state);
    }
    
    /**
     * Restores the current state of this view for each file.
     * @param finfo information about the file
     */
    protected void restoreViewState(CTFile finfo) {
        ViewState state = viewStates.get(finfo.getQualifiedName());
        if (state != null) {
            setPresentIndex(state.index);
            setTimeScale(state.scale);
        } else {
            setPresentIndex(0);
            setTimeScale(100);
        }
    }
    
    /**
     * Adds the identification string for indicating a view to be opened.
     * @param viewid the identification string
     */
    public void addView(String viewid) {
        ViewManager.getInstance().addView(viewid);
    }
    
    /**
     * Invoked before a repository change event is about to occur.
     * @param evt the sent event
     */
    public void aboutTo(RepositoryEvent evt) {
        ViewManager.getInstance().aboutTo(evt);
    }
    
    /**
     * Invoked after a repository change event occurred.
     * @param evt the sent event
     */
    public void changed(RepositoryEvent evt) {
        ViewManager.getInstance().changed(evt);
    }
}

/**
 * Stores the state of replay.
 */
class ViewState {
    
    /**
     * The index number of a change operation.
     */
    int index;
    
    /**
     * The percentage for the visible time range.
     */
    int scale;
    
    /**
     * Creates the state of replay.
     * @param index the index number of a change operation
     * @param scale the percentage of the scale
     */
    ViewState(int index, int scale) {
        this.index = index;
        this.scale = scale;
    }
}
