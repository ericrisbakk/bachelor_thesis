package dataGeneration;/* Generated By:JavaCC: Do not edit this line. TreeContract.java */
//! Be very careful with leaf indexing...because we're doing multiple trees!! BE VERY CAREFUL!!!
//! This is based on the Newick parser found at http://olduvai.sourceforge.net/tj/tj-javadoc-public/Parser/Newick.html, thanks to the author!

import java.util.*;

class TreeNode
        {
        TreeNode parent;
        Vector children;
        String name;
        int num;


        public TreeNode()
                {
                parent = null;
                children = new Vector();
                name = null;
                num = -1;
                }

        public void addChild( TreeNode c )
                {
                children.add(c);
                }

        public TreeNode getParent()
                {
                return parent;
                }

        public void setParent( TreeNode p )
                {
                this.parent = p;
                }

        public void setName( String s )
                {
                //! System.out.println(this + " has name set to "+s);
                name = s;
                }

        public String getName()
                {
                //! System.out.println(this + " returns name "+name);
                return name;
                }

        public void setNumber(int n)
                {
                num = n;
                }

        public Vector getChildren()
                {
                return children;
                }

        public void dump()
                {
                this.dumpInternal();
                System.out.println(";");
                }

        private void dumpInternal()
                {
                if( this.children.size() == 0 )
                        {
                        System.out.print(this.getName());
                        }
                else
                        {
                        System.out.print("(");
                        for(int c=0; c<children.size(); c++)
                                {
                                if( c != 0 ) System.out.print(",");
                                TreeNode tn = (TreeNode) children.elementAt(c);
                                tn.dumpInternal();
                                }
                        System.out.print(")");
                        }

                }

        public Vector getInternalEdges()
                {
                Vector v = new Vector();
                this.harvestInternalEdges(v);
                return(v);
                }

        private void harvestInternalEdges(Vector v)
                {
                if( (this.parent != null) && (this.children.size() > 0) )
                        {
                        v.addElement(this);
                        }
                for(int c=0; c<children.size(); c++)
                        {
                        TreeNode tn = (TreeNode) children.elementAt(c);
                        tn.harvestInternalEdges(v);
                        }
                }

        //! Contracts the edge feeding into this node
        public void contractEdge()
                {
                TreeNode p = this.parent;
                p.children.removeElement(this);
                for(int c=0; c<children.size(); c++)
                        {
                        TreeNode tn = (TreeNode) children.elementAt(c);
                        tn.parent = p;
                        p.addChild(tn);
                        }

                }

        }



//! ------------------------------------------

public class TreeContract implements TreeContractConstants {

        public static Vector intNodesT1 = new Vector();
        public static Vector intNodesT2 = new Vector();

        public static int numIntNodes = 0;

        public static Hashtable nameToNum;
        public static Hashtable numToName;

        public static int INFINITY = 100000;

        public static int seenLeaves = 0;

        public static TreeNode t1;

        //! Switch this to true to have verbose output
        public static final boolean DEBUG = false;

        public static int getLeafNumber( String leaf )
                {
                if( nameToNum == null ) nameToNum = new Hashtable();

                Integer i = (Integer) nameToNum.get( leaf );

                if( i != null )
                        {
                        return i.intValue();
                        }

                seenLeaves++;

                i = new Integer(seenLeaves);

                if(DEBUG) System.out.println("// Leaf '"+leaf+"' gets internal number "+seenLeaves);

                nameToNum.put( leaf, i );

                if( numToName == null )
                        {
                        numToName = new Hashtable();
                        }

                numToName.put( i, leaf );

                return seenLeaves;
                }


        public static String getLeafName( int num )
                {
                Integer i = new Integer(num);

                if( numToName == null )
                        {
                        numToName = new Hashtable();
                        }

                String s = (String) numToName.get( i );

                return s;
                }




  public static TreeNode tree;

  public static TreeNode current_node;

  private static TreeNode tn;

  public static int leaves = 0;
  public static int trees = 0;

