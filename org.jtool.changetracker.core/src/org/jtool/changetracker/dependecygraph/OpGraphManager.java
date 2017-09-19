/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.dependecygraph;

import org.jtool.changetracker.core.CTConsole;
import org.jtool.changetracker.repository.CTFile;
import org.jtool.changetracker.repository.CTProject;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.time.ZonedDateTime;

/**
 * Manages operation history graphs.
 * @author Katsuhisa Maruyama
 */
public class OpGraphManager {
    
    /**
     * The single instance of this operation history graph manager.
     */
    private static OpGraphManager instance = new OpGraphManager();
    
    /**
     * The map of operation history graphs for projects.
     */
    private Map<String, ProjectOpGraph> projectGraphs = new HashMap<String, ProjectOpGraph>();
    
    /**
     * Prohibits the creation of an instance.
     */
    private OpGraphManager() {
    }
    
    /**
     * Returns the single instance that manages the operation history graphs.
     * @return the operation history graph manager
     */
    public static OpGraphManager getInstance() {
        return instance;
    }
    
    /**
     * Clears all the operation history graphs
     */
    public void clear() {
        for (ProjectOpGraph graph : projectGraphs.values()) {
            graph.clear();
        }
        projectGraphs.clear();
    }
    
    /**
     * Finds an operation history graph of a project.
     * @param pinfo information about the project
     * @return the operation history graph for the project, or <code>null</code> if none
     */
    public ProjectOpGraph getGraph(CTProject pinfo) {
        return projectGraphs.get(pinfo.getQualifiedName());
    }
    
    /**
     * Creates an operation history graph of a project.
     * @param pinfo information about the project
     * @return the created operation history graph
     */
    public ProjectOpGraph createGraph(CTProject pinfo) {
        if (pinfo == null) {
            return null;
        }
        
        ProjectOpGraph pgraph = getGraph(pinfo);
        List<CTFile> finfos = new ArrayList<CTFile>(); 
        if (pgraph != null) {
            for (CTFile finfo : pinfo.getFiles()) {
                FileOpGraph fgraph = pgraph.get(finfo);
                if (finfo.getLastUpdatedTime().isAfter(fgraph.getLastUpdatedTime())) {
                    finfos.add(finfo);
                }
            }
            
            if (finfos.size() != 0) {
                for (CTFile finfo : finfos) {
                    FileOpGraph fgraph = OpGraphConstructor.createGraph(finfo);
                    if (fgraph.getFile() != null) {
                        fgraph.setLastUpdatedTime(ZonedDateTime.now());
                        pgraph.remove(finfo);
                        pgraph.add(fgraph);
                    } else {
                        pgraph.clear();
                        CTConsole.println("Failed to create an operation history graph " + pinfo.getQualifiedName());
                        break;
                    }
                }
                if (pgraph.size() != 0) {
                    pgraph.removeAllEdges();
                    OpGraphConstructor.collectInterEdges(pgraph);
                }
            }
            
        } else {
            pgraph = new ProjectOpGraph(pinfo);
            projectGraphs.put(pinfo.getQualifiedName(), pgraph);
            for (CTFile finfo : pinfo.getFiles()) {
                FileOpGraph fgraph = OpGraphConstructor.createGraph(finfo);
                if (fgraph.getFile() != null) {
                    fgraph.setLastUpdatedTime(ZonedDateTime.now());
                    pgraph.add(fgraph);
                } else {
                    pgraph.clear();
                    CTConsole.println("Failed to create an operation history graph " + pinfo.getQualifiedName());
                    break;
                }
            }
            if (pgraph.size() != 0) {
                OpGraphConstructor.collectInterEdges(pgraph);
            }
        }
        
        System.out.println(pgraph.toString());
        return pgraph;
    }
    
    /**
     * Tests if an operation history graph exists. 
     * @param pinfo information about the project to be checked
     * @return <code>true</code> if the operation history graph exists, otherwise <code>false</code>
     */
    public boolean existGraph(CTProject pinfo) {
        if (pinfo == null) {
            return false; 
        }
        ProjectOpGraph pgraph = projectGraphs.get(pinfo.getQualifiedName());
        if (pgraph == null) {
            return false;
        }
        
        for (CTFile finfo : pinfo.getFiles()) {
            FileOpGraph fgraph = pgraph.get(finfo);
            if (finfo.getLastUpdatedTime().isAfter(fgraph.getLastUpdatedTime())) {
                return false;
            }
        }
        return true;
    }
}
