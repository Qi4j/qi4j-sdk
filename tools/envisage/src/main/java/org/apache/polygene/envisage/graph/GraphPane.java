/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.polygene.envisage.graph;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import org.apache.polygene.envisage.event.LinkEvent;
import org.apache.polygene.envisage.event.LinkListener;
import org.apache.polygene.tools.model.descriptor.ApplicationDetailDescriptor;
import prefuse.data.Graph;

/**
 * Just a simple wrapper for ApplicationModel Graph Display
 */
public final class GraphPane
    extends JPanel
{
    private TreeGraphDisplay treeDisplay;
    private final StackedGraphDisplay stackedDisplay;
    private final List<GraphDisplay> displays;

    private JTabbedPane tabPane;
    private final JScrollPane scrollPane;

    public GraphPane()
    {
        treeDisplay = new TreeGraphDisplay();
        stackedDisplay = new StackedGraphDisplay();

        List<GraphDisplay> tmpList = new ArrayList<>( 2 );
        tmpList.add( treeDisplay );
        tmpList.add( stackedDisplay );
        displays = Collections.unmodifiableList( tmpList );

        scrollPane = new JScrollPane();
        scrollPane.setViewportView( stackedDisplay );
        int unitInc = 50;
        scrollPane.getVerticalScrollBar().setUnitIncrement( unitInc );
        scrollPane.getHorizontalScrollBar().setUnitIncrement( unitInc );

        tabPane = new JTabbedPane();
        tabPane.add( "Tree", treeDisplay );
        tabPane.add( "Stacked", scrollPane );

        this.setLayout( new BorderLayout() );
        add( tabPane, BorderLayout.CENTER );

        treeDisplay.addLinkListener( this::graphItemLinkActivated );

        stackedDisplay.addLinkListener( this::graphItemLinkActivated );

        this.addComponentListener( new ComponentAdapter()
        {
            @Override
            public void componentResized( ComponentEvent evt )
            {
                Dimension size = GraphPane.this.getSize();
                treeDisplay.setSize( size.width, size.height );
                tabPane.revalidate();
                tabPane.repaint();
            }
        } );
    }

    public void initPolygene( ApplicationDetailDescriptor descriptor )
    {
        Graph graph = GraphBuilder.buildGraph( descriptor );
        Dimension size = getSize();
        treeDisplay.setSize( size.width, size.height );
        treeDisplay.run( graph );

        graph = GraphBuilder.buildGraph( descriptor );
        stackedDisplay.setSize( size.width, size.height );
        stackedDisplay.run( graph );
    }

    public void refresh()
    {
        treeDisplay.run();
        stackedDisplay.run();
    }

    public List<GraphDisplay> getGraphDisplays()
    {
        return displays;
    }

    public void setSelectedValue( Object obj )
    {
        treeDisplay.setSelectedValue( obj );
        stackedDisplay.setSelectedValue( obj );
    }

    private void graphItemLinkActivated( LinkEvent evt )
    {
        if( evt.getSource().equals( treeDisplay ) )
        {
            stackedDisplay.setSelectedValue( evt.getObject() );
        }
        else if( evt.getSource().equals( stackedDisplay ) )
        {
            treeDisplay.setSelectedValue( evt.getObject() );
        }
    }

    /**
     * Add a listener from the list that's notified each time a change to the selection occurs.
     *
     * @param listener the LinkListener to add
     */
    public void addLinkListener( LinkListener listener )
    {
        treeDisplay.addLinkListener( listener );
        stackedDisplay.addLinkListener( listener );
    }

    /**
     * Remove a listener from the list that's notified each time a change to the selection occurs.
     *
     * @param listener the LinkListener to remove
     */
    public void removeLinkListener( LinkListener listener )
    {
        treeDisplay.removeLinkListener( listener );
        stackedDisplay.removeLinkListener( listener );
    }
}
