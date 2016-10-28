package me.gking2224.buildtools.plugin

import me.gking2224.buildtools.util.GroovyUtil
import me.gking2224.common.utils.NestedProperties
import me.gking2224.common.utils.PrefixedProperties

import org.gradle.api.Project
import org.slf4j.LoggerFactory

class EnvironmentsHandler {
    
    def logger = LoggerFactory.getLogger(EnvironmentsHandler.class)

    static final KEY = "envconfig"
    
    def Project project
    
    def props = []
    
    def EnvironmentConfig all
    
    public EnvironmentsHandler(Project project) {
        this.project = project
        all = new NonNullEnvironmentConfig("all", project, "<<unconfigured>>")
        project.ext.envProps = {}
        
    }
    
    def files(List<String> files) {
        File propsDir = getPropsDir()
        for (String f in files) {
            Properties fallback = getProperties(propsDir, f, null, null)
            Properties p = getProperties(propsDir, f, project.name, fallback)
            if (p == null) p = fallback
            if (p != null) props << new PrefixedProperties(f, p)
        }
        def ec = new PropertiesEnvironmentConfig(project.env, project, props)
        project.ext.envProps = ec
    }
    
    def env(String env, Closure c) {
        
        assert project.hasProperty("env")
        def sysEnv = project.env
        
        if (env == "all") {
            GroovyUtil.instance().configureObjectFromClosure(all, c)
        }
        if (sysEnv == env) {
            def ec = new EnvironmentConfig(env, project, all)
            ec = GroovyUtil.instance().configureObjectFromClosure(ec, c)
            project.ext.envProps = ec
        }
    }
    
    def getPropsDir() {
        def dir = System.getenv("PROPS_DIR")
        if (dir == null) {
            dir = System.getProperty("user.home")+File.separator+"properties"
        }
        File rv = new File(dir)
        assert rv.exists() : dir + " does not exist"
        assert rv.isDirectory() : dir + " is not a directory"
        
        return rv
    }
    
    def getProperties(File dir, String name, String projectName, Properties fallback) {
        Properties rv = new Properties(fallback)
        Properties rawProps = new Properties()
        String propsFileName = ((projectName == null) ? name : projectName+"-"+name) + ".properties"
        
        File f = new File(dir, propsFileName)
        if (!f.exists()) {
            return null
        }
        assert f.isFile() : propsFileName + " is not a file"
        f.withInputStream { rawProps.load(it) }
        rv.putAll(rawProps)
        return new NestedProperties(project.env, rv)
    }
}
