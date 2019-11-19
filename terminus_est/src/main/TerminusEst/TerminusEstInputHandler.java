package main.TerminusEst;

public class TerminusEstInputHandler {

    public static void main(String[] args) {
        System.out.println("Input received: ");
        for (int i = 0; i < args.length; ++i) {
            System.out.println(i + " - " + args[i]);
        }
    }
}
