package com.example.mealRecommend;

import com.example.mealRecommend.util.TrieNode;

import java.util.ArrayList;
import java.util.List;

public class trie {
    private TrieNode root;

    public trie() {
        root = new TrieNode();
    }

    public void insert(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node = node.getChildren().computeIfAbsent(c, k -> new TrieNode());
        }
        node.setEndOfWord(true);
    }

    public boolean search(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node = node.getChildren().get(c);
            if (node == null) {
                return false;
            }
        }
        return node.isEndOfWord();
    }

    public List<String> getCompletions(String prefix) {
        List<String> completions = new ArrayList<>();
        TrieNode node = root;
        for (char c : prefix.toCharArray()) {
            node = node.getChildren().get(c);
            if (node == null) {
                return completions; // No completions found
            }
        }
        findAllWords(node, prefix, completions);
        return completions;
    }

    private void findAllWords(TrieNode node, String prefix, List<String> results) {
        if (node.isEndOfWord()) {
            results.add(prefix);
        }
        for (char c : node.getChildren().keySet()) {
            findAllWords(node.getChildren().get(c), prefix + c, results);
        }
    }
}