import javafx.scene.media.AudioClip;

public class SoundManager {

    private static AudioClip click;
    private static AudioClip hover;
    private static AudioClip bounceWall;
    private static AudioClip ballToBall;

    public static void init() {
        click = load("/sounds/Click.wav");
        hover = load("/sounds/Hover.wav");
        bounceWall = load("/sounds/BounceWall.wav");
        ballToBall = load("/sounds/BallToBall.wav");

        bounceWall.setVolume(0.5);
        ballToBall.setVolume(0.4);
    }

    private static AudioClip load(String path) {
        return new AudioClip(
                SoundManager.class.getResource(path).toExternalForm()
        );
    }

    public static void playClick() {
        if (click != null) click.play();
    }

    public static void playHover() {
        if (hover != null) hover.play();
    }

    public static void playWall() {
        if (bounceWall != null) bounceWall.play();
    }

    public static void playBallToBall() {
        if (ballToBall != null) ballToBall.play();
    }
}