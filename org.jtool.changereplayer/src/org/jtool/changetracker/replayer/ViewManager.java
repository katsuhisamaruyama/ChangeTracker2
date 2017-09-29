/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.replayer;

import org.jtool.changetracker.core.Activator;
import org.jtool.changetracker.replayer.ViewStateChangedEvent;
import org.jtool.changetracker.repository.CTFile;
import org.jtool.changetracker.repository.RepositoryEvent;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import java.util.Set;
import java.util.HashSet;

/**
 * Manages views for visualization.
 * @author Katsuhisa Maruyama
 */
public class ViewManager {
    
    /**
     * The single instance of this view manager.
     */
    private static ViewManager instance = new ViewManager();
    
    /**
     * The operation visualizer binding to this view manager.
     */
    private OperationVisualizer operationVisualizer;
    
    /**
     * The view of the change explorer binding to this view manager.
     */
    private ChangeExplorerView changeExplorerView;
    
    /**
     * The collection of listeners that receives an event the view state change.
     */
    private Set<ViewStateChangedListener> listeners = new HashSet<ViewStateChangedListener>();
    
    /**
     * The collection of views to be opened when a file is specified.
     */
    private Set<String> views = new HashSet<String>();
    
    /**
     * Prohibits the creation of an instance.
     */
    private ViewManager() {
    }
    
    /**
     * Returns the single instance for the view manager.
     * @return the view manager
     */
    public static ViewManager getInstance() {
        return instance;
    }
    
    /**
     * Sets the operation visualizer binding to this view manager.
     * @param visualizer the operation visualizer
     */
    void setOperationVisualizer(OperationVisualizer visualizer) {
        operationVisualizer = visualizer;
    }
    
    /**
     * Returns the operation visualizer binding to this view manager.
     * @return the operation visualizer
     */
    OperationVisualizer getOperationVisualizer() {
        return operationVisualizer;
    }
    
    /**
     * Sets the change explorer binding to this view manager.
     * @param view the view of the operation the change explorer
     */
    void setChangeExplorerView(ChangeExplorerView view) {
        changeExplorerView = view;
    }
    
    /**
     * Returns the change explorer binding to this view manager.
     * @return the view of the operation the change explorer
     */
    ChangeExplorerView getChangeExplorerView() {
        return changeExplorerView;
    }
    
    /**
     * Adds the identification string for indicating a view to be opened.
     * @param viewid the identification string
     */
    void addView(String viewid) {
        views.add(viewid);
    }
    
    /**
     * Shows views for a file.
     * @param finfo information about the file
     */
    void show(CTFile finfo) {
        for (String viewid : views) {
            showView(viewid);
        }
        if (operationVisualizer != null) {
            operationVisualizer.open(finfo);
        }
    }
    
    /**
     * Opens a view.
     * @param viewid the identification string indicating a view to be opened
     * @return the view, or <code>null</code> if the view could not be opened
     */
    IViewPart showView(String viewid) {
        try {
            IWorkbenchPage workbenchPage = Activator.getWorkbenchPage();
            return workbenchPage.showView(viewid);
        } catch (PartInitException e) {
            return null;
        }
    }
    
    /**
     * Finds a view.
     * @param viewid the identification string for a view to be found
     * @return the view, or <code>null</code> if the view could not be found
     */
    IViewPart findView(String viewid) {
        IWorkbenchPage workbenchPage = Activator.getWorkbenchPage();
        return workbenchPage.findView(viewid);
    }
    
    /**
     * Closes a view.
     * @param view a view to be closed
     */
    void hideView(IViewPart view) {
        IWorkbenchPage workbenchPage = Activator.getWorkbenchPage();
        workbenchPage.hideView(view);
    }
    
    /**
     * Closes a view.
     * @param viewid the identification string for a view to be closed
     */
    void hideView(String viewid) {
        IViewPart view = findView(viewid);
        if (view != null) {
            hideView(view);
        }
    }
    
    /**
     * Closes this operation visualizer.
     */
    void close() {
        IWorkbenchPage workbenchPage = Activator.getWorkbenchPage();
        for (String viewid : views) {
            IViewPart view = workbenchPage.findView(viewid);
            if (view != null) {
                hideView(view);
            }
        }
    }
    
    /**
     * Invoked before a repository change event is about to occur.
     * @param evt the sent event
     */
    void aboutTo(RepositoryEvent evt) {
    }
    
    /**
     * Invoked after a repository change event occurred.
     * @param evt the sent event
     */
    void changed(RepositoryEvent evt) {
        RepositoryEvent.Type type = evt.getType();
        if (type.equals(RepositoryEvent.Type.OPEN) || type.equals(RepositoryEvent.Type.REFRESH)) {
            if (changeExplorerView != null) {
                changeExplorerView.refresh();
            }
            fire(ViewStateChangedEvent.Type.RESET);
        } else if (type.equals(RepositoryEvent.Type.OPERATION_ADD)) {
            if (changeExplorerView != null) {
                changeExplorerView.update();
            }
            if (operationVisualizer != null && operationVisualizer.getFile() != null) {
                fire(ViewStateChangedEvent.Type.UPDATE);
            }
        }
    }
    
    /**
     * Adds the listener to receive an event the replay state change.
     * @param listener the changed listener to be added
     */
    void addEventListener(ViewStateChangedListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Removes the listener which no longer receives an event the replay state change.
     * @param listener the changed listener to be removed
     */
    void removeEventListener(ViewStateChangedListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Sends the changed event to all the listeners.
     * @param type the type of the event
     */
    void fire(ViewStateChangedEvent.Type type) {
        ViewStateChangedEvent evt = new ViewStateChangedEvent(this, type);
        for (ViewStateChangedListener listener : listeners) {
            listener.notify(evt);
        }
    }
}
