import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

class MidiOutputParser{
	DataOutputStream dos;
	ArrayList<Byte> music=new ArrayList<Byte>();//按byte储存要输出的数据
	public MidiOutputParser(DataOutputStream dataOutputStream) {
		dos=dataOutputStream;
	}
	void transfer(int x){//不定长数字转化
		byte[] bit=new byte[1010];
		int now=-1;
		byte temp;
		if(x==0) {//0的时候要特判一下
			music.add((byte) 0);
			return ;
		}
		while(x!=0) {//先拆分成二进制
			bit[++now]=(byte)(x%2);
			x=x/2;
		}
		while(now%7!=0) {//7位一组，不够用0补齐
			now++;
			bit[now]=0;
		}
		while(now>7) {//不到最后，转换为正常byte类型
			temp=0;
			temp+=8*1+4*bit[now]+2*bit[now-1]+bit[now-2];
			music.add(temp);
			temp=0;
			temp+=8*bit[now-3]+4*bit[now-4]+2*bit[now-5]+bit[now-6];
			music.add(temp);
			now-=7;
		}
		if(now==7) {//当到结束时
			temp=0;
			temp+=8*0+4*bit[now]+2*bit[now-1]+bit[now-2];
			music.add(temp);
			temp=0;
			temp+=8*bit[now-3]+4*bit[now-4]+2*bit[now-5]+bit[now-6];
			music.add(temp);
			now-=7;
		}
	}
	private int max(int xx,int yy) {
		if(xx>yy)
			return xx;
		return yy;
	}
	void writeFile(LinkedList<Measure> filee) {
		final byte[] head= {0x4d,0x54,0x68,0x64,0x00,0x00,0x00,0x06,0x00,0x00,0x00,0x01,0x00,0x78,0x4d,0x54,0x72,0x6B};//文件头的头块
		Note[] temp=new Note[1000010];
		int top,end,num;
		try {
			dos.write(head);//输出头块
			top=-1;
			end=-1;
			num=0;
			for(Measure str:filee){//扫描音符，并添加关音操作，并将start time转化为全局时间
				for(Note value:str.notes) {
					temp[++top]=new Note(num*(0x78)+value.startTime,value.duration,value.keyName,value.power,value.prevContinue);
					temp[++top]=new Note(num*(0x78)+value.startTime+value.duration,0,value.keyName,0,value.prevContinue);
				}
				num++;
			}
			Arrays.sort(temp,0,top+1);//排序输出
			for(int i=0;i<=top;i++) {
				if(i==0)
					transfer(0);
				else
					transfer(temp[i].startTime-temp[i-1].startTime);
				music.add((byte) 0x90);
				music.add((byte) temp[i].keyName);
				music.add((byte) temp[i].power);
				end=max(end,temp[i].startTime);//结束肯定是关闭某个音符，找start最大的
			}
			transfer(0);//轨道结束时间
			dos.writeInt(music.size()+3);
			for(int i=0;i<music.size();i++) //依次输出
				dos.write(music.get(i));
			dos.write(0xff);//轨道结束标识
			dos.write(0x2F);
			dos.write(0x00);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
