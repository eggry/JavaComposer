
public class Note implements Comparable<Note>{
	int startTime;
	int duration;
	int keyName;
	boolean prevContinue;
	boolean nextContinue;
	@Override
	public int compareTo(final Note arg0) {//�ȱȽϿ�ʼʱ�䣬�ٱȽϽ���ʱ�䣬�ٱȽ�����
		return this.startTime==arg0.startTime?
				this.duration==arg0.duration?
						this.keyName-arg0.keyName
						:this.duration-arg0.duration
				:this.startTime-arg0.startTime;
	}
}
