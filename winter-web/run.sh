#$JAVA_HOME/bin/java -Dio.netty.leakDetectionLevel=paranoid -Dio.netty.leakDetection.targetRecords=20 -Dfile.encoding=UTF-8 -p /home/jkuhn/Devel/git/frmk/winter/winter-modules/winter-web/target/classes:/home/jkuhn/Devel/git/frmk/winter/winter-root/winter-core/target/classes:/home/jkuhn/Devel/git/frmk/winter/winter-root/winter-core-annotation/target/classes:/home/jkuhn/.m2/repository/org/apache/logging/log4j/log4j-api/2.13.3/log4j-api-2.13.3.jar:/home/jkuhn/Devel/git/frmk/winter/winter-modules/winter-configuration/target/classes:/home/jkuhn/Devel/git/frmk/winter/winter-root/winter-core-compiler/target/classes:/home/jkuhn/.m2/repository/org/apache/commons/commons-text/1.9/commons-text-1.9.jar:/home/jkuhn/.m2/repository/io/netty/netty-transport-native-epoll/4.1.53.Final/netty-transport-native-epoll-4.1.53.Final-linux-x86_64.jar:/home/jkuhn/.m2/repository/io/netty/netty-common/4.1.53.Final/netty-common-4.1.53.Final.jar:/home/jkuhn/.m2/repository/io/netty/netty-buffer/4.1.53.Final/netty-buffer-4.1.53.Final.jar:/home/jkuhn/.m2/repository/io/netty/netty-transport/4.1.53.Final/netty-transport-4.1.53.Final.jar:/home/jkuhn/.m2/repository/io/netty/netty-codec-http2/4.1.53.Final/netty-codec-http2-4.1.53.Final.jar:/home/jkuhn/.m2/repository/io/netty/netty-codec/4.1.53.Final/netty-codec-4.1.53.Final.jar:/home/jkuhn/.m2/repository/io/netty/netty-handler/4.1.53.Final/netty-handler-4.1.53.Final.jar:/home/jkuhn/.m2/repository/io/netty/netty-codec-http/4.1.53.Final/netty-codec-http-4.1.53.Final.jar:/home/jkuhn/.m2/repository/io/projectreactor/reactor-core/3.4.0/reactor-core-3.4.0.jar:/home/jkuhn/.m2/repository/org/reactivestreams/reactive-streams/1.0.3/reactive-streams-1.0.3.jar:/home/jkuhn/.m2/repository/com/fasterxml/jackson/core/jackson-databind/2.11.3/jackson-databind-2.11.3.jar:/home/jkuhn/.m2/repository/com/fasterxml/jackson/core/jackson-annotations/2.11.3/jackson-annotations-2.11.3.jar:/home/jkuhn/.m2/repository/com/fasterxml/jackson/core/jackson-core/2.11.3/jackson-core-2.11.3.jar -classpath /home/jkuhn/.m2/repository/org/apache/commons/commons-lang3/3.11/commons-lang3-3.11.jar:/home/jkuhn/.m2/repository/io/netty/netty-resolver/4.1.53.Final/netty-resolver-4.1.53.Final.jar:/home/jkuhn/.m2/repository/io/netty/netty-transport-native-unix-common/4.1.53.Final/netty-transport-native-unix-common-4.1.53.Final.jar:/home/jkuhn/.m2/repository/org/apache/logging/log4j/log4j-core/2.13.3/log4j-core-2.13.3.jar:/home/jkuhn/.m2/repository/org/junit/platform/junit-platform-launcher/1.5.1/junit-platform-launcher-1.5.1.jar:/home/jkuhn/.m2/repository/org/apiguardian/apiguardian-api/1.1.0/apiguardian-api-1.1.0.jar:/home/jkuhn/.m2/repository/org/junit/platform/junit-platform-engine/1.5.1/junit-platform-engine-1.5.1.jar:/home/jkuhn/.m2/repository/org/junit/platform/junit-platform-commons/1.5.1/junit-platform-commons-1.5.1.jar:/home/jkuhn/.m2/repository/org/junit/jupiter/junit-jupiter-api/5.5.1/junit-jupiter-api-5.5.1.jar:/home/jkuhn/.m2/repository/org/opentest4j/opentest4j/1.2.0/opentest4j-1.2.0.jar:/home/jkuhn/.m2/repository/org/junit/jupiter/junit-jupiter-params/5.5.1/junit-jupiter-params-5.5.1.jar:/home/jkuhn/.m2/repository/org/junit/jupiter/junit-jupiter-engine/5.5.1/junit-jupiter-engine-5.5.1.jar --patch-module io.winterframework.mod.web=/home/jkuhn/Devel/git/frmk/winter/winter-modules/winter-web/target/test-classes --add-reads io.winterframework.mod.web=ALL-UNNAMED --add-reads io.winterframework.core=ALL-UNNAMED --add-reads io.winterframework.core.annotation=ALL-UNNAMED --add-reads io.winterframework.mod.configuration=ALL-UNNAMED --add-reads io.winterframework.core.compiler=ALL-UNNAMED -m io.winterframework.mod.web/io.winterframework.mod.web.app.App
$JAVA_HOME/bin/java -Dio.netty.leakDetectionLevel=disabled -Dio.netty.leakDetection.targetRecords=20 -Dfile.encoding=UTF-8 -p /home/jkuhn/Devel/git/frmk/winter/winter-modules/winter-web/target/classes:/home/jkuhn/Devel/git/frmk/winter/winter-root/winter-core/target/classes:/home/jkuhn/Devel/git/frmk/winter/winter-root/winter-core-annotation/target/classes:/home/jkuhn/.m2/repository/org/apache/logging/log4j/log4j-api/2.13.3/log4j-api-2.13.3.jar:/home/jkuhn/Devel/git/frmk/winter/winter-modules/winter-configuration/target/classes:/home/jkuhn/Devel/git/frmk/winter/winter-root/winter-core-compiler/target/classes:/home/jkuhn/.m2/repository/org/apache/commons/commons-text/1.9/commons-text-1.9.jar:/home/jkuhn/.m2/repository/io/netty/netty-transport-native-epoll/4.1.53.Final/netty-transport-native-epoll-4.1.53.Final-linux-x86_64.jar:/home/jkuhn/.m2/repository/io/netty/netty-common/4.1.53.Final/netty-common-4.1.53.Final.jar:/home/jkuhn/.m2/repository/io/netty/netty-buffer/4.1.53.Final/netty-buffer-4.1.53.Final.jar:/home/jkuhn/.m2/repository/io/netty/netty-transport/4.1.53.Final/netty-transport-4.1.53.Final.jar:/home/jkuhn/.m2/repository/io/netty/netty-codec-http2/4.1.53.Final/netty-codec-http2-4.1.53.Final.jar:/home/jkuhn/.m2/repository/io/netty/netty-codec/4.1.53.Final/netty-codec-4.1.53.Final.jar:/home/jkuhn/.m2/repository/io/netty/netty-handler/4.1.53.Final/netty-handler-4.1.53.Final.jar:/home/jkuhn/.m2/repository/io/netty/netty-codec-http/4.1.53.Final/netty-codec-http-4.1.53.Final.jar:/home/jkuhn/.m2/repository/io/projectreactor/reactor-core/3.4.0/reactor-core-3.4.0.jar:/home/jkuhn/.m2/repository/org/reactivestreams/reactive-streams/1.0.3/reactive-streams-1.0.3.jar:/home/jkuhn/.m2/repository/com/fasterxml/jackson/core/jackson-databind/2.11.3/jackson-databind-2.11.3.jar:/home/jkuhn/.m2/repository/com/fasterxml/jackson/core/jackson-annotations/2.11.3/jackson-annotations-2.11.3.jar:/home/jkuhn/.m2/repository/com/fasterxml/jackson/core/jackson-core/2.11.3/jackson-core-2.11.3.jar -classpath /home/jkuhn/.m2/repository/org/apache/commons/commons-lang3/3.11/commons-lang3-3.11.jar:/home/jkuhn/.m2/repository/io/netty/netty-resolver/4.1.53.Final/netty-resolver-4.1.53.Final.jar:/home/jkuhn/.m2/repository/io/netty/netty-transport-native-unix-common/4.1.53.Final/netty-transport-native-unix-common-4.1.53.Final.jar:/home/jkuhn/.m2/repository/org/apache/logging/log4j/log4j-core/2.13.3/log4j-core-2.13.3.jar:/home/jkuhn/.m2/repository/org/junit/platform/junit-platform-launcher/1.5.1/junit-platform-launcher-1.5.1.jar:/home/jkuhn/.m2/repository/org/apiguardian/apiguardian-api/1.1.0/apiguardian-api-1.1.0.jar:/home/jkuhn/.m2/repository/org/junit/platform/junit-platform-engine/1.5.1/junit-platform-engine-1.5.1.jar:/home/jkuhn/.m2/repository/org/junit/platform/junit-platform-commons/1.5.1/junit-platform-commons-1.5.1.jar:/home/jkuhn/.m2/repository/org/junit/jupiter/junit-jupiter-api/5.5.1/junit-jupiter-api-5.5.1.jar:/home/jkuhn/.m2/repository/org/opentest4j/opentest4j/1.2.0/opentest4j-1.2.0.jar:/home/jkuhn/.m2/repository/org/junit/jupiter/junit-jupiter-params/5.5.1/junit-jupiter-params-5.5.1.jar:/home/jkuhn/.m2/repository/org/junit/jupiter/junit-jupiter-engine/5.5.1/junit-jupiter-engine-5.5.1.jar --patch-module io.winterframework.mod.web=/home/jkuhn/Devel/git/frmk/winter/winter-modules/winter-web/target/test-classes --add-reads io.winterframework.mod.web=ALL-UNNAMED --add-reads io.winterframework.core=ALL-UNNAMED --add-reads io.winterframework.core.annotation=ALL-UNNAMED --add-reads io.winterframework.mod.configuration=ALL-UNNAMED --add-reads io.winterframework.core.compiler=ALL-UNNAMED -m io.winterframework.mod.web/io.winterframework.mod.web.app.App