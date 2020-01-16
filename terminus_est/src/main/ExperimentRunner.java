package main;

import main.TerminusEst.TerminusEstSolution;
import main.TerminusEst.TerminusEstV4;
import main.utility.Tuple2;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ExperimentRunner {

    public static String dataFolder = "D:/Uni/TreeGen/Data/";
    public static String finalDataFolder = "D:/Uni/TreeGen/FinalData/";
    public static String dataSubset = "D:/Uni/DataInput/";

    /**
     * Fetches files from the relevant folder. All files are assumed to follow a pattern of
     * n[X]r[Y]c[Z]_[Inst], where X is the taxa size, Y the number of rSPR moves, Z the percent of
     * contractions performed, and Inst an instance identifer.
     */
    public class DataFetcher {
        String folderPath;
        int[] b;
        int[] n;
        int[] r;
        int[] c;
        int[] inst;

        int inst_lower, inst_upper;

        String[] files;
        int file;

        public DataFetcher(String file) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                ArrayList<String> f = new ArrayList<>();
                String dataPoint = br.readLine();
                while (dataPoint != null) {
                    f.add(dataPoint);
                    dataPoint = br.readLine();
                }

                files = f.toArray(new String[] {});

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public DataFetcher(int[] b, int[] n, int[] c, int inst_lower, int inst_upper) {
            this.folderPath = finalDataFolder;
            this.b = b;
            this.n = n;
            this.c = c;
            this.inst_lower = inst_lower;
            this.inst_upper = inst_upper;

            files = new String[b.length*n.length*c.length*(inst_upper - inst_lower + 1)];
            int index = 0;
            for (int ib = 0; ib < b.length; ++ib) {
                for (int in = 0; in < n.length; ++in) {
                    for (int ic = 0; ic < c.length; ++ic) {
                        for (int ii = inst_lower; ii <= inst_upper; ++ii) {
                            String name = GetFinalFileName(ib, in, ic, ii);
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

        public DataFetcher(String folderPath, int[] n, int[] r, int[] c, int[] inst) {
            this.folderPath = folderPath;
            this.n = n;
            this.r = r;
            this.c = c;
            this.inst = inst;

            files = new String[n.length*r.length*c.length*inst.length];
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

        public String GetFinalFileName(int bi, int ni, int ci, int ii) {
            return folderPath + "b" + b[bi] + "n" + n[ni] + "c" + c[ci] + "_" + ii + ".txt";
        }

        public String GetFileName(int ni, int ri, int ci, int ii) {
            return folderPath + "n" + n[ni] + "r" + r[ri] + "c" + c[ci] + "_" + inst[ii] + ".txt";
        }
    }

    public String[] getDataEasy() {
        DataFetcher df = new DataFetcher(dataSubset + "data_easy.txt");
        return df.files;
    }

    public String[] getDataMedium() {
        DataFetcher df = new DataFetcher(dataSubset + "data_medium.txt");
        return df.files;
    }

    public String[] getDataHard() {
        DataFetcher df = new DataFetcher(dataSubset + "data_hard.txt");
        return df.files;
    }


    public class DataWriter {
        FileWriter fr = null;
        BufferedWriter br = null;

        public DataWriter(String filename, String csvHeader) {
            File file = new File(filename);
            try {
                fr = new FileWriter(file);
                br = new BufferedWriter(fr);
                Write(csvHeader);
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

        public void WriteResult(String fileName, TerminusEstSolution s, int lowerBound) {
            String line = fileName + ", " + s.hyb + ", " + s.runtime + ", " + lowerBound + "\n";
            Write(line);
        }



        public void WriteResult(TerminusEstMCTS.ExperimentData d) {
            String line = d.GetData() + "\n";
            Write(line);
        }

        public void WriteResult(String fileName, String line) {
            String l = fileName + ", " + line;
            Write(l);
        }

        public void WriteResult(TerminusEstMCTS.TreeData d) {
            String line = d.GetData() + "\n";
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

        String hdr = "ID, REGULAR_HYB, REGULAR_RUNTIME, MCTS_HYB, MCTS_RUNTIME\n";

        DataFetcher df = new DataFetcher(dataFolder, new int[]{100}, new int[]{15}, new int[]{25, 50, 75}, instances);
        DataWriter dw = new DataWriter(outputFile, hdr);

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

    public void BasicTerminusEst(String[] files, String outputFile, int runtime, int startAt) {
        System.out.println("\nWriting to: " + outputFile + "\n");
        String hdr = "ID, BASIC_HYB, BASIC_RUNTIME, BASIC_LB\n";
        DataWriter dw = new DataWriter(outputFile, hdr);

        for (int i = startAt; i < files.length; ++i) {
            System.out.print("(" + (i+1) + "/" + files.length + ") "  +files[i] + ", ");
            TerminusEstV4 te4 = new TerminusEstV4(files[i]);
            TerminusEstSolution s = te4.ComputeSolution(runtime);
            if (s != null){
                System.out.println("Hyb: " + s.hyb + ", Runtime: " + s.runtime + ", Lower-bound: " + te4.lowerBound);
                dw.WriteResult(files[i], s, te4.lowerBound);
            }
            else {
                System.out.println("Didn't finish.");
                dw.WriteResult(files[i], "-1, " + runtime + ", " + te4.lowerBound);
            }
        }

        dw.Close();
    }

    public void ExperimentTerminusEstMCTS(int startAt, int runX) {
        String outputFile = GetFileName("TE_MCTS_" + startAt + "-" + (startAt + runX - 1));

        System.out.println("\nFile: " + outputFile + "\n");
//        int num = 6;
//        int[] instances = new int[num];
//        for (int i = 1; i <= num; ++i) {
//            instances[i-1] = i;
//        }

        String[] files = null;
        // files = new DataFetcher(dataFolder, new int[]{50, 100}, new int[]{15, 20}, new int[]{25, 50}, instances);
        // files df = new DataFetcher(new int[] {0, 1}, new int[]{50, 100}, new int[]{25, 50}, 41, 50);
        files = getDataHard();

        String hdr = TerminusEstMCTS.ExperimentData.hdr + "\n";
        System.out.println(hdr);
        DataWriter dw = new DataWriter(outputFile, hdr);

        for (int i = startAt; i < startAt + runX; ++i) {
            System.out.print("(" + (i+1) + "/" + files.length + ") "  +files[i] + ", ");
            TerminusEstMCTS.ExperimentData d = (new TerminusEstMCTS(
                    10000, 10, 0.1, 32.0, 15
            ))
                    .RunExperiment(files[i], 600);
            System.out.println(d.GetData());
            dw.WriteResult(d);
        }

        dw.Close();
    }

    public void ExperimentTreeBuild(String[] files, String outputFile, int startAt, int it, int sim, double param_c, double param_d, int trees) {
        System.out.println("\nWriting to: " + outputFile + "\n");
        String hdr = TerminusEstMCTS.TreeData.hdr + "\n";
        DataWriter dw = new DataWriter(outputFile, hdr);

        for (int i = startAt; i < files.length; ++i) {
            System.out.print("(" + (i+1) + "/" + files.length + ") "  +files[i] + ", ");
            TerminusEstMCTS tem = new TerminusEstMCTS(it, sim, param_c, param_d, trees);
            TerminusEstMCTS.TreeData d = tem.GetTreeData(files[i]);
            System.out.println(d.GetData());
            dw.WriteResult(d);
        }

        dw.Close();
    }

    public void TreeParamTuning() {
        int[] iterations = new int[] {10000, 100000, 500000};
        Tuple2<Double, Double>[] params = new Tuple2[] {
                new Tuple2(0.1, 32.0),
                new Tuple2(Math.sqrt(2), 1000.0),
                new Tuple2(5.0, 10000.0)
        };

        for (int i = 0; i < iterations.length; ++i) {
            for (int j = 0; j < params.length; ++j) {

                ExperimentTreeBuild(getDataMedium(), GetFileName(treeTE + "_medium_" + i + "_" + j), 0,
                        iterations[i], 10, params[j].item1, params[j].item2, 1);
                ExperimentTreeBuild(getDataHard(), GetFileName(treeTE + "_hard_" + i + "_" + j), 0,
                        iterations[i], 10, params[j].item1, params[j].item2, 1);
            }
        }
    }

    public void MetaSearch(int startAt, int stopAt, int step) {
        for (int i = startAt; i < stopAt; i += step) {
            ExperimentTreeBuild(getDataHard(), GetFileName(metaTE + "_hard_" + i), 0,
                    50000, 10, 1.0, 32.0, i);
        }
    }

    public static String outputFolder = "D:/Uni/DataOutput/";

    public static String basicTE = "basicTE";
    public static String treeTE = "treeTE";
    public static String metaTE = "metaTE_50k";

    public static String fType = ".csv";
    public static SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");

    public static String GetFileName(String base) {
        Date date = new Date(); // this object contains the current date value
        return outputFolder + base + "_" + format.format(date) + fType;
    }

    public static void main(String[] args) {
        ExperimentRunner er = new ExperimentRunner();
        // er.ExperimentTerminusEstMCTS();
        // er.BasicTerminusEst(er.getDataEasy(), GetFileName(basicTE + "_easy"),600);
        // er.BasicTerminusEst(er.getDataMedium(), GetFileName(basicTE + "_medium"),600, 36);
        // er.BasicTerminusEst(er.getDataHard(), GetFileName(basicTE + "_hard"),600, 31);
        er.ExperimentTerminusEstMCTS(0, 10);
        er.MetaSearch(21, 26, 4);
        er.ExperimentTerminusEstMCTS(10, 10);
        er.MetaSearch(3, 13, 4);
        er.ExperimentTerminusEstMCTS(20, 20);
        er.MetaSearch(15, 26, 4);
        er.ExperimentTerminusEstMCTS(40, 20);
        er.MetaSearch(27, 40, 2);
        er.ExperimentTerminusEstMCTS(60, 20);
        er.MetaSearch(27, 40, 2);
        er.ExperimentTerminusEstMCTS(80, 20);
        er.MetaSearch(41, 50, 2);
        er.ExperimentTerminusEstMCTS(100, 20);



    }
}