  public static void parseTrees()
        {
        TreeContract n = new TreeContract(System.in);

        try{ n.Input(); }
        catch( ParseException e )
                {
                System.out.println("Parsing error!");
                e.printStackTrace();
                System.exit(0);
                }
        //! TreeContract.t1.dump();
        }

public static String VERSION = "TreeContract.jj Version 1.0";

  public static void main(String args[])
        {
        if( args.length < 2 )
                {
                System.out.println("Usage: java TreeContract [percent] [seed] < treeFile");
                System.out.println("Where 'percent' is the percentage of internal edges that should be contracted;");
                System.out.println("Where 'seed' is an integer used to seed the random number generator: specify 0 to seed from the system clock.");
                System.exit(0);
                }

        int percent = Integer.parseInt(args[0]);
        long seed = Long.parseLong(args[1]);

        if(percent > 100)
                {
                System.out.println("First argument must be an integer between 0 and 100 inclusive.");
                System.exit(0);
                }

        if(DEBUG) System.out.println("// Will contract "+percent+"% of the internal edges randomly.");

        if(seed==0)
                {
                seed = System.currentTimeMillis();
                if(DEBUG) System.out.println("// Using system clock to seed random number generator.");
                }
        else
                {
                if(DEBUG) System.out.println("// Using value "+seed+" to seed random number generator.");
                }

                Random r = new Random(seed);

        parseTrees();

        //! The tree will be in t1

        Vector v = t1.getInternalEdges();
        if(DEBUG) System.out.println("// " + v.size() + " internal edges found.");

        int todo = (int) ((v.size() / 100.0) * percent);

        if(DEBUG) System.out.println("// Will contract "+todo+" internal edges randomly.");

        while( todo != 0 )
                {
                Vector l = t1.getInternalEdges();

                int sample = r.nextInt( l.size() );
                TreeNode contractMe = (TreeNode) l.elementAt(sample);

                contractMe.contractEdge();

                todo--;

                if(DEBUG)
                        {
                        System.out.print("// ");
                        t1.dump();
                        }
                }

        if(DEBUG) System.out.println("// Final tree:");
        t1.dump();

        }

  static final public void Input() throws ParseException {
  String s;
  double len;
    label_1:
    while (true) {
      if (jj_2_1(2)) {
        ;
      } else {
        break label_1;
      }
    tree = new TreeNode(); current_node = tree; leaves = 0;
      descendant_list();
      if (jj_2_2(2)) {
        s = label();

      } else {
        ;
      }
      if (jj_2_3(2)) {
        jj_consume_token(7);
        len = branch_length();
      } else {
        ;
      }
      jj_consume_token(8);
        trees++;

        //! System.out.println("// Processing tree "+trees+"...");

        if( trees > 1 )
                {
                System.out.println("Only one tree allowed in the input! Stopping.");
                System.exit(0);
                }

        if(trees==1)
                {
                TreeContract.t1 = tree;
                }
    }
    jj_consume_token(0);
        int taxa = TreeContract.seenLeaves;
  }

  static final public void descendant_list() throws ParseException {
  int children = 0;
    jj_consume_token(9);
        children++;
        tn = new TreeNode();
        tn.setParent(current_node);
        current_node.addChild(tn);
        current_node = tn;
    subtree();
    label_2:
    while (true) {
      if (jj_2_4(2)) {
        ;
      } else {
        break label_2;
      }
      jj_consume_token(10);
          children++;
          tn = new TreeNode();
          tn.setParent(current_node);
          current_node.addChild(tn);
          current_node = tn;
      subtree();
    }
    jj_consume_token(11);
  }

/** function subtree will set name, length and weight for each tree node */
  static final public void subtree() throws ParseException {
  String s;
  double len;
    if (jj_2_9(2)) {
      descendant_list();

      if (jj_2_5(2)) {
        s = label();

      } else {
        ;
      }
      if (jj_2_6(2)) {
        jj_consume_token(7);
        len = branch_length();

      } else {
        ;
      }
         current_node = current_node.getParent();
    } else {
      if (jj_2_7(2)) {
        s = label();
                  leaves++;
                int x = getLeafNumber(s);
                current_node.setName(s);
                //! System.out.println(current_node.getName());
                current_node.setNumber(x);
      } else {
        ;
      }
      if (jj_2_8(2)) {
        jj_consume_token(7);
        len = branch_length();

      } else {
        ;
      }
  current_node = current_node.parent;
    }
  }

