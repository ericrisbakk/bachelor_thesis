package main.TerminusEst;

import main.TerminusEstMC_SearchTree;

import java.util.*;

/**
 * New version of TerminusEst. The major changes is that the classes it depends on have been moved out of the file,
 * and it is runs in an object-oriented fashion rather than statically. Methods have also been provided to make
 * it easier for external methods to use this class.
 */
public class TerminusEstV4 {

    public Hashtable nameToNum;
    public Hashtable numToName;
    private Comparator<Tree> comp;

    //! VERSION 3: HASHING IS BY DEFAULT SWITCHED ON
    //! VERSION 2: LOOKUP TABLE FOR ALREADY COMPUTED SOLUTIONS
    public Hashtable lookup;


    public Integer intObjects[];

    public int seenLeaves = 0;

    public TerminusEstSolution solution = null;
    public boolean useRuntime;
    public double runtime;
    private boolean canceled = false;
    public long startTime;
    public int lowerBound;

    public TerminusEstV4(String file) {
        Setup(file);
    }

    private void Setup(String file) {
        TerminusEstInputHandler ih = new TerminusEstInputHandler();
        ih.InterpretFile(file, this);

        if (VERBOSE) System.out.println("// We saw "+seenLeaves+" taxa in total.");

        //! ----- This is just so we don't have to constantly create new Integer() objects for the lookup hashtable
        intObjects = new Integer [seenLeaves+1];

        for(int x=0; x< intObjects.length; x++ )
        {
            intObjects[x] = new Integer(x);
        }
        //! ------------------------------------

        lookup = new Hashtable();

        if(VERBOSE)
        {
            System.out.println("// Finished reading the two trees in.");
            System.out.println("// The trees are now stored internally as follows");

            t1.dump();
            System.out.println(";");

            t2.dump();
            System.out.println(";");
        }
    }

    public void UseHeuristic(Hashtable<String, Double> heuristic) {
        if (heuristic != null) {
            SORT_BY_HEURISTIC = true;
            comp = new TerminusEstV4.CompareByHeuristic(heuristic);
        }
        else {
            SORT_BY_HEURISTIC = false;
            comp = null;
        }
    }

    public int getLeafNumber( String leaf )
    {
        if( nameToNum == null ) nameToNum = new Hashtable();

        Integer i = (Integer) nameToNum.get( leaf );

        if( i != null )
        {
            return i.intValue();
        }

        seenLeaves++;

        i = new Integer(seenLeaves);

        if( TerminusEstV4.VERBOSE ) System.out.println("** Leaf '"+leaf+"' gets internal number "+seenLeaves);

        nameToNum.put( leaf, i );

        if( numToName == null )
        {
            numToName = new Hashtable();
        }

        numToName.put( i, leaf );

        return seenLeaves;
    }


    public String getLeafName( int num )
    {
        Integer i = new Integer(num);

        if( numToName == null )
        {
            numToName = new Hashtable();
        }

        String s = (String) numToName.get( i );

        return s;
    }

    public Tree t1;
    public Tree t2;

    public static String VERSION = "TerminusEstV4.jj Version 3, 30th March 2015";

    public final static boolean VERBOSE = false;
    public final static boolean BUILD_VERBOSE = false;

    //! -------------------------------------
    public static boolean USEMINCLUS = true;
    public static boolean BUILDNETWORK = true;
    public static boolean USEHASH = true;
    private boolean SORT_BY_HEURISTIC = false;

//! IN VERSION 2 we will remember when this call fails, and put it in a hash table...

