FROM jeanblanchard/java:serverjre-8
MAINTAINER JunHo Yoon "junoyoon@gmail.com"

RUN apk update; apk add curl bash tar

ARG MAVEN_VERSION=3.6.3
ARG MAVEN_DOWNLOAD_BASE_URL=https://apache.osuosl.org/maven/maven-3/${MAVEN_VERSION}/binaries

ARG GRADLE_VERSION=6.7.1
ARG GRADLE_DOWNLOAD_BASE_URL=https://services.gradle.org/distributions

# Install maven
RUN mkdir -p /usr/share/maven \
  && echo "Downloading maven" \
  && curl -fsSL -o /tmp/apache-maven.tar.gz ${MAVEN_DOWNLOAD_BASE_URL}/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
  && echo "Unziping maven" \
  && tar -xzf /tmp/apache-maven.tar.gz -C /usr/share/maven --strip-components=1 \
  && rm -f /tmp/apache-maven.tar.gz

# Install gradle
RUN mkdir -p /usr/share/gradle \
  && echo "Downloading gradle" \
  && curl -fsSL -o /tmp/gradle.zip ${GRADLE_DOWNLOAD_BASE_URL}/gradle-${GRADLE_VERSION}-bin.zip \
  && echo "Unziping gradle" \
  && unzip -d /usr/share/gradle /tmp/gradle.zip \
  && rm -f /tmp/gradle.zip

# Set up environment variables
ENV JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8
ENV LANG=en_US.UTF-8
ENV BASE_DIR=/opt \
    NGRINDER_HOME=/opt/ngrinder-controller \
    MAVEN_HOME=/usr/share/maven \
    GRADLE_HOME=/usr/share/gradle/gradle-${GRADLE_VERSION}

ENV PATH=$PATH:$GRADLE_HOME/bin:$MAVEN_HOME/bin

# Expose ports
EXPOSE 80 16001 12000-12009

# Volume mapping
VOLUME $NGRINDER_HOME

# Copy initial execution script
COPY scripts /scripts

# Copy final binary
COPY binary/*.war ${BASE_DIR}

# Execution
CMD ["/scripts/run.sh"]

