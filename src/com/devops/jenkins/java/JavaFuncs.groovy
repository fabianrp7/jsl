#!/usr/bin/env groovy
package com.devops.jenkins.java
import java.lang.String;
import groovy.io.*;
import com.devops.jenkins.cmn.*

class JavaFuncs{
    private final Script scriptPL

    JavaFuncs(Script scriptPL){
        this.scriptPL = scriptPL
    }

    void build(){    
      scriptPL.sh "mvn install -DskipTests"
    }

    void unitTest(){
      scriptPL.sh 'mvn test'
    }

    void analysisSonar(project) {    
      scriptPL.withSonarQubeEnv('sonarqube-interno') {
        scriptPL.sh 'mvn sonar:sonar -Dsonar.projectKey='+project.name+' -Dsonar.projectName='+project.name+' -Dsonar.login='+GlobalVars.tokenSonarQube
      }
     }
}