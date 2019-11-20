package main.TerminusEst;

import main.mcts.base.Action;
import main.mcts.base.State;
import main.utility.IDeepCopy;

import java.util.Hashtable;
import java.util.Vector;

public class TerminusEstState implements State {
    public Tree t1;
    public Tree t2;
    public Tree original1;
    public Tree original2;
    public int depth; // Number of actions taken.

    public TerminusEstState(Tree t1, Tree t2, Tree original1, Tree original2, int depth) {
        this.t1 = t1;
        this.t2 = t2;
        this.original1 = original1;
        this.original2 = original2;
        this.depth = depth;

        // TODO: Consider if new state should be collapsed upon creation.
        CollapseTrees();
    }

    @Override
    public void Apply(Action a) {
        CollapseTrees();

        TerminusEstAction tea = (TerminusEstAction) a;

        String zoekNaam = tea.taxon.getName();

        Tree alpha[] = new Tree [1];
        Tree beta[] = new Tree [1];

        Tree newGuyA = t1.copy(alpha,zoekNaam);
        Tree newGuyB = t2.copy(beta, zoekNaam);

        Tree killA = alpha[0].delete();
        if( killA == null )
            killA = newGuyA;

        Tree killB = beta[0].delete();
        if( killB == null )
            killB = newGuyB;

        t1 = killA;
        t2 = killB;
    }

    @Override
    public Action[] GetLegalActions() {
        Vector taxa = new Vector();
        t1.getLeafDescendants(taxa);

        Hashtable ht = new Hashtable();

        Tree numToTaxonT1[] = new Tree[taxa.size()];
        Tree numToTaxonT2[] = new Tree[taxa.size()];

        for(int x=0; x<taxa.size(); x++)
        {
            Tree leaf = (Tree) taxa.elementAt(x);
            leaf.num = x;	//! This assigns a number to the leaf.
            numToTaxonT1[x] = leaf;
            ht.put( leaf.name, leaf );	//! So we can find the corresponding node in the second tree
        }

        Vector taxaSecondTree = new Vector();
        t2.getLeafDescendants(taxaSecondTree);

        if( taxaSecondTree.size() != taxa.size() )
        {
            System.out.println("Catastrophic error. Trees have different numbers of taxa.");
            System.exit(0);
        }

        int n = taxa.size();

        for(int x=0; x<n; x++)
        {
            Tree leaf = (Tree) taxaSecondTree.elementAt(x);

            Tree co = (Tree) ht.get(leaf.name);
            if( co == null )
            {
                System.out.println("Catastrophic error. Couldn't find taxon from second tree in first tree.");
                System.exit(0);
            }
            if( !leaf.name.equals(co.name) )
            {
                System.out.println("Catastrophic error. Hashing problem lining up taxa.");
                System.exit(0);
            }
            leaf.num = co.num;
            numToTaxonT2[leaf.num] = leaf;
        }

        t1.buildClusters( n );
        t2.buildClusters( n );

        return new Action[0];
    }

    @Override
    public boolean EndState() {
        Vector ST = main.TerminusEst.Tree.computeMaxSTsets(t1,t2);
        if( ST.size() == 1 ) {
            Tree.collapseMaxSTsets(t1, t2, ST);
            if (!t1.isLeaf()) {
                System.out.println("ERROR! Compatible ST-set not a leaf.");
                System.exit(0);
            }
            return true;
        }

        // TODO: Other finish states?

        return false;
    }

    @Override
    public IDeepCopy DeepCopy() {
        return new TerminusEstState(t1.copy(null, null), t2.copy(null, null),
                original1, original2, depth);
    }

    private void CollapseTrees() {
        Vector ST = Tree.computeMaxSTsets(t1,t2);
        Tree.collapseMaxSTsets(t1,t2,ST);
    }
}
