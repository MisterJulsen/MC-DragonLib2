package de.mrjulsen.mcdragonlib.client.model.mesh;

public enum Rotation {
    R0(0, 0),
    R90(90, 1),
    R180(180, 2),
    R270(270, 3);

    private final int degrees;
    private final int iterations;

    private Rotation(int degrees, int iterations) {
        this.degrees = degrees;
        this.iterations = iterations;
    }

    public int getDegrees() {
        return degrees;
    }

    public int getIterations() {
        return iterations;
    }
}
