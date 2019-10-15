package swing;

import edu.wis.jtlv.env.core.smv.SMVParseException;
import edu.wis.jtlv.env.module.ModuleException;
import edu.wis.jtlv.env.spec.SpecException;
import edu.wis.jtlv.lib.mc.ATLsK.GraphExplainATLsK;
import edu.wis.jtlv.lib.mc.ModelCheckAlgException;
import edu.wis.jtlv.lib.mc.RTCTLK.GraphExplainRTCTLK;
import edu.wis.jtlv.lib.mc.RTCTLK.ViewerExplainRTCTLK;
import edu.wis.jtlv.lib.mc.RTCTL_STAR.GraphExplainRTCTLs;
import edu.wis.jtlv.old_lib.mc.ModelCheckException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static swing.VerifyJPanel.insertDocument;

public class SetGraphThread implements Runnable, ViewerListener, ActionListener {
    MultiGraph graph;
    IndexJFrame indexJFrame;
    JFrame newJFrame;
    JTextPane nodeConsole = new JTextPane();

    JPanel graphJPanel;
    JSplitPane GraphNodeJSP = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    JScrollPane nodeScroll = new JScrollPane(nodeConsole);
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    JPopupMenu jPopMenu = new JPopupMenu();
    JMenuItem copy = new JMenuItem("Copy(C)"), clear = new JMenuItem("Clear");
    protected boolean loop = true;

    public SetGraphThread(String spec, MultiGraph graph, IndexJFrame indexJFrame) {
        this.graph = graph;
        this.indexJFrame = indexJFrame;
        // 创建一个新窗口
        newJFrame = new JFrame("Counter-example for " + spec);
        newJFrame.setSize((int) (indexJFrame.width * 0.6f), (int) (indexJFrame.height * 0.7f));
        // 把新窗口的位置设置到 relativeWindow 窗口的中心
        newJFrame.setLocationRelativeTo(this.indexJFrame);
        Image logoIcon = new ImageIcon(IndexJFrame.class.getResource("/swing/Icons/logo.png")).getImage();
        newJFrame.setIconImage(logoIcon);
        // 点击窗口关闭按钮, 执行销毁窗口操作（如果设置为 EXIT_ON_CLOSE, 则点击新窗口关闭按钮后, 整个进程将结束）
        newJFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        // 窗口设置为不可改变大小 newJFrame.setResizable(false);
    }

    @Override
    public void run() {
        Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
        ViewPanel viewPanel = (ViewPanel) viewer.addDefaultView(false);
        graphJPanel = viewPanel;
        insertDocument(nodeConsole, "Node detail...", Color.BLUE, 1);
        GraphNodeJSP.setRightComponent(nodeScroll);
        GraphNodeJSP.setLeftComponent(graphJPanel);
        //重新绘制
        GraphNodeJSP.setDividerSize(5);//设置分隔条大小，以像素为单位
        GraphNodeJSP.setDividerLocation(800);
        //GraphNodeJSP.setEnabled(false);
        newJFrame.setContentPane(GraphNodeJSP);

        jPopMenu.add(copy);
        jPopMenu.add(clear);
        copy.addActionListener(this);
        clear.addActionListener(this);
        nodeConsole.add(jPopMenu);
        nodeConsole.setBorder(null);
        nodeConsole.addMouseListener(new MyMouseListener());
        newJFrame.setVisible(true);
        viewer.enableAutoLayout();
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        graph.addAttribute("ui.label", graph.getId());
        for (Node n : graph) {
            n.addAttribute("ui.label", n.getId());
        }
        graph.addAttribute("ui.stylesheet",
                "node { stroke-mode: plain; shape: circle; size: 55px; fill-color: green; z-index: 10; text-size: 22; text-style: bold; }" +
                        "node.initialState {fill-color: pink;} " +
                        "node.epistemicState {fill-color: gold;} " +
                        "edge { size: 3px; shape: line; fill-color: blue; arrow-size: 5px, 6px; arrow-shape: arrow; text-size: 18;" +
                        "text-background-mode: rounded-box; text-background-color: #fff7bc; text-alignment: at-left; text-padding: 2;} " +
                        "edge.epistemicEdge { fill-color: gold; shape: cubic-curve;} " +
                        "sprite {size: 0px;text-size: 20; text-alignment: at-right; }"
        );

        graph.setAutoCreate(false);
        graph.setStrict(false);


        // The default action when closing the view is to quit
        // the program.
        //viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);

        // We connect back the viewer to the graph,
        // the graph becomes a sink for the viewer.
        // We also install us as a viewer listener to
        // intercept the graphic events.
        ViewerPipe fromViewer = viewer.newViewerPipe();
        fromViewer.addViewerListener(this);
        fromViewer.addSink(graph);
        while (loop) {
            //fromViewer.pump();
            try {
                fromViewer.blockingPump();
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

    private class MyMouseListener extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON3) {
                jPopMenu.show(nodeConsole, e.getX(), e.getY());
            }
        }
    }

    public void viewClosed(String id) {
        loop = false;
    }

    public void buttonPushed(String id) {
        //Node n = graph.getNode(id);
        //System.out.println("-------- State "+id+" --------");
        insertDocument(nodeConsole, "\n" + "-------- State " + id + " --------", Color.BLACK, 1);
        String str;
        if (graph instanceof GraphExplainRTCTLK) {
            str = ((GraphExplainRTCTLK) graph).getNodeSatSpec(id);
            if (str != null && !str.equals(""))
                insertDocument(nodeConsole, "\n" + "[satisfies " + str + "]", Color.BLACK, 1);
            insertDocument(nodeConsole, "\n" + ((GraphExplainRTCTLK) graph).getNodeStateDetails(id), Color.BLACK, 1);
            try {
                ((GraphExplainRTCTLK) graph).getChecker().explainOneGraphNode(((GraphExplainRTCTLK) graph), id);
            } catch (ModelCheckAlgException e) {
                e.printStackTrace();
            }
        } else if (graph instanceof GraphExplainRTCTLs) {
            str = ((GraphExplainRTCTLs) graph).getNodeSatSpec(id);
            if (str != null && !str.equals(""))
                insertDocument(nodeConsole, "\n" + "[satisfies " + str + "]", Color.BLACK, 1);
            insertDocument(nodeConsole, "\n" + ((GraphExplainRTCTLs) graph).getNodeStateDetails(id), Color.BLACK, 1);
            try {
                ((GraphExplainRTCTLs) graph).getChecker().explainOneGraphNode(((GraphExplainRTCTLs) graph), id);
            } catch (ModelCheckAlgException e) {
                e.printStackTrace();
            } catch (SpecException e) {
                e.printStackTrace();
            } catch (ModelCheckException e) {
                e.printStackTrace();
            } catch (ModuleException e) {
                e.printStackTrace();
            } catch (SMVParseException e) {
                e.printStackTrace();
            }
        }
    }

    public void buttonReleased(String id) {
        //ctext.setText(ctext.getText() + "\n Button released on node " + id);
        //System.out.println("Button released on node "+id);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == copy) {
            String temp = nodeConsole.getSelectedText();
            StringSelection content = new StringSelection(temp);
            clipboard.setContents(content, null);
        } else if (e.getSource() == clear) {
            nodeConsole.setText("");
            insertDocument(nodeConsole, "Node detail...", Color.BLUE, 1);
        }
    }
}