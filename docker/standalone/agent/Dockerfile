FROM jeanblanchard/java
MAINTAINER JunHo Yoon "junoyoon@gmail.com"

# Set up environment variables
ENV BASE_DIR=/opt \
    AGENT_HOME=$BASE_DIR/ngrinder-agent \
    VERSION=3.3

ADD binray/* $BASE_DIR/builtin


# Copy initial excution script
ADD scripts /scripts

# Excution
ENTRYPOINT ["/scripts/run.sh"]
