package com.nowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    // 定义前缀树中的节点
    private class Node{
        public boolean isWordEnd;  // 敏感词最后一个字符结束标志
        public Map<Character, Node> next; // 指向下个节点的指针

        public Node() {
            this.isWordEnd = false;
            this.next = new HashMap<>();
        }
    }

    // 定义前缀树结构
    private class Trie {
        private Node root;  // 每颗树的根节点
        public Trie() {
            root = new Node();
        }

        // 向前缀树中添加词汇
        public void add(String keyWord) {
            if(keyWord == null) { // 边界条件
                return;
            }
            char[] str = keyWord.toCharArray();
            char c;
            Node cur = root; // 每次从root根节点开始构建
            for(int i = 0; i < str.length; i ++) {
                c = str[i];
                if(cur.next.get(c) == null ) { // 构建非重复的节点
                    cur.next.put(c, new Node());
                }
                // 下层，构建下个节点
                cur = cur.next.get(c);
            }

            // 构建完成后，在最后一个节点位置标记为单词结束
            if(!cur.isWordEnd) {
                cur.isWordEnd = true;
            }
        }
    }

    // 定义好替换符
    private static final String REPLACEMENT = "***";
    // 定义好树的全局变量，后面的初始化和过滤算法，都需要用到这个trie树结构
    private Trie trie = new Trie();

    /**
     * 根据敏感词文件初始化前缀树
     * 只初始化一次，即用Spring注解定义初始化位置
     * 读取敏感词文件内容
     */
    @PostConstruct
    public void init() {
        try (
                // 从当前类的类加载器定位到根目录-获取资源作为输入字节流
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("Sensitive-words.txt");
                // 将字节流改为字符流-缓冲流便于读取
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                // 从缓冲流中读取文件，每次读一行，依次添加进前缀树结构
        ){
            String keyword;
            while((keyword = reader.readLine()) != null) {
                // 将敏感词添加进trie树中
                trie.add(keyword);
            }
        } catch (IOException e) {
            logger.error("加载敏感词文件失败: " + e.getMessage());
        }
    }

    /**
     * 过滤敏感词算法
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text) {
        // 定义过滤后的结果
        StringBuilder res = new StringBuilder();
        // 边界条件，判空
        if(StringUtils.isBlank(text)) return null;
        // 定义操作中用到的三个指针
        Node cur = trie.root;   // 指向敏感词前缀树的指针
        int begin = 0;      // 指向要处理文本中敏感词的开始字符后，就停下等待
        int end = 0;   // 指向敏感词的最后一个字符，每处理完一个词，指针回位到begin位置
        char c;        // 指向处理文本中的每个字符
        char[] str = text.toCharArray();
        // 循环遍历文本字符与敏感词前缀树比对
        while(end < text.length()) { // end指针最先遍历完，敏感词也处理完了
            c = str[end]; // end指针每次都走一步

            // 注意要跳过敏感词中间的特殊符号
            if(isSymbol(c)) {
                if(cur == trie.root) {      // 如果特殊符合不是在敏感词中间，而是两侧位置
                    res.append(str[begin]); // 符号可以输出，直接跳过判断下个字符，不动前缀树
                    begin ++;
                }
                end ++;                     // end表示特殊符号直接跳过不处理，不动前缀树
                continue;  // 当前字符已经处理好了，下面逻辑不执行，继续下一轮的循环
            }

            if(cur.next.get(c) == null) { // 无下级节点，即不是敏感词
                res.append(str[begin]);   // 将一定不是敏感词开始的字符计入结果中
                end = ++begin;            // begin指针指向下个字符，end归位，重新判断新单词
                cur = trie.root;          // 敏感树指针归位，用以判断下个词开始的位置
            } else if(cur.next.get(c).isWordEnd) {  // 有字节点，且为敏感词结束标志位，说明找到了
                res.append(REPLACEMENT);
                begin = ++end;  // 将敏感词跳过，进行后面第一个字符开始判断
                cur = trie.root;
            } else {
                end ++;     // 有子节点，但不是最后字符，end继续往下进行查找
                cur = cur.next.get(c); // 因为要继续找，cur要下层，判断下个敏感字符
            }
        }

        // 最后将敏感词是处理完了，但剩下字符还没放入结果集中，最后将一定不是敏感词的字符处理好
        res.append(text.substring(begin));
        return res.toString();
    }

    // 判断是否为正常字符，即是否为符号
    private boolean isSymbol(char c) {
        // 0x2E80~0x9FFF 是东亚文字范围，要排除在外
        return !CharUtils.isAsciiAlpha(c) && (c < 0x2e80 || c > 0x9fff);
    }
}
