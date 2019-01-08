import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main {


	public static void main(String[] args) throws FileNotFoundException {
		Scanner in=new Scanner(".\\testfiles\\verytest.mid");
		System.out.println(".\\testfiles\\verytest.mid");
		MidiInputParser mip=new MidiInputParser(new DataInputStream(new FileInputStream(in.nextLine())));
		mip.prase();
		System.out.println(mip.parseResult);
		System.out.println(mip.parseResult.size()+"Measures in total.");
		in.close();
	}
}
