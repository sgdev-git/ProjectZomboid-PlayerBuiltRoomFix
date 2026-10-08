package io.shaun.playerbuiltroomfix;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Loads the real 42.21.0 target classes after ZombieBuddy transforms them.
 * This catches linkage, nested-class visibility, and verifier failures that a
 * signature-only compile stub cannot expose.
 */
public final class ActualGameClassLoadSmokeTest {
    private ActualGameClassLoadSmokeTest() {
    }

    public static void main(String[] args) throws Exception {
        assertMethod(
            "zombie.iso.objects.IsoTree",
            "isPlayerInsideARoom",
            "zombie.characters.IsoPlayer"
        );
        assertMethod(
            "zombie.iso.fboRenderChunk.FBORenderCutaways",
            "IsCutawaySquare",
            "zombie.iso.fboRenderChunk.FBORenderCutaways$CutawayWall",
            "zombie.iso.IsoGridSquare",
            "zombie.iso.IsoGridSquare",
            "long"
        );
        System.out.println("ActualGameClassLoadSmokeTest: PASS");
    }

    private static void assertMethod(String className, String methodName, String... parameterNames)
        throws Exception {
        ClassLoader loader = ActualGameClassLoadSmokeTest.class.getClassLoader();
        Class<?> target = Class.forName(className, false, loader);

        for (Method method : target.getDeclaredMethods()) {
            if (!method.getName().equals(methodName) || method.getReturnType() != boolean.class) {
                continue;
            }

            String[] actualParameters = Arrays.stream(method.getParameterTypes())
                .map(Class::getName)
                .toArray(String[]::new);
            if (Arrays.equals(actualParameters, parameterNames)) {
                return;
            }
        }

        throw new AssertionError(
            "Expected method was not loadable after transformation: "
                + className + "." + methodName + Arrays.toString(parameterNames)
        );
    }
}
