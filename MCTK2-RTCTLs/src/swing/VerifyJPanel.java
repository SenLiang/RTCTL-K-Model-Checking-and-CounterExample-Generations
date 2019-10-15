package swing;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import edu.wis.jtlv.env.Env;
import edu.wis.jtlv.env.core.smv.SMVParseException;
import edu.wis.jtlv.env.core.spec.InternalSpecLanguage;
import edu.wis.jtlv.env.module.ModuleException;
import edu.wis.jtlv.env.module.SMVModule;
import edu.wis.jtlv.env.spec.Spec;
import edu.wis.jtlv.env.spec.SpecException;
import edu.wis.jtlv.env.spec.SpecExp;
import edu.wis.jtlv.lib.AlgExceptionI;
import edu.wis.jtlv.lib.AlgResultI;
import edu.wis.jtlv.lib.AlgRunnerThread;
import edu.wis.jtlv.lib.mc.ATLsK.ATLsK_ModelCheckAlg;
import edu.wis.jtlv.lib.mc.ATLsK.GraphExplainATLsK;
import edu.wis.jtlv.lib.mc.LTL.LTLModelCheckAlg;
import edu.wis.jtlv.lib.mc.RTCTLK.GraphExplainRTCTLK;
import edu.wis.jtlv.lib.mc.RTCTLK.RTCTLKModelCheckAlg;
import edu.wis.jtlv.lib.mc.RTCTL_STAR.RTCTL_STAR_ModelCheckAlg;
import edu.wis.jtlv.lib.mc.RTCTL_STAR.ViewerExplainRTCTLs;
import edu.wis.jtlv.lib.mc.RTLTLK.RTLTLKModelCheckAlg;
import edu.wis.jtlv.old_lib.mc.ModelCheckException;
import org.antlr.runtime.RecognitionException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import sun.plugin2.util.ColorUtil;

import static edu.wis.jtlv.lib.AlgResultI.ResultStatus.failed;
import static edu.wis.jtlv.lib.AlgResultI.ResultStatus.succeed;
import static swing.IndexJFrame.controlPanel;
import static swing.IndexJFrame.editorPanel;
import static swing.Statistic.*;


public class VerifyJPanel implements ActionListener {

    IndexJFrame indexJFrame;
    MultiGraph graph;
    JPanel SpecJPanel, showGraph;
    JPopupMenu jPopMenu;
    JMenuItem copy, clear;
    Clipboard clipboard;
    JButton ExSpecButton, VerifyButton, ClearButton, SaveButton;
    Vector<JPanel> SPECVertor = new Vector<JPanel>();
    Vector<JPanel> LTLVertor = new Vector<JPanel>();
    Vector<JPanel> ATLSVertor = new Vector<JPanel>();
    Vector<JTextArea> SPECText = new Vector<JTextArea>(), LTLText = new Vector<JTextArea>(), ATLSText = new Vector<JTextArea>();


    JPanel SPEC;
    JTextArea specText = new JTextArea();// 文本窗格
    //JScrollPane specscroll;// 文本滚动条
    JButton specaButton, specmButton, specvButton, specwButton;

    JPanel LTLSPEC;
    JTextArea ltlText = new JTextArea();// 文本窗格
    JButton ltlaButton, ltlmButton, ltlvButton, ltlwButton;

    JPanel ATLSSPEC;
    JTextArea atlText = new JTextArea();// 文本窗格
    JButton atlaButton, atlmButton, atlvButton, atlwButton;

