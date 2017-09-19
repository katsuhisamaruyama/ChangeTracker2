/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.repository;

import org.jtool.changetracker.core.Activator;
import org.jtool.changetracker.core.CTPreferencePage;
import org.jtool.changetracker.core.CTConsole;
import org.jtool.changetracker.core.CTDialog;
import org.jtool.changetracker.operation.ChangeOperation;
import org.jtool.changetracker.operation.IChangeOperation;
import org.jtool.changetracker.xml.Xml2Operation;
import org.jtool.changetracker.xml.Operation2Xml;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IEditorPart;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.lang.reflect.InvocationTargetException;

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
     * The main repository that stores change operations.
     */
    private Repository mainRepository = null;
    
    /**
     * The extension string of a Java file.
     */
    public static String JAVA_FILE_EXTENTION = ".java";
    
    /**
     * Prohibits the creation of an instance.
     */
    private RepositoryManager() {
        mainRepository = createRepository(CTPreferencePage.getLocation());
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
    public void init() {
        openRepository(mainRepository);
    }
    
    /**
     * Terminates the whole information about the main and additional repositories.
     */
    public void term() {
        if (mainRepository != null) {
            mainRepository.clear();
        }
    }
    
    /**
     * Creates a repository.
     * @param loc the location of the repository
     * @return the created repository
     */
    public Repository createRepository(String loc) {
        Repository repo = collectChangeOperationsFromHistoryFiles(loc);
        return repo;
    }
    
    /**
     * Returns the main repository that stores change operations.
     * @return the main repository, or <code>null</code> if none
     */
    public Repository getMainRepository() {
        return mainRepository;
    }
    
    /**
     * Changes the location of the main repository.
     * @param loc the location of the main repository
     */
    public void changeMainRepository(String loc) {
        if (mainRepository != null && mainRepository.getLocation().equals(loc)) {
            return;
        }
        
        boolean result = CTDialog.yesnoDialog("Repository Change", "Are you Ok to close all editors?");
        if (!result) {
            return;
        }
        
        fire(mainRepository, RepositoryChangedEvent.Type.ABOUT_TO_LOCATION_CHANGE);
        closeAllEditors();
        if (mainRepository != null) {
            mainRepository.clear();
        }
        mainRepository = collectChangeOperationsFromHistoryFiles(loc);
        fire(mainRepository, RepositoryChangedEvent.Type.LOCATION_CHANGED);
    }
    
    /**
     * Closes all the editors.
     */
    private void closeAllEditors() {
        IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = workbenchWindow.getActivePage();
        for (IEditorReference editorRef : page.getEditorReferences()) {
            IEditorPart editor = editorRef.getEditor(true);
            page.closeEditor(editor, true);
        }
    }
    
    /**
     * Opens a repository.
     * @param repo the repository to be opened
     */
    public void openRepository(Repository repo) {
        fire(repo, RepositoryChangedEvent.Type.OPENED);
    }
    
    /**
     * Closes a repository.
     * @param repo the repository to be closed
     */
    public void closeRepository(Repository repo) {
        if (repo != null && !repo.getLocation().equals(mainRepository.getLocation())) {
            fire(repo, RepositoryChangedEvent.Type.ABOUT_TO_CLOSE);
            repo.clear();
            repo = null;
            fire(repo, RepositoryChangedEvent.Type.CLOSED);
        }
    }
    
    /**
     * Refreshes a repository.
     * @param repo the repository to be refreshed
     */
    public void refreshRepository(Repository repo) {
        if (repo != null) {
            fire(repo, RepositoryChangedEvent.Type.ABOUT_TO_REFRESH);
            repo.clear();
            repo = collectChangeOperationsFromHistoryFiles(repo.getLocation());
            fire(repo, RepositoryChangedEvent.Type.REFRESHED);
        }
    }
    
    /**
     * Collects change operations from history files in the default location and stores them into the repository.
     * @return the location of a repository that stores the change operations, or <code>null</code> if the collection failed
     */
    public Repository collectChangeOperationsFromHistoryFiles(String loc) {
        File dir = new File(loc);
        if (!dir.isDirectory()) {
            return null;
        }
        
        Repository repo = new Repository(loc);
        try {
            IWorkbenchWindow window = Activator.getWorkbenchWindow();
            window.run(false, true, new IRunnableWithProgress() {
                
                /**
                 * Reads history files existing in the specified directory.
                 * @param monitor the progress monitor to use to display progress and receive requests for cancellation
                 * @exception InterruptedException if the operation detects a request to cancel
                 */
                @Override
                public void run(IProgressMonitor monitor) throws InterruptedException {
                    List<File> files = Xml2Operation.getHistoryFiles(loc);
                    monitor.beginTask("Reading change operations from history files", files.size());
                    readHistoryFiles(repo, files, monitor);
                    monitor.done();
                }
            });
        } catch (InterruptedException | InvocationTargetException e) {
            repo.clear();
        }
        return repo;
    }
    
    /**
     * Reads history files and stores them into the repository.
     * @param repo the repository that stores the change operations
     * @param files the collection of the history files
     * @param monitor the progress monitor to use to display progress and receive requests for cancellation
     * @throws Exception if a request to cancel or any failure is detected
     */
    public void readHistoryFiles(Repository repo, List<File> files, IProgressMonitor monitor) throws InterruptedException {
        for (File file : files) {
            String path = file.getAbsolutePath();
            repo.storeOperationAll(Xml2Operation.getOperations(path));
            
            if (monitor.isCanceled()) {
                repo.clear();
                monitor.done();
                throw new InterruptedException("User interrupted");
            }
            monitor.worked(1);
        }
        repo.restoreCodeOnFileOperation();
        repo.compactOperations();
        repo.checkOperationConsistency();
    }
    
    /**
     * Stores change operations into the main repository.
     * @param ops the collection of the change operations to be stored
     */
    public void storeChangeOperations(List<IChangeOperation> ops) {
        ChangeOperation.sort(ops);
        ops = OperationHistoryCompactor.compact(ops);
        mainRepository.storeOperationAll(ops);
        fire(mainRepository, RepositoryChangedEvent.Type.OPERATION_ADDED);
        storeChangeOperationsIntoHistoryFile(ops);
    }
    
    /**
     * Stores a code change operation into the main repository.
     * @param op the change operation to be stored
     */
    public void storeOperation(IChangeOperation op) {
        mainRepository.storeOperation(op);
        fire(mainRepository, RepositoryChangedEvent.Type.OPERATION_ADDED);
        
        List<IChangeOperation> ops = new ArrayList<IChangeOperation>(1);
        ops.add(op);
        storeChangeOperationsIntoHistoryFile(ops);
    }
    
    /**
     * Stores change operations into a history file.
     * @param ops the collection of the change operations
     */
    private void storeChangeOperationsIntoHistoryFile(List<IChangeOperation> ops) {
        if (ops.size() == 0) {
            return;
        }
        
        try {
            long time = ops.get(0).getTimeAsLong();
            String filename = mainRepository.getLocation() + File.separatorChar + String.valueOf(time);
            Operation2Xml.storeOperations(ops, filename);
        } catch (Exception e) {
            CTConsole.println("Failed to store change operations into a history file: " + e.toString());
        }
    }
    
    /**
     * Sends a repository changed event to all the listeners.
     * @param evt the changed event.
     */
    public void fire(Repository repo, RepositoryChangedEvent.Type type) {
        RepositoryChangedEvent event = new RepositoryChangedEvent(repo, type);
        repo.fire(event);
    }
}
