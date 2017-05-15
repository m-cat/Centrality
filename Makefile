run: build
	java -cp bin Main

build:
	javac -d bin src/*.java

clean:
	rm *~ *.dot *.png
