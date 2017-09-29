/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.ui;

import org.jtool.changetracker.operation.ChangeOperation;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.CompareViewerSwitchingPane;
import org.eclipse.compare.IEncodedStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * A view that shows the results of comparing between two contents of code.
 * @author Katsuhisa Maruyama
 */
public class CodeComparingView extends CodeChangeView {
    
    /**
     * The identification string that is used to register this view.
     */
    public static final String ID = "org.jtool.changetracker.replayer.ui.CodeComparingView";
    
    /**
     * The identification string that is used to register a pop-up menu on this code viewer.
     */
    public static String POPUP_ID = "org.jtool.changetracker.replayer.ui.CodeComparingView.popup";
    
    /**
     * The viewer that displays differences between codes before and after a change
     */
    protected CompareViewerSwitchingPane compareView;
    
    /**
     * Creates a code view.
     */
    public CodeComparingView() {
        super();
    }
    
    /**
     * Creates a code viewer.
     * @return the control for the created code viewer
     */
    @Override
    protected Control createCodeView(Composite parent) {
        final CompareConfiguration compareConfiguration = new CompareConfiguration();
        compareConfiguration.setLeftLabel("Before the change"); 
        compareConfiguration.setLeftEditable(false);
        compareConfiguration.setRightLabel("After the change");
        compareConfiguration.setRightEditable(false);
        compareConfiguration.setProperty(CompareConfiguration.IGNORE_WHITESPACE, Boolean.FALSE);
        compareView = new CompareViewerSwitchingPane(parent, SWT.BORDER | SWT.FLAT) {
            
            /**
             * Returns a viewer which is able to display the given input.
             * @param oviewer the currently installed viewer or <code>null</code>
             * @param input the input object for which a viewer must be determined or <code>null</code>
             * @return a viewer for the given input, or <code>null</code> if no viewer can be determined
             */
            protected Viewer getViewer(Viewer oviewer, Object input) {
                String name = getFile().getName();
                String timeStr = ChangeOperation.getFormatedTime(getPresentTime());
                Viewer viewer = CompareUI.findContentViewer(oviewer, input, this, compareConfiguration);
                viewer.getControl().setData(CompareUI.COMPARE_VIEWER_TITLE, name + "  -  " + timeStr);
                return viewer;
            }
        };
        compareView.setContent(compareView.getViewer().getControl());
        return compareView;
    }
    
    /**
     * Obtains a menu manager attached to the code viewer.
     * @return the menu manager
     */
    protected MenuManager getMenuManager() {
        MenuManager menuManager = new MenuManager();
        menuManager.setRemoveAllWhenShown(true);
        Menu menu = menuManager.createContextMenu(compareView);
        compareView.setMenu(menu);
        getSite().registerContextMenu(POPUP_ID, menuManager, compareView.getViewer());
        return menuManager;
    }
    
    /**
     * Selects the code viewer.
     */
    @Override
    protected void selectCodeViewer() {
        updateCodeViewer();
    }
    
    /**
     * Updates the code viewer.
     */
    @Override
    protected void updateCodeViewer() {
        if (!compareView.isDisposed()) {
            String before = getPrecedentCode();
            String after = getPresentCode();
            TypedElement left = new TypedElement(before);
            TypedElement right = new TypedElement(after);
            compareView.setInput(new DiffNode(left, right));
        }
    }
    
    /**
     * Resets the code viewer.
     */
    @Override
    protected void resetCodeViewer() {
        if (!compareView.isDisposed()) {
            TypedElement left = new TypedElement("");
            TypedElement right = new TypedElement("");
            compareView.setInput(new DiffNode(left, right));
        }
    }
    
    /**
     * Defines an element to be compared.
     */
    protected class TypedElement implements ITypedElement, IEncodedStreamContentAccessor {
        
        /**
         * The contents of this element
         */
        protected String contents;
        
        /**
         * Creates an element.
         * @param contents contents of this element
         */
        public TypedElement(String contents) {
            this.contents = contents;
        }
        
        /**
         * Returns the name of this element.
         * @return always the empty string
         */
        @Override
        public String getName() {
            return "";
        }
        
        /**
         * Returns the image for this element.
         * @return always <code>null</code>
         */
        @Override
        public Image getImage() {
            return null;
        }
        
        /**
         * Returns the type of this element.
         * @return always the empty string
         */
        @Override
        public String getType() {
            return "JAVA";
        }
        
        /**
         * Returns an open input stream for this element containing the contents of this element.
         * @return the input stream
         * @exception CoreException if the contents of this object could not be accessed
         */
        @Override
        public InputStream getContents() {
            try {
                return new ByteArrayInputStream(contents.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                return new ByteArrayInputStream(contents.getBytes());
            }
        }
        
        /**
         * Returns the name of a char-set encoding to be used 
         * @return always UTF-8
         * @exception CoreException if an error happens while determining the char-set
         */
        @Override
        public String getCharset() throws CoreException {
            return "UTF-8";
        }
    }
}
