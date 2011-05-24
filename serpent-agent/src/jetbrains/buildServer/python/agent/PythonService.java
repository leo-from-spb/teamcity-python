package jetbrains.buildServer.python.agent;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.runner.BuildServiceAdapter;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import org.jetbrains.annotations.NotNull;

/**
 * @author Leonid Bushuev from JetBrains
 */

public class PythonService extends BuildServiceAdapter
{

    @NotNull
    @Override
    public ProgramCommandLine makeProgramCommandLine() throws RunBuildException
    {
        // TODO implmement PythonService.makeProgramCommandLine
        throw new RuntimeException("The PythonService.makeProgramCommandLine has not been implemented yet.");

    }
}
