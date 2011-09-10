package jetbrains.buildServer.utils;


import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

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


    @Nullable
    public static String trimRightAndNull(final String str)
    {
        String rtrimmedStr = StringUtil.trimRight(str);
        if (rtrimmedStr == null || rtrimmedStr.length() == 0)
            return null;

        return rtrimmedStr;
    }





    public static char firstChar(final @Nullable String str)
    {
        return str != null && str.length() >= 1 ? str.charAt(0) : '\0';
    }


    public static String adjustCase(final @NotNull String string, final @NotNull Set<String> strings)
    {
        if (strings.contains(string))
            return string;

        for (String s: strings)
            if (s != null && s.equalsIgnoreCase(string))
                return s;

        return string;
    }

}