    public JTextPane ctext;// Console of verificatin
    JScrollPane scroll, consoleScroll;
    public static JSplitPane HJPanel;
    Icon plusIcon = new ImageIcon(CtrlJPanel.class.getResource("/swing/Icons/plusm.gif"));
    Icon minIcon = new ImageIcon(CtrlJPanel.class.getResource("/swing/Icons/minusm.gif"));
    Icon witIcon = new ImageIcon(CtrlJPanel.class.getResource("/swing/Icons/witm.gif"));
    Icon verIcon = new ImageIcon(CtrlJPanel.class.getResource("/swing/Icons/verm.gif"));
    final String tips = "Please input a SPEC...";
    final String ltltips = "Please input a LTLSPEC...";
    final String atltips = "Please input a RTCTL*SPEC...";
    SMVModule main;//read th smv model only once.
    private  Statistic getStat; //get the time consuming, memory, etc.
    public VerifyJPanel(IndexJFrame indexJFrame) {
        this.indexJFrame = indexJFrame;
        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        SpecJPanel = new JPanel(new BorderLayout());// (new GridLayout(3,1));
        SpecJPanel.setLayout(new BoxLayout(SpecJPanel, BoxLayout.Y_AXIS));

        JPanel VerifyClear = new JPanel();
        VerifyClear.setLayout(new BoxLayout(VerifyClear, BoxLayout.X_AXIS));
        ExSpecButton = createBtn("Extract Spec", "/swing/Icons/extract.png");
        VerifyButton = createBtn("Verify All", "/swing/Icons/verifyAll.png");
        ClearButton = createBtn("Delete All", "/swing/Icons/deleteAll.png");
        SaveButton = createBtn("Save All", "/swing/Icons/saveAll.png");

        VerifyClear.add(ExSpecButton);
        VerifyClear.add(Box.createRigidArea(new Dimension(110, 50)));
        VerifyClear.add(VerifyButton);
        VerifyClear.add(Box.createRigidArea(new Dimension(110, 50)));
        VerifyClear.add(ClearButton);
        VerifyClear.add(Box.createRigidArea(new Dimension(110, 50)));
        VerifyClear.add(SaveButton);
        SpecJPanel.add(VerifyClear, BorderLayout.NORTH);


        SPEC = new JPanel();
        SPEC.setLayout(new BoxLayout(SPEC, BoxLayout.Y_AXIS));
        JPanel specpanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        specmButton = new JButton("-");
        specaButton = new JButton("+");
        JLabel specLabel = new JLabel("        SPEC        ");
        specLabel.setForeground(Color.MAGENTA);
        specvButton = new JButton("Verify");
        specwButton = new JButton("Witness");

        specText.setPreferredSize(new Dimension(500, 45));
        specText.setText(tips);
        specText.setFont(new Font("标楷体", Font.TRUETYPE_FONT | Font.ITALIC, 15));
        specText.setLineWrap(true);
        specpanel.add(specaButton);
        specpanel.add(specmButton);
        specpanel.add(specLabel);
        specpanel.add(specText);
        specpanel.add(specvButton);
        specpanel.add(specwButton);
        specpanel.setPreferredSize(new Dimension(150, 60)); // 设置容器的大小
        specaButton.addActionListener(this);
        specmButton.addActionListener(this);
        specvButton.addActionListener(this);
        specwButton.addActionListener(this);
        SPEC.add(specpanel);
        SPECVertor.add(specpanel);
        SPECText.add(specText);
        specText.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (tips.equalsIgnoreCase(specText.getText())) {
                    specText.setText("");
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if ("".equals(specText.getText())) {
                    specText.setText(tips);
                }
            }
        });

        LTLSPEC = new JPanel();
        LTLSPEC.setLayout(new BoxLayout(LTLSPEC, BoxLayout.Y_AXIS));
        JPanel ltlpanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        ltlmButton = new JButton("-");
        ltlaButton = new JButton("+");
        JLabel ltlBar = new JLabel("      LTLSPEC      ");
        ltlBar.setForeground(Color.MAGENTA);
        ltlvButton = new JButton("Verify");
        ltlwButton = new JButton("Witness");

        ltlText.setPreferredSize(new Dimension(500, 45));
        ltlText.setText(ltltips);
        ltlText.setLineWrap(true);
        ltlText.setFont(new Font("标楷体", Font.TRUETYPE_FONT | Font.ITALIC, 15));
        ltlpanel.add(ltlaButton);
        ltlpanel.add(ltlmButton);
        ltlpanel.add(ltlBar);
        ltlpanel.add(ltlText);
        ltlpanel.add(ltlvButton);
        ltlpanel.add(ltlwButton);
        ltlpanel.setPreferredSize(new Dimension(150, 60));  // 设置容器的大小
        ltlaButton.addActionListener(this);
        ltlmButton.addActionListener(this);
        ltlvButton.addActionListener(this);
        ltlwButton.addActionListener(this);

        LTLSPEC.add(ltlpanel);
        LTLVertor.add(ltlpanel);
        LTLText.add(ltlText);
        ltlText.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (ltltips.equalsIgnoreCase(ltlText.getText())) {
                    ltlText.setText("");
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if ("".equals(ltlText.getText())) {
                    ltlText.setText(ltltips);
                }
            }
        });

        ATLSSPEC = new JPanel();
        ATLSSPEC.setLayout(new BoxLayout(ATLSSPEC, BoxLayout.Y_AXIS));
        JPanel rtctlpanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        atlmButton = new JButton("-");
        atlaButton = new JButton("+");
        JLabel rtctlBar = new JLabel("    RTCTL*SPEC    ");
        rtctlBar.setForeground(Color.MAGENTA);
        atlvButton = new JButton("Verify");
        atlwButton = new JButton("Witness");
        atlText.setPreferredSize(new Dimension(500, 45));
        atlText.setText(atltips);
        atlText.setLineWrap(true);
        atlText.setFont(new Font("标楷体", Font.TRUETYPE_FONT | Font.ITALIC, 15));

        rtctlpanel.add(atlaButton);
        rtctlpanel.add(atlmButton);
        rtctlpanel.add(rtctlBar);
        rtctlpanel.add(atlText);
        rtctlpanel.add(atlvButton);
        rtctlpanel.add(atlwButton);
        rtctlpanel.setPreferredSize(new Dimension(150, 60));  // 设置容器的大小
        atlaButton.addActionListener(this);
        atlmButton.addActionListener(this);
        atlvButton.addActionListener(this);
        atlwButton.addActionListener(this);
        ATLSSPEC.add(rtctlpanel);
        ATLSVertor.add(ltlpanel);
        ATLSText.add(atlText);
        atlText.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (atltips.equalsIgnoreCase(atlText.getText())) {
                    atlText.setText("");
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if ("".equals(atlText.getText())) {
                    atlText.setText(atltips);
                }
            }
        });
        SpecJPanel.add(SPEC);
        SpecJPanel.add(LTLSPEC);
        SpecJPanel.add(ATLSSPEC);
        scroll = new JScrollPane(SpecJPanel);

        ctext = new JTextPane();
        consoleScroll = new JScrollPane(ctext);
        insertDocument(ctext, "Verification information...", Color.BLUE, 1);
        consoleScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        copy = new JMenuItem("Copy(C)");
        clear = new JMenuItem("Clear");
        jPopMenu = new JPopupMenu();
        jPopMenu.add(copy);
        jPopMenu.add(clear);
        copy.addActionListener(this);
        clear.addActionListener(this);
        ctext.add(jPopMenu);
        ctext.setBorder(null);
        ctext.addMouseListener(new MyMouseListener());

        showGraph = new JPanel(new BorderLayout());//初始化
        HJPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        HJPanel.setLeftComponent(scroll);
        HJPanel.setRightComponent(consoleScroll);
        HJPanel.setDividerSize(5);//设置分隔条大小，以像素为单位
        HJPanel.setDividerLocation(950);

        SpecJPanel.setOpaque(true);
        ctext.setOpaque(true);
        scroll.setOpaque(true);
        consoleScroll.setOpaque(true);
        HJPanel.setOpaque(true);
    }
    /**
     * 创建工具栏按钮
     * @param text 按钮名称
     * @param icon 按钮图标所在路径
     * @return 返回添加样式和监听器后的按钮
     * @author LiangSen
     */
    public JButton createBtn(String text, String icon) {
        JButton btn = new JButton(text, new ImageIcon(IndexJFrame.class.getResource(icon)));
        btn.setUI(new BasicButtonUI());// 恢复基本视觉效果
        btn.setPreferredSize(new Dimension(100, 50));// 设置按钮大小
        btn.setForeground(Color.ORANGE);
        btn.setContentAreaFilled(false);// 设置按钮透明
        btn.setFont(new Font("粗体", Font.BOLD, 16));// 按钮文本样式
        btn.setMargin(new Insets(0, 0, 0, 0));// 按钮内容与边框距离
        btn.addActionListener(this);
        return btn;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == copy) {
            String temp = ctext.getSelectedText();
            StringSelection content = new StringSelection(temp);
            clipboard.setContents(content, null);
        } else if (e.getSource() == clear) {
            ctext.setText("");
            insertDocument(ctext, "Verification information...", Color.BLUE, 1);
        } else if (e.getSource() == specaButton
                || e.getActionCommand().equals("specADD")) {
            NewSpec("");
        } else if (e.getSource() == ltlaButton
                || e.getActionCommand().equals("ltlADD")) {
            NewLTLSpec("");
        } else if (e.getSource() == atlaButton
                || e.getActionCommand().equals("atlADD")) {
            NewATLSpec("");
        } else if (e.getSource() == specmButton) {
            specText.setText("");
        } else if (e.getSource() == ltlmButton) {
            ltlText.setText("");
        } else if (e.getSource() == atlmButton) {
            atlText.setText("");
        } else if (e.getSource() == specvButton) {
            String specific = specText.getText();
            if (tips.equalsIgnoreCase(specific) || specific.equals("") || specific.trim().startsWith("--"))
                insertDocument(ctext, "\n Sorry,please input a specification !", Color.red, 2);
            else if (specific.endsWith(";"))
                GRun("SPEC ".concat(specific), false);
            else
                GRun("SPEC ".concat(specific) + ";", false);
        } else if (e.getSource() == specwButton) {
            String specific = specText.getText();
            if (tips.equalsIgnoreCase(specific) || specific.equals("") || specific.trim().startsWith("--"))
                insertDocument(ctext, "\n Sorry,please input a specification !", Color.red, 2);
            else if (specific.endsWith(";"))
                GRun("SPEC ".concat(specific), true);
            else
                GRun("SPEC ".concat(specific) + ";", true);
        } else if (e.getSource() == ltlvButton) {
            String specific = ltlText.getText();
            if (ltltips.equalsIgnoreCase(specific) || specific.equals("") || specific.trim().startsWith("--"))
                insertDocument(ctext, "\n Sorry,please input a specification !", Color.red, 2);
            else if (specific.endsWith(";"))
                GRun("LTLSPEC ".concat(specific), false);
            else
                GRun("LTLSPEC ".concat(specific) + ";", false);
        } else if (e.getSource() == ltlwButton) {
            String specific = ltlText.getText();
            if (ltltips.equalsIgnoreCase(specific) || specific.equals("") || specific.trim().startsWith("--"))
                insertDocument(ctext, "\n Sorry,please input a specification !", Color.red, 2);
            else if (specific.endsWith(";"))
                GRun("LTLSPEC ".concat(specific), true);
            else
                GRun("LTLSPEC ".concat(specific) + ";", true);
        } else if (e.getSource() == atlvButton) {
            String specific = atlText.getText();
            if (atltips.equalsIgnoreCase(specific) || specific.equals("") || specific.trim().startsWith("--"))
                insertDocument(ctext, "\n Sorry,please input a specification !", Color.red, 2);
            else if (specific.endsWith(";"))
                GRun("RTCTL*SPEC ".concat(specific), false);
            else
                GRun("RTCTL*SPEC ".concat(specific) + ";", false);
        } else if (e.getSource() == atlwButton) {
            String specific = atlText.getText();
            if (atltips.equalsIgnoreCase(specific) || specific.equals("") || specific.trim().startsWith("--"))
                insertDocument(ctext, "\n Sorry,please input a specification !", Color.red, 2);
            else if (specific.endsWith(";"))
                GRun("RTCTL*SPEC ".concat(specific), true);
            else
                GRun("RTCTL*SPEC ".concat(specific) + ";", true);
        } else if (((JButton) e.getSource()).getText().equals("Extract Spec")) {
            if (ExtractSpec()) {
                Object[] options = {"OK"};
                int response = JOptionPane.showOptionDialog(indexJFrame, "Read all specificatons from SMV successfully!",
                        "Tips", JOptionPane.YES_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            }else{
                Object[] options = {"OK"};
                int response = JOptionPane.showOptionDialog(indexJFrame, "Unable to read specifications, please check the model.",
                        "Error", JOptionPane.YES_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            }

        } else if (((JButton) e.getSource()).getText().equals("Verify All")) {
            String parse = GetAllSpec();
            if ("".equals(parse))
                insertDocument(ctext, "\n Sorry,please input a specification !", Color.red, 2);
            else
                GRun(parse, false);
        } else if (((JButton) e.getSource()).getText().equals("Delete All")) {
            while (SPECVertor.size() > 1) {
                SPEC.remove(SPECVertor.lastElement());
                SPECVertor.remove(SPECVertor.lastElement());
                SPECText.remove(SPECText.lastElement());
                SPEC.revalidate();
            }
            specText.setText("");
            while (LTLVertor.size() > 1) {
                LTLSPEC.remove(LTLVertor.lastElement());
                LTLVertor.remove(LTLVertor.lastElement());
                LTLText.remove(LTLText.lastElement());
                LTLSPEC.revalidate();
            }
            ltlText.setText("");

            while (ATLSVertor.size() > 1) {
                ATLSSPEC.remove(ATLSVertor.lastElement());
                ATLSVertor.remove(ATLSVertor.lastElement());
                ATLSText.remove(ATLSText.lastElement());
                ATLSSPEC.revalidate();
            }
            atlText.setText("");
        } else if (((JButton) e.getSource()).getText().equals("Save All")) {
            String parse = GetAllSpec(); //Read all the Specifications from SMV
            editorPanel.textModel.setText(editorPanel.textModel.getText() + parse);
            controlPanel.fileOperation.save();
            if (parse.equals("")) {
                Object[] options = {"OK"};
                int response = JOptionPane.showOptionDialog(indexJFrame, "Please input a specificaton firstly!",
                        "Error", JOptionPane.YES_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            }else{
                Object[] options = {"OK"};
                int response = JOptionPane.showOptionDialog(indexJFrame, "Save all specificatons to SMV successfully!",
                        "Tip", JOptionPane.YES_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            }
        }
    }

    private class MyMouseListener extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON3) {
                jPopMenu.show(ctext, e.getX(), e.getY());
            }

        }
    }

    protected void NewSpec(String property) {
        JPanel specp = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel specBar = new JLabel("        SPEC          ");
        JButton specmB = new JButton(minIcon);
        specmB.setContentAreaFilled(false);
        specmB.setBorderPainted(false);
        JButton specaB = new JButton(plusIcon);
        specaB.setContentAreaFilled(false);
        specaB.setBorderPainted(false);
        JButton specvB = new JButton(verIcon);
        specvB.setContentAreaFilled(false);
        specvB.setBorderPainted(false);
        JButton specwB = new JButton(witIcon);
        specwB.setContentAreaFilled(false);
        specwB.setBorderPainted(false);
        specaB.setActionCommand("specADD");

        String tips = "Please input a SPEC...";
        JTextArea specT = new JTextArea();
        specT.setPreferredSize(new Dimension(500, 45));
        specT.setLineWrap(true);
        if (property.equals(""))
            specT.setText(tips);
        else
            specT.setText(property);
        specT.setFont(new Font("标楷体", Font.TRUETYPE_FONT | Font.ITALIC, 15));
        specp.add(specaB);
        specp.add(specmB);
        specp.add(specBar);
        specp.add(specT);
        specp.add(specvB);
        specp.add(specwB);
        specp.setPreferredSize(new Dimension(150, 60));  // 设置容器的大小
        SPEC.add(specp);
        SPECVertor.add(specp);
        SPECText.add(specT);
        SPEC.revalidate();

        specT.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (tips.equalsIgnoreCase(specT.getText())) {
                    specT.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if ("".equals(specT.getText())) {
                    specT.setText(tips);
                }
            }
        });

        specaB.addActionListener(this);
        specmB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == specmB) {
                    SPEC.remove(specp);
                    int r = SPECVertor.indexOf(specp);
                    System.out.print(r);
                    SPECVertor.removeElementAt(r);
                    SPECText.removeElementAt(r);
                    SPEC.revalidate();
                }
            }
        });
        specvB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == specvB) {
                    String specific = specT.getText();
                    if (tips.equalsIgnoreCase(specific) || specific.equals("") || specific.trim().startsWith("--"))
                        insertDocument(ctext, "\n Sorry,please input a specification !", Color.red, 2);
                    else if (specific.endsWith(";"))
                        GRun("SPEC ".concat(specific), false);
                    else
                        GRun("SPEC ".concat(specific) + ";", false);

                }
            }
        });

        specwB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == specwB) {
                    String specific = specT.getText();
                    if (tips.equalsIgnoreCase(specific) || specific.equals("") || specific.trim().startsWith("--"))
                        insertDocument(ctext, "\n Sorry,please input a specification !", Color.red, 2);
                    else if (specific.endsWith(";"))
                        GRun("SPEC ".concat(specific), true);
                    else
                        GRun("SPEC ".concat(specific) + ";", true);
                }
            }
        });
    }

    protected void NewLTLSpec(String property) {
        JPanel specp = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel specBar = new JLabel("        LTLSPEC          ");
        JButton specmB = new JButton(minIcon);
        specmB.setContentAreaFilled(false);
        specmB.setBorderPainted(false);
        JButton specaB = new JButton(plusIcon);
        specaB.setContentAreaFilled(false);
        specaB.setBorderPainted(false);
        JButton specvB = new JButton(verIcon);
        specvB.setContentAreaFilled(false);
        specvB.setBorderPainted(false);
        JButton specwB = new JButton(witIcon);
        specwB.setContentAreaFilled(false);
        specwB.setBorderPainted(false);
        specaB.setActionCommand("ltlADD");

        String tips = "Please input a LTLSPEC...";
        JTextArea specT = new JTextArea();// 文本窗格
        specT.setPreferredSize(new Dimension(500, 45));
        specT.setLineWrap(true);
        if (property.equals(""))
            specT.setText(tips);
        else
            specT.setText(property);
        specT.setFont(new Font("标楷体", Font.TRUETYPE_FONT | Font.ITALIC, 15));
        specp.add(specaB);
        specp.add(specmB);
        specp.add(specBar);
        specp.add(specT);
        specp.add(specvB);
        specp.add(specwB);
        specp.setPreferredSize(new Dimension(150, 60));  // 设置容器的大小
        LTLSPEC.add(specp);
        LTLVertor.add(specp);
        LTLSPEC.revalidate();
        LTLText.add(specT);

        specT.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (tips.equalsIgnoreCase(specT.getText())) {
                    specT.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if ("".equals(specT.getText())) {
                    specT.setText(tips);
                }
            }
        });

        specaB.addActionListener(this);
        specmB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == specmB) {
                    LTLSPEC.remove(specp);
                    int r = LTLVertor.indexOf(specp);
                    LTLVertor.removeElementAt(r);
                    LTLText.removeElementAt(r);
                    LTLSPEC.revalidate();
                }
            }
        });
        specvB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == specvB) {
                    String specific = specT.getText();
                    if (tips.equalsIgnoreCase(specific) || specific.equals("") || specific.trim().startsWith("--"))
                        insertDocument(ctext, "\n Sorry,please input a specification !", Color.red, 2);
                    else if (specific.endsWith(";"))
                        GRun("LTLSPEC ".concat(specific), false);
                    else
                        GRun("LTLSPEC ".concat(specific) + ";", false);

                }
            }
        });

        specwB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == specwB) {
                    String specific = specT.getText();
                    if (tips.equalsIgnoreCase(specific) || specific.equals("") || specific.trim().startsWith("--"))
                        insertDocument(ctext, "\n Sorry,please input a specification !", Color.red, 2);
                    else if (specific.endsWith(";"))
                        GRun("LTLSPEC ".concat(specific), true);
                    else
                        GRun("LTLSPEC ".concat(specific) + ";", true);

                }
            }
        });
    }

    protected void NewATLSpec(String property) {
        JPanel specp = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel atlBar = new JLabel("       RTCTL*SPEC      ");
        JButton specmB = new JButton(minIcon);
        specmB.setContentAreaFilled(false);
        specmB.setBorderPainted(false);
        JButton specaB = new JButton(plusIcon);
        specaB.setContentAreaFilled(false);
        specaB.setBorderPainted(false);
        JButton specvB = new JButton(verIcon);
        specvB.setContentAreaFilled(false);
        specvB.setBorderPainted(false);
        JButton specwB = new JButton(witIcon);
        specwB.setContentAreaFilled(false);
        specwB.setBorderPainted(false);
        specaB.setActionCommand("atlADD");

        String tips = "Please input a RTCTL*SPEC...";
        JTextArea specT = new JTextArea();// 文本窗格
        specT.setPreferredSize(new Dimension(500, 45));
        specT.setLineWrap(true);

        if (property.equals(""))
            specT.setText(tips);
        else
            specT.setText(property);
        specT.setFont(new Font("标楷体", Font.TRUETYPE_FONT | Font.ITALIC, 15));
        specp.add(specaB);
        specp.add(specmB);
        specp.add(atlBar);
        specp.add(specT);
        specp.add(specvB);
        specp.add(specwB);
        specp.setPreferredSize(new Dimension(150, 60));  // 设置容器的大小
        ATLSSPEC.add(specp);
        ATLSVertor.add(specp);
        ATLSSPEC.revalidate();
        ATLSText.add(specT);
        specT.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (tips.equalsIgnoreCase(specT.getText())) {
                    specT.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if ("".equals(specT.getText())) {
                    specT.setText(tips);
                }
            }
        });
        specaB.addActionListener(this);
        specmB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == specmB) {
                    ATLSSPEC.remove(specp);
                    int r = ATLSVertor.indexOf(specp);
                    ATLSVertor.removeElementAt(r);
                    ATLSText.removeElementAt(r);
                    ATLSSPEC.revalidate();
                }
            }
        });

        specvB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == specvB) {
                    String specific = specT.getText();
                    if (tips.equalsIgnoreCase(specific) || specific.equals("") || specific.trim().startsWith("--"))
                        insertDocument(ctext, "\n Sorry,please input a specification !", Color.red, 2);
                    else if (specific.endsWith(";"))
                        GRun("RTCTL*SPEC ".concat(specific), false);
                    else
                        GRun("RTCTL*SPEC ".concat(specific) + ";", false);
                }
            }
        });
        specwB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == specwB) {
                    String specific = specT.getText();
                    if (tips.equalsIgnoreCase(specific) || specific.equals("") || specific.trim().startsWith("--"))
                        insertDocument(ctext, "\n Sorry,please input a specification !", Color.red, 2);
                    else if (specific.endsWith(";"))
                        GRun("RTCTL*SPEC ".concat(specific), true);
                    else
                        GRun("RTCTL*SPEC ".concat(specific) + ";", true);
                }
            }
        });
    }

    protected String GetAllSpec() {
        // TODO Auto-generated method stub
        String specstr = "";
        Iterator<JTextArea> itspec = SPECText.iterator();
        while (itspec.hasNext()) {
            String s = itspec.next().getText();
            if (!"".equals(s) && !s.equals("Please input a SPEC...") && !s.trim().startsWith("--"))//除去注释
                if (!s.endsWith(";"))
                    specstr += "\nSPEC " + s + ";";
                else
                    specstr += "\nSPEC " + s;
        }

        String ltlstr = "";
        Iterator<JTextArea> itltl = LTLText.iterator();
        while (itltl.hasNext()) {
            String s = itltl.next().getText();
            if (!"".equals(s) && !s.equals("Please input a LTLSPEC...") && !s.trim().startsWith("--"))
                if (!s.endsWith(";"))
                    specstr += "\nLTLSPEC " + s + ";";
                else
                    specstr += "\nLTLSPEC " + s;
        }
        //System.out.println("VerifyButton!"+ltlstr);

        String atlstr = "";
        Iterator<JTextArea> itatll = ATLSText.iterator();
        while (itatll.hasNext()) {
            String s = itatll.next().getText();
            if (!"".equals(s) && !s.equals("Please input a ATL*SPEC...") && !s.trim().startsWith("--"))
                if (!s.endsWith(";"))
                    specstr += "\nRTCTL*SPEC " + s + ";";
                else
                    specstr += "\nRTCTL*SPEC " + s;
        }
        return specstr + ltlstr + atlstr;
    }

    public void ReadSMVSpec() {
        String name = controlPanel.fileOperation.getFileName();
        if (name.equals("")) {
            Object[] options = {"OK"};
            JOptionPane.showOptionDialog(null, "Sorry,please input the model file first!",
                    "Warm Tips", JOptionPane.YES_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
        } else {
            String src = controlPanel.fileOperation.getPath();
            String url = src + name + ".smv";
            controlPanel.fileOperation.save();    //Save the file you are editing first.
            Env.resetEnv();
            try {

                Env.loadModule(url);
                main = (SMVModule) Env.getModule("main");
                main.setFullPrintingMode(true);
                insertDocument(ctext, "\n =======Done Loading Modules========", Color.GREEN, 1);

            } catch (Exception ie) {
                ie.printStackTrace();

                Object[] options = {"OK"};
                JOptionPane.showOptionDialog(null, "Syntax error,please check the model file first!",
                        "Warm Tips", JOptionPane.YES_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
            }finally {
                getStat=new Statistic();
                insertDocument(ctext, "\nInitial occupation of building model...\nNumber of BDD nodes:" + getStat.modelBDD +
                        "\nNumber of BDD variables:"+getStat.modelVar, Color.GRAY, 1);
            }

        }
    }

    public boolean ExtractSpec() {
        String[] all_specs = Env.getAllSpecsString();
        if (all_specs == null || all_specs.length == 0) {
            insertDocument(ctext, "\n =========No Specs loaded=========", Color.GREEN, 1);
            return false;
        } else
            insertDocument(ctext, "\n =====Automatic Loading Specs=======", Color.GREEN, 1);
        for (int i = 0; i < all_specs.length; i++) {
            if (all_specs[i].startsWith("SPEC") || all_specs[i].startsWith("CTLSPEC")) {
                if (tips.equals(specText.getText()))
                    specText.setText(all_specs[i].replaceAll("(CTLSPEC|SPEC)", "").toString());
                else
                    NewSpec(all_specs[i].replaceAll("(CTLSPEC|SPEC)", "").toString());
            } else if (all_specs[i].startsWith("LTLSPEC")) {
                if (ltltips.equals(ltlText.getText()))
                    ltlText.setText(all_specs[i].replaceAll("LTLSPEC", "").toString());
                else
                    NewLTLSpec(all_specs[i].replaceAll("LTLSPEC", "").toString());
            } else if (all_specs[i].startsWith("RTCTL*SPEC")) {
                if (atltips.equals(atlText.getText()))
                    atlText.setText(all_specs[i].replaceAll("RTCTL\\*SPEC", "").toString());
                else
                    NewATLSpec(all_specs[i].replaceAll("RTCTL\\*SPEC", "").toString());
            }
        }
        return true;
    }


    public static void insertDocument(JTextPane JTP, String str, Color textColor, int setFont)// 根据传入的颜色及文字，将文字插入控制台
    {
        SimpleAttributeSet set = new SimpleAttributeSet();
        StyleConstants.setForeground(set, textColor);// 设置文字颜色
        StyleConstants.setFontSize(set, 18);// 设置字体大小
        switch (setFont) {
            case 1://正常输出
                StyleConstants.setFontFamily(set, "新宋体");
            case 2://提示，警告，异常
                StyleConstants.setFontFamily(set, "标楷体");
            case 3://错误提示
                StyleConstants.setFontFamily(set, "华文行楷");
            default:
                StyleConstants.setFontFamily(set, "微软雅黑");
        }
        Document doc = JTP.getDocument();
        try {
            doc.insertString(doc.getLength(), str, set);// 插入文字
        } catch (BadLocationException e) {
        }
    }

    public void GRun(String parse, Boolean isgraph) {
        System.out.println("GRun----------"+parse);
        Spec[] all_specs = Env.loadSpecString(parse);
        if (all_specs == null||all_specs[0]==null) {
            insertDocument(ctext, "\n Sorry,please input correct specifications...", Color.RED, 1);
            return;
        }
        String[] SpecStr = parse.split(";");
        insertDocument(ctext, "\n ======DONE Loading Specs=========", Color.ORANGE, 1);
        AlgRunnerThread runner;
        for (int i = 0; i < all_specs.length; i++) {
            getStat.startBDDVar();
            getStat.startTimeMemory();
            insertDocument(ctext, "\n model checking " + SpecStr[i], Color.BLACK, 1);
            if (all_specs[i].getLanguage() == InternalSpecLanguage.CTL) {
                RTCTLKModelCheckAlg algorithm = new RTCTLKModelCheckAlg(main, all_specs[i]);
                if (isgraph) {//return result & graph
                    algorithm.SetShowGraph(true);
                    runner = new AlgRunnerThread(algorithm);
                    runner.runSequential();
                    if (runner.getDoResult().getResultStat() == failed) {//结果false，否则无图形反例
                        this.graph = algorithm.GetGraph();
                        SetGraphThread x = new SetGraphThread(SpecStr[i], this.graph, this.indexJFrame);
                        Thread y = new Thread(x);
                        y.start();
                    }
                } else {//return result
                    runner = new AlgRunnerThread(algorithm);
                    runner.runSequential();
                }
                if (runner.getDoResult() != null)
                    insertDocument(ctext, "\n" + runner.getDoResult().resultString() +
                            "\n" + getStat.endTime() + getStat.endBDD() +getStat.endVar()+ getStat.endMemory(), Color.BLACK, 1);
                else if (runner.getDoException() != null)
                    insertDocument(ctext, "\n" + runner.getDoException().getMessage(), Color.RED, 1);

            } else if (all_specs[i].getLanguage() == InternalSpecLanguage.RTCTLs || all_specs[i].getLanguage() == InternalSpecLanguage.LTL) {
                RTCTL_STAR_ModelCheckAlg algorithm = new RTCTL_STAR_ModelCheckAlg(main, all_specs[i]);
                if (isgraph) {//带图的反例
                    algorithm.SetShowGraph(true);
                    runner = new AlgRunnerThread(algorithm);
                    runner.runSequential();
                    if (runner.getDoResult().getResultStat() == failed) {//结果false，否则无图形反例
                        this.graph = algorithm.GetGraph();
                        SetGraphThread x = new SetGraphThread(SpecStr[i], this.graph, this.indexJFrame);
                        Thread y = new Thread(x);
                        y.start();
                    }
                } else {
                    runner = new AlgRunnerThread(algorithm);
                    runner.runSequential();
                }


                if (runner.getDoResult() != null)
                    insertDocument(ctext, "\n" + runner.getDoResult().resultString() +
                            "\n" + getStat.endTime() + getStat.endBDD() +getStat.endVar()+ getStat.endMemory(), Color.BLACK, 1);
                else if (runner.getDoException() != null)
                    insertDocument(ctext, "\n" + runner.getDoException().getMessage(), Color.RED, 1);
            }
        }
    }
}

