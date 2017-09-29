/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.replayer;

import org.jtool.changetracker.repository.CTFile;
import org.jtool.changetracker.operation.IChangeOperation;
import org.jtool.changetracker.operation.ICodeOperation;
import org.jtool.changetracker.operation.DocumentOperation;
import org.jtool.changetracker.operation.CopyOperation;
import org.jtool.changetracker.operation.FileOperation;
import org.jtool.changetracker.operation.CommandOperation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import java.util.List;

/**
 * A viewer for a table that displays change operations and select them.
 * @author Katsuhisa Maruyama
 */
public class TableControl {
    
    /**
     * A history view that contains this table control.
     */
    protected HistoryView historyView;
    
    /**
     * The table the displays change operations.
     */
    protected Table operationTable;
    
    /**
     * The listener that receives an event related to the selection of table items.
     */
    protected SelectionListener selectionListener;
    
    /**
     * The listener that receives an event related to the key press and release.
     */
    protected KeyListener keyListener;
    
    /**
     * The listener that receives an event related to the traverse.
     */
    protected TraverseListener traverseListener;
    
    /**
     * The listener that receives an event that marks a table item.
     */
    protected Listener checkListener;
    
    /**
     * Creates a control of the replay table.
     * @param view the history view that contains the table control
     */
    public TableControl(HistoryView view) {
        historyView = view;
    }
    
    /**
     * Creates a table.
     * @param parent the parent control
     */
    public void createTable(Composite parent) {
        operationTable = new Table(parent, SWT.BORDER | SWT.SINGLE | SWT.CHECK | SWT.VIRTUAL | SWT.H_SCROLL | SWT.V_SCROLL);
        operationTable.setLinesVisible(true);
        operationTable.setHeaderVisible(true);
        TableColumn idColumn = new TableColumn(operationTable, SWT.LEFT);
        idColumn.setText("seq no");
        idColumn.setWidth(50);
        idColumn.setResizable(true);
        TableColumn timeColumn = new TableColumn(operationTable, SWT.LEFT);
        timeColumn.setText("time");
        timeColumn.setWidth(200);
        timeColumn.setResizable(true);
        TableColumn detailsColumn = new TableColumn(operationTable, SWT.LEFT);
        detailsColumn.setText("contents");
        detailsColumn.setWidth(400);
        detailsColumn.setResizable(true);
        
        selectionListener = new SelectionListenerImpl();
        operationTable.addSelectionListener(selectionListener);
        checkListener = new CheckListenerImpl();
        operationTable.addListener(SWT.Selection, checkListener);
        keyListener = new KeyListenerImpl();
        operationTable.addKeyListener(keyListener);
        traverseListener = new TraverseListenerImpl();
        operationTable.addTraverseListener(traverseListener);
    }
    
    /**
     * Returns the control for the table.
     * @return the buttons control
     */
    public Control getControl() {
        return operationTable;
    }
    
    /**
     * Sets the focus to the control of the table viewer.
     */
    public void setFocus() {
        operationTable.setFocus();
    }
    
    /**
     * Disposes the control of the table viewer.
     */
    public void dispose() {
        if (!operationTable.isDisposed()) {
            operationTable.removeSelectionListener(selectionListener);
            operationTable.removeKeyListener(keyListener);
            operationTable.removeTraverseListener(traverseListener);
            operationTable.removeListener(SWT.Selection, checkListener);
        }
        operationTable.dispose();
    }
    
    /**
     * Selects a change operation in the table viewer.
     */
    public void select() {
        reveal();
        operationTable.select(historyView.getPresentIndex());
        setFocus();
    }
    
    /**
     * Changes the mark states of a change operations in the table viewer.
     */
    public void mark() {
        boolean[] marks = historyView.getPresentMarks();
        for (int index = 0; index < marks.length; index++) {
            TableItem item = operationTable.getItem(index);
            item.setChecked(marks[index]);
        }
    }
    
