import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
//import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class Main {


	public static void main(String[] args) throws IOException, MidiFormatError {
		Scanner in=new Scanner(System.in);
		System.out.println("Enter path of Midi File:(-1 to stop)");
		MarkovChain mc=new MarkovChain();
		String inputStr;
		while(in.hasNextLine()&&!(inputStr=in.nextLine().trim()).equals("-1")) {
			try {
				MidiInputParser mip=new MidiInputParser(new DataInputStream(new FileInputStream(inputStr)));
				mip.prase();
				mc.addList(mip.parseResult);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.err.println("Can't find file:"+inputStr);
				e.printStackTrace();
			} catch (MidiFormatError e) {
				// TODO Auto-generated catch block
				System.err.println("File format Err:"+inputStr);
				e.printStackTrace();
			}

		}
		System.out.println("totalCount:"+mc.totalMeasureCount+"uniqueCount:"+mc.uniqueMeasureCount()+"Ave:"+mc.totalMeasureCount/mc.totalListCount);
		mc.generate();
		System.out.println(mc.result);
		System.out.println("MeasureCnt:"+mc.result.size());
		
		System.out.println("Enter path of output Midi File:");
		try {
			DataOutputStream dos=new DataOutputStream(new BufferedOutputStream(new FileOutputStream(in.nextLine().trim())));
			MidiOutputParser mop=new MidiOutputParser(dos);
			mop.writeFile(mc.result);
			dos.close();
			System.out.println("writeOK");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("Can't write file:");
			e.printStackTrace();
		}
		in.close();

	}
}
