/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.replayer;

import org.jtool.changetracker.core.Activator;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Tool-bar actions that move the selection of change operations.
 * @author Katsuhisa Maruyama
 */
public class ToolBarAction {
    
    /**
     * A code change view that the tool-bar actions attach to.
     */
    protected CodeChangeView codeChangeView;
    
    /**
     * The action for going backward to the precedent change operation.
     */
    protected Action goPrecAction;
    
    /**
     * The action for going forward to the successive change operation.
     */
    protected Action goSuccAction;
    
    /**
     * The button for going backward over the previous change operations.
     */
    protected Action fastRewindAction;
    
    /**
     * The button for going forward over the next change operations.
     */
    protected Action fastForwardAction;
    
    /**
     * The action for jumping to the first change operation.
     */
    protected Action goFirstAction;
    
    /**
     * The action for jumping to the last change operation.
     */
    protected Action goLastAction;
    
    /**
     * The icon images.
     */
    protected static ImageDescriptor goPrecIcon      = Activator.getImageDescriptor("icons/left1.gif");
    protected static ImageDescriptor goSuccIcon      = Activator.getImageDescriptor("icons/right1.gif");
    protected static ImageDescriptor fastRewindIcon  = Activator.getImageDescriptor("icons/left3.gif");
    protected static ImageDescriptor fastForwardIcon = Activator.getImageDescriptor("icons/right3.gif");
    protected static ImageDescriptor goFirstIcon     = Activator.getImageDescriptor("icons/left2.gif");
    protected static ImageDescriptor goLastIcon      = Activator.getImageDescriptor("icons/right2.gif");
    
    /**
     * Creates tool-bar actions.
     * @param view the code change view that that the button actions attach to
     */
    public ToolBarAction(CodeChangeView view) {
        codeChangeView = view;
    }
    
    /**
     * Creates tool-bar actions that move the selection of change operations.
     */
    public void createActions() {
        goFirstAction = new Action("First") {
            
            /**
             * Runs an action.
             */
            @Override
            public void run() {
                int index = codeChangeView.getFirstOperationIndex();
                if (index != -1) {
                    codeChangeView.goTo(index);
                }
            }
        };
        goFirstAction.setToolTipText("Go to the first change operation");
        goFirstAction.setImageDescriptor(ToolBarAction.goFirstIcon);
        goFirstAction.setEnabled(true);
        
        fastRewindAction = new Action("Rewind") {
            
            /**
             * Runs an action.
             */
            @Override
            public void run() {
                int index = codeChangeView.getPreviousMarkedOperationIndex();
                if (index != -1) {
                    codeChangeView.goTo(index);
                }
            }
        };
        fastRewindAction.setToolTipText("Go to the first change operation");
        fastRewindAction.setImageDescriptor(ToolBarAction.goFirstIcon);
        fastRewindAction.setEnabled(true);
        
        goPrecAction = new Action("Precedent") {
            
            /**
             * Runs an action.
             */
            @Override
            public void run() {
                int index = codeChangeView.getPrecedentOperationIndex();
                if (index != -1) {
                    codeChangeView.goTo(index);
                }
            }
        };
        goPrecAction.setToolTipText("Go to the precedent change operation");
        goPrecAction.setImageDescriptor(ToolBarAction.goPrecIcon);
        goPrecAction.setEnabled(true);
        
        goSuccAction = new Action("Sucessive") {
            
            /**
             * Runs an action.
             */
            @Override
            public void run() {
                int index = codeChangeView.getSuccessiveOperationIndex();
                if (index != -1) {
                    codeChangeView.goTo(index);
                }
            }
        };
        goSuccAction.setToolTipText("Go to the successive change operation");
        goSuccAction.setImageDescriptor(ToolBarAction.goSuccIcon);
        goSuccAction.setEnabled(true);
        
        fastForwardAction = new Action("Forward") {
            
            /**
             * Runs an action.
             */
            @Override
            public void run() {
                int index = codeChangeView.getNextMarkedOperationIndex();
                if (index != -1) {
                    codeChangeView.goTo(index);
                }
            }
        };
        fastForwardAction.setToolTipText("Go to the successive change operation");
        fastForwardAction.setImageDescriptor(ToolBarAction.goSuccIcon);
        fastForwardAction.setEnabled(true);
        
        goLastAction = new Action("Last") {
            
            /**
             * Runs an action.
             */
            @Override
            public void run() {
                int index = codeChangeView.getLastOperationIndex();
                if (index != -1) {
                    codeChangeView.goTo(index);
                }
            }
        };
        goLastAction.setToolTipText("Go to the last change operation");
        goLastAction.setImageDescriptor(ToolBarAction.goLastIcon);
        goLastAction.setEnabled(true);
        
        IToolBarManager manager = codeChangeView.getViewSite().getActionBars().getToolBarManager();
        manager.add(goFirstAction);
        manager.add(fastRewindAction);
        manager.add(goPrecAction);
        manager.add(goSuccAction);
        manager.add(fastForwardAction);
        manager.add(goLastAction);
    }
    
    /**
     * Selects the tool-bar actions.
     */
    public void select() {
        update();
    }
    
    /**
     * Updates the tool-bar actions.
     */
    public void update() {
        int index = codeChangeView.getFirstOperationIndex();
        if (codeChangeView.getPresentIndex() != index) {
            goFirstAction.setEnabled(true);
        } else {
            goFirstAction.setEnabled(false);
        }
        index = codeChangeView.getPreviousMarkedOperationIndex();
        if (index != -1) {
            fastRewindAction.setEnabled(true);
        } else {
            fastRewindAction.setEnabled(false);
        }
        index = codeChangeView.getPrecedentOperationIndex();
        if (index != -1) {
            goPrecAction.setEnabled(true);
        } else {
            goPrecAction.setEnabled(false);
        }
        index = codeChangeView.getSuccessiveOperationIndex();
        if (index != -1) {
            goSuccAction.setEnabled(true);
        } else {
            goSuccAction.setEnabled(false);
        }
        index = codeChangeView.getNextMarkedOperationIndex();
        if (index != -1) {
            fastForwardAction.setEnabled(true);
        } else {
            fastForwardAction.setEnabled(false);
        }
        index = codeChangeView.getLastOperationIndex();
        if (codeChangeView.getPresentIndex() != index) {
            goLastAction.setEnabled(true);
        } else {
            goLastAction.setEnabled(false);
        }
    }
    
    /**
     * Resets the tool-bar actions.
     */
    public void reset() {
        goFirstAction.setEnabled(false);
        goPrecAction.setEnabled(false);
        fastRewindAction.setEnabled(false);
        fastForwardAction.setEnabled(false);
        goSuccAction.setEnabled(false);
        goLastAction.setEnabled(false);
    }
}