    /**
     * Updates the table viewer.
     */
    public void update() {
        if (!historyView.readyToVisualize()) {
            return;
        }
        
        CTFile finfo = historyView.getFile();
        List<IChangeOperation> ops = finfo.getOperations();
        if (ops.size() == 0) {
            return;
        }
        createTableItems(ops);
        operationTable.deselectAll();
        operationTable.update();
        int index = historyView.getPresentIndex();
        if (index < 0) {
            operationTable.select(0);
        } else {
            operationTable.select(index);
        }
    }
    
    /**
     * Resets the table viewer.
     */
    public void reset() {
        if (!operationTable.isDisposed()) {
            operationTable.removeAll();
            operationTable.update();
        }
    }
    
    /**
     * Assigns information about change operations into their respective table items.
     * @param ops the collection of change operations to be displayed
     */
    protected void createTableItems(List<IChangeOperation> ops) {
        boolean[] marks = historyView.getPresentMarks();
        operationTable.removeAll();
        for (int index = 0; index < ops.size(); index++) {
            IChangeOperation op = ops.get(index);
            TableItem item = new TableItem(operationTable, SWT.NONE);
            item.setText(0, String.valueOf(index + 1));
            item.setText(1, op.getFormatedTime());
            item.setText(2, createOperationTextualRepresentation(op));
            item.setChecked(marks[index]);
        }
    }
    
    /**
     * Creates the textual representation for a change operation.
     * @param op the change operation
     * @return the string representing the change operation
     */
    protected String createOperationTextualRepresentation(IChangeOperation op) {
        if (op.isDocument()) {
            return createDocumentOperationTextualRepresentation((DocumentOperation)op);
        } else if (op.isCopy()) {
            return createCopyOperationTextualRepresentation((CopyOperation)op);
        } else if (op.isFile()) {
            return createFileOperationTextualRepresentation((FileOperation)op);
        } else if (op.isCommand()) {
            return createCommandOperationTextualRepresentation((CommandOperation)op);
        }
        return "";
    }
    
    /**
     * Creates the textual representation for a document operation.
     * @param op the document operation
     * @return the string representing the document operation
     */
    protected String createDocumentOperationTextualRepresentation(DocumentOperation op) {
        StringBuilder buf = new StringBuilder();
        if (!ICodeOperation.Action.TYPING.equals(op.getAction())) {
            buf.append(op.getAction().toString());
        }
        buf.append(op.getAction().toString());
        buf.append(" ");
        buf.append(String.valueOf(op.getStart()));
        buf.append(" ");
        if (op.getInsertedText().length() > 0) {
            buf.append("ins[");
            buf.append(getShortText(op.getInsertedText()));
            buf.append("] ");
        }
        if (op.getDeletedText().length() > 0) {
            buf.append("del[");
            buf.append(getShortText(op.getDeletedText()));
            buf.append("]");
        }
        return buf.toString();
    }
    
    /**
     * Creates textual representation for a copy operation.
     * @param op the copy operation
     * @return the string representing the copy operation
     */
    protected String createCopyOperationTextualRepresentation(CopyOperation op) {
        StringBuilder buf = new StringBuilder();
        buf.append(op.getAction().toString());
        buf.append(" ");
        buf.append(String.valueOf(op.getStart()));
        buf.append(" copied[");
        buf.append(getShortText(op.getCopiedText()));
        buf.append("]");
        return buf.toString();
    }
    
    /**
     * Creates textual representation for a file operation.
     * @param op the file operation
     * @return the string representing the file operation
     */
    protected String createFileOperationTextualRepresentation(FileOperation op) {
        StringBuilder buf = new StringBuilder();
        buf.append(op.getAction().toString());
        return buf.toString();
    }
    
    /**
     * Creates textual representation for a command operation.
     * @param op the command operation
     * @return the string representing the command operation
     */
    protected String createCommandOperationTextualRepresentation(CommandOperation op) {
        StringBuilder buf = new StringBuilder();
        buf.append(op.getAction().toString());
        buf.append(" ");
        buf.append(op.getCommandId());
        return buf.toString();
    }
    
    /**
     * Returns the shorten text.
     * @param text the original text
     * @return the text consists of several characters not including the new line
     */
    protected String getShortText(String text) {
        final int LESS_LEN = 9;
        String text2;
        if (text.length() < LESS_LEN + 1) {
            text2 = text;
        } else {
            text2 = text.substring(0, LESS_LEN + 1) + "...";
        }
        return text2.replace('\n', '~');
    }
    
