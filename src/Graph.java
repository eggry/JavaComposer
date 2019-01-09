import java.util.ArrayList;

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
		return 0;
	}
}
