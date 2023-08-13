import de.articdive.jnoise.generators.noise_parameters.fade_functions.FadeFunction;
import de.articdive.jnoise.generators.noise_parameters.interpolation.Interpolation;
import de.articdive.jnoise.pipeline.JNoise;

public class Noise {
    public static JNoise noisePipeline= JNoise.newBuilder().perlin(1077, Interpolation.LINEAR, FadeFunction.IMPROVED_PERLIN_NOISE)
            .scale(1 / 16.0)
            .addModifier(v -> (v + 1) / 2.0)
            .clamp(0.0, 1.0)
            .build();
    public static void main(String[] args) {
        System.out.println(noisePipeline.evaluateNoise(50.0));
    }
}
