package me.gking2224.buildtools.util

import java.security.SecureRandom

class RandomString {

    def s
    
    public RandomString() {
        this(20L)
    }
    
    /**
     * 
     * @param length Any positive number accepted, but only multiples of 4 can be created
     */
    public RandomString(Long length) {
        if (length == null) length = 20
        length = convertLength(length)
        
        def randomBytes = _getRandom(length)
//        def timeBytes = _getTimeBytes()
//        def allBytes = _concat(randomBytes, timeBytes)
        s = Base64.getUrlEncoder().encodeToString(randomBytes)
    }
    
    def _getRandom(length) {
        SecureRandom sr = new SecureRandom()
        sr.setSeed(System.currentTimeMillis())
        def randomBytes = new byte[length]
        sr.nextBytes(randomBytes)
        randomBytes
    }
    
    def _getTimeBytes() {
        System.currentTimeMillis().toString().bytes
    }
    
    def _concat(def b1, def b2) {
        def b3 = new byte[b1.length + b2.length]
        System.arraycopy(b1, 0, b3, 0, b1.length);
        System.arraycopy(b2, 0, b3, b1.length, b2.length)
        b3
    }
    
    def String toString() {
        s
    }
    
    def static convertLength(long l) {
        (((int)(l / 4) - 1) * 3) + 3
    }

}
