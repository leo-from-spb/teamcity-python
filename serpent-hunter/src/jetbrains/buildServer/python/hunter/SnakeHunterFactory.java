package jetbrains.buildServer.python.hunter;

import com.intellij.openapi.util.SystemInfo;


/**
 * Creates an instance of Python detector, according to the current operating system.
 *
 * @author Leonid Bushuev from JetBrains
 */
public final class SnakeHunterFactory
{

    public SnakeHunter createSnakeHunter()
    {
        final SnakeHunter hunter = SystemInfo.isWindows ? new SnakeHunterForWindows()
                                 : SystemInfo.isMac ? new SnakeHunterForMac()
                                 : new SnakeHunterForUnix();
        hunter.init();
        return hunter;
    }

}
