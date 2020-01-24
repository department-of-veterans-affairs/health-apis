#!/usr/bin/env bash
java -jar ~/.m2/repository/com/h2database/h2/1.4.200/h2-1.4.200.jar -url jdbc:h2:./src/test/resources/mitre -user sa -password sa
