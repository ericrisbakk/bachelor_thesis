package main.dataGeneration;

import java.io.*;
import java.util.*;

public class RandomTree
		{

		public static int n;
		public static long s;
		public static int p;
        
		public static Random r;
		
		public static void main( String[] args )
			{

			if( args.length !=3 )
				{
				System.out.println("Usage: n s p");
				System.out.println("n = number of taxa");
				System.out.println("s = random seed (use 0 if not specified");
				System.out.println("p = number between 1 and 100 representing left/right bias, use 50 if you don't care");
				System.exit(0);
				}

			n = Integer.parseInt(args[0]);
			s = Long.parseLong(args[1]);
			p = Integer.parseInt(args[2]);

			//! System.out.println("Will generate a random tree on "+n+" taxa.");
			
			if(s==0) s = System.currentTimeMillis();
			//! System.out.println("Will use this as seed for random number generator: "+s);

			//! System.out.println("Will use "+p+" as the left/right bias.");
			
			r = new Random(s);
			
			int taxa[] = new int[n];
			for(int x=0; x<taxa.length; x++ ) taxa[x] = x;
			
			generateRandomTree( taxa );
			System.out.println(";");
			}


		public static void generateRandomTree( int taxa[] )
			{
			if(taxa.length == 1)
				{
				System.out.print(taxa[0]+1);
				return;
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
					int sample = r.nextInt(100);
					if( sample <= p )
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
			System.out.print("(");
			generateRandomTree(leftTaxa);
			System.out.print(",");
			generateRandomTree(rightTaxa);
			System.out.print(")");
			return;	
			}



		}

