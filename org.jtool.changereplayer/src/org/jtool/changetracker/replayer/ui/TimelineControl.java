/*
 *  Copyright 2017-2019
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.replayer.ui;

import org.jtool.changetracker.repository.CTFile;
import org.jtool.changetracker.repository.TimeRange;
import org.jtool.changetracker.operation.IChangeOperation;
import org.jtool.changetracker.operation.ChangeOperation;
import org.jtool.changetracker.operation.FileOperation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import java.util.List;
import java.util.ArrayList;
import java.time.ZonedDateTime;
import java.time.Duration;

/**
 * A time-line bar for change operations.
 * @author Katsuhisa Maruyama
 */
public class TimelineControl {
    
    /**
     * A code change view that contains this table control.
     */
    protected CodeChangeView codeChangeView;
    
    /**
     * The collection of time ranges during which the file is opened.
     */
    protected List<FileOpenedRange> fileOpenedRanges = new ArrayList<FileOpenedRange>();
    
    /**
     * The canvas on which the time-line bar displays.
     */
    protected Canvas canvas;
    
    /**
     * The area of the time-line bar.
     */
    protected Rectangle area;
    
    /**
     * The scroll-bar of the time line bar.
     */
    protected ScrollBar scrollBar;
    
    /**
     * The value indicating the move distance by scrolling.
     */
    protected int moveX;
    
    /**
     * The constant value that indicates the height of the time-line bar.
     */
    protected final int TIMELINE_HEIGHT = 20;
    
    /**
     * The listener that receives an event related to the paint event.
     */
    protected PaintListenerImpl paintListener;
    
    /**
     * The listener that receives an event related to the mouse click event.
     */
    protected MouseClickListenerImpl mouseClickListener;
    
    /**
     * The listener that receives an event related to the mouse move event.
     */
    protected MouseMoveListenerImpl mouseMoveListener;
    
    /**
     * The listener that receives an event related to the mouse wheel event.
     */
    private MouseWheelListenerImpl mouseWheelListener;
    
    /**
     * The listener that receives an event related to the selection events.
     */
    protected ScrollBarSelectionListenerImpl scrollBarSelectionListener;
    
    /**
     * Creates a time-time bar.
     * @param view the code change view that contains the table control
     */
    public TimelineControl(CodeChangeView view) {
        codeChangeView = view;
    }
    
    /**
     * Creates a control of a time-time bar.
     * @param parent the parent control
     */
    public void createTimeline(Composite parent) {
        canvas = new Canvas(parent, SWT.DOUBLE_BUFFERED | SWT.H_SCROLL);
        paintListener = new PaintListenerImpl();
        canvas.addPaintListener(paintListener);
        mouseClickListener = new MouseClickListenerImpl();
        canvas.addMouseListener(mouseClickListener);
        mouseMoveListener = new MouseMoveListenerImpl();
        canvas.addMouseMoveListener(mouseMoveListener);
        mouseWheelListener = new MouseWheelListenerImpl();
        canvas.addListener(SWT.MouseWheel, mouseWheelListener);
        scrollBar = canvas.getHorizontalBar();
        scrollBar.setVisible(true);
        scrollBar.setMinimum(0);
        scrollBar.setEnabled(true);
        scrollBarSelectionListener = new ScrollBarSelectionListenerImpl();
        scrollBar.addSelectionListener(scrollBarSelectionListener);
        moveX = 0;
        
        FormData data = new FormData();
        data.top = new FormAttachment(0, 0);
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(100, 0);
        data.height = TIMELINE_HEIGHT;
        canvas.setLayoutData(data);
    }
    
    /**
     * Returns the control for the time-line bar.
     * @return the time-line bar control
     */
    public Control getControl() {
        return canvas;
    }
    
    /**
     * Sets the focus to the control of the time-line bar.
     */
    public void setFocus() {
        canvas.setFocus();
    }
    
    /**
     * Disposes the control of the time-line bar.
     */
    public void dispose() {
        if (!canvas.isDisposed()) {
            canvas.removePaintListener(paintListener);
            canvas.removeMouseListener(mouseClickListener);
            canvas.removeMouseMoveListener(mouseMoveListener);
            canvas.removeListener(SWT.MouseWheel, mouseWheelListener);
        }
        if (!scrollBar.isDisposed()) {
            scrollBar.removeSelectionListener(scrollBarSelectionListener);
        }
        canvas.dispose();
        scrollBar.dispose();
    }
    
    /**
     * Selects a change operation in the time-line bar.
     */
    public void select() {
        reveal();
        canvas.redraw();
        canvas.update();
    }
    
    /**
     * Updates this view.
     */
    public void update() {
        if (!codeChangeView.readyToVisualize()) {
            return;
        }
        collectFileOpenedRanges();
        redraw();
    }
    
