package swing;

import org.jb2011.lnf.beautyeye.BeautyEyeLNFHelper;

import javax.swing.*;

public class Main {

	public static void main(String[] args)
	{
		try {
			// 设置窗口边框样式
			BeautyEyeLNFHelper.frameBorderStyle = BeautyEyeLNFHelper.FrameBorderStyle.translucencyAppleLike;
			org.jb2011.lnf.beautyeye.BeautyEyeLNFHelper.launchBeautyEyeLNF();
			// 隐藏设置按钮
			UIManager.put("RootPane.setupButtonVisible", false);
		} catch (Exception e) {
		}
		new IndexJFrame();
	}
}
