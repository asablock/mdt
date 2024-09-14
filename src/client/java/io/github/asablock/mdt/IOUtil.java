package io.github.asablock.mdt;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.io.output.WriterOutputStream;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public final class IOUtil {
    private IOUtil() {
    }

    public static <T> T suppress(Callable<? extends T> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final ChatHud CHAT_HUD = MinecraftClient.getInstance().inGameHud.getChatHud();

    public static ChatHud getChatHud() {
        return CHAT_HUD;
    }

    public static class TextWriter extends Writer {
        private final Consumer<MutableText> textConsumer;

        TextWriter(Consumer<MutableText> textConsumer) {
            this.textConsumer = textConsumer;
            this.lastLine = new StringBuffer();
        }

        final StringBuffer lastLine;
        boolean ncr = true;

        protected void newLine() {
            textConsumer.accept(Text.literal(lastLine.toString()));
            lastLine.setLength(0);
        }

        @Override
        public void write(int c) {
            char ch = (char) c;
            if (ch == '\r') {
                ncr = false;
                newLine();
            } else {
                if (ch == '\n') {
                    if (ncr) newLine();
                    else ncr = true;
                } else {
                    ncr = true;
                    lastLine.append(ch);
                }
            }
        }

        @Override
        public void write(char[] cbuf, int off, int len) {
            int offlen = off + len;
            for (int i = off; i < offlen; i++) {
                write(cbuf[i]);
            }
        }

        /**
         * Force a new line.
         */
        @Override
        public void flush() {
            newLine();
        }

        @Override
        public void close() {
            // TextWriter is uncloseable
        }
    }

    private static final TextWriter CHAT_WRITER = new TextWriter(CHAT_HUD::addMessage);
    private static final TextWriter CHAT_ERROR_WRITER = new TextWriter(t -> CHAT_HUD.addMessage(t.formatted(Formatting.RED)));
    private static final OutputStream CHAT_OUTPUT_STREAM = suppress(WriterOutputStream.builder()
            .setCharset(StandardCharsets.UTF_8).setWriter(CHAT_WRITER).setWriteImmediately(true)::get);
    private static final OutputStream CHAT_ERROR_OUTPUT_STREAM = suppress(WriterOutputStream.builder()
            .setCharset(StandardCharsets.UTF_8).setWriter(CHAT_ERROR_WRITER).setWriteImmediately(true)::get);
    private static final PrintStream CHAT_PRINT_STREAM = new PrintStream(CHAT_OUTPUT_STREAM, true);
    private static final PrintWriter CHAT_PRINT_WRITER = new PrintWriter(CHAT_WRITER, true);
    private static final PrintWriter CHAT_ERROR_PRINT_STREAM = new PrintWriter(CHAT_ERROR_OUTPUT_STREAM, true);
    private static final PrintWriter CHAT_ERROR_PRINT_WRITER = new PrintWriter(CHAT_ERROR_WRITER, true);

    public static PrintWriter getChatErrorPrintWriter() {
        return CHAT_ERROR_PRINT_WRITER;
    }

    public static TextWriter getTextWriter(Consumer<MutableText> lineAdder) {
        return new TextWriter(lineAdder);
    }

    public static OutputStream getChatOutputStream() {
        return CHAT_OUTPUT_STREAM;
    }

    public static OutputStream getChatErrorOutputStream() {
        return CHAT_ERROR_OUTPUT_STREAM;
    }

    public static PrintWriter getChatPrintWriter() {
        return CHAT_PRINT_WRITER;
    }

    public static PrintStream getChatPrintStream() {
        return CHAT_PRINT_STREAM;
    }

    public static PrintWriter getChatErrorPrintStream() {
        return CHAT_ERROR_PRINT_STREAM;
    }

}
