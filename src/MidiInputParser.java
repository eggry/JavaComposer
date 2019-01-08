
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;


public class MidiInputParser {
	DataInputStream file;
	int ticksPerQuarterNote;//一个四分音符几个tick
	int ticksPerMeasure;//一个小节几个tick
	LinkedList<Measure> parseResult=new LinkedList<Measure>();//分好小节的
	public MidiInputParser(DataInputStream file){//拿一个输入流构造解析器
		this.file=file;
		
	}
	void prase() throws IOException, MidiFormatError {
		while(file.available()>0) {//有块就解析
			parseTrunk();
		}
	}
	private void parseTrunk() throws IOException, MidiFormatError {
		final byte[] headMark= {0x4d,0x54,0x68,0x64};//文件头的头
		final byte[] trackMark={0x4d,0x54,0x72,0x6B};//内容块的头
		byte[] buffer=new byte[4];
		file.read(buffer);
		if(Arrays.equals(buffer, headMark)) {
			parseHead(file.readInt());//解析头文件，读长度传过去
			//System.out.println("parseHeadOK,tick:"+ticksPerQuarterNote);//debug
		}else {
			if(Arrays.equals(buffer, trackMark)) {
				//把内容读进来
				byte[] trunkbuffer=new byte[file.readInt()];
				file.read(trunkbuffer);
				//解析音轨
				
				parseTrack(new DataInputStream(new BufferedInputStream(new ByteArrayInputStream(trunkbuffer))));
			}else {//都不是，就报错
				throw new MidiFormatError("unknow TrunkHeadMark:"+Arrays.toString(buffer));
			}
		}
	}
	void parseHead(int length) throws IOException, MidiFormatError {//文件头解析
		if(length<6) {//头文件长度不够，退出
			file.skip(length);
			throw new MidiFormatError("Head too short");
		}
		//先都读进来
		int format=file.readUnsignedShort();//文件格式，0-单音轨
		int trackCount=file.readUnsignedShort();//几个音轨，应该是1
		this.ticksPerQuarterNote=file.readUnsignedShort();//一个四分音符几个tick
		this.ticksPerMeasure=4*ticksPerQuarterNote;//计算一个小节几个tick，未指定时默认4/4拍，即一小节是四个四分音符
		file.skip(length-6);//跳过剩余头部
		//判读是不是不标准
		if(format!=0)throw new MidiFormatError("Isn't single track file format");
		if(trackCount!=1)throw new MidiFormatError("Contains mutiple tracks");
		if(ticksPerQuarterNote>>>15==1)throw new MidiFormatError("division fromat must be ticks");
	}
	void parseTrack(DataInputStream ds) throws IOException, MidiFormatError {//轨道解析
		Measure nowMeasure= new Measure();//现在正在解析的小节
		//设小节长N个tick
		//第一个：[0,N-1]
		//第二个：[N,2N-1]
		//...
		boolean end=false;//是否退出解析
		int nowTime=0;//现在tick数
		int[] NotesStartTime= new int [256];//记录每个音符的起始时间（起点从曲子开始计算！），目前没有的用-1
		int[] NotesPower= new int [256];//记录每个音符的力度，目前没有的用0
		Arrays.fill(NotesStartTime, -1);//一开始都没有
		
		
		int eventCode=0;
		while(ds.available()>0&&!end) {
			int deltaTime=getDeltaTime(ds);
			nowTime+=deltaTime;//更新现在时间
			while(nowTime>=ticksPerMeasure*(parseResult.size()+1)) {//如果时间超过上一个小节，生成新小节
				for(int i=0;i<256;i++) {//把没结束的写到这个小节里
					if(NotesStartTime[i]!=-1) {
						int startTime = Math.max(0, NotesStartTime[i]-ticksPerMeasure*parseResult.size());//本小节中的
						int duration = ticksPerMeasure-startTime;
						boolean prevContinue = NotesStartTime[i]<ticksPerMeasure*parseResult.size();
						if(duration != 0) {
							nowMeasure.addNote(new Note(startTime, duration, i, NotesPower[i], prevContinue));
						}
					}
				}
				parseResult.add(nowMeasure);//添加新小节
				nowMeasure=new Measure();//来一个新的
			}
			
			ds.mark(5);
			int tmp=ds.readUnsignedByte();
			ds.reset();
			eventCode=tmp>>>7==0x0?eventCode:ds.readUnsignedByte();
			switch (eventCode>>>4) {
			case 0x8:{
				int note=ds.readUnsignedByte();
				ds.skipBytes(1);
				int startTime = Math.max(0, NotesStartTime[note]-ticksPerMeasure*parseResult.size());//本小节中的
				int duration = nowTime-ticksPerMeasure*parseResult.size()-startTime;
				boolean prevContinue = NotesStartTime[note]<ticksPerMeasure*parseResult.size();
				if(duration!=0) {
					nowMeasure.addNote(new Note(startTime, duration, note, NotesPower[note], prevContinue));
				}
				NotesStartTime[note]=-1;
				NotesPower[note]=0;
				break;
			}
			case 0x9:{
				int note=ds.readUnsignedByte();
				int power=ds.readUnsignedByte();
				if(power<15&&NotesStartTime[note]!=-1) {//之前已经按下，此次力度太小，认为按键抬起，写入。
					int startTime = Math.max(0, NotesStartTime[note]-ticksPerMeasure*parseResult.size());//本小节中的
					int duration = nowTime-ticksPerMeasure*parseResult.size()-startTime;
					boolean prevContinue = NotesStartTime[note]<ticksPerMeasure*parseResult.size();
					if(duration!=0) {
						nowMeasure.addNote(new Note(startTime, duration, note, NotesPower[note], prevContinue));
					}
					NotesStartTime[note]=-1;
					NotesPower[note]=0;
					break;
				}
				if(power<15) {//力度太小直接忽略
					break;
				}
				if(NotesStartTime[note]==-1) {
					NotesStartTime[note]=nowTime;
				}else {
					//System.out.println("wow:Key"+note+"prev:"+NotesPower[note]+"now"+power);//同一个音符不松开就改力度
				}
				NotesPower[note]=power;
				break;
			}
			case 0xA:{
				ds.skipBytes(2);
				break;
			}
			case 0xB:{
				ds.skipBytes(2);
				break;
			}
			case 0xC:{
				ds.skipBytes(1);
				break;
			}
			case 0xD:{
				ds.skipBytes(1);
				break;
			}
			case 0xE:{
				ds.skipBytes(2);
				break;
			}
			case 0xF:{
				switch (eventCode&0xf) {
				case 0xF:{
					int type=ds.readUnsignedByte();
					int len=ds.readUnsignedByte();
					byte[] buf=new byte[len];
					ds.read(buf);
					switch (type) {
					// TODO 还有转换小节的事件
					case 0x2f:{
						//System.out.println("ok!");
						if(nowMeasure.noteCount()!=0) {
							parseResult.add(nowMeasure);//添加新小节
						}
						return;
					}
					}
				}
				}
				break;
			}
			default:{
				throw new MidiFormatError("unknown opCode"+Integer.toHexString(eventCode));
			}
			}
		}
		System.out.println("running out without end!");
		if(nowMeasure.noteCount()!=0) {
			parseResult.add(nowMeasure);//添加新小节
		}
	}
	
	static int getDeltaTime(DataInputStream ds) throws IOException {//解析变长数->int
		int ret=0;
		int temp=0;
		while(((temp=ds.readUnsignedByte())&0x80)==0x80) {//高位是1，继续
			ret<<=7;
			ret|=(temp)&0x7f;
		}
		ret<<=7;
		ret|=(temp)&0x7f;//把最后一个写入
		return ret;
	}
}
