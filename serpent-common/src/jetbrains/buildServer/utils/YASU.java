package jetbrains.buildServer.utils;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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


    /**
     * Removes the right (end) spaces from the string.
     * @param str  string to trim; nulls and empty strings are allowed.
     * @return        the string without right spaces, or null if null given.
     */
    @Nullable
    public static String trimRight(final @Nullable String str) {
        if (str == null)
            return null;

        int n = str.length();
        if (n == 0 || !Character.isWhitespace(str.charAt(n-1)))
            return str;

        StringBuilder buf = new StringBuilder(str);
        while (buf.length() > 0 && Character.isWhitespace(buf.charAt(buf.length()-1)))
            buf.delete(buf.length() - 1, buf.length());

        return buf.toString();
    }


    @Nullable
    public static String trimRightAndNull(final @NotNull String str)
    {
        String rtrimmedStr = trimRight(str);
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


    @NotNull
    public static List<String> split(final @Nullable String str, final char separator, boolean trim, boolean excludeEmpty)
    {
        if (str == null) return Collections.emptyList();
        int n = str.length();
        ArrayList<String> result = new ArrayList<String>();
        int p1 = 0, i = 0;
        while (i <= n) {
            boolean sep = i < n && str.charAt(i) == separator || i == n;
            if (sep) {
                String item = str.substring(p1, i);
                if (trim) item = item.trim();
                if (!item.isEmpty() || !excludeEmpty) result.add(item);
                p1 = i + 1;
            }
            i++;
        }
        return result.isEmpty() ? Collections.<String>emptyList() : result;
    }
}
