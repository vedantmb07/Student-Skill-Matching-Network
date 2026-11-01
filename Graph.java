/**
 * Custom Graph Data Structure for Student Skill-Matching Network.
 * 
 * NOTE ON DATA STRUCTURE COMPLIANCE:
 * All principal data structures (Graph Adjacency List, Circular Queue, 
 * Array-based Stack, and Nodes) are implemented strictly from scratch 
 * using primitive arrays and node references. No java.util collections 
 * (Stack, Queue, LinkedList, ArrayList, HashMap, TreeMap) are used.
 */
public class Graph {

    // ==========================================
    // 1. Core Node and List Structures
    // ==========================================

    /**
     * Node in the singly-linked adjacency list.
     */
    public static class AdjNode {
        public int vertex;
        public AdjNode next;

        public AdjNode(int vertex, AdjNode next) {
            this.vertex = vertex;
            this.next = next;
        }
    }

    /**
     * Graph Vertex representation holding student or skill data.
     */
    public static class GraphNode {
        public int id;
        public String name;
        public String type; // "STUDENT" or "SKILL"

        public GraphNode(int id, String name, String type) {
            this.id = id;
            this.name = name;
            this.type = type;
        }

        @Override
        public String toString() {
            return "[" + id + "] " + name + " (" + type + ")";
        }
    }

    // ==========================================
    // 2. Custom Stack Implementation (Zero Library)
    // ==========================================

    /**
     * Array-based custom LIFO Stack for DFS and Path Reconstruction.
     * Supports both dynamic auto-resizing (default) and fixed-capacity verification mode.
     */
    public static class CustomStack {
        private int[] data;
        private int top;
        private final boolean fixedCapacity;

        public CustomStack(int initialCapacity) {
            this(initialCapacity, false);
        }

        public CustomStack(int initialCapacity, boolean fixedCapacity) {
            int cap = initialCapacity > 0 ? initialCapacity : 10;
            this.data = new int[cap];
            this.top = -1;
            this.fixedCapacity = fixedCapacity;
        }

        public void push(int value) {
            if (top + 1 >= data.length) {
                if (fixedCapacity) {
                    throw new IllegalStateException("Stack overflow: Fixed-capacity stack is full (capacity=" + data.length + ").");
                }
                int[] bigger = new int[data.length * 2];
                for (int i = 0; i <= top; i++) {
                    bigger[i] = data[i];
                }
                data = bigger;
            }
            data[++top] = value;
        }

        public int pop() {
            if (isEmpty()) {
                throw new IllegalStateException("Stack underflow: Stack is empty.");
            }
            return data[top--];
        }

        public int peek() {
            if (isEmpty()) {
                throw new IllegalStateException("Stack underflow: Stack is empty.");
            }
            return data[top];
        }

        public boolean isEmpty() {
            return top == -1;
        }

        public boolean isFull() {
            return top + 1 >= data.length;
        }

        public int size() {
            return top + 1;
        }

        public int getCapacity() {
            return data.length;
        }
    }

    // ==========================================
    // 3. Custom Queue Implementation (Zero Library)
    // ==========================================

    /**
     * Array-based circular FIFO Queue for BFS.
     * Supports both dynamic auto-resizing (default) and fixed-capacity verification mode.
     */
    public static class CustomQueue {
        private int[] data;
        private int front;
        private int rear;
        private int count;
        private int capacity;
        private final boolean fixedCapacity;

        public CustomQueue(int initialCapacity) {
            this(initialCapacity, false);
        }

        public CustomQueue(int initialCapacity, boolean fixedCapacity) {
            this.capacity = initialCapacity > 0 ? initialCapacity : 10;
            this.data = new int[this.capacity];
            this.front = 0;
            this.rear = 0;
            this.count = 0;
            this.fixedCapacity = fixedCapacity;
        }

        public void enqueue(int value) {
            if (count == capacity) {
                if (fixedCapacity) {
                    throw new IllegalStateException("Queue overflow: Fixed-capacity circular queue is full (capacity=" + capacity + ").");
                }
                int newCap = capacity * 2;
                int[] bigger = new int[newCap];
                for (int i = 0; i < count; i++) {
                    bigger[i] = data[(front + i) % capacity];
                }
                data = bigger;
                front = 0;
                rear = count;
                capacity = newCap;
            }
            data[rear] = value;
            rear = (rear + 1) % capacity;
            count++;
        }

