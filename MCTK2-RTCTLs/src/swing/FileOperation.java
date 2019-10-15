package swing;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import java.io.*;

import static swing.EditorJPanel.textModel;
import static swing.IndexJFrame.editorPanel;


public class FileOperation {
    JFileChooser filechoose = new JFileChooser();
    IndexJFrame indexJFrame;
    String src="";
    String fileName;

    public FileOperation(IndexJFrame indexJFrame) {
        this.indexJFrame = indexJFrame;
        filechoose.setAcceptAllFileFilterUsed(false);
        filechoose.addChoosableFileFilter(new MyFileFilter("SMV Code(.smv)", ".smv"));//添加文件过滤器
        FileSystemView fsv = FileSystemView.getFileSystemView();
        filechoose.setCurrentDirectory(fsv.getHomeDirectory());//设置默认路径为桌面路径
    }

    public boolean open() {
        if (JFileChooser.APPROVE_OPTION == filechoose.showOpenDialog(indexJFrame)) {
            File newFile;
            File file = filechoose.getSelectedFile();
            if (file.getName() == null) return false;
            BufferedReader br;
            newFile = file;
            src = newFile.toString();
            fileName = newFile.getName();
            setStyle(newFile);
            try {
                String s;
                StringBuffer sbf = new StringBuffer();
                br = new BufferedReader(new FileReader(newFile));
                while ((s = br.readLine()) != null) {
                    sbf.append(s + "\r\n");
                }
                br.close();
                if (sbf.length() > 2)
                    sbf = new StringBuffer(sbf.substring(0, sbf.length() - 2));
                String content = sbf.toString().replaceAll("\\t", "   ");
                textModel.setText(content);
                return true;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return false;
    }

    public boolean save() {
        if (!src.equals(""))//文件已存在
        {
            BufferedWriter br;
            File newFile = new File(src);
            try {
                String content= textModel.getText();
                br = new BufferedWriter(new FileWriter(newFile));
                br.write(content);
                br.flush();
                br.close();
                //System.out.println("Write Successfully!");
                return true;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } else//文件未存在（打开）
        {
            if (JFileChooser.APPROVE_OPTION == filechoose.showSaveDialog(indexJFrame)) {
                File newFile;
                File file = filechoose.getSelectedFile();
                if (file.getName() == null) return false;
                BufferedWriter br;
                MyFileFilter filter = (MyFileFilter) filechoose.getFileFilter();
                String ends = filter.getEnds();
                if (file.toString().indexOf(ends) != -1) {
                    newFile = file;
                } else {
                    newFile = new File(file.getAbsolutePath() + ends);
                }
                src = newFile.toString();
                fileName = newFile.getName();
                if (newFile.exists()){//已存在同名文件，删除
                    newFile.delete();
                }
                try {
                    String content= textModel.getText();
                    br = new BufferedWriter(new FileWriter(newFile));
                    br.write(content);
                    br.flush();
                    br.close();
                    return true;
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return false;
    }

    public boolean SaveAs() {
        if (JFileChooser.APPROVE_OPTION == filechoose.showSaveDialog(indexJFrame)) {
            File newFile;
            File file = filechoose.getSelectedFile();
            if (file.getName() == null) return false;
            BufferedWriter br;
            MyFileFilter filter = (MyFileFilter) filechoose.getFileFilter();
            String ends = filter.getEnds();
            if (file.toString().indexOf(ends) != -1) {
                newFile = file;
            } else {
                newFile = new File(file.getAbsolutePath() + ends);
            }
            src = newFile.toString();
            fileName = newFile.getName();
            if (newFile.exists())
            {
                Object[] options = {"Replace", "Rename"};
                int response = JOptionPane.showOptionDialog(indexJFrame, "A file with the same name already exists!",
                        "Warning", JOptionPane.YES_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                if (response == 0) {//Replace
                    newFile.delete();
                } else if (response == 1) {//Rename
                    SaveAs();
                }
            }
            try {
                String content= textModel.getText();
                br = new BufferedWriter(new FileWriter(newFile));
                br.write(content);
                br.flush();
                br.close();
                return true;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return false;
    }


    public boolean creat() {
        if (JFileChooser.APPROVE_OPTION == filechoose.showSaveDialog(indexJFrame)) {
            File newFile;
            File file = filechoose.getSelectedFile();
            if (file.getName() == null) return false;
            BufferedWriter br;
            MyFileFilter filter = (MyFileFilter) filechoose.getFileFilter();
            String ends = filter.getEnds();
            if (file.toString().indexOf(ends) != -1) {
                newFile = file;
            } else {
                newFile = new File(file.getAbsolutePath() + ends);
            }
            src = newFile.toString();
            fileName = newFile.getName();
            if (newFile.exists())
            {
                Object[] options = {"Replace", "Rename"};
                int response = JOptionPane.showOptionDialog(indexJFrame, "A file with the same name already exists!",
                        "Warning", JOptionPane.YES_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                if (response == 0) {//Replace
                    newFile.delete();
                    // SaveAs();
                } else if (response == 1) {//Rename
                    creat();
                }
            }
            setStyle(newFile);
            textModel.setText("");
            try {
                br = new BufferedWriter(new FileWriter(newFile));
                br.write("");
                br.flush(); //刷新缓冲区的数据到文件
                br.close();
                return true;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return false;
    }

    public void setStyle(File file) {
        String name = file.getName();
        indexJFrame.setTitle("MCTK2-" + file.getAbsolutePath());
    }

    public String getPath() {
        String name = getFileName();
        int t = src.lastIndexOf(name, src.length() - 1);
        String path = src.substring(0, t);
        return path;
    }

    public String getFileName() {
        if (fileName == null) return "";
        int point = fileName.indexOf(".");
        String name = fileName.substring(0, point);
        return name;
    }

    class MyFileFilter extends FileFilter {

        String ends; // 文件后缀
        String description; // 文件描述

        public MyFileFilter(String description, String ends) {
            this.ends = ends; // 设置文件后缀
            this.description = description; // 设置文件描述文字
        }

        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) return true;
            String fileName = f.getName();
            if (fileName.toUpperCase().endsWith(this.ends.toUpperCase())) return true;//判断文件名后缀
            return false;
        }

        @Override
        public String getDescription() {
            return this.description;
        }

        public String getEnds() {
            return this.ends;
        }

    }
}