    public Network hybNumAtMost( Tree t1, Tree t2, int hyb, Tree origT1, Tree origT2, int depth )
    {
        if (useRuntime && runtime < getIntervalInSeconds(System.currentTimeMillis(), startTime)) {
            canceled = true;
            return null;
        }

        if(VERBOSE) System.out.println("Entering with hyb="+hyb);

        //! origT1 and origT2 are just passed all the way down, as reference...they are never changed

        if(VERBOSE)
        {
            System.out.println("Tree 1 before collapsing:");
            t1.dump();
            System.out.println();
            System.out.println("Tree 2 before collapsing:");
            t2.dump();
            System.out.println();
        }

        Vector ST = Tree.computeMaxSTsets(t1,t2);

        if( ST.size() == 1 )
        {
            return ConstructInitialNetwork(ST, t1, t2, origT1, origT2, depth);
        }

        //! We reach this point if the two trees are not compatible.

        if( hyb == 0 ) return null;	//! is not compatible, because more than 1 max ST-set, but hyb wants 0

        //! -----------------------------------------------
        //! VERSION 2 STARTS HERE....................


        String myBitVec = null;

        if( USEHASH)
        {

            StringBuffer allTaxa = new StringBuffer();

            for(int x=0;x<ST.size();x++)
            {
                STset s = (STset) ST.elementAt(x);
                int count[] = new int[1];
                String t = s.getTaxaString(count);
                allTaxa.append(t);
                //! System.out.println("max ST set "+x+" contains taxa "+t);
                //! System.out.println(count[0]);
            }

            StringBuffer stripped = new StringBuffer();
            String back = allTaxa.toString();
            String forth = back.trim();
            allTaxa = new StringBuffer( forth );

            for(int x=0; x<allTaxa.length(); x++ )
            {
                char c = allTaxa.charAt(x);

                if( (c == '{') || (c=='}') ) continue;

                if( c == ' ' )
                {
                    //! Only add a character to the end of stripped if there isn't one there already...
                    if( stripped.length() < 1 ) continue;
                    if( stripped.charAt( stripped.length() - 1 ) == ' ' ) continue;
                }

                stripped.append(c);
            }

            String taxaList[] = stripped.toString().split(" ");


            if(VERBOSE)
            {
                System.out.println(" ---- TAXA AT THIS ITERATION --- ");
                for(int x=0; x<taxaList.length; x++)
                {
                    System.out.println(taxaList[x] + " = " + getLeafNumber(taxaList[x]));
                }
            }

            //! This is going to become a bit vector denoting which taxa we have.
            boolean got[] = new boolean[ seenLeaves + 1 ];

            for(int x=0; x<taxaList.length; x++)
            {
                int tick = getLeafNumber(taxaList[x]);
                got[tick] = true;
            }

            StringBuffer bitVec = new StringBuffer();
            for(int x=1; x<=seenLeaves; x++ )
            {
                if( got[x] ) bitVec.append('1');
                else bitVec.append('0');
            }

            myBitVec = bitVec.toString();

            if(VERBOSE)
            {
                System.out.println("Bit vector = "+myBitVec);
            }

            //! Check whether there is already a fail-recording in the hashtable.

            Integer failInt = (Integer) lookup.get(myBitVec);

            if( failInt != null )
            {
                int bound = failInt.intValue();

                //! THis means that this input is known to not have a hybridization number >= bound.

                if( bound >= hyb )
                {
                    //! WE CAN TERMINATE IMMEDIATELY...
                    if(VERBOSE) System.out.println("HASHTABLE HIT! Terminating this search branch.");
                    return null;
                }

            }

        } //! end if(USEHASH)

        //! --------------------------------------------------------------------

        Tree.collapseMaxSTsets(t1,t2,ST);

        if(VERBOSE)
        {
            System.out.println("Tree 1 after collapsing:");
            t1.dump();
            System.out.println();
            System.out.println("Tree 2 after collapsing:");
            t2.dump();
            System.out.println();
        }


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
                if( VERBOSE ) System.out.println(numToTaxonT1[x].name+" is a terminal.");
                termVec.addElement( numToTaxonT1[x] );	//! records it's position in T1
                isTerminal[x] = true;	//! let's us quickly determine if something is a terminal
            }

        }

        if(VERBOSE) System.out.println(terminals+" terminals in total!");

        //! ----------------------------------------------------------------

        if( terminals > 3*hyb )
        {
            if(VERBOSE) System.out.println("Naive 3*hyb terminal bound violated.");

            //! PUT IT IN THE HASHTABLE
            if(USEHASH) lookup.put( myBitVec, intObjects[hyb] );

            return null;
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
                        if(VERBOSE) System.out.println("2-CONFLICT: "+numToTaxonT1[m].name );
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

        boolean BrokenTwoBound = false;

        if(!SeenTwoClus)
        {
            if( terminals > (2*hyb) )
            {
                BrokenTwoBound = true;
                if(VERBOSE) System.out.println("More than 2*hyb terminals, taking the first (2*hyb)+1...");
                guessSet = new Vector();
                for(int x=0; x<=(2*hyb); x++ )
                {
                    guessSet.addElement( termVec.elementAt(x) );
                }

                // In the algorithm on paper, designed for optimizing worst-case performance, we can just use
                //! this 2r+1 guess-set. But here we wait to see if the guess set derived from minimum clusters is
                //! smaller.
            }
        }

        if((!SeenTwoClus) && USEMINCLUS)
        {
            //! Ok, let's build the stuff from minimal clusters...

            Vector v1 = new Vector();
            Vector v2 = new Vector();

            boolean hitSet[] = new boolean[n];

            t1.harvestLocalMinimal(v1);
            t2.harvestLocalMinimal(v2);

            if(VERBOSE) System.out.println(v1.size()+" quasi-minimal in T1");
            if(VERBOSE) System.out.println(v2.size()+" quasi-minimal in T2");

            buiten: for(int y=0; y<v1.size(); y++)
            {
                Tree node1 = (Tree) v1.elementAt(y);

                if(VERBOSE)
                {
                    System.out.println("Quasi minimal cluster "+y+" from T1:");
                    for(int r=0; r<node1.cluster.length; r++ )
                    {
                        if( node1.cluster[r] )
                        {
                            System.out.print( numToTaxonT1[r].name + " ");
                        }
                    }
                    if(VERBOSE) System.out.println();
                }

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

                if(VERBOSE)
                {
                    System.out.println("Quasi minimal cluster "+y+" from T2:");
                    for(int r=0; r<node1.cluster.length; r++ )
                    {
                        if( node1.cluster[r] )
                        {
                            System.out.print( numToTaxonT2[r].name + " ");
                        }
                    }
                    System.out.println();
                }

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

            int hitCount = 0;
            for(int w=0; w<hitSet.length; w++ )
            {
                if( hitSet[w] ) hitCount++;
            }
            if(VERBOSE) System.out.println("HittingSet has size "+hitCount);
            if(VERBOSE) System.out.println("Previous guessSet has size "+guessSet.size());
            if( hitCount < guessSet.size() )
            {
                if(VERBOSE) System.out.println("WE IMPROVED!");
                guessSet = new Vector();
                for(int span=0; span<hitSet.length; span++ )
                {
                    if(hitSet[span]) guessSet.addElement( numToTaxonT1[span] );
                }
            }

        }

        //! ---------------------------------------------------------------------------
        if(VERBOSE) System.out.println(guessSet.size()+" taxa to consider deleting.");

        if (SORT_BY_HEURISTIC) {
            guessSet.sort(comp);
        }

        for( int m=0; m<guessSet.size(); m++ )
        {
            Tree del = (Tree) guessSet.elementAt(m);
            if(VERBOSE) System.out.println("Deleting taxon "+del.name);

            String zoekNaam = del.name;

            Tree alpha[] = new Tree [1];
            Tree beta[] = new Tree [1];


            Tree newGuyA = t1.copy(alpha,zoekNaam);
            Tree newGuyB = t2.copy(beta, zoekNaam);

            // System.out.println("NewGuyA... "+alpha[0].name);
            // System.out.println("NewGuyB... "+beta[0].name);

            Tree killA = alpha[0].delete();
            if( killA != null )
            {
                if(VERBOSE) System.out.println("New root in T1...");
            }
            else killA = newGuyA;


            Tree killB = beta[0].delete();

            if( killB != null )
            {
                if(VERBOSE) System.out.println("New root in T2...");
            }
            else killB = newGuyB;

            if(VERBOSE)
            {
                killA.dump();
                System.out.println();
            }
            if(VERBOSE)
            {
                killB.dump();
                System.out.println();
            }

            Network net = null;
            net = hybNumAtMost( killA, killB, hyb-1, origT1, origT2, depth+1 );

            if( net != null )
            {
                return GrowNetwork(del, net, origT1, origT2);
            }
        }

        //! PUT IT IN THE HASHTABLE
        if(USEHASH) lookup.put( myBitVec, intObjects[hyb] );
        return null;
    }

    public static Network GrowNetwork(Tree del, Network net, Tree origT1, Tree origT2) {
        String s[] = getTaxaFromString(del.name);
        //! for(int p=0; p<s.length; p++) System.out.print("["+s[p]+"] ");
        //! System.out.println();

        Tree stripT1 = Tree.restrict(origT1, s);
        Tree stripT2 = Tree.restrict(origT2, s);

        if(BUILD_VERBOSE)
        {
            System.out.print("T1: ");
            stripT1.dump();	System.out.println(";");

            System.out.print("T2: ");
            stripT2.dump(); System.out.println(";");

            System.out.print("Common binary refinement: ");
        }

        Tree commonrf = Tree.commonRefinement(stripT1, stripT2);

        if(BUILD_VERBOSE)
        {
            commonrf.dump(); System.out.println(";");
        }

        //! graft him onto the old network...
        //! first we need to have the union of the two taxa sets...

        Hashtable old = net.getTaxa();

        Hashtable newTaxa = new Hashtable();

        for(int y=0; y<s.length; y++)
        {
            newTaxa.put(s[y],s[y]);
        }
        Enumeration e = old.keys();
        while( e.hasMoreElements() )
        {
            String k = (String) e.nextElement();
            newTaxa.put(k,k);
        }

        //! newTaxa now contains the union of the network taxa and the new guy to be grafted on...
        //! -----------------------------

        Tree locate[] = new Tree[2];

        locate[0] = Tree.restrict(origT1, newTaxa);
        locate[1] = Tree.restrict(origT2, newTaxa);

        int t = newTaxa.size();

        Hashtable zoekClus[] = new Hashtable[2];

        for(int loop=0; loop<2; loop++)
        {
            int counter[] = new int[1];
            counter[0] = 0;

            //! Build the 0 -> n-1 numbering, build the vertex clusters,
            //! get the leaf numbers of the to-graft taxa, put them in a search vector...

            locate[loop].assignLeafNumbersBuildClusters(t, counter);

            Hashtable l = locate[loop].getLeafToTreeMapping();

            boolean zoek[] = new boolean[t];

            for(int scan=0; scan<s.length; scan++)
            {
                Tree node = (Tree) l.get(s[scan]);
                zoek[ node.num ] = true;
            }

            //! Find an arbitrary starting point in the subtree we are grafting on
            Tree bottom = locate[loop].findLeaf(s[0]);

            //! Work up the tree until we have a vertex cluster that is a (not nec. strict) superset of 's'
            while( !Tree.superset(bottom.cluster,zoek) ) bottom = bottom.parent;

            //! Now, we've found the LCA...what now....
            Tree lca = bottom;

            //! if the lca cluster is exactly equal to the zoek cluster, go one higher.
            if( Tree.equalTo(lca.cluster,zoek) )
            {
                lca = lca.parent;
            }

            Hashtable difference = new Hashtable();

            Hashtable donotWant = new Hashtable();
            for(int x=0; x<s.length;x++ ) donotWant.put(s[x],s[x]);

            lca.getDifference( donotWant, difference );

            zoekClus[loop] = difference;	//! This is the cluster we need to look for in the network

        }

        //! Now we need to find out the re-grafting points



        net.buildLeftRightClusters();

        //! Ok, so now the pre-graft network has the vertex clusters for the two trees inside its nodes.

        Tree graftAbove[] = new Tree[2];

        for(int tree=0; tree<2; tree++)
        {
            boolean kijkClus[] = new boolean[net.currentTaxa];

            Enumeration k = zoekClus[tree].keys();

            String nom = null;

            while(k.hasMoreElements())
            {
                nom = (String) k.nextElement();
                Tree netL = net.getNetworkLeaf(nom);
                int code = netL.num;
                kijkClus[code] = true;
            }

            //! So kijkClus has now got the correct numbering for the network
            //! The last 'nom' is fine...

            Tree base = net.getNetworkLeaf(nom);

            while( !Tree.superset(base.netClus[tree], kijkClus) )
            {
                if( base.netParent[1] == null )
                {
                    //! split node
                    base = base.netParent[0];
                }
                else
                {
                    //! reticulation
                    base = base.netParent[tree];
                }
            }
            graftAbove[tree] = base;
        }

        //! System.out.println("About to graft component onto network...");

        if( graftAbove[0] == graftAbove[1] )
        {
            System.out.println("CATASTROPHIC ERROR: It should never be necessary to graft two retic edges on the same edge.");
            System.exit(0);
        }

        if( (graftAbove[0].netParent[1] != null) || (graftAbove[1].netParent[1] != null) )
        {
            System.out.println("CATASTROPHIC ERROR: It should never be necessary to graft on a reticulation edge.");
            System.exit(0);
        }

        Tree retic = new Tree();
        retic.addChild( commonrf );	//! single child
        commonrf.netParent[0] = retic;
        commonrf.netParent[1] = null;

        for(int graft=0;graft<2; graft++)
        {
            Tree target = graftAbove[graft];

            Tree inbetween = new Tree();
            inbetween.addChild(target);
            inbetween.addChild(retic);
            retic.netParent[graft] = inbetween;

            inbetween.netParent[0] = target.netParent[0];
            inbetween.netParent[1] = null;

            target.netParent[0] = inbetween;
            target.netParent[1] = null;

            inbetween.netParent[0].deleteChild(target);
            inbetween.netParent[0].addChild(inbetween);
        }

        net.resetNetwork();

        return net;
    }

    // Taes in computed MaxSTSet, and builds tree.
    public static Network ConstructInitialNetwork(Vector ST, Tree t1, Tree t2, Tree origT1, Tree origT2, int depth) {
        if(VERBOSE) System.out.println("Compatibility detected.");

        long timeEnd = System.currentTimeMillis();
        double seconds =  ((double)(timeEnd - timeNow))/1000.0;

        if (VERBOSE) {
            System.out.println("// -----------------------------");
            System.out.println("// HYBRIDIZATION NUMBER = " + depth);
            System.out.println("// -----------------------------");
            if (BUILDNETWORK == false) System.out.println("// Real-time elapsed in seconds: " + seconds);
        }

        if(BUILDNETWORK == false) System.exit(0);

        Tree.collapseMaxSTsets(t1,t2,ST);

        Network net = null;

        if(t1.isLeaf())
        {
            if(BUILD_VERBOSE) System.out.println("Will output MAAF components in hanging-back order (first is the base tree):" );
            String s[] = getTaxaFromString(t1.name);
            // for(int p=0; p<s.length; p++) System.out.print("["+s[p]+"] ");
            // System.out.println();

            Tree stripT1 = Tree.restrict(origT1, s);
            Tree stripT2 = Tree.restrict(origT2, s);

            if(BUILD_VERBOSE)
            {
                System.out.print("T1: ");
                stripT1.dump();	System.out.println(";");

                System.out.print("T2: ");
                stripT2.dump(); System.out.println(";");

                System.out.print("Common binary refinement: ");
            }

            Tree commonrf = Tree.commonRefinement(stripT1, stripT2);

            if(BUILD_VERBOSE)
            {
                commonrf.dump(); System.out.println(";");
            }

            Tree fakeRoot = new Tree();
            fakeRoot.addChild(commonrf);
            commonrf.netParent[0] = fakeRoot;
            commonrf.netParent[1] = null;

            net = new Network(fakeRoot);
        }
        else
        {
            System.out.println("ERROR! Compatible ST-set not a leaf.");
            System.exit(0);
        }

        return net;
    }

    public String GetBitString(Tree t1, Tree t2) {
        Vector ST = Tree.computeMaxSTsets(t1, t2);
        return GetBitString(ST);
    }

    public String GetBitString(Vector ST) {

        StringBuffer allTaxa = new StringBuffer();

        for(int x=0;x<ST.size();x++)
        {
            STset s = (STset) ST.elementAt(x);
            int count[] = new int[1];
            String t = s.getTaxaString(count);
            allTaxa.append(t);
            //! System.out.println("max ST set "+x+" contains taxa "+t);
            //! System.out.println(count[0]);
        }

        StringBuffer stripped = new StringBuffer();
        String back = allTaxa.toString();
        String forth = back.trim();
        allTaxa = new StringBuffer( forth );

        for(int x=0; x<allTaxa.length(); x++ )
        {
            char c = allTaxa.charAt(x);

            if( (c == '{') || (c=='}') ) continue;

            if( c == ' ' )
            {
                //! Only add a character to the end of stripped if there isn't one there already...
                if( stripped.length() < 1 ) continue;
                if( stripped.charAt( stripped.length() - 1 ) == ' ' ) continue;
            }

            stripped.append(c);
        }

        String taxaList[] = stripped.toString().split(" ");


        if(VERBOSE)
        {
            System.out.println(" ---- TAXA AT THIS ITERATION --- ");
            for(int x=0; x<taxaList.length; x++)
            {
                System.out.println(taxaList[x] + " = " + getLeafNumber(taxaList[x]));
            }
        }

        //! This is going to become a bit vector denoting which taxa we have.
        boolean got[] = new boolean[ seenLeaves + 1 ];

        for(int x=0; x<taxaList.length; x++)
        {
            int tick = getLeafNumber(taxaList[x]);
            got[tick] = true;
        }

        StringBuffer bitVec = new StringBuffer();
        for(int x=1; x<=seenLeaves; x++ )
        {
            if( got[x] ) bitVec.append('1');
            else bitVec.append('0');
        }

        return bitVec.toString();
    }

    /**
     * Given a sequence of deletions (represented by an array of strings that are the names of the taxa),
     * the method performs these deletions to check whether they correctly lead to a valid end-state..
     * @param T1
     * @param T2
     * @param deletions
     * @return False if the sequence of deletions cannot create a valid end-state, or creates it too early.
     */
    public static boolean verifyHybNum( Tree T1, Tree T2, String[] deletions)
    {
        // Ensure that we don't make any changes to original t1 and t2.
        T1 = T1.copy(null, null);
        T2 = T2.copy(null, null);

        Vector ST = Tree.computeMaxSTsets(T1, T2);
        Tree.collapseMaxSTsets(T1, T2, ST);

        for (int del = 0; del < deletions.length; ++del) {

            // We get to the finish earlier than intended.
            if (ST.size() == 1) {
                return false;
            }

            //! --------------------------------------------
            //! Building possible options.

            Vector taxa = new Vector();
            T1.getLeafDescendants(taxa);

            Hashtable ht = new Hashtable();

            Tree numToTaxonT1[] = new Tree[taxa.size()];
            Tree numToTaxonT2[] = new Tree[taxa.size()];

            for (int x = 0; x < taxa.size(); x++) {
                Tree leaf = (Tree) taxa.elementAt(x);
                leaf.num = x;    //! This assigns a number to the leaf.
                numToTaxonT1[x] = leaf;
                ht.put(leaf.name, leaf);    //! So we can find the corresponding node in the second tree
            }

            Vector taxaSecondTree = new Vector();
            T2.getLeafDescendants(taxaSecondTree);

            if (taxaSecondTree.size() != taxa.size()) {
                System.out.println("Catastrophic error. Trees have different numbers of taxa.");
                System.exit(0);
            }

            int n = taxa.size();

            for (int x = 0; x < n; x++) {
                Tree leaf = (Tree) taxaSecondTree.elementAt(x);

                Tree co = (Tree) ht.get(leaf.name);
                if (co == null) {
                    System.out.println("Catastrophic error. Couldn't find taxon from second tree in first tree.");
                    System.exit(0);
                }
                if (!leaf.name.equals(co.name)) {
                    System.out.println("Catastrophic error. Hashing problem lining up taxa.");
                    System.exit(0);
                }
                leaf.num = co.num;
                numToTaxonT2[leaf.num] = leaf;
            }

            T1.buildClusters(n);
            T2.buildClusters(n);

            //! --------------------------------------------
            //! Ok, now let's count the terminals...

            int terminals = 0;
            Vector termVec = new Vector();

            boolean isTerminal[] = new boolean[n];

            for (int x = 0; x < n; x++) {
                Tree u = numToTaxonT1[x].parent;
                Tree v = numToTaxonT2[x].parent;

                int intersect = 0;
                for (int y = 0; y < n; y++) {
                    if (u.cluster[y] && v.cluster[y]) intersect++;
                    if (intersect > 1) break;
                }
                if (intersect == 0) {
                    System.out.println("Catastrophic error, somehow a taxon is not in the intersection of its witness sets.");
                    System.exit(0);
                }
                if (intersect == 1) {
                    terminals++;
                    termVec.addElement(numToTaxonT1[x]);    //! records it's position in T1
                    isTerminal[x] = true;    //! let's us quickly determine if something is a terminal
                }

            }

            //! ----------------------------------------------------------------
            // Does the taxa to delete exist?
            Vector guessSet = taxa;
            Tree option = null;
            for (int i = 0; i < taxa.size(); ++i) {
                if (((Tree) taxa.elementAt(i)).name.equals(deletions[del])) {
                    option = (Tree) taxa.elementAt(i);
                }
            }

            if (option == null) {
                return false;
            }

            String zoekNaam = option.name;

            Tree alpha[] = new Tree [1];
            Tree beta[] = new Tree [1];


            Tree newGuyA = T1.copy(alpha,zoekNaam);
            Tree newGuyB = T2.copy(beta, zoekNaam);

            Tree killA = alpha[0].delete();
            if( killA != null )
            {
                if(VERBOSE) System.out.println("New root in T1...");
            }
            else killA = newGuyA;


            Tree killB = beta[0].delete();

            if( killB != null )
            {
                if(VERBOSE) System.out.println("New root in T2...");
            }
            else killB = newGuyB;

            if(VERBOSE)
            {
                killA.dump();
                System.out.println();
            }
            if(VERBOSE)
            {
                killB.dump();
                System.out.println();
            }

            T1 = killA;
            T2 = killB;

            ST = Tree.computeMaxSTsets(T1, T2);
            Tree.collapseMaxSTsets(T1, T2, ST);
        } // End for-loop

        if (ST.size() == 1) {
            return true;
        }

        return false;
    }


    public static String[] getTaxaFromString(String s)
    {
        String a = s.replace('{',' ');
        String b = a.replace('}',' ');

        StringBuffer clean = new StringBuffer();

        int at=0;
        boolean wasSpace = true;

        for(int x=0; x<b.length();x++)
        {
            if( b.charAt(x) == ' ')
            {
                if(!wasSpace) clean.append(' ');
                wasSpace = true;
            }
            else
            {
                clean.append(b.charAt(x));
                wasSpace = false;
            }
        }

        if(clean.charAt(clean.length()-1) == ' ') clean.deleteCharAt(clean.length()-1);
        String t = clean.toString();

        return t.split(" ");

    }

    public static long timeNow = 0;

    public TerminusEstSolution ComputeSolution(double runtime) {
        startTime = System.currentTimeMillis();
        setRuntime(runtime);

        return ComputeSolution();
    }

    public TerminusEstSolution ComputeSolution() {

        timeNow = System.currentTimeMillis();

        for( int l=0; l <= seenLeaves; l++ )
        {
            lowerBound = l;
            // System.out.println("Trying hybridization number: " + l);
            Tree T1 = t1.copy(null,null);
            Tree T2 = t2.copy(null,null);

            if (VERBOSE) System.out.println("// Trying r="+l);
            Network net = hybNumAtMost( T1, T2, l, t1, t2, 0 );
            if (useRuntime && canceled) {
                if (VERBOSE) System.out.println("Gone past runtime, canceled!");
                return new TerminusEstSolution(null, -1, runtime);
            }

            if( net != null )
            {
                //! get rid of the fake root

                if( net.root.children.size() != 1 )
                {
                    System.out.println("CATASTROPHIC ERROR, we lost the fake root...");
                    System.exit(0);
                }

                long timeEnd = System.currentTimeMillis();
                double seconds =  ((double)(timeEnd - timeNow))/1000.0;

                net.root = (Tree) net.root.children.elementAt(0);

                net.resetNetwork();

                //! ------------------- As a final check, check that the trees are actually displayed

                net.buildLeftRightClusters();

                boolean success = false;
                success = net.checkDisplay( t1, 0 );
                if(!success)
                {
                    System.out.println("CATASTROPHIC ERROR, first tree not displayed by the network.");
                    System.exit(0);
                }
                else if (VERBOSE) System.out.println("// First tree displayed by network!");

                success = net.checkDisplay( t2, 1 );
                if(!success)
                {
                    System.out.println("CATASTROPHIC ERROR, second tree not displayed by the network.");
                    System.exit(0);
                }
                else if (VERBOSE) System.out.println("// Second tree displayed by network!");


                return new TerminusEstSolution(net, l, seconds);
            }
        }
        return null;
    }

    public Network FixNetwork(Network net) {
        //! get rid of the fake root

        if( net.root.children.size() != 1 )
        {
            System.out.println("CATASTROPHIC ERROR, we lost the fake root...");
            System.exit(0);
        }

        net.root = (Tree) net.root.children.elementAt(0);

        net.resetNetwork();

        //! ------------------- As a final check, check that the trees are actually displayed

        net.buildLeftRightClusters();

        boolean success = false;
        success = net.checkDisplay( t1, 0 );
        if(!success)
        {
            System.out.println("CATASTROPHIC ERROR, first tree not displayed by the network.");
            System.exit(0);
        }
        else if (VERBOSE) System.out.println("// First tree displayed by network!");

        success = net.checkDisplay( t2, 1 );
        if(!success)
        {
            System.out.println("CATASTROPHIC ERROR, second tree not displayed by the network.");
            System.exit(0);
        }
        else if (VERBOSE) System.out.println("// Second tree displayed by network!");

        return net;
    }

    public static void DumpENewick(Network net) {
        net.root.dumpEnewick();
    }

    public static String GetENewick(Network net) {
        return net.root.getEnewick();
    }

    public TerminusEstSolution ComputePartialSolution(Tree t1Partial, Tree t2Partial, int depth, int target) {
            // System.out.println("Trying hybridization number: " + l);
            Tree T1 = t1Partial.copy(null,null);
            Tree T2 = t2Partial.copy(null,null);

            Network net = hybNumAtMost( T1, T2, target, t1, t2, depth );
            if( net != null )
            {
                // We do not remove the fake root when computing a partial solution.
                if( net.root.children.size() != 1 )
                {
                    System.out.println("CATASTROPHIC ERROR, we lost the fake root...");
                    System.exit(0);
                }

                long timeEnd = System.currentTimeMillis();
                double seconds =  ((double)(timeEnd - timeNow))/1000.0;

                return new TerminusEstSolution(net, target, seconds);
            }

        return null;
    }

    public void setRuntime(double seconds) {
        useRuntime = true;
        runtime = seconds;
    }

    /**
     * Retuurn time interval of (a-b) in seconds.
     * @param a Time in milliseconds
     * @param b Time in milliseconds.
     * @return
     */
    public static double getIntervalInSeconds(long a, long b) {
        return ((double) (a-b))/1000.0;
    }

    public boolean isCanceled() { return canceled; }

    private static class CompareByHeuristic implements Comparator<Tree> {
        private Hashtable<String, Double> h;

        CompareByHeuristic(Hashtable<String, Double> h) {
            this.h = h;
        }

        @Override
        public int compare(Tree o1, Tree o2) {
            if (getH(o1) > getH(o2)) return -1;
            if (getH(o1) < getH(o2)) return 1;

            return 0;
        }

        private double getH(Tree o) {
            String id = o.getName();
            if (h.containsKey(id)) {
                return h.get(id);
            }
            return 0;
        }
    }

    public static void main(String args[])
    {
        if( args.length == 0 || args[0].equals("-help") )
        {
            System.out.println("// This is TerminusEstV4, version 30th December 2019.");
            System.out.println("// Usage: java TerminusEstV4 [optional switches] treeFile.txt");
            System.out.println("// Optional switches:");
            System.out.println("// -nonetwork : only compute hybridization number, do not generate a network. (The default is to generate a network.)");
            System.out.println("// -nohash : do not use look-up table to store intermediate solutions. (The default is to use the hash table, which potentially uses an exponential amount of memory.");
            System.exit(0);
        }

        if( args.length != 0 )
        {
            for(int x=0; x<args.length; x++ )
            {
                if( args[x].equals("-nonetwork") )
                {
                    System.out.println("// -nonetwork switch seen: will only compute reticulation number, will not build a network.");
                    BUILDNETWORK = false;
                }
                else
                if( args[x].equals("-nohash") )
                {
                    System.out.println("// -nohash switch seen: will not use a look-up table to remember intermediate solutions.");
                    USEHASH = false;
                }
            }
        }

        System.out.print("// SETTING: ");
        if(BUILDNETWORK) System.out.println("A network WILL be constructed.");
        else System.out.println("A network will NOT be constructed.");

        System.out.print("// SETTING: ");
        if(USEHASH) System.out.println("Hash tables WILL be used.");
        else System.out.println("Hash tables will NOT be used.");

        System.out.println("// Begin TerminusEst.");
        System.out.println("// -------------------------------------");

        TerminusEstV4 te4 = null;
        if (args.length > 0){
            te4 = new TerminusEstV4(args[args.length-1]);

        }

        TerminusEstSolution solution = te4.ComputeSolution();

        Network net = solution.root;
        int l = solution.hyb;
        double seconds = solution.runtime;

        net.root.dumpNetwork();
        net.root.dumpNetworkTreeImage(0);
        net.root.dumpNetworkTreeImage(1);

        net.root.dumpEnewick();

        //! ------------------- As a final check, check that the trees are actually displayed

        net.buildLeftRightClusters();

        System.out.println("// -----------------------------");
        System.out.println("// HYBRIDIZATION NUMBER = "+l);
        System.out.println("// -----------------------------");
        System.out.println("// Real-time elapsed in seconds: "+seconds);

        //! Finished!
        System.exit(0);
    }


}
