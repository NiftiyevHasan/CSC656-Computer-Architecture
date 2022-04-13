JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
		$(JC) $(JFLAGS) $*.java

CLASSES = \
        sys1.java \
        Predictor.java \
        DynamicPredictor.java \
        sys2.java 

default: classes

classes: $(CLASSES:.java=.class)

clean:
		$(RM) *.class
