FROM centos:centos6
MAINTAINER Veronica Hong "rankyung.hong@gmail.com"

# Install necessary packages
RUN yum -y install wget tar vim java-1.7.0-openjdk-devel.x86_64

# Set up environment variables
ENV BASE_DIR=/opt \
    AGENT_HOME=$BASE_DIR/ngrinder-agent \
    JAVA_HOME=/etc/alternatives/java_sdk \
    PATH=$PATH:$JAVA_HOME/bin:$AGENT_HOME

# Copy initial excution script
ADD scripts /scripts

# Excution
CMD ["/scripts/run.sh"]
