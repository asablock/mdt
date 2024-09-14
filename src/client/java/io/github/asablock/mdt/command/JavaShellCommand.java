package io.github.asablock.mdt.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.asablock.mdt.IOUtil;
import io.github.asablock.mdt.toggle.Toggles;
import jdk.jshell.tool.JavaShellToolBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.io.input.ReaderInputStream;

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class JavaShellCommand {
    private static StringBufferReader chatReader = new StringBufferReader();
    private static final InputStream CHAT_INPUT_STREAM = IOUtil.suppress(ReaderInputStream.builder().setCharset(StandardCharsets.UTF_8).setReader(chatReader)::get);
    private static final JavaShellToolBuilder JAVA_SHELL_TOOL_BUILDER =
            JavaShellToolBuilder.builder().in(CHAT_INPUT_STREAM, null).out(IOUtil.getChatPrintStream());
    public static volatile boolean javaShellRunning = false;

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("mjavashell")
                .then(literal("exec").then(argument("code", StringArgumentType.greedyString()).executes(JavaShellCommand::executeExec)))
                .then(literal("restart").executes(JavaShellCommand::executeRestart))
                .then(literal("status").executes(JavaShellCommand::executeStatus)));
    }

    private static int executeExec(CommandContext<FabricClientCommandSource> context) {
        if (checkEnabled(context)) return 0;
        if (!javaShellRunning) {
            tryStartShell(context);
        }
        chatReader.append(StringArgumentType.getString(context, "code") + "\n");
        return 1;
    }

    private static int executeRestart(CommandContext<FabricClientCommandSource> context) {
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

    private static void tryStartShell(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Text.translatable("command.mdt.javashell.starting"));
        chatReader = new StringBufferReader();
        Thread t = new Thread(() -> {
            try {
                int exitCode = JAVA_SHELL_TOOL_BUILDER.start();
                javaShellRunning = false;
                IOUtil.getChatHud().addMessage(Text.translatable("command.mdt.javashell.exit", exitCode));
            } catch (Exception e) {
                IOUtil.getChatHud().addMessage(Text.translatable("command.mdt.javashell.exception").formatted(Formatting.RED));
                e.printStackTrace(IOUtil.getChatErrorPrintWriter());
            }
        });
        t.setDaemon(true);
        t.start();
        javaShellRunning = true;
    }

    private static class StringBufferReader extends Reader {
        private final StringBuffer sb;

        StringBufferReader() {
            this.sb = new StringBuffer();
        }

        public void append(CharSequence csq) {
            synchronized (lock) {
                sb.append(csq);
                lock.notifyAll();
            }
        }

        public void append(char ch) {
            synchronized (lock) {
                sb.append(ch);
                lock.notifyAll();
            }
        }

        // blocking
        @Override
        public int read(char[] cbuf, int off, int len) {
            synchronized (lock) {
                int read = 0;
                while (read < len) {
                    while (!sb.isEmpty()) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    cbuf[off + read] = sb.charAt(sb.length() - 1);
                    sb.deleteCharAt(sb.length() - 1);
                    read++;
                }
                return read;
            }
        }

        @Override
        public void close() {
        }
    }
}
