package jetbrains.buildServer.python.hunter;

import com.intellij.openapi.util.SystemInfo;
import jetbrains.buildServer.utils.YAFU;

import java.io.File;
import java.io.IOException;

/**
 * Factory and runner for SnakeHunter.
 * @author Leonid Bushuev from JetBrains
 */
public class SnakeHunterRunner
{

    public static void main(String[] args)
            throws IOException
    {
        for (String arg: args)
            System.out.println("\targ: " + arg);
        System.out.println("total " + args.length +" arguments.");

        SnakeHunterRunner shr = new SnakeHunterRunner();
        InstalledPythons pythons = shr.huntPythons();

        String pythonsText = pythons.found() ? pythons.toString() : "No Python installations found.";

        if (args.length == 1)
        {
            String outputFileName = args[0];
            File outputFile = new File(outputFileName);
            YAFU.writeTextFile(outputFile, pythonsText);
        }
        else
        {
            System.out.println(pythonsText);
        }
    }


    public void hunt()
    {
        huntPythons();
    }


    public InstalledPythons huntPythons()
    {
        final SnakeHunter hunter = SystemInfo.isWindows ? new SnakeHunterForWindows()
                                 : SystemInfo.isMac ? new SnakeHunterForMac()
                                 : new SnakeHunterForUnix();
        hunter.init();
        return hunter.hunt();
    }

}