    /**
     * Resets this view.
     */
    public void reset() {
        if (!canvas.isDisposed()) {
            fileOpenedRanges.clear();
            redraw();
        }
    }
    
    /**
     * Redraws this view.
     */
    public void redraw() {
        updateFileOpenedTimeRange();
        canvas.redraw();
        canvas.update();
    }
    
    /**
     * Collects time ranges during which a file is opened.
     */
    protected void collectFileOpenedRanges() {
        CTFile finfo = codeChangeView.getFile();
        fileOpenedRanges.clear();
        List<IChangeOperation> ops = finfo.getOperations();
        IChangeOperation from = finfo.getFirstOperation();
        IChangeOperation to = finfo.getLastOperation();
        for (int idx = 0; idx < ops.size(); idx++) {
            IChangeOperation op = ops.get(idx);
            if (op.isFile()) {
                FileOperation fop = (FileOperation)op;
                if (fop.isOpen() || fop.isAdd()) {
                    from = op;
                } else if (fop.isClose() || fop.isDelete() || idx == ops.size() - 1) {
                    to = op;
                    fileOpenedRanges.add(new FileOpenedRange(from, to));
                }
            }
        }
        if (from != null && to != null && from.getTime().isEqual(to.getTime())) {
            to = ops.get(ops.size() - 1);
            fileOpenedRanges.add(new FileOpenedRange(from, to));
        }
    }
    
    /**
     * Updates the starting and ending times for this time-line bar.
     */
    protected void updateFileOpenedTimeRange() {
        final int GAP_FOR_TIME = 10;
        int scale = codeChangeView.getTimeScale();
        int x = 0;
        for (FileOpenedRange range : fileOpenedRanges) {
            range.setFirstPosition(x);
            x = x + (int)(range.getDurationAsMillis() / scale);
            range.setLastPosition(x);
            x = x + GAP_FOR_TIME;
        }
        if (fileOpenedRanges.size() == 0) {
            area = new Rectangle(0, 0, 0, TIMELINE_HEIGHT);
            return;
        }
        FileOpenedRange firstRange = fileOpenedRanges.get(0);
        FileOpenedRange lastRange = fileOpenedRanges.get(fileOpenedRanges.size() - 1);
        area = new Rectangle(0, 0, lastRange.getLastPosition() - firstRange.getFirstPosition(), TIMELINE_HEIGHT);
    }
    
    /**
     *Converts a specified x-position on the time-line bar to the time.
     * @param x the x-position
     * @return the time corresponding to the x-position, or <code>null</code> if the time was not found
     */
    protected ZonedDateTime point2time(int x) {
        x = x + moveX;
        for (FileOpenedRange range : fileOpenedRanges) {
            if (range.isBetween(x)) {
                double relpos = (double)(x - range.getFirstPosition() + 1) / (double)range.getDistance();
                long reltime = (long)(range.getDurationAsMillis() * relpos);
                ZonedDateTime time = range.afterFromTime(reltime);
                return time;
            }
        }
        return null;
    }
    
    /**
     * Converts a specified time to the x-position on the time-line bar.
     * @param time the time
     * @return the value of the x-position, or <code>-1</code> if the x-position was not found
     */
    protected int time2point(ZonedDateTime time) {
        for (FileOpenedRange range : fileOpenedRanges) {
            if (range.isBetween(time)) {
                double reltime = (double)range.afterFromTime(time) / (double)range.getDurationAsMillis();
                int relpos = (int)(range.getDistance() * reltime);
                int x = range.getFirstPosition() + relpos;
                return x - moveX;
            }
        }
        return -1;
    }
    
    /**
     * Returns the index number of a change operation with the time closest to a given time.
     * @param time the time
     * @return the index number of the change operation, or <code>-1</code> if none
     */
    protected int getPresentTime(ZonedDateTime time) {
        CTFile finfo = codeChangeView.getFile();
        if (finfo == null) {
            return -1;
        }
        
        int prev = finfo.getLastOperationIndexBefore(time);
        int next = finfo.getFirstOperationIndexAfter(time);
        if (prev != -1 && next != -1) {
            ZonedDateTime ptime = finfo.getOperation(prev).getTime();
            ZonedDateTime ntime = finfo.getOperation(next).getTime();
            Duration bduration = Duration.between(ptime, time);
            Duration aduration = Duration.between(time, ntime);
            if (bduration.compareTo(aduration) <= 0) {
                return prev;
            } else {
                return next;
            }
        } else if (prev != -1) {
            return prev;
        } else if (next != -1) {
            return next;
        }
        return -1;
    }
    
