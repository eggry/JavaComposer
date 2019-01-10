
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Random;
import java.util.TreeMap;


public class MarkovChain {
	final int judgeEndExceedAveProbability=50;//�ж������ڳ���ƽ��ֵʱ��ͣ
	final int judgeEndMeetTailProbability=30;//�ж�������������β��ʱ����ͣ
	final int judgeEndRandomStopProbability=1;//�ж�����ûɶ�¶���ͣ
	final int selectOrignalHeadProbability=60;//�ж����ʴ�ԭ��ͷѡ��ͷ
	final int selectRandomNextProbability=20;//�ж���������ʱ�����һ��
	public class Graph {//�ײ�Ϊ�ڽӱ�
		class Edge{//�߱��������
			int value;//��Ȩ��i->jת���˼��Σ�
			int nodeId;//��ָ�򼸺Ž��
			Edge next;//��һ�����
			public Edge(int value, int nodeId, Edge next) {
				this.value = value;
				this.nodeId = nodeId;
				this.next = next;
			}
		}
		class Node{//��㣬��¼�߱�ͷ�Լ����ߵ�Ȩֵ�ĺ�
			Edge head;//�߱��ͷ
			int totVal;//����Ȩֵ��
			int edgeCount;
			public Node() {
				this.head = null;
				this.totVal = 0;
				this.edgeCount = 0;
			}
			void addEdge(int to){
				Edge now = head;//��head��ʼ���������Ƿ��Ѿ���from->to�ı�
				while(now!=null&&now.nodeId!=to) {
					now=now.next;//û�ҵ������滹�У��ͼ�����
				}//�˳�ʱҪô���ҵ��������ߣ�now!=null��Ҫô��û�ҵ������ߣ�now==null
				if(now==null) {
					head=new Edge(1,to,head);//û�ҵ������±�
					edgeCount++;
				}else {
					now.value++;//�ҵ���ֱ�Ӹ��±�Ȩ
				}
				totVal++;//�����˸��½���ϵ���Ϣ
			}
			int selectNext() {
				System.out.print(result.size()+1+":\t");
				if(edgeCount==0) {//û���ȣ�ֱ�Ӵӡ�ͷ����ʼ
					System.out.print("Meet an end, ");
					return selectHead();
				}
				if(judgeProbability(selectRandomNextProbability)) {
					System.out.println("Choose random next");
					return r.nextInt(nextId);//�����¼��������һ����Ϊ��һ��
				}
				//���򣬰����ϸ���ѡȡ
				int randNum=r.nextInt(totVal);
				Edge now = head;
				while(now.value<=randNum) {//��������������ߵķ�Χ�ڣ������ߡ�
					randNum-=now.value;
					now=now.next;//û�ҵ����ͼ�����
				}//������α�֤�жϾ�ȷ�����ƹ���
				System.out.println("Choose ordered next from "+edgeCount+" node(s)");
				return now.nodeId;
			}
		}
		ArrayList<Node> nodes;//��㼯�ϣ�����0���
		
		public Graph() {
			nodes=new ArrayList<Node>();
		}
		public void addEdge(int from,int to) {
			while(nodes.size()<=Math.max(from, to)) {//����������ӽ�㣬����֤������һ�ο�����������֮�����һ��
				nodes.add(new Node());
			}
			nodes.get(from).addEdge(to);//������Ӧ���ļӱ��¼�
		}
		public int selectNext(int nodeId) {//�����ڱ��nodeId�Ľ�㣬����������һ���Ľ����
			return nodes.get(nodeId).selectNext();//����ArrayList��ѯ���ۣ���һ�μ�����
		}
	}
	
	
	
	Graph countMap;//����ɷ���
	
	TreeMap<Measure,Integer> idAllocator;//��ɢ���ã�Measure->ID
	ArrayList<Measure> measures;//��ɢ���ã�ID->Measure
	int nextId;//��ɢ���ã���ţ���0��ʼ
	
	int totalMeasureCount;//һ�������˶���С�ڣ�ͳ���ñ�����
	int totalListCount;//һ�������˶��ٸ����֣�ͳ���ñ�����
	int totalVoidMeasureCount;
	
	ArrayList<Integer> headIds;//�ļ����ǿ�ͷ
	ArrayList<Integer> tailIds;//�ļ����ǽ�β
	
