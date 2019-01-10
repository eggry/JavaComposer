import java.util.TreeSet;

public class Measure implements Comparable<Measure>{
	TreeSet<Note> notes;
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
	public boolean equals(Object obj) {//¼òµ¥´Ö±©
		return this.toString()==obj.toString();
	}
	@Override
	public String toString() {
		StringBuffer s=new StringBuffer("Measure:\n");
		for(Note n:notes) {
			s.append('\t');
			s.append(n);
			s.append('\n');
		}
		return s.toString();
	}
	@Override
	public int compareTo(Measure arg0) {
		return this.toString().compareTo(arg0.toString());
	}
}