  static final public String label() throws ParseException {
  String s;
    if (jj_2_10(2)) {
      s = unquoted_label();
                         {if (true) return s;}
    } else if (jj_2_11(2)) {
      s = quoted_label();
                       {if (true) return s;}
    } else {
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

/** for each unquoted label, we need to replace '_' by ' ' */
  static final public String unquoted_label() throws ParseException {
  Token t;
    if (jj_2_12(2)) {
      t = jj_consume_token(unquoted_string);
                          String s = new String(t.toString());
                                {if (true) return s;}
                          // return s.replace('_', ' ');

    } else if (jj_2_13(2)) {
      t = jj_consume_token(double_number);
                        {if (true) return new String(t.toString());}
    } else {
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

/** for each quoted label, we remove double quotes from the string */
  static final public String quoted_label() throws ParseException {
  Token t;
    t = jj_consume_token(quoted_string);
                        String s = new String(t.toString());
                        {if (true) return s.substring(1, s.length()-1);}
    throw new Error("Missing return statement in function");
  }

  static final public double branch_length() throws ParseException {
  Token t;
    t = jj_consume_token(double_number);
                        {if (true) return Double.parseDouble(t.toString());}
    throw new Error("Missing return statement in function");
  }

  static private boolean jj_2_1(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_1(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(0, xla); }
  }

  static private boolean jj_2_2(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_2(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(1, xla); }
  }

  static private boolean jj_2_3(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_3(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(2, xla); }
  }

  static private boolean jj_2_4(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_4(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(3, xla); }
  }

  static private boolean jj_2_5(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_5(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(4, xla); }
  }

  static private boolean jj_2_6(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_6(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(5, xla); }
  }

  static private boolean jj_2_7(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_7(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(6, xla); }
  }

  static private boolean jj_2_8(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_8(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(7, xla); }
  }

  static private boolean jj_2_9(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_9(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(8, xla); }
  }

  static private boolean jj_2_10(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_10(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(9, xla); }
  }

  static private boolean jj_2_11(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_11(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(10, xla); }
  }

  static private boolean jj_2_12(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_12(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(11, xla); }
  }

  static private boolean jj_2_13(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_13(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(12, xla); }
  }

  static private boolean jj_3_13() {
    if (jj_scan_token(double_number)) return true;
    return false;
  }

  static private boolean jj_3_3() {
    if (jj_scan_token(7)) return true;
    if (jj_3R_5()) return true;
    return false;
  }

  static private boolean jj_3_2() {
    if (jj_3R_4()) return true;
    return false;
  }

  static private boolean jj_3_4() {
    if (jj_scan_token(10)) return true;
    if (jj_3R_6()) return true;
    return false;
  }

  static private boolean jj_3R_7() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3_12()) {
    jj_scanpos = xsp;
    if (jj_3_13()) return true;
    }
    return false;
  }

  static private boolean jj_3_12() {
    if (jj_scan_token(unquoted_string)) return true;
    return false;
  }

  static private boolean jj_3_1() {
    if (jj_3R_3()) return true;
    return false;
  }

  static private boolean jj_3_11() {
    if (jj_3R_8()) return true;
    return false;
  }

  static private boolean jj_3R_3() {
    if (jj_scan_token(9)) return true;
    if (jj_3R_6()) return true;
    Token xsp;
    while (true) {
      xsp = jj_scanpos;
      if (jj_3_4()) { jj_scanpos = xsp; break; }
    }
    if (jj_scan_token(11)) return true;
    return false;
  }

  static private boolean jj_3_10() {
    if (jj_3R_7()) return true;
    return false;
  }

  static private boolean jj_3R_4() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3_10()) {
    jj_scanpos = xsp;
    if (jj_3_11()) return true;
    }
    return false;
  }

