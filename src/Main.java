import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
//import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;

public class Main {


	public static void main(String[] args) throws IOException, MidiFormatError {
		Scanner in=new Scanner(System.in);
		System.out.println("Enter path of Midi File(-1 to stop):");
		MarkovChain mc=new MarkovChain();
		String inputStr;
		while(in.hasNextLine()&&!(inputStr=in.nextLine().trim()).equals("-1")) {
			try {
				if(inputStr.length()>=2&&inputStr.charAt(0)=='"'&&inputStr.charAt(inputStr.length()-1)=='"') {
					inputStr=inputStr.substring(1, inputStr.length()-1);
				}
				DataInputStream dis=new DataInputStream(new FileInputStream(inputStr));
				try {
					MidiInputParser mip=new MidiInputParser(dis);
					mc.addList(mip.prase());
				}
				catch (MidiFormatError e) {
					System.err.println("File format Err:"+inputStr);
					e.printStackTrace();
				}
				dis.close();
			} catch (IOException e) {
				System.err.println("Can't read file:"+inputStr);
				e.printStackTrace();
			} 
		}
		System.out.println();
		System.out.println("ReadOK!");
		System.out.println(mc);
		String filename=mc.getSeed()+".mid";
		System.out.println("Enter path of output Midi File:\t[Your input]+\""+filename+"\"");
		try {
			inputStr=in.nextLine().trim();
			if(inputStr.length()>=2&&inputStr.charAt(0)=='"'&&inputStr.charAt(inputStr.length()-1)=='"') {
				inputStr=inputStr.substring(1, inputStr.length()-1);
			}
			if(inputStr.equals("")) {
				inputStr=".\\";
			}
			File file=new File(inputStr+filename);
			file.getParentFile().mkdirs();
			file.createNewFile();
			System.out.println("Output path:\t"+file.getCanonicalPath());
			DataOutputStream dos=new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
			LinkedList<Measure> result=mc.generate();
			System.out.println();
			System.out.println("GenerateOK!");
			System.out.println("MeasureCnt:"+result.size());
			MidiOutputParser mop=new MidiOutputParser(dos);
			mop.writeFile(result);
			dos.close();
			System.out.println("write file OK!");
		} catch (IOException e) {
			System.err.println("Can't write file:");
			e.printStackTrace();
		}
		in.close();
	}
}
