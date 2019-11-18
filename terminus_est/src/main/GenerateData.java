package main;

import java.util.Random;


public class GenerateData {

    public static Random rTreeGen = new Random(0);
    public static int pTreeGen = 50;
    public static int nTreeGen = 30;

    // PILFERED, from main.dataGeneration.RandomTree.java
    public static String GenerateTreeInstance(int[] taxa) {
        if(taxa.length == 1)
        {
            return "" + (taxa[0] + 1);
        }

        int goLeft = 0;
        int goRight = 0;

        boolean leftright[] = new boolean[ taxa.length ];

        while( (goLeft == 0) || (goRight==0) )
        {
            goLeft = 0;
            goRight = 0;

            for( int x=0; x<leftright.length; x++ )
            {
                int sample = rTreeGen.nextInt(100);
                if( sample <= pTreeGen )
                {
                    goLeft++;
                    leftright[x] = false;
                }
                else
                {
                    goRight++;
                    leftright[x] = true;
                }
            }
        }

        //!System.out.println("Got here.");
        int leftTaxa[] = new int[ goLeft ];
        int rightTaxa[] = new int[ goRight ];
        int atLeft = 0;
        int atRight = 0;
        //!System.out.println("Got here 2.");

        //!System.out.println(goLeft+" "+goRight);

        for(int x=0; x<taxa.length; x++)
        {
            //!System.out.println("At "+x);
            //!System.out.println("atLeft = "+atLeft);
            //!System.out.println("atRight = "+atRight);

            if( !leftright[x] )
            {
                leftTaxa[atLeft++] = taxa[x];
            }
            else rightTaxa[atRight++] = taxa[x];
        }

        return
                "("
                + GenerateTreeInstance(leftTaxa)
                + ","
                + GenerateTreeInstance(rightTaxa)
                + ")";
    }

    public static void main(String[] args) {
        int taxa[] = new int[nTreeGen];
        for(int x=0; x<taxa.length; x++ ) taxa[x] = x;

        System.out.println(GenerateTreeInstance(taxa));

        
    }
}
