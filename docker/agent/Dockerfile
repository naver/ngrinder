FROM jeanblanchard/java:serverjre-8
MAINTAINER JunHo Yoon "junoyoon@gmail.com"

RUN apk update; apk add curl bash


# Set up environment variables
ENV BASE_DIR=/opt \
    NGRINDER_AGENT_BASE=/opt/ngrinder-agent \
    NGRINDER_AGENT_HOME=/opt/ngrinder-agent/.ngrinder-agent

VOLUME $NGRINDER_AGENT_BASE

# Copy initial execution script
ADD scripts /scripts

# Excution
ENTRYPOINT ["/scripts/run.sh"]
