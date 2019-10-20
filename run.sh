export CLASSPATH=$CLASSPATH:/roy/match/match-base/target/match-base-0.0.1-SNAPSHOT-jar-with-dependencies.jar:/roy/match/match-batch/target/match-batch-0.0.1-SNAPSHOT-jar-with-dependencies.jar

java -server -Dspring.profiles.active=prod com.roy.football.batch.lancher.MatchCalculationJobLancher

java -server -Dspring.profiles.active=prod com.roy.football.batch.lancher.HistoryMatchCalculationJobLancher