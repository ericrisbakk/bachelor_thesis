package main;

import main.TerminusEst.TerminusEstSolution;
import main.TerminusEst.TerminusEstV4;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
        int[] inst;

        String[] files;
        int file;

        public DataFetcher(String folderPath, int[] n, int[] r, int[] c, int[] inst) {
            this.folderPath = folderPath;
            this.n = n;
            this.r = r;
            this.c = c;
            this.inst = inst;

            files = new String[n.length*r.length*c.length*20];
            int index = 0;
            // Get all the trees!
            for (int in = 0; in < n.length; ++in) {
                for (int ir = 0; ir < r.length; ++ir) {
                    for (int ic = 0; ic < c.length; ++ic) {
                        for (int ii = 0; ii < inst.length; ++ii) {
                            String name = GetFileName(in, ir, ic, ii);
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

        public String GetFileName(int ni, int ri, int ci, int ii) {
            return folderPath + "n" + n[ni] + "r" + r[ri] + "c" + c[ci] + "_" + inst[ii] + ".txt";
        }
    }

    public class DataWriter {
        FileWriter fr = null;
        BufferedWriter br = null;

        public DataWriter(String filename) {
            File file = new File(filename);
            try {
                fr = new FileWriter(file);
                br = new BufferedWriter(fr);
                Write("ID, REGULAR_HYB, REGULAR_RUNTIME, MCTS_HYB, MCTS_RUNTIME\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void Write(String s) {
            try {
                br.write(s);
                br.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void WriteResult (String fileName, TerminusEstSolution regular, TerminusEstSolution mcts){
            Write(fileName + ", " + regular.hyb + ", " + regular.runtime + ", " + mcts.hyb + ", " + mcts.runtime + "\n");
        }

        public void WriteResult(String fileName, TerminusEstSolution s) {
            String line = fileName + ", " + s.hyb + ", " + s.runtime + "\n";
            Write(line);
        }

        public void Close () {
            try {
                br.close();
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Q: For simple data-sets (r <= 15), does TerminusEstMCTS always find a solution? And is it optimal?
     */
    public void RunExperimentA() {
        String outputFile = "D:/Uni/DataOutput/BasicMCTSResultsN100.csv";

        int[] instances = new int[20];
        for (int i = 1; i <= 20; ++i) {
            instances[i-1] = i;
        }

        DataFetcher df = new DataFetcher(dataFolder, new int[]{100}, new int[]{15}, new int[]{25, 50, 75}, instances);
        DataWriter dw = new DataWriter(outputFile);

        for (int i = 0; i < df.files.length; ++i) {
            System.out.print(df.files[i]);
            TerminusEstSolution s_normal = (new TerminusEstV4(df.files[i])).ComputeSolution();
            System.out.print("\t" + s_normal.hyb + ", " + s_normal.runtime);
            TerminusEstSolution s_mcts = TerminusEstMCTS.AttemptSolution(df.files[i]);
            System.out.println("\t" + s_mcts.hyb + ", " + s_mcts.runtime);
            dw.WriteResult(df.files[i], s_normal, s_mcts);
        }

        dw.Close();
    }

    public void ExperimentBasicTerminusEst() {
        String outputFile = "D:/Uni/DataOutput/BasicTerminusEst.csv";

        int num = 6;
        int[] instances = new int[num];
        for (int i = 1; i <= num; ++i) {
            instances[i-1] = i;
        }

        DataFetcher df = new DataFetcher(dataFolder, new int[]{50, 100}, new int[]{15, 20}, new int[]{25, 50}, instances);
        DataWriter dw = new DataWriter(outputFile);

        for (int i = 0; i < df.files.length; ++i) {
            System.out.print(df.files[i]);
            TerminusEstSolution s = (new TerminusEstV4(df.files[i])).ComputeSolution(600);
            dw.WriteResult(df.files[i], s);
        }

        dw.Close();
    }

    public static void main(String[] args) {
        ExperimentRunner er = new ExperimentRunner();
        er.ExperimentBasicTerminusEst();
    }
}
