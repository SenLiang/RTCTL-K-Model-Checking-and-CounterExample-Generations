package edu.wis.jtlv.lib.mc.RTCTLK;

import edu.wis.jtlv.lib.mc.ModelCheckAlgException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.graphicGraph.stylesheet.StyleConstants;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;

public class ViewerExplainRTCTLK implements ViewerListener {
    protected boolean loop = true;

    public GraphExplainRTCTLK getGraph() {
        return graph;
    }

    public void setGraph(GraphExplainRTCTLK graph) {
        this.graph = graph;
    }

    private GraphExplainRTCTLK graph;
    SpriteManager spriteMgr;

    public ViewerExplainRTCTLK(GraphExplainRTCTLK G) {
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        graph = G;

/*
        spriteMgr = new SpriteManager(graph);
        Sprite s = spriteMgr.addSprite("S1");
        s.addAttribute("ui.label", "S1's label");
        s.setPosition(StyleConstants.Units.PX, 30, 180, 0);
        s.attachToNode("1.0");
*/

        // We do as usual to display a graph. This
        // connect the graph outputs to the viewer.
        // The viewer is a sink of the graph.
/*
        graph = new SingleGraph("ViewerExplainRTCTLK");
        Node initNode;
        initNode = graph.addNode("A" );
        graph.addNode("B" );
        graph.addNode("C" );
        graph.addNode("D" );
        //  graph.addNode("E");

        graph.addEdge("AB", "A", "B", true);
        graph.addEdge("BC", "B", "C", true);
        graph.addEdge("CD", "C", "D", true);
        graph.addEdge("DC","D", "C", true);
*/

        graph.addAttribute("ui.label", graph.getId());

        for (Node n: graph) {
            n.addAttribute("ui.label", n.getId());
        }


//        for (Edge e: graph.getEachEdge()) {
//            e.addAttribute("ui.label", e.getId());
//        }


        graph.addAttribute("ui.stylesheet",
                "node { stroke-mode: plain; shape: circle; size: 40px; fill-color: green; z-index: 10; text-size: 11; }" +
                "node.initialState {fill-color: pink;} " +
                "node.epistemicState {fill-color: gold;} " +
                "edge { size: 2px; shape: line; fill-color: green; arrow-size: 8px, 6px; arrow-shape: arrow; }" +
                "edge.epistemicEdge { fill-color: gold; shape: cubic-curve;} " +
                        "sprite {size: 0px;}"
        );

        Viewer viewer = graph.display();

        // The default action when closing the view is to quit
        // the program.
        viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);

        // We connect back the viewer to the graph,
        // the graph becomes a sink for the viewer.
        // We also install us as a viewer listener to
        // intercept the graphic events.
        ViewerPipe fromViewer = viewer.newViewerPipe();
        fromViewer.addViewerListener(this);
        fromViewer.addSink(graph);

        // Then we need a loop to do our work and to wait for events.
        // In this loop we will need to call the
        // pump() method before each use of the graph to copy back events
        // that have already occurred in the viewer thread inside
        // our thread.

        while(loop) {
            fromViewer.pump(); // or fromViewer.blockingPump(); in the nightly builds

            // here your simulation code.

            // You do not necessarily need to use a loop, this is only an example.
            // as long as you call pump() before using the graph. pump() is non
            // blocking.  If you only use the loop to look at event, use blockingPump()
            // to avoid 100% CPU usage. The blockingPump() method is only available from
            // the nightly builds.
        }
    }

    public void viewClosed(String id) {
        loop = false;
    }

    public void buttonPushed(String id) {
        //Node n = graph.getNode(id);
        System.out.println("-------- State "+id+" --------");
        String str = graph.getNodeSatSpec(id);
        if(str!=null && !str.equals(""))
            System.out.println("[satisfies " + str + "]");
        System.out.println( graph.getNodeStateDetails(id));

        try {
            graph.getChecker().explainOneGraphNode(graph, id);
        } catch (ModelCheckAlgException e) {
            e.printStackTrace();
        }
    }

    public void buttonReleased(String id) {
        //System.out.println("Button released on node "+id);
    }
}