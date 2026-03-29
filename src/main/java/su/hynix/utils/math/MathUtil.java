package su.hynix.utils.math;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import su.hynix.utils.Wrapper;
import su.hynix.utils.misc.FrameRateUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MathUtil implements Wrapper {

    public static double interpolate(double start, double end, double factor) {
        return start + (end - start) * factor;
    }

    public static float interpolate(float start, float end, double factor) {
        return (float) ((double) start + (double) (end - start) * factor);
    }

    public static int interpolate(int start, int end, double factor) {
        return (int) ((double) start + (double) (end - start) * factor);
    }

    public static float calculateGaussian(float x, float sigma) {
        double PI = Math.PI;
        double output = (double) 1.0F / Math.sqrt((double) 2.0F * PI * (double) (sigma * sigma));
        return (float) (output * Math.exp((double) (-(x * x)) / ((double) 2.0F * (double) (sigma * sigma))));
    }

    public static Vector3d interpolate(Vector3d end, Vector3d start, float multiple) {
        return new Vector3d(
                interpolate(end.getX(), start.getX(), multiple),
                interpolate(end.getY(), start.getY(), multiple),
                interpolate(end.getZ(), start.getZ(), multiple));
    }

    public static float interpolate(float current, float old, float scale) {
        return old + (current - old) * scale;
    }

    public static float lerp(float end, float start, float multiple) {
        return (float) (end + (start - end) * MathHelper.clamp(deltaTime() * multiple, 0, 1));
    }

    public static double lerp(double end, double start, double multiple) {
        return (end + (start - end) * MathHelper.clamp(deltaTime() * multiple, 0, 1));
    }

    public static boolean isHovered(float mouseX, float mouseY, float x, float y, float width, float height) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }

    public static Vector2f rotationToEntity(Entity target) {
        Vector3d vector3d = target.getPositionVec().subtract(Minecraft.getInstance().player.getPositionVec());
        double magnitude = Math.hypot(vector3d.x, vector3d.z);
        return new Vector2f(
                (float) Math.toDegrees(Math.atan2(vector3d.z, vector3d.x)) - 90.0F,
                (float) (-Math.toDegrees(Math.atan2(vector3d.y, magnitude))));
    }

    public static double round(double num, double increment) {
        double v = (double) Math.round(num / increment) * increment;
        BigDecimal bd = new BigDecimal(v);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static float round(float num, float increment) {
        float v = (float) Math.round(num / increment) * increment;
        BigDecimal bd = BigDecimal.valueOf(v);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.floatValue();
    }

    public static float fast(float end, float start, float multiple) {
        return (1 - MathHelper.clamp((float) (deltaTime() * multiple), 0, 1)) * end
                + MathHelper.clamp((float) (deltaTime() * multiple), 0, 1) * start;
    }

    public static Vector3d fast(Vector3d end, Vector3d start, float multiple) {
        return new Vector3d(
                fast((float) end.getX(), (float) start.getX(), multiple),
                fast((float) end.getY(), (float) start.getY(), multiple),
                fast((float) end.getZ(), (float) start.getZ(), multiple));
    }

    public static double step(double value, double steps) {
        double roundedValue = Math.round(value / steps) * steps;
        return Math.round(roundedValue * 100.0) / 100.0;
    }

    public static float random(float min, float max) {
        return (float) (Math.random() * (double) (max - min) + (double) min);
    }

    public static double deltaTime() {
        return FrameRateUtil.getFps() > 5 ? (1f / FrameRateUtil.getFps()) : 0.016f;
    }

    public static Vector3d interpolate(Entity entity, float partialTicks) {
        double posX = Vector3d.lerp(entity.lastTickPosX, entity.getPosX(), partialTicks);
        double posY = Vector3d.lerp(entity.lastTickPosY, entity.getPosY(), partialTicks);
        double posZ = Vector3d.lerp(entity.lastTickPosZ, entity.getPosZ(), partialTicks);
        return new Vector3d(posX, posY, posZ);
    }
}
