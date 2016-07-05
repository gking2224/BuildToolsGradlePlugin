package me.gking2224.buildtools.plugin

import me.gking2224.buildtools.util.PropertiesResolver

import org.gradle.api.Project

class SecretPropertiesConfigurer extends AbstractProjectConfigurer {
    
    static final String SECRET_PROPERTIES_FILE = "secret.properties"
    static final String SECRET_BUILD_FILE = "secret.gradle"

    public SecretPropertiesConfigurer(Project p) {
        super(p);
    }

    @Override
    public Object configureProject() {
        
        if (project.file(SECRET_PROPERTIES_FILE).exists()) {
            logger.debug("Reading properties from $SECRET_PROPERTIES_FILE")
            new PropertiesResolver(project).resolveProperties(project.readProps(project.file(SECRET_PROPERTIES_FILE)))
        }
        
        if (project.file(SECRET_BUILD_FILE).exists()) {
            logger.debug("Applying $SECRET_BUILD_FILE")
            project.apply(from:SECRET_BUILD_FILE)
        }
    }

}
