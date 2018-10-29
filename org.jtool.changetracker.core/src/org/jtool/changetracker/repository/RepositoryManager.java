/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.repository;

import org.jtool.changetracker.core.CTPreferencePage;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collection;

/**
 * Manages the repositories that store change operations.
 * @author Katsuhisa Maruyama
 */
public class RepositoryManager {
    
    /**
     * The single instance of this repository manager.
     */
    private static RepositoryManager instance = new RepositoryManager();
    
    /**
     * The collection of repository handlers that are loaded from the extension point.
     */
    private Set<IRepositoryHandler> repositoryHandlers = new HashSet<IRepositoryHandler>();
    
    /**
     * The collection of all repositories that were opened in the past.
     */
    private Map<String, Repository> repositories = new HashMap<String, Repository>();
    
    /**
     * The collection of repositories that continuously store change operations.
     */
    private Set<Repository> onlineRepositories = new HashSet<Repository>();
    
    /**
     * Prohibits the creation of an instance.
     */
    private RepositoryManager() {
        repositoryHandlers = RepositoryHandlerLoader.load();
    }
    
    /**
     * Returns the single instance that manages the repository of change operations.
     * @return the history manager
     */
    public static RepositoryManager getInstance() {
        return instance;
    }
    
    /**
     * Returns the collection of the repository handlers that are loaded from the extension point.
     * @return the collection of the repository handlers
     */
    public Set<IRepositoryHandler> getRepositoryHandlers() {
        return repositoryHandlers;
    }
    
    /**
     * Initializes the whole information about the main repository.
     */
    public void initialize() {
        for (IRepositoryHandler handler : repositoryHandlers) {
            handler.initialize();
        }
    }
    
    /**
     * Terminates the whole information about the main and additional repositories.
     */
    public void terminate() {
        repositories.clear();
        onlineRepositories.clear();
        
        for (IRepositoryHandler handler : repositoryHandlers) {
            handler.terminate();
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
     * Opens a repository.
     * @param location the location of the repository
     * @return the opened repository
     */
    public Repository openRepository(String location) {
        Repository repo = getRepository(location);
        if (repo != null) {
            repo.clear();
        }
        repo = new Repository(location);
        for (IRepositoryHandler handler: repositoryHandlers) {
            repo.addEventListener(handler);
        }
        repo.fireAboutTo(RepositoryEvent.Type.OPEN);
        repo.collectFromHistoryFiles();
        repositories.put(location, repo);
        repo.fireChanged(RepositoryEvent.Type.OPEN);
        return repo;
    }
    
    /**
     * Closes a repository.
     * @param repo the repository to be closed
     */
    public void closeRepository(Repository repo) {
        if (repo == null) {
            return;
        }
        
        repo.fireAboutTo(RepositoryEvent.Type.CLOSE);
        repo.clear();
        repo.fireChanged(RepositoryEvent.Type.CLOSE);
        repo = null;
    }
    
    /**
     * Refreshes a repository.
     * @param repo the repository to be refreshed
     */
    public void refreshRepository(Repository repo) {
        if (repo == null) {
            return;
        }
        
        repo.fireAboutTo(RepositoryEvent.Type.REFRESH);
        repo.clear();
        repo = new Repository(repo.getLocation());
        repo.collectFromHistoryFiles();
        repo.fireChanged(RepositoryEvent.Type.REFRESH);
    }
    
    /**
     * Returns the collection of repositories that continuously store change operations.
     * @return the online repositories
     */
    public Set<Repository> getOnlineRepositories() {
        return onlineRepositories;
    }
    
    /**
     * Adds a repository that continuously stores change operations.
     * @param repo the online repository to be added
     */
    public void addOnlineRepository(Repository repo) {
        onlineRepositories.add(repo);
        for (IRepositoryHandler handler: repositoryHandlers) {
            repo.addEventListener(handler);
        }
    }
    
    /**
     * Removes a repository that continuously stores change operations.
     * @param repo the online repository to be removed
     */
    public void removeOnlineRepository(Repository repo) {
        onlineRepositories.remove(repo);
        for (IRepositoryHandler handler: repositoryHandlers) {
            repo.removeEventListener(handler);
        }
    }
    
    /**
     * Loads a repository from the preferences.
     * @return the repository
     */
    public Repository loadRepository() {
        String location = CTPreferencePage.getLocation();
        if (location == null || location.length() == 0) {
            CTPreferencePage.setLocation(CTPreferencePage.getDefaultLoaction());
        }
        return openRepository(location);
    }
    
    /**
     * Stores a repository into the preferences.
     * @param repo the repository to be stored
     */
    public void storeRepository(Repository repo) {
        CTPreferencePage.setLocation(repo.getLocation());
    }
}
