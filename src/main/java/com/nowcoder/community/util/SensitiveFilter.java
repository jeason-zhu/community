package com.nowcoder.community.util;


import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {
    private static  final Logger logg = LoggerFactory.getLogger(SensitiveFilter.class);
//根节点
    private TrieNode rootNode=new TrieNode();
//敏感词替换符
    private static  final String REPLACEMENT="***";

//初始化前缀树
    @PostConstruct
    public void init()
    {
        //获取类加载器 -> 字节流 -> 缓冲流
        BufferedReader bufferedReader = null;
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
            bufferedReader = new BufferedReader(new InputStreamReader(is));
            //读取关键词,一行一个词
            String keyword;
            while((keyword = bufferedReader.readLine()) != null )
            {
                addKeyword(keyword);
            }
        } catch (Exception e) {
            logg.error("加载敏感词文件失败，",e.getMessage());
        }
    }
//过滤敏感词,返回过滤之后的词
    public  String   filter(String text)
    {
        if(StringUtils.isBlank(text))
        {
            return  null;
        }
//        指针1 指向树根节点
        TrieNode tempNode = rootNode;
//        指针2 指向字符串子串的头
        int start = 0;
//        指针3 指向字符串子串的尾
        int position = 0;
//      结果
        StringBuilder sb = new StringBuilder();
        //start不一定会往后走，而position一定会
        while(start<text.length())
        {
            char c = text.charAt(position);
            //跳过字符
            if(isSymbol(c))
            {
                if(tempNode==rootNode)
                {
                    sb.append(c);
                    start++;
                }
                position++;
                continue;
            }
            //检查下级节点
            tempNode=tempNode.getSubNode(c);
            if(tempNode==null)
            {
                //start开头的字符串不是敏感词
                sb.append(text.charAt(start));
                //指向下一个字符
                position=++start;
                //重新指向根节点
                tempNode=rootNode;
            }
            //是敏感词
            else if(tempNode.isKeywordEnd())
            {
                //替代敏感词
                sb.append(REPLACEMENT);
                start = ++position;
                //重新指向根节点
                tempNode=rootNode;
            }
            //检查下个字符(疑似敏感词)
            else
            {
                position++;
            }
        }
        //将最后一批字符计入结果
        sb.append(text.substring(start));
        return sb.toString();
    }
//      判断是否为符号
    public boolean isSymbol(Character c)
    {
        // 0x2E80~0x9FFF 是东亚文字范围
        //如果非数字和字母 并且【不在东亚文字范围】！！   则是字符
        return !CharUtils.isAsciiAlphanumeric(c) && (c<0x2E80 || c>0x9FFF);
    }
//前缀树
    private class TrieNode
    {
        //关键词结束标志符
        private boolean isKeywordEnd;
        //一个节点可以有多个子节点  子节点(key为下级字符，value是下级节点)
        private Map<Character,TrieNode> subNode = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }
        //获取子节点
        public TrieNode getSubNode(Character ch) {
            return subNode.get(ch);
        }
        //添加子节点
        public void addSubNode(Character ch,TrieNode trieNode) {
            subNode.put(ch,trieNode);
        }
    }
//将敏感词添加到前缀树
    private void addKeyword(String keyword)
    {
        //从根节点开始检查
        TrieNode tempNode = rootNode;
        if(keyword==null)
        {
            return;
        }
        for(int i=0;i<keyword.length();i++)
        {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            //子节点不存在
            if(subNode==null)
            {
                subNode = new TrieNode();
                tempNode.addSubNode(c,subNode);
            }
            //指向下个节点
            tempNode = subNode;
            //是否为结束标志
            if(i==keyword.length()-1)
            {
                tempNode.setKeywordEnd(true);
            }
        }

    }
}
