FROM vasdvp/health-apis-dev-tools:mvn-3.8-jdk-16

COPY maven/ /sentinel
RUN chmod 755 /sentinel/*sh
ENTRYPOINT ["/sentinel/entrypoint.sh"]
CMD []
