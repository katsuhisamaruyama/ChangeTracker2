/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.dependencygraph;

import org.jtool.changetracker.operation.ICodeOperation;
import org.jtool.changetracker.operation.IChangeOperation;
import org.jtool.changetracker.repository.FileInfo;
import org.jtool.changetracker.repository.ProjectInfo;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import java.util.List;
import java.util.ArrayList;

/**
 * Constructs an operation dependency graph for a file.
 * @author Katsuhisa Maruyama
 */
public class OpDepGraphConstructor {
    
    /**
     * The operation dependency graph for the project that contains the created file operation dependency graph.
     */
    private static ProjectOpDepGraph projectGraph;
    
    /**
     * The operation dependency graph for the file.
     */
    private static FileOpDepGraph fileGraph;
    
    /**
     * Creates an operation dependency graph corresponding to the specified file.
     * @param finfo the file information
     * @return the created operation graph for the file
     */
    static FileOpDepGraph createGraph(final FileInfo finfo) {
        
        Job job = new Job("Collecting operations from history files") {
        
            /**
             * Executes this job. Returns the result of the execution.
             * @param monitor the progress monitor to use to display progress and receive requests for cancellation
             */
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    fileGraph = new FileOpDepGraph(finfo);
                    List<IChangeOperation> operations = finfo.getOperations();
                    
                    monitor.beginTask("Constructing operation graph: " + finfo.getName(), operations.size() * 2);
                    collectOperationNodes(operations, monitor);
                    collectDependenceEdges(monitor);
                    
                    return Status.OK_STATUS;
                    
                } catch (Exception e) {
                    System.err.println("Failed to construct operation history graphs");
                    projectGraph = null;
                    fileGraph = null;
                    return Status.CANCEL_STATUS;
                    
                } finally {
                    monitor.done();
                }
            }
        };
        job.schedule();
        
        return fileGraph;
    }
    
    /**
     * Collects operation nodes of the operation dependency graph.
     * @param ops the collection of the operations
     * @param monitor the progress monitor to use to display progress and receive requests for cancellation
     * @exception InterruptedException if the operation detects a request to cancel
     */
    private static void collectOperationNodes(List<IChangeOperation> operations, IProgressMonitor monitor) throws InterruptedException {
        int id = 0;
        for (int idx = 0; idx < operations.size(); idx++) {
            
            monitor.subTask("Collecting opearation nodes " + String.valueOf(idx + 1) + "/" + operations.size());
            IChangeOperation operation = operations.get(idx);
            
            if (operation instanceof ICodeOperation) {
                ICodeOperation op = (ICodeOperation)operation;
                if (op.getDeletedText().length() != 0) {
                    OpDepGraphNode node = new OpDepGraphRemoveNode(id, op);
                    id++;
                    fileGraph.add(node);
                }
                
                if (op.getInsertedText().length() != 0) {
                    OpDepGraphNode node = new OpDepGraphAddNode(id, op);
                    id++;
                    fileGraph.add(node);
                }
                
                if (op.getCopiedText().length() != 0) {
                    OpDepGraphNode node = new OpDepGraphCopyNode(id, op);
                    id++;
                    fileGraph.add(node);
                }
            }
            
            if (monitor.isCanceled()) {
                monitor.done();
                throw new InterruptedException();
            }
            monitor.worked(1);
        }
    }
    
    /**
     * Collects dependence edges of the operation graph.
     * @param graph the operation graph under creation
     * @param monitor the progress monitor to use to display progress and receive requests for cancellation
     * @throws InterruptedException if the operation detects a request to cancel
     */
    private static void collectDependenceEdges(IProgressMonitor monitor) throws InterruptedException {
        int idx = 1;
        List<OpDepGraphNode> nodes = new ArrayList<OpDepGraphNode>(fileGraph.getAllNodes());
        ProjectOpDepGraph.sortNodes(nodes);
        
        for (OpDepGraphNode node : nodes) {
            monitor.subTask("Collecting dependence edges " + String.valueOf(idx) + "/" + nodes.size());
            
            if (node.getId() != 0) {
                collectDependenceEdge(node);
            }
            idx++;
            
            if (monitor.isCanceled()) {
                monitor.done();
                throw new InterruptedException();
            }
            monitor.worked(1);
        }
    }
    
    /**
     * Finds an operation node which a given operation node depends on and creates a dependence edge between those nodes.
     * @param node the operation node
     */
    private static void collectDependenceEdge(OpDepGraphNode node) {
        for (int idx = node.getId() - 1; idx >= 0; idx--) {
            OpDepGraphNode n = fileGraph.getNode(idx);
            
            if (node.getAdjustedOffsetValues().size() == 0) {
                break;
            }
            
            if (n != null) {
                if (node.dependsOn(n)) {
                    fileGraph.add(new OpDepGraphEdge(n, node, OpDepGraphEdge.Sort.NORMAL));
                } else {
                    node.adjustOffsetValuesBackward(n);
                }
            }
        }
    }
    
    /**
     * Collects inter-edges across file operation dependency graphs.
     * @param pgraph the operation dependency graph for a project
     */
    public static void collectInterEdges(ProjectOpDepGraph pgraph) {
        projectGraph = pgraph;
        final ProjectInfo pinfo = pgraph.getProjectInfo();
        
        Job job = new Job("Collecting operations from history files") {
            
            /**
             * Executes this job. Returns the result of the execution.
             * @param monitor the progress monitor to use to display progress and receive requests for cancellation
             */
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    List<IChangeOperation> operations = pinfo.getOperations();
                    
                    monitor.beginTask("Collecting inter-edges in the operation dependency graph: " + pinfo.getName(), operations.size());
                    collectCCPEdges(operations, monitor);
                    
                    return Status.OK_STATUS;
                    
                } catch (Exception e) {
                    System.err.println("Failed to collect inter-edges");
                    projectGraph = null;
                    fileGraph = null;
                    return Status.CANCEL_STATUS;
                    
                } finally {
                    monitor.done();
                }
            }
        };
        job.schedule();
    }
    
    /**
     * Collects ccp-edges between the node for the copy/copy operation and the node for the paste operation.
     * @param ops the collection of the operations
     * @param monitor the progress monitor to use to display progress and receive requests for cancellation
     * @exception InterruptedException if the operation detects a request to cancel
     */
    private static void collectCCPEdges(List<IChangeOperation> operations, IProgressMonitor monitor) throws InterruptedException {
        for (int srcIdx = 0; srcIdx < operations.size(); srcIdx++) {
            monitor.subTask("Collecting ccp edges " + String.valueOf(srcIdx + 1) + "/" + operations.size());
            
            IChangeOperation sop = operations.get(srcIdx);
            if (sop instanceof ICodeOperation) {
                ICodeOperation srcOp = (ICodeOperation)sop;
                
                if (isCutOrCopyOperation(srcOp)) {
                    for (int dstIdx = srcIdx + 1; dstIdx < operations.size(); dstIdx++) {
                        IChangeOperation dop = operations.get(dstIdx);
                        if (dop instanceof ICodeOperation) {
                            ICodeOperation dstOp = (ICodeOperation)dop;
                            
                            if (isCutOrCopyOperation(dstOp)) {
                                break;
                            }
                            
                            if (isPasteOperation(dstOp)) {
                                String srcText = srcOp.getCutOrCopiedText();
                                String dstText = dstOp.getInsertedText();
                                
                                if (dstText.endsWith(srcText)) {
                                    OpDepGraphNode srcNode = projectGraph.getOperationNode(srcOp);
                                    OpDepGraphNode dstNode = projectGraph.getOperationNode(dstOp);
                                    if (srcNode != null && dstNode != null) {
                                        
                                        OpDepGraphEdge edge = new OpDepGraphEdge(srcNode, dstNode, OpDepGraphEdge.Sort.CPP);
                                        if (!projectGraph.contains(edge)) {
                                            projectGraph.add(edge);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            if (monitor.isCanceled()) {
                monitor.done();
                throw new InterruptedException();
            }
            monitor.worked(1);
        }
    }
    
    /**
     * Tests if this operation cuts or copies any text.
     * @param operation the operation to be checked
     * @return <code>true</code> if this operation cuts or copies any text, otherwise <code>false</code>
     */
    private static boolean isCutOrCopyOperation(ICodeOperation operation) {
        return operation.isCut() || operation.isCopy();
    }
    
    /**
     * Tests if this operation pastes any text.
     * @param operation the operation to be checked
     * @return <code>true</code> if this operation pastes any text, otherwise <code>false</code>
     */
    private static boolean isPasteOperation(ICodeOperation operation) {
        return operation.isPaste();
    }
}
