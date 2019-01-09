
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.TreeMap;


public class MarkovChain {
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
			public Node() {
				this.head = null;
				this.totVal = 0;
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
			final Node nowNode=nodes.get(from);//����ArrayList��ѯ���ۣ���һ�μ�����
			Edge now = nowNode.head;//��head��ʼ���������Ƿ��Ѿ���from->to�ı�
			while(now!=null&&now.nodeId!=to) {
				now=now.next;//û�ҵ������滹�У��ͼ�����
			}//�˳�ʱҪô���ҵ��������ߣ�now!=null��Ҫô��û�ҵ������ߣ�now==null
			if(now==null) {
				nowNode.head=new Edge(1,to,nowNode.head);//û�ҵ������±�
			}else {
				now.value++;//�ҵ���ֱ�Ӹ��±�Ȩ
			}
			nowNode.totVal++;//�����˸��½���ϵ���Ϣ
		}
		public int selectNext(int nodeId) {//�����ڱ��nodeId�Ľ�㣬����������һ���Ľ����
			final Node nowNode=nodes.get(nodeId);//����ArrayList��ѯ���ۣ���һ�μ�����
			if(nowNode.totVal==0) {//û���ȣ�ֱ�ӱ�-1
				return -1;
			}
			int randSelectProbability=40;//�ж����������һ��
			if(judgeProbability(randSelectProbability)) {
				System.out.println("Choose random next");
				return r.nextInt(nextId);//�����¼��������һ����Ϊ��һ��
			}
			//���򣬰����ϸ���ѡȡ
			int randNum=r.nextInt(nowNode.totVal);
			Edge now = nowNode.head;
			while(now.value<=randNum) {//��������������ߵķ�Χ�ڣ������ߡ�
				randNum-=now.value;
				now=now.next;//û�ҵ����ͼ�����
			}//������α�֤�жϾ�ȷ�����ƹ���
			System.out.println("Choose ordered next");
			return now.nodeId;
		}
	}
	
	
	
	Graph countMap;//����ɷ���
	
	TreeMap<Measure,Integer> idAllocator;//��ɢ���ã�Measure->ID
	ArrayList<Measure> measures;//��ɢ���ã�ID->Measure
	int nextId;//��ɢ���ã���ţ���0��ʼ
	
	int totalMeasureCount;//һ�������˶���С�ڣ�ͳ���ñ�����
	int totalListCount;//һ�������˶��ٸ����֣�ͳ���ñ�����
	
	ArrayList<Integer> headIds;//�ļ����ǿ�ͷ
	ArrayList<Integer> tailIds;//�ļ����ǽ�β
	
	LinkedList<Measure> result;//���߽��
	
	Random r=new Random();
	
	boolean judgeProbability(int probability) {//����һ���¼��ķ������ʣ��ж�Ҫ��Ҫ��������֤�жϾ�ȷ�����ƹ���
		int randNum=r.nextInt(100);
		return 0<=randNum&&randNum<probability;
	}

	public MarkovChain() {//��ʼ��
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
	public int uniqueMeasureCount() {//ͳ��ȥ�غ��С����
		return nextId;
	}
	public void addList(LinkedList<Measure> list) {//����һ�����ӵķ���������ӵ�����
		totalMeasureCount+=list.size();//ͳ����Ϣ
		totalListCount++;
		int prevNode=-1;//ǰһ��С�ڵı��
		for(Measure m:list) {//����
			if(!idAllocator.containsKey(m)) {//�µ�С�ڣ���ɢ��
				idAllocator.put(m, nextId++);
				measures.add(m);
			}
			int nowNode=idAllocator.get(m);//���С�ڱ��
			if(prevNode!=-1) {
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
		final int selectHeadProbability=50;//�ж����ʴ�ԭ��ͷѡ��ͷ
		if(judgeProbability(selectHeadProbability)) {
			System.out.println("Choose original head");
			return headIds.get(r.nextInt(headIds.size()));//���У��ӿ�ͷ�б�ѡһ��
		}
		System.out.println("Choose random head");
		return r.nextInt(nextId);//�������ѡһ������ͷ
	}
	boolean judgeEnd(int id) {//�������ڵ�С�ڣ�����Ҫ��Ҫ����
		final int exceedAveProbability=60;//�ж������ڳ���ƽ��ֵʱ��ͣ
		final int meetTailProbability=60;//�ж�������������β��ʱ����ͣ
		final int randomStopProbability=5;//�ж�����ûɶ�¶���ͣ
		if(id==-1) {//�����Խ���ˣ��϶�����
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
	public void generate() {//�����������������
		int nowId=selectHead();//��ѡһ�����
		while(!judgeEnd(nowId)) {//����������ͼ�����
			result.add(measures.get(nowId));//���������
			nowId=countMap.selectNext(nowId);//�ٴ����������һ���ߵ��Ķ�
		}
		if(nowId!=-1) {//���������Ϊ��·���ߣ��Ͱ�����Ǹ�����
			result.add(measures.get(nowId));
		}
		
	}
}
