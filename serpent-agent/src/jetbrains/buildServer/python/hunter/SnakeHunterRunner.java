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
        SnakeHunterFactory factory = new SnakeHunterFactory();
        SnakeHunter hunter = factory.createSnakeHunter();

        InstalledPythons pythons = hunter.hunt();

        printResults(args, pythons);
    }


    private static void printResults(String[] args, InstalledPythons pythons) throws IOException
    {
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

}
