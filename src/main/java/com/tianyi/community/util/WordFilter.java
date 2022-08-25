package com.tianyi.community.util;

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
public class WordFilter {

    private static Logger logger = LoggerFactory.getLogger(WordFilter.class);

    // string to replace the offensive words
    private static final String REPLACEMENT = "***";

    // root of trie
    private TrieNode rootNode = new TrieNode();

    // constuct the offensive word trie when start the program
    @PostConstruct
    public void init() {
        // path under target class directory
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("offensive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                ) {
                    String keyword;
                    while ((keyword = reader.readLine()) != null) {
                        this.addWord(keyword);
                    }
        } catch (IOException e) {
            logger.error("Error when loading offensive word txt file" + e.getMessage());
        }


    }

    // add word to the trie
    private void addWord(String word) {
        TrieNode tempNode = rootNode;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            if (subNode == null) {
                // init subNode
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }
            // if has c, then go to the next
            tempNode = subNode;

            // set end
            if (i == word.length() - 1) {
                tempNode.setEnd(true);
            }

        }
    }

    /**
     * filter offensive words
     * @param text text to be filtered
     * @return text after filter
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }

        // pointer on the trie
        TrieNode tempNode = rootNode;
        // pointers on text, indicate begin/end of the word
        int begin = 0, end = 0;
        // result
        StringBuilder sb = new StringBuilder();

        while (end < text.length()) {
            char c = text.charAt(end);
            if (isSymbol(c)) {
                // when start searching the word start at the new char
                if (tempNode == rootNode) {
                    sb.append(c);
                    begin++;
                }
                // whether the symbol is in the middle or at the beginning of the word, end will go one step further
                end++;
                continue;
            }

            tempNode = tempNode.getSubNode(c);

            if (tempNode == null) {
                // not offensive word
                sb.append(text.charAt(begin));
                end = ++begin;
                tempNode = rootNode;
            } else if (tempNode.isEnd()) {
                sb.append(REPLACEMENT);
                // go to the next char tgt
                begin = ++end;
            } else {
                end++;
            }
        }

        sb.append(text.substring(begin));
        return sb.toString();

    }

    private boolean isSymbol(Character c) {
        return !CharUtils.isAsciiAlphanumeric(c);
    }


    private class TrieNode {
        private boolean isEnd = false;
        private Map<Character, TrieNode> map = new HashMap<>();

        public boolean isEnd() {
            return isEnd;
        }

        public void setEnd(boolean end) {
            isEnd = end;
        }

        public void addSubNode(Character c, TrieNode node) {
            map.put(c, node);
        }

        public TrieNode getSubNode(Character c) {
            return map.get(c);
        }


    }

}
