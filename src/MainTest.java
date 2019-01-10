import static org.junit.jupiter.api.Assertions.*;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

import org.junit.jupiter.api.Test;

class MainTest {

	@Test
	void test() {
		Scanner in=new Scanner(System.in);
		System.out.println("Enter path of Midi File:(-1 to stop)");
		//MarkovChain mc=new MarkovChain();
		String inputStr;
		while(in.hasNextLine()&&!(inputStr=in.nextLine().trim()).equals("-1")) {
			try {
				MidiInputParser mip=new MidiInputParser(new DataInputStream(new FileInputStream(inputStr)));
				mip.prase();
				System.out.println(mip.parseResult);
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
		in.close();
//		System.out.println("totalCount:"+mc.totalMeasureCount+"uniqueCount:"+mc.uniqueMeasureCount()+"Ave:"+mc.totalMeasureCount/mc.totalListCount);
//		mc.generate();
//		System.out.println(mc.result);
//		System.out.println("MeasureCnt:"+mc.result.size());
	}

}
