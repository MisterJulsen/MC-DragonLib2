package de.mrjulsen.mcdragonlib.client.util;

import org.joml.Vector2f;
import java.util.ArrayList;
import java.util.List;

public class Triangulator {

    /**
     * Trianguliert ein Polygon mit dem Ear-Clipping-Algorithmus.
     *
     * @param vertices Die Liste der Vertices des Polygons in der richtigen Reihenfolge.
     * @return Eine Liste von Dreiecken (jedes Dreieck ist eine Liste von 3 Vector2f).
     */
    public static List<List<Vector2f>> triangulate(List<Vector2f> vertices) {
        List<List<Vector2f>> triangles = new ArrayList<>();
        List<Vector2f> remainingVertices = new ArrayList<>(vertices);

        // Der Algorithmus läuft, bis nur noch 3 Vertices übrig sind (das letzte Dreieck)
        while (remainingVertices.size() > 3) {
            boolean earFound = false;
            for (int i = 0; i < remainingVertices.size(); i++) {
                // Drei aufeinanderfolgende Punkte für ein potenzielles "Ohr"
                Vector2f p1 = remainingVertices.get(i);
                Vector2f p2 = remainingVertices.get((i + 1) % remainingVertices.size());
                Vector2f p3 = remainingVertices.get((i + 2) % remainingVertices.size());

                // 1. Prüfen, ob das Dreieck ein "Ohr" ist
                if (isEar(p1, p2, p3, remainingVertices)) {
                    // 2. Das Dreieck zur Liste hinzufügen
                    List<Vector2f> triangle = new ArrayList<>();
                    triangle.add(p1);
                    triangle.add(p2);
                    triangle.add(p3);
                    triangles.add(triangle);

                    // 3. Den mittleren Punkt (die "Ohrspitze") aus der Liste entfernen
                    remainingVertices.remove((i + 1) % remainingVertices.size());
                    earFound = true;
                    break; // Die Schleife neu starten, da sich die Indizes geändert haben
                }
            }
            if (!earFound) {
                // Sollte bei einem einfachen Polygon nicht passieren
                System.err.println("Kein Ohr gefunden, Algorithmus bricht ab.");
                break;
            }
        }

        // Das letzte verbleibende Dreieck hinzufügen
        if (remainingVertices.size() == 3) {
            triangles.add(remainingVertices);
        }

        return triangles;
    }

    /**
     * Hilfsmethode zur Überprüfung, ob ein Dreieck ein "Ohr" ist.
     * Ein "Ohr" ist ein Dreieck, das:
     * 1. Konvex ist (in Bezug auf die Polygonrichtung).
     * 2. Keinen anderen Polygonpunkt in seinem Inneren enthält.
     */
    private static boolean isEar(Vector2f a, Vector2f b, Vector2f c, List<Vector2f> polygon) {
        // Prüfung auf Konvexität (hier vereinfacht durch Prüfung der Orientierung)
        // Ein Kreuzprodukt kann hier helfen, die Orientierung zu bestimmen.
        if ((b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x) < 0) {
            return false; // Konkave Ecke (bei CCW-Orientierung)
        }

        // Prüfung, ob andere Punkte im Dreieck liegen
        for (Vector2f p : polygon) {
            if (p.equals(a) || p.equals(b) || p.equals(c)) {
                continue;
            }
            if (isPointInTriangle(p, a, b, c)) {
                return false; // Ein anderer Punkt liegt im Dreieck
            }
        }
        return true;
    }

    /**
     * Prüft, ob ein Punkt innerhalb eines Dreiecks liegt.
     */
    private static boolean isPointInTriangle(Vector2f pt, Vector2f v1, Vector2f v2, Vector2f v3) {
        // Baryzentrische Koordinaten-Methode ist hier robust
        float d1, d2, d3;
        boolean has_neg, has_pos;

        d1 = sign(pt, v1, v2);
        d2 = sign(pt, v2, v3);
        d3 = sign(pt, v3, v1);

        has_neg = (d1 < 0) || (d2 < 0) || (d3 < 0);
        has_pos = (d1 > 0) || (d2 > 0) || (d3 > 0);

        return !(has_neg && has_pos);
    }

    private static float sign(Vector2f p1, Vector2f p2, Vector2f p3) {
        return (p1.x - p3.x) * (p2.y - p3.y) - (p2.x - p3.x) * (p1.y - p3.y);
    }
}