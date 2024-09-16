package io.github.asablock.mdt;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.io.output.WriterOutputStream;

import java.io.*;
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
        private MutableText lastLine;

        TextWriter(Consumer<MutableText> textConsumer) {
            this.textConsumer = textConsumer;
            newLine();
        }

        boolean ncr = true;

        protected void newLine() {
            lastLine = Text.empty();
            textConsumer.accept(lastLine);
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
                    lastLine.append(Character.toString(ch));
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

        @Override
        public void flush() {
            // Unnecessary for TextWriter
        }

        @Override
        public void close() {
            // TextWriter is uncloseable
        }
    }

    public static final TextWriter CHAT_WRITER = new TextWriter(CHAT_HUD::addMessage);
    public static final TextWriter CHAT_ERROR_WRITER = new TextWriter(t -> CHAT_HUD.addMessage(t.formatted(Formatting.RED)));

    // These are common instances. Don't close them.
    public static final OutputStream CHAT_OUTPUT_STREAM = newChatOutputStream();
    public static final OutputStream CHAT_ERROR_OUTPUT_STREAM = newChatErrorOutputStream();
    public static final PrintStream CHAT_PRINT_STREAM = newChatPrintStream();
    public static final PrintWriter CHAT_PRINT_WRITER = newChatPrintWriter();
    public static final PrintStream CHAT_ERROR_PRINT_STREAM = newChatErrorPrintStream();
    public static final PrintWriter CHAT_ERROR_PRINT_WRITER = newChatErrorPrintWriter();

    public static PrintWriter newChatErrorPrintWriter() {
        return new PrintWriter(CHAT_ERROR_WRITER, true);
    }

    public static TextWriter getTextWriter(Consumer<MutableText> lineAdder) {
        return new TextWriter(lineAdder);
    }

    public static OutputStream newChatOutputStream() {
        return suppress(WriterOutputStream.builder()
                .setCharset(StandardCharsets.UTF_8).setWriter(CHAT_WRITER).setWriteImmediately(true)::get);
    }

    public static OutputStream newChatErrorOutputStream() {
        return suppress(WriterOutputStream.builder()
                .setCharset(StandardCharsets.UTF_8).setWriter(CHAT_ERROR_WRITER).setWriteImmediately(true)::get);
    }

    public static PrintWriter newChatPrintWriter() {
        return new PrintWriter(CHAT_WRITER, true);
    }

    public static PrintStream newChatPrintStream() {
        return new PrintStream(newChatOutputStream(), true);
    }

    public static PrintStream newChatErrorPrintStream() {
        return new PrintStream(newChatErrorOutputStream(), true, StandardCharsets.UTF_8);
    }

    public static class StringBufferReader extends Reader {
        private final StringBuffer sb;

        public StringBufferReader() {
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
                    while (sb.isEmpty()) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    char ch = sb.charAt(0);
                    cbuf[off + read] = ch;
                    sb.deleteCharAt(0);
                    read++;
                    if (ch == '\n') break;
                }
                return read;
            }
        }

        @Override
        public void close() {
        }
    }
}
