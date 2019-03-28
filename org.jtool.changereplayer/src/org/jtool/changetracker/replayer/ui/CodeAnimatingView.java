/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.replayer.ui;

import org.jtool.changetracker.operation.IChangeOperation;
import org.jtool.changetracker.operation.DocumentOperation;
import org.jtool.changetracker.operation.CopyOperation;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jdt.internal.ui.text.SimpleJavaSourceViewerConfiguration;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import java.util.List;
import java.util.ArrayList;

/**
 * A view that shows animated changes of code.
 * @author Katsuhisa Maruyama
 */
@SuppressWarnings({ "restriction" })
public class CodeAnimatingView extends CodeChangeView {
    
    /**
     * The identification string that is used to register this view.
     */
    public static final String ID = "org.jtool.changetracker.replayer.ui.CodeAnimatingView";
    
    /**
     * The identification string that is used to register a pop-up menu on this code viewer.
     */
    public static String POPUP_ID = "org.jtool.changetracker.replayer.ui.CodeAnimatedView.popup";
    
    /**
     * The viewer that displays the animated source code.
     */
    protected JavaSourceViewer sourceViewer;
    
    /**
     * The configuration of this code viewer.
     */
    protected JavaSourceViewerConfiguration sourceViewerConf;
    
    /**
     * The objects coloring the code.
     */
    protected static final Color BLACK = new Color(null, 0x00, 0x00, 0x00);
    protected static final Color GRAY = new Color(null, 0xaa, 0xaa, 0xaa);
    protected static final Color RED = new Color(null, 0xff, 0xd1, 0xe8);
    protected static final Color YELLOW = new Color(null, 0xff, 0xff, 0xc6);
    protected static final Color BLUE = new Color(null, 0xd1, 0xe8, 0xff);
    protected static final Color WHITE = new Color(null, 0xff, 0xff, 0xff);
    
    /**
     * Creates a code view.
     */
    public CodeAnimatingView() {
        super();
    }
    
    /**
     * Creates a code viewer.
     * @return the control for the created code viewer
     */
    @Override
    protected Control createCodeView(Composite parent) {
        IDocument document = new Document();
        JavaTextTools tools = JavaPlugin.getDefault().getJavaTextTools();
        tools.setupJavaDocumentPartitioner(document, IJavaPartitions.JAVA_PARTITIONING);
        sourceViewerConf = new SimpleJavaSourceViewerConfiguration(tools.getColorManager(),
                JavaPlugin.getDefault().getPreferenceStore(), null, IJavaPartitions.JAVA_PARTITIONING, false);
        
        sourceViewer = new JavaSourceViewer(parent, null, null, false,
                SWT.BORDER | SWT.MULTI | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL,
                JavaPlugin.getDefault().getPreferenceStore());
        sourceViewer.setEditable(false);
        sourceViewer.configure(sourceViewerConf);
        sourceViewer.setDocument(document);
        
        StyledText styledText = sourceViewer.getTextWidget();
        styledText.setFont(JFaceResources.getFont(PreferenceConstants.EDITOR_TEXT_FONT));
        
        if (getFile() != null) {
            updateCodeViewer();
        }
        
        return sourceViewer.getControl();
    }
    
    /**
     * Rolls back the configuration of the code viewer.
     */
    protected void unconfigure() {
        sourceViewer.unconfigure();
    }
    
    /**
     * Obtains a menu manager attached to the code viewer.
     * @return the menu manager
     */
    protected MenuManager getMenuManager() {
        MenuManager menuManager = new MenuManager();
        menuManager.setRemoveAllWhenShown(true);
        Menu menu = menuManager.createContextMenu(sourceViewer.getControl());
        sourceViewer.getControl().setMenu(menu);
        getSite().registerContextMenu(POPUP_ID, menuManager, sourceViewer);
        return menuManager;
    }
    
