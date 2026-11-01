import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Main application for Student Skill-Matching Network.
 * 
 * Provides:
 * 1. Interactive terminal console menu.
 * 2. Embedded zero-dependency HTTP server (com.sun.net.httpserver) for the Web Frontend.
 * 3. Direct integration with the custom-built Graph data structure.
 */
public class Main {

    private static final int DEFAULT_PORT = 8080;
    private static Graph graph = new Graph(32);
    private static HttpServer server = null;
    private static int activePort = DEFAULT_PORT;

    public static void main(String[] args) {
        // 1. Populate initial network data
        initializeSampleData(graph);

        // 2. Start lightweight embedded web server
        startWebServer();

        // 3. Check CLI arguments
        if (args.length > 0 && args[0].equalsIgnoreCase("--test")) {
            GraphTest.runAllTests();
            stopWebServer();
            return;
        }

        // 4. Interactive Console Loop
        runConsoleMenu();

        // 5. Cleanup on exit
        stopWebServer();
    }

    // ==========================================================
    // Sample Data Setup
    // ==========================================================

    private static void initializeSampleData(Graph g) {
        // Students
        int alex = g.addNode("Alex", "STUDENT");
        int sam = g.addNode("Sam", "STUDENT");
        int maya = g.addNode("Maya", "STUDENT");
        int leo = g.addNode("Leo", "STUDENT");
        int zoe = g.addNode("Zoe", "STUDENT");
        int ryan = g.addNode("Ryan", "STUDENT");

        // Skills
        int java = g.addNode("Java", "SKILL");
        int python = g.addNode("Python", "SKILL");
        int web = g.addNode("Web Dev", "SKILL");
        int ai = g.addNode("AI", "SKILL");
        int cloud = g.addNode("Cloud", "SKILL");
        int db = g.addNode("Database", "SKILL");

        // Connect Students to Skills
        g.addEdge(alex, java);
        g.addEdge(alex, db);

        g.addEdge(sam, python);
        g.addEdge(sam, ai);
        g.addEdge(sam, db);

        g.addEdge(maya, web);
        g.addEdge(maya, java);

        g.addEdge(leo, web);
        g.addEdge(leo, cloud);

        g.addEdge(zoe, python);
        g.addEdge(zoe, ai);

        g.addEdge(ryan, cloud);
        g.addEdge(ryan, ai);

        // Direct Collaboration (Peer to Peer)
        g.addEdge(alex, sam);
    }

    // ==========================================================
    // Interactive Console Menu
    // ==========================================================

    private static void runConsoleMenu() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("============================================================");
        System.out.println("       🎓 STUDENT SKILL-MATCHING NETWORK SYSTEM");
        System.out.println("============================================================");
        System.out.printf(" 🌐 Web Frontend URL: http://localhost:%d\n", activePort);
        System.out.println(" 💻 Console Interface Ready");
        System.out.println("============================================================");

