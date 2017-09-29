/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.sample;

import org.jtool.changetracker.replayer.ui.CodeComparingView;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A view that shows the results of comparing between two contents of code.
 * @author Katsuhisa Maruyama
 */
public class CodeComparingViewSample extends CodeComparingView {
    
    /**
     * The identification string that is used to register this view.
     */
    public static final String ID = "org.jtool.changetracker.sample.CodeComparingViewSample";
    
    /**
     * Creates a code view.
     */
    public CodeComparingViewSample() {
        super();
    }
    
    /**
     * Creates a code viewer.
     * @return the control for the created code viewer
     */
    @Override
    protected Control createCodeView(Composite parent) {
        Control control = super.createCodeView(parent);
        
        MenuManager manager = getMenuManager();
        manager.addMenuListener(new IMenuListener() {
            
            /**
             * Receives the event when the menu is about to be shown by the given menu manager.
             * @param manager the menu manager
             */
            @Override
            public void menuAboutToShow(IMenuManager manager) {
                manager.add(new LabelAction("Label"));
                manager.add(new TextSelectionAction("Select"));
            }
        });
        return control;
    }
    
    /**
     * An action for selecting text.
     */
    private class TextSelectionAction extends Action {
        
        /**
         * Creates an instance for performing the backward slicing.
         * @param text the text displayed as a menu item
         */
        public TextSelectionAction(String text) {
            super();
            
            setText(text);
            setEnabled(true);
        }
        
        /**
         * Performs the menu action.
         */
        @Override
        public void run() {
            System.out.println("SELECT");
        }
    }
}
