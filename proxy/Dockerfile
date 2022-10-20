FROM amd64/openjdk:18-alpine

COPY /srv /proxy
WORKDIR /proxy

CMD rm -rf /proxy/logs
CMD echo "Starting the Proxy" && java -jar /proxy/velocity.jar