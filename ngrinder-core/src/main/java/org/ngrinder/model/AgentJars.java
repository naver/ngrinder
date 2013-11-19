package org.ngrinder.model;

/**
 * Agent referenced jar files
 * 
 * @author Matt
 * @since 3.3
 */
public enum AgentJars {

    ANTLR("antlr"),
    ASM("asm"),
    COMMONS("common"),
    DNSJAVA("dnsjava"),
    DOM4J("dom4j"),
    GRINDER("grinder"),
    GROOVY_ALL("groovy-all"),
    GSON("gson"),
    HAMCREST_ALL("hamcrest-all"),
    HIBERNATE("hibernate"),
    JAVASSIST("javassist"),
    JBOSS_TRANSACTION("jboss-transaction"),
    JCL_OVER_SLF("jcl-over-slf4j"),
    JNA("jna"),
    JSON("json"),
    JUNIT_DEP("junit-dep"),
    JYTHON("jython-standalone"),
    LOG("log"),
    NATIVE("native"),
    NGRINDER_DNS("ngrinder-dns"),
    NGRINDER_GROOVY("ngrinder-groovy"),
    PICOCONTAINER("picocontainer"),
    SIGAR("sigar"),
    SLF4J("slf4j-api"),
    XML_APIS("xml-apis"),
    XZ("xz");


    private final String shortName;

    /**
     * Constructor.
     *
     * @param shortName
     *            short name of jar
     */
    AgentJars(String shortName) {
        this.shortName = shortName;
    }

    /**
     * Get the short name.
     *
     * @return short name
     */
    public String getShortName() {
        return shortName;
    }
}