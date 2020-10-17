#!/usr/bin/env groovy
package com.devops.jenkins.cmn
import com.devops.jenkins.cmn.*
import java.lang.String

class CmnStgs{
    private final Script scriptPL

        CmnStgs(Script scriptPL) {
        this.scriptPL = scriptPL
    }

    def cmnFuncs = new CmnFuncs(scriptPL)

    void stgReadParameters(projectName) {
        scriptPL.stage("Read Parameters"){
            def project = cmnFuncs.getProjectInfo(projectName)
            return project
        }
    }

    void stgCheckoutSCM(project){
        scriptPL.stage("Checkout SCM"){
            scriptPL.cleanWs()
            scriptPL.checkout scriptPL.scm
        }
    }


    void stgDeployUrbanCodeDeploy(project) {
        scriptPL.stage("Artifact Deploy Urban Code"){
            def projectUCD = cmnFuncs.getEnvObject(project)
            def vName = projectUCD.env + '_' + scriptPL.env.BUILD_NUMBER+'_ci'
            cmnFuncs.zipFile(vName, '.')
            cmnFuncs.deliveryUrbanCodeDeploy(project,vName,projectUCD)
            if(projectUCD.env != "prd")
            {
                cmnFuncs.deployUrbanCodeDeploy(project,vName,projectUCD)
            }            
        }
    }

    void stgDocker(project){
        scriptPL.stage('Build and upload docker image'){
            cmnFuncs.gitSCM(project)
            cmnFuncs.buildDockerImage(project)
            // cmnFuncs.loginDockerHub(project)
            // cmnFuncs.uploadDockerImage(project)
        }
    }    
}
