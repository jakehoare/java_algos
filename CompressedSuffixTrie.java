package assignment4;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import static java.lang.Math.max;
import java.util.Scanner;

public class CompressedSuffixTrie {

    /** Define data structures and inner node class */
    static final String LETTERS = "ACGT";  // the permitted DNA characters
    String text; // the full text of the trie
    CSTNode root;  // the root node
  
    public static class CSTNode {
        // Node represents characters from index 'start' to index 'end-1' of text
        int start, end;
        CSTNode parent;
        CSTNode[] children;
        CSTNode suffixLink;

        // Constructor for CSTNode
        CSTNode(int start, int end, CSTNode parent) {
            this.start = start;
            this.end = end;
            this.parent = parent;
            // Children with indices 0 to 3 correspond to characters A, C, G, T respectively
            // and are intially all null.
            children = new CSTNode[LETTERS.length()];
        }
        
        boolean isInternal() {
            for (int i = 0; i < LETTERS.length(); ++i)
                if (children[i] != null)
                    return true;                
            return false;
        }
        
        public String toString() {
            return "Start : " + start + " End : " + end;
        }
    }
    
    /** Constructor */
    /* TIME COMPLEXITY ANALYSIS
    Each suffix is input in turn beginning with the longest (the whole text).
    To input a suffix we move down the trie starting at the root looking for the 
    first character that does not match.  If that point is the end of a node we create
    a new child with the remainder of the suffix.  If that point is in the middle
    of the text represented by a node we split the node.
    Since processing each character of each suffix is done in constant time and 
    we navigate through each character of each suffix the time complexity 
    is O(n^2) where n is the length of the text.
    */
    public CompressedSuffixTrie(String f) {  
        // Create the root and read the input file
        root = new CSTNode(0, 0, null);
        text = fileToString(f);
        if (text == null || text.equals(""))
            return;
        int inputLength = text.length();
        
        // Array dna[] maps each character of the file to an integer from 0 to 3.
        int[] dna = new int[inputLength];        
        for (int i = 0; i < inputLength; ++i)
            dna[i] = LETTERS.indexOf(text.charAt(i));
        
        CSTNode node;
        int j;  // index of character being input
        int nodeChar;  // index of character already in trie
        
        // Input each suffix starting with the longest first
        for (int i = 0; i < inputLength; ++i) {
            
            // Start at the root and the beginning of the suffix
            node = root;
            nodeChar = node.start;
            j = i;
            
            while (j < inputLength) {
                // Test if we are at the end of the text represented by this node
                if (nodeChar >= node.end) { 
                    if (node.children[dna[j]] == null) {
                        // No child starts with this character so create a new child
                        // with the rest of the suffix.
                        node.children[dna[j]] = new CSTNode(j, inputLength, node);
                        break;
                    } else {
                        // A child starts with this character so update node to that child
                        node = node.children[dna[j]];
                        nodeChar = node.start;
                    }
                    
                } else {  // Not at the end of the text represented by this node
                    // If character of j matches with that already in trie then increment both
                    if (dna[j] == dna[nodeChar]) {
                        ++j;
                        ++nodeChar;
                    // If character j does not match with node we split the node 
                    } else {
                        // New node n1 replaces the part of node that is already matched
                        CSTNode n1 = new CSTNode(node.start, nodeChar, node.parent);
                        node.parent.children[dna[node.start]] = n1;
                        // New node n2 is the rest of the substring being input
                        CSTNode n2 = new CSTNode(j, inputLength, n1);
                        n1.children[dna[j]] = n2;
                        // Update the old node to be a child of n1
                        node.parent = n1;
                        node.start = nodeChar;
                        n1.children[dna[nodeChar]] = node;
                        break;
                    }
                }                
            }
        }                    
    }
    
    
    /** Secondary constructor */
    /* 
    Based upon Ukkonen's method for creating a suffix trie.
    Characters in the original text are incorprated one at a time.  If the 
    current node has no child starting with the character to be input then a new node is 
    created representing the rest of the text.  Or else if the charatcter to be input
    is at start + remainder of the child then do nothing. If the path ends without the
    character then split the edge by creating a new child.
    The base algorithm is O(n^3) because each existing path must be extended for each new 
    charcater to be added and traversing a path could take O(n).
    However through the use of suffix links to avoid traversing the existing trie
    the time complexity is O(n).
    */
    public CompressedSuffixTrie(String f, int a) {  // a is a dummy to distinguish from other constructor
        // Create the root and read the input file
        root = new CSTNode(0, 0, null);
        text = fileToString(f);
        if (text == null || text.equals(""))
            return;
        int inputLength = text.length();
        
        // Array dna[] maps each character of the file to an integer from 0 to 3.
        int[] dna = new int[inputLength];        
        for (int i = 0; i < inputLength; ++i)
            dna[i] = LETTERS.indexOf(text.charAt(i));

        // Remainder is the number of charcaters apart from the current one 
        // that we still have to input.
        int remainder = -1;
        CSTNode node = root;
        
        // Loop over each character of the input string
        for (int i = 0; i < inputLength; ++i) {
            ++remainder;
            CSTNode previous = null;  // previous node to be split
            while (remainder >= 0) {
                // Get the child corresponding to the first character to be input
                CSTNode child = node.children[dna[i - remainder]];
                
                // If a child already stores this character and the number of characters to be 
                // input is greater than stored at this child, then move down the trie
                // finding the next child and decremeting remainder.
                while (child != null && remainder >= child.end - child.start) {
                    remainder -= child.end - child.start;
                    node = child;
                    child = child.children[dna[i - remainder]];
                }
                
                // If this character is not in the trie then make a new node storing
                // the rest of the input with a suffixLink from the last node to be split (if any).
                if (child == null) {
                    node.children[dna[i]] = new CSTNode(i, inputLength, node);
                    if (previous != null)
                        previous.suffixLink = node;
                    previous = null;
                } else {
                    // If this child already stores the text upto i then update the
                    // suffixLink (if any).
                    if (dna[i] == dna[child.start + remainder]) {
                        if (previous != null)
                            previous.suffixLink = node;
                        break;
                    } else {
                        // This child stores some but not all of the text to be entered.
                        // Create a new node which becomes tha parent of this current child.
                        CSTNode newNode = new CSTNode(child.start, child.start + remainder, node);
                        newNode.children[dna[i]] = new CSTNode(i, inputLength, newNode);
                        newNode.children[dna[child.start + remainder]] = child;
                        // Update the old child
                        child.start += remainder;
                        child.parent = newNode;
                        node.children[dna[i - remainder]] = newNode;
                        if (previous != null)
                            previous.suffixLink = newNode;
                        previous = newNode; // a node has been created by a split
                    }
                }
                
                if (node == root)
                    --remainder;
                else
                    node = node.suffixLink;
                
            }
        }       
    }
        
    
    /** Method for finding the first occurrence of a pattern s in the DNA sequence */
    /* TIME COMPLEXITY ANALYSIS
    Starting with the root of the trie we examine each child node for a match with the 
    first charcater of the input string s.  Since the nodes each represent a different 
    starting character, at most only one node can match.
    In the worst case for time complexity we match only one character at each node, and then 
    examine the children of that node for another match of the next character.
    This procedure is repeated for each character of s and thus takes 4 * m steps where
    4 is the number of charcaters in the DNA alphabet and m is the length of s.
    Each step takes O(1) primitive opeartions so the method runs in O(4 * m) = O(m) time.
    */
    public int findString(String s) {
        int i;      // pointer to an index in the text to be searched
        int j = 0;  // pointer to an index in the input substring
        int length = s.length();    // length of the substring that we are searching for
        int nodeLength;             // length of the text at the current node
        boolean noChildProcessed;   // flag whether a child has been processed
        CSTNode v = root;
        CSTNode w;
        
        do {
            noChildProcessed = true;
            // Loop over each child w of node v
            for (int child = 0; child < LETTERS.length(); ++child) {
                w = v.children[child];
                if (w == null)
                    continue;
                i = w.start;
                
                if (LETTERS.indexOf(s.charAt(j)) == child) {
                    // The first character at this node is the current character in the substring
                    nodeLength = w.end - i;
                    if (length <= nodeLength) {
                        // Substring being searched for must be entirely at this node or not in text
                        if (s.substring(j, j + length).equals(text.substring(i, i + length)))
                            return i - j;
                        else
                            return -1;
                    } else {
                        if (s.substring(j, j + nodeLength).equals(text.substring(i, i + nodeLength))) { 
                            // Substring being searched for matches this node but is longer
                            length = length - nodeLength;
                            j = j + nodeLength;
                            v = w;  // continue along this branch of the tree
                            noChildProcessed = false;
                            break;
                        }                        
                    }
                }
            }
        // No match if no child was processed (the first character was not matched)
        // or we have reached an external node at the end of the tree
        } while (!noChildProcessed && v.isInternal());        
        return -1;
    }     

    
    /** Method for computing the degree of similarity of two DNA sequences stored in the text files f1 and f2 */
    /* TIME COMPLEXITY ANALYSIS
    Each entry of the 2-dimensional array takes O(1) operations to compute.  Since the array
    is m * n size where m and n are the input string lengths it takes O(mn) to calculate the entire array.
    To derive the longest subsequence we start at the bottom right of the array and 
    move to the top left. In the worst case this traverses the array in both dimensions
    (i.e. horizontally and vertically) with each step taking O(1) primitive operations and 
    so takes O(m + n).
    The dominant procedure is calculation of the array so this method runs in O(mn).
    */
    public static float similarityAnalyser(String f1, String f2, String f3) {
        
        // Convert files to strings
        String x = fileToString(f1);
        String y = fileToString(f2);
        if (x == null || y == null || x.equals("") || y.equals("")) {
            System.out.println("At least one of the input files is empty.");
            return 0;
        }
        int xLength = x.length();
        int yLength = y.length();
        
        // Initialise array to store the length of the longest common subsequences.
        // Note that the value stored at index 0 of the array is always zero.
        // Index (i+1, j+1) of the array correspsonds to the length of the longest 
        // common subsequence upto and including index i of f1 and index j of f2.
        int[][] longestSubsequences = new int[xLength + 1][yLength + 1];

        // Outer loop moving through f1
        for (int i = 0; i < xLength; ++i) {
            // Inner loop moving through f2
            for (int j = 0; j < yLength; ++j) {
                if (x.charAt(i) == y.charAt(j))
                    // If characters match add 1 to the longest subsequence where f1 and f2 are both 1 character shorter
                    longestSubsequences[i + 1][j + 1] = longestSubsequences[i][j] + 1;
                else
                    // If characters do not match take the larger of the subsequences where either f1 or f2 is 1 character shorter
                    longestSubsequences[i + 1][j + 1] = max(longestSubsequences[i + 1][j], longestSubsequences[i][j + 1]);                
            }            
        }
        
        // Find the longest subsequence.
        String longest = "";
        int i = xLength - 1; // Start form the end of both input strings
        int j = yLength - 1;
        while (i >= 0 && j >= 0) {
            if (x.charAt(i) == y.charAt(j)) {
                // If charcaters match add that character to the front of result and decrement both i and j
                longest = x.charAt(i) + longest;
                --i;
                --j;
            } else if (longestSubsequences[i][j + 1] > longestSubsequences[i + 1][j])
                // If charcaters do not match and decrementing i gives a longer subsequence than
                // decrementing j, then decrement i.
                --i;
            else
                // If charcaters do not match and decrementing j gives a longer subsequence
                // or decrementing i or j gives the same subsequence length, then decrement j.
                --j;
        }
        
        // Save the longest common subsequence to file f3
        FileWriter writer = null;
        try {
            writer = new FileWriter(f3 , false);
        } catch (IOException ex) {
            System.out.println(ex);
        }
        PrintWriter printToFile = new PrintWriter(writer);
        printToFile.println(longest);
        printToFile.close();
        // Return the degree of similarity
        return (float) longestSubsequences[xLength][yLength] / max(xLength, yLength);
    }
    
    public static String fileToString (String f) {
        Scanner s;
        String result = "";
        
        // Open input file or give an error if it does not exist.
        try {
            s = new Scanner(new File(f));
        } catch (FileNotFoundException ex) {
            System.out.println(f + " does not exist.");
            return null;
        }

        // Read the input file one token (word) at a time
        while (s.hasNext())
            result += s.next();    
        s.close();
        return result; 
    }
}
