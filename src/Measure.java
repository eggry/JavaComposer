import java.util.Set;
import java.util.TreeSet;

public class Measure {
	private Set<Note> notes;
	public Measure() {
		notes=new TreeSet<Note>();
	}
	public void addNote(Note note) {
		notes.add(note);
	}
	@Override
	public String toString() {
		String ret="Measure:\n";
		for(Note n:notes) {
			ret+='\t'+n.toString()+'\n';
		}
		return ret;
	}
}
