/*
 *  Copyright 2016
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
        CLEAR, UPDATE, DEFAULT;
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
    private Type type = RepositoryChangedEvent.Type.DEFAULT;
    
    /**
     * Creates an instance containing information on a repository information.
     * @param repository the source of this event
     */
    public RepositoryChangedEvent(Repository repository) {
        this.repository = repository;
    }
    
    /**
     * Creates an instance containing information on a changed event.
     * @param repository the source of this event
     * @param type the type of this event
     */
    public RepositoryChangedEvent(Repository repository, Type type) {
        this(repository);
        this.type = type;
    }
    
    /**
     * Creates an instance containing information on a changed event.
     * @param repository the source of this event
     * @param type the type of this event
     * @param object the object sent by this event
     */
    public RepositoryChangedEvent(Repository repository, Type type, Object object) {
        this(repository, type);
        this.object = object;
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
