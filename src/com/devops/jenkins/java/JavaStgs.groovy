#!/usr/bin/env groovy
package com.devops.jenkins.java
import java.lang.String

class JavaStgs{
    private final Script scriptPL

        JavaStgs(Script scriptPL) {
        this.scriptPL = scriptPL
    }

    def javaFuncs = new com.devops.jenkins.java.JavaFuncs(scriptPL)
    void stgBuild() {
        scriptPL.stage("Build"){
            javaFuncs.build()
        }
    }

    void stgTest() {
        scriptPL.stage("Unit Tests"){
            javaFuncs.unitTest()
        }
    }

    void stgSonarQubeAnalysis(project) {
        scriptPL.stage("Sonar Qube Analysis"){   
            javaFuncs.analysisSonar(project)
        }
    }
}