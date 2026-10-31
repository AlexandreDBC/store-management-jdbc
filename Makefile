# Le répertoire où les fichiers .class seront placés
BINDIR = out

# Le chemin vers la librairie graphique gui.jar
OJDBC_JAR = libs/ojdbc17.jar

# Le classpath pour la compilation et l'exécution
CLASSPATH = $(BINDIR):$(OJDBC_JAR)

SRC_MAIN=$(shell find src -name '*.java')

all: compile

# Compiler tous les fichiers .java du projet
compile:
	@mkdir -p $(BINDIR)
	javac -d $(BINDIR) -classpath $(OJDBC_JAR) -sourcepath src $(SRC_MAIN)

start:
	java -classpath $(CLASSPATH) app.RunApplication

# Nettoyer le projet (supprimer les fichiers compilés)
clean:
	rm -rf $(BINDIR)

.PHONY: all compile start clean