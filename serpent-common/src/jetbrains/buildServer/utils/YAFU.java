package jetbrains.buildServer.utils;

import com.sun.istack.internal.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Yet Another File Utils.
 *
 * @author Leonid.Bushuev
 */
public final class YAFU
{


    public static void writeTextFile(final @NotNull File file, final @NotNull String text)
            throws IOException
    {
        Writer writer = new FileWriter(file);
        try
        {
            writer.append(text);
        }
        finally
        {
            writer.close();
        }
    }


}
