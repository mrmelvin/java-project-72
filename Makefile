setup:
	make -C app setup

clean:
	make -C app clean

build:
	make -C app build

install:
	make -C app install

start-dist:
	make -C app start-dist

geenrate-migrations:
	make -C app generate-migrations

run:
	make -C app run

test:
	make -C app test

report:
	make -C app report

lint:
	make -C app lint
	
.PHONY: build
