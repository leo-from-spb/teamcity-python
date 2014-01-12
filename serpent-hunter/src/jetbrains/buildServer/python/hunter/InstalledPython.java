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

    /**
     * Sequential order in which this installation has been found.
     * Started with 1.
     */
    public final int foundOrder;



    public InstalledPython(final @NotNull PythonKind kind,
                           final @NotNull PythonVersion version,
                           final @Nullable Bitness bitness,
                           final @NotNull File executable,
                           final int foundOrder)
    {
        this.kind = kind;
        this.version = version;
        this.bitness = bitness;
        this.executable = executable;
        this.foundOrder = foundOrder;
    }



    @Override
    public int compareTo(@NotNull InstalledPython that)
    {
        if (this == that)
            return 0;

        int z = this.kind.compareTo(that.kind);
        if (z == 0)
            z = this.version.compareTo(that.version);
        if (z == 0)
            z = compareBitnesses(this.bitness, that.bitness);
        if (z == 0)
            z = this.executable.compareTo(that.executable);
        if (z == 0)
            z = (-this.foundOrder) - (-that.foundOrder); // please don't "simplify" minuses
        return z;
    }


    private static int compareBitnesses(final @Nullable Bitness bitness1, final @Nullable Bitness bitness2)
    {
        byte v1 = bitness1 != null ? bitness1.value : (byte) -1;
        byte v2 = bitness2 != null ? bitness2.value : (byte) -1;
        return v1 - v2;
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InstalledPython that = (InstalledPython) o;

        if (kind != that.kind) return false;
        if (bitness != that.bitness) return false;
        if (foundOrder != that.foundOrder) return false;
        if (!executable.equals(that.executable)) return false;
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
