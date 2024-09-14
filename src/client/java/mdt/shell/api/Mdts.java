package mdt.shell.api;

import io.github.asablock.mdt.toggle.Toggles;
import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * This class contains several methods to help use JShell.
 */
public final class Mdts {
    private Mdts() {
    }

    public static void immersiveMode(boolean setTo) {
        Toggles.javaShell_immersiveMode.set(setTo);
    }

    public static Class<?> mapcls(String yarnName) {
        try {
            return Class.forName(FabricLoader.getInstance().getMappingResolver().mapClassName("yarn", yarnName));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Field mapfield(String owner, String fname, String descriptor) {
        Class<?> cls = mapcls(owner);
        String expname = FabricLoader.getInstance().getMappingResolver().mapFieldName("yarn", owner, fname, descriptor);
        Field f;
        try {
            f = cls.getField(expname);
        } catch (NoSuchFieldException e) {
            try {
                f = cls.getDeclaredField(expname);
            } catch (NoSuchFieldException ex) {
                throw new RuntimeException(ex);
            }
        }
        f.setAccessible(true);
        return f;
    }

    public static Method mapmethod(String owner, String mname, String descriptor) {
        Class<?> cls = mapcls(owner);
        String expname = FabricLoader.getInstance().getMappingResolver().mapMethodName("yarn", owner, mname, descriptor);
        Method m;
        try {
            m = cls.getMethod(expname);
        } catch (NoSuchMethodException e) {
            try {
                m = cls.getDeclaredMethod(expname);
            } catch (NoSuchMethodException ex) {
                throw new RuntimeException(ex);
            }
        }
        m.setAccessible(true);
        return m;
    }
}
