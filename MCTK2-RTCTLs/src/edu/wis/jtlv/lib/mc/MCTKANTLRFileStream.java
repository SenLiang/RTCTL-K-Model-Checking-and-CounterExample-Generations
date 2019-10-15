package edu.wis.jtlv.lib.mc;

import edu.wis.jtlv.env.Env;
import edu.wis.jtlv.env.spec.Spec;
import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CharStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MCTKANTLRFileStream extends ANTLRFileStream{
    public String dataStr;
    public MCTKANTLRFileStream(String s) throws IOException {
        super(s);
    }
    public String[] seperateSpecsFromModules() {
        dataStr = new String(data);
        if(dataStr.equals("")) return null;
        int charNum=0;

        dataStr=dataStr.replaceAll("[-]{2,}.*[\n\r]{1}", "");
        String regEx = "^(CTLSPEC|LTLSPEC|SPEC|RTCTL\\*SPEC)(.|\n|\r)*?(CTLSPEC|LTLSPEC|SPEC|RTCTL\\*SPEC|;)$";
        // 编译正则表达式,匹配以;结尾的规范
        Pattern pattern = Pattern.compile(regEx,Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(dataStr);
        ArrayList<String> tempList = new ArrayList<String>();
        while(matcher.find()){
            tempList.add(matcher.group());
            dataStr = dataStr.replace(matcher.group(), "");
        }
        String []specStr = new String[tempList.size()];
        tempList.toArray(specStr);
        charNum+=specStr.length;
        if(charNum>0) {
            this.data = dataStr.toCharArray();
            this.n = this.data.length;
            this.reset();
        }
        return specStr;
    }
}
