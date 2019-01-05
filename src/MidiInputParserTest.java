import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;

import org.junit.jupiter.api.Test;

class MidiInputParserTest {

	@Test
	void testGetDeltaTime() {
		byte[] test= {(byte) 0x40,(byte) 0x80,(byte) 0x80, 0x00};
		int testNum=0x40;

		assertEquals(MidiInputParser.getDeltaTime(new ByteArrayInputStream(test)), testNum);
	}


}
