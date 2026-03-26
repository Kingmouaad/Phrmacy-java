package com.pharmacy.datastructures;

import com.pharmacy.db.InteractionDAO;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.sql.SQLException;
import java.util.*;

/**
 * JGraphT-based drug interaction network.
 *
 * WHY WE USE JGraphT (a Graph):
 * - Before: checking drug interactions was done with simple SQL queries
 *   comparing two drugs at a time. No big-picture view.
 * - Now: we model the entire drug interaction network as a GRAPH:
 *     • Vertices (nodes)  = drug names (active ingredients)
 *     • Edges (connections) = known interactions between two drugs
 *
 *   This lets us ask powerful questions like:
 *     "Which drugs interact with Aspirin?" → just look at Aspirin's neighbors
 *     "Is there ANY chain of interactions between Drug A and Drug D?"
 *         → graph traversal (BFS/DFS) — impossible with simple SQL.
 *
 * HOW IT WORKS:
 * - On startup, we load all interactions from the DB and build the graph.
 * - Each edge stores severity and description in a custom labeled edge class.
 * - Before selling multiple drugs, we check if any pair is connected by an edge.
 */
public class DrugInteractionGraph {

    /**
     * Custom edge class that carries interaction metadata (severity + description).
     */
    public static class InteractionEdge extends DefaultEdge {
        private String severity;
        private String description;

        public InteractionEdge() {}

        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        @Override
        public String toString() {
            return "[" + severity + "] " + description;
        }

        // Expose source/target from DefaultEdge
        public String getSourceDrug() { return (String) getSource(); }
        public String getTargetDrug() { return (String) getTarget(); }
    }

    // The core data structure: an undirected graph of drug interactions
    private final Graph<String, InteractionEdge> graph;
    private final InteractionDAO interactionDAO;

    public DrugInteractionGraph() {
        this.graph = new SimpleGraph<>(InteractionEdge.class);
        this.interactionDAO = new InteractionDAO();
        loadFromDatabase();
    }

    /**
     * Load all interactions from the database and build the graph.
     */
    public void loadFromDatabase() {
        try {
            List<Object[]> allInteractions = interactionDAO.findAll();
            for (Object[] row : allInteractions) {
                // row: {id, drug_a, drug_b, severity, description}
                String drugA = (String) row[1];
                String drugB = (String) row[2];
                String severity = (String) row[3];
                String description = (String) row[4];

                addInteraction(drugA, drugB, severity, description);
            }
            System.out.println("[DrugInteractionGraph] Loaded " + graph.edgeSet().size()
                    + " interactions, " + graph.vertexSet().size() + " drugs.");
        } catch (SQLException e) {
            System.out.println("[DrugInteractionGraph] Error loading: " + e.getMessage());
        }
    }

    /**
     * Add an interaction edge to the graph.
     */
    public void addInteraction(String drugA, String drugB, String severity, String description) {
        // Add vertices if not already present
        graph.addVertex(drugA);
        graph.addVertex(drugB);

        // Add edge with metadata
        InteractionEdge edge = graph.addEdge(drugA, drugB);
        if (edge != null) {
            edge.setSeverity(severity);
            edge.setDescription(description);
        }
    }

    /**
     * Check if two drugs interact — O(1) edge lookup.
     * Returns the InteractionEdge if they interact, null if safe.
     */
    public InteractionEdge checkInteraction(String drugA, String drugB) {
        if (!graph.containsVertex(drugA) || !graph.containsVertex(drugB)) {
            return null;
        }
        return graph.getEdge(drugA, drugB);
    }

    /**
     * Check a new drug against a list of drugs the customer is already taking.
     * Returns list of all found interactions.
     */
    public List<InteractionEdge> checkAgainstList(String newDrug, List<String> currentDrugs) {
        List<InteractionEdge> conflicts = new ArrayList<>();
        if (!graph.containsVertex(newDrug)) return conflicts;

        for (String drug : currentDrugs) {
            InteractionEdge edge = checkInteraction(newDrug, drug);
            if (edge != null) {
                conflicts.add(edge);
            }
        }
        return conflicts;
    }

    /**
     * Get all drugs that interact with a given drug — just look at neighbors.
     */
    public Set<String> getInteractingDrugs(String drugName) {
        Set<String> neighbors = new HashSet<>();
        if (!graph.containsVertex(drugName)) return neighbors;

        for (InteractionEdge edge : graph.edgesOf(drugName)) {
            String source = edge.getSourceDrug();
            String target = edge.getTargetDrug();
            neighbors.add(source.equals(drugName) ? target : source);
        }
        return neighbors;
    }

    /**
     * Print the full interaction network — great for debugging or admin panel.
     */
    public void printNetwork() {
        System.out.println("\n═══════════════════════════════════════════");
        System.out.println("       DRUG INTERACTION NETWORK (Graph)    ");
        System.out.println("═══════════════════════════════════════════");
        System.out.println("Drugs (Vertices): " + graph.vertexSet().size());
        System.out.println("Interactions (Edges): " + graph.edgeSet().size());
        System.out.println("───────────────────────────────────────────");

        for (InteractionEdge edge : graph.edgeSet()) {
            String source = edge.getSourceDrug();
            String target = edge.getTargetDrug();
            System.out.println("  " + source + " ←→ " + target +
                    "  [" + edge.getSeverity() + "] " + edge.getDescription());
        }
        System.out.println("═══════════════════════════════════════════\n");
    }

    /**
     * Get the number of known interactions.
     */
    public int getInteractionCount() {
        return graph.edgeSet().size();
    }

    /**
     * Get the number of known drugs in the network.
     */
    public int getDrugCount() {
        return graph.vertexSet().size();
    }

    /**
     * Get the underlying graph (for advanced queries or GUI visualization).
     */
    public Graph<String, InteractionEdge> getGraph() {
        return graph;
    }
}
