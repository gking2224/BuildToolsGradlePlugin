package me.gking2224.buildtools.util

import static org.junit.Assert.*

import org.junit.Test

class FilePathTest {

    @Test(expected=AssertionError)
    def void testCreateNull() {
        new FilePath(null)
    }
    
    @Test(expected=AssertionError)
    def void testCreateEmpty() {
        new FilePath([])
    }
    
    @Test(expected=AssertionError)
    def void testCreateWrongType() {
        new FilePath(1L)
    }
    
    @Test(expected=AssertionError)
    def void testCreateNullInList() {
        new FilePath("a", null)
    }
    
    @Test
    def void testCreateNoDuplicateSeparators() {
        def path = "/Users/gk/test"
        assertEquals path, new FilePath("/Users/gk/", "test").filePath
        assertEquals path, new FilePath("/Users/gk", "test").filePath
    }
    
    @Test
    def void testCreateWithFile() {
        def path = "/Users/gk/test"
        def dir = new File("/Users/gk")
        assertEquals path, new FilePath(dir, "test").filePath
    }
    
    @Test
    def void testCreateWithMultiple() {
        def path = "/Users/gk/test/dummy"
        def dir = new File("/Users/gk")
        assertEquals path, new FilePath(dir, "test", "dummy").filePath
        assertEquals path, new FilePath(dir, "test/", "dummy").filePath
    }

}
