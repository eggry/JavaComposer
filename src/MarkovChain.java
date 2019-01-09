
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

public class MarkovChain {
	Map<Measure,Integer> idAllocator;//离散化用
	int nextId;
	int totalMeasureCount;//一共有多少小节
	LinkedList<Integer> headIds;//哪几个是开头
	LinkedList<Integer> tailIds;//哪几个是结尾
	LinkedList<Measure> result;
	int[][] countMap=new int[10000][10000];//cM[i][j]:i接下来是j的次数
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
	public void addList(LinkedList<Measure> list) {//接受一个
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