    /**
     * Obtains the current selection on the code.
     * @return the selection on the code, or <code>null</code> if the selection does not indicate a text
     */
    protected TextSelection getSelection() {
        ISelection selection = sourceViewer.getSelection();
        if (selection instanceof TextSelection) {
            return (TextSelection)selection;
        }
        return null;
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
    public void updateCodeViewer() {
        String code = getPresentCode();
        sourceViewer.getTextWidget().setText(code);
        decorateCode(code);
    }
    
    /**
     * Resets the code viewer.
     */
    @Override
    protected void resetCodeViewer() {
        sourceViewer.getTextWidget().setText("");
    }
    
    /**
     * Decorates the representation of the code displayed on the code viewer.
     * @param code the content of the code
     */
    protected void decorateCode(String code) {
        StyledText styledText = sourceViewer.getTextWidget();
        styledText.setStyleRange(null);
        sourceViewer.configure(sourceViewerConf);
        
        if (code != null) {
            List<StyleRange> ranges = getColoredStyleRanges(code);
            int leftmost = -1;
            if (ranges.size() > 0) {
                leftmost = ranges.get(0).start;
            }
            
            for (StyleRange range : ranges) {
                styledText.setStyleRange(range);
            }
            
            reveal(leftmost, code);
        }
    }
    
    /**
     * Obtains the colored style ranges for the decorated code.
     * @param code the code to be decorated
     * @return the collection of the colored style ranges
     */
    protected List<StyleRange> getColoredStyleRanges(String code) {
        List<StyleRange> ranges = new ArrayList<StyleRange>();
        List<IChangeOperation> ops = getFile().getOperations();
        int idx = getPresentIndex();
        
        IChangeOperation op = ops.get(idx);
        if (!op.isDocumentOrCopy()) {
            op = null;
        }
        IChangeOperation opn = null;
        if (idx + 1 < ops.size()) {
            opn = ops.get(idx + 1);
            if (!opn.isDocumentOrCopy()) {
                opn = null;
            }
        }
        
        if (op != null) {
            if (op.isDocument()) {
                DocumentOperation dop = (DocumentOperation)op;
                int start = dop.getStart();
                int len = dop.getInsertedText().length();
                
                if (len > 0 && 0 <= start && start + len < code.length()) {
                    StyleRange range = new StyleRange(start, len, BLACK, RED);
                    ranges.add(range);
                }
                
            } else if (op.isCopy()) {
                CopyOperation cop = (CopyOperation)op;
                int start = cop.getStart();
                int len = cop.getCopiedText().length();
                
                if (len > 0 && 0 <= start && start + len < code.length()) {
                    StyleRange range = new StyleRange(start, len, BLACK, YELLOW);
                    ranges.add(range);
                }
            }
        }
        
        if (opn != null) {
            if (opn.isDocument()) {
                DocumentOperation dop = (DocumentOperation)opn;
                int start = dop.getStart();
                int len = dop.getDeletedText().length();
                
                if (len > 0 && 0 <= start && start + len < code.length()) {
                    StyleRange range = new StyleRange(start, len, BLACK, BLUE);
                    ranges.add(range);
                }
            }
        }
        
        return ranges;
    }
    
    /**
     * Ensures that the character at a given offset is visible, scrolling the control if necessary.
     * @param offset the offset value of the visible character
     * @param code the content of the code
     */
    protected void reveal(int offset, String code) {
        if (offset < 0 || code.length() <= offset) {
            return;
        }
        
        StyledText styledText = sourceViewer.getTextWidget();
        Point selection = styledText.getSelectionRange();
        
        int line = styledText.getLineAtOffset(offset);
        int top = 0;
        if (line - 10 > top) {
            top = styledText.getOffsetAtLine(line - 10);
        }
        
        int bline = styledText.getLineCount() - 1;
        int bottom = styledText.getOffsetAtLine(bline);
        if (line + 10 < bline) {
            bottom = styledText.getOffsetAtLine(line + 10);
        }
        
        if (top <= selection.x && selection.x + selection.y <= bottom) {
            return;
        }
        
        styledText.setSelection(bottom);
    }
}
