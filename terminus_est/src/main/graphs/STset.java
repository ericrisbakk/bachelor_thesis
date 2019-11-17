package main.graphs;

import java.util.*;

class STset
{
    public Tree quasiLCA[];
    Vector subtrees[];
    STset children[];


    public STset()
    {
        //! one per tree
        quasiLCA = new Tree[2];
        subtrees = new Vector[2];

        subtrees[0] = new Vector();
        subtrees[1] = new Vector();

        children = new STset[2];	//! used to build the tree that reflects the order they are merged
    }

    public Tree makeTree()
    {
        if( children[0] == null )
        {
            Tree t = new Tree();
            t.name = ((Tree) (subtrees[0].elementAt(0))).name;

            t.netParent[0] = null;
            t.netParent[1] = null;

            return t;
        }
        else
        {
            Tree t = new Tree();
            Tree t1 = children[0].makeTree();
            Tree t2 = children[1].makeTree();
            t.addChild(t1);
            t.addChild(t2);

            t1.netParent[0] = t;
            t1.netParent[1] = null;

            t2.netParent[0] = t;
            t2.netParent[1] = null;

            return t;
        }
    }

    public void dump()
    {
        if( children[0] != null )
        {
            System.out.print("<");
            children[0].dump();
            System.out.print(",");
            children[1].dump();
            System.out.print(">");
        }
        else
        {
            System.out.print(((Tree) (subtrees[0].elementAt(0))).name);
        }
    }

//! NEW IN VERSION 2 ---------



// ---------------------------------


    //! count[] passes back the number of taxa in total that are seen
    public String getTaxaString(int count[])
    {
        StringBuffer sb = new StringBuffer();
        //! sb.append('{');

        for(int x=0; x<subtrees[0].size(); x++ )
        {
            ((Tree) subtrees[0].elementAt(x)).harvestTaxa(sb, count);
        }
        //! sb.append('}');

        return sb.toString();
    }

//! merges A and B and remembers this

    public STset(STset A, STset B)
    {
        quasiLCA = new Tree[2];
        subtrees = new Vector[2];
        children = new STset[2];

        children[0] = A;
        children[1] = B;

        for(int t=0; t<=1; t++ )
        {
            subtrees[t] = new Vector();
            quasiLCA[t] = A.quasiLCA[t];

            for(int x=0; x<A.subtrees[t].size(); x++ )
            {
                subtrees[t].addElement( A.subtrees[t].elementAt(x) );
            }

            for(int x=0; x<B.subtrees[t].size(); x++ )
            {
                subtrees[t].addElement( B.subtrees[t].elementAt(x) );
            }

            if( subtrees[t].size() == quasiLCA[t].children.size() )
            {
                if( quasiLCA[t].parent != null )
                {
                    subtrees[t].removeAllElements();
                    subtrees[t].addElement( quasiLCA[t] );
                    quasiLCA[t] = quasiLCA[t].parent;
                }
            }
        }


    }

}