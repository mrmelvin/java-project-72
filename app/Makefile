setup:
	gradle wrapper --gradle-version 7.4

clean:
	./gradlew clean

build:
	./gradlew clean build

start:
	APP_ENV=development ./gradlew run

install:
	./gradlew clean install

start-dist:
	APP_ENV=production ./build/install/java-javalin-blog/bin/java-javalin-blog

generate-migrations:
	./gradlew generateMigrations

run:
	./gradlew run

test:
	./gradlew test

report:
	./gradlew jacocoTestReport

lint:
	./gradlew checkstyleMain checkstyleTest

.PHONY: build