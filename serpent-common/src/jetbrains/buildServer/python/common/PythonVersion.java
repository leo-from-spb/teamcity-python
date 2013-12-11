package jetbrains.buildServer.python.common;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Version number of the python.
 * @author Leonid Bushuev from JetBrains
 */
public final class PythonVersion
    implements Comparable<PythonVersion>
{
    /**
     * Major version part.
     */
    public final int major;

    /**
     * Minor version part.
     */
    public final int minor;

    /**
     * String representation of the version number.
     */
    @NotNull
    public final String string;



    private static final Pattern pattern =
            Pattern.compile("(\\d+)\\.(\\d+)");


    public PythonVersion(final @NotNull String string)
    {
        this.string = string;

        Matcher m = pattern.matcher(string);
        if (m.find())
        {
            major = Integer.parseInt(m.group(1));
            minor = Integer.parseInt(m.group(2));
        }
        else
        {
            throw new IllegalArgumentException("Could not parse the python version: " + string);
        }
    }


    public PythonVersion(int major, int minor, @NotNull String string)
    {
        this.major = major;
        this.minor = minor;
        this.string = string;
    }


    @Override
    public String toString()
    {
        return major+"."+minor+" ["+string+"]";
    }


    @Override
    public int compareTo(PythonVersion that)
    {
        int z = this.major - that.major;
        if (z == 0)
            z = this.minor - that.minor;
        if (z == 0)
            z = this.string.compareTo(that.string);
        return z;
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null) return false;

        return (o instanceof PythonVersion && equals((PythonVersion)o));
    }

    public boolean equals(PythonVersion that)
    {
        return this.major == that.major
            && this.minor == that.minor
            && this.string.equals(that.string);
    }


    @Override
    public int hashCode()
    {
        return (major << 16) | minor;
    }


    /**
     * Empty zero version.
     */
    @NotNull
    public static final PythonVersion zero = new PythonVersion(0, 0, "");

}