    /**
     * Ensures that the selected change operation is visible, scrolling the time-line bar if necessary.
     */
    protected void reveal() {
       if (area == null) {
           return;
       }
       int x = time2point(codeChangeView.getPresentTime()) + moveX;
       if (area.width > canvas.getBounds().width) {
           if (x < 10) {
               scrollBar.setSelection(0);
               moveX = scrollBar.getSelection();
           } else if (x < area.x + area.width) {
               scrollBar.setSelection(x - 10);
               moveX = scrollBar.getSelection();
           }
       }
    }
    
    /**
     * Represents the time period during which the file is opened.
     */
    protected class FileOpenedRange extends TimeRange {
        
        /**
         * The x-position of the first change operation.
         */
        protected int firstPosition;
        
        /**
         * The x-position of the last change operation.
         */
        protected int lastPosition;
        
        /**
         * Creates an instance that represents time period during which the file is live.
         * @param first the first change operation in the the time period
         * @param last the last change operation in the time period
         */
        protected FileOpenedRange(IChangeOperation first, IChangeOperation last) {
            super(first.getTime(), last.getTime());
        }
        
        /**
         * Sets the x-position of the first change operation.
         * @param x the x-position
         */
        protected void setFirstPosition(int x) {
            firstPosition = x;
        }
        
        /**
         * Returns the x-position of the first change operation.
         * @return x the x-position
         */
        protected int getFirstPosition() {
            return firstPosition;
        }
        
        /**
         * Sets the x-position of the last change operation.
         * @param x the x-position
         */
        protected void setLastPosition(int x) {
            lastPosition = x;
        }
        
        /**
         * Returns the x-position of the last change operation.
         * @return x the x-position
         */
        protected int getLastPosition() {
            return lastPosition;
        }
        
        /**
         * Tests if the x-position is inclusive between this range.
         * @param x
         * @return <code>true</code> if the x-position is inclusive between this range, otherwise <code>false</code>
         */
        protected boolean isBetween(int x) {
            return firstPosition <= x && x <= lastPosition;
        }
        
        /**
         * Returns the distance between the first and end positions.
         * @return the distance
         */
        protected int getDistance() {
            return lastPosition - firstPosition;
        }
    }
    
    /**
     * Deals with a paint event.
     */
    protected class PaintListenerImpl implements PaintListener {
        
        /**
         * Creates a listener that deals with a paint event.
         */
        PaintListenerImpl() {
        }
        
        /**
         * Receives the paint event when a paint event occurs for the control.
         * @param evt the event containing information about the paint
         */
        @Override
        public void paintControl(PaintEvent evt) {
            if (!codeChangeView.readyToVisualize()) {
                return;
            }
            
            GC gc = evt.gc;
            Display display = canvas.getDisplay();
            if (area != null) {
                draw(gc, display);
                scrollBar.setMaximum(area.width + 1);
                scrollBar.setThumb(Math.min(area.width, canvas.getBounds().width));
            }
        }
        
        /**
         * Draws this time-line bar.
         * @param gc the SWT drawing capabilities
         * @param device the draw-able device
         */
        protected void draw(GC gc, Device device) {
            Color bcolor = new Color(device, 255, 255, 255);
            gc.setBackground(bcolor);
            gc.fillRectangle(canvas.getBounds());
            
            Color acolor = new Color(device, 250, 240, 230);
            gc.setForeground(acolor);
            gc.setBackground(acolor);
            gc.fillGradientRectangle(area.x, area.y, area.width, area.height, true);
            gc.drawRectangle(area);
            
            for (FileOpenedRange range : fileOpenedRanges) {
                drawRect(gc, device, range);
            }
            for (IChangeOperation op : codeChangeView.getFile().getOperations()) {
                drawLine(gc, device, op);
            }
            drawPresentTime(gc, device);
        }
        
        /**
         * Draws a rectangle highlight on this time-line bar.
         * @param gc the drawing capabilities
         * @param device device the draw-able device
         * @param op a change operation corresponding to the rectangle highlight
         */
        protected void drawRect(GC gc, Device device, FileOpenedRange range) {
            int left = time2point(range.getFrom());
            int right = time2point(range.getTo());
            if (left != -1 && right != -1) {
                int width = right - left + 1;
                Color fcolor = new Color(device, 137, 221, 93);
                Color bcolor = new Color(device,198, 255, 134);
                gc.setForeground(fcolor);
                gc.setBackground(bcolor);
                gc.fillGradientRectangle(left, 0, width, TIMELINE_HEIGHT, true);
            }
        }
        
        /**
         * Draws a line highlight on this time-line bar.
         * @param gc the drawing capabilities
         * @param device device the draw-able device
         * @param op a change operation corresponding to the line highlight
         */
        protected void drawLine(GC gc, Device device, IChangeOperation op) {
            int x = time2point(op.getTime());
            if (x != -1) {
                Color fcolor = new Color(device,139, 105, 20);
                Color bcolor = new Color(device, 0, 0, 0);
                gc.setForeground(fcolor);
                gc.setBackground(bcolor);
                gc.drawLine(x, 0, x, TIMELINE_HEIGHT);
            }
        }
        