	LinkedList<Measure> result;//���߽��
	
	Random r;
	long seed;
	
	public long getSeed() {
		return seed;
	}

	public void setSeed(long seed) {
		this.seed = seed;
	}
	public void setSeed() {
		this.seed = new Date().getTime();
	}
	boolean judgeProbability(int probability) {//����һ���¼��ķ������ʣ��ж�Ҫ��Ҫ��������֤�жϾ�ȷ�����ƹ���
		int randNum=r.nextInt(100);
		return 0<=randNum&&randNum<probability;
	}
	int getAverageLength() {
		return (totalMeasureCount-totalVoidMeasureCount)/totalListCount;
	}
	public MarkovChain() {//��ʼ��
		countMap=new Graph();
		idAllocator=new TreeMap<Measure,Integer>();
		measures=new ArrayList<Measure>();
		nextId=0;
		totalMeasureCount=0;
		totalListCount=0;
		totalVoidMeasureCount=0;
		headIds=new ArrayList<Integer>();
		tailIds=new ArrayList<Integer>();
		result=new LinkedList<Measure>();
		seed=new Date().getTime();
		r=new Random(seed);
	}
	public int uniqueMeasureCount() {//ͳ��ȥ�غ��С����
		return nextId;
	}
	public void addList(LinkedList<Measure> list) {//����һ�����ӵķ���������ӵ�����
		totalMeasureCount+=list.size();//ͳ����Ϣ
		totalListCount++;
		int prevNode=-1;//ǰһ��С�ڵı��
		int voidMeasureCount=0;
		int voidMeasureId=0;
		for(Measure m:list) {//����
			if(!idAllocator.containsKey(m)) {//�µ�С�ڣ���ɢ��
				idAllocator.put(m, nextId++);
				measures.add(m);
			}
			if(prevNode==-1&&m.notes.size()==0) {//������ͷ�հ�
				totalVoidMeasureCount++;
				continue;
			}
			int nowNode=idAllocator.get(m);//���С�ڱ��
			if(prevNode!=-1) {
				if(m.notes.size()==0) {
					voidMeasureCount++;
					totalVoidMeasureCount++;
					voidMeasureId=nowNode;
					continue;
				}
				if(voidMeasureCount>0) {
					countMap.addEdge(prevNode,voidMeasureId);//ֻд��һ��
					prevNode=voidMeasureId;
					voidMeasureCount=0;
					totalVoidMeasureCount--;
				}
				countMap.addEdge(prevNode, nowNode);//��ͨ�ģ���¼ת�ƹ�ϵ
			}else {
				headIds.add(nowNode);//��һ�������뿪ͷ��
			}
			prevNode=nowNode;//������һ��
		}
		tailIds.add(prevNode);//��¼��β
	}
	int selectHead() {//��һ����ͷ
		if(headIds.size()==0) {
			return -1;//ɶҲû�У���������
		}
		System.out.print(result.size()+1+":\t");
		if(judgeProbability(selectOrignalHeadProbability)) {
			System.out.println("Choose original head");
			return headIds.get(r.nextInt(headIds.size()));//���У��ӿ�ͷ�б�ѡһ��
		}
		System.out.println("Choose random head");
		return r.nextInt(nextId);//�������ѡһ������ͷ
	}
	boolean judgeEnd(int id) {//�������ڵ�С�ڣ�����Ҫ��Ҫ����
		if(id==-1) {//�����Խ���ˣ��϶�����
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
	public LinkedList<Measure> generate() {//�����������������
		int nowId=selectHead();//��ѡһ�����
		while(!judgeEnd(nowId)) {//����������ͼ�����
			result.add(measures.get(nowId));//���������
			nowId=countMap.selectNext(nowId);//�ٴ����������һ���ߵ��Ķ�
		}
		if(nowId!=-1) {//���������Ϊ��·���ߣ��Ͱ�����Ǹ�����
			result.add(measures.get(nowId));
		}
		return result;
	}

	public String toString() {
		return "MarkovChain [UniqueMeasureCount=" + nextId + ", ReceivedMeasureCount=" + totalMeasureCount+ ", totalVoidMeasureCountInHeadAndTails=" +totalVoidMeasureCount +
				", AverageLength=" + getAverageLength() + ", seed=" + seed+ "]";
	}
}
