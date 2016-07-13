package me.gking2224.buildtools.util

import static org.junit.Assert.*

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


class FileHelperTest {

    def fh
    def file
    def f2
    
    def dupe1
    def dupe2
    def dupe3
    
    @Before
    void before() {
        fh = FileHelper.instance()
        new File("/tmp/tmp2/a/b/c").mkdirs()
        new File("/tmp/tmp2/x/y/z").mkdirs()
        
        dupe1 = new File("/tmp/tmp2/a/b/c/matching.file")
        dupe2 = new File("/tmp/tmp2/x/y/z/matching.file")
        dupe3 = new File("/tmp/tmp2/x/matching.file")
        dupe1.createNewFile()
        dupe2.createNewFile()
        dupe3.createNewFile()
        
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
        assertEquals(file, fh.asFile("/tmp/deleteme"))
        assertEquals(file, fh.asFile("/tmp", "deleteme"))
        assertEquals(file, fh.asFile(null, file))
        assertEquals(file, fh.asFile(file))
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
    
    @Test
    public void filesFromPatternWithDupes_NoMatch() {
        assertEquals([], fh.filesFromPattern(new File("/tmp/tmp2"), "matching.file"))
    }
    
    @Test
    public void filesFromPatternWithDupes_Recurse() {
        assertEquals([dupe1, dupe3, dupe2], fh.filesFromPattern(new File("/tmp/tmp2"), "**/m.*.file"))
    }
    
    @Test
    public void filesFromPattern_ComplexRecursion() {
        assertEquals([dupe1], fh.filesFromPattern(new File("/tmp/tmp2"), "**/b/**/m.*.file"))
        assertEquals([dupe1], fh.filesFromPattern(new File("/tmp/tmp2"), "**/a/**/.*.file"))
    }
    
    @Test
    public void filesFromPattern_UnNecessaryRecursion() {
        assertEquals([dupe1], fh.filesFromPattern(new File("/tmp/tmp2"), "a/**/b/**/c/**/matching.file"))
    }
    
    @Test
    public void filesFromPattern_LeadingSlash() {
        assertEquals([dupe1, dupe3, dupe2], fh.filesFromPattern(new File("/tmp/tmp2"), "/**/matching.file"))
        assertEquals([dupe1], fh.filesFromPattern(new File("/tmp/tmp2"), "/a/**/matching.file"))
    }
    
    @Test
    public void filesFromPattern_Wildcard() {
        assertEquals([dupe1], fh.filesFromPattern(new File("/tmp/tmp2"), "a/b/c/.*"))
        assertEquals([dupe1], fh.filesFromPattern(new File("/tmp/tmp2"), "a/b/c/.*\\..*"))
    }
    
    @Test
    def void splitFilePattern() {
        assertEquals(["**", "tmp"], fh.splitFilePattern("**/tmp"))
        assertEquals(["**", "a/tmp"], fh.splitFilePattern("**/a/tmp"))
        assertEquals(["a", "tmp"], fh.splitFilePattern("a/tmp"))
        assertEquals(["tmp", null], fh.splitFilePattern("tmp"))
    }

}
