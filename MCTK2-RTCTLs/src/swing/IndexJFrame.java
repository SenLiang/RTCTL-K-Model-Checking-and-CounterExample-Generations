package swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;


public class IndexJFrame extends JFrame implements MouseListener {
	// 获得屏幕的大小
	final static int width = Toolkit.getDefaultToolkit().getScreenSize().width;
	final static int height = Toolkit.getDefaultToolkit().getScreenSize().height;
	// 定义全局组件
	JPanel backgroundPanel;
	JPanel topPanel;
	JPanel topMenu,pageMenu;
	JPanel centerPanel;
	JLabel modelTexter, verifyUI;
	public static CtrlJPanel controlPanel;
	public static EditorJPanel editorPanel;
	public static VerifyJPanel verifyPanel;
	public IndexJFrame()
	{
		Image logoIcon = new ImageIcon(IndexJFrame.class.getResource("/swing/Icons/logo.png")).getImage();
		this.setIconImage(logoIcon);
		initBackgroundPanel();
		this.setTitle("  MCTK 2.0  ");
		this.setSize((int) (width * 0.8f), (int) (height * 0.8f));
		this.setVisible(true);
		this.setLocationRelativeTo(null);    // 此窗口将置于屏幕的中央。
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			// 按下窗口关闭钮事件处理
			public void windowClosing(WindowEvent e) {
				Object[] options = {"Exit", "Cancel"};
				int response = JOptionPane.showOptionDialog(null, "Do you want to exit MCTK ?",
						"Confirm Exit", JOptionPane.YES_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
				if (response == 0) {
					System.exit(0);
				}
			}
		});
	}

	// 初始化背景面板
	public void initBackgroundPanel() {
		backgroundPanel = new JPanel(new BorderLayout());
		backgroundPanel.setOpaque(true);//设置不透明

		initTop();
		initCenterPanel();
		backgroundPanel.add(topPanel, "North");
		backgroundPanel.add(centerPanel, "Center");
		this.add(backgroundPanel);
	}

	// 初始化顶部顶部面板
	public void initTop() {
		initTopMenu();
		initPageMenu();
		topPanel = new JPanel(new BorderLayout());
		topPanel.setPreferredSize(new Dimension(width, 90));
		topPanel.add(topMenu, "North");
		topPanel.add(pageMenu, "Center");
	}
	// 初始化顶部菜单
	public void initTopMenu() {
		topMenu = new JPanel(new BorderLayout());
		controlPanel=new CtrlJPanel(this);
	}

	// 初始化顶部菜单
	public void initPageMenu() {
		pageMenu = new JPanel(new FlowLayout());
		//topMenu.setPreferredSize(new Dimension(900, 40));
		//pageMenu.setOpaque(false);
		String[] nameStrings = {" SMV Editor ", " Verification "};
		modelTexter = CreateMenuLabel(modelTexter, nameStrings[0], "text", pageMenu);
		modelTexter.setName("Editor");
		//添加一横线
		JLabel line = new JLabel("<html>&nbsp;<font color='#D2D2D2'>|</font>&nbsp;</html>");
		pageMenu.add(line);

		verifyUI = CreateMenuLabel(verifyUI, nameStrings[1], "verify", pageMenu);
		verifyUI.setName("Verify");
	}
	// 初始化中心面板
	public void initCenterPanel() {
		centerPanel = new JPanel(new BorderLayout());
		modelTexter.setText("<html><font color='#336699' style='font-weight:bold'>" + " SMV Editor " + "</font>&nbsp;</html>");
		//centerPanel.setOpaque(false);// 设置控件透明
		editorPanel=new EditorJPanel(this);
		verifyPanel=new VerifyJPanel(this);
	}

	// 创建顶部菜单Label
	public JLabel CreateMenuLabel(JLabel jlabel, String text, String name, JPanel jpanel) {
		Icon icon = new ImageIcon(IndexJFrame.class.getResource("/swing/Icons/" + name + ".png"));
		jlabel = new JLabel(icon);
		jlabel.setText("<html><font color='black'>" + text + "</font>&nbsp;</html>");
		jlabel.addMouseListener(this);
		jlabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
		jpanel.add(jlabel);
		return jlabel;
	}
	// 创建编辑器面板
	public void creatEditor() {
		centerPanel.removeAll();
		centerPanel.add("West",editorPanel.rowScroll);
		centerPanel.add("Center",editorPanel.textScroll);
		centerPanel.updateUI();
	}
	// 创建验证面板
	public void creatVerifyUI() {
		centerPanel.removeAll();
		centerPanel.add(VerifyJPanel.HJPanel, "Center");
		centerPanel.updateUI();
	}
	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == modelTexter) {
			creatEditor();
			modelTexter.setText("<html><font color='#336699' style='font-weight:bold'>" + " SMV Editor " + "</font>&nbsp;</html>");
			verifyUI.setText("<html><font color='black'>" + " Verification " + "</font>&nbsp;</html>");
		} else if (e.getSource() == verifyUI) {
			creatVerifyUI();
			modelTexter.setText("<html><font color='black'>" + " SMV Editor " + "</font>&nbsp;</html>");
			verifyUI.setText("<html><font color='#336699' style='font-weight:bold'>" + " Verification " + "</font>&nbsp;</html>");
			verifyPanel.ReadSMVSpec();//读取SMV中文本属性
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

}
