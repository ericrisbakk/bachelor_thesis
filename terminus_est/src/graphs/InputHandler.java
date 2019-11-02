package graphs;
import java.io.*;

public class InputHandler {

    public static void parseTrees(String filename)
    {
        File file = new File(filename);

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String content = "";

            try {
                String line = br.readLine();
                do {
                    content += line;
                    line = br.readLine();
                } while (line != null);
            }
            catch(IOException e) {
                System.out.println("Trouble reading in file.");
                e.printStackTrace();
            }

            System.out.println("Trees received: " + content);
        }
        catch( FileNotFoundException e )
        {
            System.out.println("Parsing error!");
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void ParseInput(String trees) {
        String tree1 = trees.split(";")[0];
        String tree2 = trees.split(";")[1];

        Tree root1 = new Tree();
        Tree root2 = new Tree();
    }

    public static void main(String[] args) {
        parseTrees("D:\\Uni\\bachelor_thesis\\terminus_est\\Data\\simpleTree.txt");
    }
}
