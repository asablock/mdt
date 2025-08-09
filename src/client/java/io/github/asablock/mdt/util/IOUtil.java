package io.github.asablock.mdt.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.io.output.WriterOutputStream;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public final class IOUtil {
    private IOUtil() {
    }

    /**
     * This method throws t without t being a checked exception. (Hack)
     * @param t The exception to throw.
     * @return Never returns.
     * @param <X> A compiler hack.
     * @throws X A compiler hack. Compiler will think X is an unchecked exception, so that the actual exception won't be checked.
     */
    @SuppressWarnings("unchecked")
    public static <X extends Throwable> RuntimeException sneakyThrow(Throwable t) throws X {
        Objects.requireNonNull(t);
        throw (X) t;
        // This is a hack to make the compiler happy.
    }

    public static <T> T suppress(Callable<? extends T> callable) {
        try {
            return callable.call();
        } catch (Throwable e) {
            throw sneakyThrow(e); // throw inside sneakyThrow without being checked
        }
    }

    private static ChatHud chatHud;

    public static Consumer<Text> chatAppender() {
        return t -> {
            if (chatHud == null) {
                InGameHud igh = MinecraftClient.getInstance().inGameHud;
                if (igh != null) chatHud = igh.getChatHud();
            }
            if (chatHud != null) {
                chatHud.addMessage(t);
            }
        };
    }

    public static class TextWriter extends Writer {
        private final Consumer<? super MutableText> textConsumer;
        private MutableText lastLine;

        TextWriter(Consumer<? super MutableText> textConsumer) {
            this.textConsumer = textConsumer;
            lastLine = Text.literal("");
        }

        // for CRLF support
        boolean ncr = true;

        protected void newLine() {
            textConsumer.accept(lastLine);
            lastLine = Text.empty();
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

    public static final TextWriter CHAT_WRITER = new TextWriter(chatAppender());
    public static final TextWriter CHAT_ERROR_WRITER = new TextWriter(t -> chatAppender().accept(t.formatted(Formatting.RED)));

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

    public static TextWriter getTextWriter(Consumer<? super MutableText> lineAdder) {
        return new TextWriter(lineAdder);
    }

    public static PrintStream getTextStream(Consumer<? super MutableText> lineAdder) {
        return new PrintStream(suppress(WriterOutputStream.builder().setWriter(getTextWriter(lineAdder)).setCharset(StandardCharsets.UTF_8).setWriteImmediately(true)::get), true, StandardCharsets.UTF_8);
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
        return new PrintStream(newChatOutputStream(), true, StandardCharsets.UTF_8);
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

    /**
     * This class is used to delegate multiple PrintStreams to a single OutputStream.
     */
    public static class MultipleDelegatedPrintStream extends PrintStream {
        private final List<PrintStream> printStreams;
        private final boolean canClose;

        public MultipleDelegatedPrintStream(boolean canClose, PrintStream... streams) {
            super(OutputStream.nullOutputStream(), true, StandardCharsets.UTF_8);
            this.canClose = canClose;
            this.printStreams = new ArrayList<>();
            printStreams.addAll(Arrays.asList(streams));
        }

        public void addStream(PrintStream stream) {
            printStreams.add(stream);
        }

        public void removeStream(PrintStream stream) {
            printStreams.remove(stream);
        }

        @Override
        public void write(int b) {
            for (PrintStream stream : printStreams) {
                stream.write(b);
            }
        }

        @Override
        public void print(boolean b) {
            for (PrintStream stream : printStreams) {
                stream.print(b);
            }
        }

        @Override
        public void print(char c) {
            for (PrintStream stream : printStreams) {
                stream.print(c);
            }
        }

        @Override
        public void print(int i) {
            for (PrintStream stream : printStreams) {
                stream.print(i);
            }
        }

        @Override
        public void print(long l) {
            for (PrintStream stream : printStreams) {
                stream.print(l);
            }
        }

        @Override
        public void print(float f) {
            for (PrintStream stream : printStreams) {
                stream.print(f);
            }
        }

        @Override
        public void print(double d) {
            for (PrintStream stream : printStreams) {
                stream.print(d);
            }
        }

        @Override
        public void print(char[] s) {
            for (PrintStream stream : printStreams) {
                stream.print(s);
            }
        }

        @Override
        public void println() {
            for (PrintStream stream : printStreams) {
                stream.println();
            }
        }

        @Override
        public void println(boolean x) {
            for (PrintStream stream : printStreams) {
                stream.println(x);
            }
        }

        @Override
        public void println(char x) {
            for (PrintStream stream : printStreams) {
                stream.println(x);
            }
        }

        @Override
        public void println(int x) {
            for (PrintStream stream : printStreams) {
                stream.println(x);
            }
        }

        @Override
        public void println(long x) {
            for (PrintStream stream : printStreams) {
                stream.println(x);
            }
        }

        @Override
        public void println(float x) {
            for (PrintStream stream : printStreams) {
                stream.println(x);
            }
        }

        @Override
        public void println(double x) {
            for (PrintStream stream : printStreams) {
                stream.println(x);
            }
        }

        @Override
        public void println(char[] x) {
            for (PrintStream stream : printStreams) {
                stream.println(x);
            }
        }

        @Override
        public void println(Object x) {
            for (PrintStream stream : printStreams) {
                stream.println(x);
            }
        }

        @Override
        public void flush() {
            for (PrintStream stream : printStreams) {
                stream.flush();
            }
        }

        @Override
        public void close() {
            if (canClose) {
                for (PrintStream stream : printStreams) {
                    stream.close();
                }
            }
        }

        @Override
        public void write(byte[] b) throws IOException {
            for (PrintStream stream : printStreams) {
                stream.write(b);
            }
        }

        @Override
        public void write(byte[] buf, int off, int len) {
            for (PrintStream stream : printStreams) {
                stream.write(buf, off, len);
            }
        }

        @Override
        public void print(String s) {
            for (PrintStream stream : printStreams) {
                stream.print(s);
            }
        }

        @Override
        public void print(Object obj) {
            for (PrintStream stream : printStreams) {
                stream.print(obj);
            }
        }

        @Override
        public void println(String x) {
            for (PrintStream stream : printStreams) {
                stream.println(x);
            }
        }

        @Override
        public void writeBytes(byte[] buf) {
            for (PrintStream stream : printStreams) {
                stream.writeBytes(buf);
            }
        }

        @Override
        public PrintStream printf(String format, Object... args) {
            for (PrintStream stream : printStreams) {
                stream.printf(format, args);
            }
            return this;
        }

        @Override
        public PrintStream printf(Locale l, String format, Object... args) {
            for (PrintStream stream : printStreams) {
                stream.printf(l, format, args);
            }
            return this;
        }

        @Override
        public PrintStream format(String format, Object... args) {
            for (PrintStream stream : printStreams) {
                stream.format(format, args);
            }
            return this;
        }

        @Override
        public PrintStream format(Locale l, String format, Object... args) {
            for (PrintStream stream : printStreams) {
                stream.format(l, format, args);
            }
            return this;
        }
    }

    public static MultipleDelegatedPrintStream createMultipleDelegatedPrintStream(boolean canClose, PrintStream... streams) {
        return new MultipleDelegatedPrintStream(canClose, streams);
    }
}
