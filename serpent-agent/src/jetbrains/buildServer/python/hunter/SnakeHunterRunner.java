package jetbrains.buildServer.python.hunter;

import jetbrains.buildServer.utils.YAFU;

import java.io.File;
import java.io.IOException;
import java.util.SortedMap;

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

        InstalledPythons pythons =
                hunter.hunt();
        SortedMap<String,String> selection =
                hunter.select(pythons);

        printResults(args, pythons, selection);
    }


    private static void printResults(String[] args,
                                     InstalledPythons pythons,
                                     SortedMap<String,String> selection)
            throws IOException
    {
        String pythonsText = PrepareSummaryText(pythons, selection);

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

    static String PrepareSummaryText(InstalledPythons pythons, SortedMap<String, String> selection)
    {
        StringBuilder buf = new StringBuilder();

        if (pythons.found())
        {
            buf.append("Found Pythons: \n");
            for(InstalledPython python: pythons.getPythons())
                buf.append('\t').append(python.toString()).append('\n');
            buf.append("Selected Pythons: \n");
            for (SortedMap.Entry<String,String> entry: selection.entrySet())
                buf.append('\t').append(entry.getKey()).append(" = ").append(entry.getValue()).append('\n');
        }
        else
        {
            buf.append("No Python installations found.");
        }

        return buf.toString();
    }

}
