
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;


public class MidiInputParser {
	private DataInputStream file;//����������
	private int ticksPerQuarterNote;//һ���ķ���������tick
	private int ticksPerMeasure;//һ��С�ڼ���tick
	private final static int minNote=23;//C2��do����������͵Ķ�����
	private final static int maxNote=83;//C5��si��������ߵ���������
	private final static int minPower=30;//С����������Ķ���Ϊ�Ǿ���
	private int maxDuartion;//���������������Ҫ��
	private LinkedList<Measure> parseResult=new LinkedList<Measure>();//�ֺ�С�ڵĽ������
	private Measure nowMeasure;
	private int nowTime=0;//����tick��
	private int[] notesStartTime= new int [256];//��¼ÿ����������ʼʱ�䣨��������ͷ��ʼ���㣡����Ŀǰû�е���-1
	private int[] notesPower= new int [256];//��¼ÿ�����������ȣ�Ŀǰû�е���0
	
	public MidiInputParser(DataInputStream file){//��һ�����������������
		this.file=file;
	}
	
	public LinkedList<Measure> prase() throws IOException, MidiFormatError {//����
		while(file.available()>0) {//�п�ͽ���
			parseTrunk();
		}
		return parseResult;
	}
	
	private void parseTrunk() throws IOException, MidiFormatError {
		final byte[] headMark= {0x4d,0x54,0x68,0x64};//ͷ���ͷ
		final byte[] trackMark={0x4d,0x54,0x72,0x6B};//���ݿ��ͷ
		byte[] buffer=new byte[4];
		file.read(buffer);//ͷ�ĸ��ֽڶ�����
		if(Arrays.equals(buffer, headMark)) {//�����ͷ���ͷ
			parseHead(file.readInt());//����ͷ�ļ��������ȴ���ȥ
			//System.out.println("parseHeadOK,tick:"+ticksPerQuarterNote);//debug
		}else {
			if(Arrays.equals(buffer, trackMark)) {//������ݿ��ͷ
				//�����ݶ�����
				byte[] trunkbuffer=new byte[file.readInt()];
				file.read(trunkbuffer);
				//�������죬��������buffer��װ
				parseTrack(new DataInputStream(new BufferedInputStream(new ByteArrayInputStream(trunkbuffer))));
			}else {//�����ǣ��ͱ���
				throw new MidiFormatError("unknow TrunkHeadMark:"+Arrays.toString(buffer));
			}
		}
	}
	
	private void parseHead(int length) throws IOException, MidiFormatError {//�ļ�ͷ����
		if(length<6) {//ͷ�ļ����Ȳ������˳�
			file.skip(length);
			throw new MidiFormatError("Head too short");
		}
		//�ȶ�������
		int format=file.readUnsignedShort();//�ļ���ʽ��0-������
		int trackCount=file.readUnsignedShort();//�������죬Ӧ����1
		this.ticksPerQuarterNote=file.readUnsignedShort();//һ���ķ���������tick
		this.ticksPerMeasure=4*ticksPerQuarterNote;//����һ��С�ڼ���tick��δָ��ʱĬ��4/4�ģ���һС�����ĸ��ķ�����
		this.maxDuartion=this.ticksPerMeasure;
		file.skip(length-6);//����ʣ��ͷ��
		//�ж��ǲ��ǲ�֧�ֵĸ�ʽ
		if(format!=0) {//������0��ʽ
			throw new MidiFormatError("Isn't single track file format");
		}
		if(trackCount!=1) {//0��ʽֻ��1�����
			throw new MidiFormatError("Contains mutiple tracks");
		}
		if(ticksPerQuarterNote>>>15==1) {//ʱ������tick��ʽ
			throw new MidiFormatError("division fromat must be ticks");
		}
	}
	
	private void parseTrack(DataInputStream ds) throws IOException, MidiFormatError {//�������
		if(!ds.markSupported()) {//����������֧��mark
			throw new IllegalArgumentException("DataInputStream must be buffered!");
		}
		//��С�ڳ�N��tick
		//��һ����[0,N-1]
		//�ڶ�����[N,2N-1]
		//...
		nowMeasure= new Measure();//�������ڽ�����С��
		
		Arrays.fill(notesStartTime, -1);//һ��ʼ��û��
		int eventCode=0;//�¼����
		while(ds.available()>0) {//�о�һֱ��
			int deltaTime=getDeltaTime(ds);//����deltaTime
			nowTime+=deltaTime;//��������ʱ��
			while(nowTime>=ticksPerMeasure*(parseResult.size()+1)) {//���ʱ�䳬����һ��С�ڣ�������С��
				for(int i=Math.max(minNote,0);i<=Math.min(maxNote,255);i++) {//��û������д�����С����
					if(notesStartTime[i]!=-1) {
						writeNoteToMeasure(ticksPerMeasure*(parseResult.size()+1),i);
					}
				}
				parseResult.add(nowMeasure);//�����С��
				nowMeasure=new Measure();//��һ���µ�
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
					int type=ds.readUnsignedByte();//�¼�����
					int len=ds.readUnsignedByte();//���ݳ���
					byte[] buf=new byte[len];
					ds.read(buf);
					switch (type) {
					case 0x2f:{
						if(nowMeasure.noteCount()!=0) {
							parseResult.add(nowMeasure);//�����С��
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
			parseResult.add(nowMeasure);//�����С��
		}
	}
	private void noteChange(int nowTime,int note,int power) {
		if(note<minNote||note>maxNote) {
			return ;//С�������ֱ�Ӹɵ�
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
		int startTime = Math.max(0, notesStartTime[note]-ticksPerMeasure*parseResult.size());//��С���е�
		int duration = endTime-ticksPerMeasure*parseResult.size()-startTime;
		boolean prevContinue = notesStartTime[note]<ticksPerMeasure*parseResult.size();
		if(duration != 0 && endTime-notesStartTime[note]<=maxDuartion) {
			final int convertToStandard=960/this.ticksPerQuarterNote;
			nowMeasure.addNote(new Note(convertToStandard*startTime, convertToStandard*duration, note, notesPower[note], prevContinue));
		}
	}
	private static int getDeltaTime(DataInputStream ds) throws IOException {//�����䳤��->int
		int ret=0;
		int temp=0;
		while(((temp=ds.readUnsignedByte())&0x80)==0x80) {//��λ��1������
			ret<<=7;
			ret|=(temp)&0x7f;
		}
		ret<<=7;
		ret|=(temp)&0x7f;//�����һ��д��
		return ret;
	}
}
