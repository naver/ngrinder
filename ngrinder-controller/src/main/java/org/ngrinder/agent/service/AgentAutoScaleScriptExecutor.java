package org.ngrinder.agent.service;

import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import com.google.common.io.Files;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static org.ngrinder.common.util.ExceptionUtils.processException;

/**
 * Created by junoyoon on 15. 7. 28.
 */
public class AgentAutoScaleScriptExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(AgentAutoScaleScriptExecutor.class);

    private final String controllerIP;
    private final String controllerPort;
    private final String dockerRepo;
    private final String dockerTag;

    public AgentAutoScaleScriptExecutor(String controllerIP, String controllerPort, String dockerRepo, String dockerTag) {
        this.controllerIP = controllerIP;
        this.controllerPort = controllerPort;
        this.dockerRepo = dockerRepo;
        this.dockerTag = dockerTag;
    }

    public String getControllerIP() {
        return controllerIP;
    }

    public String getControllerPort() {
        return controllerPort;
    }

    public String getDockerRepo() {
        return dockerRepo;
    }

    public String getDockerTag() {
        return dockerTag;
    }

    /**
     * Get the initial script with the given value map for operation EC2 node.
     *
     * @param values map of initial script referencing values.
     * @param cmd    the operation name which maybe add, on and off
     * @return String the script content.
     */
    private String getShellScriptViaTemplate(Map<String, Object> values, String cmd) {
        try {
            String newFileName;
            if (cmd.equalsIgnoreCase("add")) {
                newFileName = "add.sh";
            } else if (cmd.equalsIgnoreCase("on")) {
                newFileName = "on.sh";
            } else if (cmd.equalsIgnoreCase("off")) {
                newFileName = "off.sh";
            } else {
                throw processException("Error while fetching the script template since bad command.");
            }

            Configuration freemarkerConfig = new Configuration();
            ClassPathResource cpr = new ClassPathResource("agent_autoscale_script");
            freemarkerConfig.setDirectoryForTemplateLoading(cpr.getFile());
            freemarkerConfig.setObjectWrapper(new DefaultObjectWrapper());
            freemarker.template.Template template = freemarkerConfig.getTemplate("provision.sh");
            StringWriter writer = new StringWriter();

            template.process(values, writer);

            String scriptName = cpr.getFile() + "/" + newFileName;
            FileWriter fw = new FileWriter(new File(scriptName));
            BufferedWriter bw = new BufferedWriter(fw);
            String scriptContent = writer.toString();
            bw.write(scriptContent);
            bw.close();
            fw.close();
            return scriptContent;
        } catch (Exception e) {
            throw processException("Error while fetching the script template.", e);
        }
    }

    /**
     * Script file generator based on the script template
     *
     * @param ctrl_IP,           ngrinder controller IP
     * @param ctrl_port,         ngrinder controller PORT
     * @param agent_docker_repo, the docker image repository name
     * @param agent_docker_tag,  the docker image tag
     * @param cmd,               the operation command, such as ADD, ON, OFF
     * @return String script content
     */
    private String generateScriptBasedOnTemplate(String ctrl_IP, String ctrl_port, String agent_docker_repo,
                                                 String agent_docker_tag, String cmd) {

        Map<String, Object> values = newHashMap();
        values.put("agent_controller_ip", ctrl_IP);
        values.put("agent_controller_port", ctrl_port);
        values.put("agent_image_repo", agent_docker_repo);
        values.put("agent_image_tag", agent_docker_tag);
        values.put("agent_work_mode", cmd.toUpperCase());
        return getShellScriptViaTemplate(values, cmd);
    }

    private String getShellScriptByAction(String action) {

        return generateScriptBasedOnTemplate(controllerIP, controllerPort, dockerRepo, dockerTag, action);

    }

    public void run(String node_ip, String node_user, String action) {
        SshShellExecutor sshShellExecutor = new SshShellExecutor(node_ip, node_user);
        try {
            int ret = sshShellExecutor.exec(getShellScriptByAction(action));
            LOG.info("Shell script execute with return result: {} for action {}", ret, action);
        } catch (Exception e) {
            throw processException(e);
        }
    }

    class SshShellExecutor {

        private Connection ssh_conn;

        private String host_ip;

        private String user;

        private String charset = Charset.defaultCharset().toString();

        private static final int SSH_TIME_OUT = 1000 * 5 * 60;

        private static final int LOGIN_RETRY_CNT = 10;

        public SshShellExecutor(String ip, String usr) {
            this.host_ip = ip;
            this.user = usr;
        }

        private boolean login(){
            ssh_conn = new Connection(host_ip);
            int retries = 0;
            while(retries < LOGIN_RETRY_CNT) {
                try {
                    ssh_conn.connect();
                    String priKey = null;
                    try {
                        priKey = Files.toString(new File("/home/agent/.ssh/id_rsa"), Charset.forName("UTF-8")).trim();
                    } catch (IOException e) {
                        throw processException(e);
                    }
                    char privateKey[] = priKey.toCharArray();
                    return ssh_conn.authenticateWithPublicKey(user, privateKey, "");
                } catch (IOException e) {
                    retries++;
                    LOG.info("Ssh login failed {} times {}", retries, e);
                    try {
                        /*
                         * If the connection failed, then wait 20s before to do the next try, because if to login via SSH
                         * after VM created as soon as possible, VM will not allow the SSH connection from remote, maybe the
                         * SSH daemon is not ready.
                         */
                        Thread.sleep(20 * 1000L);
                    } catch (InterruptedException e1) {
                        throw processException(e1);
                    }
                    if(retries >= LOGIN_RETRY_CNT){
                        throw processException(e);
                    }
                }
            }
            return false;
        }

        public int exec(String shellScriptContent) throws Exception {
            InputStream stdOut = null;
            InputStream stdErr = null;
            String outStr = "";
            String outErr = "";
            int ret = -1;
            int retries = 0;
            try {
                if (login()) {
                    Session session = ssh_conn.openSession();
                    session.execCommand(shellScriptContent);

                    stdOut = new StreamGobbler(session.getStdout());
                    stdErr = new StreamGobbler(session.getStderr());
                    outStr = processStream(stdOut, charset);
                    outErr = processStream(stdErr, charset);

                    session.waitForCondition(ChannelCondition.EXIT_STATUS, SSH_TIME_OUT);

                    LOG.info("OutStr: '{}'", outStr);
                    LOG.info("OutErr: '{}'", outErr);

                    ret = session.getExitStatus();
                } else {
                    throw processException("Login failed " + host_ip);
                }

            } finally {
                if (ssh_conn != null) {
                    ssh_conn.close();
                }
                IOUtils.closeQuietly(stdOut);
                IOUtils.closeQuietly(stdErr);
            }
            return ret;
        }


        private String processStream(InputStream in, String charset) throws Exception {
            byte[] buf = new byte[8192];
            StringBuilder sb = new StringBuilder();
            while (in.read(buf) != -1) {
                sb.append(new String(buf, charset));
            }
            return StringUtils.trim(sb.toString());
        }
    }
}
