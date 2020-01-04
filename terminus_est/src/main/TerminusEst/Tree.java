package main.TerminusEst;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class Tree {
    Tree parent;
    Vector children;
    boolean isLeaf;

    //! This is only for taxa...
    String name;
    int num;

    //! ----- data under here does not get cloned...

    boolean cluster[];

    //! ---------------------------------------------

    Tree netParent[];	//! A size-2 array, the at most two parents in the network
    //! only used in the constructive phase of the algorithm...
    boolean visited;
    boolean netClus[][];
    int printNum;

    //! ----------------------------------------------

    int hybLabel;

    //! ----------------------------------------------

    public Tree()
    {
        isLeaf = false;
        parent = null;
        children = null;

        //! Things only relevant for taxa...
        name = null;
        num = -1;

        netParent = new Tree[2];
    }

    public void harvestTreeClus(Vector harvest)
    {
        if( this.isLeaf() )
        {
            harvest.addElement( cluster );
        }
        else
        {
            harvest.addElement( cluster );
            for(int x=0; x<children.size(); x++ )
            {
                Tree child = (Tree) children.elementAt(x);
                child.harvestTreeClus(harvest);
            }
        }

    }

    public void harvestNetClus(Vector harvestNet, int tree)
    {
        if( this.isLeaf() )
        {
            harvestNet.addElement( netClus[tree] );
        }
        else
        {
            harvestNet.addElement( netClus[tree] );

            for(int x=0; x<children.size(); x++ )
            {
                Tree child = (Tree) children.elementAt(x);

                if( child.netParent[0] == this ) child.harvestNetClus(harvestNet, tree);	//! has the effect of only visiting
                //! treenodes and reticulations via their left edge
            }
        }
    }

    public void renumber( Hashtable netMap )
    {
        if( this.isLeaf() )
        {
            Tree netNode = (Tree) netMap.get(name);
            int newNum = netNode.num;
            this.num = newNum;
        }
        else
        {
            for(int x=0; x<children.size(); x++ )
            {
                Tree child = (Tree) children.elementAt(x);
                child.renumber(netMap);
            }
        }
    }

    public void getDifference( Hashtable donotWant, Hashtable diff )
    {
        if( this.isLeaf() )
        {
            String check = (String) donotWant.get(this.name);
            if( check == null ) diff.put(this.name, this.name);
        }
        else
        {
            for(int x=0; x<children.size(); x++ )
            {
                Tree child = (Tree) children.elementAt(x);
                child.getDifference(donotWant, diff);
            }
        }
    }

    //! returns true if v1 is a superset of v2
    public static boolean superset(boolean v1[], boolean v2[])
    {
        for(int x=0; x<v1.length; x++)
        {
            if(v2[x] && (!v1[x])) return false;
        }
        return true;
    }

    public static boolean equalTo(boolean v1[], boolean v2[])
    {
        for(int x=0; x<v1.length; x++)
        {
            if(v2[x] != v1[x]) return false;
        }
        return true;
    }

    public void assignLeafNumbersBuildClusters(int num, int counter[])
    {
        cluster = new boolean[num];

        if(this.isLeaf())
        {
            this.num = counter[0]++;
            cluster = new boolean[num];
            cluster[this.num] = true;
        }
        else
        {
            for(int x=0; x<children.size(); x++ )
            {
                Tree child = (Tree) children.elementAt(x);
                child.assignLeafNumbersBuildClusters(num,counter);
                for(int y=0; y<num; y++)
                {
                    this.cluster[y] = child.cluster[y] || this.cluster[y];
                }
            }

        }

    }


    //! ----------------------
    public Tree findLeaf(String str)
    {
        if( this.isLeaf() )
        {
            if( name.equals(str) ) return this;
            return null;
        }
        else
        {
            for(int x=0; x<children.size(); x++ )
            {
                Tree child = (Tree) children.elementAt(x);
                Tree gotIt = child.findLeaf(str);
                if( gotIt != null ) return gotIt;
            }
            return null;
        }
    }
//! ---------------------

    public void buildClusters(int n)
    {
        cluster = new boolean[n];

        if( this.isLeaf() )
        {
            cluster[num] = true;
        }
        else
        {
            for(int x=0; x<children.size(); x++ )
            {
                Tree child = (Tree) children.elementAt(x);
                child.buildClusters(n);
                for( int y=0; y<child.cluster.length; y++ )
                {
                    if( child.cluster[y] ) cluster[y] = true;
                }
            }
        }
    }






    public Tree copy(Tree findMe[], String huntMe)
    {
        Tree me = new Tree();
        me.isLeaf = this.isLeaf;
        me.parent = null;
        me.name = this.name;
        me.num = this.num;

        if( (me.name != null) && (huntMe != null))
        {
            if( me.name.equals(huntMe) )
                findMe[0] = me;
        }

        if( this.children == null )
        {
            me.children = null;
            return me;
        }

        me.children = new Vector();
        for(int x=0; x<this.children.size(); x++ )
        {
            Tree c = (Tree) this.children.elementAt(x);
            Tree cop = c.copy( findMe, huntMe );
            cop.parent = me;
            me.children.addElement( cop );
        }

        return me;
    }

    //! returns true if a is an ancestor of b
    public static boolean isAncestorOf( Tree a, Tree b )
    {
        Tree scan = b;

        do	{
            if( scan == a ) return true;
            scan = scan.parent;
        }
        while( scan != null );

        return false;
    }

    public void deleteChild(Tree c)
    {
        children.removeElement(c);
    }

    public void addChild( Tree c )
    {
        if( children == null )
        {
            children = new Vector();
        }

        children.add(c);
    }

    public Tree getParent()
    {
        return parent;
    }

    //! Only relevant for leaves
    public void setName( String s )
    {
        name = s;
    }

    //! Only relevant for leaves
    public String getName()
    {
        return name;
    }

    //! Only relevant for leaves
    public void setNumber(int n)
    {
        num = n;
        //! System.out.println("A leaf is receiving number: "+num);
    }

    public Vector getChildren()
    {
        return children;
    }

    public void setParent( Tree p )
    {
        this.parent = p;
    }

    public boolean isLeaf()
    {
        return (children==null);
    }

    public boolean isRoot()
    {
        return( parent == null );
    }

    //! if the root of the tree changes, it returns the new root, otherwise null
    public Tree delete()
    {
        Tree p = this.parent;

        p.children.removeElement( this );

        if( p.children.size() > 1 )
            return null;

        if( p.children.size() == 0 )
        {
            System.out.println("Catastrophic error. We encountered a parent node with outdegree 1.");
            System.exit(0);
        }

        //! So, parent now has 1 child, suppress....
        Tree sibling = (Tree) p.children.elementAt(0);

        Tree grandparent = p.parent;

        if( grandparent != null )
        {
            grandparent.children.removeElement( p );
            grandparent.children.addElement( sibling );
            sibling.parent = grandparent;
            return null;
        }
        else
        {
            //! It was the root...the sibling becomes the new root...
            sibling.parent = null;
            return sibling;
        }

    }

    public void dump()
    {
        if(!isLeaf()) System.out.print("(");
        boolean begun = false;
        if(!isLeaf())
            for(int x=0; x<children.size();x++)
            {
                if( begun ) System.out.print(",");
                Tree t = (Tree) children.elementAt(x);
                t.dump();
                begun = true;
            }

        if( this.isLeaf() ) System.out.print(name);
        if(!isLeaf()) System.out.print(")");
    }

    public String GetDump() {
        String s = "";
        if(!isLeaf()) s += "(";
        boolean begun = false;
        if(!isLeaf())
            for(int x=0; x<children.size();x++)
            {
                if( begun ) s += ",";
                Tree t = (Tree) children.elementAt(x);
                s += t.GetDump();
                begun = true;
            }

        if( this.isLeaf() ) s += name;
        if(!isLeaf()) s += ")";

        return s;
    }

    @Override
    public String toString() {
        return GetDump();
    }


    public void getLeafDescendants( Vector v )
    {
        if( this.isLeaf() )
        {
            v.addElement(this);
            return;
        }
        else
        {
            for(int x=0; x<children.size();x++)
            {
                ((Tree) children.elementAt(x)).getLeafDescendants(v);
            }
        }
    }


    public void harvestLocalMinimal(Vector v1)
    {
        if( this.isLeaf() ) return;

        boolean meMinimal = true;

        for(int x=0; x<children.size(); x++ )
        {
            Tree child = (Tree) children.elementAt(x);
            if( child.isLeaf() == false )
            {
                meMinimal = false;
                child.harvestLocalMinimal(v1);
            }
        }
        if( meMinimal )
        {
            v1.add(this);
        }
    }

    public int countLeafDescendants()
    {
        if( this.isLeaf() ) return 1;

        int sum = 0;
        for(int x=0; x<children.size();x++)
        {
            sum += ((Tree) children.elementAt(x)).countLeafDescendants();
        }
        return sum;
    }

    public static void collapseMaxSTsets(Tree t1, Tree t2, Vector ST)
    {
        for(int y=0;y<ST.size();y++)
        {
            STset s = (STset) ST.elementAt(y);

            int count[] = new int[1];
            String combName = s.getTaxaString(count);
            if(count[0] == 1) continue;	//! no need to collapse, it's a singleton

            combName = "{" + combName + "}";

            Tree colNodeT1 = new Tree();
            colNodeT1.setName(combName);

            Tree prune = s.quasiLCA[0];
            for(int x=0; x<s.subtrees[0].size(); x++)
            {
                prune.children.removeElement((Tree) s.subtrees[0].elementAt(x));
            }
            if(prune.children.size() == 0)
            {
                prune.children = null;
                prune.name = combName;
            }
            else
            {
                prune.children.addElement(colNodeT1);
                colNodeT1.parent = prune;
            }
            //! -----------------------

            Tree colNodeT2 = new Tree();
            colNodeT2.setName(combName);

            prune = s.quasiLCA[1];
            for(int x=0; x<s.subtrees[1].size(); x++)
            {
                prune.children.removeElement((Tree) s.subtrees[1].elementAt(x));
            }
            if(prune.children.size() == 0)
            {
                prune.children = null;
                prune.name = combName;
            }
            else
            {
                prune.children.addElement(colNodeT2);
                colNodeT2.parent = prune;
            }


        }


    }

    public Hashtable getLeafToTreeMapping()
    {
        Hashtable ht = new Hashtable();
        this.getLeafToTreeMapping(ht);
        return ht;
    }

    private void getLeafToTreeMapping(Hashtable ht)
    {
        if( this.isLeaf() )
        {
            ht.put( this.name, this );
        }
        else
        {
            for(int x=0; x<children.size();x++)
            {
                ((Tree) children.elementAt(x)).getLeafToTreeMapping(ht);
            }
        }
    }

    public static Vector computeMaxSTsets(Tree t1, Tree t2)
    {
        Vector v1 = new Vector();
        t1.getLeafDescendants(v1);

        Vector v2 = new Vector();
        t2.getLeafDescendants(v2);

        //! System.out.println(v1.size());
        //! System.out.println(v2.size());

        Vector ST = new Vector();

        for(int x=0; x<v1.size(); x++)
        {
            //! System.out.println("Constructing singleton ST-set.");

            STset s = new STset();
            Tree leaf = (Tree) v1.elementAt(x);

            s.subtrees[0].addElement(leaf);

            s.quasiLCA[0] = leaf.parent;

            for( int y=0; y<v2.size(); y++ )
            {
                Tree blad = (Tree) v2.elementAt(y);
                //! System.out.println(blad.name);
                if( blad.name.equals( leaf.name ) )
                {
                    //! System.out.println("Found corresponding taxon in tree 2.");
                    s.subtrees[1].addElement(blad);
                    s.quasiLCA[1] = blad.parent;
                    break;
                }
            }
            ST.addElement(s);
        }

        //! We now have our initial set of ST-sets.

        boolean merged = true;

        while(merged)
        {
            merged = false;

            if( ST.size() == 1 ) return ST;	//! compatible!

            outer: for(int x=0; x<ST.size(); x++ )
                for(int y=(x+1); y<ST.size(); y++ )
                {
                    STset A = (STset) ST.elementAt(x);
                    STset B = (STset) ST.elementAt(y);

                    if( (A.quasiLCA[0] == B.quasiLCA[0]) && (A.quasiLCA[1] == B.quasiLCA[1]) )
                    {
                        merged = true;

                        STset AB = new STset(A,B);
                        ST.removeElement(A);
                        ST.removeElement(B);
                        ST.addElement(AB);
                        break outer;
                    }
                }

        }
        return ST;
    }

    public void harvestTaxa( StringBuffer sb, int count[])
    {
        if( this.isLeaf() )
        {
            sb.append(" "+this.name+" ");
            count[0]++;
            return;
        }
        else
        {
            for(int x=0; x<children.size();x++)
            {
                ((Tree) children.elementAt(x)).harvestTaxa(sb, count);
            }
        }
    }

    //! This ASSUMES THEY ARE COMPATIBLE!
    public static Tree commonRefinement( Tree t1, Tree t2 )
    {
        Vector ST = Tree.computeMaxSTsets(t1,t2);
        if( ST.size() != 1 )
        {
            System.out.println("Tried to find common refinement of two non-compatible trees. Exiting.");
            System.exit(0);
        }
        Tree t = ((STset) (ST.elementAt(0))).makeTree();
        return t;
    }


    public static Tree restrict( Tree orig, Hashtable resTaxa )
    {
        String s[] = new String [resTaxa.size()];
        int counter = 0;

        Enumeration e = resTaxa.keys();
        while( e.hasMoreElements() )
        {
            s[counter++] = (String) e.nextElement();
        }
        return Tree.restrict( orig, s );
    }


    public static Tree restrict( Tree orig, String resTaxa[] )
    {
        Tree stripT1 = orig.copy(null, null);

        Hashtable hT1 = stripT1.getLeafToTreeMapping();

        //! These are the things we *shouldn't* delete...
        Hashtable want = new Hashtable();
        for(int x=0; x<resTaxa.length; x++)
        {
            want.put(resTaxa[x],resTaxa[x]);
        }

        Enumeration e = hT1.keys();
        while( e.hasMoreElements() )
        {
            String seek = (String) e.nextElement();
            if( want.get(seek) != null ) continue;

            Tree kill1 = (Tree) hT1.get(seek);

            if(kill1 == null)
            {
                System.out.println("Catastrophic error looking for: "+seek+" ... "+kill1);
                System.exit(0);
            }
            Tree temp = kill1.delete();
            if( temp != null ) stripT1 = temp;
        }
        return stripT1;
    }

    //! -------------------------------------------------------------

    public void dumpEnewick()
    {
        int lab[] = new int[1];
        lab[0] = 1;	//! numbering of hybridization nodes starts at 1

        this.allocateHybLabel(lab);

        System.out.print("// ");

        this.internalENewickDump();

        System.out.println("root;");
    }

    private void internalENewickDump()
    {
        if(this.isLeaf())
        {
            System.out.print(name);
            return;
        }

        System.out.print("(");

        for(int x=0; x<children.size();x++)
        {
            if( x!=0 ) System.out.print(",");
            Tree c =(Tree) children.elementAt(x);
            if( c.netParent[0] == this ) c.internalENewickDump();
            else
            if( c.netParent[1] != null )
            {
                if( c.netParent[1] == this )
                {
                    System.out.print("#H"+c.hybLabel);
                }
                else
                {
                    System.out.println("CATASTROPHIC ERROR (1) with reticulation node parent.");
                    System.exit(0);
                }
            }
            else
            {
                System.out.println("CATASTROPHIC ERROR (2) with reticulation node parent.");
                System.exit(0);
            }

        }

        System.out.print(")");
        if(this.netParent[1] != null)
        {
            System.out.print("#H"+hybLabel);
        }

    }

    public String getEnewick()
    {
        int lab[] = new int[1];
        lab[0] = 1;	//! numbering of hybridization nodes starts at 1

        this.allocateHybLabel(lab);

        return this.getInternalENewick() + "root;";
    }

    private String getInternalENewick()
    {
        if(this.isLeaf())
        {
            return name;
        }

        String s = "(";

        for(int x=0; x<children.size();x++)
        {
            if( x!=0 ) s += ",";
            Tree c =(Tree) children.elementAt(x);
            if( c.netParent[0] == this ) c.internalENewickDump();
            else
            if( c.netParent[1] != null )
            {
                if( c.netParent[1] == this )
                {
                    s += "#H"+c.hybLabel;
                }
                else
                {
                    System.out.println("CATASTROPHIC ERROR (1) with reticulation node parent.");
                    System.exit(0);
                }
            }
            else
            {
                System.out.println("CATASTROPHIC ERROR (2) with reticulation node parent.");
                System.exit(0);
            }

        }

        s += ")";
        if(this.netParent[1] != null)
        {
            s += "#H"+hybLabel;
        }

        return s;
    }

    public void allocateHybLabel(int label[])
    {
        if( this.isLeaf() ) return;

        //! If it's a hybrid node, give it a number
        if( netParent[1] != null )
        {
            hybLabel = label[0]++;
        }

        for(int x=0; x<children.size();x++)
        {
            Tree c =(Tree) children.elementAt(x);
            if( c.netParent[0] == this ) c.allocateHybLabel(label);
        }
    }




    //! ---------------------------------------------------------------


    //! These functions are only used when the tree nodes are part of a network

    //! This visits every node of the network only once, by traversing a canonical spanning tree

    public void cleanNetwork(Hashtable nameToLeaf, int count[] )
    {
        visited = false;

        netClus = null;

        if( this.isLeaf() )
        {
            //! record it in the hashtable
            if(nameToLeaf != null) nameToLeaf.put( this.name, this );
            this.num = count[0]++;

            return;
        }

        if(children.size() > 2 )
        {
            System.out.println("CATASTROPHIC ERROR: shouldn't have nodes with outdegree > 2 at this point!");
            System.exit(0);
        }

        for(int x=0; x<children.size();x++)
        {
            Tree c =(Tree) children.elementAt(x);

            if( c.netParent[0] == this ) c.cleanNetwork(nameToLeaf, count);	//! has the effect of only visiting
            //! treenodes and reticulations via their left edge

            if( (c.netParent[0] != this) && (c.netParent[1] != this) )
            {
                System.out.println("CATASTROPHIC ERROR: is not registered as either parent of a retic node.");
                System.exit(0);
            }
        }

    }

//! ----------------------

//! Assumes the nodes have been numbered via dumpNetwork()

    public void dumpNetworkTreeImage(int tree)
    {
        int count[] = new int[1];
        count[0] = 1000;

        System.out.println("strict digraph G"+(tree+1)+" {");

        this.numberForDumping(null);

        this.dumpArcsTreeImage(tree);

        System.out.println("}");
    }

//! -------------------------------------------

    public void dumpNetwork()
    {
        int count[] = new int[1];
        count[0] = 1000;

        System.out.println("strict digraph G0 {");

        this.numberForDumping(count);

        this.dumpArcs();

        System.out.println("}");
    }

    //! ----------------------------
    private void dumpArcs()
    {
        if(this.isLeaf()) return;

        for(int x=0; x<children.size();x++)
        {
            Tree c =(Tree) children.elementAt(x);

            if( c.netParent[0] == this ) c.dumpArcs();
            System.out.println(this.printNum + " -> " + c.printNum);
        }
    }

//! ----------------------------------
//! This needs to be improved to colour the edges properly...

    private void dumpArcsTreeImage(int tree)
    {
        if(this.isLeaf()) return;

        for(int x=0; x<children.size();x++)
        {
            Tree c =(Tree) children.elementAt(x);

            if( c.netParent[0] == this ) c.dumpArcsTreeImage(tree);

            if( c.netParent[1] == null) System.out.println(this.printNum + " -> " + c.printNum + " [color=blue]");
            else	{
                if(c.netParent[tree] == this ) System.out.println(this.printNum + " -> " + c.printNum + " [color=blue]");
                else System.out.println(this.printNum + " -> " + c.printNum + " [color=red]");
            }

        }
    }



//! ----------------------------------

    private void numberForDumping(int count[])
    {
        if(count != null) printNum = count[0]++;

        if(this.isLeaf())
        {
            System.out.println(printNum + " [shape=circle, width=0.3, label=\""+this.name+"\"];");
        }
        else
        {
            System.out.println(printNum + " [shape=point];");
        }

        if(!this.isLeaf())
            for(int x=0; x<children.size();x++)
            {
                Tree c =(Tree) children.elementAt(x);

                if( c.netParent[0] == this ) c.numberForDumping(count);
            }
    }




//! -----------------------

    public void buildOneSidedClusters(int side, int taxa)
    {
        if(netClus == null)
        {
            netClus = new boolean[2][taxa];
        }

        if( this.isLeaf() )
        {
            netClus[side][this.num] = true;
            return;
        }

        if(children.size() > 2 )
        {
            System.out.println("CATASTROPHIC ERROR: shouldn't have nodes with outdegree > 2 at this point!");
            System.exit(0);
        }

        for(int x=0; x<children.size();x++)
        {
            Tree c =(Tree) children.elementAt(x);

            //! if c is a split node, do it anyway...
            if( c.netParent[1] == null )
            {
                c.buildOneSidedClusters(side,taxa);
                for( int y=0; y<taxa; y++)
                {
                    this.netClus[side][y] = this.netClus[side][y] || c.netClus[side][y];
                }
            }
            else
            if( c.netParent[side] == this )
            {
                c.buildOneSidedClusters(side,taxa);
                for( int y=0; y<taxa; y++)
                {
                    this.netClus[side][y] = this.netClus[side][y] || c.netClus[side][y];
                }
            }
        }

    }


}
