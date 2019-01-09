import java.util.ArrayList;

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
		return 0;
	}
}
