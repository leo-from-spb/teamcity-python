package jetbrains.buildServer.utils;

import org.jetbrains.annotations.NotNull;

import java.io.*;

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
