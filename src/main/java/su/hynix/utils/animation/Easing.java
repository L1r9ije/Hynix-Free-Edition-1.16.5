package su.hynix.utils.animation;

@FunctionalInterface
public interface Easing {
    double ease(double value);
}