<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:p="http://www.netbeans.org/ns/project/1"
                xmlns:xalan="http://xml.apache.org/xslt"
                xmlns:j2seproject1="http://www.netbeans.org/ns/j2se-project/1"
                xmlns:j2seproject2="http://www.netbeans.org/ns/j2se-project/2"
                xmlns:j2seproject3="http://www.netbeans.org/ns/j2se-project/3"
                xmlns:jaxrpc="http://www.netbeans.org/ns/j2se-project/jax-rpc"
                xmlns:projdeps="http://www.netbeans.org/ns/ant-project-references/1"
                xmlns:projdeps2="http://www.netbeans.org/ns/ant-project-references/2"
                xmlns:libs="http://www.netbeans.org/ns/ant-project-libraries/1"
                exclude-result-prefixes="xalan p projdeps projdeps2 j2seproject2 libs">
    <!-- XXX should use namespaces for NB in-VM tasks from ant/browsetask and debuggerjpda/ant (Ant 1.6.1 and higher only) -->
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>
    <xsl:template match="/">

        <xsl:comment><![CDATA[
*** GENERATED FROM project.xml - DO NOT EDIT  ***
***         EDIT ../build.xml INSTEAD         ***

        ]]></xsl:comment>

        <xsl:variable name="name" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:name"/>
        <xsl:variable name="codename" select="translate($name, ' ', '_')"/>
        <project>
            <target name="-server-embedded-macrodef-debug">
                <macrodef>
                    <xsl:attribute name="name">server-embedded-debug</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">classname</xsl:attribute>
                        <xsl:attribute name="default">${main.class}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">classpath</xsl:attribute>
                        <xsl:attribute name="default">${debug.classpath}</xsl:attribute>
                    </attribute>
                    <element>
                        <xsl:attribute name="name">customize</xsl:attribute>
                        <xsl:attribute name="optional">true</xsl:attribute>
                    </element>
                    <sequential>
                        <java fork="true" classname="@{{classname}}">
                            <xsl:attribute name="dir">${work.dir}</xsl:attribute>
                            <xsl:if test="/p:project/p:configuration/j2seproject3:data/j2seproject3:explicit-platform">
                                <xsl:attribute name="jvm">${platform.java}</xsl:attribute>
                            </xsl:if>
                            <jvmarg line="${{endorsed.classpath.cmd.line.arg}}"/>
                            <jvmarg line="${{debug-args-line}}"/>
                            <jvmarg value="-Xrunjdwp:transport=${{server-debug-transport}},address=${{server-debug-port}},server=y,suspend=n"/>
                            <jvmarg value="-Dfile.encoding=${{runtime.encoding}}"/>
                            <redirector inputencoding="${{runtime.encoding}}" outputencoding="${{runtime.encoding}}" errorencoding="${{runtime.encoding}}"/>
                            <jvmarg line="${{run.jvmargs}}"/>
                            <jvmarg line="${{run.jvmargs.ide}}"/>
                            <classpath>
                                <path path="@{{classpath}}"/>
                            </classpath>
                            <syspropertyset>
                                <propertyref prefix="run-sys-prop."/>
                                <mapper type="glob" from="run-sys-prop.*" to="*"/>
                            </syspropertyset>
                            <customize/>
                        </java>
                    </sequential>
                </macrodef>
            </target>
            <target name="debug-embedded-server">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,compile</xsl:attribute>
                <xsl:attribute name="description">Debug Embedded Server project in IDE.</xsl:attribute>
                <property name="server-debug-transport" value="${{server.debug.transport}}" />
                <property name="server-debug-port" value="${{server.debug.port}}" />
                <server-embedded-debug />
            </target>
            
            <target name="-server-embedded-macrodef-profile">
                <macrodef>
                    <xsl:attribute name="name">resolve</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">name</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">value</xsl:attribute>
                    </attribute>
                    <sequential>
                        <property name="@{{name}}" value="${{env.@{{value}}}}"/>
                    </sequential>
                </macrodef>

                <macrodef>
                    <xsl:attribute name="name">server-embedded-profile</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">classname</xsl:attribute>
                        <xsl:attribute name="default">${main.class}</xsl:attribute>
                    </attribute>
                    <element>
                        <xsl:attribute name="name">customize</xsl:attribute>
                        <xsl:attribute name="optional">true</xsl:attribute>
                    </element>
                    <sequential>
                        <property environment="env"/>
                        <!--resolve name="profiler.current.path" value="${{profiler.info.pathvar}}"/-->
                        <java fork="true" classname="@{{classname}}" >
                            <jvmarg line="${{endorsed.classpath.cmd.line.arg}}"/>
                            <jvmarg line="${{profiler-args}}"/>                            
                            <arg line="${{application.args}}"/>
                            <classpath>
                                <path path="${{run.classpath}}"/>
                            </classpath>
                            <syspropertyset>
                                <propertyref prefix="run-sys-prop."/>
                                <mapper type="glob" from="run-sys-prop.*" to="*"/>
                            </syspropertyset>
                            <customize/>
                        </java>
                    </sequential>
                </macrodef>
            </target>
            <target name="profile-embedded-server">
                <xsl:attribute name="depends">init,compile,-profile-init-check</xsl:attribute>
                <property name="profiler-args" value="${{profiler.args}}" />
                <server-embedded-profile />
            </target>
            <target name="package-dist-jar">
                <!--xsl:attribute name="depends">jar</xsl:attribute-->
                <delete dir="${{package.dist.root}}/package-dist" quiet="true" />
                 <mkdir dir="${{package.dist.root}}/package-dist"/>
                <copy todir="${{package.dist.root}}/package-dist">
                    <fileset dir="${{dist.dir}}" />
                </copy>
                <jar update="true" zipfile="${{package.dist.root}}/package-dist/${{package.dist.jar}}" basedir="${{package.wars.temp}}" />
                <delete dir="${{package.wars.temp}}" quiet="true" />
            </target>    

            <target name="package-dist-wars">
                <xsl:attribute name="depends">jar</xsl:attribute> 
                <delete dir="${{package.dist.root}}/package-dist" quiet="true"/>
                <mkdir dir="${{package.dist.root}}/package-dist"/>
                <copy todir="${{package.dist.root}}/package-dist">
                    <fileset dir="${{dist.dir}}"/>
                </copy>
                <mkdir dir="${{package.dist.root}}/package-dist/${{package.wars.folder}}"/>
        
                <copy todir="${{package.dist.root}}/package-dist/${{package.wars.folder}}">
                    <!--fileset dir="${{package.wars.temp}}" /-->
                    <fileset dir="${{package.wars.temp}}" >
                        <exclude name="**/server-instance.properties"/> 
                    </fileset>
                </copy>
                <!--delete dir="${{package.wars.temp}}" quiet="true"/-->
                <delete>
                    <fileset dir="${{package.wars.temp}}" >
                        <exclude name="**/server-instance.properties"/>
                    </fileset>    
                </delete>        
                <jar update="true" zipfile="${{package.dist.root}}/package-dist/${{package.dist.jar}}"  basedir="${{package.wars.temp}}" />                
                <delete dir="${{package.wars.temp}}" quiet="true"/>                
            </target>
            <target name="package-dist-unpacked-wars">
                <!--xsl:attribute name="depends">jar</xsl:attribute--> 
                <copy todir="${{package.dist.root}}/package-dist">
                    <fileset dir="${{dist.dir}}"/>
                </copy>
                <jar update="true" zipfile="${{package.dist.root}}/package-dist/${{package.dist.jar}}"  basedir="${{package.wars.temp}}" />                
                <delete dir="${{package.wars.temp}}" quiet="true"/>                
            </target>
        </project>

    </xsl:template>

    <!--xsl:template name="createPath">
        <xsl:param name="roots"/>
        <xsl:for-each select="$roots/j2seproject3:root">
            <xsl:if test="position() != 1">
                <xsl:text>:</xsl:text>
            </xsl:if>
            <xsl:text>${</xsl:text>
            <xsl:value-of select="@id"/>
            <xsl:text>}</xsl:text>
        </xsl:for-each>
    </xsl:template-->
    
</xsl:stylesheet>