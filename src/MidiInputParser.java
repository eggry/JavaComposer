
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
	void prase() {
		try {
			while(file.available()>0) {//�п�ͽ���
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
		final byte[] headMark= {0x4d,0x54,0x68,0x64};//�ļ�ͷ��ͷ
		final byte[] trackMark={0x4d,0x54,0x72,0x6B};//���ݿ��ͷ
		byte[] buffer=new byte[4];
		file.read(buffer);
		if(Arrays.equals(buffer, headMark)) {
			parseHead(file.readInt());//����ͷ�ļ��������ȴ���ȥ
			System.out.println("parseHeadOK,tick:"+ticksPerQuarterNote);//debug
		}else {
			if(Arrays.equals(buffer, trackMark)) {
				//�����ݶ�����
				byte[] trunkbuffer=new byte[file.readInt()];
				file.read(trunkbuffer);
				//��������
				parseTrack(new DataInputStream(new ByteArrayInputStream(trunkbuffer)));
			}else {//�����ǣ��ͱ���
				throw new MidiFormatError();
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
	LinkedList<Measure> parseTrack(DataInputStream ds) throws IOException {//�������
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
		
		
		
		while(ds.available()>0&&!end) {
			int deltaTime=getDeltaTime(ds);
			nowTime+=deltaTime;//��������ʱ��
			while(nowTime>=ticksPerQuarterNote*(parseResult.size()+1)) {//���ʱ�䳬����һ��С�ڣ�������С��
				for(int i=0;i<256;i++) {//��û������д�����С����
					if(NotesStartTime[i]!=-1) {
						int startTime = Math.max(0, NotesStartTime[i]-ticksPerQuarterNote*parseResult.size());
						int duration = ticksPerQuarterNote*(parseResult.size()+1)-1-startTime;
						boolean prevContinue = true;
						nowMeasure.addNote(new Note(startTime, duration, i, NotesPower[i], prevContinue));
					}
				}
				parseResult.add(nowMeasure);//�����С��
				System.out.println(nowMeasure);
				nowMeasure=new Measure();//��һ���µ�
			}
			
			
			int eventCode=ds.readUnsignedByte();//��event
			System.out.println(Integer.toHexString(eventCode));
			switch (eventCode>>>4) {
			case 0x8:{
				int note=ds.readUnsignedByte();
				ds.skipBytes(2);
				int startTime = Math.max(0, NotesStartTime[note]-ticksPerQuarterNote*parseResult.size());
				int duration = nowTime-startTime;
				boolean prevContinue = NotesStartTime[note]<ticksPerQuarterNote*parseResult.size();
				nowMeasure.addNote(new Note(startTime, duration, note, NotesPower[note], prevContinue));
				
				NotesStartTime[note]=-1;
				NotesPower[note]=0;
				break;
			}
			case 0x9:{
				int note=ds.readUnsignedByte();
				int power=ds.readUnsignedByte();
				if(power<15&&NotesStartTime[note]!=-1) {//֮ǰ�Ѿ����£��˴�����̫С����Ϊ����̧��д�롣
					int startTime = Math.max(0, NotesStartTime[note]-ticksPerQuarterNote*parseResult.size());
					int duration = nowTime-startTime;
					boolean prevContinue = NotesStartTime[note]<ticksPerQuarterNote*parseResult.size();
					nowMeasure.addNote(new Note(startTime, duration, note, NotesPower[note], prevContinue));
					
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
					System.out.println("wow");//ͬһ���������ɿ��͸�����
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
					case 0x2f:{
						return parseResult;
					}
					}
				}
				}
				break;
			}
			}
		}
		return parseResult;
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
