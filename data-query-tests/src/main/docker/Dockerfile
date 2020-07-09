FROM vasdvp/health-apis-dev-tools:mvn-3.6-jdk-14

COPY maven/ /sentinel
RUN chmod 755 /sentinel/*sh
ENTRYPOINT ["/sentinel/entrypoint.sh"]
CMD []
