package me.gking2224.buildtools.util

import static org.junit.Assert.*

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


class FileHelperTest {

    def fh
    def file
    def f2
    
    @Before
    void before() {
        fh = FileHelper.instance()
        file = new File("/tmp/deleteme")
        file.createNewFile()
        f2 = new File("/tmp/alsodeleteme")
        f2.createNewFile()
    }
    
    @Test
    public void testFilesAsString() {
        
        assertEquals("/tmp/deleteme", fh.filesAsString("/tmp/deleteme"))
        assertEquals("/tmp/deleteme", fh.filesAsString(file))
        assertEquals("[/tmp/deleteme, /tmp/deleteme]", fh.filesAsString(["/tmp/deleteme", "/tmp/deleteme" ]))
        assertEquals("[/tmp/deleteme, /tmp/deleteme]", fh.filesAsString([file, file]))
        
    }
    
    @Test
    public void asFile() {
        
        assertEquals(file, fh.asFile(null, "/tmp/deleteme"))
        assertEquals(file, fh.asFile("/tmp", "deleteme"))
        assertEquals(file, fh.asFile(null, file))
        assertEquals(file, fh.asFile("/tmp", file))
        assertEquals([file, file], fh.asFile("/tmp", ["deleteme", "/deleteme"]))
        assertEquals([file, file], fh.asFile("/tmp", [file, file]))
        assertEquals([file, file], fh.asFile(null, ["/tmp/deleteme", "/tmp/deleteme"]))
    }
    
    @Test
    public void fileCollection() {
        assertEquals([file], fh.fileCollection(null, "/tmp/deleteme"))
        assertEquals([file], fh.fileCollection(null, file))
        assertEquals([file], fh.fileCollection("/tmp", file))
        assertEquals([file], fh.fileCollection("/tmp", "deleteme"))
        assertEquals([file, file], fh.fileCollection("/tmp", ["deleteme", "deleteme"]))
        assertEquals([file, file], fh.fileCollection("/tmp", [file, file]))
        assertEquals([file, file], fh.fileCollection("/tmp", [file, "deleteme"]))
        assertEquals([], fh.fileCollection("/tmp", null))
        assertEquals([], fh.fileCollection("/tmp", []))
    }
    
    @Test
    public void filesFromPattern() {
        assertEquals([f2, file], fh.filesFromPattern(new File("/tmp/"), ".*delete.*"))
        assertEquals([f2, file], fh.filesFromPattern("/tmp/", ".*delete.*"))
        assertEquals([file], fh.filesFromPattern(new File("/tmp/"), "^delete.*"))
    }

}
