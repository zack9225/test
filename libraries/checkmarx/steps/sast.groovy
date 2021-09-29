void call() {
    stage('Static Analysis: Checkmarx') {
        String project = config.ProjectName
        project = project.replace('\\', '\\\\')
        String sastHigh = config.SASTHigh ?: '5'
        String sastMedium = config.SASTMedium ?: '5'
        String cxServer = config.CxServer ?: 'https://192.169.116.104'
        String userPreset = config.User_Preset ?: 'High and Medium'
        String lang = config.Language
        String preset = lang + " - " + userPreset
        
        inside_forge_pod 'cli-tools_checkmarx', {
            dir("${WORKSPACE}") {
                withChecks('Checkmarx Scan') {
                    withCredentials([
                        usernamePassword(credentialsId: config.CxCred,  passwordVariable: 'CHECKMARX_PASS', usernameVariable: 'CHECKMARX_UNAME')
                    ]) {
                        String script = """/opt/CxConsolePlugin/runCxConsole.sh scan -v \
-ProjectName \"$project\" \\
-CxServer \"$cxServer\" \\
-LocationType \"folder\" \\
-SASTHigh \"$sastHigh\" \\
-SASTMedium \"$sastMedium\" \\
-Preset \"$preset\" \\
-ReportXML \"cx_output.xml\" \\
-LocationPath \"${WORKSPACE}\" \\
-TrustedCertificates \\
-Incremental \\
-CxUser \"\$CHECKMARX_UNAME\" \\
-CxPassword \"\$CHECKMARX_PASS\""""

                        def statusCode = sh(script: script, returnStatus: true)

                        if (statusCode == 0 || statusCode > 5) {
                            sh "cp /opt/CxConsolePlugin/Checkmarx/Reports/cx_output.xml ${WORKSPACE}"
                            archiveArtifacts artifacts: "cx_output.xml"
                        }
                    }
                }
            }
        }
    }
}
