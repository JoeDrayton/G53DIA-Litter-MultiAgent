# Makefile
# RCS: @(#) $Id: Makefile,v 1.4 2010/02/04 12:28:02 bsl Exp $

JAVAC=javac
JAR=jar
#FLAGS=
FLAGS=-g -classpath bin -sourcepath src
TAGS=ctags
BLDDIR=bin
DOCDIR=doc
SRCDIR=src
JARNAME=G53DIA.jar
# Packages to compile
PACKAGES = uk.ac.nott.cs.g53dia
# Subdirectories with java files
JAVA_DIRS	:= $(foreach pack, $(PACKAGES), src/$(subst .,/,$(pack)))
# All the .java source files
JAVA_SRC	:= $(foreach dir, $(JAVA_DIRS), $(wildcard $(dir)/*.java))
# The intermediate java files and main classes we should build
JAVA_OBJS	:= $(JAVA_SRC:.java=.class)

%.class: %.java
	@echo "Compiling" $< "..."
	@$(JAVAC) -d $(BLDDIR) $(FLAGS) $<
	@echo "done."

all: demo
	@echo "Creating jar file..."
	@$(JAR) cmvf $(SRCDIR)/manifest-addition $(JARNAME) -C $(BLDDIR) \
		uk/ac/nott/cs/g53dia/multiagent -C $(BLDDIR) uk/ac/nott/cs/g53dia/multilibrary $(BLDDIR)/uk/ac/nott/cs/g53dia/multisimulator/*.class\
		>/dev/null
	@echo "done."

classes: $(SRCDIR)/uk/ac/nott/cs/g53dia/multiagent/*.java
	@echo "Creating build directory $(BLDDIR)..."
	@mkdir -p $(BLDDIR)
	@echo "Compiling source files..."
	@$(JAVAC) -d $(BLDDIR) $(FLAGS) $(SRCDIR)/uk/ac/nott/cs/g53dia/multiagent/*.java
	@echo "Copying image files..."
	@cp -r $(SRCDIR)/uk/ac/nott/cs/g53dia/multilibrary/images $(BLDDIR)/uk/ac/nott/cs/g53dia/multilibrary
	@echo "done."

docs: $(SRCDIR)/uk/ac/nott/cs/g53dia/multiagent/*.java
	@echo "Creating javadoc..."
	@javadoc $(SRCDIR)/uk/ac/nott/cs/g53dia/multiagent/*.java  $(SRCDIR)/uk/ac/nott/cs/g53dia/multilibrary/*.java   $(SRCDIR)/uk/ac/nott/cs/g53dia/multisimulator/*.java -d $(DOCDIR) -private >/dev/null
	@echo "done."

demo: classes
	@echo "Compiling demo..."
	@$(JAVAC) -d $(BLDDIR) $(FLAGS) $(SRCDIR)/uk/ac/nott/cs/g53dia/multisimulator/*.java
	@echo "done."

clean::
	rm -f $(BLDDIR)/uk/ac/nott/cs/g53dia/multiagent/*.class
	rm -f $(BLDDIR)/uk/ac/nott/cs/g53dia/multilibrary/*.class
	rm -f $(BLDDIR)/uk/ac/nott/cs/g53dia/multilibrary/images/*
	rm -f $(BLDDIR)/uk/ac/nott/cs/g53dia/multisimulator/*.class
	rm -f $(JARNAME)

distclean:: clean
	rm -rf $(DOCDIR)/*
	rm -f tags

test:: all
	java -jar $(JARNAME) 
