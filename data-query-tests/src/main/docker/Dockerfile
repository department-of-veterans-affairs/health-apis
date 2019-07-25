FROM vasdvp/health-apis-maven:3.6-jdk-12

COPY maven/ /sentinel
RUN chmod 755 /sentinel/*sh
ENTRYPOINT ["/sentinel/entrypoint.sh"]
CMD []