  static private boolean jj_3_8() {
    if (jj_scan_token(7)) return true;
    if (jj_3R_5()) return true;
    return false;
  }

  static private boolean jj_3_7() {
    if (jj_3R_4()) return true;
    return false;
  }

  static private boolean jj_3R_9() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3_7()) jj_scanpos = xsp;
    xsp = jj_scanpos;
    if (jj_3_8()) jj_scanpos = xsp;
    return false;
  }

  static private boolean jj_3_6() {
    if (jj_scan_token(7)) return true;
    if (jj_3R_5()) return true;
    return false;
  }

  static private boolean jj_3_5() {
    if (jj_3R_4()) return true;
    return false;
  }

  static private boolean jj_3_9() {
    if (jj_3R_3()) return true;
    return false;
  }

  static private boolean jj_3R_6() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3_9()) {
    jj_scanpos = xsp;
    if (jj_3R_9()) return true;
    }
    return false;
  }

  static private boolean jj_3R_5() {
    if (jj_scan_token(double_number)) return true;
    return false;
  }

  static private boolean jj_3R_8() {
    if (jj_scan_token(quoted_string)) return true;
    return false;
  }

  static private boolean jj_initialized_once = false;
  /** Generated Token Manager. */
  static public TreeContractTokenManager token_source;
  static SimpleCharStream jj_input_stream;
  /** Current token. */
  static public Token token;
  /** Next token. */
  static public Token jj_nt;
  static private int jj_ntk;
  static private Token jj_scanpos, jj_lastpos;
  static private int jj_la;
  static private int jj_gen;
  static final private int[] jj_la1 = new int[0];
  static private int[] jj_la1_0;
  static {
      jj_la1_init_0();
   }
   private static void jj_la1_init_0() {
      jj_la1_0 = new int[] {};
   }
  static final private JJCalls[] jj_2_rtns = new JJCalls[13];
  static private boolean jj_rescan = false;
  static private int jj_gc = 0;

  /** Constructor with InputStream. */
  public TreeContract(java.io.InputStream stream) {
     this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public TreeContract(java.io.InputStream stream, String encoding) {
    if (jj_initialized_once) {
      System.out.println("ERROR: Second call to constructor of static parser.  ");
      System.out.println("       You must either use ReInit() or set the JavaCC option STATIC to false");
      System.out.println("       during parser generation.");
      throw new Error();
    }
    jj_initialized_once = true;
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new TreeContractTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 0; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  static public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  /** Reinitialise. */
  static public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 0; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Constructor. */
  public TreeContract(java.io.Reader stream) {
    if (jj_initialized_once) {
      System.out.println("ERROR: Second call to constructor of static parser. ");
      System.out.println("       You must either use ReInit() or set the JavaCC option STATIC to false");
      System.out.println("       during parser generation.");
      throw new Error();
    }
    jj_initialized_once = true;
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new TreeContractTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 0; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  static public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 0; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Constructor with generated Token Manager. */
  public TreeContract(TreeContractTokenManager tm) {
    if (jj_initialized_once) {
      System.out.println("ERROR: Second call to constructor of static parser. ");
      System.out.println("       You must either use ReInit() or set the JavaCC option STATIC to false");
      System.out.println("       during parser generation.");
      throw new Error();
    }
    jj_initialized_once = true;
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 0; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(TreeContractTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 0; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  static private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      if (++jj_gc > 100) {
        jj_gc = 0;
        for (int i = 0; i < jj_2_rtns.length; i++) {
          JJCalls c = jj_2_rtns[i];
          while (c != null) {
            if (c.gen < jj_gen) c.first = null;
            c = c.next;
          }
        }
      }
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  static private final class LookaheadSuccess extends Error { }
  static final private LookaheadSuccess jj_ls = new LookaheadSuccess();
  static private boolean jj_scan_token(int kind) {
    if (jj_scanpos == jj_lastpos) {
      jj_la--;
      if (jj_scanpos.next == null) {
        jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();
      } else {
        jj_lastpos = jj_scanpos = jj_scanpos.next;
      }
    } else {
      jj_scanpos = jj_scanpos.next;
    }
    if (jj_rescan) {
      int i = 0; Token tok = token;
      while (tok != null && tok != jj_scanpos) { i++; tok = tok.next; }
      if (tok != null) jj_add_error_token(kind, i);
    }
    if (jj_scanpos.kind != kind) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) throw jj_ls;
    return false;
  }


/** Get the next Token. */
  static final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

/** Get the specific Token. */
  static final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  static private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  static private List<int[]> jj_expentries = new ArrayList<int[]>();
  static private int[] jj_expentry;
  static private int jj_kind = -1;
  static private int[] jj_lasttokens = new int[100];
  static private int jj_endpos;

  static private void jj_add_error_token(int kind, int pos) {
    if (pos >= 100) return;
    if (pos == jj_endpos + 1) {
      jj_lasttokens[jj_endpos++] = kind;
    } else if (jj_endpos != 0) {
      jj_expentry = new int[jj_endpos];
      for (int i = 0; i < jj_endpos; i++) {
        jj_expentry[i] = jj_lasttokens[i];
      }
      boolean exists = false;
      for (Iterator<?> it = jj_expentries.iterator(); it.hasNext();) {
        exists = true;
        int[] oldentry = (int[])(it.next());
        if (oldentry.length == jj_expentry.length) {
          for (int i = 0; i < jj_expentry.length; i++) {
            if (oldentry[i] != jj_expentry[i]) {
              exists = false;
              break;
            }
          }
          if (exists) break;
        }
      }
      if (!exists) jj_expentries.add(jj_expentry);
      if (pos != 0) jj_lasttokens[(jj_endpos = pos) - 1] = kind;
    }
  }

  /** Generate ParseException. */
  static public ParseException generateParseException() {
    jj_expentries.clear();
    boolean[] la1tokens = new boolean[25];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 0; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 25; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.add(jj_expentry);
      }
    }
    jj_endpos = 0;
    jj_rescan_token();
    jj_add_error_token(0, 0);
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = jj_expentries.get(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  /** Enable tracing. */
  static final public void enable_tracing() {
  }

  /** Disable tracing. */
  static final public void disable_tracing() {
  }

  static private void jj_rescan_token() {
    jj_rescan = true;
    for (int i = 0; i < 13; i++) {
    try {
      JJCalls p = jj_2_rtns[i];
      do {
        if (p.gen > jj_gen) {
          jj_la = p.arg; jj_lastpos = jj_scanpos = p.first;
          switch (i) {
            case 0: jj_3_1(); break;
            case 1: jj_3_2(); break;
            case 2: jj_3_3(); break;
            case 3: jj_3_4(); break;
            case 4: jj_3_5(); break;
            case 5: jj_3_6(); break;
            case 6: jj_3_7(); break;
            case 7: jj_3_8(); break;
            case 8: jj_3_9(); break;
            case 9: jj_3_10(); break;
            case 10: jj_3_11(); break;
            case 11: jj_3_12(); break;
            case 12: jj_3_13(); break;
          }
        }
        p = p.next;
      } while (p != null);
      } catch(LookaheadSuccess ls) { }
    }
    jj_rescan = false;
  }

  static private void jj_save(int index, int xla) {
    JJCalls p = jj_2_rtns[index];
    while (p.gen > jj_gen) {
      if (p.next == null) { p = p.next = new JJCalls(); break; }
      p = p.next;
    }
    p.gen = jj_gen + xla - jj_la; p.first = token; p.arg = xla;
  }

  static final class JJCalls {
    int gen;
    Token first;
    int arg;
    JJCalls next;
  }

}