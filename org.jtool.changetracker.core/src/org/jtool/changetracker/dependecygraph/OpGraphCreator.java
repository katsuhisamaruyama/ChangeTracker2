/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.dependecygraph;

import org.jtool.changetracker.operation.ICodeOperation;
import org.jtool.changetracker.operation.CodeOperation;
import org.jtool.changetracker.operation.DocumentOperation;
import org.jtool.changetracker.operation.IChangeOperation;
import org.jtool.changetracker.repository.CTProject;
import org.jtool.changetracker.repository.CTFile;
import org.jtool.changetracker.dependencyanalyzer.JavaConstruct;
import org.jtool.changetracker.dependencyanalyzer.ParseableSnapshot;
import org.jtool.changetracker.dependencyanalyzer.DependencyDetector;
import org.jtool.changetracker.core.CTConsole;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import java.util.List;

/**
 * Constructs an operation history graph for a file.
 * @author Katsuhisa Maruyama
 */
class OpGraphCreator {
    
    /**
     * Creates an operation history graph of a file.
     * @param finfo information about the file
     * @return the created operation history graph for the file
     */
    static OpGraphForFile createGraph(CTFile finfo) {
        OpGraphForFile fgraph = new OpGraphForFile(finfo);
        Job job = new Job("Constructing an operation history graph") {
            
            /**
             * Executes this job. Returns the result of the execution.
             * @param monitor the progress monitor to use to display progress and receive requests for cancellation
             */
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    List<IChangeOperation> ops = finfo.getOperations();
                    monitor.beginTask("Constructing an operation history graph: " + finfo.getQualifiedName(), ops.size() * 2);
                    
                    collectOperationNodes(finfo, fgraph, monitor);
                    collectJavaConstructNodes(finfo, fgraph, monitor);
                    collectDependencyEdges(fgraph, monitor);
                    collectNoChangeEdges(fgraph, monitor);
                    return Status.OK_STATUS;
                    
                } catch (Exception e) {
                    CTConsole.println("Failed to construct an operation history graph");
                    fgraph.clear();
                    return Status.CANCEL_STATUS;
                    
                } finally {
                    monitor.done();
                }
            }
        };
        job.schedule();
        
        System.out.println(fgraph.toSummaryString());
        System.out.println(fgraph.toStringOnOperationNodes());
        System.out.println(fgraph.toStringOnJavaConstructNodes());
        System.out.println(fgraph.toStringOnEdges(OpGraphEdge.Sort.BACKWARD_EDITING));
        System.out.println(fgraph.toStringOnEdges(OpGraphEdge.Sort.FORWARD_EDITING));
        System.out.println(fgraph.toStringOnEdges(OpGraphEdge.Sort.NO_CHANGE));
        return fgraph;
    }
    
    /**
     * Collects ordered editing within the operation history graph for a file.
     * @param fgraph the operation history graph for the file
     */
    static void collectOrdedEdges(OpGraphForFile fgraph) {
        CTFile finfo = fgraph.getFile();
        Job job = new Job("Constructing an operation history graph") {
            
            /**
             * Executes this job. Returns the result of the execution.
             * @param monitor the progress monitor to use to display progress and receive requests for cancellation
             */
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    List<IChangeOperation> ops = finfo.getOperations();
                    monitor.beginTask("Constructing an operation history graph: " + finfo.getQualifiedName(), ops.size() * 2);
                    collectOrderedEdges(fgraph, monitor);
                    return Status.OK_STATUS;
                } catch (Exception e) {
                    CTConsole.println("Failed to construct an operation history graph");
                    fgraph.clear();
                    return Status.CANCEL_STATUS;
                } finally {
                    monitor.done();
                }
            }
        };
        job.schedule();
        System.out.println(fgraph.toStringOnEdges(OpGraphEdge.Sort.ORDERED_EDITING));
    }
    
    /**
     * Collects operation nodes of an operation history graph.
     * @param finfo information about a file
     * @param fgraph the operation history graph
     * @param monitor the progress monitor to use to display progress and receive requests for cancellation
     * @exception InterruptedException if the operation detects a request to cancel
     */
    private static void collectOperationNodes(CTFile finfo, OpGraphForFile fgraph, IProgressMonitor monitor) throws InterruptedException {
        List<IChangeOperation> operations = finfo.getOperations();
        for (int idx = 0; idx < operations.size(); idx++) {
            
            monitor.subTask("Collecting opearation nodes " +
                             String.valueOf(idx + 1) + "/" + String.valueOf(operations.size()));
            IChangeOperation op = operations.get(idx);
            if (op.isDocumentOrCopy()) {
                ICodeOperation operation = (ICodeOperation)op;
                OperationNode node = new OperationNode(fgraph, operation);
                fgraph.add(node);
            }
            
            if (monitor.isCanceled()) {
                monitor.done();
                throw new InterruptedException();
            }
            monitor.worked(1);
        }
    }
    
    /**
     * Collects Java construct nodes of an operation history graph.
     * @param finfo information about a file
     * @param fgraph the operation history graph
     * @param monitor the progress monitor to use to display progress and receive requests for cancellation
     * @exception InterruptedException if the operation detects a request to cancel
     */
    private static void collectJavaConstructNodes(CTFile finfo, OpGraphForFile fgraph, IProgressMonitor monitor) throws InterruptedException {
        List<ParseableSnapshot> snapshots = finfo.getSnapshots();
        for (int idx = 0; idx < snapshots.size(); idx++) {
            
            monitor.subTask("Collecting Java class member nodes " +
                             String.valueOf(idx + 1) + "/" + String.valueOf(snapshots.size()));
            ParseableSnapshot sn = snapshots.get(idx);
            for (JavaConstruct con : sn.getJavaClassMembers()) {
                JavaMemberNode node = new JavaMemberNode(fgraph, con);
                fgraph.add(node);
            }
            
            if (monitor.isCanceled()) {
                monitor.done();
                throw new InterruptedException();
            }
            monitor.worked(1);
        }
    }
    
    /**
     * Collects forward and backward editing edges of an operation history graph.
     * @param fgraph the operation history graph
     * @param monitor the progress monitor to use to display progress and receive requests for cancellation
     * @throws InterruptedException if the operation detects a request to cancel
     */
    private static void collectDependencyEdges(OpGraphForFile fgraph, IProgressMonitor monitor) throws InterruptedException {
        List<OperationNode> nodes = fgraph.getOperationNodes();
        for (int idx = 0; idx < nodes.size(); idx++) {
            OperationNode opnode = nodes.get(idx);
            
            monitor.subTask("Collecting backward and forward editing edges: " +
                             String.valueOf(idx + 1) + "/" + String.valueOf(nodes.size()));
            for (JavaConstruct construct : opnode.getOperation().getBackwardJavaConstructs()) {
                JavaMemberNode jcnode = fgraph.getJavaConstructNode(construct);
                if (jcnode != null) {
                    OpGraphEdge edge = new OpGraphEdge(jcnode, opnode, OpGraphEdge.Sort.BACKWARD_EDITING);
                    fgraph.add(edge);
                }
            }
            for (JavaConstruct construct : opnode.getOperation().getForwardJavaConstructs()) {
                JavaMemberNode jcnode = fgraph.getJavaConstructNode(construct);
                if (jcnode != null) {
                    OpGraphEdge edge = new OpGraphEdge(opnode, jcnode, OpGraphEdge.Sort.FORWARD_EDITING);
                    fgraph.add(edge);
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
     * Collects ordered editing edges of an operation history graph.
     * @param fgraph the operation history graph
     * @param monitor the progress monitor to use to display progress and receive requests for cancellation
     * @throws InterruptedException if the operation detects a request to cancel
     */
    private static void collectOrderedEdges(OpGraphForFile fgraph, IProgressMonitor monitor) throws InterruptedException {
        List<OperationNode> nodes = fgraph.getOperationNodes();
        for (int idx = 0; idx < nodes.size(); idx++) {
            OperationNode opnode = nodes.get(idx);
            
            monitor.subTask("Collecting ordered editing edges: " +
                             String.valueOf(idx + 1) + "/" + String.valueOf(nodes.size()));
            for (ICodeOperation op : opnode.getOperation().getAffectingOperations()) {
                OperationNode node = fgraph.getOperationNode(op);
                OpGraphEdge edge = new OpGraphEdge(node, opnode, OpGraphEdge.Sort.ORDERED_EDITING);
                fgraph.add(edge);
            }
            
            if (monitor.isCanceled()) {
                monitor.done();
                throw new InterruptedException();
            }
            monitor.worked(1);
        }
    }
    
    /**
     * Collects no-change edges of an operation history graph.
     * @param fgraph the operation history graph
     * @param monitor the progress monitor to use to display progress and receive requests for cancellation
     * @exception InterruptedException if the operation detects a request to cancel
     */
    private static void collectNoChangeEdges(OpGraphForFile fgraph, IProgressMonitor monitor) throws InterruptedException {
        List<ParseableSnapshot> snapshots = fgraph.getSnapshots();
        if (snapshots.size() < 2) {
            return;
        }
        for (int idx = 0; idx < snapshots.size() - 1; idx++) {
            
            monitor.subTask("Collecting backward and forward editing edges: " +
                    String.valueOf(idx + 1) + "/" + String.valueOf(snapshots.size() - 1));
            ParseableSnapshot srcsn = snapshots.get(idx);
            ParseableSnapshot dstsn = snapshots.get(idx + 1);
            collectNoChangeEdgesByOffset(fgraph, srcsn, dstsn);
            collectNoChangeEdgesByName(fgraph, srcsn, dstsn);
            
            if (monitor.isCanceled()) {
                monitor.done();
                throw new InterruptedException();
            }
            monitor.worked(1);
        }
    }
    
    /**
     * Collects no-change edges of an operation history graph based on offset values of Java constructs.
     * @param fgraph the operation history graph
     * @param srcsn the former snapshot that contains the Java constructs.
     * @param dstsn the latter snapshot that contains the Java constructs.
     */
    private static void collectNoChangeEdgesByOffset(OpGraphForFile fgraph, ParseableSnapshot srcsn, ParseableSnapshot dstsn) {
        for (JavaConstruct src : srcsn.getJavaClassMembers()) {
            for (JavaConstruct dst : dstsn.getJavaClassMembers()) {
                
                JavaMemberNode srcnode = fgraph.getJavaConstructNode(src);
                JavaMemberNode dstnode = fgraph.getJavaConstructNode(dst);
                if (noChange(srcnode, dstnode)) {
                    int srcoff = src.getStart();
                    int dstoff = DependencyDetector.adjustBackwardOffset(dst.getStart(), getOperations(srcsn, dstsn));
                    if (srcoff == dstoff && src.getSimpleName().equals(dst.getSimpleName())) {
                        OpGraphEdge edge = new OpGraphEdge(srcnode, dstnode, OpGraphEdge.Sort.NO_CHANGE);
                        fgraph.add(edge);
                    }
                }
            }
        }
    }
    
    /**
     * Collects no-change edges of an operation history graph based on offset values of Java constructs.
     * @param fgraph the operation history graph
     * @param srcsn the former snapshot that contains the Java constructs.
     * @param dstsn the latter snapshot that contains the Java constructs.
     */
    private static void collectNoChangeEdgesByName(OpGraphForFile fgraph, ParseableSnapshot srcsn, ParseableSnapshot dstsn) {
        for (JavaConstruct src : srcsn.getJavaClassMembers()) {
            for (JavaConstruct dst : dstsn.getJavaClassMembers()) {
                
                JavaMemberNode srcnode = fgraph.getJavaConstructNode(src);
                JavaMemberNode dstnode = fgraph.getJavaConstructNode(dst);
                if (noChange(srcnode, dstnode)) {
                    if (src.getSimpleName().equals(dst.getSimpleName())) {
                        OpGraphEdge edge = new OpGraphEdge(srcnode, dstnode, OpGraphEdge.Sort.NO_CHANGE);
                        fgraph.add(edge);
                    }
                }
            }
        }
    }
    
    /**
     * Tests if Java construct nodes relate to any change.
     * @param srcnode the Java construct node within a former snapshot
     * @param dstnode the Java construct node within a latter snapshot
     * @return <code>true></code> if no changes affected the Java constructs, otherwise <code>false</code>
     */
    private static boolean noChange(JavaMemberNode srcnode, JavaMemberNode dstnode) {
        int NumOfCopyNodes = 0;
        for (OpGraphNode node : srcnode.getDstNodes()) {
            if (node.isOperation() && ((OperationNode)node).getOperation().isCopy()) {
                NumOfCopyNodes++;
            }
        }
        int NumOfAddNodes = 0;
        for (OpGraphNode node : dstnode.getSrcNodes()) {
            if (node.isOperation() && ((OperationNode)node).getSrcNodes().size() == 0) {
                NumOfAddNodes++;
            }
        }
        
        int NumOfForwardNodes = srcnode.getDstNodes().size() - NumOfCopyNodes;
        int NumOfBackwardNodes = dstnode.getSrcNodes().size() - NumOfAddNodes;
        return NumOfForwardNodes == 0 || NumOfBackwardNodes == 0;
    }
    
    /**
     * Collects inter-edges across file operation history graphs within a project.
     * @param pgraph an operation history graph for the project
     */
    static void collectInterEdges(OpGraphForProject pgraph) {
        CTProject pinfo = pgraph.getProject();
        Job job = new Job("Collecting operations from history files") {
            
            /**
             * Executes this job. Returns the result of the execution.
             * @param monitor the progress monitor to use to display progress and receive requests for cancellation
             */
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    List<IChangeOperation> ops = pinfo.getOperations();
                    monitor.beginTask("Collecting inter-edges: " + pinfo.getName(), ops.size());
                    collectCCPEdges(pgraph, ops, monitor);
                    return Status.OK_STATUS;
                } catch (Exception e) {
                    CTConsole.println("Failed to collect inter-edges");
                    return Status.CANCEL_STATUS;
                } finally {
                    monitor.done();
                }
            }
        };
        job.schedule();
        System.out.println(pgraph.toStringOnEdges(OpGraphEdge.Sort.CCP_EDITING));
    }
    
    /**
     * Collects ccp-edges between the node for the copy/copy operation and the node for the paste operation.
     * @param projectGraph a operation history graph for the project
     * @param ops the collection of the change operations
     * @param monitor the progress monitor to use to display progress and receive requests for cancellation
     * @exception InterruptedException if the operation detects a request to cancel
     */
    private static void collectCCPEdges(OpGraphForProject pgraph, List<IChangeOperation> ops, IProgressMonitor monitor) throws InterruptedException {
        for (int idx = 0; idx < ops.size(); idx++) {
            monitor.subTask("Collecting ccp edges " + String.valueOf(idx + 1) + "/" + ops.size());
            IChangeOperation op = ops.get(idx);
            if (op.isDocument()) {
                DocumentOperation dop = (DocumentOperation)op;
                if (dop.isPaste()) {
                    ICodeOperation cc = dop.getCutOrCopyOperationForPaste();
                    if (cc != null) {
                        OpGraphNode srcNode = pgraph.getOperationNode((ICodeOperation)cc);
                        OpGraphNode dstNode = pgraph.getOperationNode((ICodeOperation)op);
                        if (srcNode != null && dstNode != null) {
                            OpGraphEdge edge = new OpGraphEdge(srcNode, dstNode, OpGraphEdge.Sort.CCP_EDITING);
                            if (!pgraph.contains(edge)) {
                                pgraph.add(edge);
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
        
        System.out.println(pgraph.toStringOnEdges((OpGraphEdge.Sort.CCP_EDITING)));
    }
    
    /**
     * Collects all code change operations that appear between two snapshots.
     * @param srcsn the former snapshot 
     * @param dstsn the latter snapshot
     * @return the collection of the code change operations, not including a change operation performed at the starting time.
     */
    private static List<CodeOperation> getOperations(ParseableSnapshot srcsn, ParseableSnapshot dstsn) {
        return CodeOperation.getOperations(srcsn.getFile(), srcsn.getTime(), dstsn.getTime());
    }
}
