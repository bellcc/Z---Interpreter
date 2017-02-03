import java.util.Scanner;
import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;

public class Main
{
    public static void main(String [] args) throws FileNotFoundException
    {    	
        if(args.length != 1)
        {
            System.out.println("CORRECT USAGE: java Main <filename.zpm>");
            System.exit(0);
        }

        File file = new File(args[0]);

        if(!file.exists())
        {
            System.out.println("FILE DOES NOT EXIST: " + args[0]);
            System.exit(0);
        }

        ArrayList<String> lines = new ArrayList<String>();

        Scanner reader = new Scanner(file);

        while(reader.hasNext())
        {
            lines.add(reader.nextLine());
        }

        reader.close();

        Program program = new Program(lines);
        program.interpret();
    }
}
