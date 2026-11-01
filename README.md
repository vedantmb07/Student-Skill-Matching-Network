# 🎓 Student Skill-Matching Network

A lightweight, high-performance Java console application and interactive web frontend for skill-based student matching and collaboration path discovery, built entirely on a **custom Graph data structure** without external libraries or standard collection frameworks.

[![Java](https://img.shields.io/badge/Java-25%2B%20%7C%20LTS-orange.svg)](https://www.oracle.com/java/)
[![Data Structures](https://img.shields.io/badge/Graph%20DS-Zero--Library%20Custom-blue.svg)](#-principal-data-structure-architecture)
[![Tests](https://img.shields.io/badge/Tests-5%2F5%20Passing%20(100%25)-brightgreen.svg)](#-testing--verification-suite)
[![Dependencies](https://img.shields.io/badge/Dependencies-Zero%20(Pure%20JDK)-success.svg)](#-features)

---

## 📌 Project Overview

In academic and project environments, students often need to find peers with complementary skills or identify connection paths for collaboration. The **Student Skill-Matching Network** models this ecosystem as an undirected graph:
- **Vertices (Nodes):** Represent either a **Student** (e.g., Alice, Bob) or a **Skill** (e.g., Java, Python, Machine Learning).
- **Edges (Connections):** Represent a student possessing a skill (`Student <-> Skill`) or direct peer collaborations (`Student <-> Student`).

By querying this graph:
1. **Skill Matching:** Find all students possessing a specific skill with an immediate adjacency list lookup.
2. **Collaboration Bridge (BFS):** Find the shortest connection chain between any two students or skills by edge count.
3. **Exploration (DFS):** Perform depth-first exploration along connected paths.

---

## 🧩 Principal Data Structure Architecture

Per microproject assessment requirements, **all principal data structures are implemented from scratch** using primitive arrays, pointers, and custom nodes. **No standard collection classes** (`Stack`, `Queue`, `LinkedList`, `ArrayList`, `TreeMap`, `HashMap`, `HashSet`) are used in the assessed implementation.

```
                      +-----------------------------+
                      |            Graph            |
                      |-----------------------------|
                      | GraphNode[] nodes           |
                      | AdjNode[]   adjList         |
                      +--------------+--------------+
                                     |
             +-----------------------+-----------------------+
             |                                               |
             v                                               v
+-------------------------+                     +-------------------------+
|      CustomQueue        |                     |       CustomStack       |
|-------------------------|                     |-------------------------|
| int[] data (Circular)   |                     | int[] data (Array-LIFO) |
| int front, rear, count  |                     | int top                 |
| enqueue() / dequeue()   |                     | push() / pop() / peek() |
+-------------------------+                     +-------------------------+
       (Used by BFS)                                (Used by DFS & Paths)
```

### Component Breakdown
1. **Adjacency List (`AdjNode`):** Singly-linked list node (`int vertex; AdjNode next;`). Edges are stored as pointers without `LinkedList`.
2. **Dynamic Vertex Array (`GraphNode[]`):** Array-backed storage for nodes. Name lookup is performed via linear scan without `HashMap` or `TreeMap`.
3. **Custom Queue (`CustomQueue`):** Circular array FIFO queue with dynamic resizing for Breadth-First Search (BFS).
4. **Custom Stack (`CustomStack`):** Dynamic array LIFO stack for Depth-First Search (DFS) and path reconstruction.
5. **Visited Tracking (`boolean[] visited`):** Tracks visited state during traversals to prevent infinite loops in cyclic components.
6. **Path Reconstruction (`int[] parent`):** Backtracks parent pointers into `CustomStack` to reconstruct exact paths in order.

---

## ✨ Features

- **Dual Interface:**
  - 💻 **Interactive Console CLI:** Fast terminal menu for all operations.
  - 🌐 **Embedded Web Frontend:** Single-page dashboard served via JDK's built-in `com.sun.net.httpserver.HttpServer` (zero npm, zero external web servers, zero external jars).
- **Interactive HTML5 Canvas Graph:** Draggable nodes, spring-embedder layout, real-time node additions, and glowing neon path highlights.
- **Path Search & Algorithm Comparison:**
  - **BFS:** Guarantees the **shortest path by edge count** in unweighted graphs.
  - **DFS:** Explores deeply along branches; does not guarantee shortest path.
- **Skill-Based Matching:** Instantly queries adjacent neighbors to list all students possessing any skill.
- **Extremely Fast & Minimal:** Contains only 3 Java files; compiles in under 1 second.

---

## 🧪 Testing & Verification Suite

The graph implementation passes 5 core verification tests:

| # | Test Requirement | Description & Result | Status |
|---|------------------|----------------------|:------:|
| 1 | **Visited Tracking** | Verified cycle prevention (`A-B-C-A`). All reachable nodes visited exactly once without infinite loops. | `PASS ✅` |
| 2 | **Disconnected / Unreachable Target** | Two disconnected subgraphs tested. Searching across components safely returns `found=false` and `path=null`. | `PASS ✅` |
| 3 | **Path Reconstruction Integrity** | Validated that reconstructed path connects `start` to `dest` and every step `(u, v)` exists in the adjacency list. | `PASS ✅` |
| 4 | **Valid Node Input Bounds** | Tested negative IDs (`-1`), out-of-bounds indices, and non-existent student lookups; all rejected safely. | `PASS ✅` |
| 5 | **BFS Shortest vs DFS Non-Guarantee** | Tested on a shortcut graph. **BFS found the 1-edge shortcut**, while **DFS took the 4-edge long branch**. | `PASS ✅` |

---

## 🚀 Compile and Run Instructions

### Prerequisites
- **Java JDK 8 or higher** (Tested with OpenJDK / Oracle JDK 25 LTS).
- No external dependencies, build tools (Maven/Gradle), or Node.js required.

### 1. Compile All Files (< 1 second)
Open your terminal in the project directory and run:
```bash
javac *.java
```

### 2. Run the Application (Console + Web Frontend)
```bash
java Main
```
- The interactive **Console Menu** will appear in your terminal.
- The **Web Frontend** will automatically start at:
  👉 **http://localhost:8080**

### 3. Run Standalone Test Suite
You can run the standalone verification test suite at any time:
```bash
java GraphTest
```
Or via the `--test` flag:
```bash
java Main --test
```

---

## 📁 File Structure

```
Student-Skill-Matching-Network/
│
├── Graph.java          # Custom Graph DS, AdjNode, CustomQueue, CustomStack, BFS/DFS
├── GraphTest.java      # 5 automated verification test cases
├── Main.java           # Interactive Console CLI + Embedded Web Server & REST API
├── web/
│   └── index.html      # Modern Web Frontend (HTML5 Canvas visualizer, CSS & JS)
└── README.md           # Project documentation, architecture & run instructions
```

---

## 💡 BFS vs DFS Shortest Path Demonstration

In unweighted graphs:
- **BFS (Breadth-First Search)** explores vertices level by level in expanding concentric circles using `CustomQueue`. The first time the destination vertex is discovered, the path traversed is mathematically guaranteed to have the **minimum edge count**.
- **DFS (Depth-First Search)** traverses as deeply as possible along the first branch using `CustomStack` or recursion. It may stumble across a long, convoluted path before considering direct shortcuts, and therefore **does not guarantee shortest path**.

### Proof from `GraphTest.java`
```
Start [0] ──────────── (Shortcut: 1 Edge) ────────────> Destination [4]
    \                                                         ^
     └─> Hop1 [1] ──> Hop2 [2] ──> Hop3 [3] ──────────────────┘
                 (Long Branch: 4 Edges)

Result:
  • BFS Shortest Path: Start -> Destination (1 edge)
  • DFS Path:         Start -> Hop1 -> Hop2 -> Hop3 -> Destination (4 edges)
```

