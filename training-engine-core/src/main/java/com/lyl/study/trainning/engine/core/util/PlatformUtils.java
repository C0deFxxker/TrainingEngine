package com.lyl.study.trainning.engine.core.util;

import sun.misc.Unsafe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 平台相关的工具类
 *
 * @author liyilin
 */
public final class PlatformUtils {
    private static final boolean IS_ANDROID = isAndroid0();
    private static final boolean IS_WINDOWS = isWindows0();

    private static final boolean IS_ROOT = isRoot0();

    private static final int JAVA_VERSION = javaVersion0();

    private static final int BIT_MODE = bitMode0();
    private static final int ADDRESS_SIZE = addressSize0();
    private static final Unsafe UNSAFE;

    static {
        ByteBuffer direct = ByteBuffer.allocateDirect(1);
        Field addressField;
        try {
            addressField = Buffer.class.getDeclaredField("address");
            addressField.setAccessible(true);
            if (addressField.getLong(ByteBuffer.allocate(1)) != 0) {
                // A heap buffer must have 0 address.
                addressField = null;
            } else {
                if (addressField.getLong(direct) == 0) {
                    // A direct buffer must have non-zero address.
                    addressField = null;
                }
            }
        } catch (Throwable t) {
            // Failed to access the address field.
            addressField = null;
        }

        Unsafe unsafe;
        if (addressField != null) {
            try {
                Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
                unsafeField.setAccessible(true);
                unsafe = (Unsafe) unsafeField.get(null);

                // Ensure the unsafe supports all necessary methods to work around the mistake in the latest OpenJDK.
                // https://github.com/netty/netty/issues/1061
                // http://www.mail-archive.com/jdk6-dev@openjdk.java.net/msg00698.html
                if (unsafe != null) {
                    unsafe.getClass().getDeclaredMethod(
                            "copyMemory", Object.class, long.class, Object.class, long.class, long.class);
                }
            } catch (Throwable cause) {
                // Unsafe.copyMemory(Object, long, Object, long, long) unavailable.
                unsafe = null;
            }
        } else {
            // If we cannot access the address of a direct buffer, there's no point of using unsafe.
            // Let's just pretend unsafe is unavailable for overall simplicity.
            unsafe = null;
        }

        UNSAFE = unsafe;
    }

    private static boolean isWindows0() {
        return System.getProperty("os.name", "").toLowerCase(Locale.US).contains("win");
    }

    private static boolean isAndroid0() {
        boolean android;
        try {
            Class.forName("android.app.Application", false, getSystemClassLoader());
            android = true;
        } catch (Exception e) {
            // Failed to load the class uniquely available in Android.
            android = false;
        }
        return android;
    }

    private static boolean isRoot0() {
        if (isWindows()) {
            return false;
        }

        String[] ID_COMMANDS = {"/usr/bin/id", "/bin/id", "/usr/xpg4/bin/id", "id"};
        Pattern UID_PATTERN = Pattern.compile("^(?:0|[1-9][0-9]*)$");
        for (String idCmd : ID_COMMANDS) {
            Process p = null;
            BufferedReader in = null;
            String uid = null;
            try {
                p = Runtime.getRuntime().exec(new String[]{idCmd, "-u"});
                in = new BufferedReader(new InputStreamReader(p.getInputStream(), Charset.forName("US-ASCII")));
                uid = in.readLine();
                in.close();

                for (; ; ) {
                    try {
                        int exitCode = p.waitFor();
                        if (exitCode != 0) {
                            uid = null;
                        }
                        break;
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                }
            } catch (Exception e) {
                // Failed to run the command.
                uid = null;
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // Ignore
                    }
                }
                if (p != null) {
                    try {
                        p.destroy();
                    } catch (Exception e) {
                        // Android sometimes triggers an ErrnoException.
                    }
                }
            }

            if (uid != null && UID_PATTERN.matcher(uid).matches()) {
                return "0".equals(uid);
            }
        }

        Pattern PERMISSION_DENIED = Pattern.compile(".*(?:denied|not.*permitted).*");
        for (int i = 1023; i > 0; i--) {
            ServerSocket ss = null;
            try {
                ss = new ServerSocket();
                ss.setReuseAddress(true);
                ss.bind(new InetSocketAddress(i));
                return true;
            } catch (Exception e) {
                // Failed to bind.
                // Check the error message so that we don't always need to bind 1023 times.
                String message = e.getMessage();
                if (message == null) {
                    message = "";
                }
                message = message.toLowerCase();
                if (PERMISSION_DENIED.matcher(message).matches()) {
                    break;
                }
            } finally {
                if (ss != null) {
                    try {
                        ss.close();
                    } catch (Exception e) {
                        // Ignore.
                    }
                }
            }
        }

