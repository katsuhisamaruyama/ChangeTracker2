/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.repository;

import org.jtool.changetracker.core.CTPreferencePage;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

/**
 * Manages the repository that stores information about change operations.
 * @author Katsuhisa Maruyama
 */
public class RepositoryManager {
    
    /**
     * The single instance of this repository manager.
     */
    private static RepositoryManager instance = new RepositoryManager();
    
    /**
     * The collection of all repositories that were opened in the past.
     */
    private Map<String, Repository> repositories = new HashMap<String, Repository>();
    
    /**
     * The current repository that is focused on.
     */
    private Repository currentRepository;
    
    /**
     * The repository that continuously stores online change operations.
     */
    private Repository onlineRepository;
    
    /**
     * Prohibits the creation of an instance.
     */
    private RepositoryManager() {
    }
    
    /**
     * Returns the single instance that manages the repository of change operations.
     * @return the history manager
     */
    public static RepositoryManager getInstance() {
        return instance;
    }
    
    /**
     * Initializes the whole information about the main repository.
     */
    public void initialize() {
        String location = CTPreferencePage.getLocation();
        if (location == null || location.length() == 0) {
            CTPreferencePage.setLocation(CTPreferencePage.getDefaultLoaction());
        }
        
        createRepository(location);
    }
    
    /**
     * Terminates the whole information about the main and additional repositories.
     */
    public void terminate() {
        repositories.clear();
        currentRepository = null;
        onlineRepository = null;
    }
    
    /**
     * Sets the repository that continuously stores online change operations.
     * @param repo the online repository
     */
    public void setOnlineRepository(Repository repo) {
        onlineRepository = repo;
    }
    
    /**
     * Returns the repository that continuously stores online change operations.
     * @return the online repository
     */
    public Repository getOnlineRepository() {
        return onlineRepository;
    }
    
    /**
     * Specifies if the online repository is used.
     * @param bool <code>true</code> if the online repository is used, otherwise <code>false</code>
     */
    public void useOnlineRepository(boolean bool) {
        if (onlineRepository != null && bool) {
            onlineRepository.collectChangeOperationsFromHistoryFiles();
        } else {
            onlineRepository.clear();
            onlineRepository = null;
        }
    }
    
    /**
     * Returns the repository corresponding to the location
     * @param location the location of a repository
     * @return the corresponding repository, <code>null</code> if there is no corresponding repository
     */
    public Repository getRepository(String location) {
        if (location == null) {
            return null;
        }
        return repositories.get(location);
    }
    
    /**
     * Returns the collection of all repositories.
     * @return all the repositories
     */
    public Collection<Repository> getAllRepositories() {
        return repositories.values();
    }
    
    /**
     * Returns the current repository that is focused on.
     * @return the current repository
     */
    public Repository getCurrentRepository() {
        return currentRepository;
    }
    
    /**
     * Creates a new repository.
     * @param location the location of the main repository
     * @return the created repository
     */
    public Repository createRepository(String location) {
        Repository repo = getRepository(location);
        if (repo != null) {
            repo.clear();
        }
        repo = new Repository(location);
        repo.collectChangeOperationsFromHistoryFiles();
        repositories.put(location, repo);
        return repo;
    }
    
    /**
     * Opens a repository.
     * @param repo the repository to be opened
     */
    public void openRepository(Repository repo) {
        if (repo == null) {
            return;
        }
        
        fire(repo, RepositoryChangedEvent.Type.ABOUT_TO_OPEN);
        currentRepository = repo;
        CTPreferencePage.setLocation(currentRepository.getLocation());
        fire(repo, RepositoryChangedEvent.Type.OPENED);
    }
    
    /**
     * Closes a repository.
     * @param repo the repository to be closed
     */
    public void closeRepository(Repository repo) {
        if (repo == null) {
            return;
        }
        
        fire(repo, RepositoryChangedEvent.Type.ABOUT_TO_CLOSE);
        repo.clear();
        repo = null;
        fire(repo, RepositoryChangedEvent.Type.CLOSED);
    }
    
    /**
     * Refreshes a repository.
     * @param repo the repository to be refreshed
     */
    public void refreshRepository(Repository repo) {
        if (repo == null) {
            return;
        }
        
        fire(repo, RepositoryChangedEvent.Type.ABOUT_TO_REFRESH);
        repo.clear();
        repo = new Repository(repo.getLocation());
        repo.collectChangeOperationsFromHistoryFiles();
        fire(repo, RepositoryChangedEvent.Type.REFRESHED);
    }
    
    /**
     * Sends a repository changed event to all the listeners.
     * @param evt the changed event.
     */
    private void fire(Repository repo, RepositoryChangedEvent.Type type) {
        RepositoryChangedEvent event = new RepositoryChangedEvent(repo, type);
        repo.fire(event);
    }
}
