FROM jeanblanchard/java
MAINTAINER JunHo Yoon "junoyoon@gmail.com"

RUN apk update; apk add curl

# Set up environment variables
ENV BASE_DIR=/opt

# Download ngrinder controller file
RUN mkdir -p $BASE_DIR/ngrinder-controller

# Expose ports
EXPOSE 80 16001 12000-12009

# Volume mappting
VOLUME ["/root/.ngrinder"]

ADD http://www.h2database.com/h2-2014-04-05.zip ${BASE_DIR}
RUN cd ${BASE_DIR} && unzip h2*

# Copy initial excution script
ADD scripts /scripts

# Copy final binary
ADD binary/*.war ${BASE_DIR}/ngrinder-controller/

# Excution
CMD ["/scripts/run.sh"]

