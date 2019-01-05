import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main {


	public static void main(String[] args) throws FileNotFoundException {
		Scanner in=new Scanner(System.in);
		
		MidiInputParser mip=new MidiInputParser(new DataInputStream(new FileInputStream(in.nextLine())));
	}
}
