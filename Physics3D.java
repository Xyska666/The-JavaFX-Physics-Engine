import java.util.List;

public class Physics3D {

    public static final double GRAVITY = 900;
    public static final double AIR_DENSITY = 1.225; // кг/м^3 (уровень моря)
    public static final double DRAG_COEFF_SPHERE = 0.47; // шар

    public static void handleCollisions(List<Ball3D> balls) {

        for (int i = 0; i < balls.size(); i++) {
            for (int j = i + 1; j < balls.size(); j++) {

                Ball3D a = balls.get(i);
                Ball3D b = balls.get(j);

                double dx = b.x - a.x;
                double dy = b.y - a.y;
                double dz = b.z - a.z;

                double dist2 = dx * dx + dy * dy + dz * dz;
                double minDist = a.radius + b.radius;

                if (dist2 < minDist * minDist) {

                    double dist = Math.sqrt(dist2);
                    if (dist == 0) dist = 0.01;

                    double nx = dx / dist;
                    double ny = dy / dist;
                    double nz = dz / dist;

                    double overlap = minDist - dist;

                    a.x -= nx * overlap / 2;
                    a.y -= ny * overlap / 2;
                    a.z -= nz * overlap / 2;

                    b.x += nx * overlap / 2;
                    b.y += ny * overlap / 2;
                    b.z += nz * overlap / 2;

                    double rvx = b.vx - a.vx;
                    double rvy = b.vy - a.vy;
                    double rvz = b.vz - a.vz;

                    double velAlongNormal = rvx * nx + rvy * ny + rvz * nz;
                    if (velAlongNormal > 0) continue;

                    double e = Math.min(a.restitution, b.restitution);

                    double impulse = -(1 + e) * velAlongNormal;
                    impulse /= (1 / a.mass + 1 / b.mass);

                    double impulseX = impulse * nx;
                    double impulseY = impulse * ny;
                    double impulseZ = impulse * nz;

                    a.vx -= impulseX / a.mass;
                    a.vy -= impulseY / a.mass;
                    a.vz -= impulseZ / a.mass;

                    b.vx += impulseX / b.mass;
                    b.vy += impulseY / b.mass;
                    b.vz += impulseZ / b.mass;

                    SoundManager.playBallToBall();
                }
            }
        }
    }
}