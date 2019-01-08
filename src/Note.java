
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

	int startTime;//在小节中的开始时间
	int duration;//在小节中的持续时间
	int keyName;//名字
	int power;// 力度
	boolean prevContinue;//是否与上一个小节的这个音符相连，前后相连只需要一个就行，方便序列化处理
	
	@Override
	public int compareTo(final Note arg0) {//先比较开始时间，再比较结束时间，再比较音符
		return this.startTime==arg0.startTime?
				this.duration==arg0.duration?
						this.keyName-arg0.keyName
						:this.duration-arg0.duration
				:this.startTime-arg0.startTime;
	}
}