        return false;
    }

    @SuppressWarnings("LoopStatementThatDoesntLoop")
    private static int javaVersion0() {
        int javaVersion;

        // Not really a loop
        for (; ; ) {
            // Android
            if (isAndroid()) {
                javaVersion = 6;
                break;
            }

            try {
                Class.forName("java.time.Clock", false, getClassLoader(Object.class));
                javaVersion = 8;
                break;
            } catch (Exception e) {
                // Ignore
            }

            try {
                Class.forName("java.util.concurrent.LinkedTransferQueue", false, getClassLoader(BlockingQueue.class));
                javaVersion = 7;
                break;
            } catch (Exception e) {
                // Ignore
            }

            javaVersion = 6;
            break;
        }

        return javaVersion;
    }

    private static int bitMode0() {
        int bitMode;

        // And then the vendor specific ones which is probably most reliable.
        bitMode = SystemPropertyUtils.getInt("sun.arch.data.model", 0);
        if (bitMode > 0) {
            return bitMode;
        }
        bitMode = SystemPropertyUtils.getInt("com.ibm.vm.bitmode", 0);
        if (bitMode > 0) {
            return bitMode;
        }

        // os.arch also gives us a good hint.
//        String arch = SystemPropertyUtils.get("os.arch", "").toLowerCase(Locale.US).trim();
//        if ("amd64".equals(arch) || "x86_64".equals(arch)) {
//            return 64;
//        } else if ("i386".equals(arch) || "i486".equals(arch) || "i586".equals(arch) || "i686".equals(arch)) {
//            return 32;
//        }

        // Last resort: guess from VM name and then fall back to most common 64-bit mode.
        String vm = SystemPropertyUtils.get("java.vm.name", "").toLowerCase(Locale.US);
        Pattern BIT_PATTERN = Pattern.compile("([1-9][0-9]+)-?bit");
        Matcher m = BIT_PATTERN.matcher(vm);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        } else {
            return 64;
        }
    }

    private static int addressSize0() {
        if (!hasUnsafe()) {
            return -1;
        }
        return UNSAFE.addressSize();
    }

    /**
     * @return 如果当前是Windows系统则返回 {@code true}，否则返回 {@code false}
     */
    public static boolean isWindows() {
        return IS_WINDOWS;
    }

    /**
     * @return 如果当前是安卓系统则返回 {@code true}，否则返回 {@code false}
     */
    public static boolean isAndroid() {
        return IS_ANDROID;
    }

    /**
     * @return 如果当前运行用户是root则返回 {@code true}，否则返回 {@code false}
     */
    public static boolean isRoot() {
        return IS_ROOT;
    }

    /**
     * @return 当前JVM的Java版本号（例如：6，7，8）
     */
    public static int javaVersion() {
        return JAVA_VERSION;
    }

    /**
     * @return 当前Jvm的位数（通常是32或64）
     */
    public static int getBitMode() {
        return BIT_MODE;
    }

    public static boolean hasUnsafe() {
        return UNSAFE != null;
    }

    /**
     * @return 当前操作系统位数（通常是32或64）
     */
    public static int getAddressSize() {
        return ADDRESS_SIZE;
    }

    /**
     * 获取系统级ClassLoader
     *
     * @return 系统级ClassLoader
     */
    public static ClassLoader getSystemClassLoader() {
        if (System.getSecurityManager() == null) {
            return ClassLoader.getSystemClassLoader();
        } else {
            return AccessController.doPrivileged((PrivilegedAction<ClassLoader>) ClassLoader::getSystemClassLoader);
        }
    }

    /**
     * 获取加载指定Java类的ClassLoader
     *
     * @param clazz 指定Java类
     * @return 加载指定Java类的ClassLoader
     */
    public static ClassLoader getClassLoader(Class<?> clazz) {
        if (System.getSecurityManager() == null) {
            return clazz.getClassLoader();
        } else {
            return AccessController.doPrivileged((PrivilegedAction<ClassLoader>) clazz::getClassLoader);
        }
    }

    private PlatformUtils() {
        // Unused
    }
}
