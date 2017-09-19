/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.operation;

import java.util.Map;

import org.jtool.changetracker.repository.CTPath;

import java.util.HashMap;
import java.time.ZonedDateTime;

/**
 * Stores information on a refactoring operation.
 * @author Katsuhisa Maruyama
 */
public class RefactoringOperation extends ChangeOperation {
    
    /**
     * The action of a command operation.
     */
    public enum Action {
        EXECUTION, UNDO, REDO;
    }
    
    /**
     * The name of a refactoring.
     */
    private String name;
    
    /**
     * The map that stores arguments of a refactoring.
     */
    private Map<String, String> argumentMap;
    
    /**
     * The starting point of the text that is contained the selection.
     */
    private int selectionStart;
    
    /**
     * The text that is contained the selection.
     */
    private String selectedText;
    
    /**
     * Creates an instance storing information on this refactoring operation.
     * @param time the time when the refactoring operation was performed
     * @param pathinfo information about path of a resource on which the refactoring operation was performed
     * @param action the action of the refactoring operation
     * @param author the author's name
     */
    public RefactoringOperation(ZonedDateTime time, CTPath pathinfo, String action, String author) {
        super(time, Type.REFACTOR, pathinfo, action, author);
    }
    
    /**
     * Creates an instance storing information on this refactoring operation.
     * @param time the time when the refactoring operation was performed
     * @param pathinfo information about path of a resource on which the refactoring operation was performed
     * @param action the action of the refactoring operation
     */
    public RefactoringOperation(ZonedDateTime time, CTPath pathinfo, String action) {
        this(time, pathinfo, action, ChangeOperation.getUserName());
    }
    
    /**
     *Sets the string representing the command name of this refactoring operation.
     * @param name the command name of the refactoring operation
     */
    public void setName(String name) {
        assert name != null;
        
        this.name = name;
    }
    
    /**
     * Returns the name of a refactoring.
     * @return the refactoring name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the refactoring arguments.
     * @param map the map of refactoring arguments
     */
    public void setArguments(Map<String, String> map) {
        argumentMap = map;
    }
    
    /**
     * Returns the refactoring arguments.
     * @return the map of refactoring arguments
     */
    public Map<String, String> getArguments() {
        return argumentMap;
    }
    
    /**
     * Returns the value corresponding to a given key in the refactoring arguments.
     * @return the value string
     */
    public String getArgument(String key) {
        return argumentMap.get(key);
    }
    
    /**
     * Returns the text of the refactoring arguments.
     * @return the the refactoring argument text
     */
    public String getArgumentText() {
        if (argumentMap.size() == 0) {
            return "";
        }
        
        StringBuilder buf = new StringBuilder();
        for (String key : argumentMap.keySet()) {
            String value = argumentMap.get(key);
            buf.append(";" + key + ":" + value);
        }
        return buf.toString().substring(1);
    }
    
    /**
     * Sets the refactoring arguments from a text.
     * @param text the text of the refactoring arguments
     */
    public void setArguments(String text) {
        argumentMap = new HashMap<String, String>();
        if (text.length() == 0) {
            return;
        }
        
        String[] result = text.split(";");
        for (int i = 0; i < result.length; i++) {
            String[] element = result[i].split(":");
            argumentMap.put(element[0], element[1]);
            
        }
    }
    
    /**
     * Sets the starting point of the text that is contained the selection.
     * @param start the starting point of the text
     */
    public void setSelectionStart(int start) {
        selectionStart = start;
    }
    
    /**
     * Returns the starting point of the text that is contained the selection.
     * @return the starting point of the text
     */
    public int getSelectionStart() {
        return selectionStart;
    }
    
    /**
     * Returns the ending point of the text that is contained the selection.
     * @return the ending point of the text
     */
    public int getSelectionEnd() {
        return selectionStart + selectedText.length() - 1;
    }
    
    /**
     * Sets the text that is contained the selection.
     * @param text the selected text
     */
    public void setSelectedText(String text) {
        selectedText = text;
    }
    
    /**
     * Returns the text that is contained the selection.
     * @return text the selected text
     */
    public String getSelectedText() {
        return selectedText;
    }
    
    /**
     * Tests if this refactoring operation represents the normal refactoring.
     * @return <code>true</code> if this this refactoring operation represents the normal refactoring, otherwise <code>false</code>
     */
    public boolean isNormal() {
        return action.equals(CommandOperation.Action.EXECUTION.toString());
    }
    
    /**
     * Tests if this refactoring operation undoes a past refactoring.
     * @return <code>true</code> if this this refactoring operation undoes a past refactoring, otherwise <code>false</code>
     */
    public boolean isUndo() {
        return action.equals(Action.UNDO.toString());
    }
    
    /**
     * Tests if this refactoring operation redoes a past refactoring.
     * @return <code>true</code> if this refactoring operation redoes a past refactoring, otherwise <code>false</code>
     */
    public boolean isRedo() {
        return action.equals(Action.REDO.toString());
    }
    
    /**
     * Returns the string for printing, which does not contain a new line character at its end.
     * @return the string for printing
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(super.toString());
        buf.append(" name=[" + name + "]");
        buf.append(" range=[" + getSelectionStart() + "-" + getSelectionEnd() + "]");
        buf.append(" code=[" + getShortText(selectedText) + "]");
        return buf.toString();
    }
}
