package me.gking2224.buildtools.util

import static org.junit.Assert.*

import org.junit.Test

 class VersionTest {

    @Test
    void testConstruct_NonSnapshot() {
        def v = "1.0.0"
     
        def vObj = new Version(v)
        assertTrue !vObj.isSnapshot
        assertEquals(v, vObj.rawVersion)
        assertEquals(v, vObj.versionNum)
        assertEquals("[1, 0, 0]", (Arrays.asList(vObj.parts)).toListString())
    }

    @Test
    void testConstruct_Snapshot() {
        def v = "1.0.0-SNAPSHOT"
     
        def vObj = new Version(v)
        assertTrue vObj.isSnapshot
        assertEquals(v, vObj.rawVersion)
        assertEquals("1.0.0", vObj.versionNum)
        assertEquals("[1, 0, 0]", (Arrays.asList(vObj.parts)).toListString())
    }
    
    @Test
    void testConstructor_Parts() {
        def v = new Version(1,2,3)
        assertEquals("1.2.3", v.rawVersion)
        assertEquals("1.2.3", v.versionNum)
        assertFalse(v.isSnapshot)
    }
    
    @Test
    void testConstructor_SnapshotParts() {
        def v = new Version(false, 1,2,3)
        assertEquals("1.2.3", v.rawVersion)
        assertEquals("1.2.3", v.versionNum)
        assertFalse(v.isSnapshot)
        
        
        def v2 = new Version(true, 1,2,3)
        assertEquals("1.2.3-SNAPSHOT", v2.rawVersion)
        assertEquals("1.2.3", v2.versionNum)
        assertTrue(v2.isSnapshot)
    }

    @Test
    void testIncrement_NoArgs() {
        assertEquals("1.0.1-SNAPSHOT", new Version("1.0.0-SNAPSHOT").increment().rawVersion)
        assertEquals("1.0.2-SNAPSHOT", new Version("1.0.1").increment().rawVersion)
        assertEquals("2.3.8-SNAPSHOT", new Version("2.3.7").increment().rawVersion)
        assertEquals("10.11.13-SNAPSHOT", new Version("10.11.12-SNAPSHOT").increment().rawVersion)
    }

    @Test
    void testIncrement_Type() {
        assertEquals("1.0.1-SNAPSHOT", new Version("1.0.0-SNAPSHOT").increment(Version.IncType.PATCH).rawVersion)
        assertEquals("1.0.1-SNAPSHOT", new Version("1.0.0").increment(Version.IncType.PATCH).rawVersion)
        assertEquals("1.1.0-SNAPSHOT", new Version("1.0.2").increment(Version.IncType.MINOR).rawVersion)
        assertEquals("1.1.0-SNAPSHOT", new Version("1.0.2-SNAPSHOT").increment(Version.IncType.MINOR).rawVersion)
        assertEquals("2.0.0-SNAPSHOT", new Version("1.0.2").increment(Version.IncType.MAJOR).rawVersion)
        assertEquals("2.0.0-SNAPSHOT", new Version("1.0.2-SNAPSHOT").increment(Version.IncType.MAJOR).rawVersion)
    }

    @Test
    void testRelease() {
        assertEquals("1.0.0", new Version("1.0.0-SNAPSHOT").release().rawVersion)
        assertEquals("21.35.57", new Version("21.35.57-SNAPSHOT").release().rawVersion)
        assertEquals("21.35.57", new Version("21.35.57").release().rawVersion)
    }
    
    @Test
    void testPartsAsString() {
        assert "1.2.3" == Version.partsAsString(false, 1, 2, 3)
        assert "3.2.1-SNAPSHOT" == Version.partsAsString(true, 3, 2, 1)
    }
    
    @Test
    void testToString() {
        assertEquals("1.2.3-SNAPSHOT", new Version(true, 1, 2, 3).toString())
        assertEquals("10.11.12", new Version(false, 10, 11, 12).toString())
    }
}
