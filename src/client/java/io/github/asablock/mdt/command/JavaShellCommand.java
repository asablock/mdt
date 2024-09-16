package io.github.asablock.mdt.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.asablock.mdt.IOUtil;
import io.github.asablock.mdt.Mdt;
import io.github.asablock.mdt.toggle.Toggles;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.io.input.ReaderInputStream;

import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class JavaShellCommand {
    private static IOUtil.StringBufferReader chatReader;
    private static InputStream chatInputStream;
    private static Method methodBuilder;
    private static Method methodIn;
    private static Method methodOut;
    private static Method methodStart;
    private static Throwable initThrowable;
    private static final String[] EMPTY_STRING_ARGUMENTS = {};
    public static volatile boolean javaShellRunning = false;

    static {
        try {
            initThrowable = null;
            Class<?> clazz = Class.forName("jdk.jshell.tool.JavaShellToolBuilder");
            methodBuilder = clazz.getDeclaredMethod("builder");
            methodIn = clazz.getDeclaredMethod("in", InputStream.class, InputStream.class);
            methodOut = clazz.getDeclaredMethod("out", PrintStream.class);
            methodStart = clazz.getDeclaredMethod("start", String[].class);
        } catch (Throwable e) {
            initThrowable = e;
            Mdt.LOGGER.error("Cannot find Java Shell!", e);
        }
    }

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("mjavashell")
                .then(literal("exec").then(argument("code", StringArgumentType.greedyString()).executes(JavaShellCommand::executeExec)))
                .then(literal("restart").executes(JavaShellCommand::executeRestart))
                .then(literal("status").executes(JavaShellCommand::executeStatus)));
    }

    private static int executeExec(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        if (checkEnabled(context)) return 0;
        if (!javaShellRunning) {
            tryStartShell(context);
        }
        appendLine(StringArgumentType.getString(context, "code"));
        return 1;
    }

    public static void appendLine(String line) {
        chatReader.append(line + '\n');
    }

    private static int executeRestart(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        if (checkEnabled(context)) return 0;
        if (javaShellRunning) {
            chatReader.append("/exit\n"); // cause JShell to exit
            javaShellRunning = false;
        }
        tryStartShell(context);
        return 1;
    }

    private static int executeStatus(CommandContext<FabricClientCommandSource> context) {
        checkEnabled(context);
        boolean bl = javaShellRunning;
        context.getSource().sendFeedback(Text.translatable(bl ? "command.mdt.javashell.status_running" : "command.mdt.javashell.status_not_started"));
        return bl ? 1 : 0;
    }

    private static boolean checkEnabled(CommandContext<FabricClientCommandSource> context) {
        if (!Toggles.javaShell_enabled.get()) {
            context.getSource().sendFeedback(Text.translatable("command.mdt.javashell.warning"));
            return true;
        }
        return false;
    }

    private static void tryStartShell(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        context.getSource().sendFeedback(Text.translatable("command.mdt.javashell.starting"));
        chatReader = new IOUtil.StringBufferReader();
        chatInputStream = IOUtil.suppress(ReaderInputStream.builder().setCharset(StandardCharsets.UTF_8).setReader(chatReader)::get);
        createJavaShellThread(newBuilderInstance(context)).start();
        javaShellRunning = true;
    }

    private static Thread createJavaShellThread(final Object builder) {
        Thread t = new Thread(() -> {
            try {
                int exitCode = (Integer) methodStart.invoke(builder, (Object) EMPTY_STRING_ARGUMENTS);
                javaShellRunning = false;
                IOUtil.getChatHud().addMessage(Text.translatable("command.mdt.javashell.exit", exitCode));
            } catch (Exception e) {
                IOUtil.getChatHud().addMessage(Text.translatable("command.mdt.javashell.exception").formatted(Formatting.RED));
                e.printStackTrace(IOUtil.CHAT_ERROR_PRINT_WRITER);
            }
        }, "Java Shell");
        t.setDaemon(true);
        return t;
    }

    private static Object newBuilderInstance(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        if (initThrowable == null) {
            try {
                Object o = methodBuilder.invoke(null);
                methodIn.invoke(o, chatInputStream, null);
                methodOut.invoke(o, IOUtil.newChatPrintStream());
                return o;
            } catch (Throwable e) {
                context.getSource().sendError(Text.translatable("command.mdt.javashell.unavailable"));
                e.printStackTrace(IOUtil.CHAT_ERROR_PRINT_WRITER);
                throw Mdt.LINE_SEPARATOR_EXCEPTION.create();
            }
        } else {
            context.getSource().sendError(Text.translatable("command.mdt.javashell.unavailable"));
            initThrowable.printStackTrace(IOUtil.CHAT_ERROR_PRINT_WRITER);
            throw Mdt.LINE_SEPARATOR_EXCEPTION.create();
        }
    }
}