        /**
         * Draws this focal time triangle mark.
         * @param gc the drawing capabilities
         * @param device the draw-able device
         */
        protected void drawPresentTime(GC gc, Device device) {
            int center = time2point(codeChangeView.getPresentTime());
            int left = center - 3;
            int right = center + 3;
            int top = (TIMELINE_HEIGHT * 7) / 10;
            int bottom = TIMELINE_HEIGHT;
            int[] pointArray = new int[] { left, bottom, center, top, right, bottom, left, bottom };
            Color color = new Color(device, 255, 0, 0);
            gc.setForeground(color);
            gc.setBackground(color);
            gc.fillPolygon(pointArray);
            gc.drawPolygon(pointArray);
        }
    }
    
    /**
     * Deals with a mouse click event.
     */
    protected class MouseClickListenerImpl implements MouseListener {
        
        /**
         * Creates a listener that deals with a mouse click event.
         */
        MouseClickListenerImpl() {
        }
        
        /**
         * Receives the mouse event when a mouse button is pressed twice within the double click period.
         * @param evt the event containing information about the mouse double click
         */
        @Override
        public void mouseDoubleClick(MouseEvent evt) {
            if (!codeChangeView.readyToVisualize()) {
                return;
            }
            if (evt.y < 0 && canvas.getBounds().height < evt.y) {
                return;
            }
            
            ZonedDateTime time = point2time(evt.x);
            if (time == null) {
                return;
            }
            int index = getPresentTime(time);
            if (index != -1) {
                codeChangeView.goTo(index);
            }
        }
        
        /**
         * Receives the mouse event when a mouse button is pressed.
         * @param evt the event containing information about the mouse button press
         */
        @Override
        public void mouseDown(MouseEvent evt) {
        }
        
        /**
         * Receives the mouse event when a mouse button is released.
         * @param evt the event containing information about the mouse button release
         */
        @Override
        public void mouseUp(MouseEvent e) {
        }
    }
    
    /**
     * Deals with a mouse move event.
     */
    protected class MouseMoveListenerImpl implements MouseMoveListener {
        
        /**
         * Creates a listener that deals with a mouse move event.
         */
        MouseMoveListenerImpl() {
        }
        
        /**
         * Receives the mouse event when the mouse moves.
         * @param evt an event containing information about the mouse move
         */
        @Override
        public void mouseMove(MouseEvent evt) {
            if (!codeChangeView.readyToVisualize()) {
                return;
            }
            if (evt.y < 0 && canvas.getBounds().height < evt.y) {
                return;
            }
            
            ZonedDateTime time = point2time(evt.x);
            if (time == null) {
                return;
            }
            canvas.setToolTipText(ChangeOperation.getFormatedTime(time));
        }
    }
    
    /**
     * Deals with a mouse wheel event.
     */
    protected class MouseWheelListenerImpl implements Listener {
        
        /**
         * Creates a listener that deals with a mouse wheel event.
         */
        MouseWheelListenerImpl() {
        }
        
        /**
         * Receives the event when the mouse wheel moves.
         * @param evt an event containing information about the mouse wheel move
         */
        @Override
        public void handleEvent(Event evt) {
            if (!codeChangeView.readyToVisualize()) {
                return;
            }
            if (evt.y < 0 && canvas.getBounds().height < evt.y) {
                return;
            }
            
            if (evt.count > 0) {
                codeChangeView.zoominTimeScale();
            } else {
                codeChangeView.zoomoutTimeScale();
                if (moveX > canvas.getBounds().width) {
                    moveX = canvas.getBounds().width - scrollBar.getThumbBounds().width;
                } else {
                    moveX = 0;
                }
            }
            redraw();
        }
    }
    
    /**
     * Deals with a selection event.
     */
    protected class ScrollBarSelectionListenerImpl implements SelectionListener {
        
        /**
         * Creates a listener that deals with a selection event.
         */
        ScrollBarSelectionListenerImpl() {
        }
        
        /**
         * Receives the event when the default selection occurs in the control
         * @param evt the event containing information about the default selection
         */
        @Override
        public void widgetDefaultSelected(SelectionEvent evt) {
        }
        
        /**
         * Receives the event when the selection occurs in the control
         * @param evt the event containing information about the selection
         */
        @Override
        public void widgetSelected(SelectionEvent evt) {
            if (!codeChangeView.readyToVisualize()) {
                return;
            }
            
            if (area.width > canvas.getBounds().width) {
                moveX = scrollBar.getSelection();
                redraw();
            } else {
                moveX = 0;
            }
        }
    }
}
