
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;


public class MidiInputParser {
	DataInputStream file;
	int ticksPerQuarterNote;//һ���ķ���������tick
	int ticksPerMeasure;//һ��С�ڼ���tick
	LinkedList<Measure> parseResult=new LinkedList<Measure>();//�ֺ�С�ڵ�
	public MidiInputParser(DataInputStream file){//��һ�����������������
		this.file=file;
		
	}
	void prase() throws IOException, MidiFormatError {
		while(file.available()>0) {//�п�ͽ���
			parseTrunk();
		}
	}
	private void parseTrunk() throws IOException, MidiFormatError {
		final byte[] headMark= {0x4d,0x54,0x68,0x64};//�ļ�ͷ��ͷ
		final byte[] trackMark={0x4d,0x54,0x72,0x6B};//���ݿ��ͷ
		byte[] buffer=new byte[4];
		file.read(buffer);
		if(Arrays.equals(buffer, headMark)) {
			parseHead(file.readInt());//����ͷ�ļ��������ȴ���ȥ
			//System.out.println("parseHeadOK,tick:"+ticksPerQuarterNote);//debug
		}else {
			if(Arrays.equals(buffer, trackMark)) {
				//�����ݶ�����
				byte[] trunkbuffer=new byte[file.readInt()];
				file.read(trunkbuffer);
				//��������
				
				parseTrack(new DataInputStream(new BufferedInputStream(new ByteArrayInputStream(trunkbuffer))));
			}else {//�����ǣ��ͱ���
				throw new MidiFormatError("unknow TrunkHeadMark:"+Arrays.toString(buffer));
			}
		}
	}
	void parseHead(int length) throws IOException, MidiFormatError {//�ļ�ͷ����
		if(length<6) {//ͷ�ļ����Ȳ������˳�
			file.skip(length);
			throw new MidiFormatError("Head too short");
		}
		//�ȶ�������
		int format=file.readUnsignedShort();//�ļ���ʽ��0-������
		int trackCount=file.readUnsignedShort();//�������죬Ӧ����1
		this.ticksPerQuarterNote=file.readUnsignedShort();//һ���ķ���������tick
		this.ticksPerMeasure=4*ticksPerQuarterNote;//����һ��С�ڼ���tick��δָ��ʱĬ��4/4�ģ���һС�����ĸ��ķ�����
		file.skip(length-6);//����ʣ��ͷ��
		//�ж��ǲ��ǲ���׼
		if(format!=0)throw new MidiFormatError("Isn't single track file format");
		if(trackCount!=1)throw new MidiFormatError("Contains mutiple tracks");
		if(ticksPerQuarterNote>>>15==1)throw new MidiFormatError("division fromat must be ticks");
	}
	void parseTrack(DataInputStream ds) throws IOException, MidiFormatError {//�������
		Measure nowMeasure= new Measure();//�������ڽ�����С��
		//��С�ڳ�N��tick
		//��һ����[0,N-1]
		//�ڶ�����[N,2N-1]
		//...
		boolean end=false;//�Ƿ��˳�����
		int nowTime=0;//����tick��
		int[] NotesStartTime= new int [256];//��¼ÿ����������ʼʱ�䣨�������ӿ�ʼ���㣡����Ŀǰû�е���-1
		int[] NotesPower= new int [256];//��¼ÿ�����������ȣ�Ŀǰû�е���0
		Arrays.fill(NotesStartTime, -1);//һ��ʼ��û��
		
		
		int eventCode=0;
		while(ds.available()>0&&!end) {
			int deltaTime=getDeltaTime(ds);
			nowTime+=deltaTime;//��������ʱ��
			while(nowTime>=ticksPerMeasure*(parseResult.size()+1)) {//���ʱ�䳬����һ��С�ڣ�������С��
				for(int i=0;i<256;i++) {//��û������д�����С����
					if(NotesStartTime[i]!=-1) {
						int startTime = Math.max(0, NotesStartTime[i]-ticksPerMeasure*parseResult.size());//��С���е�
						int duration = ticksPerMeasure-startTime;
						boolean prevContinue = NotesStartTime[i]<ticksPerMeasure*parseResult.size();
						if(duration != 0) {
							nowMeasure.addNote(new Note(startTime, duration, i, NotesPower[i], prevContinue));
						}
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
				int startTime = Math.max(0, NotesStartTime[note]-ticksPerMeasure*parseResult.size());//��С���е�
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
				if(power<15&&NotesStartTime[note]!=-1) {//֮ǰ�Ѿ����£��˴�����̫С����Ϊ����̧��д�롣
					int startTime = Math.max(0, NotesStartTime[note]-ticksPerMeasure*parseResult.size());//��С���е�
					int duration = nowTime-ticksPerMeasure*parseResult.size()-startTime;
					boolean prevContinue = NotesStartTime[note]<ticksPerMeasure*parseResult.size();
					if(duration!=0) {
						nowMeasure.addNote(new Note(startTime, duration, note, NotesPower[note], prevContinue));
					}
					NotesStartTime[note]=-1;
					NotesPower[note]=0;
					break;
				}
				if(power<15) {//����̫Сֱ�Ӻ���
					break;
				}
				if(NotesStartTime[note]==-1) {
					NotesStartTime[note]=nowTime;
				}else {
					//System.out.println("wow:Key"+note+"prev:"+NotesPower[note]+"now"+power);//ͬһ���������ɿ��͸�����
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
					// TODO ����ת��С�ڵ��¼�
					case 0x2f:{
						//System.out.println("ok!");
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
	
	static int getDeltaTime(DataInputStream ds) throws IOException {//�����䳤��->int
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
