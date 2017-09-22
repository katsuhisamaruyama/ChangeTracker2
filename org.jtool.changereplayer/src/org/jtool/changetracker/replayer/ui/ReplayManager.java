/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.replayer.ui;

import org.jtool.changetracker.core.CTConsole;
import org.jtool.changetracker.repository.CTFile;
import org.jtool.changetracker.repository.RepositoryChangedEvent;
import org.jtool.changetracker.repository.RepositoryChangedListener;
import org.jtool.changetracker.operation.IChangeOperation;
import org.jtool.changetracker.replayer.Activator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.progress.UIJob;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.time.ZonedDateTime;

/**
 * A manager that manages the history of change operations.
 * @author Katsuhisa Maruyama
 */
public class ReplayManager implements RepositoryChangedListener {
    
    /**
     * The single instance of this operation history manager.
     */
    private static ReplayManager instance = new ReplayManager();
    
    /**
     * The information about a file related to the operation history.
     */
    private CTFile fileInfo = null;
    
    /**
     * The index number of a present change operation of interest in the operation history.
     */
    private int presentIndex = -1;
    
    /**
     * The present contents of the source code when a present change operation was performed.
     */
    private String presentCode = "";
    
    /**
     * The change explorer view that shows change operations in the operation history.
     */
    private ChangeExplorerView changeExplorerView;
    
    /**
     * An array that memorizes which change operations are marked.
     */
    private boolean[] presentMarks;
    
    /**
     * The collection of listeners that receives an event the replay state change.
     */
    private Set<ReplayStateChangedListener> listeners = new HashSet<ReplayStateChangedListener>();
    
    /**
     * A flag that indicates if change operations are ready to be replayed.
     */
    private boolean readyToReplay = false;
    
    /**
     * The percentage for the visible time range.
     */
    private int timeScale = 100;
    
    /**
     * The storage that memorizing the state of replay.
     */
    private Map<String, ReplayState> replayStates = new HashMap<String, ReplayState>();
    
    /**
     * Creates a singleton replay manager.
     */
    private ReplayManager() {
    }
    
    /**
     * Returns the single instance that manages the repository of change operations.
     * @return the history manager
     */
    public static ReplayManager getInstance() {
        return instance;
    }
    
