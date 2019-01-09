import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class GraphTest {

	@Test
	void testGraph() {
		Graph G=new Graph();
		G.addEdge(1, 2);
		G.addEdge(2, 5);
		G.addEdge(5, 3);
		G.addEdge(3, 4);
		G.addEdge(5, 3);
		G.addEdge(3, 1);
		G.addEdge(1, 3);
		G.addEdge(1, 3);
		G.addEdge(0, 0);
	}


}
