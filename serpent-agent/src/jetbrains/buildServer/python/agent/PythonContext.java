package jetbrains.buildServer.python.agent;

import java.io.File;

/**
 * Python context.
 * Incapsulates all things related python (place, files, libraries, etc.)
 * but not depended the working directory and current build.
 *
 * <p>Value object.</p>
 *
 * @author Leonid Bushuev from JetBrains
 */
public class PythonContext
{
    /**
     * Where python is located.
     */
    public final File directory;

    /**
     * What is to executed.
     */
    public final File executable;


    public PythonContext(File directory, File executable)
    {
        this.directory = directory;
        this.executable = executable;
    }

}