        public int dequeue() {
            if (isEmpty()) {
                throw new IllegalStateException("Queue underflow: Queue is empty.");
            }
            int value = data[front];
            front = (front + 1) % capacity;
            count--;
            return value;
        }

        public boolean isEmpty() {
            return count == 0;
        }

        public boolean isFull() {
            return count == capacity;
        }

        public int size() {
            return count;
        }

        public int getCapacity() {
            return capacity;
        }
    }

    // ==========================================
    // 4. Path Result Model
    // ==========================================

    public static class PathResult {
        public final int[] path;       // Ordered node IDs from start to dest
        public final int length;       // Number of vertices in path
        public final int edgeCount;    // Number of edges (length - 1)
        public final boolean found;    // True if path exists, false if unreachable

        public PathResult(int[] path, int length, boolean found) {
            this.path = path;
            this.length = length;
            this.edgeCount = found ? Math.max(0, length - 1) : -1;
            this.found = found;
        }

        public String formatPath(Graph graph) {
            if (!found || path == null || length == 0) {
                return "No path exists (unreachable or disconnected).";
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < length; i++) {
                GraphNode node = graph.getNode(path[i]);
                if (node != null) {
                    sb.append(node.name).append(" [").append(node.type).append("]");
                } else {
                    sb.append("Node#").append(path[i]);
                }
                if (i < length - 1) {
                    sb.append(" -> ");
                }
            }
            sb.append(" (Total Edges: ").append(edgeCount).append(")");
            return sb.toString();
        }
    }

    // ==========================================
    // 5. Graph State & Internal Storage
    // ==========================================

    private GraphNode[] nodes;
    private AdjNode[] adjList;
    private int nodeCount;
    private int capacity;

    public Graph() {
        this(16);
    }

    public Graph(int initialCapacity) {
        this.capacity = initialCapacity > 0 ? initialCapacity : 16;
        this.nodes = new GraphNode[this.capacity];
        this.adjList = new AdjNode[this.capacity];
        this.nodeCount = 0;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public boolean isValidNode(int id) {
        return id >= 0 && id < nodeCount && nodes[id] != null;
    }

    public GraphNode getNode(int id) {
        if (!isValidNode(id)) {
            return null;
        }
        return nodes[id];
    }

    private void ensureCapacity() {
        if (nodeCount >= capacity) {
            int newCap = capacity * 2;
            GraphNode[] newNodes = new GraphNode[newCap];
            AdjNode[] newAdjList = new AdjNode[newCap];
            for (int i = 0; i < nodeCount; i++) {
                newNodes[i] = nodes[i];
                newAdjList[i] = adjList[i];
            }
            nodes = newNodes;
            adjList = newAdjList;
            capacity = newCap;
        }
    }

    // ==========================================
    // 6. Node & Edge Mutators
    // ==========================================

    /**
     * Add a student or skill node. Checks for duplicates.
     */
    public int addNode(String name, String type) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Node name cannot be null or empty.");
        }
        String cleanName = name.trim();
        String cleanType = (type == null || type.trim().isEmpty()) ? "STUDENT" : type.trim().toUpperCase();

        int existing = findNodeByName(cleanName);
        if (existing != -1) {
            return existing; // Already exists
        }