        while (running) {
            System.out.println("\n----------------- [ CONSOLE MENU ] -----------------");
            System.out.println("1. Display All Students & Skills");
            System.out.println("2. Add a New Student");
            System.out.println("3. Add a New Skill");
            System.out.println("4. Connect Student to Skill (or Peer-to-Peer)");
            System.out.println("5. Match Students by Skill");
            System.out.println("6. Find Shortest Path / Collaboration Bridge (BFS)");
            System.out.println("7. Find Path via DFS (Deep Exploration)");
            System.out.println("8. Compare BFS vs DFS on Same Query");
            System.out.println("9. Run Graph Verification Test Suite");
            System.out.println("0. Exit Application");
            System.out.print("Select an option (0-9): ");

            String input = scanner.nextLine().trim();
            System.out.println();

            switch (input) {
                case "1":
                    displayAllNodes();
                    break;
                case "2":
                    System.out.print("Enter Student Name: ");
                    String sName = scanner.nextLine().trim();
                    if (!sName.isEmpty()) {
                        int id = graph.addNode(sName, "STUDENT");
                        System.out.println("✔ Student registered with ID: " + id);
                    }
                    break;
                case "3":
                    System.out.print("Enter Skill Name: ");
                    String kName = scanner.nextLine().trim();
                    if (!kName.isEmpty()) {
                        int id = graph.addNode(kName, "SKILL");
                        System.out.println("✔ Skill registered with ID: " + id);
                    }
                    break;
                case "4":
                    System.out.print("Enter First Entity Name (Student/Skill): ");
                    String e1 = scanner.nextLine().trim();
                    System.out.print("Enter Second Entity Name (Student/Skill): ");
                    String e2 = scanner.nextLine().trim();
                    boolean linked = graph.addEdgeByName(e1, e2);
                    if (linked) {
                        System.out.printf("✔ Connected '%s' <---> '%s'\n", e1, e2);
                    } else {
                        System.out.println("❌ Failed to connect: Verify that both entities exist and are not already linked.");
                    }
                    break;
                case "5":
                    System.out.print("Enter Skill to Search: ");
                    String searchSkill = scanner.nextLine().trim();
                    Graph.GraphNode[] matches = graph.getStudentsWithSkill(searchSkill);
                    if (matches.length == 0) {
                        System.out.println("No students found possessing skill: " + searchSkill);
                    } else {
                        System.out.printf("Found %d student(s) with skill '%s':\n", matches.length, searchSkill);
                        for (Graph.GraphNode m : matches) {
                            System.out.println("  • " + m.name + " (ID: " + m.id + ")");
                        }
                    }
                    break;
                case "6":
                    findPathCLI(scanner, true);
                    break;
                case "7":
                    findPathCLI(scanner, false);
                    break;
                case "8":
                    comparePathsCLI(scanner);
                    break;
                case "9":
                    GraphTest.runAllTests();
                    break;
                case "0":
                    System.out.println("Exiting Student Skill-Matching Network. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Please choose between 0 and 9.");
                    break;
            }
        }
    }

    private static void displayAllNodes() {
        Graph.GraphNode[] all = graph.getAllNodes();
        System.out.println("Current Network Nodes (" + all.length + " total):");
        System.out.println("--- Students ---");
        for (Graph.GraphNode n : all) {
            if ("STUDENT".equalsIgnoreCase(n.type)) {
                Graph.GraphNode[] skills = graph.getSkillsOfStudent(n.name);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < skills.length; i++) {
                    sb.append(skills[i].name);
                    if (i < skills.length - 1) sb.append(", ");
                }
                String skillStr = sb.length() > 0 ? sb.toString() : "No skills linked yet";
                System.out.printf("  [%d] %-12s | Skills: %s\n", n.id, n.name, skillStr);
            }
        }
        System.out.println("--- Skills ---");
        for (Graph.GraphNode n : all) {
            if ("SKILL".equalsIgnoreCase(n.type)) {
                Graph.GraphNode[] studs = graph.getStudentsWithSkill(n.name);
                System.out.printf("  [%d] %-18s | Possessed by %d student(s)\n", n.id, n.name, studs.length);
            }
        }
    }

    private static void findPathCLI(Scanner scanner, boolean isBfs) {
        String algoName = isBfs ? "BFS (Shortest Path)" : "DFS (Depth Exploration)";
        System.out.println("=== Find Path using " + algoName + " ===");
        System.out.print("Enter Source Name: ");
        String src = scanner.nextLine().trim();
        System.out.print("Enter Target Name: ");
        String tgt = scanner.nextLine().trim();

        int u = graph.findNodeByName(src);
        int v = graph.findNodeByName(tgt);

        if (u == -1 || v == -1) {
            System.out.println("❌ Invalid node name(s). Please verify that both exist.");
            return;
        }

        Graph.PathResult result = isBfs ? graph.findShortestPathBFS(u, v) : graph.findPathDFS(u, v);
        System.out.println("Result: " + result.formatPath(graph));
    }

    private static void comparePathsCLI(Scanner scanner) {
        System.out.println("=== Compare BFS vs DFS Pathing ===");
        System.out.print("Enter Source Name: ");
        String src = scanner.nextLine().trim();
        System.out.print("Enter Target Name: ");
        String tgt = scanner.nextLine().trim();

        int u = graph.findNodeByName(src);
        int v = graph.findNodeByName(tgt);

        if (u == -1 || v == -1) {
            System.out.println("❌ Invalid node name(s). Please verify that both exist.");
            return;
        }

        Graph.PathResult bfs = graph.findShortestPathBFS(u, v);
        Graph.PathResult dfs = graph.findPathDFS(u, v);

        System.out.println("\n[BFS Shortest Path (by edge count)]:");
        System.out.println("  " + bfs.formatPath(graph));
        System.out.println("[DFS Exploration Path]:");
        System.out.println("  " + dfs.formatPath(graph));

        if (bfs.found && dfs.found) {
            System.out.printf("\nEdge Count Difference: BFS = %d edge(s), DFS = %d edge(s)\n",
                    bfs.edgeCount, dfs.edgeCount);
            if (bfs.edgeCount < dfs.edgeCount) {
                System.out.println("💡 Note: BFS found a strictly shorter path than DFS, verifying unweighted shortest path guarantee.");
            } else {
                System.out.println("💡 Note: In this instance, both paths reached destination in equal edges.");
            }
        }
    }

    // ==========================================================
    // Embedded HTTP Server (Frontend & REST Endpoints)
    // ==========================================================

    private static void startWebServer() {
        int[] portsToTry = {DEFAULT_PORT, 8081, 8082, 9000};
        for (int p : portsToTry) {
            try {
                server = HttpServer.create(new InetSocketAddress(p), 0);
                activePort = p;
                break;
            } catch (IOException e) {
                // Port occupied, try next
            }
        }

        if (server == null) {
            System.err.println("Warning: Unable to bind embedded web server port.");
            return;
        }

        // Web UI root handler
        server.createContext("/", new FrontendHandler());

        // API Endpoints
        server.createContext("/api/graph", new ApiGraphHandler());
        server.createContext("/api/match", new ApiMatchHandler());
        server.createContext("/api/path", new ApiPathHandler());
        server.createContext("/api/node", new ApiNodeHandler());
        server.createContext("/api/edge", new ApiEdgeHandler());
        server.createContext("/api/tests", new ApiTestsHandler());

        server.setExecutor(null);
        server.start();
    }

    private static void stopWebServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    // ==========================================================
    // HTTP Handlers
    // ==========================================================

    static class FrontendHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            byte[] responseBytes;
            File htmlFile = new File("web/index.html");
            if (!htmlFile.exists()) {
                htmlFile = new File("Student-Skill-Matching-Network/web/index.html");
            }

            if (htmlFile.exists()) {
                responseBytes = Files.readAllBytes(htmlFile.toPath());
            } else {
                String fallback = "<html><body><h1>Student Skill-Matching Network</h1><p>index.html not found.</p></body></html>";
                responseBytes = fallback.getBytes(StandardCharsets.UTF_8);
            }

            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }
    }

    static class ApiGraphHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Graph.GraphNode[] allNodes = graph.getAllNodes();
            int[][] allEdges = graph.getAllEdges();

            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"nodes\": [\n");
            for (int i = 0; i < allNodes.length; i++) {
                Graph.GraphNode n = allNodes[i];
                json.append(String.format("    {\"id\": %d, \"name\": \"%s\", \"type\": \"%s\"}",
                        n.id, escapeJson(n.name), n.type));
                if (i < allNodes.length - 1) json.append(",");
                json.append("\n");
            }
            json.append("  ],\n");
            json.append("  \"edges\": [\n");
            for (int i = 0; i < allEdges.length; i++) {
                json.append(String.format("    {\"source\": %d, \"target\": %d}", allEdges[i][0], allEdges[i][1]));
                if (i < allEdges.length - 1) json.append(",");
                json.append("\n");
            }
            json.append("  ]\n");
            json.append("}");

            sendJsonResponse(exchange, 200, json.toString());
        }
    }

    static class ApiMatchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String skill = getParam(query, "skill");

            if (skill == null || skill.trim().isEmpty()) {
                sendJsonResponse(exchange, 400, "{\"error\": \"Missing 'skill' query parameter\"}");
                return;
            }

            Graph.GraphNode[] matched = graph.getStudentsWithSkill(skill);
            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"skill\": \"").append(escapeJson(skill)).append("\",\n");
            json.append("  \"count\": ").append(matched.length).append(",\n");
            json.append("  \"students\": [\n");
            for (int i = 0; i < matched.length; i++) {
                json.append(String.format("    {\"id\": %d, \"name\": \"%s\"}", matched[i].id, escapeJson(matched[i].name)));
                if (i < matched.length - 1) json.append(",");
                json.append("\n");
            }
            json.append("  ]\n");
            json.append("}");

            sendJsonResponse(exchange, 200, json.toString());
        }
    }

    static class ApiPathHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String from = getParam(query, "from");
            String to = getParam(query, "to");
            String algo = getParam(query, "algo");
            if (algo == null) algo = "bfs";

            int u = graph.findNodeByName(from);
            int v = graph.findNodeByName(to);

            if (u == -1 || v == -1) {
                sendJsonResponse(exchange, 404, "{\"found\": false, \"error\": \"Source or target not found\"}");
                return;
            }

            Graph.PathResult result = "dfs".equalsIgnoreCase(algo)
                    ? graph.findPathDFS(u, v)
                    : graph.findShortestPathBFS(u, v);

            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"found\": ").append(result.found).append(",\n");
            json.append("  \"algorithm\": \"").append(algo.toUpperCase()).append("\",\n");
            json.append("  \"edgeCount\": ").append(result.edgeCount).append(",\n");
            json.append("  \"path\": [\n");
            if (result.found && result.path != null) {
                for (int i = 0; i < result.length; i++) {
                    Graph.GraphNode node = graph.getNode(result.path[i]);
                    json.append(String.format("    {\"id\": %d, \"name\": \"%s\", \"type\": \"%s\"}",
                            node.id, escapeJson(node.name), node.type));
                    if (i < result.length - 1) json.append(",");
                    json.append("\n");
                }
            }
            json.append("  ]\n");
            json.append("}");

            sendJsonResponse(exchange, 200, json.toString());
        }
    }

    static class ApiNodeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
                return;
            }
            String body = readBody(exchange);
            String name = getParamFromBody(body, "name");
            String type = getParamFromBody(body, "type");

            if (name == null || name.trim().isEmpty()) {
                sendJsonResponse(exchange, 400, "{\"error\": \"Name is required\"}");
                return;
            }

            int id = graph.addNode(name, type != null ? type : "STUDENT");
            sendJsonResponse(exchange, 200, String.format("{\"success\": true, \"id\": %d, \"name\": \"%s\"}",
                    id, escapeJson(name)));
        }
    }

    static class ApiEdgeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
                return;
            }
            String body = readBody(exchange);
            String from = getParamFromBody(body, "from");
            String to = getParamFromBody(body, "to");

            boolean success = graph.addEdgeByName(from, to);
            sendJsonResponse(exchange, success ? 200 : 400,
                    String.format("{\"success\": %b, \"from\": \"%s\", \"to\": \"%s\"}",
                            success, escapeJson(from), escapeJson(to)));
        }
    }

    static class ApiTestsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            boolean pass = GraphTest.runAllTests();
            String json = String.format("{\n  \"allPassed\": %b,\n  \"testsRun\": 6,\n  \"testsPassed\": %d\n}",
                    pass, pass ? 6 : 0);
            sendJsonResponse(exchange, 200, json);
        }
    }

    // ==========================================================
    // HTTP Utilities
    // ==========================================================

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String responseJson) throws IOException {
        byte[] bytes = responseJson.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String getParam(String query, String key) {
        if (query == null) return null;
        for (String pair : query.split("&")) {
            String[] parts = pair.split("=");
            if (parts.length >= 2 && parts[0].equalsIgnoreCase(key)) {
                try {
                    return URLDecoder.decode(parts[1], StandardCharsets.UTF_8.name());
                } catch (Exception e) {
                    return parts[1];
                }
            }
        }
        return null;
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            int r;
            while ((r = is.read(buf)) != -1) {
                baos.write(buf, 0, r);
            }
            return baos.toString(StandardCharsets.UTF_8.name());
        }
    }

    private static String getParamFromBody(String body, String key) {
        if (body == null) return null;
        // Simple JSON extractor or form-encoded extractor
        // Try key: "value"
        String search = "\"" + key + "\":";
        int idx = body.indexOf(search);
        if (idx != -1) {
            int start = body.indexOf("\"", idx + search.length());
            if (start != -1) {
                int end = body.indexOf("\"", start + 1);
                if (end != -1) {
                    return body.substring(start + 1, end);
                }
            }
        }
        return getParam(body, key);
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
