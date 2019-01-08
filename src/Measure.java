import java.util.Set;
import java.util.TreeSet;

public class Measure implements Comparable<Measure>{
	private Set<Note> notes;
	public Measure() {
		notes=new TreeSet<Note>();
	}
	public void addNote(Note note) {
		notes.add(note);
	}
	public int noteCount() {
		return notes.size();
	}

	@Override
	public boolean equals(Object obj) {//м╛ио
		return this.toString()==obj.toString();
	}
	@Override
	public String toString() {
		String ret="Measure:\n";
		for(Note n:notes) {
			ret+='\t'+n.toString()+'\n';
		}
		return ret;
	}
	@Override
	public int compareTo(Measure arg0) {
		return this.toString().compareTo(arg0.toString());
	}
}
