package grapefruit.command.runtime.util;

public final class PrimitivesUtil {
    private PrimitivesUtil() {}

    public static byte safeByte(Byte value) {
        return value == null ? (byte) 0 : value;
    }

    public static short safeShort(Short value) {
        return value == null ? (short) 0 : value;
    }

    public static int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    public static long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    public static float safeFloat(Float value) {
        return value == null ? 0.0F : value;
    }

    public static double safeDouble(Double value) {
        return value == null ? 0.0D : value;
    }

    public static char safeChar(Character value) {
        return value == null ? ' ' : value;
    }

    public static boolean safeBoolean(Boolean value) {
        return value != null && value;
    }
}
