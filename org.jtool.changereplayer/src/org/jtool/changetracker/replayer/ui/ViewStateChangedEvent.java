/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.replayer.ui;

import java.util.EventObject;

/**
 * Manages an event that represents the change of replay state.
 * @author Katsuhisa Maruyama
 */
public class ViewStateChangedEvent extends EventObject {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Defines the type of an event.
     */
    public enum Type {
        INDEX_CHANGED, MARK_CHANGED, FILE_OPENED, UPDATE, RESET, DEFAULT;
    }
    
    /**
     * The type of the event.
     */
    private Type type = Type.DEFAULT;
    
    /**
     * Creates an event that represents the change of replay state.
     * @param source the instance on which the event initially occurred
     */
    public ViewStateChangedEvent(Object source) {
        super(source);
    }
    
    /**
     * Creates an event that represents the change of replay state.
     * @param source the instance on which the event initially occurred
     * @param the type of an event
     */
    public ViewStateChangedEvent(Object source, Type type) {
        this(source);
        this.type = type;
    }
    
    /**
     * Returns the type of the event.
     * @return the event type
     */
    public ViewStateChangedEvent.Type getType() {
        return type;
    }
}
