
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;


public class MarkovChain {
	private final int judgeEndExceedAveProbability=500;//有多大概率在超过平均值时暂停
	private final int judgeEndMeetTailProbability=300;//有多大概率在碰到结尾的时候暂停
	private final int judgeEndRandomStopProbability=5;//有多大概率没啥事儿就停
	private final int selectOrignalHeadProbability=600;//有多大概率从原开头选开头
	private final int selectRandomNextProbability=200;//有多大概率游走时随便找一个
	private class Graph {//底层为邻接表
		private class Edge{//边表（链表）结点
			private int value;//边权（i->j转移了几次）
			private int nodeId;//边指向几号结点
			private Edge next;//下一个结点
			public Edge(int value, int nodeId, Edge next) {
				this.value = value;
				this.nodeId = nodeId;
				this.next = next;
			}
		}
		private class Node{//结点，记录边表头以及出边的权值的和
			private Edge head;//边表的头
			private int totVal;//出边权值和
			private int edgeCount;
			public Node() {
				this.head = null;
				this.totVal = 0;
				this.edgeCount = 0;
			}
			private void addEdge(int to){
				Edge now = head;//从head开始遍历，找是否已经有from->to的边
				while(now!=null&&now.nodeId!=to) {
					now=now.next;//没找到，下面还有，就继续找
				}//退出时要么是找到了这条边，now!=null，要么是没找到这条边，now==null
				if(now==null) {
					head=new Edge(1,to,head);//没找到，加新边
					edgeCount++;
				}else {
					now.value++;//找到，直接更新边权
				}
				totVal++;//别忘了更新结点上的信息
			}
			private int selectNext() {
				System.out.print(result.size()+1+":\t");
				if(edgeCount==0) {//没出度，直接从“头”开始
					System.out.print("Meet an end, ");
					return selectHead();
				}
				if(judgeProbability(selectRandomNextProbability)) {
					System.out.print("Choose random next:");
					return r.nextInt(nextId);//命中事件，随便找一个作为下一步
				}
				//否则，按链上概率选取
				int randNum=r.nextInt(totVal);
				Edge now = head;
				while(now.value<=randNum) {//随机数不在这条边的范围内，继续走。
					randNum-=now.value;
					now=now.next;//没找到，就继续找
				}//上面这段保证判断精确，我推过了
				System.out.print("Choose ordered next from "+edgeCount+" node(s):");
				return now.nodeId;
			}
		}
		private ArrayList<Node> nodes;//结点集合，结点从0编号
		
		public Graph() {
			nodes=new ArrayList<Node>();
		}
		public void addEdge(int from,int to) {
			while(nodes.size()<=Math.max(from, to)) {//如果不够，加结点，可以证明，第一次可能是两个，之后最多一个
				nodes.add(new Node());
			}
			nodes.get(from).addEdge(to);//调用相应结点的加边事件
		}
		public int selectNext(int nodeId) {//现在在编号nodeId的结点，返回游走下一步的结点编号
			return nodes.get(nodeId).selectNext();//减少ArrayList查询代价，查一次记下来
		}
	}
	
	
	
	private Graph countMap;//马尔可夫链
	
	private TreeMap<Measure,Integer> idAllocator;//离散化用，Measure->ID
	private ArrayList<Measure> measures;//离散化用，ID->Measure
	private int nextId;//离散化用，编号，从0开始
	
	private int totalMeasureCount;//一共处理了多少小节（统计用变量）
	private int totalListCount;//一共处理了多少个音乐（统计用变量）
	private int totalVoidMeasureCount;
	
	private ArrayList<Integer> headIds;//哪几个是开头
	private TreeSet<Integer> tailIds;//哪几个是结尾
	
	private LinkedList<Measure> result;//游走结果
	
	private Random r;
	private long seed;
	
	public long getSeed() {
		return seed;
	}