    /**
     * Initializes this operation history manager.
     */
    public void init() {
        UIJob job = new UIJob("Initialize") {
            
            /**
             * Run the job in the UI thread.
             * @param monitor the progress monitor to use to display progress
             */
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                openChangeExplorerView();
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }
    
    /**
     * Opens the change explorer view.
     */
    private void openChangeExplorerView() {
        try {
            IWorkbenchPage workbenchPage = Activator.getWorkbenchPage();
            workbenchPage.showView(ChangeExplorerView.ID);
            changeExplorerView = (ChangeExplorerView)workbenchPage.findView(ChangeExplorerView.ID);
        } catch (PartInitException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Opens the history view.
     */
    private void openHistoryView() {
        IWorkbenchPage workbenchPage = Activator.getWorkbenchPage();
        boolean exists = false;
        IViewReference[] refs = workbenchPage.getViewReferences();
        for (IViewReference ref : refs) {
            IViewPart view = ref.getView(false);
            if (view instanceof HistoryView) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            try {
                workbenchPage.showView(HistoryView.ID);
                // historyView = (HistoryView)workbenchPage.findView(HistoryView.ID);
            } catch (PartInitException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Opens the code change view.
     */
    private void openCodeChangeView() {
        IWorkbenchPage workbenchPage = Activator.getWorkbenchPage();
        boolean exists = false;
        IViewReference[] refs = workbenchPage.getViewReferences();
        for (IViewReference ref : refs) {
            IViewPart view = ref.getView(false);
            if (view instanceof CodeChangeView) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            try {
                workbenchPage.showView(CodeChangeView.ID);
            } catch (PartInitException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Opens a new code change view.
     */
    void open(CTFile finfo) {
        openHistoryView();
        openCodeChangeView();
        
        if (fileInfo != null) {
            if (fileInfo.getQualifiedName().equals(finfo.getQualifiedName())) {
                return;
            }
            storeReplayState();
        }
        fileInfo = finfo;
        presentMarks = new boolean[fileInfo.getOperations().size()];
        setAllMarks(true);
        fire(ReplayStateChangedEvent.Type.FILE_OPENED);
        restoreReplayState(fileInfo);
        readyToReplay = true;
    }
    
    /**
     * Closes an existing code change view.
     */
    void close() {
        if (readyToReplay()) {
            storeReplayState();
            fire(ReplayStateChangedEvent.Type.RESET);
            fileInfo = null;
            presentIndex = -1;
            presentCode = "";
            presentMarks = null;
            readyToReplay = false;
        }
    }
    
    /**
     * Tests if change operations are ready to be replayed.
     * @return <code>true</code> if change operations are ready to be replayed, otherwise <code>false</code>
     */
    boolean readyToReplay() {
        return readyToReplay;
    }
    
    /**
     * Receives a repository changed event.
     * @param evt the received event
     */
    @Override
    public void notify(RepositoryChangedEvent evt) {
        RepositoryChangedEvent.Type type = evt.getType();
        if (type.equals(RepositoryChangedEvent.Type.OPENED) ||
            type.equals(RepositoryChangedEvent.Type.REFRESHED) ||
            type.equals(RepositoryChangedEvent.Type.LOCATION_CHANGED)) {
            if (changeExplorerView != null) {
                changeExplorerView.update();
            }
            fire(ReplayStateChangedEvent.Type.RESET);
        } else if (type.equals(RepositoryChangedEvent.Type.OPERATION_ADDED)) {
            if (fileInfo != null) {
                if (changeExplorerView != null) {
                    changeExplorerView.update();
                }
                presentMarks = new boolean[fileInfo.getOperations().size()];
                fire(ReplayStateChangedEvent.Type.UPDATE);
            }
        }
    }
    
    /**
     * Returns information about a file related to the operation history.
     * @return the file information
     */
    CTFile getFile() {
        return fileInfo;
    }
    
    /**
     * Sets information about a file related to the operation history.
     * @param finfo the file information
     */
     void setFile(CTFile finfo) {
         fileInfo = finfo;
    }
    
    /**
     * Returns the index number of the present change operation in the operation history.
     * @return the index number of the change operation
     */
    int getPresentIndex() {
        return presentIndex;
    }
    
    /**
     * Returns the time when the present change operation was performed.
     * @return the time of the change operation, or <code>null</code> if the present time is invalid.
     */
    ZonedDateTime getPresentTime() {
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
     * Sets the index number of a present change operation of interest.
     * @param index the index number of the change operation
     * @return <code>true</code> if the index number is successfully set, otherwise <code>false</code>
     */
    boolean setPresentIndex(int index) {
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
        fire(ReplayStateChangedEvent.Type.INDEX_CHANGED);
    }
    
    /**
     * Obtains the contents of the source code when a present change operation was performed.
     * @param index the index number of the change operation
     * @return the contents of the source code, or the empty string if there is no precedent source code found.
     */
    private String getCode(int index) {
        if (fileInfo != null) {
            String code;
            if (index == presentIndex ) {
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
    String getPresentCode() {
        return presentCode;
    }
    
    /**
     * Returns the precedent contents of the source code.
     * @return the precedent source code, or the empty string if there is no precedent source code found.
     */
    String getPrecedentCode() {
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
    String getSucessiveCode() {
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
    void goTo(int index) {
        if (fileInfo == null) {
            return;
        }
        setPresentIndexWithoutCheck(index);
    }
    
    /**
     * Returns the index number of the precedent change operation.
     * @return the index number of the precedent change operation, <code>-1</code> if node
     */
    int getPrecedentOperationIndex() {
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
    int getSuccessiveOperationIndex() {
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
    int getFirstOperationIndex() {
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
    int getLastOperationIndex() {
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
    protected int getPreviousMarkedOperationIndex() {
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
    protected int getNextMarkedOperationIndex() {
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
    boolean[] getPresentMarks() {
        return presentMarks;
    }
    
    /**
     * Marks or unmarks all the change operations.
     * @param mark <code>true</code> if the change operations will be marked, otherwise <code>false</code>
     */
    void setAllMarks(boolean mark) {
        for (int index = 0; index < presentMarks.length; index ++) {
            presentMarks[index] = mark;
        }
    }
    
    /**
     * Marks and unmarks all the change operations.
     * @param marks an array that memorizes which change operations are marked.
     */
    void setAllMarks(boolean[] marks) {
        for (int index = 0; index < marks.length; index++) {
            presentMarks[index] = marks[index];
        }
        fire(ReplayStateChangedEvent.Type.MARK_CHANGED);
    }
    
    /**
     * Marks a change operation.
     * @param index the index number of the change operation to be marked
     */
    void markOperation(int index) {
        if (index < 0 && index >= presentMarks.length) {
            return;
        }
        presentMarks[index] = true;
        fire(ReplayStateChangedEvent.Type.MARK_CHANGED);
    }
    
    /**
     * Unmarks a change operation.
     * @param index the index number of the change operation to be unmarked
     */
    void unmarkOperation(int index) {
        if (index < 0 && index >= presentMarks.length) {
            return;
        }
        presentMarks[index] = false;
        fire(ReplayStateChangedEvent.Type.MARK_CHANGED);
    }
    
    /**
     * Returns the scale for the time range.
     * @return the percentage of the scale
     */
    int getTimeScale() {
        return timeScale;
    }
    
    /**
     * Sets the scale for the time range.
     * @param scale the percentage of the scale
     */
    void setTimeScale(int scale) {
        timeScale = scale;
    }
    
    /**
     * Zooms in the time range.
     */
    void zoominTimeScale() {
        if (timeScale > 20) {
            setTimeScale(timeScale - 20);
        }
    }
    
    /**
     * Zooms out the time range.
     */
    void zoomoutTimeScale() {
        if (timeScale < 1000) {
            setTimeScale(timeScale + 20);
        }
    }
    
    /**
     * Adds the listener to receive an event the replay state change.
     * @param listener the changed listener to be added
     */
    public void addEventListener(ReplayStateChangedListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Removes the listener which no longer receives an event the replay state change.
     * @param listener the changed listener to be removed
     */
    public void removeEventListener(ReplayStateChangedListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Sends the changed event to all the listeners.
     * @param type the type of the event
     */
    void fire(ReplayStateChangedEvent.Type type) {
        ReplayStateChangedEvent evt = new ReplayStateChangedEvent(this, type);
        for (ReplayStateChangedListener listener : listeners) {
            listener.notify(evt);
        }
    }
    
    /**
     * Stores the current state of replay for a file.
     * @param finfo information about the file 
     */
    private void storeReplayState() {
        ReplayState state = new ReplayState(presentIndex, timeScale);
        replayStates.put(fileInfo.getQualifiedName(), state);
    }
    
    /**
     * Restores the state of the source code viewer.
     * @param state the state of the source code viewer
     */
    private void restoreReplayState(CTFile finfo) {
        ReplayState state = replayStates.get(finfo.getQualifiedName());
        if (state != null) {
            setPresentIndex(state.index);
            setTimeScale(state.scale);
        } else {
            setPresentIndex(0);
            setTimeScale(100);
        }
    }
    
    /**
     * Stores the state of replay.
     */
    private class ReplayState {
        
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
        public ReplayState(int index, int scale) {
            this.index = index;
            this.scale = scale;
        }
    }
}
