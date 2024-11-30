package com.arcanewarrior.misc;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Unfortunately our negative space font can't output unicode characters directly, they end up double escaped, which is not what we want
 * <p></p>
 * It's a <a href="https://github.com/google/gson/issues/388">GSON Issue</a>
 * <p></p>
 * So, this class is our workaround. We overwrite the normal writing method to see if we have any \\ in our buffer, and if so, we skip one of the backslashes
 */
public class UnicodeWorkaroundWriter extends Writer {
    private final Writer out;

    public UnicodeWorkaroundWriter(@NotNull Path output) throws IOException {
        this.out = Files.newBufferedWriter(output);
    }

    @Override
    public void write(char @NotNull [] cbuf, int off, int len) throws IOException {
        for (int i = 0; i < len; i++) {
            char c = cbuf[i + off];
            if (i + 1 < len) {
                char nextChar = cbuf[i + off + 1];
                if (c == '\\' && nextChar == '\\') {
                    continue;
                }
            }
            out.write(c);
        }
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }
}
