package me.gking2224.buildtools.util

class FilePath {

    Object[] _parts
    def String filePath
    
    public FilePath(Object... parts) {
        _parts = parts
        filePath = createPath()
    }

    def createPath() {
        pathFromParts(_parts)
    }
    
    static def pathFromParts(Object... parts) {
        _validateParts(parts)
        StringBuilder builder = new StringBuilder()
        parts.eachWithIndex{p,i->
            boolean lastIteration = (i == (parts.length - 1))
            if (File.isAssignableFrom(p.class)) {
                builder.append(((File)p).absolutePath)
            }
            else if ([GString,String].any{it.isAssignableFrom(p.class)}){
                builder.append((String)p)
            }
            else {
                throw new IllegalStateException("Invalid type: ${p.class} <should not get here!>")
            }
            def lastChar = builder.substring(builder.length()-1)
            if (builder.substring(builder.length()-1) != File.separator && !lastIteration) {
                builder.append(File.separator)
            }
        }
        builder.toString()
    }
    
    static def _validateParts(Object... parts) {
        assert parts != null
        assert parts.length != 0
        parts.each {p->
            assert p != null
            assert [File,String,GString].any {c->c.isAssignableFrom(p.class)} : "$p (${p.class}) is invalid"
        }
    }
}
