package me.gking2224.buildtools.util

public class Version {

    enum IncType {
        MAJOR, MINOR, PATCH
    }
    static final String SNAPSHOT_SFX = "-SNAPSHOT"
    static final String ALREADY_RELEASED_ERR_MSG = "not possible to release a released version"
    static final String UNKNOWN_INC_TYPE_ERR_MSG = "unknown IncType"
    def boolean isSnapshot
    def String versionNum
    def String rawVersion
    def Integer[] parts
    
    Version(Integer... parts) {
        this(false, parts)
    }
    Version(boolean isSnapshot, Integer... parts) {
        this(partsAsString(isSnapshot, parts))
    }
    Version(String v) {
        this.rawVersion = v
        isSnapshot = v.endsWith(SNAPSHOT_SFX)
        versionNum = (isSnapshot)?v.replace(SNAPSHOT_SFX, ""):v
        parts = versionNum.split("\\.").collect {Integer.parseInt it}
    }
    
    def Version increment() {
        return increment(IncType.PATCH)
    }
    
    def Version increment(IncType type) {
        switch (type) {
            case IncType.MAJOR:
                return new Version(true, parts[0]+1, 0, 0)
            case IncType.MINOR:
                return new Version(true, parts[0], parts[1]+1, 0)
            case IncType.PATCH:
                return new Version(true, parts[0], parts[1], parts[2]+1)
            default:
                throw new IllegalArgumentException(UNKNOWN_INC_TYPE_ERR_MSG)
        }
    }
    
    def Version release() {
        if (!isSnapshot) throw new IllegalStateException(ALREADY_RELEASED_ERR_MSG)
        return new Version(false, parts)
    }
    static String partsAsString(boolean isSnapshot, Integer... parts) {
        return parts.join(".") + ((isSnapshot)?"-SNAPSHOT":"")
    }
    
    public String toString() {
        rawVersion
    }
}
