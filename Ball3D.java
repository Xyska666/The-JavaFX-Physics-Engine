import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;

public class Ball3D {
    public BallMaterial materialType;
    public double mass;

    public double restitution = 0.9;


    public double x, y, z;
    public double vx, vy, vz;
    public double radius;

    private double soundCooldown = 0;
    private static final double SOUND_DELAY = 0.15;

    public Sphere sphere;
    private PhongMaterial material;

    public Ball3D(
            double x, double y, double z,
            double vx, double vy, double vz,
            double radius,
            double mass,
            BallMaterial material,
            Color color
    ) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.vx = vx;
        this.vy = vy;
        this.vz = vz;
        this.radius = radius;

        this.materialType = material;

        this.mass = Math.max(1.0, mass);
        this.materialType = material;
        this.restitution = material.restitution;

        sphere = new Sphere(radius);
        PhongMaterial fxMaterial = new PhongMaterial(color);
        sphere.setMaterial(fxMaterial);

        updateModel();
    }

    public void update(double dt) {
        if (soundCooldown > 0) {
            soundCooldown -= dt;
        }

        double gravity = Physics3D.GRAVITY * (1.0 + Math.log10(mass) * 0.15);
        vy += gravity * dt;

        double drag = materialType.airDrag * vy * Math.abs(vy) / mass;
        vy -= drag * dt;

        x += vx * dt;
        y += vy * dt;
        z += vz * dt;

        double half = MainApp.WORLD_SIZE / 2;
        double r = radius;

        if (x - r < -half || x + r > half) {
            vx = -vx * restitution;
            playWallSound();
        }

        if (z - r < -half || z + r > half) {
            vz = -vz * restitution;
            playWallSound();
        }

        if (y + r > half) {
            y = half - r;

            double massDamping = 1.0 / (1.0 + mass / 200.0);

            double bounce = restitution * massDamping;

            vy = -vy * bounce;

            double friction = 1.0 - materialType.friction;
            friction = Math.max(0.6, friction);

            vx *= friction;
            vz *= friction;

            playWallSound();
        }
        updateModel();
    }

    private void playWallSound() {
        if (soundCooldown <= 0) {
            SoundManager.playWall();
            soundCooldown = SOUND_DELAY;
        }
    }

    private void updateModel() {
        sphere.setTranslateX(x);
        sphere.setTranslateY(y);
        sphere.setTranslateZ(z);
    }
}