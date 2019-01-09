
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.TreeMap;


public class MarkovChain {
	public class Graph {//底层为邻接表
		class Edge{//边表（链表）结点
			int value;//边权（i->j转移了几次）
			int nodeId;//边指向几号结点
			Edge next;//下一个结点
			public Edge(int value, int nodeId, Edge next) {
				this.value = value;
				this.nodeId = nodeId;
				this.next = next;
			}
		}
		class Node{//结点，记录边表头以及出边的权值的和
			Edge head;//边表的头
			int totVal;//出边权值和
			public Node() {
				this.head = null;
				this.totVal = 0;
			}
		}
		ArrayList<Node> nodes;//结点集合，结点从0编号
		
		public Graph() {
			nodes=new ArrayList<Node>();
		}
		public void addEdge(int from,int to) {
			while(nodes.size()<=Math.max(from, to)) {//如果不够，加结点，可以证明，第一次可能是两个，之后最多一个
				nodes.add(new Node());
			}
			final Node nowNode=nodes.get(from);//减少ArrayList查询代价，查一次记下来
			Edge now = nowNode.head;//从head开始遍历，找是否已经有from->to的边
			while(now!=null&&now.nodeId!=to) {
				now=now.next;//没找到，下面还有，就继续找
			}//退出时要么是找到了这条边，now!=null，要么是没找到这条边，now==null
			if(now==null) {
				nowNode.head=new Edge(1,to,nowNode.head);//没找到，加新边
			}else {
				now.value++;//找到，直接更新边权
			}
			nowNode.totVal++;//别忘了更新结点上的信息
		}
		public int selectNext(int nodeId) {//现在在编号nodeId的结点，返回游走下一步的结点编号
			final Node nowNode=nodes.get(nodeId);//减少ArrayList查询代价，查一次记下来
			if(nowNode.totVal==0) {//没出度，直接报-1
				return -1;
			}
			int randSelectProbability=40;//有多大概率随便找一个
			if(judgeProbability(randSelectProbability)) {
				System.out.println("Choose random next");
				return r.nextInt(nextId);//命中事件，随便找一个作为下一步
			}
			//否则，按链上概率选取
			int randNum=r.nextInt(nowNode.totVal);
			Edge now = nowNode.head;
			while(now.value<=randNum) {//随机数不在这条边的范围内，继续走。
				randNum-=now.value;
				now=now.next;//没找到，就继续找
			}//上面这段保证判断精确，我推过了
			System.out.println("Choose ordered next");
			return now.nodeId;
		}
	}
	
	
	
	Graph countMap;//马尔可夫链
	
	TreeMap<Measure,Integer> idAllocator;//离散化用，Measure->ID
	ArrayList<Measure> measures;//离散化用，ID->Measure
	int nextId;//离散化用，编号，从0开始
	
	int totalMeasureCount;//一共处理了多少小节（统计用变量）
	int totalListCount;//一共处理了多少个音乐（统计用变量）
	
	ArrayList<Integer> headIds;//哪几个是开头
	ArrayList<Integer> tailIds;//哪几个是结尾
	
	LinkedList<Measure> result;//游走结果
	
	Random r=new Random();
	
	boolean judgeProbability(int probability) {//给定一个事件的发生概率，判断要不要发生，保证判断精确，我推过了
		int randNum=r.nextInt(100);
		return 0<=randNum&&randNum<probability;
	}

	public MarkovChain() {//初始化
		countMap=new Graph();
		idAllocator=new TreeMap<Measure,Integer>();
		measures=new ArrayList<Measure>();
		nextId=0;
		totalMeasureCount=0;
		totalListCount=0;
		headIds=new ArrayList<Integer>();
		tailIds=new ArrayList<Integer>();
		result=new LinkedList<Measure>();
	}
	public int uniqueMeasureCount() {//统计去重后的小节数
		return nextId;
	}
	public void addList(LinkedList<Measure> list) {//接受一首曲子的分析结果，加到链里
		totalMeasureCount+=list.size();//统计信息
		totalListCount++;
		int prevNode=-1;//前一个小节的编号
		for(Measure m:list) {//遍历
			if(!idAllocator.containsKey(m)) {//新的小节，离散化
				idAllocator.put(m, nextId++);
				measures.add(m);
			}
			int nowNode=idAllocator.get(m);//获得小节编号
			if(prevNode!=-1) {
				countMap.addEdge(prevNode, nowNode);//普通的，记录转移关系
			}else {
				headIds.add(nowNode);//第一个，加入开头池
			}
			prevNode=nowNode;//继续下一个
		}
		tailIds.add(prevNode);//记录结尾
	}
	int selectHead() {//找一个开头
		if(headIds.size()==0) {
			return -1;//啥也没有，不用找了
		}
		final int selectHeadProbability=50;//有多大概率从原开头选开头
		if(judgeProbability(selectHeadProbability)) {
			System.out.println("Choose original head");
			return headIds.get(r.nextInt(headIds.size()));//命中，从开头列表选一个
		}
		System.out.println("Choose random head");
		return r.nextInt(nextId);//否则，随便选一个当开头
	}
	boolean judgeEnd(int id) {//给定现在的小节，决定要不要结束
		final int exceedAveProbability=60;//有多大概率在超过平均值时暂停
		final int meetTailProbability=60;//有多大概率在碰到结尾的时候暂停
		final int randomStopProbability=5;//有多大概率没啥事儿就停
		if(id==-1) {//如果都越界了，肯定结束
			return true;
		}
		
		if(result.size()> totalMeasureCount/totalListCount && judgeProbability(exceedAveProbability) ) {
			System.out.println("Exit because outAve");
			return true;
		}
		if(tailIds.contains(id) && judgeProbability(meetTailProbability) ) {
			System.out.println("Exit because meet tail");
			return true;
		}
		if(judgeProbability(randomStopProbability) ) {
			System.out.println("Exit because no because");
			return true;
		}
		return false;
	}
	public void generate() {//随机游走生成新曲子
		int nowId=selectHead();//先选一个起点
		while(!judgeEnd(nowId)) {//如果不结束就继续走
			result.add(measures.get(nowId));//把这个加上
			nowId=countMap.selectNext(nowId);//再从链里决定下一步走到哪儿
		}
		if(nowId!=-1) {//如果不是因为无路可走，就把最后那个加上
			result.add(measures.get(nowId));
		}
		
	}
}
