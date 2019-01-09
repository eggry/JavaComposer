
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeMap;

public class MarkovChain {
	Graph countMap;//马尔可夫链
	
	TreeMap<Measure,Integer> idAllocator;//离散化用，Measure->ID
	ArrayList<Measure> measures;//离散化用，ID->Measure
	int nextId;//离散化用，编号，从0开始
	
	int totalMeasureCount;//一共处理了多少小节（统计用变量）
	
	ArrayList<Integer> headIds;//哪几个是开头
	ArrayList<Integer> tailIds;//哪几个是结尾
	
	LinkedList<Measure> result;//游走结果
	
	static final int selectHeadProbability=100;//有多大概率从原开头选开头
	
	

	public MarkovChain() {//初始化
		countMap=new Graph();
		idAllocator=new TreeMap<Measure,Integer>();
		measures=new ArrayList<Measure>();
		nextId=0;
		totalMeasureCount=0;
		headIds=new ArrayList<Integer>();
		tailIds=new ArrayList<Integer>();
		result=new LinkedList<Measure>();
	}
	public int uniqueMeasureCount() {//统计去重后的小节数
		return nextId;
	}
	public void addList(LinkedList<Measure> list) {//接受一首曲子的分析结果，加到链里
		totalMeasureCount+=list.size();//统计信息
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
		return 0;
	}
	boolean judgeEnd(int id) {//决定要不要结束
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
