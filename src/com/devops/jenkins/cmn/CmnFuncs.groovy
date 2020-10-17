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
            scriptPL.dir("jenkinssharedlibrary"){
                scriptPL.git credentialsId: GlobalVars.giteaCredentialsId, url: GlobalVars.urlShareLibrary
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

    void saveArtifacts(project) {
        scriptPL.archiveArtifacts '*.zip'
    }

    void getSonarQubeQGate(project){
        scriptPL.withSonarQubeEnv('sonarqube-interno') {
            scriptPL.timeout(time: 10, unit: 'MINUTES') {            
                def qg = scriptPL.waitForQualityGate()
                return qg
            }
        }
    }

    //Email
    void sendEmail(subject,body,emails){
                scriptPL.emailext body: body,
                recipientProviders: [[$class: 'DevelopersRecipientProvider'], [$class: 'RequesterRecipientProvider']],
                subject: subject, to: emails 
    }

void analysisKiuwanNewProj(project){
    
    switch(scriptPL.env.JOB_BASE_NAME) {
        case "dev":
        case "desa":  
            scriptPL.echo 'Analisis:Kiuwan'
            // variable utilizada para identificar la linea base en Kiuwan
            scriptPL.withCredentials([scriptPL.usernamePassword(credentialsId: GlobalVars.kiuwanCredentialsId, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]){
                scriptPL.echo "Waiting " + GlobalVars.kiuwanTime + " seconds for kiuwan analysis to complete"
                scriptPL.sleep GlobalVars.kiuwanTime
                //Get kiuwan's last delivery
                def response = scriptPL.sh(script: 'curl --user "$USERNAME":"$PASSWORD" --proxy http://"$USERNAME":"$PASSWORD"@' + GlobalVars.proxyIp + ' ' + project.kiuwan.lastDelivery, returnStdout: true)
                def data = new JsonSlurperClassic().parseText(response)

                def applicationLastAnalises = scriptPL.sh (script: 'curl --user "$USERNAME":"$PASSWORD" --proxy http://"$USERNAME":"$PASSWORD"@' + GlobalVars.proxyIp + ' ' + project.kiuwan.lastAnalysis, returnStdout: true)
                //def applicationBaseAnalises = scriptPL.sh (script: 'curl --user "$USERNAME":"$PASSWORD" --proxy http://"$USERNAME":"$PASSWORD"@' + GlobalVars.proxyIp + ' ' + project.kiuwan.baseAnalysis, returnStdout: true)

                def lastAnalisesData = new JsonSlurperClassic().parseText(applicationLastAnalises)
                //def baseAnalisesData = new JsonSlurperClassic().parseText(applicationBaseAnalises)

                scriptPL.echo "Imprimir response LastAnalisesData"
                scriptPL.echo "${lastAnalisesData.Security.Vulnerabilities}"
                //scriptPL.echo "Imprimir response BaseAnalisesData"
                //scriptPL.echo "${baseAnalisesData.Security.Vulnerabilities}"

                // Suma de vulnerabilidades de las lineas bases: actual y ultima.
                def difVeryHigh = lastAnalisesData.Security.Vulnerabilities['VeryHigh']
                def difHigh = lastAnalisesData.Security.Vulnerabilities['High']
                def difNormal = lastAnalisesData.Security.Vulnerabilities['Normal']
                def difLow = lastAnalisesData.Security.Vulnerabilities['Low']

                scriptPL.echo "Result data: ${data}"
                scriptPL.echo "Result data[0]: ${data[0]}"
                scriptPL.echo "difVeryHigh: ${difVeryHigh}"
                scriptPL.echo "difHigh: ${difHigh}"
                scriptPL.echo "difNormal: ${difNormal}"
                scriptPL.echo "difLow: ${difLow}"

                if (data != null && data[0] != null) { 
                    //Print the result of last delivery and baseline
                    scriptPL.echo "Result: ${data[0].auditResult}\nDate(UTC): ${data[0].creationDate}"
                    //El segundo if es para notificarle a la celula, en caso de tener un delivery exitoso,
                    if (data[0].auditResult == "FAIL") {
                        scriptPL.error("Last Kiuwan delivery failed. Please check the Kiuwan results for this application")
                    }
                }
                    if (difVeryHigh > 0 || difHigh > 0 || difNormal > 0 || difLow > 0) {
                    scriptPL.error("La ultima linea base tiene vulnerabilidades. Por favor corregirlas y despues avisar a seguridad de la informacion.\n Se inyectaron ${difVeryHigh} Vulnerabilidades Muy altas, \n ${difHigh} Vulnerabilidades Altas, \n ${difNormal} Vulnerabilidades Normales, \n ${difLow} Vulnerabilidades Bajas")
                        }
                }   
            break
        case "test":
        case "master":
        break          
        }
           
    }
}


