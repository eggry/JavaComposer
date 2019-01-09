import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MidiOutputParserTest {

	@Test
	void testWriteFile() {
		MidiOutputParser test=new MidiOutputParser(null);
		test.transfer(1);
		System.out.println(test.music);
	}

}
