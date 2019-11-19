package main.TerminusEst;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class TerminusEstInputHandler {

    // Tree construction.
    public Tree tree;
    public Tree current_node;
    private Tree next;

    // Tree info.
    public int leaves = 0;
    public int trees = 0;

    public static void main(String[] args) {
        TerminusEstInputHandler ih = new TerminusEstInputHandler();
        System.out.println("Interpreting file.");
        ih.InterpretFile(args[0]);
        System.out.println("Done interpreting file.");
    }

    public void InterpretFile(String file) {
        String s1 = "", s2 = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            try {
                s1 = br.readLine();
                s2 = br.readLine();

            } catch (IOException e) {
                System.out.println("// Reading file failed.");
                e.printStackTrace();
            }

        } catch (FileNotFoundException e) {
            System.out.println("// File reading failed.");
            e.printStackTrace();
        }

        System.out.println("Trees: \n" + s1 + "\n" + s2);
        if (s1 == "" || s1 == "") {
            System.out.println("// Trees given are empty...");
            System.exit(0);
        }

        SetTree(s1);
        SetTree(s2);

        if (TerminusEstV4.VERBOSE) {
        System.out.println("\nVerifying trees from input handler: ");
        TerminusEstV4.t1.dump();
        System.out.println();
        TerminusEstV4.t2.dump();
        System.out.println("\n");
        }

    }

    /**
     * Adds one of the Tree objects in TerminusEstV4.
     * @param s Tree in Newick format.
     */
    public void SetTree(String s) {
        tree = new Tree();
        current_node = tree;
        leaves = 0;
        ConstructTree(s);
        /*
        ( s = label() {}  )?
        ( ":" len = branch_length()   )?
        ";"
        */
        if( trees == 0 )
        {
            TerminusEstV4.t1 = tree;
        }
        else
        if( trees == 1 )
        {
            TerminusEstV4.t2 = tree;
        }
        else
        {
            System.out.println("ERROR! We can only deal with two trees.");
            System.exit(0);
        }

        trees++;
    }

    public void ConstructTree(String s) {
        int i = 1;
        char current;
        while (i < s.length()-2) {
            current = s.charAt(i);
            // End of Tree
            if (current == ';') {
                return;
            }
            // New Subtree
            else if (current == '(') {
                next = new Tree();
                next.setParent(current_node);
                current_node.addChild(next);
                current_node = next;
                ++i;
            }
            // New Child
            else if (current == ',') {
                ++i;
            }
            // End of Subtree
            else if (current == ')') {
                current_node = current_node.parent;
                ++i;
            }
            // Child
            else {
                // Get the taxa name.
                String label = "";

                // Get entire.
                while (current != '('
                && current != ','
                && current != ')') {
                    label += current;
                    ++i;
                    current = s.charAt(i);
                }

                ++leaves;
                // Build leaf node.
                Tree leafNode = new Tree();
                leafNode.setParent(current_node);
                leafNode.parent.addChild(leafNode);

                int x = TerminusEstV4.getLeafNumber(label);
                leafNode.setName(label);
                leafNode.setNumber(x);

                // Create leaf node.
            }
        } // End while-loop.
    }

    public String GetLabel(String s, int i) {
        String leafName = "";
        // Get entire.
        while (s.charAt(i) != '('
                && s.charAt(i) != ','
                && s.charAt(i) != ')') {
            leafName += s.charAt(i);
            ++i;
        }

        return leafName;
    }
}
