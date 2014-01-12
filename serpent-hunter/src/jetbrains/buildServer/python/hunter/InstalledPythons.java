package jetbrains.buildServer.python.hunter;

import java.util.*;

/**
 * Installed pythons.
 * @author Leonid Bushuev from JetBrains
 */
public final class InstalledPythons
{

    private final SortedSet<InstalledPython> pythons =
            new TreeSet<InstalledPython>();



    void addPython(InstalledPython python)
    {
        pythons.add(python);
    }


    public SortedSet<InstalledPython> getPythons()
    {
        return Collections.unmodifiableSortedSet(pythons);
    }


    public boolean found()
    {
        return ! pythons.isEmpty();
    }


    @Override
    public String toString()
    {
        return toString("");
    }


    public String toString(String linePrefix)
    {
        StringBuilder buf = new StringBuilder();
        for (InstalledPython python: pythons)
            buf.append(linePrefix).append(python.toString()).append('\n');
        return buf.toString();
    }
}



