FROM jeanblanchard/java
MAINTAINER JunHo Yoon "junoyoon@gmail.com"

# Set up environment variables
ENV BASE_DIR=/opt

# Download ngrinder controller file
RUN mkdir -p $BASE_DIR

# Expose ports
EXPOSE 80 16001 12000-12009

# Volume mappting
VOLUME ["/root/.ngrinder"]

# Copy initial excution script
ADD scripts /scripts

ADD binary/*.war ${BASE_DIR}/

# Excution
CMD ["/scripts/run.sh"]

