#!/usr/bin/env groovy
package com.devops.jenkins.cmn
import java.lang.String;
import groovy.io.*;
import groovy.json.JsonSlurperClassic;

class CmnFuncs{
    private final Script scriptPL

    CmnFuncs(Script scriptPL) {
        this.scriptPL = scriptPL
    }

    //Projects
    void getProjectInfo(projectName){
            scriptPL.cleanWs()
            def project
            scriptPL.dir("jsl"){
                scriptPL.git credentialsId: GlobalVars.gitCredentialsId, url: GlobalVars.urlShareLibrary
                def path = scriptPL.pwd()
                def pathJson = path + "/" + "resources/projects/projects.txt"
                def jsonSlurper = new JsonSlurperClassic()
                project = jsonSlurper.parse(new File(pathJson))
                assert project instanceof Map
                assert project
                project = project.Projects.find { p -> p.name == projectName }
                scriptPL.println project
            }
            scriptPL.cleanWs()
            return project
    }

    void getEnvObject(project){
        def projectUCD  = [:]
         switch (scriptPL.env.JOB_BASE_NAME) {
             case "dev":
             case "desa":
             projectUCD = project.ucd.dev
             break
             case "test":
             case "pruebas":
             projectUCD = project.ucd.test
             break
             case "master":
             projectUCD = project.ucd.prd
             break
            default:
            break
         }
         return projectUCD
    }

    void buildDockerImage(project){
         scriptPL.sh "docker image build -f Dockerfile . --tag codevopsti/" + project.name
    }

    void uploadDockerImage(project){
         scriptPL.sh "docker push codevopsti/" + project.name
    }

    void loginDockerHub(project){
         scriptPL.sh "docker login -u=" + GlobalVars.dockerHubUser + " -p=" + GlobalVars.dockerHubPass
    }

    void gitSCM(project){
         scriptPL.git branch: scriptPL.env.JOB_BASE_NAME, credentialsId: GlobalVars.giteaCredentialsId, url: project.repository
    }

    //SSH
    void zipFile(fileName, sourcePath){
        scriptPL.sh "zip -r " + fileName + ".zip " + sourcePath
    }
}


