package main.graphs;

import java.util.*;

public class Network {
    Hashtable mapNameToLeaf;	//! also functions as a set of taxa...this should be
    //! invariant i.e. always correct!
    Tree root;

    int currentTaxa;

    public Network( Tree top )
    {
        this.root = top;
        resetNetwork();
    }

    public void resetNetwork()
    {
        mapNameToLeaf = new Hashtable();

        int count[] = new int[1];
        count[0] = 0;

        root.cleanNetwork(mapNameToLeaf, count);

        currentTaxa = count[0];

        if(TerminusEstV3.BUILD_VERBOSE) System.out.println("Network currently has "+currentTaxa+" taxa.");
    }

    public Hashtable getTaxa()
    {
        return mapNameToLeaf;
    }

    public boolean checkDisplay(Tree t, int which)
    {
        Tree safe = t.copy(null,null);

        t.renumber(mapNameToLeaf);	//! makes sure the 'num' values are synchronised with the network
        t.buildClusters(currentTaxa);

        //! This bit is extremely lazy...

        Vector harvestNet = new Vector();
        this.root.harvestNetClus(harvestNet,which);

        Vector harvestTree = new Vector();
        t.harvestTreeClus(harvestTree);

        funky: for(int l=0; l<harvestTree.size(); l++)
        {
            boolean cl[] = (boolean[]) harvestTree.elementAt(l);

            for(int m=0; m<harvestNet.size(); m++)
            {
                boolean compare[] = (boolean[]) harvestNet.elementAt(m);
                if( Tree.equalTo(compare,cl) )
                {
                    continue funky;	//! found it!
                }
            }
            return false;	//! couldn't find the cluster in the network
        }

        return true;
    }

    public Tree getNetworkLeaf(String s)
    {
        return( (Tree) mapNameToLeaf.get(s) );
    }

    //! make sure that currentTaxa is correct...

    public void buildLeftRightClusters()
    {
        for(int tree=0; tree<2; tree++ )
        {
            root.buildOneSidedClusters(tree, currentTaxa);
        }

    }

}