
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

public class MarkovChain {
	Map<Measure,Integer> idAllocator;//��ɢ����
	int nextId;
	int totalMeasureCount;//һ���ж���С��
	LinkedList<Integer> headIds;//�ļ����ǿ�ͷ
	LinkedList<Integer> tailIds;//�ļ����ǽ�β
	LinkedList<Measure> result;
	int[][] countMap=new int[10000][10000];//cM[i][j]:i��������j�Ĵ���
	public MarkovChain() {
		nextId=0;
		totalMeasureCount=0;
		idAllocator=new TreeMap<Measure,Integer>();
		headIds=new LinkedList<Integer>();
		tailIds=new LinkedList<Integer>();
		
	}
	public int uniqueMeasureCount() {
		return nextId;
	}
	public void addList(LinkedList<Measure> list) {//����һ��
		int prevNode=-1;
		totalMeasureCount+=list.size();
		for(Measure m:list) {
			if(!idAllocator.containsKey(m)) {
				idAllocator.put(m, nextId++);
			}
			int nowNode=idAllocator.get(m);
			if(prevNode!=-1) {
				countMap[prevNode][nowNode]++;
			}else {
				headIds.add(nowNode);
			}
			prevNode=nowNode;
		}
		tailIds.add(prevNode);
	}
	public void generate() {
		
	}
}
