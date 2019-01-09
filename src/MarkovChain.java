
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeMap;

public class MarkovChain {
	Graph countMap;//����ɷ���
	
	TreeMap<Measure,Integer> idAllocator;//��ɢ���ã�Measure->ID
	ArrayList<Measure> measures;//��ɢ���ã�ID->Measure
	int nextId;//��ɢ���ã���ţ���0��ʼ
	
	int totalMeasureCount;//һ�������˶���С�ڣ�ͳ���ñ�����
	
	ArrayList<Integer> headIds;//�ļ����ǿ�ͷ
	ArrayList<Integer> tailIds;//�ļ����ǽ�β
	
	LinkedList<Measure> result;//���߽��
	
	static final int selectHeadProbability=100;//�ж����ʴ�ԭ��ͷѡ��ͷ
	
	

	public MarkovChain() {//��ʼ��
		countMap=new Graph();
		idAllocator=new TreeMap<Measure,Integer>();
		measures=new ArrayList<Measure>();
		nextId=0;
		totalMeasureCount=0;
		headIds=new ArrayList<Integer>();
		tailIds=new ArrayList<Integer>();
		result=new LinkedList<Measure>();
	}
	public int uniqueMeasureCount() {//ͳ��ȥ�غ��С����
		return nextId;
	}
	public void addList(LinkedList<Measure> list) {//����һ�����ӵķ���������ӵ�����
		totalMeasureCount+=list.size();//ͳ����Ϣ
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
		return 0;
	}
	boolean judgeEnd(int id) {//����Ҫ��Ҫ����
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
