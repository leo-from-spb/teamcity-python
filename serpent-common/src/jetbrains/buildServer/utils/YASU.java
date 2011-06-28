package jetbrains.buildServer.utils;


import org.jetbrains.annotations.Nullable;

/**
 * Yet Another String Util
 *
 * @author Leonid.Bushuev
 */
public final class YASU
{

    @Nullable
    public static String trimAndNull(final String str)
    {
        if (str == null)
            return null;

        String trimmedStr = str.trim();
        if (trimmedStr.length() == 0)
            return null;

        return trimmedStr;
    }


    public static char firstChar(final @Nullable String str)
    {
        return str != null && str.length() >= 1 ? str.charAt(0) : '\0';
    }

}
