/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.replayer.ui;

import org.jtool.changetracker.repository.CTFile;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * A view that displays the history of change operations.
 * @author Katsuhisa Maruyama
 */
public class HistoryView extends ViewPart implements ViewStateChangedListener {
    
    /**
     * The identification string that is used to register this view.
     */
    public static final String ID = "org.jtool.changetracker.replayer.ui.HistoryView";
    
    /**
     * The instance that visualizes change operations.
     */
    protected static OperationVisualizer operationVisualizer;
    
    /**
     * The control for the replay table.
     */
    protected TableControl tableControl;
    
    /**
     * The control for the replay buttons.
     */
    protected ButtonControl buttonControl;
    
    /**
     * Creates an instance of this operation history view.
     * @param manager the replay manager
     */
    public HistoryView() {
        ViewManager.getInstance().addEventListener(this);
        if (ViewManager.getInstance().getChangeExplorerView() != null) {
            operationVisualizer = ViewManager.getInstance().getOperationVisualizer();
        } else {
            ViewManager.getInstance().hideView(this);
        }
    }
    
    /**
     * Creates this operation history view.
     * @param parent the parent control
     */
    @Override
    public void createPartControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.BORDER);
        FormLayout layout = new FormLayout();
        composite.setLayout(layout);
        
        buttonControl = new ButtonControl(this);
        buttonControl.createButtons(composite);
        
        tableControl = new TableControl(this);
        tableControl.createTable(composite);
        
        FormData tcdata = new FormData();
        tcdata.top = new FormAttachment(0, 0);
        tcdata.bottom = new FormAttachment(buttonControl.getControl(), -2);
        tcdata.left = new FormAttachment(0, 0);
        tcdata.right = new FormAttachment(100, 0);
        tableControl.getControl().setLayoutData(tcdata);
    }
    
    /**
     * Sets the focus to this view.
     */
    @Override
    public void setFocus() {
        tableControl.setFocus();
    }
    
    /**
     * Disposes this this view.
     */
    @Override
    public void dispose() {
        if (tableControl != null) {
            tableControl.dispose();
        }
        if (buttonControl != null) {
            buttonControl.dispose();
        }
        ViewManager.getInstance().removeEventListener(this);
        super.dispose();
    }
    
    /**
     * Receives an event that represents the change of replay state.
     * @param evt the event
     */
    @Override
    public void notify(ViewStateChangedEvent evt) {
        ViewStateChangedEvent.Type type = evt.getType();
        if (type.equals(ViewStateChangedEvent.Type.INDEX_CHANGED)) {
            select();
        } if (type.equals(ViewStateChangedEvent.Type.MARK_CHANGED)) {
            mark();
        } else if (type.equals(ViewStateChangedEvent.Type.FILE_OPENED)) {
            open();
        } else if (type.equals(ViewStateChangedEvent.Type.UPDATE)) {
            update();
        } else if (type.equals(ViewStateChangedEvent.Type.RESET)) {
            reset();
        }
    }
    
    /**
     * Selects a change operation in this operation history view.
     */
    protected void select() {
        UIJob job = new UIJob("Select") {
            
            /**
             * Run the job in the UI thread.
             * @param monitor the progress monitor to use to display progress
             */
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                if (!readyToVisualize()) {
                    return Status.CANCEL_STATUS;
                }
                tableControl.select();
                buttonControl.update();
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }
    
    /**
     * Changes the mark states of a change operations in this operation history view.
     */
    protected void mark() {
        UIJob job = new UIJob("Mark") {
            
            /**
             * Run the job in the UI thread.
             * @param monitor the progress monitor to use to display progress
             */
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                if (!readyToVisualize()) {
                    return Status.CANCEL_STATUS;
                }
                tableControl.mark();
                buttonControl.update();
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }
    
    /**
     * Opens this view.
     */
    protected void open() {
        update();
        setFocus();
    }
    /**
     * Updates this view.
     */
    protected void update() {
        UIJob job = new UIJob("Update") {
            
            /**
             * Run the job in the UI thread.
             * @param monitor the progress monitor to use to display progress
             */
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                if (!readyToVisualize()) {
                    return Status.CANCEL_STATUS;
                }
                tableControl.update();
                buttonControl.update();
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }
    
    /**
     * Resets this view.
     */
    protected void reset() {
        UIJob job = new UIJob("Reset") {
            
            /**
             * Run the job in the UI thread.
             * @param monitor the progress monitor to use to display progress
             */
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                if (!readyToVisualize()) {
                    return Status.CANCEL_STATUS;
                }
                tableControl.reset();
                buttonControl.reset();
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }
    
    /**
     * Returns information about a file related to the operation history.
     * @return the file information
     */
    protected CTFile getFile() {
        return operationVisualizer.getFile();
    }
    
    /**
     * Tests if change operations are ready to be visualized.
     * @return <code>true</code> if change operations are ready to be visualized, otherwise <code>false</code>
     */
    protected boolean readyToVisualize() {
        return operationVisualizer != null && operationVisualizer.readyToVisualize();
    }
    
    /**
     * Goes to a specified change operation.
     * @param index the index number of the change operation
     */
    protected void goTo(int index) {
        operationVisualizer.goTo(index);
    }
    
    /**
     * Returns the index number of the present change operation in the operation history.
     * @return the index number of the change operation
     */
    protected int getPresentIndex() {
        return operationVisualizer.getPresentIndex();
    }
    
    /**
     * Returns the index number of the precedent change operation.
     * @return the index number of the precedent change operation, <code>-1</code> if node
     */
    protected int getPrecedentOperationIndex() {
        return operationVisualizer.getPrecedentOperationIndex();
    }
    
    /**
     * Returns the index number of the successive change operation.
     * @return the index number of the successive change operation, <code>-1</code> if node
     */
    protected int getSuccessiveOperationIndex() {
        return operationVisualizer.getSuccessiveOperationIndex();
    }
    
    /**
     * Returns the first change operation.
     * @return the first change operation, <code>-1</code> if node
     */
    protected int getFirstOperationIndex() {
        return operationVisualizer.getFirstOperationIndex();
    }
    
    /**
     * Returns the last change operation.
     * @return the last change operation, <code>-1</code> if node
     */
    protected int getLastOperationIndex() {
        return operationVisualizer.getLastOperationIndex();
    }
    
    /**
     * Finds the index number of a previous change operation with a mark.
     * @return the index number of the previous change operation, or <code>-1</code> if none
     */
    protected int getPreviousMarkedOperationIndex() {
        return operationVisualizer.getPreviousMarkedOperationIndex();
    }
    
    /**
     * Finds the index number of a next change operation with a mark.
     * @return the index number of the next change operation, or <code>-1</code> if none
     */
    protected int getNextMarkedOperationIndex() {
        return operationVisualizer.getNextMarkedOperationIndex();
    }
    
    /**
     * Marks or unmarks all the change operations.
     * @param mark <code>true</code> if the change operations will be marked, otherwise <code>false</code>
     */
    protected void setAllMarks(boolean mark) {
        operationVisualizer.setAllMarks(mark);
    }
    
    /**
     * Return information about marked and unmarked change operations.
     * @return the array that memorizes which change operations are marked
     */
    protected boolean[] getPresentMarks() {
        return operationVisualizer.getPresentMarks();
    }
    
    /**
     * Marks a change operation.
     * @param index the index number of the change operation to be marked
     */
    protected void markOperation(int index) {
        operationVisualizer.markOperation(index);
    }
    
    /**
     * Unmarks a change operation.
     * @param index the index number of the change operation to be unmarked
     */
    protected void unmarkOperation(int index) {
        operationVisualizer.unmarkOperation(index);
    }
}
