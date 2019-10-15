package edu.wis.jtlv.lib.mc.RTCTL_STAR;

import edu.wis.jtlv.env.core.smv.SMVParseException;
import edu.wis.jtlv.env.module.ModuleException;
import edu.wis.jtlv.env.spec.SpecException;
import edu.wis.jtlv.lib.mc.ModelCheckAlgException;
import edu.wis.jtlv.old_lib.mc.ModelCheckException;
import org.graphstream.graph.Node;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;

public class ViewerExplainRTCTLs implements ViewerListener {
    protected boolean loop = true;

    public GraphExplainRTCTLs getGraph() {
        return graph;
    }

    public void setGraph(GraphExplainRTCTLs graph) {
        this.graph = graph;
    }

    private GraphExplainRTCTLs graph;

    public ViewerExplainRTCTLs(GraphExplainRTCTLs G) {
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        graph = G;
        graph.addAttribute("ui.label", graph.getId());

        for (Node n: graph) {
            n.addAttribute("ui.label", n.getId());
        }


//        for (Edge e: graph.getEachEdge()) {
//            e.addAttribute("ui.label", e.getId());
//        }


        graph.addAttribute("ui.stylesheet",
                "node { stroke-mode: plain; shape: circle; size: 55px; fill-color: green; z-index: 10; text-size: 22; text-style: bold; }" +
                "node.initialState {fill-color: pink;} " +
                "node.epistemicState {fill-color: gold;} " +
                "edge { size: 3px; shape: line; fill-color: blue; arrow-size: 5px, 6px; arrow-shape: arrow; text-size: 18;" +
                        "text-background-mode: rounded-box; text-background-color: #fff7bc; text-alignment: at-left; text-padding: 2;} " +
                "edge.epistemicEdge { fill-color: gold; shape: cubic-curve;} " +
                        "sprite {size: 0px;text-size: 20; text-alignment: at-right; }"
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
            try {
                graph.getChecker().explainOneGraphNode(graph,id);
            } catch (ModelCheckException e) {
                e.printStackTrace();
            } catch (SpecException e) {
                e.printStackTrace();
            } catch (SMVParseException e) {
                e.printStackTrace();
            } catch (ModuleException e) {
                e.printStackTrace();
            }
        } catch (ModelCheckAlgException e) {
            e.printStackTrace();
        }

    }

    public void buttonReleased(String id) {
        //System.out.println("Button released on node "+id);
    }
}