package jetbrains.buildServer.python.hunter;


import jetbrains.buildServer.python.common.PythonKind;
import jetbrains.buildServer.python.common.PythonVersion;
import jetbrains.buildServer.util.Bitness;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;


/**
 * Installed python. Value object.
 *
 * @author Leonid Bushuev from JetBrains
 */
public final class InstalledPython
    implements Comparable<InstalledPython>
{

    /**
     * Kind of the python.
     */
    @NotNull
    public final PythonKind kind;

    /**
     * Version of the python.
     */
    @NotNull
    public final PythonVersion version;

    /**
     * Python bitness.
     * Or null if could not determine.
     */
    @Nullable
    public final Bitness bitness;

    /**
     * File to execute.
     */
    @NotNull
    public final File executable;



    public InstalledPython(final @NotNull  PythonKind kind,
                           final @NotNull  PythonVersion version,
                           final @Nullable Bitness bitness,
                           final @NotNull  File executable)
    {
        this.kind = kind;
        this.version = version;
        this.bitness = bitness;
        this.executable = executable;
    }



    @Override
    public int compareTo(InstalledPython that)
    {
        if (this == that)
            return 0;
        if (that == null)
            throw new IllegalArgumentException("That is null.");

        int z = this.kind.compareTo(that.kind);
        if (z == 0)
            z = this.version.compareTo(that.version);
        if (z == 0)
            z = this.executable.compareTo(that.executable);
        return z;
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InstalledPython that = (InstalledPython) o;

        if (!executable.equals(that.executable)) return false;
        if (kind != that.kind) return false;
        if (!version.equals(that.version)) return false;

        return true;
    }


    @Override
    public int hashCode()
    {
        return (kind.hashCode() << 24) | version.hashCode();
    }


    @Override
    public String toString()
    {
        String bitnessStr = bitness != null ? Byte.toString(bitness.value) : "??";
        return kind.code + " " + version + " x"+bitnessStr + " " + executable;
    }
}
