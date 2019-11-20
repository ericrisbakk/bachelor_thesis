package main.TerminusEst;

import main.mcts.base.Action;
import main.mcts.base.State;
import main.utility.IDeepCopy;

import java.util.Hashtable;
import java.util.Vector;

public class TerminusEstState implements State {
    public final boolean USEMINCLUS = true; // If false, we're using all taxa instead - we always use size-2 conflicting clusters, however.

    public Tree t1;
    public Tree t2;
    // public Tree original1;
    // public Tree original2;
    public int depth; // Number of actions taken.

    public TerminusEstState(Tree t1, Tree t2,
                            // Tree original1, Tree original2,
                            int depth) {
        this.t1 = t1;
        this.t2 = t2;
        // this.original1 = original1;
        // this.original2 = original2;
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

        //! --------------------------------------------

        //! Ok, now let's count the terminals...

        int terminals = 0;
        Vector termVec = new Vector();

        boolean isTerminal[] = new boolean[n];

        for( int x=0; x<n; x++ )
        {
            Tree u = numToTaxonT1[x].parent;
            Tree v = numToTaxonT2[x].parent;

            int intersect = 0;
            for( int y=0; y<n; y++ )
            {
                if( u.cluster[y] && v.cluster[y] ) intersect++;
                if( intersect > 1 ) break;
            }
            if( intersect == 0 )
            {
                System.out.println("Catastrophic error, somehow a taxon is not in the intersection of its witness sets.");
                System.exit(0);
            }
            if( intersect == 1 )
            {
                terminals++;
                termVec.addElement( numToTaxonT1[x] );	//! records it's position in T1
                isTerminal[x] = true;	//! let's us quickly determine if something is a terminal
            }

        }

        Vector guessSet = taxa;

        boolean SeenTwoClus = false;

        //! ----------------------------------------------
        //! Check whether there are two conflicting size-2 clusters

        outerWall: for(int x=0; x<termVec.size(); x++ )
        {
            Tree termT1 = (Tree) termVec.elementAt(x);

            int look = termT1.num;

            Tree termT2 = numToTaxonT2[look];

            int look2 = -1;
            int look3 = -1;

            Tree pt1 = termT1.parent;
            Tree pt2 = termT2.parent;

            int c1size = 0;
            int c2size = 0;

            boolean union[] = new boolean[ pt1.cluster.length ];

            for(int y=0; y<pt1.cluster.length; y++ )
            {
                if( pt1.cluster[y] ) c1size++;
                if( pt2.cluster[y] ) c2size++;
                union[y] = pt1.cluster[y] || pt2.cluster[y];
                if( (c1size > 2) || (c2size > 2) ) break;
            }

            if( (c1size == 2) && (c2size == 2) )
            {
                SeenTwoClus = true;
                guessSet = new Vector();

                int check = 0;
                for( int m=0; m<union.length; m++ )
                {
                    if( union[m] )
                    {
                        guessSet.addElement( numToTaxonT1[m] );
                        check++;
                    }
                }
                if( check != 3 )
                {
                    System.out.println("Catastrophic error. Claimed to have found two conflicting size-2 clusters but didn't get 3 taxa.");
                    System.exit(0);
                }
                break outerWall;
            }
        }

        //! -----------------------------------------------

        if((!SeenTwoClus) && USEMINCLUS)
        {
            //! Ok, let's build the stuff from minimal clusters...

            Vector v1 = new Vector();
            Vector v2 = new Vector();

            boolean hitSet[] = new boolean[n];

            t1.harvestLocalMinimal(v1);
            t2.harvestLocalMinimal(v2);

            buiten: for(int y=0; y<v1.size(); y++)
            {
                Tree node1 = (Tree) v1.elementAt(y);

                //! Check that none of the T2 clusters are inside it.

                binnen: for(int z=0; z<v2.size(); z++ )
                {
                    Tree node2 = (Tree) v2.elementAt(z);

                    for(int q=0; q<n; q++ )
                    {
                        if( node2.cluster[q] && (!node1.cluster[q]) ) continue binnen;
                    }
                    continue buiten;
                }
                //! Ok, we can now take 2 taxa from this cluster...

                int toDo = 2;
                boolean gotTerminal = false;

                for( int z=0; z<node1.cluster.length; z++ )
                {
                    if( toDo == 0 ) break;
                    if( !node1.cluster[z] ) continue;
                    if( isTerminal[z] )
                    {
                        hitSet[z] = true;
                        gotTerminal = true;
                        toDo--;
                        continue;
                    }
                    if( (toDo == 2) || gotTerminal )
                    {
                        hitSet[z] = true;
                        toDo--;
                        continue;
                    }
                }
            }

            outer: for(int y=0; y<v2.size(); y++)
            {
                Tree node1 = (Tree) v2.elementAt(y);

                //! Check that none of the T1 clusters are inside it.

                inner: for(int z=0; z<v1.size(); z++ )
                {
                    Tree node2 = (Tree) v1.elementAt(z);

                    for(int q=0; q<n; q++ )
                    {
                        if( node2.cluster[q] && (!node1.cluster[q]) ) continue inner;
                    }
                    continue outer;
                }
                //! Ok, we can now take 2 taxa from this cluster...

                int toDo = 2;
                boolean gotTerminal = false;

                for( int z=0; z<node1.cluster.length; z++ )
                {
                    if( toDo == 0 ) break;
                    if( !node1.cluster[z] ) continue;
                    if( isTerminal[z] )
                    {
                        hitSet[z] = true;
                        gotTerminal = true;
                        toDo--;
                        continue;
                    }
                    if( (toDo == 2) || gotTerminal )
                    {
                        hitSet[z] = true;
                        toDo--;
                        continue;
                    }
                }
            }

            // Create guessSet
            guessSet = new Vector();
            for(int span=0; span<hitSet.length; span++ )
            {
                if(hitSet[span]) guessSet.addElement( numToTaxonT1[span] );
            }

        }

        Action[] legalActions = new Action[guessSet.size()];
        for (int i = 0; i < legalActions.length; ++i) {
            legalActions[i] = new TerminusEstAction((Tree) guessSet.elementAt(i));
        }

        return legalActions;
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
                // original1, original2,
                depth);
    }

    private void CollapseTrees() {
        Vector ST = Tree.computeMaxSTsets(t1,t2);
        Tree.collapseMaxSTsets(t1,t2,ST);
    }
}
