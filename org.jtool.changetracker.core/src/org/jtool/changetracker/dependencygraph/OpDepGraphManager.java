/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.dependencygraph;

import org.jtool.changetracker.repository.FileInfo;
import org.jtool.changetracker.repository.ProjectInfo;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages operation dependency graphs.
 * @author Katsuhisa Maruyama
 */
public class OpDepGraphManager {
    
    /**
     * The single instance of this repository manager.
     */
    private static OpDepGraphManager instance = new OpDepGraphManager();
    
    /**
     * The map of operation dependency graphs for projects.
     */
    private Map<String, ProjectOpDepGraph> projectGraphs;
    
    /**
     * Prohibits the creation of an instance.
     */
    private OpDepGraphManager() {
        this.projectGraphs = new HashMap<String, ProjectOpDepGraph>();
    }
    
    /**
     * Returns the single instance that manages the operation dependency graphs.
     * @return the history manager
     */
    public static OpDepGraphManager getInstance() {
        return instance;
    }
    
    /**
     * Clears all the operation dependency graphs
     */
    public void clear() {
        for (ProjectOpDepGraph graph : projectGraphs.values()) {
            graph.clear();
        }
        projectGraphs.clear();
    }
    
    /**
     * Finds the operation dependency graph corresponding to a project.
     * @param pinfo the information about the project to be found
     * @return the operation dependency graph for the project, or <code>null</code> if none
     */
    public ProjectOpDepGraph getGraph(ProjectInfo pinfo) {
        return projectGraphs.get(ProjectOpDepGraph.getKey(pinfo));
    }
    
    /**
     * Stores the operation dependency graph for a project.
     * @param pinfo the information about the project to be created
     */
    public void storeOperationDependenceGraph(ProjectInfo pinfo) {
        ProjectOpDepGraph pgraph = projectGraphs.get(ProjectOpDepGraph.getKey(pinfo));
        
        if (pgraph != null) {
            List<FileInfo> files = new ArrayList<FileInfo>(); 
            for (FileInfo finfo : pinfo.getAllFiles()) {
                FileOpDepGraph fgraph = pgraph.get(finfo);
                if (finfo.getLastUpdatedTime().isAfter(fgraph.getLastUpdatedTime())) {
                    files.add(finfo);
                }
            }
            
            if (files.size() != 0) {
                for (FileInfo finfo : files) {
                    FileOpDepGraph fgraph = OpDepGraphConstructor.createGraph(finfo);
                    fgraph.setLastUpdatedTime(ZonedDateTime.now());
                    
                    pgraph.remove(finfo);
                    pgraph.add(fgraph);
                }
                pgraph.removeAllEdges();
                
                OpDepGraphConstructor.collectInterEdges(pgraph);
            }
            
        } else {
            pgraph = new ProjectOpDepGraph(pinfo);
            projectGraphs.put(ProjectOpDepGraph.getKey(pinfo), pgraph);
            
            for (FileInfo finfo : pinfo.getAllFiles()) {
                FileOpDepGraph fgraph = OpDepGraphConstructor.createGraph(finfo);
                if (fgraph == null) {
                    clear();
                    break;
                }
                
                fgraph.setLastUpdatedTime(ZonedDateTime.now());
                pgraph.add(fgraph);
            }
            
            if (pgraph.size() != 0) {
                OpDepGraphConstructor.collectInterEdges(pgraph);
            }
        }
        
        // System.out.println(graph.toString());
    }
    
    /**
     * Tests if the operation dependency graph already exists. 
     * @param pinfo the information about the project to be checked
     * @return <code>true</code> if the operation dependency graph already exists, otherwise <code>false</code>
     */
    public boolean existOperationDependenceGraph(ProjectInfo pinfo) {
        ProjectOpDepGraph pgraph = projectGraphs.get(ProjectOpDepGraph.getKey(pinfo));
        if (pgraph == null) {
            return false;
        }
        
        for (FileInfo finfo : pinfo.getAllFiles()) {
            FileOpDepGraph fgraph = pgraph.get(finfo);
            if (finfo.getLastUpdatedTime().isAfter(fgraph.getLastUpdatedTime())) {
                return false;
            }
        }
        return true;
    }
}
