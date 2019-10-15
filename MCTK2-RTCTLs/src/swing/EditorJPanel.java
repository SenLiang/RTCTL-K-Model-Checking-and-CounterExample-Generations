package swing;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;



public class EditorJPanel implements KeyListener, ActionListener, ChangeListener{
    IndexJFrame indexJFrame;
    static JTextPane rowNumber;
    static JTextPane textModel=new JTextPane();
    JScrollPane rowScroll,textScroll;
    ColorKeyWords cKeyWord;

    JPopupMenu jPopMenu;
    JPopupMenu cPopMenu;

    Clipboard clipboard;
    static StringBuffer rowContent;
    BoundedRangeModel model;

    Font  DeFont=new Font("TimesRoman",0,18);;
    public EditorJPanel(IndexJFrame indexJFrame)
    {
        this.indexJFrame=indexJFrame;
        clipboard=Toolkit.getDefaultToolkit().getSystemClipboard();
        textModel.setBorder(null);
        textScroll=new JScrollPane(textModel);
        rowNumber=new JTextPane(); rowNumber.setBorder(null);
        rowScroll=new JScrollPane(rowNumber);
        rowScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        jPopMenu=new JPopupMenu();
        cPopMenu=new JPopupMenu();
        cKeyWord=new ColorKeyWords(textModel);
        setFontStyle(DeFont);
        EditorInit();
    }


    public void EditorInit()
    {
        JMenuItem cut=new JMenuItem("Cut(X)");
        JMenuItem copy=new JMenuItem("Copy(C)");
        JMenuItem paste=new JMenuItem("Paste(V)");
        jPopMenu.add(cut);
        jPopMenu.add(copy);
        jPopMenu.add(paste);
        JMenuItem clear=new JMenuItem("Clear");
        cPopMenu.add(clear);
        cut.addActionListener(this);
        copy.addActionListener(this);
        paste.addActionListener(this);
        clear.addActionListener(this);

        textModel.add(jPopMenu);
        textModel.addMouseListener(new MyMouseListener());
        //+++++++++++++++++++++++++++++++++++++++++++++++++++
        textModel.getDocument().addDocumentListener(cKeyWord);

        rowNumber.setForeground(Color.lightGray);
        rowNumber.setText("1");
        rowNumber.setPreferredSize(new Dimension(45,  Toolkit.getDefaultToolkit().getScreenSize().height));
        rowNumber.setEnabled(false);
        rowNumber.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);


        indexJFrame.centerPanel.add("West",rowScroll);
        indexJFrame.centerPanel.add("Center",textScroll);
        textModel.addKeyListener(this);
        model=textScroll.getVerticalScrollBar().getModel();
        model.addChangeListener(this);
    }
    public void setFontStyle(Font font)
    {
        textModel.setFont(font);
        rowNumber.setFont(font);
        setRowContent();
    }
    @Override
    public void keyPressed(KeyEvent e) { }
    @Override
    public void keyReleased(KeyEvent e) {
        if ((e.isControlDown() == true) && ((e.getKeyCode() == KeyEvent.VK_V)|(e.getKeyCode() == KeyEvent.VK_X)))
            setRowContent();//检测到键盘输入，更新行号
        StringBuffer s=new StringBuffer(textModel.getText());//获取当前文本内容
        if(e.getKeyCode()==9)//修改tab缩进值
        {
            int pos=textModel.getCaretPosition();
            StyledDocument doc = textModel.getStyledDocument();
            Style style = doc.addStyle("normalstyle", null);
            try {
                doc.remove(pos-1, 1);
                doc.insertString(pos-1, "   ", style);
            } catch (BadLocationException be) {
                be.printStackTrace();
            }
        }
    }
    @Override
    public void keyTyped(KeyEvent e) {
        char x=e.getKeyChar();//获取当前键盘输入符号
        String s=textModel.getText().replaceAll("\n", "");//获取当前文本内容
        int pos=textModel.getCaretPosition();
        if(x=='\n')
        {
            setRowContent();
            tabTime();
            if(pos<s.length()&&s.charAt(pos)=='}')
            {
                StyledDocument doc = textModel.getStyledDocument();
                try {
                    doc.remove(pos+3, 3);
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
            }
            return ;
        }
        if(e.getKeyChar()=='}'&&s.charAt(pos-1)==' ')//当未回车插入}时不进行此操作
        {
            StyledDocument doc = textModel.getStyledDocument();
            try {
                doc.remove(pos-3, 3);
            } catch (BadLocationException e1) {
                e1.printStackTrace();
            }
        }
    }

    public static void setRowContent()
    {
        rowContent=new StringBuffer();
        Element map = textModel.getDocument().getDefaultRootElement();
        int count=map.getElementCount();
        rowNumber.setText("");
        for(int i=0;i<count;i++)
        {
            rowContent.append((i+1)+"\n");
        }
        rowNumber.setText(rowContent.toString());
    }

    public void tabTime() //缩进处理
    {
        StyledDocument doc = textModel.getStyledDocument();
        Style style = doc.addStyle("normalstyle", null);
        int tabNum=0;
        String sx= textModel.getText().replaceAll("\n", "");
        int pos=textModel.getCaretPosition();
        for(int i=0;i<pos;i++)
        {
            if(sx.charAt(i)=='{')
            {
                tabNum++;
            }
            if(sx.charAt(i)=='}')
            {
                tabNum--;
            }
        }
        for(int i=0;i<tabNum;i++)
        {
            try {
                doc.insertString(textModel.getCaretPosition(), "   ", style);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String name= e.getActionCommand();
        if(name.equals("Cut(X)"))
        {
            String temp=textModel.getSelectedText();
            StringSelection content = new StringSelection(temp);
            clipboard.setContents(content,null);
            int x=textModel.getSelectionStart();
            int y=textModel.getSelectionEnd();
            StyledDocument doc = textModel.getStyledDocument();
            try {
                doc.remove(x,y-x);
            } catch (BadLocationException be) {
                be.printStackTrace();
            }
            setRowContent();//更新行号
        }
        if(name.equals("Paste(V)"))
        {
            Transferable contents = clipboard.getContents(this);
            DataFlavor flavor = DataFlavor.stringFlavor;
            String str="";
            if(contents.isDataFlavorSupported(flavor))
                try{
                    str = (String)contents.getTransferData(flavor);
                }catch(Exception ee){}

            int pos=textModel.getCaretPosition();
            StyledDocument doc = textModel.getStyledDocument();
            Style style = doc.addStyle("normalstyle", null);
            try {
                doc.insertString(pos, str, style);
            } catch (BadLocationException be) {
                be.printStackTrace();
            }
            setRowContent();//更新行号
        }
        if(name.equals("Copy(C)"))
        {
            String temp=textModel.getSelectedText();
            StringSelection content = new StringSelection(temp);
            clipboard.setContents(content,null);
        }
    }

    private class MyMouseListener extends MouseAdapter
    {
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON3) {
                jPopMenu.show(textModel, e.getX(), e.getY());
            }
        }
    }
    @Override
    public void stateChanged(ChangeEvent e) {
        if(e.getSource()==model)
        {
            JScrollBar sBar = textScroll.getVerticalScrollBar();
            int x=sBar.getValue();
            JScrollBar sBar2 = rowScroll.getVerticalScrollBar();
            sBar2.setValue(x);
            rowScroll.setVerticalScrollBar(sBar2);
        }
    }
}
