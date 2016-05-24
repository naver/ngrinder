FROM jeanblanchard/java:serverjre-8
MAINTAINER JunHo Yoon "junoyoon@gmail.com"

RUN apk update; apk add curl bash

# Set up environment variables
ENV BASE_DIR=/opt \
    NGRINDER_HOME=/opt/ngrinder-controller

# Expose ports
EXPOSE 80 16001 12000-12009

# Volume mapping
VOLUME $NGRINDER_HOME

# Copy initial execution script
ADD scripts /scripts

# Copy final binary
ADD binary/*.war ${BASE_DIR}

# Execution
CMD ["/scripts/run.sh"]