        ensureCapacity();
        int newId = nodeCount;
        nodes[newId] = new GraphNode(newId, cleanName, cleanType);
        adjList[newId] = null;
        nodeCount++;
        return newId;
    }

    /**
     * Find node ID by exact or case-insensitive name using linear scan (zero library map).
     */
    public int findNodeByName(String name) {
        if (name == null) return -1;
        String clean = name.trim();
        for (int i = 0; i < nodeCount; i++) {
            if (nodes[i] != null && nodes[i].name.equalsIgnoreCase(clean)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Add an undirected edge between vertices u and v.
     */
    public boolean addEdge(int u, int v) {
        if (!isValidNode(u) || !isValidNode(v)) {
            return false;
        }
        if (u == v) {
            return false; // No self loops in peer-matching network
        }

        // Check if edge already exists
        AdjNode curr = adjList[u];
        while (curr != null) {
            if (curr.vertex == v) {
                return false; // Edge already exists
            }
            curr = curr.next;
        }

        // Add edge in both directions
        adjList[u] = new AdjNode(v, adjList[u]);
        adjList[v] = new AdjNode(u, adjList[v]);
        return true;
    }

    public boolean addEdgeByName(String nameA, String nameB) {
        int u = findNodeByName(nameA);
        int v = findNodeByName(nameB);
        if (u == -1 || v == -1) {
            return false;
        }
        return addEdge(u, v);
    }

    public boolean hasEdge(int u, int v) {
        if (!isValidNode(u) || !isValidNode(v)) {
            return false;
        }
        AdjNode curr = adjList[u];
        while (curr != null) {
            if (curr.vertex == v) return true;
            curr = curr.next;
        }
        return false;
    }

    // ==========================================
    // 7. Graph Traversal: BFS (Shortest Path by Edge Count)
    // ==========================================

    /**
     * Finds the shortest path between start and dest using Breadth-First Search.
     * Guarantees the minimum edge-count path in an unweighted graph.
     */
    public PathResult findShortestPathBFS(int start, int dest) {
        if (!isValidNode(start) || !isValidNode(dest)) {
            return new PathResult(null, 0, false);
        }

        if (start == dest) {
            return new PathResult(new int[]{start}, 1, true);
        }

        boolean[] visited = new boolean[nodeCount];
        int[] parent = new int[nodeCount];
        for (int i = 0; i < nodeCount; i++) {
            parent[i] = -1;
        }

        CustomQueue queue = new CustomQueue(nodeCount + 2);

        visited[start] = true;
        parent[start] = start;
        queue.enqueue(start);

        boolean found = false;

        while (!queue.isEmpty()) {
            int curr = queue.dequeue();
            if (curr == dest) {
                found = true;
                break;
            }

            AdjNode neighbor = adjList[curr];
            while (neighbor != null) {
                int next = neighbor.vertex;
                if (!visited[next]) {
                    visited[next] = true;
                    parent[next] = curr;
                    queue.enqueue(next);
                }
                neighbor = neighbor.next;
            }
        }

        if (!found) {
            // Disconnected or unreachable destination
            return new PathResult(null, 0, false);
        }

        // Path reconstruction using custom stack
        return reconstructPath(start, dest, parent);
    }

    // ==========================================
    // 8. Graph Traversal: DFS (Does NOT guarantee Shortest Path)
    // ==========================================

    /**
     * Finds a path between start and dest using Depth-First Search.
     * Note: DFS explores deeply along branches and does NOT guarantee shortest path.
     */
    public PathResult findPathDFS(int start, int dest) {
        if (!isValidNode(start) || !isValidNode(dest)) {
            return new PathResult(null, 0, false);
        }

        if (start == dest) {
            return new PathResult(new int[]{start}, 1, true);
        }

        boolean[] visited = new boolean[nodeCount];
        int[] parent = new int[nodeCount];
        for (int i = 0; i < nodeCount; i++) {
            parent[i] = -1;
        }
        parent[start] = start;

        boolean found = dfsRecursive(start, dest, visited, parent);
        if (!found) {
            return new PathResult(null, 0, false);
        }

        return reconstructPath(start, dest, parent);
    }

    private boolean dfsRecursive(int curr, int dest, boolean[] visited, int[] parent) {
        visited[curr] = true;
        if (curr == dest) {
            return true;
        }

        AdjNode neighbor = adjList[curr];
        while (neighbor != null) {
            int next = neighbor.vertex;
            if (!visited[next]) {
                parent[next] = curr;
                if (dfsRecursive(next, dest, visited, parent)) {
                    return true;
                }
            }
            neighbor = neighbor.next;
        }
        return false;
    }

    // ==========================================
    // 9. Path Reconstruction via Custom Stack
    // ==========================================

    private PathResult reconstructPath(int start, int dest, int[] parent) {
        CustomStack stack = new CustomStack(nodeCount + 2);
        int curr = dest;

        while (curr != start) {
            stack.push(curr);
            curr = parent[curr];
            if (curr == -1) {
                // Failsafe in case of corrupted parent chain
                return new PathResult(null, 0, false);
            }
        }
        stack.push(start);

        int pathLen = stack.size();
        int[] path = new int[pathLen];
        int idx = 0;
        while (!stack.isEmpty()) {
            path[idx++] = stack.pop();
        }

        return new PathResult(path, pathLen, true);
    }

    // ==========================================
    // 10. Traversal Order Tracking (For Testing Visited)
    // ==========================================

    public int[] getBFSTraversalOrder(int start) {
        if (!isValidNode(start)) return new int[0];

        boolean[] visited = new boolean[nodeCount];
        int[] order = new int[nodeCount];
        int orderCount = 0;

        CustomQueue queue = new CustomQueue(nodeCount);
        visited[start] = true;
        queue.enqueue(start);

        while (!queue.isEmpty()) {
            int curr = queue.dequeue();
            order[orderCount++] = curr;

            AdjNode p = adjList[curr];
            while (p != null) {
                if (!visited[p.vertex]) {
                    visited[p.vertex] = true;
                    queue.enqueue(p.vertex);
                }
                p = p.next;
            }
        }

        int[] result = new int[orderCount];
        for (int i = 0; i < orderCount; i++) result[i] = order[i];
        return result;
    }

    // ==========================================
    // 11. Domain-Specific Skill Matching Queries
    // ==========================================

    /**
     * Find all students possessing the specified skill.
     */
    public GraphNode[] getStudentsWithSkill(String skillName) {
        int skillId = findNodeByName(skillName);
        if (skillId == -1) {
            return new GraphNode[0];
        }

        // Count matching student neighbors
        int count = 0;
        AdjNode curr = adjList[skillId];
        while (curr != null) {
            GraphNode n = nodes[curr.vertex];
            if (n != null && "STUDENT".equalsIgnoreCase(n.type)) {
                count++;
            }
            curr = curr.next;
        }

        GraphNode[] result = new GraphNode[count];
        int idx = 0;
        curr = adjList[skillId];
        while (curr != null) {
            GraphNode n = nodes[curr.vertex];
            if (n != null && "STUDENT".equalsIgnoreCase(n.type)) {
                result[idx++] = n;
            }
            curr = curr.next;
        }
        return result;
    }

    /**
     * Find all skills connected to a specific student.
     */
    public GraphNode[] getSkillsOfStudent(String studentName) {
        int studentId = findNodeByName(studentName);
        if (studentId == -1) {
            return new GraphNode[0];
        }

        int count = 0;
        AdjNode curr = adjList[studentId];
        while (curr != null) {
            GraphNode n = nodes[curr.vertex];
            if (n != null && "SKILL".equalsIgnoreCase(n.type)) {
                count++;
            }
            curr = curr.next;
        }

        GraphNode[] result = new GraphNode[count];
        int idx = 0;
        curr = adjList[studentId];
        while (curr != null) {
            GraphNode n = nodes[curr.vertex];
            if (n != null && "SKILL".equalsIgnoreCase(n.type)) {
                result[idx++] = n;
            }
            curr = curr.next;
        }
        return result;
    }

    /**
     * Get all registered nodes as an array.
     */
    public GraphNode[] getAllNodes() {
        GraphNode[] result = new GraphNode[nodeCount];
        for (int i = 0; i < nodeCount; i++) {
            result[i] = nodes[i];
        }
        return result;
    }

    /**
     * Get all unique undirected edges as an int[E][2] array.
     */
    public int[][] getAllEdges() {
        // Count total unique edges where u < v
        int edgeCount = 0;
        for (int u = 0; u < nodeCount; u++) {
            AdjNode curr = adjList[u];
            while (curr != null) {
                if (u < curr.vertex) {
                    edgeCount++;
                }
                curr = curr.next;
            }
        }

        int[][] edges = new int[edgeCount][2];
        int idx = 0;
        for (int u = 0; u < nodeCount; u++) {
            AdjNode curr = adjList[u];
            while (curr != null) {
                if (u < curr.vertex) {
                    edges[idx][0] = u;
                    edges[idx][1] = curr.vertex;
                    idx++;
                }
                curr = curr.next;
            }
        }
        return edges;
    }
}
