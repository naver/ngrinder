FROM centos:centos6
MAINTAINER Veronica Hong "rankyung.hong@gmail.com"

# Install necessary packages
RUN yum -y install curl vim java-1.7.0-openjdk-devel.x86_64

# Set up environment variables
ENV BASE_DIR=/opt \
    JAVA_HOME=/etc/alternatives/java_sdk \
    PATH=$PATH:$JAVA_HOME/bin

# Download ngrinder controller file
RUN mkdir $BASE_DIR/ngrinder-controller \
&& curl -L -o /$BASE_DIR/ngrinder-controller/ngrinder-controller-3.3.war \
"http://downloads.sourceforge.net/project/ngrinder/ngrinder-3.3/ngrinder-controller-3.3.war?r=http%3A%2F%2Fsourceforge.net%2Fprojects%2Fngrinder%2Ffiles%2Fngrinder-3.3%2F&ts=1422129105&use_mirror=cznic"

# Expose ports
EXPOSE 80 16001 12000-12009

# Volume mappting
VOLUME ["/root/.ngrinder"]

# Copy initial excution script
ADD scripts /scripts

# Excution
CMD ["/scripts/run.sh"]

