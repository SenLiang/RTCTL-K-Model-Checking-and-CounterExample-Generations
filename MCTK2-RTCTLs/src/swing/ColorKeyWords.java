package swing;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.*;
import java.awt.*;

import static swing.CtrlJPanel.uo;

class ColorKeyWords implements DocumentListener{
	private KeyWord keywords;//定义KeyWord对象，用于判断是否是关键词
	private Style keywordStyle;
	private Style spcwordStyle;
	private Style operwordStyle;
	private Style normalStyle;
	public ColorKeyWords(JTextPane text) {
		keywordStyle = ((StyledDocument) text.getDocument()).addStyle("keyStyle", null);
		spcwordStyle = ((StyledDocument) text.getDocument()).addStyle("spcStyle", null);
		operwordStyle = ((StyledDocument) text.getDocument()).addStyle("operStyle", null);
		normalStyle = ((StyledDocument) text.getDocument()).addStyle("normalStyle", null);
		StyleConstants.setForeground(keywordStyle, Color.RED);//设置关键字显示为红色
		StyleConstants.setForeground(spcwordStyle, Color.BLUE);//设置SPEC显示为浅色
		StyleConstants.setForeground(operwordStyle, Color.MAGENTA);//设置关键字显示为蓝色
		StyleConstants.setForeground(normalStyle, Color.BLACK);
		keywords=new KeyWord();
	}

	public void color(StyledDocument doc, int pos, int len) throws BadLocationException 
	{
		int start = wordStart(doc, pos);
		int end = wordEnd(doc,pos+len);
		char ch;
		while (start < end) {
			ch = getChar(doc, start);
			if (Character.isLetter(ch) || ch == '_') 
			{
				start = colorWord(doc, start);
			} else {
				SwingUtilities.invokeLater(new Coloring(doc, start, 1, normalStyle));
				++start;
			}
		}
	}
	public int colorWord(StyledDocument doc, int pos) throws BadLocationException 
	{
		int wordEnd = wordEnd(doc, pos);
		String word = doc.getText(pos, wordEnd - pos);
		if (keywords.isSMV(word))
		{
			SwingUtilities.invokeLater(new Coloring(doc, pos, wordEnd - pos, keywordStyle));
		} else if (keywords.isSPC(word)) {
			SwingUtilities.invokeLater(new Coloring(doc, pos, wordEnd - pos,spcwordStyle ));
		} else if (keywords.isOPER(word)) {
			SwingUtilities.invokeLater(new Coloring(doc, pos, wordEnd - pos, operwordStyle));
		} else {
			SwingUtilities.invokeLater(new Coloring(doc, pos, wordEnd - pos, normalStyle));
		}
		return wordEnd;
	}

	public char getChar(Document doc, int pos) throws BadLocationException 
	{
		return doc.getText(pos, 1).charAt(0);
	}

	public int wordStart(Document doc, int pos) throws BadLocationException 
	{
		for (; pos > 0 && isWordChar(doc, pos - 1); --pos);
		return pos;
	}

	public int wordEnd(Document doc, int pos) throws BadLocationException 
	{
		for (; isWordChar(doc, pos); ++pos);
		return pos;
	}


	public boolean isWordChar(Document doc, int pos) throws BadLocationException 
	{
		char ch = getChar(doc, pos);
		if (Character.isLetter(ch) || Character.isDigit(ch) || ch == '_') 
		{ 
			return true; 
		}
		return false;
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
	}

	@Override
	public void insertUpdate(DocumentEvent e)
	{
		try
		{
			color((StyledDocument) e.getDocument(), e.getOffset(), e.getLength());
		}
		catch (BadLocationException e1)
		{
			e1.printStackTrace();
		}
	}

	@Override
	public void removeUpdate(DocumentEvent e)
	{
		try {
			color((StyledDocument) e.getDocument(), e.getOffset(), 0);
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
	}

	private class Coloring implements Runnable 
	{
		private StyledDocument doc;
		private Style style;
		private int pos;
		private int len;

		public Coloring(StyledDocument doc, int pos, int len, Style style) 
		{
			this.doc = doc;
			this.pos = pos;
			this.len = len;
			this.style = style;
		}

		public void run()
		{
			try {
				doc.setCharacterAttributes(pos, len, style, true);
			} catch (Exception e) {}
		}
	}
}