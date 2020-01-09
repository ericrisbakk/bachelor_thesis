package main.utility;

import main.mcts.NodeMCTS;

import java.util.Arrays;
import java.util.Comparator;

public abstract class Traversal {

    /**
     * Search through
     */
        SearchContext context;

        public abstract void Setup();

        public abstract void Process();

        public void StartDepthFirstTraversal(NodeMCTS n, Comparator<NodeMCTS> comp) {
            Setup();
            DepthFirstTraversal(n, comp);
            Process();
        }

        protected void DepthFirstTraversal(NodeMCTS n, Comparator<NodeMCTS> comp) {
            BeforeStop(n);
            if (StopCondition(n)) {
                OnStop(n);
                return;
            }

            BeforeSort(n);
            NodeMCTS[] children = n.children;
            if (comp != null) {
                Arrays.sort(children, comp);
            }

            BeforeRecursion(n);
            for (var c :
                    children) {
                DepthFirstTraversal(n, comp);
            }

            Final(n);
        }

        /**
         * Condition for telling whether the search should stop at this node.
         *
         * @return
         */
        public abstract boolean StopCondition(NodeMCTS n);

        public abstract void BeforeStop(NodeMCTS n);

        public abstract void OnStop(NodeMCTS n);

        public abstract void BeforeSort(NodeMCTS n);

        public abstract void BeforeRecursion(NodeMCTS n);

        public abstract void Final(NodeMCTS n);
}
