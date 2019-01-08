
public class Note implements Comparable<Note>{
	public Note(int startTime, int duration, int keyName, int power, boolean prevContinue) {
		this.startTime = startTime;
		this.duration = duration;
		this.keyName = keyName;
		this.power = power;
		this.prevContinue = prevContinue;
	}

	@Override
	public String toString() {
		return "startTime=" + startTime + ", duration=" + duration + ", keyName=" + keyName + ", power=" + power
				+ ", prevContinue=" + prevContinue ;
	}

	int startTime;//��С���еĿ�ʼʱ��
	int duration;//��С���еĳ���ʱ��
	int keyName;//����
	int power;// ����
	boolean prevContinue;//�Ƿ�����һ��С�ڵ��������������ǰ������ֻ��Ҫһ�����У��������л�����
	
	@Override
	public int compareTo(final Note arg0) {//�ȱȽϿ�ʼʱ�䣬�ٱȽϽ���ʱ�䣬�ٱȽ�����
		return this.startTime==arg0.startTime?
				this.duration==arg0.duration?
						this.keyName-arg0.keyName
						:this.duration-arg0.duration
				:this.startTime-arg0.startTime;
	}
}