	public void setSeed(long seed) {
		this.seed = seed;
	}
	public void setSeed() {
		this.seed = new Date().getTime();
	}
	private boolean judgeProbability(int probability) {//给定一个事件的发生概率，判断要不要发生，保证判断精确，我推过了
		int randNum=r.nextInt(1000);
		return 0<=randNum&&randNum<probability;
	}
	public int getAverageLength() {
		return (totalMeasureCount-totalVoidMeasureCount)/(totalListCount+1);
	}
	public MarkovChain() {//初始化
		countMap=new Graph();
		idAllocator=new TreeMap<Measure,Integer>();
		measures=new ArrayList<Measure>();
		nextId=0;
		totalMeasureCount=0;
		totalListCount=0;
		totalVoidMeasureCount=0;
		headIds=new ArrayList<Integer>();
		tailIds=new TreeSet<Integer>();
		result=new LinkedList<Measure>();
		seed=new Date().getTime();
		r=new Random(seed);
	}
	public int uniqueMeasureCount() {//统计去重后的小节数
		return nextId;
	}
	public void addList(LinkedList<Measure> list) {//接受一首曲子的分析结果，加到链里
		totalMeasureCount+=list.size();//统计信息
		totalListCount++;
		int prevNode=-1;//前一个小节的编号
		int voidMeasureCount=0;
		int voidMeasureId=0;
		for(Measure m:list) {//遍历
			if(!idAllocator.containsKey(m)) {//新的小节，离散化
				idAllocator.put(m, nextId++);
				measures.add(m);
			}
			if(prevNode==-1&&m.notes.size()==0) {//跳过开头空白
				totalVoidMeasureCount++;
				continue;
			}
			int nowNode=idAllocator.get(m);//获得小节编号
			if(prevNode!=-1) {
				if(m.notes.size()==0) {
					voidMeasureCount++;
					totalVoidMeasureCount++;
					voidMeasureId=nowNode;
					continue;
				}
				if(voidMeasureCount>0) {
					countMap.addEdge(prevNode,voidMeasureId);//只写入一个
					prevNode=voidMeasureId;
					voidMeasureCount=0;
					totalVoidMeasureCount--;
				}
				countMap.addEdge(prevNode, nowNode);//普通的，记录转移关系
			}else {
				headIds.add(nowNode);//第一个，加入开头池
			}
			prevNode=nowNode;//继续下一个
		}
		tailIds.add(prevNode);//记录结尾
	}
	private int selectHead() {//找一个开头
		if(headIds.size()==0) {
			System.out.print("No head:");
			return -1;//啥也没有，不用找了
		}
		System.out.print(result.size()+1+":\t");
		if(judgeProbability(selectOrignalHeadProbability)) {
			System.out.print("Choose original head:");
			return headIds.get(r.nextInt(headIds.size()));//命中，从开头列表选一个
		}
		System.out.print("Choose random head:");
		return r.nextInt(nextId);//否则，随便选一个当开头
	}
	private boolean judgeEnd(int id) {//给定现在的小节，决定要不要结束
		if(id==-1) {//如果都越界了，肯定结束
			System.out.println("Exit because no measure in the Chain");
			return true;
		}
		
		if(result.size()> getAverageLength() && judgeProbability(judgeEndExceedAveProbability) ) {
			System.out.println("Exit because outAve");
			return true;
		}
		if(tailIds.contains(id) && judgeProbability(judgeEndMeetTailProbability) ) {
			System.out.println("Exit because meet tail");
			return true;
		}
		if(judgeProbability(judgeEndRandomStopProbability) ) {
			System.out.println("Exit because no because");
			return true;
		}
		return false;
	}
	public LinkedList<Measure> generate() {//随机游走生成新曲子
		int nowId=selectHead();//先选一个起点
		System.out.println(nowId);
		while(!judgeEnd(nowId)) {//如果不结束就继续走
			result.add(measures.get(nowId));//把这个加上
			nowId=countMap.selectNext(nowId);//再从链里决定下一步走到哪儿
			System.out.println(nowId);
		}
		if(nowId!=-1) {//如果不是因为无路可走，就把最后那个加上
			result.add(measures.get(nowId));
		}
		return result;
	}

	public String toString() {
		return "MarkovChain [UniqueMeasureCount=" + nextId + ", ReceivedMeasureCount=" + totalMeasureCount+ ", totalVoidMeasureCountInHeadAndTails=" +totalVoidMeasureCount +
				", AverageLength=" + getAverageLength() + ", seed=" + seed+ "]";
	}
}
