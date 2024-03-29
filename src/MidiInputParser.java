
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;


public class MidiInputParser {
	private DataInputStream file;//数据输入流
	private int ticksPerQuarterNote;//一个四分音符几个tick
	private int ticksPerMeasure;//一个小节几个tick
	private final static int minNote=23;//C2的do，比这个音低的都忽略
	private final static int maxNote=83;//C5的si，比这个高的音都忽略
	private final static int minPower=30;//小于这个声音的都认为是静音
	private int maxDuartion;//比这个长的音都不要了
	private LinkedList<Measure> parseResult=new LinkedList<Measure>();//分好小节的解析结果
	private Measure nowMeasure;
	private int nowTime=0;//现在tick数
	private int[] notesStartTime= new int [256];//记录每个音符的起始时间（起点从曲子头开始计算！），目前没有的用-1
	private int[] notesPower= new int [256];//记录每个音符的力度，目前没有的用0
	
	public MidiInputParser(DataInputStream file){//拿一个输入流构造解析器
		this.file=file;
	}
	
	public LinkedList<Measure> prase() throws IOException, MidiFormatError {//解析
		while(file.available()>0) {//有块就解析
			parseTrunk();
		}
		return parseResult;
	}
	
	private void parseTrunk() throws IOException, MidiFormatError {
		final byte[] headMark= {0x4d,0x54,0x68,0x64};//头块的头
		final byte[] trackMark={0x4d,0x54,0x72,0x6B};//内容块的头
		byte[] buffer=new byte[4];
		file.read(buffer);//头四个字节读进来
		if(Arrays.equals(buffer, headMark)) {//如果是头块的头
			parseHead(file.readInt());//解析头文件，读长度传过去
			//System.out.println("parseHeadOK,tick:"+ticksPerQuarterNote);//debug
		}else {
			if(Arrays.equals(buffer, trackMark)) {//如果内容块的头
				//把内容读进来
				byte[] trunkbuffer=new byte[file.readInt()];
				file.read(trunkbuffer);
				//解析音轨，输入流用buffer封装
				parseTrack(new DataInputStream(new BufferedInputStream(new ByteArrayInputStream(trunkbuffer))));
			}else {//都不是，就报错
				throw new MidiFormatError("unknow TrunkHeadMark:"+Arrays.toString(buffer));
			}
		}
	}
	
	private void parseHead(int length) throws IOException, MidiFormatError {//文件头解析
		if(length<6) {//头文件长度不够，退出
			file.skip(length);
			throw new MidiFormatError("Head too short");
		}
		//先都读进来
		int format=file.readUnsignedShort();//文件格式，0-单音轨
		int trackCount=file.readUnsignedShort();//几个音轨，应该是1
		this.ticksPerQuarterNote=file.readUnsignedShort();//一个四分音符几个tick
		this.ticksPerMeasure=4*ticksPerQuarterNote;//计算一个小节几个tick，未指定时默认4/4拍，即一小节是四个四分音符
		this.maxDuartion=this.ticksPerMeasure;
		file.skip(length-6);//跳过剩余头部
		//判断是不是不支持的格式
		if(format!=0) {//必须是0格式
			throw new MidiFormatError("Isn't single track file format");
		}
		if(trackCount!=1) {//0格式只有1个轨道
			throw new MidiFormatError("Contains mutiple tracks");
		}
		if(ticksPerQuarterNote>>>15==1) {//时间码是tick格式
			throw new MidiFormatError("division fromat must be ticks");
		}
	}
	
	private void parseTrack(DataInputStream ds) throws IOException, MidiFormatError {//轨道解析
		if(!ds.markSupported()) {//输入流必须支持mark
			throw new IllegalArgumentException("DataInputStream must be buffered!");
		}
		//设小节长N个tick
		//第一个：[0,N-1]
		//第二个：[N,2N-1]
		//...
		nowMeasure= new Measure();//现在正在解析的小节
		
		Arrays.fill(notesStartTime, -1);//一开始都没有
		int eventCode=0;//事件编号
		while(ds.available()>0) {//有就一直读
			int deltaTime=getDeltaTime(ds);//解析deltaTime
			nowTime+=deltaTime;//更新现在时间
			while(nowTime>=ticksPerMeasure*(parseResult.size()+1)) {//如果时间超过上一个小节，生成新小节
				for(int i=Math.max(minNote,0);i<=Math.min(maxNote,255);i++) {//把没结束的写到这个小节里
					if(notesStartTime[i]!=-1) {
						writeNoteToMeasure(ticksPerMeasure*(parseResult.size()+1),i);
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
				noteChange(nowTime,note,0);
				break;
			}
			case 0x9:{
				int note=ds.readUnsignedByte();
				int power=ds.readUnsignedByte();
				noteChange(nowTime,note,power);
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
					int type=ds.readUnsignedByte();//事件类型
					int len=ds.readUnsignedByte();//内容长度
					byte[] buf=new byte[len];
					ds.read(buf);
					switch (type) {
					case 0x2f:{
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
	private void noteChange(int nowTime,int note,int power) {
		if(note<minNote||note>maxNote) {
			return ;//小于音域的直接干掉
		}
		if(power<=0||power<minPower) {
			if(notesPower[note]>=minPower) {
				writeNoteToMeasure(nowTime,note);
				notesStartTime[note]=-1;
				notesPower[note]=0;	
			}
		}else {
			if(notesStartTime[note]!=-1) {
				writeNoteToMeasure(nowTime,note);
			}
			notesStartTime[note]=nowTime;
			notesPower[note]=power;	
		}
	}
	
	private void writeNoteToMeasure(int endTime,int note) {
		int startTime = Math.max(0, notesStartTime[note]-ticksPerMeasure*parseResult.size());//本小节中的
		int duration = endTime-ticksPerMeasure*parseResult.size()-startTime;
		boolean prevContinue = notesStartTime[note]<ticksPerMeasure*parseResult.size();
		if(duration != 0 && endTime-notesStartTime[note]<=maxDuartion) {
			final int convertToStandard=960/this.ticksPerQuarterNote;
			nowMeasure.addNote(new Note(convertToStandard*startTime, convertToStandard*duration, note, notesPower[note], prevContinue));
		}
	}
	private static int getDeltaTime(DataInputStream ds) throws IOException {//解析变长数->int
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
