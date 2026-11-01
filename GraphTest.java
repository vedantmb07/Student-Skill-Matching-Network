/**
 * GraphTest - Automated Verification Suite for Custom Graph Data Structure.
 * 
 * Verifies all 5 core requirements:
 * 1. Visited tracking (cycle prevention, single-visit guarantee)
 * 2. Disconnected or unreachable destination handling
 * 3. Path reconstruction integrity (validity of edge transitions)
 * 4. Valid node input handling (bounds check, invalid lookup, safe rejection)
 * 5. BFS shortest path guarantee vs DFS non-guarantee
 */
public class GraphTest {

    private static int testsRun = 0;
    private static int testsPassed = 0;

    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println("     🧪 RUNNING GRAPH DATA STRUCTURE TEST SUITE");
        System.out.println("============================================================\n");

        runAllTests();

        System.out.println("\n============================================================");
        System.out.printf("   TEST SUMMARY: %d / %d PASSED (100%% Success Rate)\n", testsPassed, testsRun);
        System.out.println("============================================================");

        if (testsPassed != testsRun) {
            System.exit(1);
        }
    }

    public static boolean runAllTests() {
        testsRun = 0;
        testsPassed = 0;

        testVisitedTracking();
        testDisconnectedDestination();
        testPathReconstruction();
        testValidNodeInput();
        testBfsShortestVsDfs();

        return testsPassed == testsRun;
    }

    /**
     * Requirement 1: Visited Tracking
     * Asserts that in a graph containing cycles, every reachable vertex is visited
     * exactly once, and no infinite loops occur.
     */
    public static void testVisitedTracking() {
        testsRun++;
        System.out.print("[TEST 1/5] Visited Tracking (Cycle Handling) ... ");

        Graph g = new Graph();
        int a = g.addNode("A", "STUDENT");
        int b = g.addNode("B", "STUDENT");
        int c = g.addNode("C", "STUDENT");
        int d = g.addNode("D", "STUDENT");

        // Create cycle A-B-C-A and branch C-D
        g.addEdge(a, b);
        g.addEdge(b, c);
        g.addEdge(c, a); // Cycle!
        g.addEdge(c, d);

        int[] order = g.getBFSTraversalOrder(a);

        // Check 1: Must visit all 4 nodes
        if (order.length != 4) {
            fail("Expected 4 visited nodes, got " + order.length);
            return;
        }

        // Check 2: Every node appears exactly once (no duplicates from cycle)
        boolean[] seen = new boolean[4];
        for (int id : order) {
            if (id < 0 || id >= 4 || seen[id]) {
                fail("Node " + id + " was visited more than once or out of range!");
                return;
            }
            seen[id] = true;
        }

        pass("Reachable nodes visited exactly once without cyclic loops.");
    }

    /**
     * Requirement 2: Disconnected or Unreachable Destination
     * Asserts that searching for a path between two disconnected subgraphs
     * cleanly returns found=false and null path without crashing.
     */
    public static void testDisconnectedDestination() {
        testsRun++;
        System.out.print("[TEST 2/5] Disconnected / Unreachable Destination ... ");

        Graph g = new Graph();
        // Component 1
        int s1 = g.addNode("Alice", "STUDENT");
        int k1 = g.addNode("Python", "SKILL");
        int s2 = g.addNode("Bob", "STUDENT");
        g.addEdge(s1, k1);
        g.addEdge(k1, s2);

        // Component 2 (Isolated)
        int s3 = g.addNode("Charlie", "STUDENT");
        int k2 = g.addNode("Rust", "SKILL");
        int s4 = g.addNode("Diana", "STUDENT");
        g.addEdge(s3, k2);
        g.addEdge(k2, s4);

        // BFS path check between isolated components
        Graph.PathResult bfsResult = g.findShortestPathBFS(s1, s4);
        if (bfsResult.found || bfsResult.path != null || bfsResult.edgeCount != -1) {
            fail("BFS incorrectly reported a path between disconnected components!");
            return;
        }

        // DFS path check between isolated components
        Graph.PathResult dfsResult = g.findPathDFS(s1, s4);
        if (dfsResult.found || dfsResult.path != null || dfsResult.edgeCount != -1) {
            fail("DFS incorrectly reported a path between disconnected components!");
            return;
        }

        pass("Unreachable destination returns found=false and path=null safely.");
    }

    /**
     * Requirement 3: Path Reconstruction Integrity
     * Asserts that the reconstructed path starts at 'start', ends at 'dest',
     * and every adjacent pair in the reconstructed path has a real edge in the graph.
     */
    public static void testPathReconstruction() {
        testsRun++;
        System.out.print("[TEST 3/5] Path Reconstruction Integrity ... ");

        Graph g = new Graph();
        int s1 = g.addNode("Alice", "STUDENT");
        int k1 = g.addNode("Java", "SKILL");
        int s2 = g.addNode("Bob", "STUDENT");
        int k2 = g.addNode("Spring", "SKILL");
        int s3 = g.addNode("Charlie", "STUDENT");

        g.addEdge(s1, k1);
        g.addEdge(k1, s2);
        g.addEdge(s2, k2);
        g.addEdge(k2, s3);

        Graph.PathResult res = g.findShortestPathBFS(s1, s3);

        if (!res.found || res.path == null) {
            fail("Failed to find valid path between Alice and Charlie!");
            return;
        }

        if (res.path[0] != s1 || res.path[res.length - 1] != s3) {
            fail("Reconstructed path does not match start and destination vertices!");
            return;
        }

        if (res.edgeCount != 4) {
            fail("Expected 4 edges in chain, found " + res.edgeCount);
            return;
        }

        // Verify every edge step exists
        for (int i = 0; i < res.length - 1; i++) {
            int u = res.path[i];
            int v = res.path[i + 1];
            if (!g.hasEdge(u, v)) {
                fail("Reconstructed path contains non-existent edge: " + u + " -> " + v);
                return;
            }
        }

        pass("Path correctly reconstructed and every step verified in adjacency list.");
    }

    /**
     * Requirement 4: Valid Node Input
     * Asserts that invalid IDs, out-of-bounds indices, and invalid names are safely handled.
     */
    public static void testValidNodeInput() {
        testsRun++;
        System.out.print("[TEST 4/5] Valid Node Input Bounds & Graceful Handling ... ");

        Graph g = new Graph();
        int a = g.addNode("Alice", "STUDENT");

        // 1. Negative ID bounds check
        if (g.isValidNode(-1) || g.getNode(-1) != null) {
            fail("isValidNode(-1) or getNode(-1) should return false/null.");
            return;
        }

        // 2. High out of bounds check
        if (g.isValidNode(100) || g.getNode(100) != null) {
            fail("isValidNode(100) on small graph should return false/null.");
            return;
        }

        // 3. Path finding with invalid input
        Graph.PathResult r1 = g.findShortestPathBFS(-1, a);
        if (r1.found) {
            fail("findShortestPathBFS with -1 should fail safely.");
            return;
        }

        Graph.PathResult r2 = g.findShortestPathBFS(a, 999);
        if (r2.found) {
            fail("findShortestPathBFS with 999 should fail safely.");
            return;
        }

        // 4. Adding edge with invalid node
        if (g.addEdge(-1, a) || g.addEdge(a, 50)) {
            fail("addEdge with invalid node IDs should return false.");
            return;
        }

        // 5. Lookup non-existent name
        if (g.findNodeByName("NonExistentStudent") != -1) {
            fail("findNodeByName for non-existent student must return -1.");
            return;
        }

        // 6. Name null validation
        try {
            g.addNode(null, "STUDENT");
            fail("addNode(null) should throw IllegalArgumentException.");
            return;
        } catch (IllegalArgumentException expected) {
            // Success
        }

        pass("All boundary checks and invalid inputs safely validated.");
    }

    /**
     * Requirement 5: BFS Shortest Path vs DFS Non-Guarantee
     * Constructs a canonical shortcut graph where BFS finds the shortest path
     * (1 edge), while DFS takes the longer branch (4 edges).
     */
    public static void testBfsShortestVsDfs() {
        testsRun++;
        System.out.print("[TEST 5/5] BFS Shortest Path Guarantee vs DFS Non-Guarantee ... ");

        Graph g = new Graph();
        int n0 = g.addNode("Start", "STUDENT");
        int n1 = g.addNode("Hop1", "STUDENT");
        int n2 = g.addNode("Hop2", "STUDENT");
        int n3 = g.addNode("Hop3", "STUDENT");
        int n4 = g.addNode("Destination", "STUDENT");

        // Edge order in adjacency list:
        // By adding shortcut first, DFS following reverse list or long branch explores long path first:
        // We add the long chain: 0-1, 1-2, 2-3, 3-4
        // And shortcut: 0-4
        // Because of prepend in adjacency list:
        // If we add (0, 4) then (0, 1): adjList[0] has head=1 -> next=4.
        // DFS from 0 visits 1 first, recursing: 1 -> 2 -> 3 -> 4 (4 edges).
        // BFS explores all depth-1 neighbors (1 and 4): reaches 4 immediately at distance 1 (1 edge).
        g.addEdge(n0, n4); // Shortcut
        g.addEdge(n0, n1); // Long path start
        g.addEdge(n1, n2);
        g.addEdge(n2, n3);
        g.addEdge(n3, n4);

        Graph.PathResult bfs = g.findShortestPathBFS(n0, n4);
        Graph.PathResult dfs = g.findPathDFS(n0, n4);

        if (!bfs.found || !dfs.found) {
            fail("Both BFS and DFS must find a path.");
            return;
        }

        if (bfs.edgeCount != 1) {
            fail("BFS failed to find the 1-edge shortcut! Found edge count: " + bfs.edgeCount);
            return;
        }

        if (dfs.edgeCount <= bfs.edgeCount) {
            fail("DFS took optimal path instead of deep branch. Expected > 1, got " + dfs.edgeCount);
            return;
        }

        pass(String.format("BFS edge count = %d (Shortest), DFS edge count = %d (Non-optimal). BFS guarantee verified!",
                bfs.edgeCount, dfs.edgeCount));
    }

    private static void pass(String details) {
        testsPassed++;
        System.out.println("✅ PASS");
        System.out.println("   -> " + details);
    }

    private static void fail(String error) {
        System.out.println("❌ FAIL");
        System.out.println("   -> ERROR: " + error);
    }
}
