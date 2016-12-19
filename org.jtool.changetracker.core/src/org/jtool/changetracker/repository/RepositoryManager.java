/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.repository;

import org.jtool.changetracker.operation.IChangeOperation;
import org.jtool.changetracker.xml.Xml2Operation;
import org.jtool.changetracker.xml.Operation2Xml;
import org.jtool.changetracker.xml.XmlFileManager;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.resources.ResourcesPlugin;
import java.util.List;
import java.util.ArrayList;
import java.io.File;

/**
 * Manages the repository that stores information about code change operations.
 * @author Katsuhisa Maruyama
 */
public class RepositoryManager {
    
    /**
     * The single instance of this repository manager.
     */
    private static RepositoryManager instance = new RepositoryManager();
    
    /**
     * A repository that stores the code change operations.
     */
    private Repository repository;
    
    /**
     * The path name of the location where history files exist.
     */
    private String location;
    
    /**
     * The directory that stores history files.
     */
    private static String DEFAULT_DIRECTORY_PATH = File.separator + "#history";
    
    /**
     * The extension string of a Java file. 
     */
    public static String JAVA_FILE_EXTENTION = ".java";
    
    /**
     * The extension string of a history file. 
     */
    public static String XML_FILE_EXTENTION = ".xml";
    
    /**
     * Prohibits the creation of an instance.
     */
    private RepositoryManager() {
    }
    
    /**
     * Returns the single instance that manages the history.
     * @return the history manager
     */
    public static RepositoryManager getInstance() {
        return instance;
    }
    
    /**
     * Returns the repository that stores the code change operations.
     * @return the repository
     */
    public Repository getRepository() {
        return repository;
    }
    
    /**
     * Sets the path name of the location where history files exist.
     * @param location the path name of the location
     */
    public void setLocation(String location) {
        this.location = location;
    }
    
    /**
     * Sets the path name of the default location where history files exist.
     */
    public void setDefaultLocation() {
        this.location = getDefaultDirectoryPath();
    }
    
    /**
     * Collects code change operations from history files in the default location and stores them into a repository.
     * @return repository the repository that stores the code change operations
     */
    public Repository collectOperationsFromHistoryFiles() {
        if (location == null) {
            setDefaultLocation();
        }
        
        boolean resultMakeDir = XmlFileManager.makeDir(new File(location));
        if (!resultMakeDir) {
            return null;
        }
        
        if (repository == null) {
            repository = new Repository(location);
        } else {
            repository.clear();
        }
        
        Job job = new Job("Collecting operations from history files") {
            
            /**
             * Executes this job. Returns the result of the execution.
             * @param monitor the progress monitor to use to display progress and receive requests for cancellation
             */
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    List<File> files = getAllHistoryFiles(location);
                    
                    monitor.beginTask("Collecting code change operations", files.size());
                    readHistoryFiles(files, monitor);
                    
                    return Status.OK_STATUS;
                    
                } catch (Exception e) {
                    System.err.println("Failed to collect code change operations from history files: " + e.toString());
                    e.printStackTrace();
                    
                    repository.clear();
                    return Status.CANCEL_STATUS;
                    
                } finally {
                    monitor.done();
                }
            }
        };
        job.setUser(false);
        job.schedule();
        
        return repository;
    }
    
    /**
     * Reads the history files.
     * @return the collection of all the operations stored in the history files
     * @param monitor the progress monitor to use to display progress and receive requests for cancellation
     * @throws Exception if a request to cancel or any failure is detected
     */
    private void readHistoryFiles(List<File> files, IProgressMonitor monitor) throws Exception {
        for (File file : files) {
            String path = file.getAbsolutePath();
            List<IChangeOperation> operations = Xml2Operation.getOperations(path);
            
            OperationHistory.sort(operations);
            repository.storeOperations(operations);
            
            if (monitor.isCanceled()) {
                monitor.done();
                throw new InterruptedException("User interrupted");
            }
            monitor.worked(1);
        }
    }
    
    /**
     * Returns all descendant history files of a directory.
     * @param path the path name of the directory
     * @return the descendant files
     */
    private List<File> getAllHistoryFiles(String path) {
        List<File> files = new ArrayList<File>();
        
        File dir = new File(path);
        if (dir.isFile()) {
            if (path.endsWith(XML_FILE_EXTENTION)) {
                files.add(dir);
            }
            
        } else if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            for (File f : children) {
                files.addAll(getAllHistoryFiles(f.getPath()));
            }
        }
        
        return files;
    }
    
    /**
     * Stores code change operations into repository.
     * @param operations the collection of code change operations
     */
    public void storeOperations(List<IChangeOperation> operations) {
        operations = OperationHistory.form(operations);
        repository.addOperations(operations);
        
        storeOperationsIntoHistoryFile(operations);
    }
    
    /**
     * Stores code change operations into a history file.
     * @param operations the collection of code change operations
     */
    private void storeOperationsIntoHistoryFile(List<IChangeOperation> operations) {
        if (operations.size() == 0) {
            return;
        }
        
        try {
            long time = operations.get(0).getTimeAsLong();
            String filename = location + File.separatorChar + String.valueOf(time) + XML_FILE_EXTENTION;
            
            Operation2Xml.storeOperations(operations, filename);
        } catch (Exception e) {
            System.err.println("Failed to store code change operations into a history file: " + e.toString());
        }
    }
    
    /**
     * Returns the default path name of the directory that contains history files.
     * @return the default directory path name.
     */
    private String getDefaultDirectoryPath() {
        IPath workspaceDir = ResourcesPlugin.getWorkspace().getRoot().getLocation();
        return workspaceDir.append(DEFAULT_DIRECTORY_PATH).toString();
    }
}
