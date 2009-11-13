#!/bin/bash
BREAK_CHARS="(){}[],^%$#@\"\";:''|\\"

#set up classpath (assumes webui-framework dir, need to fix this)
FW=../../webui-framework
FILES="$PWD/lib/*.jar"
for file in $FILES
do
	CLASSPATH=$CLASSPATH:$file
done
FILES="$PWD/$FW/lib/*.jar"
for file in $FILES
do
	CLASSPATH=$CLASSPATH:$file
done
CLASSPATH=$CLASSPATH:./bin:./classes:$FW/bin:./src/
#echo $CLASSPATH
export CLASSPATH

if [ $# -eq 0 ]; then 
    rlwrap --remember -c -b $BREAK_CHARS -f $HOME/.clj_completions \
    java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n clojure.lang.Repl
else
    java clojure.lang.Script $1 -- $@
fi
#java jline.ConsoleRunner clojure.lang.Repl $1
