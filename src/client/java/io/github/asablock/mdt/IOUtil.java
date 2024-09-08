package io.github.asablock.mdt;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.Writer;
import java.text.Normalizer;
import java.util.function.Consumer;

public final class IOUtil {
    private IOUtil() {
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

    public static TextWriter getChatWriter() {
        return CHAT_WRITER;
    }

    public static TextWriter getChatErrorWriter() {
        return CHAT_ERROR_WRITER;
    }

    public static TextWriter getTextWriter(Consumer<MutableText> lineAdder) {
        return new TextWriter(lineAdder);
    }
}
