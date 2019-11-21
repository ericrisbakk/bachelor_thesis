package main;

import main.TerminusEst.TerminusEstInputHandler;
import main.TerminusEst.TerminusEstSolution;
import main.TerminusEst.TerminusEstV4;
import main.graphs.InputHandler;
import main.utility.Tuple2;

import javax.xml.crypto.Data;
import java.io.File;

public class ExperimentRunner {

    public static String dataFolder = "D:/Uni/TreeGen/Data/";
    /**
     * Fetches files from the relevant folder. All files are assumed to follow a pattern of
     * n[X]r[Y]c[Z]_[Inst], where X is the taxa size, Y the number of rSPR moves, Z the percent of
     * contractions performed, and Inst an instance identifer.
     */
    public class DataFetcher {
        String folderPath;
        int[] n;
        int[] r;
        int[] c;

        String[] files;
        int file;

        public DataFetcher(String folderPath, int[] n, int[] r, int[] c) {
            this.folderPath = folderPath;
            this.n = n;
            this.r = r;
            this.c = c;

            files = new String[n.length*r.length*c.length*20];
            int index = 0;
            // Get all the trees!
            for (int in = 0; in < n.length; ++in) {
                for (int ir = 0; ir < r.length; ++ir) {
                    for (int ic = 0; ic < c.length; ++ic) {
                        for (int inst = 1; inst <= 20; ++inst) {
                            String name = GetFileName(in, ir, ic, inst);
                            File f = new File(name);
                            if (!f.exists()) {
                                System.out.println("ERROR: File '" + f.getName() + "' does not exist.");
                                System.exit(0);
                            }
                            else {
                                files[index] = name;
                                ++index;
                            }
                        }
                    }
                }
            }
        }

        public String GetFileName(int ni, int ri, int ci, int inst) {
            return folderPath + "n" + n[ni] + "r" + r[ri] + "c" + c[ci] + "_" + inst + ".txt";
        }

    }

    /**
     * Q: For simple data-sets (r <= 15), does TerminusEstMCTS always find a solution? And is it optimal?
     */
    public void RunExperimentA() {
        DataFetcher df = new DataFetcher(dataFolder, new int[]{20, 50, 100}, new int[]{10, 15}, new int[]{25, 50, 75});
        for (int i = 0; i < df.files.length; ++i) {
            TerminusEstSolution s_normal = (new TerminusEstV4(df.files[i])).ComputeSolution();
            TerminusEstSolution s_mcts = TerminusEstMCTS.AttemptSolution(df.files[i]);
            System.out.println("(Regular) - Hyb: " + s_normal.hyb + ", Runtime: " + s_normal.runtime
                + "\t(MCTS) - Hyb: " + s_mcts.hyb + ", Runtime: " + s_mcts.runtime);
        }
    }

    public static void main(String[] args) {
        ExperimentRunner er = new ExperimentRunner();
        er.RunExperimentA();
    }
}
