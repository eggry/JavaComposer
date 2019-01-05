import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;


public class MidiInputParser {
	DataInputStream file;
	int ticksPerQuarterNote;
	int tickserMeasure;
	public MidiInputParser(DataInputStream file){
		this.file=file;
		try {
			while(file.available()>0) {
				parseTrunk();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MidiFormatError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void parseTrunk() throws IOException, MidiFormatError {
		final byte[] headMark= {0x4d,0x54,0x68,0x64};
		final byte[] trackMark={0x4d,0x54,0x72,0x6B};
		byte[] buffer=new byte[4];
		file.read(buffer);
		if(Arrays.equals(buffer, headMark)) {
			parseHead(file.readInt());
			System.out.println("parseHeadOK,tick:"+ticksPerQuarterNote);
		}else {
			if(Arrays.equals(buffer, trackMark)) {
				parseTrack(file.readInt());
			}else {
				throw new MidiFormatError();
			}
		}
		

	}
	void parseHead(int length) throws IOException, MidiFormatError {
		if(length<6) {
			file.skip(length);
			throw new MidiFormatError("Head too short");
		}
		int format=file.readUnsignedShort();
		int trackCount=file.readUnsignedShort();
		this.ticksPerQuarterNote=file.readUnsignedShort();
		this.tickserMeasure=4*ticksPerQuarterNote;
		file.skip(length-6);
		if(format!=0)throw new MidiFormatError("Isn't single track file format");
		if(trackCount!=1)throw new MidiFormatError("Contains mutiple tracks");
		if(ticksPerQuarterNote>>>15==1)throw new MidiFormatError("division fromat must be ticks");
	}
	void parseTrack(int length) throws IOException {
		byte[] buffer=new byte[length];
		file.read(buffer);
		DataInputStream ds= new DataInputStream(new ByteArrayInputStream(buffer));
		boolean end=false;
		while(ds.available()>0&&!end) {
			int deltaTime=getDeltaTime(ds);
			int eventCode=ds.readUnsignedByte();
			switch (eventCode>>>4) {
			case 0x8:{
				System.out.println(233);
				break;
			}
			case 0x9:{
				int Note=ds.readUnsignedByte();
				int Power=ds.readUnsignedByte();
				NoteOn(Note,Power);
				break;
			}
			case 0xA:{
				
			}
			case 0xB:{
				
			}
			case 0xC:{
				
			}
			case 0xF:{
				switch (eventCode&0xf) {
				case 0xF:{
					int type=ds.readUnsignedByte();
					int len=ds.readUnsignedByte();
					byte[] buf=new byte[len];
					ds.read(buf);
					switch (type) {
					case 0x2f:{
						end=true;
					}
					}
				}
				}
			}
			}
		}
	}
	private void NoteOn(int note, int power) {
		// TODO Auto-generated method stub
		
	}

	static int getDeltaTime(DataInputStream ds) throws IOException {
		int ret=0;
		int temp=0;
		while(((temp=ds.readUnsignedByte())&0x80)==0x80) {
			ret<<=7;
			ret|=(temp)&0x7f;
		}
		ret<<=7;
		ret|=(temp)&0x7f;
		return ret;
	}
}
