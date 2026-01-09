public enum BallMaterial {

    METAL   ("Металл",   0.20, 0.15, 0.002),
    WOOD    ("Дерево",   0.55, 0.30, 0.004),
    PLASTIC ("Пластик",  0.75, 0.25, 0.003),
    RUBBER  ("Резина",   0.92, 0.80, 0.001),
    STONE   ("Камень",   0.40, 0.20, 0.002);

    public final String title;
    public final double restitution;
    public final double friction;
    public final double airDrag;

    BallMaterial(String title,
                 double restitution,
                 double friction,
                 double airDrag) {
        this.title = title;
        this.restitution = restitution;
        this.friction = friction;
        this.airDrag = airDrag;
    }

    @Override
    public String toString() {
        return title;
    }
}