    /**
     * Ensures that the selected change operation is visible, scrolling the table if necessary.
     */
    protected void reveal() {
        int index = historyView.getPresentIndex();
        int size = historyView.getFile().getOperations().size();
        Rectangle area = operationTable.getClientArea();
        int num = area.height / operationTable.getItemHeight() - 1;
        int top = operationTable.getTopIndex();
        if (index < top) {
            if (size < num / 2) {
                operationTable.setTopIndex(top);
            } else {
                operationTable.setTopIndex(top - num / 2);
            }
        } else if (index >= top + num) {
            top = index - num + 1;
            if (size < num / 2) {
                operationTable.setTopIndex(top);
            } else {
                operationTable.setTopIndex(top + num / 2);
            }
        }
    }
    
    /**
     * Deals with a selection event.
     */
    protected class SelectionListenerImpl implements SelectionListener {
        
        /**
         * Creates a listener that deals with a selection event.
         */
        SelectionListenerImpl() {
        }
        
        /**
         * Receives the selection event when the default selection occurs in the control.
         * @param evt the event containing information about the selection
         */
        @Override
        public void widgetDefaultSelected(SelectionEvent evt) {
        }
        
        /**
         * Receives the selection event when selection occurs in the control.
         * @param evt the event containing information about the selection
         */
        @Override
        public void widgetSelected(SelectionEvent evt) {
            Table table = (Table)evt.getSource();
            int index = (int)table.getSelectionIndex();
            historyView.goTo(index);
        }
    }
    
    /**
     * Deals with a check event.
     */
    protected class CheckListenerImpl implements Listener {
        
        /**
         * Creates a listener that deals with a check event.
         */
        CheckListenerImpl() {
        }
        
        /**
         * Receives the selection event when an item is checked or unchecked.
         * @param evt the event containing information about the check
         */
        @Override
        public void handleEvent(Event evt) {
            if (evt.detail == SWT.CHECK) {
                if (evt.item instanceof TableItem) {
                    TableItem item = (TableItem)evt.item;
                    if (item.getChecked()) {
                        historyView.markOperation(evt.index);
                    } else {
                        historyView.unmarkOperation(evt.index);
                    }
                }
            }
        }
    }
    
    /**
     * Deals with a key event.
     */
    protected class KeyListenerImpl implements KeyListener {
        
        /**
         * Creates a listener that deals with a key event.
         */
        KeyListenerImpl() {
        }
        
        /**
         * Receives the key event when a key is pressed in the control.
         * @param evt the event containing information about the key press
         */
        @Override
        public void keyPressed(KeyEvent evt) {
        }
        
        /**
         * Receives the key event when a key is pressed in the control.
         * @param evt the event containing information about the key press
         */
        @Override
        public void keyReleased(KeyEvent evt) {
            int index = -1;
            if (evt.keyCode == SWT.ARROW_UP || evt.keyCode == SWT.ARROW_LEFT) {
                index = historyView.getPrecedentOperationIndex();
            } else if (evt.keyCode == SWT.ARROW_DOWN || evt.keyCode == SWT.ARROW_RIGHT) {
                index = historyView.getSuccessiveOperationIndex();
            }
            if (index != -1) {
                historyView.goTo(index);
            }
        }
    }
    
    /**
     * Deals with a traverse event.
     */
    protected class TraverseListenerImpl implements TraverseListener {
        
        /**
         * Creates a listener that deals with a traverse event.
         */
        TraverseListenerImpl() {
        }
        
        /**
         * Receives the traverse event when a traverse key (typically a tab or arrow key) is pressed in the control.
         * @param evt the event containing information about the traverse
         */
        @Override
        public void keyTraversed(TraverseEvent evt) {
            if (evt.detail == SWT.TRAVERSE_ARROW_PREVIOUS || evt.detail == SWT.TRAVERSE_ARROW_NEXT) {
                evt.detail = SWT.TRAVERSE_NONE;
                evt.doit = true;
            }
        }
    }
}
