/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.repository;

/**
 * Manages an event indicating that the state of the repository has been changed.
 * @author Katsuhisa Maruyama
 */
public class RepositoryChangedEvent {
    
    /**
     * Defines the type of a repository changed event.
     */
    public enum Type {
        OPERATION_ADDED,
        OPENED, CLOSED, ABOUT_TO_CLOSE,
        LOCATION_CHANGED, ABOUT_TO_LOCATION_CHANGE,
        REFRESHED, ABOUT_TO_REFRESH;
    }
    
    /**
     * The repository that is the source of this event.
     */
    private Repository repository;
    
    /**
     * The object sent by this event.
     */
    private Object object;
    
    /**
     * The type of this event.
     */
    private Type type;
    
    /**
     * Creates an instance containing information on a changed event.
     * @param repo the source of this event
     * @param type the type of this event
     */
    public RepositoryChangedEvent(Repository repo, Type type) {
        this.repository = repo;
        this.type = type;
    }
    
    /**
     * Creates an instance containing information on a changed event.
     * @param repo the source of this event
     * @param type the type of this event
     * @param obj the object sent by this event
     */
    public RepositoryChangedEvent(Repository repo, Type type, Object obj) {
        this(repo, type);
        this.object = obj;
    }
    
    /**
     * Returns the repository that is the source of this event.
     * @return the repository
     */
    public Repository getRepository() {
        return repository;
    }
    
    /**
     * Returns the type of this event.
     * @return the event type
     */
    public Type getType() {
        return type;
    }
    
    /**
     * Returns the object sent by this event.
     * @return the object
     */
    public Object getObject() {
        return object;
    }
}
